package com.avianage.corejava.classes;

public class Employee {
    /*
    What Is Object-Oriented Programming?

    Before OOP, programs were written as a sequence of procedures
    operating on separate data. As programs grew, this became hard
    to manage — data and the logic that operated on it lived far
    apart, making code difficult to understand, test, and change.

    OOP solves this by bundling data and behavior together into
    a single unit called an object.

    Java is built around four OOP principles:
    Principle 	    One-line definition
    Encapsulation 	Bundle data and methods together; control access
    Inheritance 	A class can acquire properties and behavior of another class
    Polymorphism 	One interface, multiple behaviors
    Abstraction 	Expose what is necessary, hide the complexity
    */

    // ── Fields (instance variables) ──────────────────────────────────
    private int    id;
    private String name;
    private double salary;
    private String department;

    // ── Static field (shared across all instances) ────────────────────
    private static int employeeCount = 0;

    // ── Static block (runs once when class is loaded) ─────────────────
    static {
        System.out.println("Employee class loaded.");
        // typically used for static initialization
    }

    // ── Instance block (runs every time an object is created, before constructor) ──
    {
        employeeCount++;
    }

    // ── Constructors ──────────────────────────────────────────────────
    public Employee() {
        this.name       = "Unknown";
        this.salary     = 0.0;
        this.department = "Unassigned";
    }

    public Employee(int id, String name, double salary, String department) {
        this.id         = id;
        this.name       = name;
        this.salary     = salary;
        this.department = department;
    }

    // ── Instance methods ──────────────────────────────────────────────
    public void applyRaise(double percent) {
        this.salary = this.salary * (1 + percent / 100);
    }

    public void display() {
        System.out.printf("%-5d %-12s %-15s %10.2f%n",
                id, name, department, salary);
    }

    // Method Overloading
    public void applyRaise(double amount, boolean isAbsolute) {
        if (isAbsolute) {
            this.salary += amount;
        } else {
            this.salary = this.salary * (1 + amount / 100);
        }
    }


    // ── Static method ─────────────────────────────────────────────────
    public static int getEmployeeCount() {
        return employeeCount;
    }

    // ── Getters and Setters ───────────────────────────────────────────
    public int    getId()         { return id; }
    public String getName()       { return name; }
    public double getSalary()     { return salary; }
    public String getDepartment() { return department; }

    public void setName(String name)           { this.name = name; }
    public void setSalary(double salary)       { this.salary = salary; }
    public void setDepartment(String dept)     { this.department = dept; }

    // ── toString ──────────────────────────────────────────────────────
    @Override
    public String toString() {
        return String.format("Employee{id=%d, name='%s', dept='%s', salary=%.2f}",
                id, name, department, salary);
    }

}
