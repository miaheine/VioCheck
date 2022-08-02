import java.io.IOException;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.RejectedExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.regex.Pattern;

/**
 * An ExecutorPurposeParser which parses purpose annotated Java files, and generates a call graph of the annotated code base
 * and a mapping from methods to purposes annotations. The parsing is performed concurrently.
 * 
 * @author Mia Heine Nowack
 * @version 1.0
 */

public class ExecutorPurposeParser extends PurposeParser {
    protected ExecutorService threadPool;

    public ExecutorPurposeParser(int poolSize) {
        super();
        threadPool = Executors.newFixedThreadPool(poolSize);
    }

    @Override
    public void getJavaFiles(Path dir) {
        Pattern regex = Pattern.compile(".*\\.java");
        try (DirectoryStream<Path> stream = Files.newDirectoryStream(dir)) {
            for (Path path : stream) {
                if (path.toFile().isDirectory()) {
                    getJavaFiles(path);
                } else if (regex.matcher(path.getFileName().toString()).matches()) {
                    Runnable runnableTask = () -> {
                        try {
                            parseFile(path.toAbsolutePath().toString());
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    };
                    threadPool.execute(runnableTask);
                 }
            }
        } catch (IOException | RejectedExecutionException | NullPointerException e) {
            e.printStackTrace();
        }
    }

    public void shutdown() {
        try {
            threadPool.shutdown();
            threadPool.awaitTermination(5, TimeUnit.MINUTES);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
} 
