import java.util.HashSet;
import edu.princeton.cs.algs4.DirectedCycle;
import edu.princeton.cs.algs4.SymbolDigraph;
import edu.princeton.cs.algs4.TransitiveClosure;

/**
 * A HierarchyPurposeCallGraph constructed with the use of a PurposeParser instance. 
 * The purpose call graph is constructed with a purpose hierarchy.
 * The HierarchyPurposeCallGraph implements a Purpose Violation algorithm and a Purpose Inference algorithm.
 * 
 * @author Mia Heine Nowack
 * @version 1.0  
 */

public class HierarchyPurposeCallGraph extends PurposeCallGraph {
    private SymbolDigraph poSet;  
    private TransitiveClosure hierarchy;
    private boolean violation;

    public HierarchyPurposeCallGraph(String input, String purposeFile) throws Exception {
        super(input);
        this.poSet = new SymbolDigraph(purposeFile, ", ");
        this.hierarchy = new TransitiveClosure(poSet.digraph());
        DirectedCycle finder = new DirectedCycle(poSet.digraph());
        if(finder.hasCycle()) {
            System.err.print("The Purpose Hierarchy is not valid since there is a cycle between: ");
            finder.cycle().forEach(p -> System.err.print(poSet.nameOf(p) + " "));
            throw new Exception();
        }
        violation = false;
    }

    public HierarchyPurposeCallGraph(String input, String purposeFile, int poolSize, boolean compile) throws Exception {
        super(input, poolSize, compile);
        this.poSet = new SymbolDigraph(purposeFile, ", ");
        this.hierarchy = new TransitiveClosure(poSet.digraph()); 
        DirectedCycle finder = new DirectedCycle(poSet.digraph());
        if(finder.hasCycle()) {
            System.err.print("The Purpose Hierarchy is not valid since there is a cycle between: ");
            finder.cycle().forEach(p -> System.err.print(poSet.nameOf(p) + " "));
            throw new Exception();
        }
        violation = false;    
    }

    /**
     * Checks if a purpose of the caller method is reachable from a purpose for the method call.
     * @param mp a purpose for the method m
     * @param mcp a purpose for the method call mc
     * @return true or false
     */
    public boolean isReachable(String mp, String mcp) {
        try {
            return hierarchy.reachable(poSet.indexOf(mp), poSet.indexOf(mcp));
        } catch (NullPointerException | IllegalArgumentException e) {
            System.err.println("[ERROR]: " + mp + " or " + mcp + " is not defined in the purpose hierarchy");
            return false;
        }
    }
    
    /**
     * Uses the call graph, purpose map and hierarchy to detect purpose violations.
     */
    @Override
    public int checkPurposeViolations() {
        methodCalls.forEach((m, C) -> { 
        for (String mc : C) {
            if (!purposes.containsKey(mc)) { 
                mc = parser.resolveInheritance(mc);
            } if(mc != null) {
                if(purposes.get(mc).isEmpty()) {
                    violationCount ++;
                    System.err.println("[WARNING]: " + mc + " should be annotated, ");
                    System.err.println("           it is called in " + m + " for the purpose(s) " + purposes.get(m));
                }
                for (String mcp : purposes.get(mc)) {
                violation = true;
                for(String mp : purposes.get(m)) {
                    if(isReachable(mp, mcp)) {
                        violation = false;
                    }                        
                } if(violation) {
                    violationCount ++;
                    System.err.println("[WARNING]: The call to method: " + mc + " with purpose " + mcp);
                    System.err.println("           violates the purpose of: "  + m);
                }
            }
        }
        } 
    });
    System.out.println("[INFO]: " + violationCount + " purpose violations detected");
    return violationCount;        
    }

    /**
     * Uses the map purposes and methodCalls to detect purpose violations and minimal purposes
     * in the parsed directory.
     */
    @Override
    public int checkPurposeViolationsAndMinimal() {
        methodCalls.forEach((m, C) -> { // key, value
            HashSet<String> MCP = new HashSet<>();
            boolean check = false;
            for (String mc : C) {
                if (!purposes.containsKey(mc)) { 
                    mc = parser.resolveInheritance(mc);
                 } if(mc != null) { 
                    if(purposes.get(mc).isEmpty()) {
                        violationCount ++;
                        System.err.println("[WARNING]: " + mc + " should be annotated, ");
                        System.err.println("           it is called in " + m + " for the purpose(s) " + purposes.get(m));
                    } else {
                        for (String mcp : purposes.get(mc)) {
                            MCP.add(mcp); 
                            violation = true;
                            check = true;
                            for(String mp : purposes.get(m)) {
                                if(isReachable(mp, mcp)) {
                                    violation = false;
                                }                        
                            } if(violation) {
                                check = false;
                                violationCount ++;
                                System.err.println("[WARNING]: The call to method: " + mc + " with purpose " + mcp);
                                System.err.println("           violates the purpose of: "  + m);
                            }
                        }
                    }
                }
            } if(check)
                checkMinimalPurpose(purposes.get(m), MCP, m);
        });
        System.out.println("[INFO]:    " + violationCount + " purpose violations detected");
        return violationCount;
    }

    /**
     * Takes two sets of purposes as input, and checks if every purpose in MP is reachable from at least one purpose in MCP.
     * @param MP the set of purposes for a single method
     * @param MCP the set of purposes for all methods called within the single method
     * @param m the name/identifier of the single method
     */
    private void checkMinimalPurpose(HashSet<String> MP, HashSet<String> MCP, String m) {
        HashSet<String> notMinimal = new HashSet<>();
        HashSet<String>  minimalMP = new HashSet<>();
        MP.forEach(p -> {
            boolean minimal = false;
            for(String mcp : MCP) {
                if(isReachable(p, mcp)) {
                    minimal = true;
                    minimalMP.add(p);
                }                
            }
            if(!minimal) {
                notMinimal.add(p);
            }}); 
        
        if(!notMinimal.isEmpty()) {
            System.out.println("[INFO]:    " + m + " does not use data for the purpose(s): " + notMinimal);
            System.out.println("           The minimal set of purposes is " + minimalMP); 
        }    
    }

    /**
     * Methods used to test the purpose violation algorithm
     * @return violations, the set of purpose violations detected by the algorithm
     */
    @Override
    public HashSet<String> checkPurposeViolationsTest() {
        HashSet<String> violations = new HashSet<>();
        methodCalls.forEach((m, C) -> { 
            HashSet<String> MCP = new HashSet<>();
            boolean check = true;
            for (String mc : C) {
                if (!purposes.containsKey(mc)) { 
                    mc = parser.resolveInheritance(mc);
                } if(mc != null) {
                    for (String mcp : purposes.get(mc)) {
                        MCP.add(mcp); 
                        violation = true;
                        for(String mp : purposes.get(m)) {
                            if(hierarchy.reachable(poSet.indexOf(mp), poSet.indexOf(mcp))) {
                                violation = false;
                            }                        
                        }
                        if(violation) {
                            check = false;
                            violationCount ++;
                            violations.add(mcp + " " + mc + " " + m);
                            System.err.println("[WARNING]: Purpose violation! The call to method: " + mc + " with purpose " + mcp);
                            System.err.println("violates the purpose of method:"  + m);
                        }
                    }
                }
            } if(check)
                checkMinimalPurpose(purposes.get(m), MCP, m);
        });
        System.out.println("[INFO]:    " + violationCount + " purpose violations detected");
        return violations;
    }
}
