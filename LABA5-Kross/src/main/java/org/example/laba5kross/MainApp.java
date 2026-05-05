package org.example.laba5kross;

import javafx.application.Application;
import org.example.laba5kross.ui.MainWindow;

/**
 * Точка входу в застосунок.
 *
 * Окремий клас потрібен через особливість завантаження JavaFX-модулів:
 * якщо main() знаходиться прямо в класі Application, деякі JVM
 * видають помилку при старті без явного виклику launch().ffffff
 */
public class MainApp {
    public static void main(String[] args) {
        Application.launch(MainWindow.class, args);
    }
}
