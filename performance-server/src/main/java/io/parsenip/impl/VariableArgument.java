package io.parsenip.impl;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

/**
 *
 */
public class VariableArgument<T> extends DefaultArgument<T> {

    private final List<?> validChoices;
    private final boolean isRequired;

    public VariableArgument(final Class<T> type, final Optional<String> shortName, final Optional<String> longName,
                            final Optional<String> description, final Optional<T> defaultValue,
                            final List<T> validChoices, final boolean isRequired) {
        super(type, shortName, longName, description, defaultValue);
        this.validChoices = validChoices != null ? validChoices : Collections.emptyList();
        this.isRequired = isRequired;
    }

    @Override
    public boolean isValueAccepted(final String token) {
        try {
            final T value = valueOf(token);
            return validChoices.isEmpty() || validChoices.contains(value);
        } catch (final IllegalArgumentException e) {
            return false;
        }
    }

    @Override
    public boolean isRequired() {
        return isRequired;
    }

}
