package com.aboutsip.performance.config;

import io.parsenip.ArgParser;
import io.parsenip.Argument;
import io.parsenip.CommandLine;
import io.parsenip.Parsenip;
import io.pkts.buffer.Buffer;
import io.pkts.buffer.Buffers;
import io.pkts.packet.sip.impl.SipParser;
import net.sourceforge.argparse4j.ArgumentParsers;
import net.sourceforge.argparse4j.impl.Arguments;
import net.sourceforge.argparse4j.inf.ArgumentParser;
import net.sourceforge.argparse4j.inf.ArgumentParserException;
import net.sourceforge.argparse4j.inf.Namespace;
import org.junit.Test;

import java.io.IOException;
import java.util.*;

import static org.hamcrest.CoreMatchers.*;
import static org.junit.Assert.assertThat;

/**
 * Created by jonas on 3/3/16.
 */
public class ScenarioConfigTest extends ConfigTestBase {

    @Test
    public void testLoadConfig() throws Exception {

        final ScenarioConfig scenario = loadConfiguration(ScenarioConfig.class, "test_scenario_001.yaml");
        assertThat(scenario.getName(), is("Simple INVITE Scenario"));

        assertThat(scenario.getClients().size(), is(3));
        final SIPpInstanceConfig myUAS = scenario.getClient("myUAS").get();
        assertThat(myUAS.getHost(), is("127.0.0.1"));
        assertThat(myUAS.getPort(), is(5060));

        final SIPpInstanceConfig theUAC = scenario.getClient("theUacDude").get();
        assertThat(theUAC.getHost(), is("192.168.0.100"));
        assertThat(theUAC.getPort(), is(5062));
        assertThat(theUAC.getRemoteHost(), is("127.0.0.1"));
        assertThat(theUAC.getRemotePort(), is(5060));

        final SIPpInstanceConfig anotherUAC = scenario.getClient("anotherUacDude").get();
        assertThat(anotherUAC.getHost(), is("62.63.64.65"));
        assertThat(anotherUAC.getPort(), is(5060));
        assertThat(anotherUAC.getRemoteHost(), is("127.0.0.1"));
        assertThat(anotherUAC.getRemotePort(), is(5060));

        // final List<String> actions = scenario.getActions();
        // actions.forEach(this::doIt);
        // doIt("monitor --type cpu myUAS");
        doItMyWay("monitor --type cpu myUAS");
    }

