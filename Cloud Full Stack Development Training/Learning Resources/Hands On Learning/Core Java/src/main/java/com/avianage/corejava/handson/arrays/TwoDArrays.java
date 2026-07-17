package com.avianage.corejava.handson.arrays;

public class TwoDArrays {
    static void main(String[] args) {
        // 3 employees, 3 months of performance scores
        int[][] scores = {
                {85, 90, 88},   // Sonu
                {70, 75, 80},   // Monu
                {92, 88, 95}    // Tonu
        };

        // Access: [row][column]
        System.out.println(scores[0][1]);   // 90 — Sonu's month 2 score
        System.out.println(scores[2][2]);   // 95 — Tonu's month 3 score

        // Dimensions
        System.out.println(scores.length);     // 3 — number of rows
        System.out.println(scores[0].length);  // 3 — number of columns in row 0

        // Iterating a 2 D Array
        String[] names = {"Sonu", "Monu", "Tonu"};

        for (int row = 0; row < scores.length; row++) {
            System.out.printf("%-6s: ", names[row]);
            for (int col = 0; col < scores[row].length; col++) {
                System.out.printf("%4d", scores[row][col]);
            }
            System.out.println();
        }

        // Jagged Array
        int[][] jagged = new int[3][];
        jagged[0] = new int[]{1, 2, 3};          // 3 elements
        jagged[1] = new int[]{10, 20};           // 2 elements
        jagged[2] = new int[]{100, 200, 300, 400}; // 4 elements
    }
}
