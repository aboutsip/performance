package io.parsenip;

import org.junit.Before;
import org.junit.Test;

import java.util.List;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.not;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.fail;

/**
 *
 */
public class ArgParserTest {

    private Argument<Boolean> force;

    private Argument<Integer> intArg;

    private Argument<String> stringArg;

    private Argument<String> type;

    private Argument<String> target;

    @Before
    public void setUp() {

        force = Argument.withShortName("-f")
                .withNoDescription()
                .withNoArguments()
                .withValueWhenPresent(true)
                .build();

        intArg = Argument.withShortName("-i")
                .withLongName("--int")
                .withNoDescription()
                .withNoArguments()
                .withValueWhenPresent(12)
                .withValueWhenAbsent(99)
                .build();

        stringArg = Argument.withShortName("-s")
                .withNoDescription()
                .withNoArguments()
                .withValueWhenPresent("hello world")
                .withValueWhenAbsent("not here")
                .build();

        type = Argument.withLongName("type")
                .withNoDescription()
                .withZeroOrMoreArguments()
                .ofType(String.class)
                .withChoices("cpu", "iostat", "gc")
                .withDefaultValue("cpu")
                .build();

        target = Argument.withLongName("target")
                .withNoDescription()
                .withAtLeastOneArgument()
                .ofType(String.class)
                .build();


        /*
        type = Argument.withLongName("--type")
                .withDescription("The type")
                .withZeroOrMoreArguments()
                .ofType(String.class)
                .withChoices("one", "two", "three")
                .build();
                */
    }

    /**
     * Since parsenip also is commonly used to parse regular strings which may then
     * contain the name of the program we want to ensure that if it is used this
     * way that the name of the program is consumed.
     *
     * @throws Exception
     */
    @Test
    public void testConsumeProgramName() throws Exception {
        final ArgParser parser = ArgParser.forProgramNamed("monitor").withNoDescription().build();
        final CommandLine cmd = parser.parse("monitor");
        assertThat(cmd.commandName(), is("monitor"));
    }

    @Test
    public void testVariableArgument() throws Exception {
        ArgParser parser = ArgParser.forProgramNamed("monitor")
                .withNoDescription()
                .withArgument(force)
                .withArgument(type)
                .build();

        CommandLine cmd = parser.parse("monitor cpu");
        List<String> types =  cmd.getValues(type);
        assertThat(types.size(), is(1));
        assertThat(types.get(0), is("cpu"));

        cmd = parser.parse("monitor iostat cpu");
        types =  cmd.getValues(type);
        assertThat(types.size(), is(2));
        assertThat(types.get(0), is("iostat"));
        assertThat(types.get(1), is("cpu"));

        // even though type is a positional argument, we can actually
        // put other arguments in the middle and we should still find
        // our way...
        cmd = parser.parse("monitor iostat -f cpu");
        types =  cmd.getValues(type);
        assertThat(types.size(), is(2));
        assertThat(types.get(0), is("iostat"));
        assertThat(types.get(1), is("cpu"));
        assertThat(cmd.getValue(force).get(), is(true));
    }

    @Test
    public void testUnacceptableChoice() throws Exception {
        final ArgParser parser = ArgParser.forProgramNamed("monitor")
                .withNoDescription()
                .withArgument(type)
                .withArgument(force)
                .build();
        assertException(parser, "monitor hello", "hello");

        // we should clear -f though...
        assertException(parser, "monitor -f foo", "foo");

        // and cpu, iostat and gc are valid values but the last one isn't
        assertException(parser, "monitor -f cpu iostat gc invalid", "invalid");
    }

    private static void assertException(final ArgParser parser, final String command, final String expectBadValue) {
        try {
            parser.parse(command);
            fail("Expected a ParseException");
        } catch (final ParseException e) {
            assertThat(e.toString().contains(expectBadValue), is(true));
        }
    }

