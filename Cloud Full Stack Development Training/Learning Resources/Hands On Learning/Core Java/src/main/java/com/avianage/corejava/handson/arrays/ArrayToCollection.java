package com.avianage.corejava.handson.arrays;

import java.util.List;
import java.util.ArrayList;
import java.util.Arrays;

public class ArrayToCollection {
    static void main(String[] args) {
        // Array → List
        String[] namesArr  = {"Sonu", "Monu", "Tonu"};
        List<String> list  = Arrays.asList(namesArr);   // fixed-size List backed by array

        // For a fully mutable List:
        List<String> mutableList = new ArrayList<>(Arrays.asList(namesArr));

        // List → Array
        String[] backToArray = list.toArray(new String[0]);
    }
}
