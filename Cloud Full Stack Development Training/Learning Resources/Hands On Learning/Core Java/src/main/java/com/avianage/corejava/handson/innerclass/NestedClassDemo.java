package com.avianage.corejava.handson.innerclass;

public class NestedClassDemo {
    static void main(String[] args) {
        // Create directly using the outer class name
        Employee.PayrollSummary summary = new Employee.PayrollSummary();

        summary.add(new Employee(101, "Sonu",  75000));
        summary.add(new Employee(102, "Monu",  82000));
        summary.add(new Employee(103, "Tonu",  55000));
        summary.add(new Employee(104, "Ponu",  91000));
        summary.add(new Employee(105, "Gonu",  68000));

        summary.print();
    }
}
