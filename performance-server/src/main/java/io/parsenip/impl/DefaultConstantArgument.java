package io.parsenip.impl;

import io.parsenip.Argument;
import io.parsenip.ConstantArgument;

import java.util.Optional;

/**
 *
 */
public class DefaultConstantArgument<T> extends DefaultArgument<T> implements ConstantArgument<T> {

    private final T valueWhenPresent;

    public DefaultConstantArgument(final Class<T> type,
                                   final Optional<String> shortName,
                                   final Optional<String> longName,
                                   final Optional<String> description,
                                   final T valueWhenPresent,
                                   final Optional<T> valueWhenAbsent) {
        super(type, shortName, longName, description, valueWhenAbsent);
        this.valueWhenPresent = valueWhenPresent;
    }

    @Override
    public T getValueWhenPresent() {
        return valueWhenPresent;
    }

    @Override
    public Optional<T> getValueWhenAbsent() {
        return getDefaultValue();
    }

}
