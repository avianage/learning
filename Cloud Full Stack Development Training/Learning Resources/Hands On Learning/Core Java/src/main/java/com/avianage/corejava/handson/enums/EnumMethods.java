package com.avianage.corejava.handson.enums;

import java.util.EnumSet;
import java.util.EnumMap;
import java.util.ArrayList;
import java.util.List;

public class EnumMethods {
    static void main(String[] args) {
        // Builtin Enum Methods

        // values() — all constants as an array
        EmployeeStatus[] all = EmployeeStatus.values();

        // valueOf() — get constant from its name string
        EmployeeStatus s = EmployeeStatus.valueOf("ACTIVE");   // EmployeeStatus.ACTIVE
        // throws IllegalArgumentException if name doesn't match

        // name() — constant name as String
        System.out.println(EmployeeStatus.ACTIVE.name());      // "ACTIVE"

        // ordinal() — zero-based position
        System.out.println(EmployeeStatus.TERMINATED.ordinal()); // 2

        // compareTo() — compares by ordinal
        System.out.println(EmployeeStatus.ACTIVE.compareTo(EmployeeStatus.TERMINATED)); // negative

        // toString() — same as name() by default, can be overridden
        System.out.println(EmployeeStatus.ON_LEAVE);            // ON_LEAVE

        // Enumset - Efficient Set of Enum Consts
        // All constants
        EnumSet<EmployeeStatus> allStatuses = EnumSet.allOf(EmployeeStatus.class);

        // Specific constants
        EnumSet<EmployeeStatus> activeStatuses = EnumSet.of(
                EmployeeStatus.ACTIVE,
                EmployeeStatus.ON_LEAVE);

        // Range
        EnumSet<EmployeeStatus> range = EnumSet.range(
                EmployeeStatus.ACTIVE,
                EmployeeStatus.ON_LEAVE);

        // Complement — everything NOT in a set
        EnumSet<EmployeeStatus> inactive = EnumSet.complementOf(activeStatuses);

        // Usage
        EmployeeStatus current = EmployeeStatus.ON_LEAVE;
        if (activeStatuses.contains(current)) {
            System.out.println("Employee has some level of access.");
        }

        // EnumMap - Map with Enums
        EnumMap<Department, List<String>> deptEmployees = new EnumMap<>(Department.class);

        // Group employees by department
        deptEmployees.put(Department.ENGINEERING, new ArrayList<>(List.of("Sonu", "Monu")));
        deptEmployees.put(Department.HR,          new ArrayList<>(List.of("Tonu")));
        deptEmployees.put(Department.FINANCE,     new ArrayList<>(List.of("Ponu")));
        deptEmployees.put(Department.OPERATIONS,  new ArrayList<>(List.of("Gonu")));

        // Print
        deptEmployees.forEach((dept, names) ->
                System.out.printf("%-15s %s%n", dept.getDisplayName(), names));


    }
}
