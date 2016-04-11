package io.parsenip.impl;

import com.fasterxml.jackson.dataformat.yaml.snakeyaml.scanner.Constant;
import com.google.common.collect.Lists;
import io.parsenip.*;
import io.parsenip.ParseException;

import java.text.*;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 *
 */
public class DefaultArgParser implements ArgParser {

    private final String program;

    private final boolean allowDoubleQuotes;
    private final boolean allowSingleQuotes;

    private final Optional<String> description;

    /**
     * Those arguments that have default values and as such
     * doesn't need to be present on the command line must
     * be processed at the end of parsing a command line
     * to find out the ones that indeed weren't present.
     */
    private final List<Argument<?>> argumentsWithDefaults;

    private final List<Argument<?>> arguments;

    private final List<Character> allowedChars;

    private DefaultArgParser(final String program,
                             final Optional<String> description,
                             final boolean allowDoubleQuotes,
                             final boolean allowSingleQuotes,
                             final List<Argument<?>> arguments,
                             final List<Argument<?>> argumentsWithDefaults,
                             final List<Character> allowedChars) {
        this.program = program;
        this.allowDoubleQuotes = allowDoubleQuotes;
        this.allowSingleQuotes = allowSingleQuotes;
        this.description = description;
        this.arguments = arguments;
        this.allowedChars = allowedChars;
        this.argumentsWithDefaults = argumentsWithDefaults;

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

        // Ensure that all those arguments that had default values
        // and were NOT present on the command line is indeed
        // included in the final state.
        argumentsWithDefaults.stream().filter(arg -> !state.arguments.contains(arg)).forEach(arg -> {
            state.commandLine.withArgument(arg, arg.getDefaultValue().get());
        });

        return state.commandLine.build();
    }

    private class ParseState {

        Argument<?> currentArgument;

        /**
         * The arguments we have processed so far
         */
        List<Argument<?>> arguments = new ArrayList<>();

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
        } else {
            processValue(state, token);
        }
    }

    private void processLongOptionalArgument(final ParseState state, final String token) {
        final Argument<?> arg = findArgument(token).orElseThrow(() ->
                new ParseException("Unable to find an argument named " + token, state.processedChars));

        state.currentArgument = arg;
        if (arg.isConstant()) {
            processConstantArgument(state, arg.toConstantArgument());
        }
    }

    /**
     *
     * @param state
     * @param token
     */
    private void processValue(final ParseState state, final String token) {

        // always prefer the current argument if one already has been selected
        // and is prepared to accept the value
        // TODO: not correct if the argument has already e.g. consumed 2 values and that
        // is its max args allowed.
        if (state.currentArgument == null || !state.currentArgument.isValueAccepted(token)) {
            state.currentArgument = findNextArgument(state, token);
        }

        if (state.currentArgument == null) {
            throw new ParseException("Illegal value \"" + token + "\"", 0);
        }

        state.commandLine.withArgument(state.currentArgument, state.currentArgument.valueOf(token));
        state.arguments.add(state.currentArgument);
    }

    private Argument<?> findNextArgument(final ParseState state, final String token) {
        final List<Argument<?>> candidates = arguments.stream().filter(arg -> accept(state, arg, token))
                .collect(Collectors.toList());

        if (candidates.isEmpty()) {
            throw new ParseException("Illegal value \"" + token + "\"", 0);
        }

        // TODO: should we issue a warning that we found multiple candidates that accepts
        // this value?
        return candidates.get(0);
    }

    /**
     * Check if an argument would even consider accepting the value. This is determined by if we
     * can even parse the value into the correct type, then see if the argument will accept the value
     * and then check if we were to add this to that argument, would it now have too many accepted
     * arguments (some arguments are configured to only have max 3 values etc)
     *
     * @param argument
     * @param token
     * @return
     */
    private boolean accept(final ParseState state, final Argument<?> argument, final String token) {
        if (token == null || token.isEmpty()) {
            return false;
        }
        return argument.isValueAccepted(token);
    }

    private void processConstantArgument(final ParseState state, final ConstantArgument<?> arg) {
        final Object value = arg.toConstantArgument().getValueWhenPresent();
        state.commandLine.withArgument(arg, arg.toConstantArgument().getValueWhenPresent());
        state.arguments.add(arg);

        // a constant argument will not have a value and therefore
        // we are no long working on a particular argument so
        // reset it.
        state.currentArgument = null;
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
                processConstantArgument(state, arg.toConstantArgument());
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

            final List<Argument<?>> argumentsWithDefaults = arguments.stream()
                    .filter(arg -> !arg.isRequired() && arg.getDefaultValue().isPresent())
                    .collect(Collectors.toList());

            return new DefaultArgParser(program,
                    Optional.ofNullable(description),
                    allowDoubleQuotes,
                    allowSingleQuotes,
                    Collections.unmodifiableList(arguments),
                    Collections.unmodifiableList(argumentsWithDefaults),
                    Collections.unmodifiableList(allowedChars));
        }
    }
}
