package io.parsenip.impl;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

/**
 * Created by jonas on 3/14/16.
 */
public class PreConditions {

    /**
     * Make sure that at least one of the optionals is present.
     *
     * @param msg
     * @param optionals
     * @throws IllegalArgumentException
     */
    public static void assertAtLeastOnePresent(final String msg, Optional<?> ... optionals) throws IllegalArgumentException {
        Stream.of(optionals).filter(Optional::isPresent).findFirst().orElseThrow(() -> new IllegalArgumentException(msg));
    }

    public static <T> T assertNotNull(final T reference, final String msg) throws IllegalArgumentException {
        if (reference == null) {
            throw new IllegalArgumentException(msg);
        }
        return reference;
    }

    public static <T> T assertNotNull(final T reference) throws IllegalArgumentException {
        if (reference == null) {
            throw new IllegalArgumentException("Value cannot be null");
        }
        return reference;
    }

    /**
     * Check if a string is empty, which includes null check.
     *
     * @param string
     * @return true if the string is either null or empty
     */
    public static boolean checkIfEmpty(final String string) {
        return string == null || string.isEmpty();
    }

    public static boolean checkIfNotEmpty(final String string) {
        return !checkIfEmpty(string);
    }

    public static String assertNotEmpty(final String reference, final String msg) throws IllegalArgumentException {
        if (reference == null || reference.isEmpty()) {
            throw new IllegalArgumentException(msg);
        }
        return reference;
    }

    /**
     * Assert so that any collection type isn't null or empty (so lists etc)
     *
     * @param reference
     * @param msg
     * @param <T>
     * @return
     * @throws IllegalArgumentException
     */
    public static <T> List<T> assertNotEmpty(final List<T> reference, final String msg) throws IllegalArgumentException {
        if (reference == null || reference.isEmpty()) {
            throw new IllegalArgumentException(msg);
        }
        return reference;
    }

    public static void assertArgument(final boolean expression, final String msg) throws IllegalArgumentException {
        if (!expression) {
            throw new IllegalArgumentException(msg);
        }
    }
}
