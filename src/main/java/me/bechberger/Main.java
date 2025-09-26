package me.bechberger;

import me.bechberger.jfr.tool.*;
import me.bechberger.jfr.wrap.*;
import me.bechberger.jfr.wrap.nodes.AstConditional;
import me.bechberger.jfr.wrap.nodes.AstNode;
import me.bechberger.jfr.wrap.nodes.BooleanNode;
import me.bechberger.jfr.wrap.nodes.ProgramNode;
import picocli.CommandLine;

import java.text.ParseException;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.Callable;

/* @CommandLine.Command(
        name = "query",
        mixinStandardHelpOptions = true,
        version = "0.1",
        description = "Java Flight Recorder Query command line tool",
        subcommands = {
                HelpCommand.class,
                ViewCommand.class,
                QueryCommand.class,
                WebCommand.class
        }
) */
public class Main /* implements Callable<Integer> */ {
/* 
    @CommandLine.Spec
    CommandLine.Model.CommandSpec spec;

    private static final int EXIT_OK = 0;
    private static final int EXIT_FAILED = 1;
    private static final int EXIT_WRONG_ARGUMENTS = 2;

    private static String INFO = """
            Before using this tool, you must have a recording file.
            A file can be created by starting a recording from command line:
            
             java -XX:StartFlightRecording:filename=recording.jfr,duration=30s ...
            
            A recording can also be started on an already running Java Virtual Machine:
            
             jcmd (to list available pids)
             jcmd <pid> JFR.start
            
            Recording data can be dumped to file using the JFR.dump command:
            
             jcmd <pid> JFR.dump filename=recording.jfr
            
            The contents of the recording can then be printed, for example:
            
                view gc recording.jfr
                view allocation-by-site recording.jfr
            
            For more information about available commands, use 'help'
            
            Usage:
                [--json] [file] query
                
            Options:
                --json      Output results in JSON format instead of table format.
                           Durations are output as nanoseconds, RecordedThread 
                           objects are formatted as JSON objects with proper structure.
                           Data rows are formatted as objects with column names as keys.
            
            Examples:
                query.jar "SELECT * FROM GCPhaseParallel LIMIT 5"
                query.jar --json recording.jfr "SELECT gcId, duration, name FROM GCPhaseParallel"
            
            """;

    @Override
    public Integer call() {
        System.out.println(INFO);
        System.out.println();
        // print usage with picocli
        spec.commandLine().usage(System.out);
        return EXIT_OK;
    } */

    public static void main(String[] args) {
        Evaluator evaluator = Evaluator.getInstance();
        String input;
        boolean jsonOutput = false;
        
        // Temporary for API-Development
        if(args.length == 0) {
          JFRAPI api = new JFRAPI("src/main/java/me/bechberger/jfr/minecraft.jfr");
          api.start();
          return;
        }



        if(args.length == 2 && args[0].equals("web")) {
            JFRAPI api = new JFRAPI(args[1]);  // or new JFRAPI() for default port
            api.start();
            return;
        }
        // Check for --json flag
        if (args.length > 0 && "--json".equals(args[0])) {
            jsonOutput = true;
            // Remove --json flag from args
            String[] newArgs = new String[args.length - 1];
            System.arraycopy(args, 1, newArgs, 0, args.length - 1);
            args = newArgs;
        }
        /*
         * checks if both file and query are provided
         */
        if(args.length > 1) {
            System.out.println("Using file: " + args[0]);
            evaluator.setFile(args[0]);
            input = args[1];
        } else if (args.length == 1) {
            // If only one argumnt given choose default file
            System.err.println("No file specified, using default: src/main/java/me/bechberger/jfr/renaissance.jfr");
            evaluator.setFile("src/main/java/me/bechberger/jfr/voronoi2.jfr");
            input = args[0];

        } else {
            // If started in IDE/without arguments choose those specified here
            System.err.println("Neither query nor file specified, using as hard coded in main method");
            evaluator.setFile("src/main/java/me/bechberger/jfr/minecraft.jfr");
            input = "@SELECT eventThread, AVG(duration) FROM X GROUP BY eventThread HAVING AVG(duration) > 16ms; X = SELECT * FROM GCPhaseParallel;";
        }
        Lexer lexer = new Lexer(input);
        List<Token> tokens = lexer.tokenize();
        Parser parser = new Parser(tokens, input);
        try {
            ProgramNode res = parser.parse();
            /* System.out.println(res.toString(0)); */
            res.eval();
            if (jsonOutput) {
                System.out.println(evaluator.getJsonOutput());
            } else {
                System.out.println(evaluator.getOutput());
            }
        } catch (Exception e) {
            if(e instanceof ParseException) {
                if(e.getCause() != null) {
                    System.err.println("Error during parsing: " + e.getCause().getMessage());
                } else {
                    System.err.println("Error during parsing: " + e.getMessage());
                }
            } else {
                if(e.getCause() != null) {
                    System.err.println("Error during evaluation: " + e.getCause().getMessage());
                } else {
                    System.err.println("Error during evaluation: " + e.getMessage());
                }
            }
        } finally {
            evaluator.destruct();
        }   
    }
}
