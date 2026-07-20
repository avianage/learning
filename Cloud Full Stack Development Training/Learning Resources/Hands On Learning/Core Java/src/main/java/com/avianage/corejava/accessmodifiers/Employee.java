package com.avianage.corejava.accessmodifiers;

public class Employee {
    /*
    Access Modifiers

    An access modifier controls who can access a class, field, method, or constructor.
    Java has four:
    Modifier 	Keyword 	    Visibility
    Private 	private 	    Only within the same class
    Default 	(no keyword) 	Within the same package
    Protected 	protected 	    Same package + subclasses (any package)
    Public 	    public 	        Everywhere

    Think of it as concentric circles of visibility — private is the tightest, public is the widest.

    public    → entire application
    protected → package + subclasses
    default   → package only
    private   → class only
     */

    private int    id;
    private String name;
    private double salary;

    public Employee(int id, String name, double salary) {
        this.id     = id;
        this.name   = name;
        this.salary = salary;
    }

    // Controlled access through public methods
    public double getSalary() { return salary; }
    public void   setSalary(double salary) {
        if (salary > 0) {          // validation inside the class
            this.salary = salary;
        }
    }
}
