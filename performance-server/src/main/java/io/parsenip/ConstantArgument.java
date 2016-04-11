package io.parsenip;

import java.util.Optional;

/**
 *
 */
public interface ConstantArgument<T> extends Argument<T> {

    T getValueWhenPresent();

    Optional<T> getValueWhenAbsent();

    @Override
    default boolean isConstant() {
        return true;
    }

    @Override
    default ConstantArgument<T> toConstantArgument() {
        return this;
    }

    @Override
    default Optional<T> getDefaultValue() {
        return getValueWhenAbsent();
    }

}
