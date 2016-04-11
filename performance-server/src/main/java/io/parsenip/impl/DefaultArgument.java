package io.parsenip.impl;

import io.parsenip.Argument;

import java.util.*;
import java.util.function.Function;

/**
 *
 */
public abstract class DefaultArgument<T> implements Argument<T> {

    private final Optional<String> shortName;
    private final Optional<String> longName;
    private final Optional<String> description;
    private final Optional<T> defaultValue;
    private final Class<T> type;

    private static final Map<Class<?> , Function<String, ?>> parsers = new HashMap<>();

    static {
        parsers.put(Boolean.class, s -> {
            if ("true".equalsIgnoreCase(s)) {
                return true;
            }
            if ("false".equalsIgnoreCase(s)) {
                return false;
            }
            throw new IllegalArgumentException("Cannot convert " + s + " into a Boolean");
        });
        parsers.put(String.class, s -> s);
        parsers.put(Integer.class, Integer::parseInt);
        parsers.put(Double.class, Double::parseDouble);
        parsers.put(Float.class, Float::parseFloat);
    }


    public DefaultArgument(final Class<T> type,
                           final Optional<String> shortName,
                           final Optional<String> longName,
                           final Optional<String> description,
                           final Optional<T> defaultValue ) {
        this.type = type;
        this.shortName = shortName;
        this.longName = longName;
        this.description = description;
        this.defaultValue = defaultValue;
    }

    @Override
    public Class<T> getType() {
        return type;
    }

    @Override
    public T valueOf(final String value) throws IllegalArgumentException {
        if (value == null || value.isEmpty()) {
            throw new IllegalArgumentException("The value cannot be null or empty");
        }

        try {
            return (T) parsers.get(type).apply(value);
        } catch (final NullPointerException e) {
            throw new IllegalArgumentException("There is no registered parser for type " + type.getName());
        }
    }


    @Override
    public Optional<String> getShortName() {
        return shortName;
    }

    @Override
    public Optional<String> getLongName() {
        return longName;
    }

    @Override
    public Optional<T> getDefaultValue() {
        return defaultValue;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o){
            return true;
        }

