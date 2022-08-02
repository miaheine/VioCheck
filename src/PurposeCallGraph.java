import java.util.HashMap;
import java.util.HashSet;
import java.util.concurrent.ConcurrentHashMap;

/**
 * A Purpose Call Graph constructed with the use of a PurposeParser instance. 
 * The Purpsoe Call Graph implements a Purpose Violation algorithm and a Purpose Inference algorithm.
 * 
 * @author Mia Heine Nowack
 * @version 1.0  
 */

public class PurposeCallGraph {
    protected ConcurrentHashMap<String, HashSet<String>> purposes;
    protected ConcurrentHashMap<String, HashSet<String>> methodCalls;
    protected int violationCount;
    protected PurposeParser parser;
    protected HashMap<String, HashSet<String>> inferredPurposes;

    public PurposeCallGraph(String input) {
        parser = new PurposeParser();
        parser.parseInput(input);
        this.purposes = parser.getPurposeMap();
        this.methodCalls = parser.getMethodCalls();
        this.violationCount = 0;
    }

    public PurposeCallGraph(String input, int poolSize, boolean compile) {
        if(compile) {
            parser = new PurposeParserCompiler(poolSize);
        } else {
            parser = new ExecutorPurposeParser(poolSize);
        }
        parser.parseInput(input);
        this.purposes = parser.getPurposeMap();
        this.methodCalls = parser.getMethodCalls();
        this.violationCount = 0;
        ((ExecutorPurposeParser) parser).shutdown();
    }

    public static void main(String[] args) {
        PurposeCallGraph CG = new PurposeCallGraph("C:/Users/Mia/Documents/GitHub/Contra/src/Test/MethodCallsTest");
        CG.checkPurposeViolationsAndMinimal();
    }

    public void deleteAndRename() {
        ((PurposeParserCompiler) parser).deleteAndRenameFiles();
    }

    public int getCallGraphSize() {
        return methodCalls.size();
    }

    public int getNumberOfMethodCalls() {
        int size = 0;
        for(HashSet<String> C : methodCalls.values()) {
            size += C.size();
        }
        return size;
    }

    public int getPurposeMapSize() {
        int size = 0;
        for (HashSet<String> P : purposes.values()) {
           if(!P.isEmpty()) {
                size++;
            } 
        }
        return size;
    }

    /**
     * Uses the map purposes and methodCalls to detect purpose violations and minimal purposes
     * in the parsed directory.
     */
    public int checkPurposeViolationsAndMinimal() {
        violationCount = 0;
        methodCalls.forEach((m, C) -> { // key, value
            boolean check = false;
            HashSet<String> MCP = new HashSet<>(); //purposes for all method calls in m
            for (String mc : C) {
                if (!purposes.containsKey(mc)) { // avoids built-in methods and library methods
                    mc = parser.resolveInheritance(mc);
                } if(mc != null) {
                    if(purposes.get(mc).isEmpty()) {
                        violationCount ++;
                        System.err.println("[WARNING]: " + mc + " should be annotated, ");
                        System.err.println("           it is called in " + m + " for the purpose(s) " + purposes.get(m));
                    } else {
                        for (String mcp : purposes.get(mc)) { 
                            check = true;
                            MCP.add(mcp);
                            if (!purposes.get(m).contains(mcp)) {
                                check = false; 
                                violationCount ++;
                                System.err.println("[WARNING]: The call to method: " + mc + " with purpose " + mcp);
                                System.err.println("           violates the purpose of: "  + m);
                            }
                        }
                    }
                   
                } 
            } if(check) {
               checkMinimalPurpose(purposes.get(m), MCP, m); 
            }            
        });
        System.out.println("[INFO]:    "+ violationCount + " purpose violations detected");
        return violationCount;
    }

    /**
     * Uses the map purposes and methodCalls to detect purpose violations
     * in the parsed directory.
     */
    public int checkPurposeViolations() {
        violationCount = 0;
        methodCalls.forEach((m, C) -> { // key, value
            for (String mc : C) {
                if (!purposes.containsKey(mc)) { // avoids built-in methods like println and methods with no purpose
                    mc = parser.resolveInheritance(mc);
                } if(mc != null) { 
                    if(purposes.get(mc).isEmpty()) {
                        violationCount ++;
                        System.err.println("[WARNING]: " + mc + " should be annotated, ");
                        System.err.println("           it is called in " + m + " for the purpose(s) " + purposes.get(m));
                    }
                    for (String mcp : purposes.get(mc)) { 
                        if (!purposes.get(m).contains(mcp)) { 
                            violationCount ++;
                            System.err.println("[WARNING]: The call to method: " + mc + " with purpose " + mcp);
                            System.err.println("           violates the purpose of: "  + m);
                        }
                    }
                }
            }
        });
        System.out.println("[INFO]:    "+ violationCount + " purpose violations detected");
        return violationCount;
    }

    /**
     * Takes two sets of purposes as input, and checks if MP is a subset of MCP.
     * @param MP the set of purposes for a single method
     * @param MCP the set of purposes for all methods called within the single method
     * @param m the name/identifier of the single method
     */
    private void checkMinimalPurpose(HashSet<String> MP, HashSet<String> MCP, String m) {
        HashSet<String> notMinimal = new HashSet<>();
        HashSet<String>  minimalMP = new HashSet<>();
        for (String p : MP) {
            if(!MCP.contains(p)) {
                notMinimal.add(p);
            } else {
                minimalMP.add(p);
            }
        } if(!notMinimal.isEmpty()) {
            System.out.println("[INFO]:    " + m + " does not use data for the purpose(s): " + notMinimal);
            System.out.println("           The minimal set of purposes is " + minimalMP); 
        }
    }

    /**
     * Pretty printing of the elements from the two hashMaps methodCalls and purposes.
     */
    public void printPurposesAndMethodCalls() {
        System.out.println();
        System.out.println("Methods and purposes:");
        purposes.forEach((key, value) -> {
            if(!value.isEmpty()) {
               System.out.println(key + " -> " + value); 
            }
        }); 

        System.out.println("---------------------------------------");

        System.out.println("Methods and methodCalls:");
        methodCalls.forEach((key, value) -> {
            System.out.println(key + " -> " + value);
        });
    }






    ///////////// The following section provides methods used for benchmarking and testing /////////////

    public HashSet<String> checkPurposeViolationsTest() {
        HashSet<String> violations = new HashSet<>();
        violationCount = 0;
        methodCalls.forEach((m, C) -> { // key, value
            HashSet<String> MCP = new HashSet<>(); //map for purposes for the method calls
            for (String mc : C) {
                if (!purposes.containsKey(mc)) { // avoids built-in methods like println and methods with no purpose
                    mc = parser.resolveInheritance(mc);
                } if(mc != null) { // avoids built-in methods like println and methods with no purpose
                    for (String mcp : purposes.get(mc)) { 
                        MCP.add(mcp);
                        if (!purposes.get(m).contains(mcp)) { 
                            violationCount ++;
                            violations.add(mcp + " " + mc + " " + m);
                            System.err.println("[WARNING]: Purpose violation! The call to method: " + mc + " with purpose " + mcp);
                            System.err.println("violates the purpose of method: " + m);
                        }
                    }
                }
            }
            //Checks if the annotated set of purposes for m, is the minimal set of purposes
            checkMinimalPurpose(purposes.get(m), MCP, m);
        });
        return violations;
    }
}
