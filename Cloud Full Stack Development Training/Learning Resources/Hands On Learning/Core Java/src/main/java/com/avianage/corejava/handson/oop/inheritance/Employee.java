package com.avianage.corejava.handson.oop.inheritance;

public class Employee {
    /*
    What Is Inheritance?

    Inheritance is a mechanism where a class acquires the fields and methods of another
    class. The acquiring class can then extend, specialize, or override that behavior.

    The core idea: don't repeat yourself. If two classes share common state and behavior,
    extract it into a parent class.

    Employee (parent)
    ├── id, name, salary, department
    ├── getSalary(), applyRaise(), display()
    │
    ├── Manager (child)       → adds: teamSize, getTeamSize(), conductReview()
    │
    ├── Developer (child)     → adds: techStack, getTechStack(), writeCode()
    │
    └── Contractor (child)    → adds: contractEndDate, getDailyRate()

    Manager, Developer, and Contractor all get id, name, salary, and all of Employee's
    methods for free — without rewriting them.
     */

    // Terminology
    /*
    Term 	                    Meaning
    Parent class / Superclass 	The class being inherited from
    Child class / Subclass 	    The class that inherits
    extends 	                The keyword used to inherit
     */
    private int    id;
    private String name;
    private double salary;
    private String department;

    public Employee(int id, String name, double salary, String department) {
        this.id         = id;
        this.name       = name;
        this.salary     = salary;
        this.department = department;
    }

    public int    getId()         { return id; }
    public String getName()       { return name; }
    public double getSalary()     { return salary; }
    public String getDepartment() { return department; }
    public void   setSalary(double salary) { this.salary = salary; }

    public void applyRaise(double percent) {
        this.salary = this.salary * (1 + percent / 100);
    }

    public void display() {
        System.out.printf("%-5d %-12s %-15s %10.2f%n",
                id, name, department, salary);
    }

    @Override
    public String toString() {
        return String.format("Employee{id=%d, name='%s', dept='%s', salary=%.2f}",
                id, name, department, salary);
    }

    /*
    IS-A vs HAS-A

    IS-A — Inheritance. Manager IS-A Employee.
    Manager m = new Manager(...);
    System.out.println(m instanceof Employee);   // true — every Manager is an Employee
    System.out.println(m instanceof Manager);    // true

    HAS-A — Composition. Employee HAS-A Department.
    public class Employee {
        private Department department;    // Employee HAS-A Department object
    }

    Prefer composition over inheritance when the relationship is "has-a" rather than
    "is-a". Inheritance is often overused — only use it when a genuine IS-A relationship
    exists.
     */

    // final Keyword
    // final method — cannot be overridden
    // final class — cannot be subclassed
    // String is a final class — that is partly why it is immutable.
    public final double calculateTax() {
        return getSalary() * 0.10;   // fixed tax logic — no subclass should change this
    }

    // @Override
    // public double calculateTax() { }   // compile error — cannot override final method

    /*
    Types of Inheritance in Java

    Java supports the following:
    Single inheritance — one parent:
    class Manager extends Employee { }

    Multilevel inheritance — chain of parent → child → grandchild:
    class Employee { }
    class Manager extends Employee { }
    class Director extends Manager { }   // Director → Manager → Employee

    Hierarchical inheritance — multiple children from one parent:
    class Manager   extends Employee { }
    class Developer extends Employee { }
    class Contractor extends Employee { }

    Multiple inheritance — NOT supported for classes in Java:
    class X extends A, B { }   // compile error — not allowed

    Java avoids multiple class inheritance to prevent the Diamond Problem — ambiguity
    when two parent classes have the same method. Interfaces solve this (Module 12).
     */
}
