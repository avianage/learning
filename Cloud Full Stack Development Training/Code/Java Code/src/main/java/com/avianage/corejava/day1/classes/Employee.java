package com.avianage.corejava.day1.classes;

public class Employee {

    static long officePhone = 123L;

    int id;
    String name;
    double salary;
    long phone;

    @Override
    public String toString() {
        return "Employee{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", salary=" + salary +
                ", phone=" + phone +
                '}';
    }
}
