import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import org.antlr.v4.runtime.CharStreams;
import org.antlr.v4.runtime.CommonTokenStream;
import org.antlr.v4.runtime.tree.ParseTree;
import org.antlr.v4.runtime.tree.ParseTreeWalker;

/**
 * A parser which parses purpose annotated Java files, and generates a call graph of the annotated code base
 * and a mapping from methods to purposes annotations. During the parsing, copies of the annotated files are created, 
 * and the purposes are removed, such the .class files can be generated.
 * 
 * @author Mia Heine Nowack
 * @version 1.0
 */

public class PurposeParserCompiler extends ExecutorPurposeParser {
    public PurposeParserCompiler(int poolSize) {
        super(poolSize);
    }

    @Override
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
            File purposeFile = new File(path);
            File renamedFile = new File(path + "p");
            purposeFile.renameTo(renamedFile);
            files.put(purposeFile, renamedFile);
            FileWriter writer = new FileWriter(purposeFile);
            writer.write(listener.rewriter.getText());
            writer.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Iterates over all the directories from the user input. All .java files are compiled using System Calls.
     * @throws InterruptedException
     */
    public void compileAndRemove(String[] cmds, String dir) {
        for (String path : cmds) {
            compileJavaFiles(path, dir);
        }
        deleteAndRenameFiles();
    }

    private void compileJavaFiles(String cmd, String dir) {
        try {
            Process p = Runtime.getRuntime().exec("javac " + cmd, null, new File(dir));
            p.waitFor();
        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
        }
    }

    public void deleteAndRenameFiles() {
        try {
            files.forEach((java, javap) -> {
                if(java.delete()) {
                    javap.renameTo(new File(java.toString())); 
                } else {
                    System.out.println("Could not delete file " + java);
                }
            });
        } catch (NullPointerException | SecurityException e) {
            e.printStackTrace();
        }
    }

}
