package io.parsenip.impl;

import io.parsenip.Argument;
import io.parsenip.CommandLine;

import java.util.*;

import static io.parsenip.impl.PreConditions.assertArgument;
import static io.parsenip.impl.PreConditions.assertAtLeastOnePresent;

/**
 *
 */
public class DefaultCommandLine implements CommandLine {

    private final String commandName;
    private final Map<String, ArgumentHolder> shortNameHolder;
    private final Map<String, ArgumentHolder> longNameHolder;


    public static Builder withCommandName(final String commandName) {
        return new Builder(commandName);
    }

    private DefaultCommandLine(final String commandName, final Map<String, ArgumentHolder> shortNameHolder,
                               final Map<String, ArgumentHolder> longNameHolder) {
        this.commandName = commandName;
        this.shortNameHolder = shortNameHolder;
        this.longNameHolder = longNameHolder;
    }

    @Override
    public String commandName() {
        return commandName;
    }

    @Override
    public <T> Optional<T> getValue(final Class<T> type, final String argumentName) {
        final ArgumentHolder holder = shortNameHolder.getOrDefault(argumentName, longNameHolder.get(argumentName));
        if (holder == null || holder.values.isEmpty()) {
            return Optional.empty();
        }

        final Object value = holder.values.get(0);
        if (!type.isAssignableFrom(value.getClass())) {
            throw new IllegalArgumentException("The value of argument \"" + argumentName + "\" is not of type "
                    + type.getName() + " but rather of type " + value.getClass().getName());
        }
        return Optional.of((T)value);
    }

    @Override
    public <T> Optional<List<T>> getValues(Class<T> type, String argumentName) {
        return null;
    }

    @Override
    public Optional<Boolean> getBoolean(final String argumentName) {
        return getValue(Boolean.class, argumentName);
    }

    @Override
    public Optional<Integer> getInteger(final String argumentName) {
        return getValue(Integer.class, argumentName);
    }

    @Override
    public Optional<Double> getDouble(final String argumentName) {
        return getValue(Double.class, argumentName);
    }

    @Override
    public Optional<Float> getFloat(final String argumentName) {
        return getValue(Float.class, argumentName);
    }

    @Override
    public Optional<Byte> getByte(final String argumentName) {
        return getValue(Byte.class, argumentName);
    }

    @Override
    public Optional<String> getString(final String argumentName) {
        return getValue(String.class, argumentName);
    }

    @Override
    public Optional<Character> getChar(final String argumentName) {
        return getValue(Character.class, argumentName);
    }

    public static class Builder {

        private final String commandName;
        private final Map<String, ArgumentHolder> shortNameHolder = new HashMap<>();
        private final Map<String, ArgumentHolder> longNameHolder = new HashMap<>();

        public Builder(final String commandName) {
            this.commandName = commandName;
        }

        public Builder withArgumentInternal(final Optional<String> shortName, final Optional<String> longName, final Class<?> type, final List<?> values) {
            assertAtLeastOnePresent("You must specify either the short or long name or both", shortName, longName);
            PreConditions.assertNotEmpty(values, "You must specify at least one value");

            // check so that all values in the list are indeed of the specified type
            assertArgument(values.size() ==
                    values.stream().filter(value -> type.isAssignableFrom(value.getClass())).count(),
                    "Not all values in the list were of type " + type);

            final ArgumentHolder holder = new ArgumentHolder(type, values);
            shortName.ifPresent(name -> shortNameHolder.put(name, holder));
            longName.ifPresent(name -> longNameHolder.put(name, holder));
            return this;
        }

        public Builder withArgument(final Optional<String> shortName, final Optional<String> longName, final Class<?> type, final Object value) {
            final List<Object> values = new ArrayList<>();
            values.add(value);
            return withArgumentInternal(shortName, longName, type, values);
        }

        public CommandLine build() {
            return new DefaultCommandLine(commandName, shortNameHolder, longNameHolder);
        }

    }

    private static class ArgumentHolder {

        private final Class<?> type;
        private final List<?> values;

        private ArgumentHolder(final Class<?> type, final List<?> values) {
            this.type = type;
            this.values = values;
        }
    }
}
