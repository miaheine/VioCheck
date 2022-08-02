import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.stream.IntStream;

/**
 * Benchmark source code is provided by Matti Karppa and Riko Jakob for the ITU course Applied Algorithms.
 * The code is modified and adapted to this project by Mia Heine Nowack.
 */

public class Benchmark {
    private static long[] ns;

    @FunctionalInterface  
    interface TriFun {  
        void accept(String input, int threads, boolean compile);
    } 
    
    public static double measure(Runnable f) {
        Instant start = Instant.now();
        f.run();
        Instant end = Instant.now();
        return Duration.between(start, end).toNanos() / 1e9;
    }

    public static double[][] benchmark(Consumer<String> f, String[] args, int N) {
        int m = args.length;
        double[][] M = new double[m][N];
        for (int i = 0; i < m; ++i) {
            for (int j = 0; j < N; ++j) {
                String arg = args[i];
                M[i][j] = measure(() -> f.accept(arg));
            }
        }
        double[][] R = new double[m][2];
        for (int i = 0; i < m; ++i) {
            for (int j = 0; j < N; ++j) {
                R[i][0] += M[i][j];
            }
            R[i][0] /= N;
            for (int j = 0; j < N; ++j) {
                double x = M[i][j] - R[i][0];
                R[i][1] += x*x;
            }
            R[i][1] = Math.sqrt(R[i][1] / (N - 1));
        }
        return R;
    }

    public static double[][] benchmarkBiconsumer(BiConsumer<String, String> f, String[] args, int N) {
        try {
            int m = args.length;
        double[][] M = new double[m][N];
        for (int i = 0; i < m; ++i) {
            for (int j = 0; j < N; ++j) {
                String arg = args[i];
                M[i][j] = measure(() -> f.accept(arg, arg + "/PurposeFile.txt"));
            }
        }
        double[][] R = new double[m][2];
        for (int i = 0; i < m; ++i) {
            for (int j = 0; j < N; ++j) {
                R[i][0] += M[i][j];
            }
            R[i][0] /= N;
            for (int j = 0; j < N; ++j) {
                double x = M[i][j] - R[i][0];
                R[i][1] += x*x;
            }
            R[i][1] = Math.sqrt(R[i][1] / (N - 1));
        }
        return R;
        } catch (Exception e) {
            return null;
        }
        
    }

    public static double[][] benchmarkTri(TriFun f, String[] args, int N) {
        try {
            int m = args.length;
        double[][] M = new double[m][N];
        for (int i = 0; i < m; ++i) {
            for (int j = 0; j < N; ++j) {
                String arg = args[i];
                M[i][j] = measure(() -> f.accept(arg, 6, false));
            }
        }
        double[][] R = new double[m][2];
        for (int i = 0; i < m; ++i) {
            for (int j = 0; j < N; ++j) {
                R[i][0] += M[i][j];
            }
            R[i][0] /= N;
            for (int j = 0; j < N; ++j) {
                double x = M[i][j] - R[i][0];
                R[i][1] += x*x;
            }
            R[i][1] = Math.sqrt(R[i][1] / (N - 1));
        }
        return R;
            
        } catch (Exception e) {
            return null;
        }
    }

    public static double[][] benchmarkVio(String[] args, int N) {
        int m = args.length;
        double[][] M = new double[m][N];
        for (int i = 0; i < m; ++i) {
            for (int j = 0; j < N; ++j) {
                PurposeCallGraph pcg = new PurposeCallGraph(args[i], 6, false);
                M[i][j] = measure(() -> pcg.checkPurposeViolations());
            }
        }
        double[][] R = new double[m][2];
        for (int i = 0; i < m; ++i) {
            for (int j = 0; j < N; ++j) {
                R[i][0] += M[i][j];
            }
            R[i][0] /= N;
            for (int j = 0; j < N; ++j) {
                double x = M[i][j] - R[i][0];
                R[i][1] += x*x;
            }
            R[i][1] = Math.sqrt(R[i][1] / (N - 1));
        }
        return R;
    }

