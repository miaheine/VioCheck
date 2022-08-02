package Test.MethodCallsTest;
public class Data {
    public void {marketing} getData() {
        System.out.println("data");
    }

    public void {marketing} collectData() {
        printData("ss");
    }

    public void {marketing} printData(String data) {
        useData(data);
    }

    public void {research, marketing} useData(String data) {
        int res = 0;
        if(data.length() > 10) {
            res = data.length();
	        printData(data);	
        } else {
            printData(data);
            System.out.println("Length shorter than 10");
        }
    }
}