import java.io.File;
import java.io.IOException;
import picocli.CommandLine;
import picocli.CommandLine.Command;
import picocli.CommandLine.Option;
import picocli.CommandLine.Parameters;

/**
 * VioCheck is a command-line tool that parses an input directory of purpose annotated Java files, 
 * with different options to check the source code for purpose violations, purpose inference or compile the code.
 * The command-line tool is implemented with Picocli framework.
 * @author Mia Heine Nowack
 * @version 1.0
 */

@Command (
    name = "vioCheck",
    description = "parses directory and checks for violations",
    version = "vioCheck 1.0",
    mixinStandardHelpOptions = true
)
public class VioCheck implements Runnable {

    @Option (names = {"-h", "--hierarchy"}, description = "an option to define a purpose hierarchy, which must be in the directory and named PurposeFile.txt") 
    private boolean hierarchy;

    @Option (names = {"-w", "--warnings"}, description = "an option to check purpose violations only and not purpose inference")
    private boolean warnings;

    @Option (
        names= {"-c", "--compile"}, 
        split = ",", 
        description = "the Java file or files to be compiled, seperated by comma. If you want to use the option *.java, it must be captured in quotes, eg: \"*.java\"")
    String[] files;

    @Option (names= {"-t", "--threads"}, description = "the number of threads used to parse the files", defaultValue = "4")
    private int threads;

    @Option (names= {"-p", "--print"}, description = "prints all annotated methods with their purposes and method calls")
    boolean print;

    @Parameters (
        arity = "1",
        paramLabel = "<directoryPath>",
        description = "path to the directory or file to parse"
    ) private String directory; 
    
    private void compileJavaFiles(String cmd, String dir) {
        try {
            Process p = Runtime.getRuntime().exec("javac " + cmd, null, new File(dir));
            p.waitFor();
        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void run() {
        System.out.println("Parsing directory ...");
        try {
            PurposeCallGraph pcg;
        if(hierarchy) {
            if(files != null) {
                if(threads <= 1) {
                    threads = 4;
                }
                pcg = new HierarchyPurposeCallGraph(directory, directory + "/PurposeFile.txt", threads, true); 
                for (String path : files) {
                    compileJavaFiles(path, directory);
                } pcg.deleteAndRename();
            } else if(threads > 1) {
                pcg = new HierarchyPurposeCallGraph(directory, directory + "/PurposeFile.txt", threads, false);
            } else { 
                pcg = new HierarchyPurposeCallGraph(directory, directory + "/PurposeFile.txt");
            }
        } else {
            if(files != null) {
                if(threads <= 1) {
                    threads = 4;
                }
                pcg = new PurposeCallGraph(directory, threads, true); 
                for (String path : files) {
                    compileJavaFiles(path, directory);
                } pcg.deleteAndRename();
            } else if(threads > 1) {
                pcg = new PurposeCallGraph(directory, threads, false);
            } else { 
                pcg = new PurposeCallGraph(directory);
            }
        } if(warnings) {
            pcg.checkPurposeViolations();
        } else {
            pcg.checkPurposeViolationsAndMinimal();
        } if(print) {
            pcg.printPurposesAndMethodCalls();
        }  
            
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    public static void main(String[] args) {
        System.exit(new CommandLine(new VioCheck()).execute(args));
    }
}
