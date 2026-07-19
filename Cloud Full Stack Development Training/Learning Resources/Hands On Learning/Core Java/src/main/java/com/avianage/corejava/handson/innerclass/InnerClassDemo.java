package com.avianage.corejava.handson.innerclass;

public class InnerClassDemo {
    static void main(String[] args) {
        // Step 1: create outer object
        Department dept = new Department("Engineering", 500000.0);

        // Step 2: create inner object via outer object
        Department.BudgetReport report = dept.new BudgetReport("Ponu");

        report.print();
    }
}
