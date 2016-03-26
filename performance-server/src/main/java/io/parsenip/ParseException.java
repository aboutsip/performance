package io.parsenip;

/**
 * Copy of the java.text.ParseException but one that extends {@link RuntimeException}
 * instead.
 */
public class ParseException extends RuntimeException {

    private int errorOffset;

    public ParseException(String s, int errorOffset) {
        super(s);
        this.errorOffset = errorOffset;
    }

    public int getErrorOffset () {
        return errorOffset;
    }
}
