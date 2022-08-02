import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Stack;
import java.util.concurrent.ConcurrentHashMap;
import org.antlr.v4.runtime.TokenStream;
import org.antlr.v4.runtime.TokenStreamRewriter;

/**
 * A PurposeListener which collects method purposes and method calls during traversal of the parse tree. 
 * @author Mia Heine Nowack
 * @version 2.0
 */

public class PurposeListener extends Java9ParserBaseListener {
    private HashMap<String, HashSet<String>> methodPurposes;
    private HashMap<String, HashSet<String>> callGraph;
    private Stack<HashSet<String>> methodCalls;
    private HashMap<String, String> globalObjects;
    private HashMap<String, String> localObjects;
    private ConcurrentHashMap<String, String> inheritance;
    private String className;
    private String superClass;
    public TokenStreamRewriter rewriter;

    public PurposeListener(TokenStream tokens, ConcurrentHashMap<String, String> inheritance) {
        methodPurposes = new HashMap<>();
        callGraph = new HashMap<>();
        methodCalls = new Stack<>();
        globalObjects = new HashMap<>();
        localObjects = new HashMap<>();
        rewriter = new TokenStreamRewriter(tokens);
        this.inheritance = inheritance;
    }

    /**
     * Retrieves the class name
     */
    public void enterNormalClassDeclaration(Java9Parser.NormalClassDeclarationContext ctx) {
        try {
            className = ctx.identifier().getText();
            if(ctx.superclass() != null) {
                superClass = ctx.superclass().classType().getText();
                inheritance.put(className, superClass);
            } 
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Retrieves the interface name
     */
    public void enterNormalInterfaceDeclaration(Java9Parser.NormalInterfaceDeclarationContext ctx) {
        try {
            className = ctx.identifier().getText();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Adds an empty set onto the methodCalls stack, such that method calls within the current method can be saved.
     */
    public void enterMethodDeclaration(Java9Parser.MethodDeclarationContext ctx) {
        methodCalls.push(new HashSet<String>());
    }

    /**
     * Adds the method name and set of purposes to the map methodPurposes.
     * The method name and method calls within the method are added to the call graph.
     */
    public void exitMethodDeclaration(Java9Parser.MethodDeclarationContext ctx) {
        try {
            String methodName = className + "." + ctx.methodHeader().methodDeclarator().identifier().getText()
                        + getParams(ctx);
            HashSet<String> purposes = new HashSet<>();
            if(ctx.methodHeader().methodPurpose() != null) {
                Iterator<Java9Parser.PurposeContext> iter = ctx.methodHeader().methodPurpose().purpose().iterator();
                iter.forEachRemaining(p -> {
                    purposes.add(p.getText());
                });
                methodPurposes.put(methodName, purposes);
                if(!methodCalls.peek().isEmpty()) {
                  callGraph.put(methodName, methodCalls.pop());  
                }
                
            } else { //Unannotated methods are also added
                methodPurposes.put(methodName, purposes);
            }
            localObjects.clear();
            
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Uses the TokenStreamRewriter to remove the set of purposes from all method declarations.
     */
    public void enterMethodPurpose(Java9Parser.MethodPurposeContext ctx) {
        rewriter.delete(ctx.start, ctx.stop);
    }

    /**
     * Checks if the method takes any parameters. If it does and any of the parameters are instances of a class, the parameter is added to the map localObjects as a pair 
     * of the parameter name and the class name.
     */
    public void enterMethodDeclarator(Java9Parser.MethodDeclaratorContext ctx) {
        try {
            if(ctx.formalParameterList() != null) { //check if the method takes any parameters
                if(ctx.formalParameterList().formalParameters() != null) { //several parameters
                    Iterator<Java9Parser.FormalParameterContext> iter = ctx.formalParameterList().formalParameters().formalParameter().iterator();
                    iter.forEachRemaining(param -> { //check that the parameter is an instance of a class or interface
                        if(param.unannType() != null && param.unannType().unannReferenceType() != null && param.unannType().unannReferenceType().unannClassOrInterfaceType() != null) {
                            localObjects.put(param.variableDeclaratorId().getText(), param.unannType().getText());
                        }
                    });
                    
                } if(ctx.formalParameterList().lastFormalParameter() != null) { //one parameter or last of several
                    if(ctx.formalParameterList().lastFormalParameter().unannType() != null && ctx.formalParameterList().lastFormalParameter().unannType().unannReferenceType() != null && ctx.formalParameterList().lastFormalParameter().unannType().unannReferenceType().unannClassOrInterfaceType() != null) {
                        localObjects.put(ctx.formalParameterList().lastFormalParameter().variableDeclaratorId().getText(), ctx.formalParameterList().lastFormalParameter().unannType().getText());
                    } else if (ctx.formalParameterList().lastFormalParameter().formalParameter().unannType() != null && ctx.formalParameterList().lastFormalParameter().formalParameter().unannType().unannReferenceType() != null && ctx.formalParameterList().lastFormalParameter().formalParameter().unannType().unannReferenceType().unannClassOrInterfaceType() != null) {
                        localObjects.put(ctx.formalParameterList().lastFormalParameter().formalParameter().variableDeclaratorId().getText(), ctx.formalParameterList().lastFormalParameter().formalParameter().unannType().getText());
                    } 
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Checks if any of the local variables declared in the method are instances of a class.
     * If so, the local variable is added to the map localObjects, as a pair of the variable name and class name.
     */
    public void enterLocalVariableDeclaration(Java9Parser.LocalVariableDeclarationContext ctx) {
        try {
            String classInstance = "";
            if(ctx.unannType() != null && ctx.unannType().unannReferenceType() != null) {
                if(ctx.unannType().unannReferenceType().unannClassOrInterfaceType() != null) {
                    classInstance = ctx.unannType().unannReferenceType().unannClassOrInterfaceType().getText();
                
                } else if(ctx.unannType().unannReferenceType().unannArrayType() != null && ctx.unannType().unannReferenceType().unannArrayType().unannClassOrInterfaceType() != null) {
                    classInstance = ctx.unannType().unannReferenceType().unannArrayType().unannClassOrInterfaceType().getText();
                }
                if(classInstance.length() > 0 && ctx.variableDeclaratorList() != null) {
                    final String value = classInstance;
                    Iterator<Java9Parser.VariableDeclaratorContext> iter = ctx.variableDeclaratorList().variableDeclarator().iterator();
                    iter.forEachRemaining(v -> {
                        if(v.variableDeclaratorId() != null)
                            localObjects.put(v.variableDeclaratorId().getText(), value);
                    });  
                }  
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Checks if any fields are instances of a class.
     * If so, the field is added to the map ClassInstances, as a pair of the variable name and class name.
     */
    public void exitFieldDeclaration(Java9Parser.FieldDeclarationContext ctx) {
        try {
            String classInstance = "";
            if(ctx.unannType() != null && ctx.unannType().unannReferenceType() != null) {
                //If class or interface type
                if(ctx.unannType().unannReferenceType().unannClassOrInterfaceType() != null) {
                    //If collection object (ArrayList, HashMap etc)
                    if(ctx.unannType().unannReferenceType().unannClassOrInterfaceType().unannClassType_lfno_unannClassOrInterfaceType() != null && ctx.unannType().unannReferenceType().unannClassOrInterfaceType().unannClassType_lfno_unannClassOrInterfaceType().typeArguments() != null) {
                        int size = ctx.unannType().unannReferenceType().unannClassOrInterfaceType().unannClassType_lfno_unannClassOrInterfaceType().typeArguments().typeArgumentList().typeArgument().size();
                        //If the collection is a hashMap, we get the type of the value and not the key..
                        if(size == 2) {
                            classInstance = ctx.unannType().unannReferenceType().unannClassOrInterfaceType().unannClassType_lfno_unannClassOrInterfaceType().typeArguments().typeArgumentList().typeArgument().get(size-1).getText();
                        } else if(size == 1) {
                            classInstance = ctx.unannType().unannReferenceType().unannClassOrInterfaceType().unannClassType_lfno_unannClassOrInterfaceType().typeArguments().typeArgumentList().getText();
                        }         
                    } else { //If object of a class
                        classInstance = ctx.unannType().unannReferenceType().unannClassOrInterfaceType().getText(); 
                    } 
                    //If array of class instance objects
                } else if(ctx.unannType().unannReferenceType().unannArrayType() != null && ctx.unannType().unannReferenceType().unannArrayType().unannClassOrInterfaceType() != null) {
                    classInstance = ctx.unannType().unannReferenceType().unannArrayType().unannClassOrInterfaceType().getText(); 
                    
                }
                if(classInstance.length() > 0 && ctx.variableDeclaratorList() != null) {
                    final String value = classInstance;
                    Iterator<Java9Parser.VariableDeclaratorContext> iter = ctx.variableDeclaratorList().variableDeclarator().iterator();
                    iter.forEachRemaining(v -> {
                        if(v.variableDeclaratorId() != null)
                            globalObjects.put(v.variableDeclaratorId().getText(), value);
                    });  
                }
            } 
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Adds the variable of the for each loop to the map of local variables.
     */
    public void enterEnhancedForStatement(Java9Parser.EnhancedForStatementContext ctx) {
        try {
            if(ctx.unannType() != null && ctx.variableDeclaratorId() != null) {
                localObjects.put(ctx.variableDeclaratorId().getText(), ctx.unannType().getText());
            }            
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Adds the variable of the for each loop to the map of local variables.
     */
    public void enterEnhancedForStatementNoShortIf(Java9Parser.EnhancedForStatementNoShortIfContext ctx) {
        try {
            if(ctx.unannType() != null && ctx.variableDeclaratorId() != null) {
                localObjects.put(ctx.variableDeclaratorId().getText(), ctx.unannType().getText());
            }            
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    
    /**
     * Counts the number of parameters for a method.
     * @param ctx
     * @return the number of parameters in a parenthesis or "()"
     */
    public String getParams(Java9Parser.MethodDeclarationContext ctx) {
        try {
            String args = ctx.methodHeader().methodDeclarator().formalParameterList().getText();
            int count = args.split(",").length;
            return "(" + count + ")";
        } catch (Exception e) {
            return "()";
        }
    }

    /**
     * Count the number of arguments for a method call.
     * @param ctx
     * @return the number of arguments in a parenthesis or "()"
     */
    private String getArguments(Java9Parser.MethodInvocationContext ctx) {
        try {
            String args = ctx.argumentList().getText();
            int count = args.split(",").length;
            return "(" + count + ")";
        } catch (Exception e) {
            return "()";
        }
    }

    /**
     * Count the number of arguments for a method call.
     * @param ctx
     * @return the number of arguments in a parenthesis or "()"
     */
    private String getArguments(Java9Parser.MethodInvocation_lf_primaryContext ctx) {
        try {
            String args = ctx.argumentList().getText();
            int count = args.split(",").length;
            return "(" + count + ")";
        } catch (Exception e) {
            return "()";
        }
    }

    /**
     * Count the number of arguments for a method call.
     * @param ctx
     * @return the number of arguments in a parenthesis or "()"
     */
    private String getArguments(Java9Parser.MethodInvocation_lfno_primaryContext ctx) {
        try {
            String args = ctx.argumentList().getText();
            int count = args.split(",").length;
            return "(" + count + ")";
        } catch (Exception e) {
            return "()";
        }
    }

    private void addLocalObject(String key, String lambdaParam) {
        if(localObjects.containsKey(key)) {
            localObjects.put(lambdaParam, localObjects.get(key));
        } else if(globalObjects.containsKey(key)) {
            localObjects.put(lambdaParam, globalObjects.get(key));
        }
    }

    private boolean addInstanceMethodCall(String key, String value) {
        if(localObjects.containsKey(key)) { //if instance method call
            methodCalls.peek().add(localObjects.get(key) + "." + value);
            return true;
        } else if(globalObjects.containsKey(key)) { //if instance method call
            methodCalls.peek().add(globalObjects.get(key) + "." + value);
            return true;
        } else {
            return false;
        }
    }

    /**
     * Adds the method name to the set on the stack methodCalls.
     * The class name differs depending on the type of method call.
     * If there is no set on the stack, the method call is outside the scope of a method,
     * and won't be added.
     */
    public void enterMethodInvocation(Java9Parser.MethodInvocationContext ctx) {
        try {
            if(!methodCalls.empty() && className != null) {
                //Method call on super class    
                if(ctx.superKeyword() != null) {
                String methodName = ctx.identifier().getText();
                methodCalls.peek().add(superClass + "." + methodName + getArguments(ctx));

                } else if (ctx.methodName() != null) { //direct method call same class
                    String methodName = ctx.methodName().identifier().getText();
                    methodCalls.peek().add(className + "." + methodName + getArguments(ctx));
                }

                //Static or instance method call, including calls on variables
                else if (ctx.identifier() != null && ctx.typeName() != null) {
                    String methodName = ctx.identifier().getText();
                    String key = ctx.typeName().identifier().getText();
                    if(Character.isUpperCase(ctx.typeName().identifier().getText().charAt(0))) { //if static method call
                        methodCalls.peek().add(ctx.typeName().identifier().getText() + "." + methodName + getArguments(ctx));
                    } else if(ctx.argumentList() != null && ctx.argumentList() != null && ctx.argumentList().expression().size() > 0) { //if lambda
                        Iterator<Java9Parser.ExpressionContext> iter = ctx.argumentList().expression().iterator();
                        iter.forEachRemaining(expr -> {
                            if(expr.lambdaExpression() != null && expr.lambdaExpression().lambdaParameters() != null) {
                                if(expr.lambdaExpression().lambdaParameters().inferredFormalParameterList() != null && expr.lambdaExpression().lambdaParameters().inferredFormalParameterList().identifier().size() == 2) { //only saves second object (the value in map)
                                    addLocalObject(key, expr.lambdaExpression().lambdaParameters().inferredFormalParameterList().identifier(1).getText());
                                } else {
                                    addLocalObject(key, expr.lambdaExpression().lambdaParameters().getText());  
                                }
                                
                            } else {
                                addInstanceMethodCall(key, methodName + getArguments(ctx));
                            }
                        });
                    } else if(addInstanceMethodCall(key, methodName + getArguments(ctx))) { //if instance method call
                        return; 
                    }
                
                } else if(ctx.primary() != null && ctx.primary().primaryNoNewArray_lfno_primary() != null) {
                    String methodName = ctx.identifier().getText();
                    //Method call on this reference
                    if(ctx.primary().primaryNoNewArray_lfno_primary().getText().equals("this")) {
                        methodCalls.peek().add(className + "." + methodName + getArguments(ctx));
                    } //Method call on array object
                     else if(ctx.primary().primaryNoNewArray_lfno_primary().arrayAccess_lfno_primary() != null) {
                        String key = ctx.primary().primaryNoNewArray_lfno_primary().arrayAccess_lfno_primary().expressionName().identifier().getText();
                        if(localObjects.containsKey(key)) {
                            methodCalls.peek().add(localObjects.get(key) + "." + methodName + getArguments(ctx));     
                        } else if(globalObjects.containsKey(key)) {
                            methodCalls.peek().add(globalObjects.get(key) + "." + methodName + getArguments(ctx));     
                        } 
                    //Method call on collection object
                    } else if(ctx.primary().primaryNoNewArray_lfno_primary().methodInvocation_lfno_primary() != null && ctx.primary().primaryNoNewArray_lfno_primary().methodInvocation_lfno_primary().typeName() != null) {
                        String key = ctx.primary().primaryNoNewArray_lfno_primary().methodInvocation_lfno_primary().typeName().getText();
                        if(localObjects.containsKey(key)) {
                            methodCalls.peek().add(localObjects.get(key) + "." + methodName + getArguments(ctx));     
                        } else if(globalObjects.containsKey(key)) {
                            methodCalls.peek().add(globalObjects.get(key) + "." + methodName + getArguments(ctx));     
                        }                        
                    }
                //Any other case
                } else if (ctx.identifier() != null) {
                    String methodName = ctx.identifier().getText();
                    methodCalls.peek().add(className + "." + methodName + getArguments(ctx));
                }  
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Adds the method name to the set on the stack methodCalls.
     * If there is no set on the stack, the method call is outside the scope of a method,
     * and won't be added.
     */
    public void enterMethodInvocation_lf_primary(Java9Parser.MethodInvocation_lf_primaryContext ctx) { 
        try {
            if(!methodCalls.empty() && className != null) {
              if (ctx.identifier() != null) {
                String methodName = ctx.identifier().getText();
                methodCalls.peek().add(className + "." + methodName + getArguments(ctx));
                }  
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Adds the method name to the set on the stack methodCalls.
     * The class name differs depending on the type of method call.
     * If there is no set on the stack, the method call is outside the scope of a method,
     * and won't be added.
     */
    public void enterMethodInvocation_lfno_primary(Java9Parser.MethodInvocation_lfno_primaryContext ctx) {
        try {
            if(!methodCalls.empty() && className != null) {
                if (ctx.methodName() != null) {
                String methodName = ctx.methodName().identifier().getText();
                methodCalls.peek().add(className + "." + methodName + getArguments(ctx));

                } else if (ctx.identifier() != null && ctx.typeName() != null) {
                    String methodName = ctx.identifier().getText();
                    if(Character.isUpperCase(ctx.typeName().identifier().getText().charAt(0))) { //if static method call
                        methodCalls.peek().add(ctx.typeName().identifier().getText() + "." + methodName + getArguments(ctx));
                    } else if(localObjects.containsKey(ctx.typeName().identifier().getText())) { //if instance method call
                        methodCalls.peek().add(localObjects.get(ctx.typeName().identifier().getText()) + "." + methodName + getArguments(ctx));
                    } else if(globalObjects.containsKey(ctx.typeName().identifier().getText())) { //if instance method call
                        methodCalls.peek().add(globalObjects.get(ctx.typeName().identifier().getText()) + "." + methodName + getArguments(ctx));
                    }
                } else if (ctx.identifier() != null) { //if method call from same class
                    String methodName = ctx.identifier().getText();
                    methodCalls.peek().add(className + "." + methodName + getArguments(ctx));
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Handles method calls from a method reference
     */
    public void enterMethodReference(Java9Parser.MethodReferenceContext ctx) {
        try {
            if(!methodCalls.isEmpty() && className != null)
            if(ctx.expressionName() != null) {
                String methodName = ctx.identifier().getText();
                methodCalls.peek().add(ctx.expressionName().getText() + "." + methodName + "()");
            }
            
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Handles method calls from a method reference
     */
    public void enterMethodReference_lfno_primary(Java9Parser.MethodReference_lfno_primaryContext ctx) {
        try {
            if(!methodCalls.isEmpty() && className != null) {
                if(ctx.expressionName() != null) {
                String methodName = ctx.identifier().getText();
                methodCalls.peek().add(ctx.expressionName().getText() + "." + methodName + "()");
                }
            }
            

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * @return the map of method purposes.
     */
    public HashMap<String, HashSet<String>> getPurposeMap() {
        return methodPurposes;
    }

    /**
     * @return the map of method calls.
     */
    public HashMap<String, HashSet<String>> getMethodCalls() {
        return callGraph;
    }
}
