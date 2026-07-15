package com.ibm.revision.day1.classes;

public class ClassDemo {

    public static void main(String[] args) {

        Employee obj = new Employee();
        System.out.println(obj.toString());
        obj.id = 1;
        obj.name = "Sonu";
//		obj.salary = 10.75;
        System.out.println(obj.toString());
        System.out.println(Employee.officePhone);

        Employee obj2 = new Employee();
        System.out.println(obj2.toString());
        obj2.id = 2;
        obj2.name = "Monu";
        obj2.salary = 11.25;
        System.out.println(obj2.toString());
    }

}