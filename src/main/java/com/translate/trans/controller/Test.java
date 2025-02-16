package com.translate.trans.controller;

public class Test {
    public static void main(String[] args) {
        String input = "123Đặng456"; // Chuỗi cần kiểm tra
        String a = "1.";
        String b = ".4 .";
        String c = ".5";
        String d = ".5R";
        String e = ".5 Á";

        boolean containsLetter = input.matches(".*\\p{L}.*");
        System.out.println(containsLetter);
        System.out.println(a.matches(".*\\p{L}.*"));
        System.out.println(b.matches(".*\\p{L}.*"));
        System.out.println(c.matches(".*\\p{L}.*"));
        System.out.println(d.matches(".*\\p{L}.*"));
        System.out.println(e.matches(".*\\p{L}.*"));
        System.out.println("Hoàng A".matches(".*\\p{L}.*"));
        System.out.println("#21a".matches(".*\\p{L}.*"));

        System.out.println("#21".matches(".*\\p{L}.*"));
        System.out.println("2. fj".matches(".*\\p{L}.*"));

    }
}