    /**
     * This command line is actually what made me write this little library. The argparse one
     * didn't actually allow you to have firs the 'type' that accepted only three types of
     * values, and then another final positional argument that picked up the 'target'.
     * I needed that and as such, i wrote this library...
     *
     * @throws Exception
     */
    @Test
    public void testFullProgram() throws Exception {
        final ArgParser parser = ArgParser.forProgramNamed("monitor")
                .withDescription("monitor a certain remote host")
                .withAllowDoubleQuotedStrings()
                .withArgument(force)
                .withArgument(type)
                .withArgument(target)
                .build();

        CommandLine cmd = parser.parse("monitor cpu iostat 192.168.0.100");
        assertValues(cmd.getValues(type), "cpu", "iostat");
        assertValues(cmd.getValues(target), "192.168.0.100");

        // now, since the 'type' actually has a list of accepted values
        // we COULD potentially allow to mix the positional arguments. E.g.
        // the argument that has the most specific match could be allowed to win.
        // However, currently we will always first ask the last arument that consumed
        // the latest token and if accepts it, it will get it. So, this test ensure that
        // that is actually the case but if we decide to change the behavior so that in
        // this case, the "gc" value actually ended up with the type, this test case
        // serves as a reminder of how things work right now and obviously, this test
        // has to change...
        cmd = parser.parse("monitor cpu iostat 62.63.64.65 gc");
        assertValues(cmd.getValues(type), "cpu", "iostat");
        assertValues(cmd.getValues(target), "62.63.64.65", "gc");
    }

    private static void assertValues(final List<?> actualValues, final Object ... expectedValues) {
        assertThat(actualValues.size(), is(expectedValues.length));
        for (int i = 0; i < actualValues.size(); ++i) {
            final Object actualValue = actualValues.get(i);
            final Object expectedValue = expectedValues[i];
            assertThat(actualValue.getClass().isAssignableFrom(expectedValue.getClass()), is(true));
            assertThat(actualValue, is(expectedValue));
        }
    }

    /**
     * A constant argument is an argument that doesn't take any parameters but if that
     * argument is given, the value of is set to something constant.
     *
     * @throws Exception
     */
    @Test
    public void testConstantArgument() throws Exception {
        final ArgParser parser = ArgParser.forProgramNamed("monitor")
                .withNoDescription()
                .withArgument(force)
                .build();

        final CommandLine cmd = parser.parse("monitor -f");
        assertThat(cmd.getValue(force).get(), is(true));
    }

    /**
     * For constant arguments you can group them together when specifying them.
     * I.e. you could do '-f -i' or you can do '-fi' and it should yield the
     * same result.
     *
     * @throws Exception
     */
    @Test
    public void testSeveralConstantArgumentGroupedTogether() throws Exception {
        final ArgParser parser = ArgParser.forProgramNamed("monitor")
                .withNoDescription()
                .withArgument(force)
                .withArgument(intArg)
                .withArgument(stringArg)
                .build();

        CommandLine cmd = parser.parse("monitor -f -i");
        assertThat(cmd.getValue(force).get(), is(true));
        assertThat(cmd.getValue(intArg).get(), is(12));

        // group them together
        cmd = parser.parse("monitor -fi");
        assertThat(cmd.getValue(force).get(), is(true));
        assertThat(cmd.getValue(intArg).get(), is(12));

        cmd = parser.parse("monitor -if");
        assertThat(cmd.getValue(force).get(), is(true));
        assertThat(cmd.getValue(intArg).get(), is(12));

        cmd = parser.parse("monitor -sif");
        assertThat(cmd.getValue(force).get(), is(true));
        assertThat(cmd.getValue(stringArg).get(), is("hello world"));
        assertThat(cmd.getValue(intArg).get(), is(12));
    }

    /**
     * You can also specify a value for a constant argument when it is NOT
     * present.
     *
     * Also note that a constant argument, which the --force argument is in the
     * below case may NOT have a value associated with it when it is not present.
     * In that case, if the value is not present and you ask for it you will
     * not get back anything.
     *
     * @throws Exception
     */
    @Test
    public void testConstantArgumentWhenNotPresent() throws Exception {
        final ArgParser parser = ArgParser.forProgramNamed("monitor")
                .withNoDescription()
                .withArgument(force)
                .withArgument(intArg)
                .withArgument(stringArg)
                .build();

        // note that the force argument doesn't have a value
        // associated with it when it is NOT present.
        // This means when it is not present that when asked
        // for that argument you will get an empty optional back
        CommandLine cmd = parser.parse("monitor");
        assertThat(cmd.getValue(force).isPresent(), is(false));
        assertThat(cmd.getValue(intArg).get(), is(99));
        assertThat(cmd.getValue(intArg).get(), is(99));
        assertThat(cmd.getValue(stringArg).get(), is("not here"));

        // OK, -f is back again and as such should have the only
        // possible value and that is true and use the long name
        // for the 'int' argument
        cmd = parser.parse("monitor -f --int");
        assertThat(cmd.getValue(force).get(), is(true));
        assertThat(cmd.getValue(intArg).get(), is(12));
        assertThat(cmd.getValue(stringArg).get(), is("not here"));

    }

    /**
     * The boolean flag is just a special case of the constant argument.
     *
     * @throws Exception
     */
    @Test
    public void testBooleanFlag() throws Exception {

    }

}
