package com.avianage.corejava.handson.innerclass;

public class Employee {

    // Static Nested Class

    private int    id;
    private String name;
    private double salary;

    public Employee(int id, String name, double salary) {
        this.id     = id;
        this.name   = name;
        this.salary = salary;
    }

    public int    getId()     { return id; }
    public String getName()   { return name; }
    public double getSalary() { return salary; }

    // ── Static nested class ───────────────────────────────────────────
    public static class PayrollSummary {

        private int    count;
        private double totalSalary;

        public void add(Employee e) {
            count++;
            totalSalary += e.getSalary();   // accesses via getter — no outer instance
        }

        public void print() {
            System.out.println("Employees   : " + count);
            System.out.printf("Total payroll: %.2f%n", totalSalary);
            System.out.printf("Average      : %.2f%n",
                    count > 0 ? totalSalary / count : 0);
        }
    }
}
