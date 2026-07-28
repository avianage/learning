package com.learning.autowire.constructor;

public class Car {
    private Specification specification;

    public Car(Specification specification) {
        this.specification = specification;
    }

    // Setter can be commented or removed for Autowiring by Constructor
//    public void setSpecification(Specification specification) {
//        this.specification = specification;
//    }

    public void displayDetails(){
        System.out.println("Car Details: " + specification);
    }
}
