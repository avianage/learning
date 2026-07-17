package com.avianage.corejava.handson.stringhandling;

import java.util.Arrays;

public class StringMethods {
    static void main(String[] args) {
        // Length and Character Methods
        String name = "Sonu Sharma";

        System.out.println(name.length());        // 11 — number of characters
        System.out.println(name.charAt(0));       // 'S'
        System.out.println(name.charAt(5));       // 'S'
        System.out.println(name.indexOf('o'));    // 1 — first occurrence
        System.out.println(name.lastIndexOf('a'));// 10
        System.out.println(name.indexOf("Sharma"));  // 5

        // Case Conversion
        String dept = "Engineering";

        System.out.println(dept.toUpperCase());   // ENGINEERING
        System.out.println(dept.toLowerCase());   // engineering

        // Trimming and Stripping
        String raw = "   Sonu   ";

        System.out.println(raw.trim());           // "Sonu" — removes leading/trailing whitespace
        System.out.println(raw.strip());          // "Sonu" — Unicode-aware (Java 11+)
        System.out.println(raw.stripLeading());   // "Sonu   "
        System.out.println(raw.stripTrailing());  // "   Sonu"

        // Substring
        String fullName = "Sonu Sharma";

        System.out.println(fullName.substring(5));      // "Sharma"  — from index 5 to end
        System.out.println(fullName.substring(0, 4));   // "Sonu"    — from 0 (inclusive) to 4 (exclusive)

        // Checking Content
        String role = "Senior Engineer";

        System.out.println(role.startsWith("Senior"));        // true
        System.out.println(role.endsWith("Engineer"));         // true
        System.out.println(role.contains("Engineer"));         // true
        System.out.println(role.isEmpty());                    // false
        System.out.println("".isEmpty());                      // true
        System.out.println("  ".isEmpty());                    // false — only checks length == 0
        System.out.println("  ".isBlank());                    // true  — whitespace-only (Java 11+)

        // Replace
        String dept1 = "Engineering Department";

        System.out.println(dept1.replace('e', 'E'));
        // EnginEEring DEpartmEnt — replaces all occurrences

        System.out.println(dept1.replace("Engineering", "HR"));
        // HR Department

        System.out.println(dept1.replaceAll("[aeiou]", "*"));
        // *ng*n**r*ng D*p*rtm*nt — regex replace (Module 29 covers regex in depth)

        System.out.println(dept1.replaceFirst("[A-Z]", "X"));
        // Xngineering Department — replaces first match only

        // Splitting and Joining
        // Split
        String csv = "Sonu,Monu,Tonu,Ponu,Gonu";
        String[] names = csv.split(",");

        for (String name1 : names) {
            System.out.println(name1);
        }
        // Sonu
        // Monu
        // Tonu
        // Ponu
        // Gonu

        // Split with limit — max number of pieces
        String[] parts = csv.split(",", 3);
        System.out.println(Arrays.toString(parts));   // [Sonu, Monu, Tonu,Ponu,Gonu]

        // Join
        String joined = String.join(", ", "Sonu", "Monu", "Tonu");
        System.out.println(joined);   // Sonu, Monu, Tonu

        String joinedFromArray = String.join(" | ", names);
        System.out.println(joinedFromArray);   // Sonu | Monu | Tonu | Ponu | Gonu

        // Comparisons
        String s1 = "Engineering";
        String s2 = "engineering";

        System.out.println(s1.equals(s2));              // false — case-sensitive
        System.out.println(s1.equalsIgnoreCase(s2));    // true  — case-insensitive
        System.out.println(s1.compareTo(s2));           // negative — 'E'(69) < 'e'(101)
        System.out.println(s1.compareToIgnoreCase(s2)); // 0 — equal ignoring case

        // Conversion Other types to String
        int    id      = 101;
        double salary  = 75000.0;
        boolean active = true;

        String s4 = String.valueOf(id);       // "101"
        String s5 = String.valueOf(salary);   // "75000.0"
        String s6 = String.valueOf(active);   // "true"

        // String.format for formatted output
        String line = String.format("ID: %d, Name: %-10s, Salary: %.2f", 101, "Sonu", 75000.0);
        System.out.println(line);   // ID: 101, Name: Sonu      , Salary: 75000.00

        // chars() - stream of characters (Java 8+)
        String name2 = "Sonu";
        name2.chars()
                .forEach(c -> System.out.print((char) c + " "));
        // S o n u

        // repeat() (Java 11+)

        String separator = "-".repeat(30);
        System.out.println(separator);   // ------------------------------

    }
}
