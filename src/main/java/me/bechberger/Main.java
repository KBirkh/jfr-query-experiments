package me.bechberger;

import me.bechberger.jfr.tool.*;
import me.bechberger.jfr.wrap.*;
import me.bechberger.jfr.wrap.nodes.AstConditional;
import me.bechberger.jfr.wrap.nodes.AstNode;
import me.bechberger.jfr.wrap.nodes.BooleanNode;
import me.bechberger.jfr.wrap.nodes.ProgramNode;
import picocli.CommandLine;

import java.text.ParseException;
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
        String input = "@SELECT COUNT(), SUM(gcP.duration), MIN(gcP.duration), MAX(gcP.duration), AVG(gcP.duration) FROM [SELECT * FROM GCPhaseParallel] AS gcP WHERE p90(gcP.duration) GROUP BY gcP.eventThread, gcP.name HAVING COUNT() > 1 ORDER BY SUM(gcP.duration) LIMIT 6";
        Lexer lexer = new Lexer(input);
        Parser parser = new Parser(lexer.tokenize(), input);
        try {
            ProgramNode res = parser.parse();
            res.eval();
            Evaluator evaluator = Evaluator.getInstance();
            System.out.println(evaluator);
            /* System.out.println(res.toString(0)); */
        } catch (ParseException e) {
            
            e.printStackTrace();
        }   
    }
}
