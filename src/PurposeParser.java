import java.io.File;
import java.io.IOException;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashSet;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.RejectedExecutionException;
import java.util.regex.Pattern;
import org.antlr.v4.runtime.CharStreams;
import org.antlr.v4.runtime.CommonTokenStream;
import org.antlr.v4.runtime.tree.ParseTree;
import org.antlr.v4.runtime.tree.ParseTreeWalker;

/**
 * A parser which parses purpose annotated Java files, and generates a call graph of the annotated code base
 * and a mapping from methods to purposes annotations. 
 * 
 * @author Mia Heine Nowack
 * @version 2.0
 */

public class PurposeParser {
    protected ConcurrentHashMap<File, File> files;
    protected ConcurrentHashMap<String, HashSet<String>> purposes;
    protected ConcurrentHashMap<String, HashSet<String>> methodCalls;
    protected ConcurrentHashMap<String, String> inheritance;

    public PurposeParser() {
        files = new ConcurrentHashMap<>();
        purposes = new ConcurrentHashMap<>();
        methodCalls = new ConcurrentHashMap<>();
        inheritance = new ConcurrentHashMap<>();
    }

    /**
     * Checks if the input String is a directory or single file. 
     * If it's a directory, the method getJavaFiles is called. If not, the single file is parsed.
     * @param input
     */
    public void parseInput(String input) {
        Path path = Paths.get(input);
        Pattern regex = Pattern.compile(".*\\.java");
        if(path.toFile().isDirectory()) {
            getJavaFiles(path); 
        } else if(regex.matcher(input).matches()) {
            parseFile(input);
        }
    }

    /**
     * Finds all Java files in a directory and parses each file with the method parseFile.
     * @param dir Path of the input directory
     */
    public void getJavaFiles(Path dir) {
        Pattern regex = Pattern.compile(".*\\.java"); // makes sure we only compile the regex once
        try (DirectoryStream<Path> stream = Files.newDirectoryStream(dir)) {
            for (Path path : stream) {
                if (path.toFile().isDirectory()) {
                    getJavaFiles(path);
                } else if (regex.matcher(path.getFileName().toString()).matches()) {
                    parseFile(path.toAbsolutePath().toString());
                }
            }
        } catch (IOException | RejectedExecutionException | NullPointerException e) {
            e.printStackTrace();
        }
    }

    /**
     * Creates the ANTLR lexer, parser and parse tree. A single file is parsed
     * and the result is added to the two maps: purposes and methodCalls. 
     * @param path Java file path
     */
    public void parseFile(String path) {
        try {
            // create a lexer that feeds off of input CharStream
            Java9Lexer lexer = new Java9Lexer(CharStreams.fromFileName(path));
            // create a buffer of tokens pulled from the lexer
            CommonTokenStream tokens = new CommonTokenStream(lexer);
            // create a parser that feeds off the tokens buffer
            Java9Parser parser = new Java9Parser(tokens);
            // begin parsing at compilationUnit rule (top of file)
            ParseTree tree = parser.compilationUnit();
            ParseTreeWalker walker = new ParseTreeWalker();
            PurposeListener listener = new PurposeListener(tokens, inheritance);
            walker.walk(listener, tree);
            purposes.putAll(listener.getPurposeMap());
            methodCalls.putAll(listener.getMethodCalls());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public ConcurrentHashMap<String, HashSet<String>> getPurposeMap() {
        return purposes;
    }

    public ConcurrentHashMap<String, HashSet<String>> getMethodCalls() {
        return methodCalls;
    }

    public String resolveInheritance(String methodCall) {
        String superName = null;
        String className = methodCall.split("[.]")[0];
        String methodName = methodCall.split("[.]")[1];
        if(inheritance.containsKey(className)) {
            String name = inheritance.get(className) + "." + methodName;
            if(purposes.containsKey(name)) {
                superName = name;
            } else {
                superName = resolveInheritance(name);
            }
        }
        return superName;
    }

    


    //Measurements//

    public void baseLevelParsing(String dir) {
        Pattern regex = Pattern.compile(".*\\.java"); // makes sure we only compile the regex once
        try (DirectoryStream<Path> stream = Files.newDirectoryStream(Paths.get(dir))) {
            for (Path path : stream) {
                if (path.toFile().isDirectory()) {
                    getJavaFiles(path);
                } else if (regex.matcher(path.getFileName().toString()).matches()) {
                    parseBase(path.toAbsolutePath().toString());
                }
            }
        } catch (IOException | RejectedExecutionException | NullPointerException e) {
            e.printStackTrace();
        }
    }

    private void parseBase(String path) {
        try {
            // create a lexer that feeds off of input CharStream
            Java9Lexer lexer = new Java9Lexer(CharStreams.fromFileName(path));
            // create a buffer of tokens pulled from the lexer
            CommonTokenStream tokens = new CommonTokenStream(lexer);
            // create a parser that feeds off the tokens buffer
            Java9Parser parser = new Java9Parser(tokens);
            // begin parsing at compilationUnit rule (top of file)
            ParseTree tree = parser.compilationUnit();
            ParseTreeWalker walker = new ParseTreeWalker();
            Java9ParserBaseListener listener = new Java9ParserBaseListener();
            walker.walk(listener, tree);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
