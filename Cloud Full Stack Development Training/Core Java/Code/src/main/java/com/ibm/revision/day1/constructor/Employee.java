package com.ibm.revision.day1.constructor;

public class Employee {
    int id;
    String name;
    double salary;

    public Employee() {
        super();
        System.out.println("Default Constructor");
    }

    public Employee(int id, String name){
        super();
        System.out.println("2 Args Constructor");
        this.id = id;
        this.name = name;
    }

    public Employee(int id, String name, double salary){
        super();
        System.out.println("3 Arg Constructor");
        this.id = id;
        this.name = name;
        this.salary = salary;
    }

    @Override
    public String toString() {
        return "Employee{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", salary=" + salary +
                '}';
    }
}
