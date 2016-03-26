package io.parsenip.impl;

import io.parsenip.Argument;

import java.util.List;
import java.util.Optional;
import java.util.function.Function;

/**
 *
 */
public class DefaultArgument<T> implements Argument<T> {

    @Override
    public Optional<String> getShortName() {
        return null;
    }

    @Override
    public Optional<String> getLongName() {
        return null;
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
            return null;
        }

        @Override
        public VariableArgumentStartStep withAtLeastOneArgument() {
            return null;
        }

        @Override
        public VariableArgumentStartStep withManyArguments() {
            return null;
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
        public <T> VariableArgumentOptionalStep<T> ofType(Class<T> type) {
            return null;
        }
    }

    private static class ConstantFinalBuilder<T> implements ConstantArgumentFinalStep<T> {

        private final Optional<String> shortName;
        private final Optional<String> longName;
        private final Optional<String> description;

        private final T valueWhenPresent;

        private T valueWhenAbsent;


        private ConstantFinalBuilder(final Optional<String> shortName,
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
            return new DefaultConstantArgument<>(shortName, longName, description, valueWhenPresent,
                    Optional.ofNullable(valueWhenAbsent));
        }
    }

    private static class InitialBuilder<T> implements ShortNameStep,
            LongNameStep, HelpStep, ArgumentStep, ConstantArgumentStep,
            VariableArgumentStartStep {
        @Override
        public ConstantArgumentStep withNoArguments() {
            return null;
        }

        @Override
        public VariableArgumentStartStep withSingleArgument() {
            return null;
        }

        @Override
        public VariableArgumentStartStep withAtLeastOneArgument() {
            return null;
        }

        @Override
        public VariableArgumentStartStep withManyArguments() {
            return null;
        }

        @Override
        public <T> ConstantArgumentFinalStep<T> withValueWhenPresent(T value) {
            return null;
        }

        @Override
        public HelpStep withLongName(String longName) {
            return null;
        }

        @Override
        public HelpStep withShortName(String shortName) {
            return null;
        }

        @Override
        public ArgumentStep withDescription(String description) {
            return null;
        }

        @Override
        public ArgumentStep withNoDescription() {
            return null;
        }

        @Override
        public <T> VariableArgumentOptionalStep<T> ofType(Class<T> type) {
            return null;
        }
    }
}
