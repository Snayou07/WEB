package org.example.task1;

import java.util.ArrayList;
import java.util.List;

public class Task1 {
    public static void main(String[] args) {
        List<Abonent> abonents = new ArrayList<>();

        // Створення масиву об'єктів
        try {
            abonents.add(new Abonent("Коваленко", "Іван", "Петрович", "Київ", 120, 0));
            abonents.add(new Abonent("Шевченко", "Олена", "Іванівна", "Львів", 45, 15));
            abonents.add(new Abonent("Бойко", "Тарас", "Сергійович", "Одеса", 200, 5));
            abonents.add(new Abonent("Мельник", "Анна", "Вікторівна", "Дніпро", 30, 0));
            abonents.add(new Abonent("Ткаченко", "Василь", "Олегович", "Харків", 150, 40));
        } catch (InvalidAbonentDataException | NegativeTimeException e) {
            System.err.println("Помилка створення абонента: " + e.getMessage());
        }

        int targetLocalTime = 100;

        System.out.println("=== Абоненти (внутр. час > " + targetLocalTime + " хв) ===");
        for (Abonent ab : abonents) {
            if (ab.getLocalCallTime() > targetLocalTime) {
                System.out.println(ab);
            }
        }

        System.out.println("\n=== Абоненти (користувалися міжнародним зв’язком) ===");
        for (Abonent ab : abonents) {
            if (ab.getInternationalCallTime() > 0) {
                System.out.println(ab);
            }
        }
    }
}