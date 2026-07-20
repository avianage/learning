package com.avianage.corejava.flowcontrol;

public class ControlStatements {
    static void main(String[] args) {
        // If Statements
        double salary = 95000;

        if (salary > 80000) {
            System.out.println("Senior pay band");
        }

        // If - else
        boolean isActive = false;

        if (isActive) {
            System.out.println("Employee is active");
        } else {
            System.out.println("Employee is inactive");
        }

        // If - else If - else
        double salary1 = 72000;
        String band;

        if (salary1 >= 100000) {
            band = "Executive";
        } else if (salary1 >= 80000) {
            band = "Senior";
        } else if (salary1 >= 60000) {
            band = "Mid-level";
        } else {
            band = "Junior";
        }

        System.out.println("Pay band: " + band);   // Pay band: Mid-level

        // Nested If
        String department = "Engineering";
        double salary2 = 90000;

        if (department.equals("Engineering")) {
            if (salary2 > 85000) {
                System.out.println("Senior Engineer bonus: 20%");
            } else {
                System.out.println("Engineer bonus: 10%");
            }
        } else {
            System.out.println("Standard bonus: 5%");
        }

        // Switch Statements
        String department1 = "HR";
        int bonusPercent;

        switch (department1) {
            case "Engineering":
                bonusPercent = 20;
                break;
            case "Sales":
                bonusPercent = 25;
                break;
            case "HR":
            case "Finance":
                bonusPercent = 10;    // HR and Finance share the same value — fall-through
                break;
            default:
                bonusPercent = 5;
                break;
        }

        System.out.println("Bonus: " + bonusPercent + "%");   // Bonus: 10%

        // Arrow Switch Expression (Java 14+)
        String department2 = "Sales";

        int bonusPercent1 = switch (department2) {
            case "Engineering"         -> 20;
            case "Sales"               -> 25;
            case "HR", "Finance"       -> 10;    // multiple labels, comma-separated
            default                    -> 5;
        };

        System.out.println("Bonus: " + bonusPercent1 + "%");   // Bonus: 25%

        // For Multi line
        int bonusPercent2 = switch (department) {
            case "Sales" -> {
                System.out.println("Sales team — commission included");
                yield 25;    // yield returns the value from a block
            }
            default -> 5;
        };

    }

}
