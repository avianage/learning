package com.avianage.corejava.oop.inheritance;

public class ConstructorChaining {
    /*
    Constructor Chaining in Inheritance

    When you create a Manager object, constructors run in this order:
    Object() → Employee() → Manager()

    Every constructor calls its parent's constructor first, all the way up to
    Object (the root of all Java classes):

    class Employee {
        Employee() { System.out.println("Employee constructor"); }
    }

    class Manager extends Employee {
        Manager() {
            super();   // Java inserts this automatically if you don't write it
            System.out.println("Manager constructor");
        }
    }

    class Director extends Manager {
        Director() {
            super();
            System.out.println("Director constructor");
        }
    }

    // In main:
    Director d = new Director();

    Output:
    Employee constructor
    Manager constructor
    Director constructor

     */
}
