package com.avianage.corejava.innerclass;

public class Department {
    // Inner Classes
    private String name;
    private double budget;

    public Department(String name, double budget) {
        this.name   = name;
        this.budget = budget;
    }

    // ── Regular inner class ───────────────────────────────────────────
    public class BudgetReport {

        private String reportedBy;

        public BudgetReport(String reportedBy) {
            this.reportedBy = reportedBy;
        }

        public void print() {
            // Directly accesses outer class private fields — no getter needed
            System.out.println("=== Budget Report ===");
            System.out.println("Department  : " + name);          // outer field
            System.out.println("Budget      : " + budget);        // outer field
            System.out.println("Reported by : " + reportedBy);
        }
    }

    public String getName()   { return name; }
    public double getBudget() { return budget; }
}