    public static double[][] benchmarkVioMini(String[] args, int N) {
        int m = args.length;
        double[][] M = new double[m][N];
        for (int i = 0; i < m; ++i) {
            for (int j = 0; j < N; ++j) {
                PurposeCallGraph pcg = new PurposeCallGraph(args[i], 6, false);
                M[i][j] = measure(() -> pcg.checkPurposeViolationsAndMinimal());
            }
        }
        double[][] R = new double[m][2];
        for (int i = 0; i < m; ++i) {
            for (int j = 0; j < N; ++j) {
                R[i][0] += M[i][j];
            }
            R[i][0] /= N;
            for (int j = 0; j < N; ++j) {
                double x = M[i][j] - R[i][0];
                R[i][1] += x*x;
            }
            R[i][1] = Math.sqrt(R[i][1] / (N - 1));
        }
        return R;
    }

    public static double[][] benchmarkHierarchyVio(String[] args, int N) throws Exception {
        int m = args.length;
        double[][] M = new double[m][N];
        for (int i = 0; i < m; ++i) {
            for (int j = 0; j < N; ++j) {
                HierarchyPurposeCallGraph pcg = new HierarchyPurposeCallGraph(args[i], args[i] + "/PurposeFile.txt", 6, false);
                M[i][j] = measure(() -> pcg.checkPurposeViolations());
            }
        }
        double[][] R = new double[m][2];
        for (int i = 0; i < m; ++i) {
            for (int j = 0; j < N; ++j) {
                R[i][0] += M[i][j];
            }
            R[i][0] /= N;
            for (int j = 0; j < N; ++j) {
                double x = M[i][j] - R[i][0];
                R[i][1] += x*x;
            }
            R[i][1] = Math.sqrt(R[i][1] / (N - 1));
        }
        return R;
    }

    public static double[][] benchmarkHierarchyVioMini(String[] args, int N) throws Exception {
        int m = args.length;
        double[][] M = new double[m][N];
        for (int i = 0; i < m; ++i) {
            for (int j = 0; j < N; ++j) {
                HierarchyPurposeCallGraph pcg = new HierarchyPurposeCallGraph(args[i], args[i] + "/PurposeFile.txt", 6, false);
                M[i][j] = measure(() -> pcg.checkPurposeViolationsAndMinimal());
            }
        }
        double[][] R = new double[m][2];
        for (int i = 0; i < m; ++i) {
            for (int j = 0; j < N; ++j) {
                R[i][0] += M[i][j];
            }
            R[i][0] /= N;
            for (int j = 0; j < N; ++j) {
                double x = M[i][j] - R[i][0];
                R[i][1] += x*x;
            }
            R[i][1] = Math.sqrt(R[i][1] / (N - 1));
        }
        return R;
    }

    public static int[] generateInput(int n) {
        return IntStream.range(1,n+1).toArray();
    }

    public static void writeCsv (long[] ns, double[][] res, String filename) {
        File f = new File(filename);
        try (PrintWriter pw = new PrintWriter(f);) {
            for (int i = 0; i < ns.length; ++i) {
                String[] fields = new String[] {
                Long.toString(ns[i]),
                String.format("%.17f",res[i][0]),
                String.format("%.17f",res[i][1])
            };
            pw. println (String.join ("/",fields));
            }
        }
        catch (FileNotFoundException e) {
            e.printStackTrace();
        }
    }

