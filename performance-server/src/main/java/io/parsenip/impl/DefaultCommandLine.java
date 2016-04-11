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
    final Map<Argument<?>, List<?>> arguments;

    public static Builder withCommandName(final String commandName) {
        return new Builder(commandName);
    }

    private DefaultCommandLine(final String commandName, final Map<Argument<?>, List<?>> arguments) {
        this.commandName = commandName;
        this.arguments = arguments;
    }

    @Override
    public String commandName() {
        return commandName;
    }

    @Override
    public <T> Optional<T> getValue(final Argument<T> argument) {
        return (Optional<T>)arguments.getOrDefault(argument, Collections.emptyList()).stream().findFirst();
    }

    @Override
    public <T> List<T> getValues(final Argument<T> argument) {
        return (List<T>)arguments.getOrDefault(argument, Collections.emptyList());
    }

    public static class Builder {

        private final String commandName;
        private final Map<Argument<?>, List<?>> arguments = new HashMap<>();

        public Builder(final String commandName) {
            this.commandName = commandName;
        }

        public Builder withArgument(final Argument<?> arg, final Object value) {
            final List values = arguments.computeIfAbsent(arg, key -> new ArrayList<Object>());
            values.add(value);
            return this;
        }

        public CommandLine build() {
            return new DefaultCommandLine(commandName, arguments);
        }
    }
}
