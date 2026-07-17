package com.avianage.corejava.day2.oop.encapsulation;

public class EncapsulationDemo {

    public static void main(String[] args) {
        Employee emp = new Employee();
        System.out.println(emp.toString());
//		emp.salary = 10.25;
        emp.setSalary(10.25);
//		System.out.println(emp.salary);
        System.out.println(emp.getSalary());
        System.out.println(emp.toString());

    }

}