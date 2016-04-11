package io.parsenip;

import io.parsenip.impl.DefaultArgument;

import java.util.List;
import java.util.Optional;
import java.util.function.Function;

/**
 * 1. Name step - specify the name of the Option
 * 2. Help Step - specify the help for the Option
 * 3. Argument Steps - specify if the option takes any arguments and if so, what arguments it takes (including type)
 * 4. Build Step
 */
public interface Argument<T> {

    Optional<String> getShortName();

    Optional<String> getLongName();

    /**
     * Whether or not this argument is a constant argument. I.e. it doesn't
     * take any values itself but if the option is given on the command line
     * it will imply a specific value.
     *
     * @return
     */
    default boolean isConstant() {
        return false;
    }

    /**
     * Whether or not this argument is required
     * to specify on the command line or an error
     * will occur when parsing.
     *
     * @return
     */
    default boolean isRequired() {
        return false;
    }

    /**
     * An {@link Argument} may have a default value specified.
     *
     * @return
     */
    default Optional<T> getDefaultValue() {
        return Optional.empty();
    }

    /**
     * Check if the raw unparsed token would be accepted by this {@link Argument}.
     * The {@link Argument} would first try to convert the raw string into the
     * correct type and if that succeeds, checks against its internal, if present,
     * list of acceptable choices. If all matches then true is returned, otherwise false.
     *
     * @param token
     * @return
     */
    default boolean isValueAccepted(final String token) {
        return false;
    }

    Class<T> getType();

    T valueOf(final String value) throws IllegalArgumentException;

    /**
     * If this {@link Argument} is indeed a {@link ConstantArgument} then you can cast it
     * using this method.
     *
     * @return
     */
    default ConstantArgument<T> toConstantArgument() {
        throw new ClassCastException("Unable to cast " + this.getClass().getName()
                + " into a " + ConstantArgument.class.getName());
    }

    /**
     * Two {@link Argument}s are considered equal if their name matches (so the
     * value is NOT taken into consideration).
     *
     * @param other
     * @return
     */
    @Override
    boolean equals(Object other);

    @Override
    int hashCode();

    /**
     * Start building a new {@link Argument} with the specified
     * long name.
     *
     * @param longName the long name of the {@link Argument}, such as
     *                 "--type", "type" etc.
     * @return the next step in the builder chain, which in this case
     * will give you the chance to also (optionally) specify a short
     * name for this {@link Argument}.
     */
    static ShortNameStep withLongName(final String longName) {
        return DefaultArgument.withLongName(longName);
    }

    /**
     * Start building a new {@link Argument} with the specified
     * short name.
     *
     * @param shortName the short name of the {@link Argument}, such as
     *                 "-t"
     * @return the next step in the builder chain, which in this case
     * will give you the chance to also (optionally) specify a long
     * name for this {@link Argument}.
     */
    static LongNameStep withShortName(final String shortName) {
        return DefaultArgument.withShortName(shortName);
    }


    // ----------------------------------------
    // Step 1 - Specify the name
    //

    interface LongNameStep extends HelpStep {
        HelpStep withLongName(String longName);
    }

    interface ShortNameStep extends HelpStep {
        HelpStep withShortName(String shortName);
    }

    // ----------------------------------------
    // Step 2 - Setup the help section (or skip it)
    //

    interface HelpStep {

        /**
         * Short description of the purpose of the argument.
         *
         * @param description the brief description
         * @return the next step in the builder process
         */
        ArgumentStep withDescription(String description);

        /**
         * If you don't want a description you really have to
         * say so. You really should have one and this at least
         * forces you to contemplate your life choices :-)
         *
         * @return the next step in the builder process
         */
        ArgumentStep withNoDescription();
    }

    // ------------------------------------------------------------------------
    // Step 3 - Specify what arguments are available, which are done
    //          in two sub-steps. One for constants and one for variables.
    //

    interface ArgumentStep {

        /**
         * Our option doesn't take any arguments. Hence, this option is a
         * "constant" which is often used as a flag parameter (when used with the boolean type).
         *
         * @return
         */
        ConstantArgumentStep withNoArguments();

        /**
         * Our option takes a single argument.
         */
        VariableArgumentStartStep withSingleArgument();

        /**
         * Our option takes at least one argument. May take more.
         */
        VariableArgumentStartStep withAtLeastOneArgument();

        /**
         * Our option takes many arguments.
         */
        VariableArgumentStartStep withZeroOrMoreArguments();
    }

    // ------------------------------------------------------------------------
    // Step 3a - For the Constant arguments, specify the
    //           value to be present when option exists etc
    //
    interface ConstantArgumentStep {

        /**
         * Every constant argument must have a a value associated with it if
         * it indeed present on the command line.
         *
         * @param value the value to present when the argument is present.
         * @return
         * @throws IllegalArgumentException in case the supplied value is null.
         */
        <T> ConstantArgumentFinalStep<T> withValueWhenPresent(T value) throws IllegalArgumentException;
    }

    interface ConstantArgumentFinalStep<T> extends Build<T> {

        /**
         * A value to associate with the constant argument when it has NOT
         * been specified. If you specify this value then this constant
         * argument will always be present when queried through the command line.
         *
         * @param value
         * @return
         */
        ConstantArgumentFinalStep<T> withValueWhenAbsent(T value);
    }

    // ------------------------------------------------------------------------
    // Step 3b - For those arguments with a variable number of arguments
    //           we will force you to first specify the type. Now,
    //           we could figure out the type anyway depending on what the
    //           user chooses to do but that is not always the case so we'll just
    //           force the user always to do it, plus it reads nicer when typing
    //           everything out.
    //
    interface VariableArgumentStartStep {
        <T> VariableArgumentOptionalStep<T> ofType(Class<T> type);
    }

    interface VariableArgumentOptionalStep<T> extends Build<T> {

        /**
         * If the {@link Argument} isn't specified on the command line and you specify
         * a default value, that value will be produced instead (just as if the user
         * had specified that value on the command line).
         *
         * Note, if you specify this {@link Argument} to be required, then specifying a default
         * value doesn't make much sense, does it...
         *
         * @param value
         * @return
         */
        VariableArgumentOptionalStep<T> withDefaultValue(T value);

        VariableArgumentOptionalStep<T> withChoice(T value);
        VariableArgumentOptionalStep<T> withChoices(T... values);
        VariableArgumentOptionalStep<T> withChoices(List<T> values);

        VariableArgumentOptionalStep<T> onArgument(Function<T, T> f) throws IllegalArgumentException;

        /**
         * This option is required. If it is missing from the command line an exception will
         * be thrown. By default, all options are optional.
         *
         * @return
         */
        VariableArgumentOptionalStep<T> isRequired();

        /**
         * Indicate that this {@link Argument} is optional, which it is by default but sometimes
         * it is nice to be explicit since then you don't need to remember what the default
         * behavior is.
         *
         * @return
         */
        VariableArgumentOptionalStep<T> isOptional();
    }

    /**
     * The final build step at which time we must have figured
     * out the type...
     *
     * @param <T>
     */
    interface Build<T> {

        /**
         * Build the argument
         *
         * @return
         */
        Argument<T> build();
    }


}
