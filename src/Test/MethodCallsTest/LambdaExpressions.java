package Test.MethodCallsTest;

@FunctionalInterface  //It is optional  
interface Drawable{  
    public void {marketing} draw();  
}  
  
public class LambdaExpressions {

    public void {marketing} drawNew() {
        System.out.println("Dangerous drawing");
    }  
     
    public void {research} drawing() {
        int width=10;   
        Drawable d2=()->{  
            drawNew();  
        };  
        d2.draw();   
    } 
}  
