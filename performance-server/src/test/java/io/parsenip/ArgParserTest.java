package io.parsenip;

import io.dropwizard.cli.Command;
import net.sourceforge.argparse4j.inf.ArgumentParser;
import org.hamcrest.CoreMatchers;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.not;
import static org.junit.Assert.assertThat;

/**
 *
 */
public class ArgParserTest {

    private Argument<String> type;

    private Argument<Boolean> force;

    private Argument<Integer> intArg;

    @Before
    public void setUp() {

        force = Argument.withShortName("-f")
                .withNoDescription()
                .withNoArguments()
                .withValueWhenPresent(true)
                .build();

        intArg = Argument.withShortName("-i")
                .withNoDescription()
                .withNoArguments()
                .withValueWhenPresent(12)
                .build();



        /*
        type = Argument.withLongName("--type")
                .withDescription("The type")
                .withManyArguments()
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
        assertThat(cmd.getBoolean("-f").get(), is(true));
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
                .build();

        CommandLine cmd = parser.parse("monitor -f -i");
        assertThat(cmd.getBoolean("-f").get(), is(true));
        assertThat(cmd.getInteger("-i").get(), is(12));

        // group them together
        cmd = parser.parse("monitor -fi");
        assertThat(cmd.getBoolean("-f").get(), is(true));
        assertThat(cmd.getInteger("-i").get(), is(12));

        cmd = parser.parse("monitor -if");
        assertThat(cmd.getBoolean("-f").get(), is(true));
        assertThat(cmd.getInteger("-i").get(), is(12));
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
