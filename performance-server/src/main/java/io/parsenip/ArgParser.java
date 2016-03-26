package io.parsenip;

import io.parsenip.impl.DefaultArgParser;

import java.util.List;

/**
 *
 */
public interface ArgParser {

    /**
     * Parse the supplied command line.
     *
     *
     *
     * @param cmdLine
     * @return
     */
    CommandLine parse(String cmdLine) throws ParseException;

    /**
     * Parse the supplied command line.
     *
     * @param args
     * @return
     */
    CommandLine parse(String[] args);

    CommandLine parse(List<String> args);

    /**
     * Start constructing a new parser for the named program.
     *
     * @param name the name of the program (in java you don't get the
     *                    name of the executable like you do in other languages
     *                    so you have to supply it yourself)
     * @return the next step in the build process, which in this case is to
     * configure the help (or skip it if you really really must)
     */
    static HelpStep forProgramNamed(final String name) {
        return DefaultArgParser.forProgramNamed(name);
    }

    // ----------------------------------------
    // Step 1 - Setup the help section, or skip it but
    //          since you really shouldn't, the builder
    //          will force you to explicitly say that you
    //          do not want to provide any help

    interface HelpStep {

        /**
         * The description (brief) of what this command does.
         *
         * @return the step for finalizing and building the actual {@link ArgParser}.
         */
        Builder withDescription(String description);

        /**
         * Are you really sure that you don't need a description?
         *
         * @return the step for finalizing and building the actual {@link ArgParser}.
         */
        Builder withNoDescription();
    }

    interface Builder {

        /**
         * If you choose to have the {@link ArgParser} parse the entire
         * raw command line through the method {@link ArgParser#parse(String)} you may
         * want to ensure that strings within quotes are preserved as one token and not
         * multiple (since by default it will just split the command line up based on
         * white space)
         *
         * @return
         */
        Builder withAllowDoubleQuotedStrings();

        /**
         * Same as {@link Builder#withAllowDoubleQuotedStrings()} but configures this
         * parser to also allow for single quoted strings.
         *
         * @return
         */
        Builder withAllowSingleQuotedStrings();

        /**
         * Add an argument.
         *
         * @param argument the {@link Argument}. If the argument is null, it is silently
         *                 ignored.
         * @return
         */
        Builder withArgument(Argument<?> argument);

        /**
         * By default the '-' character is the one that precedes optional arguments (e.g. -f, --force) but
         * you can change it to anything.
         *
         * @param chars
         * @return
         */
        Builder withPrefixChars(char... chars);

        /**
         * Build the parser.
         *
         * @return
         */
        ArgParser build();
    }

}
