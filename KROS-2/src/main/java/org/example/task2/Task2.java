package org.example.task2;

import java.util.Arrays;

public class Task2 {
    public static void main(String[] args) {
        Continent eurasia = new Continent("Євразія (фрагмент)");

        eurasia.addDivision(Arrays.asList("Київська Русь", "Візантійська імперія"), 882, 1240);
        eurasia.addDivision(Arrays.asList("Велике князівство Литовське", "Золота Орда"), 1241, 1569);
        eurasia.addDivision(Arrays.asList("Україна", "Польща", "Німеччина", "Франція"), 1991, 2026);

        System.out.println("=== Демонстрація пошуку через інтерфейс ===");

        eurasia.searchDivisionByYear(1000);
        System.out.println("-------------------------------------------------");

        eurasia.searchDivisionByYear(1400);
        System.out.println("-------------------------------------------------");

        eurasia.searchDivisionByYear(2024);
    }
}