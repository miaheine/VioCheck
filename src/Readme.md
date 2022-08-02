## VioCheck

The command-line tool VioCheck can parse purpose annotated Java files, and detect purpose violations in the source code. It can also detect the minimal purposes for each annotated method in a code base.

#### Instructions on how to run the tool

1. Add the local path to the directory VioCheck as an environmental varible named VIOCHECK_HOME
2. Open the src folder on the command-line, and use the following commands to compile and run the tool:

   - compile \*.java
   - jrun VioCheck _path to input directory_ -[options]

3. The -[options] for the tool are:

   - -c or --compile _name of the .java file(s) to compile or "\*.java"_
   - -w or --warning
   - -h or --hierarchy
   - -t or --threads _number of threads_
   - -p or --print

4. The paths to the directories WebShopExample, and Test can be used as _path to input directory_.

#### Overview of the Java Classes

    - VioCheck.java is the main method from were the functionality of the tool is executed.
    - PurposeParser.java is the parser that generates the parse tree and collects method calls and purpose annotations, such that the call graph can be created.
    - PurposeListener.java implements an ANLTR4 listener interface, and it collects the method calls and purpose annotations during traversal of the parse tree.
    - ExecutorPurposeParser.java extends the PurposeParser with a threadpool and concurrent parsing.
    - PurposeCallGraph.java represents a call graph and implements a purpose violation algorithm, and a purpose inference algorithm.
    - HierarchyPurposeCallGraph.java extends the PurposeCallGraph with a purpose hierarchy.
