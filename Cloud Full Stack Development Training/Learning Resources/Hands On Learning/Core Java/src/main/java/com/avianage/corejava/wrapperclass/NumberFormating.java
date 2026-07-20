package com.avianage.corejava.wrapperclass;

public class NumberFormating {
    static void main(String[] args) {
        // Number Formatting - printf & String.format

        double salary3 = 75000.50;

        // printf - print with formats
        System.out.printf("Salary %.2f%n", salary3);
        System.out.printf("%-15s %8.2f%n", "Sonu", salary3);

        // String.format
        String line = String.format("%-15s %8.2f", "Monu", 82000.0);
        System.out.println(line);

        /*
        Common format specifier:
        Specifier	Meaning
        %d	        Integer
        %f	        Floating point
        %.2f	    Float with 2 decimal places
        %s	        String
        %n	        Newline (platform-independent, prefer over \n in printf)
        %10s	    Right-aligned in field of width 10
        %-10s	    Left-aligned in field of width 10
         */
        // Formatted salary slip
        System.out.printf("%-12s %-15s %10s%n", "ID", "Name", "Salary");
        System.out.printf("%-12d %-15s %10.2f%n", 101, "Sonu",  75000.0);
        System.out.printf("%-12d %-15s %10.2f%n", 102, "Monu",  82000.0);
        System.out.printf("%-12d %-15s %10.2f%n", 103, "Tonu",  55000.0);
    }
}
