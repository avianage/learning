package com.avianage.corejava.classes;

public class EmployeeDemo {
    public static void main(String[] args) {

        // Create objects using the parameterized constructor
        Employee e1 = new Employee(101, "Sonu",  75000.0, "Engineering");
        Employee e2 = new Employee(102, "Monu",  82000.0, "Engineering");
        Employee e3 = new Employee(103, "Tonu",  55000.0, "HR");
        Employee e4 = new Employee(104, "Ponu",  91000.0, "Finance");
        Employee e5 = new Employee(105, "Gonu",  68000.0, "Operations");

        // Call instance methods
        e1.applyRaise(10);
        e1.display();   // 101   Sonu         Engineering       82500.00

        // Access static member through class name (not through object)
        System.out.println("Total employees: " + Employee.getEmployeeCount());   // 5

        // toString is called implicitly when printing
        System.out.println(e2);
        // Employee{id=102, name='Monu', dept='Engineering', salary=82000.00}

        // Fields - Instance Vs Static
        Employee e6 = new Employee(101, "Sonu", 75000.0, "Engineering");
        Employee e7 = new Employee(102, "Monu", 82000.0, "HR");

        // e6 and e7 each have their own name, salary, department
        e6.setSalary(80000);    // only changes e1's salary, e2 is unaffected

        // Static fields belong to the class. One copy shared across all objects:
        // employeeCount is incremented every time an object is created
        System.out.println(Employee.getEmployeeCount());   // 7 after creating e6 and e7

        // Method Overloading
        // Both valid:
        e1.applyRaise(10);              // 10% raise
        e1.applyRaise(5000, true);      // +5000 flat raise

    }
}