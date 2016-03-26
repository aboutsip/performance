package io.parsenip;

import org.junit.Test;

/**
 * Created by jonas on 3/8/16.
 */
public class ArgumentTest {

    @Test
    public void testBasicArgument() throws Exception {

        Argument.withShortName("-t").withDescription("Type of monitor");

        // Create a flag parameter
        // Argument<Boolean> force1 = Argument.withLongName("--force").withShortName("-f").withDescription("force it").withNoArguments().setValue(true).build();

        // Argument<Boolean> force2 = Argument.withShortName("-f").withLongName("--force").withDescription("force it").withNoArguments().setValue(true).build();

        // default for flag parameter is true when present, false otherwise. You can change that...
        // hmmmm... if a user does 'whenSelected' doesn't that mean it will be a constant? Perhaps still
        // more obvious whats going on if you actually have to say "asConstant"
        // Argument.withLongName("--force").asConstant().whenSelected(true);

        // Argument.ofString()

        // Count the number of occurrences by specifying the argument as a count argument.
        // you can then do -vvvvv or --verbose --verbose etc and you will get back how many
        // times that argument has been specified.
        // A count argument cannot have
        // Argument.withShortName("-v").withLongName("--verbose").withDescription("Turn on verbose logging").asCount().withDefaultValue(0);

        // A constant is a special parameter
        // if there is a default value specified then it will also mean that
        // the argument is optional.
        // Argument.withLongName("--force").whenSelected(70).withDefaultValue(4);

        // Argument.withLongName("--force").withDefaultValue(true).whenSelected(false)

        // Argument.withLongName("--age").withDefaultValue(true).whenSelected(false)

        // Argument.withLongName("--age").withDefaultValue(5).with


        // Argument.withLongName("target").withChoice("hello").withChoice("bu").withChoices("hello", "one").withDefaultValue("hello");

        // Argument.withLongName("target").asOptional().asConstant().withDefaultValue(true);

        // Optional arguments - anything that has a default value specified is by definition an optional argument.
        // Constant arguments - anything that is using the 'whenSelected' is by definition a constant argument and will not accept
        //                      any other type of configuration.

        // Positional arguments - goes by the name of the parameter. If '--' is specified it is assumed NOT to be
        //                        a positional argument.
        //                        Cannot be constant arguments. Can still be optional though if the defaultValue is chosen

        // Create a constant Integer param

        // Integer constant
        // Argument<Integer> c1 = Argument.withShortName("-k").withNoArguments().setValue(4).build();
        // Argument<Integer> c2 = Argument.withShortName("-k").withNoArguments().setValue(4).withDefaultValue(55).build();

        // Boolean constant = flag. Set to 'true' when value is present.
        // Argument<Boolean> c3 = Argument.withShortName("-j").withLongName("--july").withNoArguments().setValue(true).withDefaultValue(false).build();
        // Argument<Boolean> c4 = Argument.withShortName("-j").withLongName("--july").withNoArguments().setValue(true).build();

        // Double constant
        // Argument<Double> c5 = Argument.withShortName("-d").withNoArguments().setValue(4.0).build();

        // Variable argument with exactly one allowed value from a specific list
        // Argument<Integer> arg1 = Argument.withShortName("-k").withOneArgument().withValidator((Integer t) -> t < 4).build();

        // Variable arguments with min 3 args and no more than 44 and you can only choose 3 :-) which is also default
        // so it makes no sense.
        // Argument<Integer> arg2 = Argument.withShortName("-k").withMinArguments(3).withMaxArguments(44).withChoice(3).withDefaultValue(3).build();

        // Argument<String> arg4 = Argument.withShortName("target").withDescription("The targets").withMinArguments(3).withAnyValue().build();

        // Create an option where the user can choose between three values and must specify at least one
        // Argument<String> arg5 = Argument.withShortName("target").withDescription("The targets").withMinArguments(1).withChoice("sut").withChoice("myUAS").withChoice("myUAC").build();
    }
}