    public static void writeLatexTabular(long[] ns, double[][] res, String filename) {
        File f = new File(filename);
        try (PrintWriter pw = new PrintWriter(f)) {
            pw.println("\\begin{tabular}{rrr}");
            pw.println("$n$& Average & Standard deviation" + "\\\\\\hline");
            for (int i = 0; i < ns.length; i++) {
                String[] fields = new String[] {
                    Long.toString(ns[i]),
                    String.format("%.6f", res[i][0]),
                    String.format("%.6f", res[i][1])
                };
                pw.println(String.join(" & ",fields) + "\\\\");
            }
            pw.println("\\end{tabular}");
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
    }

    public static long countLines(Path file) {
        long lines = 0;
        try {
            lines = Files.lines(file).count();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return lines;
    }

    public static ArrayList<Long> countLines(ArrayList<Long> count, Path dir) {
        try (DirectoryStream<Path> stream = Files.newDirectoryStream(dir)) {
            for (Path path : stream) {
                if (path.toFile().isDirectory()) {
                    countLines(count, path);
                } else {
                    count.add(countLines(path));
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return count;
    }

    public static void measurementsLines(String[] args) {
        int N = 5;
        PurposeParser parser = new PurposeParser();
        double[][] resBase, res, resHierarchy, resCom;

        benchmark(PurposeCallGraph::new, args, 1);
        
        resCom = benchmarkTri(PurposeCallGraph::new, args, N);
        writeCsv(ns, resCom, "parse_lines_compile.csv");
        writeLatexTabular(ns, resCom, "parse_lines_compile_tabular.tex");
        
        res = benchmark(PurposeCallGraph::new, args, N);
        writeCsv(ns, res, "parse_lines.csv");
        writeLatexTabular(ns, res, "parse_lines_tabular.tex");
        
        resBase = benchmark(parser::baseLevelParsing, args, N);
        writeCsv(ns, resBase, "parse_base_lines.csv");
        writeLatexTabular(ns, resBase, "parse_base_lines_tabular.tex"); 

        resHierarchy = benchmarkBiconsumer((t, u) -> {
                try {
                    new HierarchyPurposeCallGraph(t, u);
                } catch (Exception e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
            }, args, N);
        writeCsv(ns, resHierarchy, "parse_lines_hierarchy_new.csv");
        writeLatexTabular(ns, resHierarchy, "parse_lines_hierarchy_new_tabular.tex"); 
    }

    private static void setLineCount(String[] args) {
        long[] ns = new long[args.length];
        for (int i = 0; i < ns.length; i++) {
            ArrayList<Long> list = new ArrayList<>();
            long count = 0;
            for(long l : countLines(list, Paths.get(args[i]))) {
                count += l;
            }
            ns[i] = count;
            System.out.println(ns[i]);
        }
    }

    public static void measureConcurrent(String [] args) {
       /*  int N = 3;
        double[][] res2, res4, res6, res8, res12;

        measureDirSize("C:/Users/Mia/Documents/GitHub/Contra/src/Test/MethodCallsTest");
        
        res2 = benchmarkTri(PurposeCallGraph::new, args, N, 2);
        writeCsv(ns, res2, "parse_lines_2threads.csv");
        writeLatexTabular(ns, res2, "parse_lines_2threads_tabular.tex");

        measureDirSize("C:/Users/Mia/Documents/GitHub/Contra/src/Test/MethodCallsTest");

        res4 = benchmarkTri(PurposeCallGraph::new, args, N, 4);
        writeCsv(ns, res4, "parse_lines_4threads.csv");
        writeLatexTabular(ns, res4, "parse_lines_4threads_tabular.tex");

        measureDirSize("C:/Users/Mia/Documents/GitHub/Contra/src/Test/MethodCallsTest");

        res6 = benchmarkTri(PurposeCallGraph::new, args, N, 6);
        writeCsv(ns, res6, "parse_lines_6threads.csv");
        writeLatexTabular(ns, res6, "parse_lines_6threads_tabular.tex");

        measureDirSize("C:/Users/Mia/Documents/GitHub/Contra/src/Test/MethodCallsTest");

        res8 = benchmarkTri(PurposeCallGraph::new, args, N, 8);
        writeCsv(ns, res8, "parse_lines_8threads.csv");
        writeLatexTabular(ns, res8, "parse_lines_8threads_tabular.tex");

        measureDirSize("C:/Users/Mia/Documents/GitHub/Contra/src/Test/MethodCallsTest");

        res12 = benchmarkTri(PurposeCallGraph::new, args, N, 12);
        writeCsv(ns, res12, "parse_lines_12threads.csv");
        writeLatexTabular(ns, res12, "parse_lines_12threads_tabular.tex"); */
    }

    public static void measureAlgorithmsLines(String[] args) {
        int N = 5;
        double[][] resVio, resMin, resH, resHMin;

       /*  resHMin = benchmarkHierarchyVioMini(args, N);
        writeCsv(ns, resHMin, "vio_min_hierarchy_lines.csv");
        writeLatexTabular(ns, resHMin, "vio_min_hierarchy_tabular.tex");
         */
        /* resH = benchmarkHierarchyVio(args, N);
        writeCsv(ns, resH, "vio_hierarchy_lines.csv");
        writeLatexTabular(ns, resH, "vio_hierarchy_tabular.tex"); */
        
       /*  resMin = benchmarkVioMini(args, N);
        writeCsv(ns, resMin, "vio_min_lines.csv");
        writeCsv(ns, resMin, "vio_min_lines_tabular.tex");
         */
        resVio = benchmarkVio(args, N);
        writeCsv(ns, resVio, "vio_lines.csv");
        writeLatexTabular(ns, resVio, "vio_lines_tabular.tex");
    }

    public static void measureAlgorithmsAnno(String[] args) {
        int N = 5;
        long [] annos = {0,33,65,128,179,207};
        double[][] resVio, resMin, resH, resHMin;

        /* resH = benchmarkHierarchyVio(args, N);
        writeCsv(annos, resH, "checkVio_hierarchy_anno.csv");
        writeLatexTabular(annos, resH, "checkVio_hierarchy_anno_tabular.tex");

        resHMin = benchmarkHierarchyVioMini(args, N);
        writeCsv(annos, resHMin, "checkVioMin_hierarchy_anno.csv");
        writeLatexTabular(annos, resHMin, "checkVioMin_hierarchy_anno_tabular.tex"); */
        
        resVio = benchmarkVio(args, N);
        writeCsv(annos, resVio, "checkVio_anno.csv");
        writeLatexTabular(annos, resVio, "checkVio_anno_tabular.tex");

        resMin = benchmarkVioMini(args, N);
        writeCsv(annos, resMin, "checkVioMin_anno.csv");
        writeLatexTabular(annos, resMin, "chechVioMin_anno_tabular.tex");  
    }

    public static void measurementsParsingAnno(String[] args) {
        int N = 5;
        long [] annos = {0,33,65,128,179,207};
        double[][] resBase, res, resHierarchy, resCom, res6;

        PurposeParser parser = new PurposeParser();
        benchmark(PurposeCallGraph::new, args, 1);
        
        resCom = benchmarkTri(PurposeCallGraph::new, args, N);
        writeCsv(annos, resCom, "parse_anno_compile.csv");
        writeLatexTabular(annos, resCom, "parse_anno_compile_tabular.tex");
        
        res = benchmark(PurposeCallGraph::new, args, N);
        writeCsv(annos, res, "parse_anno.csv");
        writeLatexTabular(annos, res, "parse_anno_tabular.tex");

        res6 = benchmarkTri(PurposeCallGraph::new, args, N);
        writeCsv(annos, res6, "parse_anno_6threads.csv");
        writeLatexTabular(annos, res6, "parse_anno_6threads_tabular.tex");
        
        resBase = benchmark(parser::baseLevelParsing, args, N);
        writeCsv(annos, resBase, "parse_base_anno.csv");
        writeLatexTabular(annos, resBase, "parse_base_anno_tabular.tex"); 

        resHierarchy = benchmarkBiconsumer((t, u) -> {
                try {
                    new HierarchyPurposeCallGraph(t, u);
                } catch (Exception e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
            }, args, N);
        writeCsv(annos, resHierarchy, "parse_hierarchy_anno.csv");
        writeLatexTabular(annos, resHierarchy, "parse_hierarchy_anno_tabular.tex");
    }

    public static long[] getNumberOfAnnotations(PurposeCallGraph[] args) {
        long[] sizes = new long[args.length]; 
        for (int i = 0; i < args.length; i++) {
            sizes[i] = args[i].getPurposeMapSize();
            System.out.println(args[i]);
            System.out.println("Method calls: " + args[i].getCallGraphSize());
            System.out.println("Anno: " + sizes[i]);
        }
        return sizes;
    }

    public static void main(String[] args) throws InterruptedException {
        String [] inputLines = {"C:/Users/Mia/Documents/Annotated-teammates/Lines1", "C:/Users/Mia/Documents/Annotated-teammates/Lines2", "C:/Users/Mia/Documents/Annotated-teammates/Lines3",
        "C:/Users/Mia/Documents/Annotated-teammates/Lines4", "C:/Users/Mia/Documents/Annotated-teammates/Lines5", "C:/Users/Mia/Documents/Annotated-teammates/Lines6"};
       
        String[] inputAnno = {"C:/Users/Mia/Documents/Annotated-teammates/Anno1", "C:/Users/Mia/Documents/Annotated-teammates/Anno2",
        "C:/Users/Mia/Documents/Annotated-teammates/Anno3", "C:/Users/Mia/Documents/Annotated-teammates/Anno4", "C:/Users/Mia/Documents/Annotated-teammates/Anno5", 
        "C:/Users/Mia/Documents/Annotated-teammates/Anno6"};

        String[] args1 = {"C:/Users/Mia/Desktop/VioCheck/VioCheck/src/AnnotatedTeammates"};
        setLineCount(args1);

        //measurementsParsingAnno(inputAnno);
        //setLineCount(inputAnno);
        //measureAlgorithmsAnno(inputAnno);
        // measurementsLines(inputLines);
        //measureConcurrent(inputLines);
        //measureAlgorithmsLines(inputLines);
    }
}
