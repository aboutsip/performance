package io.parsenip.impl;

import com.fasterxml.jackson.dataformat.yaml.snakeyaml.scanner.Constant;
import com.google.common.collect.Lists;
import io.parsenip.*;

import java.util.*;
import java.util.stream.Collectors;

/**
 *
 */
public class DefaultArgParser implements ArgParser {

    private final String program;

    private final boolean allowDoubleQuotes;
    private final boolean allowSingleQuotes;

    private final Optional<String> description;

    private final List<Argument<?>> arguments;

    private final List<Character> allowedChars;

    private DefaultArgParser(final String program,
                             final Optional<String> description,
                             final boolean allowDoubleQuotes,
                             final boolean allowSingleQuotes,
                             final List<Argument<?>> arguments,
                             final List<Character> allowedChars) {
        this.program = program;
        this.allowDoubleQuotes = allowDoubleQuotes;
        this.allowSingleQuotes = allowSingleQuotes;
        this.description = description;
        this.arguments = arguments;
        this.allowedChars = allowedChars;
    }

    @Override
    public CommandLine parse(final String cmdLine) throws ParseException {
        return parse(Tokenizer.split(cmdLine, allowDoubleQuotes, allowSingleQuotes));
    }

    @Override
    public CommandLine parse(final String[] args) {
        return parse(Arrays.asList(args));
    }

    @Override
    public CommandLine parse(final List<String> args) {
        final List<String> tokens = checkFirstArgument(program, args);
        final ParseState state = new ParseState();
        state.commandLine = DefaultCommandLine.withCommandName(program);

        // if the first argument was the name of the program we have
        // consumed it so we add that number of characters to the count
        // of processed chars.
        if (args.size() != tokens.size()) {
            state.processedChars += args.get(0).length() + 1;
        }

        tokens.forEach(t -> processToken(state, t));
        return state.commandLine.build();
    }

    private class ParseState {

        Argument<?> currentArgument;

        /**
         * Keep track of the contant arguments that we did see because
         * the ones that weren't present may have a default value when
         * not present and if so, they will be added to the final state
         * of the {@link CommandLine} as if they were present.
         */
        List<ConstantArgument<?>> presentConstants = new ArrayList<>();

        /**
         * Where we are in the processing of characters. Only
         * used for being able to point out exactly where the
         * parsing failed.
         */
        int processedChars = 0;

        DefaultCommandLine.Builder commandLine;

    }

    private void processToken(final ParseState state, final String token) {
        final boolean isOptionalChar = isOptionalChar(token.charAt(0));

        // long optional token. Note, still doable to just have '--'.
        // will be checked next
        if (token.length() >= 2 && isOptionalChar && isOptionalChar(token.charAt(1))) {
            processLongOptionalArgument(state, token);
        } else if (isOptionalChar){
            processShortOptionalArgument(state, token);
        }
    }

    private void processLongOptionalArgument(final ParseState state, final String token) {

    }

    private void processShortOptionalArgument(final ParseState state, final String token) {
        if (token.length() < 2) {
            throw new ParseException("Illegal token. Expected the name of the parameter after the '"
                    + token.charAt(0) + "'", state.processedChars);
        }

        final String initialChar = token.substring(0, 1);

        for (int i = 1; i < token.length(); ++ i) {
            final char opt = token.charAt(i);
            final String name = initialChar + opt;
            final Argument<?> arg = findArgument(name).orElseThrow(() ->
                    new ParseException("Unable to find an argument named " + name, state.processedChars));

            if (arg.isConstant()) {
                final Object value = arg.toConstantArgument().getValueWhenPresent();
                state.commandLine.withArgument(arg.getShortName(), arg.getLongName(), value.getClass(), value);
                state.presentConstants.add(arg.toConstantArgument());

                // a constant argument will not have a value and therefore
                // we are no long working on a particular argument so
                // reset it.
                state.currentArgument = null;
            } else if (i < token.length()) {
                throw new ParseException("Optional argument \"" + name + "\" requires a value but there "
                        + "are other short options grouped together with this one. This is not allowed", 0);
            } else {
                state.currentArgument = arg;
            }
        };
    }

    private Optional<Argument<?>> findArgument(final String name) {
        // TODO: not that speed is super important but we should probably
        // store them in a map or something... for now, this is fine...
        return arguments.stream().filter(arg -> name.equals(arg.getLongName().orElse(null))
                || name.equals(arg.getShortName().orElse(null)))
                .findFirst();
    }

    /**
     * Check if the character is on the list of allowed optional characters, which of course
     * means that if it is than this is an indication that the token is an optional flag/token.
     *
     * @param ch
     * @return
     */
    private boolean isOptionalChar(final char ch) {
        return allowedChars.stream().filter(c -> c.equals(ch)).findFirst().isPresent();
    }

    public static HelpStep forProgramNamed(final String name) {
        return new DefaultBuilder(name);
    }

    private static List<String> splitArguments(final String cmdLine) {
        return null;
    }

    /**
     * Consume the first argument if it matches the name of the program.
     *
     * @param programName
     * @param args
     * @return
     */
    private static List<String> checkFirstArgument(final String programName, final List<String> args) {
        if (!args.isEmpty() && programName.equals(args.get(0))) {
            return args.stream().skip(1).collect(Collectors.toList());
        }
        return args;
    }

    private static final class DefaultBuilder implements HelpStep, Builder {

        private final String program;

        private boolean allowDoubleQuotes = false;
        private boolean allowSingleQuotes = false;

        private String description;

        private List<Argument<?>> arguments = new ArrayList<>();

        private List<Character> allowedChars = new ArrayList<>();

        private DefaultBuilder(final String program) {
            this.program = program;
        }

        @Override
        public Builder withAllowDoubleQuotedStrings() {
            return this;
        }

        @Override
        public Builder withAllowSingleQuotedStrings() {
            return this;
        }

        @Override
        public Builder withArgument(final Argument<?> argument) {
            if (argument != null) {
                arguments.add(argument);
            }
            return this;
        }

        @Override
        public Builder withPrefixChars(final char... chars) {
            for (int i = 0; i < (chars == null ? 0 : chars.length); ++i) {
                allowedChars.add(chars[i]);
            }
            return this;
        }

        @Override
        public Builder withDescription(final String description) {
            this.description = description != null && description.isEmpty() ? null : description;
            return this;
        }

        @Override
        public Builder withNoDescription() {
            return this;
        }

        @Override
        public ArgParser build() {
            if (allowedChars.isEmpty()) {
                allowedChars.add('-');
            }

            return new DefaultArgParser(program,
                    Optional.ofNullable(description),
                    allowDoubleQuotes,
                    allowSingleQuotes,
                    Collections.unmodifiableList(arguments),
                    Collections.unmodifiableList(allowedChars));
        }
    }
}
