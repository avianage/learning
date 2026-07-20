package com.avianage.corejava.classes.constructors;

public class Employee {
    /*
    A constructor is a special method that runs when an object is created.
    It initializes the object's state.

    Rules:
    - Same name as the class
    - No return type (not even void)
    - Called automatically by new
    - If you define no constructor, Java provides a default no-arg constructor
    - Once you define any constructor, Java removes the default — you must define
      your own no-arg if you need it
     */

    // Constructor Overloading

    private int    id;
    private String name;
    private double salary;
    private String department;

    // No-arg constructor
    public Employee() {
        this(0, "Unknown", 0.0, "Unassigned");   // delegates to full constructor
    }

    // Partial constructor
    public Employee(String name, String department) {
        this(0, name, 0.0, department);           // delegates to full constructor
    }

    // Full constructor
    public Employee(int id, String name, double salary, String department) {
        this.id         = id;
        this.name       = name;
        this.salary     = salary;
        this.department = department;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public double getSalary() {
        return salary;
    }

    public void setSalary(double salary) {
        this.salary = salary;
    }

    public String getDepartment() {
        return department;
    }

    public void setDepartment(String department) {
        this.department = department;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public void display() {
    }

    public void applyRaise(double raise) {
    }
// Constructor Chaining with this()
    //this() calls another constructor in the same class. It must be the first statement
    // in the constructor body. This avoids duplicating initialization logic.

    /*
    The this Keyword

    this refers to the current object — the one on which the method or constructor
    was called.

    1. Disambiguate field from parameter
    public Employee(String name, double salary) {
        this.name   = name;    // this.name = field, name = parameter
        this.salary = salary;
    }

    2. Call another constructor
    public Employee(String name) {
        this(name, 0.0);   // must be first statement
    }

    3. Pass the current object to another method
    public void register(EmployeeRegistry registry) {
        registry.add(this);   // passing the current Employee object
    }
     */

    /*
    Execution Order — Putting It Together

    When new Employee(101, "Sonu", 75000, "Engineering") is called:

    1. Memory allocated on heap for the new Employee object
    2. Static block runs — only once, when the class is first loaded (not on every new)
    3. Instance block runs — every time a new object is created, before the constructor
    4. Constructor runs — initializes fields
    5. Reference returned and assigned to the variable

     */
}
