package com.avianage.corejava.stringhandling;

public class CommonStringPatterns {
    public static boolean isNumeric(String s) {
        if (s == null || s.isBlank()) return false;
        try {
            Double.parseDouble(s);
            return true;
        } catch (NumberFormatException e) {
            return false;
        }
    }

    // Count Character Occurrence
    public static int countOccurrences(String text, char target) {
        int count = 0;
        for (char c : text.toCharArray()) {
            if (c == target) count++;
        }
        return count;
    }

    // Palindrome Check
    public static boolean isPalindrome(String s) {
        String cleaned  = s.toLowerCase().replaceAll("[^a-z0-9]", "");
        String reversed = new StringBuilder(cleaned).reverse().toString();
        return cleaned.equals(reversed);
    }

    static void main(String[] args) {
        System.out.println(isNumeric("75000"));   // true
        System.out.println(isNumeric("75k"));     // false
        System.out.println(isNumeric(null));      // false

        // Null Safe
        String name = null;

        // Risky:
        if (name.equals("Sonu")) { }       // NullPointerException

        // Safe — put the literal first:
        if ("Sonu".equals(name)) { }       // false, no exception

        // Or explicit null check:
        if (name != null && name.equals("Sonu")) { }

        System.out.println(countOccurrences("Engineering", 'e'));  // 1
        System.out.println(countOccurrences("Engineering", 'i'));  // 2

        // Reverse a String
        String original = "Sonu";
        String reversed = new StringBuilder(original).reverse().toString();
        System.out.println(reversed);   // unoS


        System.out.println(isPalindrome("racecar"));    // true
        System.out.println(isPalindrome("Engineering")); // false


    }









}
