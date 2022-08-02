package Test.MethodCallsTest;

import java.util.ArrayList;
import java.util.HashMap;

public class Calls {
    private Data field = new Data();
    private String s;
    String[] strings = new String[5];
    Data[] array = new Data[2];
    ArrayList<Integer> numbers = new ArrayList<Integer>();
    ArrayList<Data> data = new ArrayList<>();
    private HashMap<Integer, Data> dataObjects = new HashMap<>();

    public Calls() {
        numbers.add(5);
        numbers.add(9);
        numbers.add(8);
        numbers.add(1);
        data.add(new Data());
        dataObjects.put(1, new Data());
    }

    public void {research} callLambdaObject() {
        data.forEach(d -> {
            d.getData();
        });
    }

    public void {research} callLambdaMap() {
        dataObjects.forEach((i, d) -> {
            i++;
            d.getData();
        });
    }

    public int {marketing} add(int a, int b) {
        return a+b;
    }

    public void {research} callLambda() {
        numbers.forEach((n) -> {
            add(n, 2);
        });
    }

    public void {research} callArrayObject() {
        array[0].getData();
    }

    public void {research} callArrayList() {
        data.get(0).getData();
    }

    public void {research} callMapObject() {
        dataObjects.get(1).getData();
    }

    public void {research} callMapLoop() {
        for (Data d : dataObjects.values()) {
            d.getData();
          }
    }

    public void {research} forLoop() {
        for(String s : strings) {
            s.split(".");
        }
    }

    public void {research} callParam(Data param) {
        param.getData();
    }
    
    public void {research} callLocal() {
        Data localV = new Data();
        localV.useData("data");
    }

    public void {research} callField() {
        field.useData("data");
    }
    
    public void {marketing} callThis() {
        this.callLocal();
    }

    public void {research} callSeveral(Calls param, String y) {
        Data local = new Data();
        param.callThis();
        local.getData();
        callField();
    }   

    //Tests method references
    public static class MethodReference {  
        public static void {research} threadStatus(){  
            System.out.println("Thread is running...");  
        } 
        
        public static void {marketing} mRefCall() {
            Thread t2=new Thread(MethodReference::threadStatus);  
            t2.start();
        } 

        public static void main(String[] args) {
            
        }
    }  
}
