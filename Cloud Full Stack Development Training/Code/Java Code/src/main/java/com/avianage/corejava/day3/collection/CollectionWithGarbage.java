package com.avianage.corejava.day3.collection;

import java.util.ArrayList;

public class CollectionWithGarbage {
    static void main(String[] args) {
        ArrayList<String> friends = new ArrayList<>();
        System.out.println(friends.size());
        System.out.println(friends);
        friends.add("Sonu");
        friends.add("Monu");
        friends.add("Tonu");
        System.out.println(friends.size());
        System.out.println(friends);

        System.out.println(friends.size());
        System.out.println(friends);

    }
}
