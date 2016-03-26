package io.parsenip.impl;

import io.parsenip.Argument;
import io.parsenip.ConstantArgument;

import java.util.Optional;

/**
 *
 */
public class DefaultConstantArgument<T> implements ConstantArgument<T> {

    private final Optional<String> shortName;
    private final Optional<String> longName;
    private final Optional<String> description;
    private final T valueWhenPresent;
    private final Optional<T> valueWhenAbsent;

    public DefaultConstantArgument(final Optional<String> shortName,
                                   final Optional<String> longName,
                                   final Optional<String> description,
                                   final T valueWhenPresent,
                                   final Optional<T> valueWhenAbsent) {
        this.shortName = shortName;
        this.longName = longName;
        this.description = description;
        this.valueWhenPresent = valueWhenPresent;
        this.valueWhenAbsent = valueWhenAbsent;
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
    public T getValueWhenPresent() {
        return valueWhenPresent;
    }

    @Override
    public Optional<T> getValueWhenAbsent() {
        return valueWhenAbsent;
    }
}
