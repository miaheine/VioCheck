package Test.MethodCallsTest;
/**
 * Test classes for method calls to super classes
 * Source: * https://www.javatpoint.com/inheritance-in-java
 */

 public class Animal {  
    void {marketing} eat(){
        System.out.println("eating...");
    }
    void {marketing} sleep() {
        System.out.println("sleeping...");
    }  
}  

class Dog extends Animal {  
    void eat() {
        System.out.println("eating bread...");
    }  
    
    void {beef} bark() {
        System.out.println("barking...");
    }  
    
    void {research} work() { 
        bark(); 
        super.eat();
    }  

}  

class BabyDog extends Dog {  
    void {marketing} weep() {
        System.out.println("weeping...");
    }  
} 

class TestSuper2 {  
    public void {research} test() {
        BabyDog d = new BabyDog();  
        d.weep();
        d.bark();
        d.sleep();
    }

    public void {research} test2() {
        Dog d = new Dog();
        d.work();
        d.sleep();
    }
} 