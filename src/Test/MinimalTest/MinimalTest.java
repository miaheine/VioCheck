package Test.MinimalTest;

public class MinimalTest {
    public void {research, marketing} notMinimal() {
        marketing();
    }
    
    public void {marketing} marketing() {
        System.out.println("marketing..");
    }

    public void {marketing} minimal() {
        marketing();
    }

    public static void main(String[] args) {
        MinimalTest t = new MinimalTest();
        t.minimal();
    }
}
