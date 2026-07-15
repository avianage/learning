package com.avianage.corejava.day2.exception;

import java.util.InputMismatchException;
import java.util.Scanner;

public class ExceptionDemo {
    static void main(String[] args) {
        int num3 = 0;

        try (Scanner sc = new Scanner(System.in)){
            System.out.println("Enter an Integer");
            int num = sc.nextInt();
            System.out.println("Enter Another Number:");
            int num2 = sc.nextInt();
            num3 = num/num2;
        } catch (InputMismatchException | ArithmeticException) {
            System.out.println("Wrong!");
        }
    }
}
