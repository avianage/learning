package com.avianage.corejava.day2.commons.objects;

public class ObjectDemo {
    static void main(String[] args) {
        Employee emp1 = new Employee(1, "Sonu", 10.25);
        Employee emp2 = new Employee(2, "Sonu", 10.25);

        System.out.println(emp1.toString());
        System.out.println(emp2.toString());
        System.out.println(emp1.hashCode());
        System.out.println(emp2.hashCode());
        System.out.println(emp1.equals(emp2));
    }
}
