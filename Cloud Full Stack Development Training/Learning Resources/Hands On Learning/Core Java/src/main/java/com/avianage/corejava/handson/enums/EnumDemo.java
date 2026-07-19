package com.avianage.corejava.handson.enums;

public class EnumDemo {
    static void main(String[] args) {
        EmployeeStatus status = EmployeeStatus.ACTIVE;

        // Type-safe — this won't compile:
        // EmployeeStatus bad = 99;      // compile error
        // EmployeeStatus bad = "ACTIVE"; // compile error

        System.out.println(status);          // ACTIVE — readable by default
        System.out.println(status.name());   // ACTIVE — same
        System.out.println(status.ordinal()); // 0 — zero-based position in declaration

        // ENUM in switch
        EmployeeStatus status1 = EmployeeStatus.ON_LEAVE;

        switch (status1) {
            case ACTIVE:
                System.out.println("Employee is active — full access granted.");
                break;
            case ON_LEAVE:
                System.out.println("Employee on leave — read-only access.");
                break;
            case INACTIVE:
            case TERMINATED:
                System.out.println("Employee not active — access denied.");
                break;
        }

        // With Java 14+
        String access = switch (status1) {
            case ACTIVE     -> "Full access";
            case ON_LEAVE   -> "Read-only";
            case INACTIVE,
                 TERMINATED -> "No access";
        };
        System.out.println(access);

        // Enums with fields and methods
        Department dept = Department.ENGINEERING;

        System.out.println(dept.getDisplayName());   // Engineering
        System.out.println(dept.getLocation());      // Pune
        System.out.println(dept.getBudget());        // 500000.0
        System.out.println(dept.budgetCategory());   // High
        System.out.println(dept.isInCity("Pune"));   // true

        // Iterate over all enum constants
        for (Department d : Department.values()) {
            System.out.printf("%-15s %-10s %-10s %10.0f%n",
                    d.getDisplayName(), d.getLocation(),
                    d.budgetCategory(), d.getBudget());
        }

        // Enums with abstract methods
        double salary = 75000;

        for (JobGrade grade : JobGrade.values()) {
            System.out.printf("%-10s %-20s Bonus: %.2f%n",
                    grade, grade.describe(), grade.applyBonus(salary));
        }

    }
}
