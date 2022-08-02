import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import java.util.HashSet;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import edu.princeton.cs.algs4.SymbolDigraph;
import edu.princeton.cs.algs4.TransitiveClosure;

/**
 * Junit tests of the purpose hierarchy, call graph and purpose violation algorithm
 */

public class ParserTest {
    private static SymbolDigraph poSet;  
    private static TransitiveClosure hierarchy;

    @BeforeAll
    public static void setupHierarchy() {
        poSet = new SymbolDigraph("C:/Users/Mia/Documents/GitHub/Contra/src/PurposeFile.txt", ", ");
        hierarchy = new TransitiveClosure(poSet.digraph());
    }
    
    //Purpose Violation Algorithm

    @Test
    public void violationAlgorithm_noViolations() {
        PurposeCallGraph pcg = new PurposeCallGraph("C:/Users/Mia/Documents/GitHub/Contra/src/Test/MinimalTest");
        assertTrue(pcg.checkPurposeViolationsTest().isEmpty());
    }

    @Test
    public void violationAlgorithm_directViolation() {
        PurposeCallGraph pcg = new PurposeCallGraph("C:/Users/Mia/Documents/GitHub/Contra/src/Test/MethodCallsTest/Data.java");
        HashSet<String> set = new HashSet<>();
        set.add("research Data.useData(1) Data.printData(1)"); //purpose methodcall method
        assertTrue(pcg.checkPurposeViolationsTest().equals(set));
    }

    @Test
    public void violationAlgorithm_Hierachy_NoViolation() throws Exception {
        PurposeCallGraph pcg = new HierarchyPurposeCallGraph("C:/Users/Mia/Documents/GitHub/Contra/src/Test/HierarchyTest/Record.java","C:/Users/Mia/Documents/GitHub/Contra/src/PurposeFile.txt");
        assertTrue(pcg.checkPurposeViolationsTest().isEmpty());
    }

    @Test
    public void violationAlgorithm_Hierachy_ViolationParent() throws Exception {
        PurposeCallGraph pcg = new HierarchyPurposeCallGraph("C:/Users/Mia/Documents/GitHub/Contra/src/Test/HierarchyTest","C:/Users/Mia/Documents/GitHub/Contra/src/PurposeFile.txt");
        assertTrue(pcg.checkPurposeViolationsTest().contains("study Questionnaire.useAnswers(1) Questionnaire.getAnswers()"));
        
    }

    @Test
    public void violationAlgorithm_Hierachy_ViolationDifferentBranch() throws Exception {
        PurposeCallGraph pcg = new HierarchyPurposeCallGraph("C:/Users/Mia/Documents/GitHub/Contra/src/Test/HierarchyTest","C:/Users/Mia/Documents/GitHub/Contra/src/PurposeFile.txt");
        assertTrue(pcg.checkPurposeViolationsTest().contains("marketing Questionnaire.publishAnswers(1) Questionnaire.useAnswers(1)"));
    }

    //Different method calls in the purpose violation algorithm

    @Test
    public void violationAlgorithm_superClassViolation() {
        PurposeCallGraph pcg = new PurposeCallGraph("C:/Users/Mia/Documents/GitHub/Contra/src/Test/MethodCallsTest");
        assertTrue(pcg.checkPurposeViolationsTest().contains("marketing Animal.eat() Dog.work()"));
    }

    @Test
    public void violationAlgorithm_thisViolation() {
        PurposeCallGraph pcg = new PurposeCallGraph("C:/Users/Mia/Documents/GitHub/Contra/src/Test/MethodCallsTest");
        assertTrue(pcg.checkPurposeViolationsTest().contains("research Calls.callLocal() Calls.callThis()"));
    }

    @Test
    public void violationAlgorithm_fieldViolation() {
        PurposeCallGraph pcg = new PurposeCallGraph("C:/Users/Mia/Documents/GitHub/Contra/src/Test/MethodCallsTest");
        assertTrue(pcg.checkPurposeViolationsTest().contains("marketing Data.useData(1) Calls.callField()"));
    }

    @Test
    public void violationAlgorithm_arrayObjectViolation() {
        PurposeCallGraph pcg = new PurposeCallGraph("C:/Users/Mia/Documents/GitHub/Contra/src/Test/MethodCallsTest");
        assertTrue(pcg.checkPurposeViolationsTest().contains("marketing Data.getData() Calls.callArrayObject()"));
    }

    @Test
    public void violationAlgorithm_methodReferenceViolation() {
        PurposeCallGraph pcg = new PurposeCallGraph("C:/Users/Mia/Documents/GitHub/Contra/src/Test/MethodCallsTest");
        assertTrue(pcg.checkPurposeViolationsTest().contains("research MethodReference.threadStatus() MethodReference.mRefCall()"));
    }

    @Test
    public void violationAlgorithm_lambdaViolation() {
        PurposeCallGraph pcg = new PurposeCallGraph("C:/Users/Mia/Documents/GitHub/Contra/src/Test/MethodCallsTest");
        assertTrue(pcg.checkPurposeViolationsTest().contains("marketing Calls.add(2) Calls.callLambda()"));
    }

    @Test
    public void violationAlgorithm_arrayListViolation() {
        PurposeCallGraph pcg = new PurposeCallGraph("C:/Users/Mia/Documents/GitHub/Contra/src/Test/MethodCallsTest");
        assertTrue(pcg.checkPurposeViolationsTest().contains("marketing Data.getData() Calls.callArrayList()"));
    }

    @Test
    public void violationAlgorithm_hashMapViolation() {
        PurposeCallGraph pcg = new PurposeCallGraph("C:/Users/Mia/Documents/GitHub/Contra/src/Test/MethodCallsTest");
        assertTrue(pcg.checkPurposeViolationsTest().contains("marketing Data.getData() Calls.callMapObject()"));
    }

    @Test
    public void violationAlgorithm_hashMapLoopViolation() {
        PurposeCallGraph pcg = new PurposeCallGraph("C:/Users/Mia/Documents/GitHub/Contra/src/Test/MethodCallsTest");
        assertTrue(pcg.checkPurposeViolationsTest().contains("marketing Data.getData() Calls.callMapLoop()"));
    }


    @Test
    public void violationAlgorithm_lambdaObjectViolation() {
        PurposeCallGraph pcg = new PurposeCallGraph("C:/Users/Mia/Documents/GitHub/Contra/src/Test/MethodCallsTest");
        assertTrue(pcg.checkPurposeViolationsTest().contains("marketing Data.getData() Calls.callLambdaObject()"));
    }

    //Hierarchy

    @Test
    public void hierarchy_childReachable() {
        assertTrue(hierarchy.reachable(poSet.indexOf("research"), poSet.indexOf("study")));
    }

    @Test
    public void hierarchy_grandChildReachable() {
        assertTrue(hierarchy.reachable(poSet.indexOf("research"), poSet.indexOf("interview")));
    }

    @Test
    public void hierarchy_parentNotReachable() {
        assertFalse(hierarchy.reachable(poSet.indexOf("study"), poSet.indexOf("research")));
    }

    @Test
    public void hierarchy_unRelatedNotReachable() {
        assertFalse(hierarchy.reachable(poSet.indexOf("research"), poSet.indexOf("marketing")));
    }    
}
