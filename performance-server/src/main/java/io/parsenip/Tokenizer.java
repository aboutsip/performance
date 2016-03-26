package io.parsenip;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 *
 */
public class Tokenizer {

    /**
     * A single quote
     */
    public static final char SQUOT = '\'';

    /**
     * A double quote
     */
    public static final char DQUOT = '"';

    /**
     * A back slash character
     */
    public static final char BACK_SLASH = '\\';

    /**
     * A single space
     */
    public static final char SP = ' ';

    /**
     * A tab character
     */
    public static final char HTAB = '\t';

    /**
     * Split the supplied buffer into a list of strings.
     *
     * @param buffer the buffer to split
     * @param allowDoubleQuotedString whether or not to allow double quoted strings (i.e. something in
     *                                double quotes will be treated as a single token)
     * @param allowSingleQuotedString whether or not to allow single quoted strings (i.e. something in *
     *                                single quotes will be treated as a single token)
     * @return a list of strings
     * @throws ParseException if anything goes wrong while parsing. E.g., we allow double quoted strings
     * and there is a start quote but not an end one. That would yield a {@link ParseException}
     */
    public static List<String> split(final String buffer,
                                     final boolean allowDoubleQuotedString,
                                     final boolean allowSingleQuotedString) throws ParseException {

        final List<String> tokens = new ArrayList<>();

        String str = buffer.trim();
        while (!str.isEmpty()) {
            final Optional<String> token;
            final boolean yesQuotedString;

            if (allowDoubleQuotedString && isNext(0, DQUOT, buffer)) {
                token = Optional.of(getDoubleQuotedString(str));
                yesQuotedString = true;
            } else if (allowSingleQuotedString && isNext(0, SQUOT, buffer)) {
                token = Optional.of(getSingleQuotedString(str));
                yesQuotedString = true;
            } else {
                token = getNextToken(str);
                yesQuotedString = false;
            }

            token.ifPresent(tokens::add);
            str = str.substring(token.map(String::length).orElse(0) + (yesQuotedString ? 2 : 0), str.length()).trim();
        }

        return tokens;
    }

    public static List<String> split(final String buffer) throws ParseException {
        return split(buffer, false, false);
    }

    /**
     * Get the next token.
     *
     * Note, if the buffer starts with white space(s) this function will
     * NOT remote those first and as such, you will get back an empty
     * {@link Optional}.
     *
     * @param buffer
     * @return
     */
    public static Optional<String> getNextToken(final String buffer) {
        if (buffer == null || buffer.isEmpty()) {
            return Optional.empty();
        }

        String result = buffer;
        for (int i = 0; i < buffer.length(); ++i) {
            if(isNext(i, SP, buffer) || isNext(i, HTAB, buffer)) {
                result = buffer.substring(0, i);
                break;
            }
        }

        return result.isEmpty() ? Optional.empty() : Optional.of(result);
    }

    public static String getDoubleQuotedString(final String buffer) throws ParseException {
        return getQuotedString(DQUOT, buffer);
    }

    public static String getSingleQuotedString(final String buffer) throws ParseException {
        return getQuotedString(SQUOT, buffer);
    }

    private static String getQuotedString(final char quoteChar, final String buffer) throws ParseException {
        int index = 0;
        expect(index, quoteChar, buffer);
        while (++index < buffer.length()) {
            final char ch = buffer.charAt(index);
            if (ch == quoteChar) {
                return buffer.substring(1, index);
            } else if (ch == BACK_SLASH) {
                // skip ahead...
                ++index;
            }
        }

        throw new ParseException("Did not find the ending quote characther (" + quoteChar + ")", buffer.length());
    }

    /**
     * Check if the next character is the specified character.
     *
     * @param index
     * @param ch
     * @param buffer
     * @return
     */
    public static boolean isNext(int index, final char ch, final String buffer) {
        return buffer.charAt(index) == ch;
    }

    /**
     * Expect that the next character is the specified one.
     *
     * @param index the index to check.
     * @param ch the character that is expected.
     * @param buffer the buffer to check.
     * @throws ParseException in case we didn't find the expected character.
     */
    public static void expect(final int index, final char ch, final String buffer) throws ParseException {
        if (buffer.charAt(index) != ch) {
            throw new ParseException("Expected \"" + ch + "\"", index);
        }
    }

}
