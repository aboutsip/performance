package io.parsenip;

import java.util.List;
import java.util.Optional;

/**
 *
 */
public interface CommandLine {

    /**
     * The name of the command.
     *
     * @return
     */
    String commandName();

    // <T> Optional<T> getValue(Class<T> type, String argumentName);
    <T> Optional<T> getValue(Argument<T> argument);

    // <T> List<T> getValues(Class<T> type, String argumentName);
    <T> List<T> getValues(Argument<T> argument);

    // Optional<Boolean> getBoolean(String argumentName);
    // Optional<Integer> getInteger(String argumentName);
    // Optional<Double> getDouble(String argumentName);
    // Optional<Float> getFloat(String argumentName);
    // Optional<Byte> getByte(String argumentName);
    // Optional<String> getString(String argumentName);
    // Optional<Character> getChar(String argumentName);

}
