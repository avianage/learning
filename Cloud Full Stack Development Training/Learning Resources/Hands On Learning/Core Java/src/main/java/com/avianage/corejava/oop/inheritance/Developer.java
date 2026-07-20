package com.avianage.corejava.oop.inheritance;

public class Developer extends Employee{
    // Method Overriding
    /*
    A subclass can provide its own implementation of a method defined in the
    parent class. The parent's version is replaced for subclass objects.

    Rules:
    Same method name, same parameters, same (or covariant) return type
    Access modifier can be the same or wider (never narrower)
    @Override annotation — not required, but always use it: it tells the compiler
    to verify you are actually overriding something

     */
    private String techStack;

    public Developer(int id, String name, double salary,
                     String department, String techStack) {
        super(id, name, salary, department);
        this.techStack = techStack;
    }

    public String getTechStack() { return techStack; }

    @Override
    public void applyRaise(double percent) {
        // Developers get an extra 5% on top of the standard raise
        super.applyRaise(percent + 5);
    }

    @Override
    public void display() {
        super.display();
        System.out.printf("     Tech Stack: %s%n", techStack);
    }

    /*
    Overriding vs Overloading

    A common point of confusion:
    Aspect 	        Overriding 	                        Overloading
    Where 	        Parent–child classes 	            Same class (or child class)
    Method name 	Same 	                            Same
    Parameters 	    Same 	                            Different
    Return type 	Same (or covariant) 	            Can differ
    Resolved at 	Runtime 	                        Compile time
    Annotation 	    @Override 	                        None
    Purpose 	    Specializing inherited behavior 	Multiple versions of a method
     */
}
