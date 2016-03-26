package io.parsenip;

import org.junit.Test;

import java.text.ParseException;
import java.util.List;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.fail;

/**
 *
 */
public class TokenizerTest {

    @Test
    public void testSplit() throws Exception {
        // basic
        assertSplit("hello world", true, true, "hello", "world");
        assertSplit("hello", true, true, "hello");
        assertSplit("hello world again", true, true, "hello", "world", "again");
        assertSplit("", true, true);

        // quotes
        assertSplit("'hello world'", true, true, "hello world");
        assertSplit("\"hello world\"", true, true, "hello world");
        assertSplit("\"hello\"", true, true, "hello");
        assertSplit("'hello'", true, true, "hello");

        // spaces & tabs
        assertSplit("    hello      ", true, true, "hello"); // spaces
        assertSplit("       hello      ", true, true, "hello"); // tab
        assertSplit("   hello world", true, true, "hello", "world"); // spaces + two tokens
        assertSplit("hello world    ", true, true, "hello", "world"); // spaces + two tokens
        assertSplit("hello        world", true, true, "hello", "world"); // space in the middle
        assertSplit("hello       world", true, true, "hello", "world"); // tabs in the middle
        assertSplit("       hello       world", true, true, "hello", "world"); // tabs and space in the beginning, tabs in the middle

        // quotes when they are not allowed
        assertSplit("'hello world'", false, false, "'hello",  "world'"); // note how the final tokens have ' still in them
        assertSplit("\"hello world\"", false, false, "\"hello", "world\""); // and these ones still has the "
        assertSplit("   \"hello world\"", false, false, "\"hello", "world\""); // with spaces (shouldn't matter of course)
        assertSplit("  \"    hello world\"", false, false, "\"", "hello", "world\""); // the " will be its own token
    }

    private static void assertSplit(final String toSplit,
                                    final boolean allowDoubleQuotedString,
                                    final boolean allowSingleQuotedString,
                                    final String ... expected) throws ParseException {
        final List<String> result = Tokenizer.split(toSplit, allowDoubleQuotedString, allowSingleQuotedString);
        assertThat(result.size(), is(expected.length));

        for (int i = 0; i < result.size(); ++i) {
            assertThat(result.get(i), is(expected[i]));
        }
    }


    @Test
    public void testNextToken() throws Exception {
        assertThat(Tokenizer.getNextToken("hello").get(), is("hello"));

        // single space
        assertThat(Tokenizer.getNextToken("hello world").get(), is("hello"));

        // many spaces
        assertThat(Tokenizer.getNextToken("hello       world").get(), is("hello"));

        // tab
        assertThat(Tokenizer.getNextToken("hello    world").get(), is("hello"));

        // empty string
        assertThat(Tokenizer.getNextToken("").isPresent(), is(false));

        // white space in the beginning...
        assertThat(Tokenizer.getNextToken("   hello").isPresent(), is(false));
    }

    @Test
    public void testDoubleQuote() throws Exception {
        assertThat(Tokenizer.getDoubleQuotedString("\"Hello World\""), is("Hello World"));
        assertThat(Tokenizer.getDoubleQuotedString("\"Hello\""), is("Hello"));
        assertThat(Tokenizer.getDoubleQuotedString("\"\""), is(""));
    }

    @Test
    public void testDoubleQuoteMissingEndQuote() throws Exception {
        assertFailWithParseException(Tokenizer::getDoubleQuotedString, "\"Missing EndQuote");
        assertFailWithParseException(Tokenizer::getDoubleQuotedString, "Missing the beginning quote");
    }

    @Test
    public void testSingleQuote() throws Exception {
        assertThat(Tokenizer.getDoubleQuotedString("'Hello World'"), is("Hello World"));
        assertThat(Tokenizer.getDoubleQuotedString("'Hello'"), is("Hello"));
        assertThat(Tokenizer.getDoubleQuotedString("''"), is(""));
    }

    @Test
    public void testSingleQuoteMissingEndQuote() throws Exception {
        assertFailWithParseException(Tokenizer::getSingleQuotedString, "'Missing EndQuote");
        assertFailWithParseException(Tokenizer::getSingleQuotedString, "Missing the beginning quote");
    }

    @FunctionalInterface
    interface FunctionThatThrowsException<T, R> {
        R apply(T t) throws ParseException;
    }

    private static void assertFailWithParseException(final FunctionThatThrowsException<String, String> f, final String toParse) throws ParseException {
        try {
            f.apply(toParse);
            fail("Expected a ParseException");
        } catch (final ParseException e) {
            // expected
        }
    }
}