        try {
            final DefaultArgument<?> that = (DefaultArgument<?>) o;
            return shortName.equals(that.shortName) && longName.equals(that.longName);

        } catch (final ClassCastException e) {
            return false;
        }
    }

    @Override
    public int hashCode() {
        int result = shortName.hashCode();
        result = 31 * result + longName.hashCode();
        return result;
    }

    public static ShortNameStep withLongName(final String longName) {
        final NameBuilder builder = new NameBuilder();
        builder.withLongName(longName);
        return builder;
    }

    public static LongNameStep withShortName(final String shortName) {
        final NameBuilder builder = new NameBuilder();
        builder.withShortName(shortName);
        return builder;
    }

    private static class NameBuilder implements ShortNameStep, LongNameStep {

        private String shortName;
        private String longName;
        private String description;

        @Override
        public HelpStep withLongName(final String longName) {
            this.longName = longName;
            return this;
        }

        @Override
        public HelpStep withShortName(final String shortName) {
            this.shortName = shortName;
            return this;
        }

        @Override
        public ArgumentStep withDescription(final String description) {
            this.description = description == null || description.isEmpty() ? null : description;
            return new SelectArgumentBuilder(shortName, longName, description);
        }

        @Override
        public ArgumentStep withNoDescription() {
            this.description = null;
            return new SelectArgumentBuilder(shortName, longName, description);
        }
    }

    private static class SelectArgumentBuilder implements ArgumentStep, ConstantArgumentStep, VariableArgumentStartStep  {

        private final Optional<String> shortName;
        private final Optional<String> longName;
        private final Optional<String> description;

        private int minNoOfArguments = 0;
        private int maxNoOfArguments = 0;

        private SelectArgumentBuilder(final String shortName, final String longName, final String description) {
            this.shortName = Optional.ofNullable(shortName);
            this.longName = Optional.ofNullable(longName);
            this.description = Optional.ofNullable(description);
        }

        // ----------------------------------------------------------------------
        // --- ArgumentStep builder
        // ----------------------------------------------------------------------

        @Override
        public ConstantArgumentStep withNoArguments() {
            return this;
        }

        @Override
        public VariableArgumentStartStep withSingleArgument() {
            minNoOfArguments = 1;
            maxNoOfArguments = 1;
            return this;
        }

        @Override
        public VariableArgumentStartStep withAtLeastOneArgument() {
            minNoOfArguments = 1;
            maxNoOfArguments = Integer.MAX_VALUE;
            return this;
        }

        @Override
        public VariableArgumentStartStep withZeroOrMoreArguments() {
            minNoOfArguments = 0;
            maxNoOfArguments = Integer.MAX_VALUE;
            return this;
        }

        // ----------------------------------------------------------------------
        // --- ConstantArgumentStep builder
        // ----------------------------------------------------------------------

        @Override
        public <T> ConstantArgumentFinalStep<T> withValueWhenPresent(final T value) {
            if (value == null) {
                throw new IllegalArgumentException("The value when present cannot be null");
            }

            return new ConstantFinalBuilder<>(shortName, longName, description, value);
        }

        // ----------------------------------------------------------------------
        // --- VariableArgumentStartStep builder
        // ----------------------------------------------------------------------

        @Override
        public <T> VariableArgumentOptionalStep<T> ofType(final Class<T> type) {
            return new VariableArgumentOptionalStepBuilder<>(type, shortName, longName,
                    description, minNoOfArguments, maxNoOfArguments);
        }
    }

    private static class VariableArgumentOptionalStepBuilder<T> implements VariableArgumentOptionalStep<T> {
        private final Optional<String> shortName;
        private final Optional<String> longName;
        private final Optional<String> description;

        private final Class<T> type;

        private final int minNoOfArguments;
        private final int maxNoOfArguments;

        private T defaultvalue;

        private List<T> validChoices = new ArrayList<>();

        private boolean isRequired = false;

        private Function<T, T> onArgumentFunction;

        private VariableArgumentOptionalStepBuilder(final Class<T> type, final Optional<String> shortName,
                                                    final Optional<String> longName,
                                                    final Optional<String> description,
                                                    final int minNoOfArguments,
                                                    final int maxNoOfArguments) {
            this.type = type;
            this.shortName = shortName;
            this.longName = longName;
            this.description = description;
            this.minNoOfArguments = minNoOfArguments;
            this.maxNoOfArguments = maxNoOfArguments;
        }

        @Override
        public VariableArgumentOptionalStep<T> withDefaultValue(final T value) {
            this.defaultvalue = value;
            return this;
        }

        @Override
        public VariableArgumentOptionalStep<T> withChoice(final T value) {
            if (value != null) {
                validChoices.add(value);
            }
            return this;
        }

        @Override
        public VariableArgumentOptionalStep<T> withChoices(final T... values) {
            if (values != null) {
                Arrays.stream(values).filter(value -> value != null).forEach(validChoices::add);
            }
            return this;
        }

        @Override
        public VariableArgumentOptionalStep<T> withChoices(final List<T> values) {
            if (values != null) {
                values.stream().filter(value -> value != null).forEach(validChoices::add);
            }
            return this;
        }

        @Override
        public VariableArgumentOptionalStep<T> onArgument(final Function<T, T> f) throws IllegalArgumentException {
            onArgumentFunction = f;
            return this;
        }

        @Override
        public VariableArgumentOptionalStep<T> isRequired() {
            this.isRequired = true;
            return this;
        }

        @Override
        public VariableArgumentOptionalStep<T> isOptional() {
            this.isRequired = false;
            return this;
        }

        @Override
        public Argument<T> build() {
            return new VariableArgument<>(type, shortName, longName, description, Optional.ofNullable(defaultvalue),
                    validChoices, isRequired);
        }
    }

    private static class ConstantFinalBuilder<T> implements ConstantArgumentFinalStep<T> {

        private final Optional<String> shortName;
        private final Optional<String> longName;
        private final Optional<String> description;

        private final T valueWhenPresent;

        private T valueWhenAbsent;


        private ConstantFinalBuilder( final Optional<String> shortName,
                                     final Optional<String> longName,
                                     final Optional<String> description,
                                     final T valueWhenPresent) {
            this.shortName = shortName;
            this.longName = longName;
            this.description = description;
            this.valueWhenPresent = valueWhenPresent;
        }

        @Override
        public ConstantArgumentFinalStep<T> withValueWhenAbsent(T value) {
            this.valueWhenAbsent = value;
            return this;
        }

        @Override
        public Argument<T> build() {
            return new DefaultConstantArgument<>((Class<T>)valueWhenPresent.getClass(), shortName, longName, description, valueWhenPresent,
                    Optional.ofNullable(valueWhenAbsent));
        }
    }
}
