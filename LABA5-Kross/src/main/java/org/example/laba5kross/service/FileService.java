package org.example.laba5kross.service;

import org.example.laba5kross.model.Game;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;


public class FileService {

    private static final String CSV_HEADER =
            "Назва гри,Сайт,Мова гри,Ціна (₴),Кількість гравців," +
                    "Мін. вік,Тривалість (хв),Складність,Рейтинг,URL";

    // ── Запис ─────────────────────────────────────────────────────────────────


    public void writeCsv(List<Game> games, File file) throws IOException {
        try (BufferedWriter writer = new BufferedWriter(
                new OutputStreamWriter(new FileOutputStream(file), StandardCharsets.UTF_8))) {

            // BOM для коректного відображення кирилиці в Excel
            writer.write('\uFEFF');
            writer.write(CSV_HEADER);
            writer.newLine();

            for (Game g : games) {
                writer.write(formatRow(g));
                writer.newLine();
            }
        }
    }

    private String formatRow(Game g) {
        return String.format("\"%s\",\"%s\",\"%s\",%.2f,\"%s\",%d,%d,\"%s\",%.1f,\"%s\"",
                escape(g.getName()),
                escape(g.getSite()),
                escape(g.getLanguage()),
                g.getPrice(),
                escape(g.getPlayers()),
                g.getMinAge(),
                g.getDurationMinutes(),
                escape(g.getDifficulty()),
                g.getRating(),
                escape(g.getUrl()));
    }

    // ── Читання ───────────────────────────────────────────────────────────────


    public List<Game> readCsv(File file) throws IOException {
        List<Game> result = new ArrayList<>();

        try (BufferedReader reader = new BufferedReader(
                new InputStreamReader(new FileInputStream(file), StandardCharsets.UTF_8))) {

            String line = reader.readLine(); // пропускаємо заголовок (і можливий BOM)
            while ((line = reader.readLine()) != null) {
                if (line.isBlank()) continue;
                Game g = parseRow(line);
                if (g != null) result.add(g);
            }
        }
        return result;
    }

    private Game parseRow(String line) {
        try {
            // Простий CSV-парсер (підтримує поля в лапках)
            String[] parts = splitCsvLine(line);
            if (parts.length < 10) return null;

            String name       = parts[0];
            String site       = parts[1];
            String language   = parts[2];
            double price      = Double.parseDouble(parts[3].replace(",", "."));
            String players    = parts[4];
            int    minAge     = Integer.parseInt(parts[5].trim());
            int    duration   = Integer.parseInt(parts[6].trim());
            String difficulty = parts[7];
            double rating     = Double.parseDouble(parts[8].replace(",", "."));
            String url        = parts[9];

            return new Game(name, site, language, price, players,
                    minAge, duration, difficulty, rating, url);
        } catch (Exception e) {
            return null; // пропускаємо некоректний рядок
        }
    }

    /** Розбиває рядок CSV з полями в лапках. */
    private String[] splitCsvLine(String line) {
        List<String> tokens = new ArrayList<>();
        StringBuilder sb    = new StringBuilder();
        boolean inQuotes    = false;

        for (char c : line.toCharArray()) {
            if (c == '"') {
                inQuotes = !inQuotes;
            } else if (c == ',' && !inQuotes) {
                tokens.add(sb.toString().trim());
                sb.setLength(0);
            } else {
                sb.append(c);
            }
        }
        tokens.add(sb.toString().trim());
        return tokens.toArray(new String[0]);
    }

    private String escape(String s) {
        return s == null ? "" : s.replace("\"", "\"\"");
    }
}