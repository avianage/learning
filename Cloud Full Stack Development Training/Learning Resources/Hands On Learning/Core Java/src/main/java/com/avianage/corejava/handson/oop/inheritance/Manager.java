package com.avianage.corejava.handson.oop.inheritance;

public class Manager extends Employee{
    private int    teamSize;
    private String projectName;

    public Manager(int id, String name, double salary,
                   String department, int teamSize, String projectName) {
        super(id, name, salary, department);   // call Employee's constructor
        this.teamSize    = teamSize;
        this.projectName = projectName;
    }

    public int    getTeamSize()    { return teamSize; }
    public String getProjectName() { return projectName; }

    public void conductReview() {
        System.out.println(getName() + " is conducting performance review for project: " + projectName);
    }

    @Override
    public void display() {
        super.display();   // call Employee's display first
        System.out.printf("     Team Size: %d, Project: %s%n", teamSize, projectName);
    }

    /*
    super Keyword

    super refers to the parent class. Two uses:
    1. Call the parent constructor
    public Manager(int id, String name, double salary,
                   String department, int teamSize, String projectName) {
        super(id, name, salary, department);   // MUST be the first statement
        this.teamSize    = teamSize;
        this.projectName = projectName;
    }

    super() must be the first statement in the child constructor. If you do not call it
    explicitly, Java automatically inserts super() (the no-arg parent constructor). If
    the parent has no no-arg constructor, you must call super(...) explicitly.

    2. Call a parent method
    @Override
    public void display() {
        super.display();    // runs Employee's display()
        System.out.printf("     Team Size: %d, Project: %s%n", teamSize, projectName);
    }
     */
}
