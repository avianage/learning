package com.avianage.corejava.day1.methods;

public class MethodDemo {
    void printNums() {
        for (int i = 1; i <= 5; i++){
            System.out.println(i);
        }
    }

    static void printNums2(){
        for (int i = 0 ; i<=5; i++){
            System.out.println(i);
        }
    }

    public static void main(String[] args){
        MethodDemo obj = new MethodDemo();
        obj.printNums();
        MethodDemo.printNums2();
//      MethodDemo.printNums(); // CE
//        obj.printNums2(); // warning
    }
}
