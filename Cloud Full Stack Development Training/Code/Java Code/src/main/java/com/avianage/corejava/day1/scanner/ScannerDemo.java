package com.avianage.corejava.day1.scanner;

import java.util.Scanner;

public class ScannerDemo {
    static void main(String[] args) {
        Scanner sc = new Scanner(System.in);
        String username = sc.next();
        System.out.println("Welcome "+ username + "!");
        sc.close();
    }
}
