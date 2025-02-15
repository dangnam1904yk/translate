package com.translate.trans.controller;

public class Test {
    public static void main(String[] args) {
        String str = "1 ÊXAMPLE 1 TLY 6 & y"; // Test different cases

        // Remove non-letter characters (keep only Unicode letters)
        String lettersOnly = str.replaceAll("[^\\p{L}]", "");

        // Check if remaining string contains only uppercase letters
        if (lettersOnly.matches("\\p{Lu}+")) {
            System.out.println("UPPER.");
        } else {
            System.out.println("lowercase.");
        }

    }
}