    private void doItMyWay(final String cmd) {
        try {
            System.out.print(cmd + " --->");
            System.out.println(parseArgumentMyWay(cmd));
        } catch (ArgumentParserException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private CommandLine parseArgumentMyWay(final String cmd) throws IOException, ArgumentParserException {
        final Map<String, ArgParser> parsers = new HashMap<>();
        parsers.put("monitor", createMonitorParsenip());

        // need to redo this...
        final String[] args = splitCmd(cmd);
        final ArgParser parser = parsers.get(args[0]);
        return parser.parse(cmd);
    }

    private void doIt(final String cmd) {
        try {
            System.out.print(cmd + " --->");
            System.out.println(parseArgument(cmd));
        } catch (ArgumentParserException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private Namespace parseArgument(final String cmd) throws ArgumentParserException, IOException {
        final Map<String, ArgumentParser> parsers = new HashMap<>();
        parsers.put("report", createReportParser());
        parsers.put("stop", createStopParser());
        parsers.put("start", createStartParser());
        parsers.put("pause", createPauseParser());
        parsers.put("monitor", createMonitorArgParser());

        final String[] args = splitCmd(cmd);
        final ArgumentParser parser = parsers.get(args[0]);
        return parser.parseArgs(Arrays.copyOfRange(args, 1, args.length));
    }

    private String[] splitCmd(final String cmd) throws IOException, ArgumentParserException {
        final List<String> parts = new ArrayList<>();
        final Buffer buffer = Buffers.wrap(cmd);
        int count = 0; // just to a prevent runaway loop in case the parsing is off
        final int MAX = 100;
        while (buffer.hasReadableBytes() && ++count < MAX) {
            final Buffer result;
            SipParser.consumeWS(buffer);
            if (SipParser.isNext(buffer, SipParser.DQUOT)) {
                result = SipParser.consumeQuotedString(buffer);
            } else {
                result = SipParser.consumeToken(buffer);
            }

            if (result != null) {
                parts.add(result.toString());
            }
        }

        if (count == MAX) {
            throw new ArgumentParserException("Unable to parse due to internal error. Loop never finished", null);
        }

        return parts.toArray(new String[0]);
    }

    private ArgumentParser createStartParser() {
        final ArgumentParser p = ArgumentParsers.newArgumentParser("start")
                .description("Start a particular process");
        p.addArgument("target").nargs("*").help("Which process to start");
        return p;
    }

    private ArgumentParser createPauseParser() {
        final ArgumentParser p = ArgumentParsers.newArgumentParser("pause")
                .description("Pause a particular SIPp instance");
        p.addArgument("target").nargs("*").help("Which sipp instance to pause");
        return p;
    }

    private ArgumentParser createStopParser() {
        final ArgumentParser p = ArgumentParsers.newArgumentParser("stop")
                .description("Stop (quit) a particular process");
        p.addArgument("target").nargs("*").help("Which process(es) to stop");
        p.addArgument("--force")
                .action(Arguments.storeConst())
                .setConst(true)
                .setDefault(false)
                .help("Force the process to stop (will eventually kill it if it doesn't stop by itself)");

        return p;
    }

    private ArgumentParser createReportParser() {
        final ArgumentParser p = ArgumentParsers.newArgumentParser("report")
                .description("Generate a report for a particular process(es)");

        p.addArgument("target").nargs("*").help("Which process(es) to include in the report");

        p.addArgument("--snapshot")
                .action(Arguments.storeConst())
                .setConst(true)
                .setDefault(false)
                .help("Generate a snapshot report");

        p.addArgument("--include")
                .choices("gc", "cpu", "iostat")
                .nargs(1)
                .setDefault("gc", "cpu")
                .help("What type of information to include in the report");

        p.addArgument("--format")
                .choices("html", "text", "pdf")
                .nargs(1)
                .setDefault("text")
                .help("The format of the report (not a valid option for snapshot)");

        p.addArgument("--from")
                .setDefault("start")
                .help("From which point in time the report should be generated. Will be from the beginning of the run if not specified");

        p.addArgument("--to")
                .setDefault("now")
                .help("To which point in time the report should be generated. Will be \"now\" if not specified.");

        p.addArgument("--title")
                .setDefault("Report")
                .help("The title of the report");

        p.addArgument("--name")
                .setDefault("report")
                .help("The name of the report");

        return p;
    }

    private ArgParser createMonitorParsenip() {

        Argument<Boolean> forceFlag = Argument.withShortName("-f")
                .withLongName("--force")
                .withDescription("force it")
                .withNoArguments()
                .withValueWhenPresent(true)
                .build();

        // Makes no sense having a constant parameter to be required.
        // If it is required then you may just hard code the actual
        // value into your code. The user has no choice anyway!
        Argument<Boolean> forceFlag1 = Argument.withLongName("--force")
                .withDescription("force it")
                .withNoArguments()
                .withValueWhenPresent(true) // will give us the type
                .withValueWhenAbsent(false) // will give us the type
                .build();

        Argument<String> type = Argument.withLongName("--type")
                .withDescription("The type of monitoring")
                .withAtLeastOneArgument()
                .ofType(String.class)
                .isRequired()
                .withDefaultValue("cpu")
                .withChoices("cpu", "iostat", "gc")
                .build();

        ArgParser parser = ArgParser.forProgramNamed("monitor")
                .withDescription("monitor a process")
                .withArgument(forceFlag)
                .withArgument(type)
                .withAllowDoubleQuotedStrings()
                .build();


        return null;
    }

    /**
     * Create the argument parser for the 'monitor'
     * @return
     */
    private ArgumentParser createMonitorArgParser() {
        final ArgumentParser p = ArgumentParsers.newArgumentParser("monitor")
                .description("Monitor a particular process");

        p.addArgument("--type")
                .choices("gc", "cpu", "iostat")
                .nargs("*")
                .setDefault("cpu")
                .help("What type of monitor to use (you may use multiple)");

        p.addArgument("target").nargs("+").help("Which process to monitor");

        System.err.println(p.formatHelp());
        return p;
    }
}
