package com.avianage.corejava.handson.innerclass;

import java.util.Arrays;

public class AnonInnerClassDemo {
    static void main(String[] args) {
        Printable p = new EmployeeReport();
        p.print();

        // Anonymous class approach — no separate class file needed
        Printable p1 = new Printable() {
            @Override
            public void print() {
                System.out.println("Employee report printed (anonymous).");
            }
        };
        p1.print();


        // Lambda Equivalent
        // Same sort, with lambda (Module 19)
        Arrays.sort(employees,
                (a, b) -> Double.compare(b.getSalary(), a.getSalary()));
    }
}
