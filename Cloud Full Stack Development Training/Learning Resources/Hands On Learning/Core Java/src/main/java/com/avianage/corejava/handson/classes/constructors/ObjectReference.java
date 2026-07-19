package com.avianage.corejava.handson.classes.constructors;

public class ObjectReference {
    static void main(String[] args) {
        Employee e1 = new Employee(101, "Sonu", 75000, "Engineering");
        Employee e2 = e1;   // e2 holds the same reference — no new object created

        e2.setSalary(90000);
        System.out.println(e1.getSalary());   // 90000 — e1 and e2 point to the same object

        /*
        Stack          Heap
        ──────         ──────────────────────
        e1 → @1001     Object @1001
        e2 → @1001     name   = "Sonu"
                 salary = 90000    ← changed via e2, seen via e1
         */

        // Passing Obj to Methods
        Employee emp = new Employee(101, "Sonu", 75000, "Engineering");
        givePromotion(emp, "Management", 20);

        System.out.println(emp.getDepartment());  // Management — object state changed
        System.out.println(emp);                  // emp is not null — local reassignment had no effect

        // Call with any number of arguments
        System.out.println(totalSalary(75000, 82000));                      // 157000.0
        System.out.println(totalSalary(75000, 82000, 55000, 91000, 68000)); // 371000.0
        System.out.println(totalSalary());
    }

    // Null Reference
    public void printEmployee(Employee e) {
        if (e == null) {
            System.out.println("No employee provided.");
            return;
        }
        e.display();
    }

    // Passing Obj to Methods
    public static void givePromotion(Employee e, String newDept, double raise) {
        e.setDepartment(newDept);    // modifies the original object
        e.applyRaise(raise);         // modifies the original object
        e = null;                    // only affects the local copy of the reference
    }

    // Varargs - Variable Number of Arguments
    public static double totalSalary(double... salaries) {
        double total = 0;
        for (double s : salaries) total += s;
        return total;
    }

}
