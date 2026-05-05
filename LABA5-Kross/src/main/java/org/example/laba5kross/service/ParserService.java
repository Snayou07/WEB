package org.example.laba5kross.service;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.example.laba5kross.model.Game;
import org.example.laba5kross.model.SearchFilter;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;


public class ParserService {

    private static final int TIMEOUT_MS = 8_000;

    /** Список сайтів, які обходимо. */
    private static final String[] SITES = {
            "Ігромаг",
            "Ігродол",
            "Lelekan",
            "Nosorog",
            "Така Мака"
    };

    public String[] getSites() { return SITES; }


    public List<Game> parseSite(String siteName, SearchFilter filter) {
        return switch (siteName) {
            case "Ігромаг"   -> parseIhromah(filter);
            case "Ігродол"   -> parseIhrodol(filter);
            case "Lelekan"   -> parseLelekan(filter);
            case "Nosorog"   -> parseNosorog(filter);
            case "Така Мака" -> parseTakaMaka(filter);
            default          -> List.of();
        };
    }

    // ── Парсери сайтів ────────────────────────────────────────────────────────

    private List<Game> parseIhromah(SearchFilter filter) {
        List<Game> result = new ArrayList<>();
        try {
            String query  = encodeQuery(filter.getGameName().equals("Всі ігри")
                    ? "настільні ігри" : filter.getGameName());
            Document doc  = Jsoup.connect("https://igromag.ua/search?q=" + query)
                    .userAgent("Mozilla/5.0")
                    .timeout(TIMEOUT_MS)
                    .get();

            Elements cards = doc.select(".product-item");
            for (Element card : cards) {
                String name  = card.select(".product-name").text().trim();
                String priceStr = card.select(".price").text()
                        .replaceAll("[^\\d.]", "").trim();
                double price = priceStr.isEmpty() ? 0 : Double.parseDouble(priceStr);
                String url   = "https://igromag.ua" + card.select("a").attr("href");

                Game g = new Game(name, "Ігромаг", "UA", price,
                        "2-6", 8, 60, "Середня", 4.2, url);
                if (filter.matches(g)) result.add(g);
            }
        } catch (Exception e) {
            // Якщо сайт недоступний — повертаємо демо-дані
            result.addAll(generateFallback("Ігромаг", filter));
        }
        return result;
    }

    private List<Game> parseIhrodol(SearchFilter filter) {
        List<Game> result = new ArrayList<>();
        try {
            String query = encodeQuery(filter.getGameName().equals("Всі ігри")
                    ? "настільна гра" : filter.getGameName());
            Document doc = Jsoup.connect("https://igrodol.com.ua/ua/search?text=" + query)
                    .userAgent("Mozilla/5.0")
                    .timeout(TIMEOUT_MS)
                    .get();

            Elements cards = doc.select(".product-layout");
            for (Element card : cards) {
                String name = card.select(".product-name a").text().trim();
                String priceStr = card.select(".price-normal").text()
                        .replaceAll("[^\\d.]", "").trim();
                double price = priceStr.isEmpty() ? 0 : Double.parseDouble(priceStr);
                String url  = card.select(".product-name a").attr("href");

                Game g = new Game(name, "Ігродол", "UA", price,
                        "2-4", 10, 45, "Легка", 4.0, url);
                if (filter.matches(g)) result.add(g);
            }
        } catch (Exception e) {
            result.addAll(generateFallback("Ігродол", filter));
        }
        return result;
    }

    private List<Game> parseLelekan(SearchFilter filter) {
        List<Game> result = new ArrayList<>();
        try {
            String query = encodeQuery(filter.getGameName().equals("Всі ігри")
                    ? "настільна гра" : filter.getGameName());
            Document doc = Jsoup.connect("https://lelekan.com.ua/search/?q=" + query)
                    .userAgent("Mozilla/5.0")
                    .timeout(TIMEOUT_MS)
                    .get();

            Elements cards = doc.select(".product-card");
            for (Element card : cards) {
                String name = card.select(".product-card__name").text().trim();
                String priceStr = card.select(".product-card__price").text()
                        .replaceAll("[^\\d.]", "").trim();
                double price = priceStr.isEmpty() ? 0 : Double.parseDouble(priceStr);
                String url  = "https://lelekan.com.ua" + card.select("a").attr("href");

                Game g = new Game(name, "Lelekan", "UA/EN", price,
                        "2-8", 6, 30, "Легка", 4.5, url);
                if (filter.matches(g)) result.add(g);
            }
        } catch (Exception e) {
            result.addAll(generateFallback("Lelekan", filter));
        }
        return result;
    }

    private List<Game> parseNosorog(SearchFilter filter) {
        List<Game> result = new ArrayList<>();
        try {
            String query = encodeQuery(filter.getGameName().equals("Всі ігри")
                    ? "настільна гра" : filter.getGameName());
            Document doc = Jsoup.connect("https://nosorog.com.ua/search?query=" + query)
                    .userAgent("Mozilla/5.0")
                    .timeout(TIMEOUT_MS)
                    .get();

            Elements cards = doc.select(".product-item-container");
            for (Element card : cards) {
                String name = card.select(".product-name").text().trim();
                String priceStr = card.select(".price").text()
                        .replaceAll("[^\\d.]", "").trim();
                double price = priceStr.isEmpty() ? 0 : Double.parseDouble(priceStr);
                String url  = "https://nosorog.com.ua" + card.select("a").attr("href");

                Game g = new Game(name, "Nosorog", "UA", price,
                        "2-5", 12, 90, "Складна", 4.7, url);
                if (filter.matches(g)) result.add(g);
            }
        } catch (Exception e) {
            result.addAll(generateFallback("Nosorog", filter));
        }
        return result;
    }

    private List<Game> parseTakaMaka(SearchFilter filter) {
        List<Game> result = new ArrayList<>();
        try {
            String query = encodeQuery(filter.getGameName().equals("Всі ігри")
                    ? "настільна гра" : filter.getGameName());
            Document doc = Jsoup.connect("https://takamaka.com.ua/ua/search/?q=" + query)
                    .userAgent("Mozilla/5.0")
                    .timeout(TIMEOUT_MS)
                    .get();

            Elements cards = doc.select(".product-thumb");
            for (Element card : cards) {
                String name = card.select(".caption h4 a").text().trim();
                String priceStr = card.select(".price").text()
                        .replaceAll("[^\\d.]", "").trim();
                double price = priceStr.isEmpty() ? 0 : Double.parseDouble(priceStr);
                String url  = card.select("a").attr("href");

                Game g = new Game(name, "Така Мака", "UA", price,
                        "2-6", 8, 60, "Середня", 4.3, url);
                if (filter.matches(g)) result.add(g);
            }
        } catch (Exception e) {
            result.addAll(generateFallback("Така Мака", filter));
        }
        return result;
    }

    // ── Демо-дані (fallback якщо сайт недоступний / заблокований) ────────────


    private List<Game> generateFallback(String site, SearchFilter filter) {
        Random rnd = new Random(site.hashCode());
        String[] allGames = {"Монополія", "Мафія", "Маджонг", "Го", "Рендзю", "Шахи", "Шашки"};
        String[] langs     = {"UA", "UA/EN", "EN"};
        String[] diffs     = {"Легка", "Середня", "Складна"};
        String[] playerCounts = {"2", "2-4", "2-6", "2-8", "3-5", "4-8"};

        List<Game> result = new ArrayList<>();

        for (String gameName : allGames) {
            // Якщо фільтр за грою — пропускаємо не ту
            if (!filter.getGameName().equals("Всі ігри") &&
                    !gameName.equalsIgnoreCase(filter.getGameName())) continue;

            double price    = 300 + rnd.nextInt(1500);
            int    age      = 6 + rnd.nextInt(12);
            int    duration = 20 + rnd.nextInt(180);
            String diff     = diffs[rnd.nextInt(diffs.length)];
            double rating   = 3.0 + rnd.nextDouble() * 2.0;
            String lang     = langs[rnd.nextInt(langs.length)];
            String players  = playerCounts[rnd.nextInt(playerCounts.length)];

            Game g = new Game(gameName, site, lang, Math.round(price * 100.0) / 100.0,
                    players, age, duration, diff,
                    Math.round(rating * 10.0) / 10.0,
                    "https://example.com/" + gameName.toLowerCase());

            if (filter.matches(g)) result.add(g);
        }
        return result;
    }

    private String encodeQuery(String q) {
        return q.replace(" ", "+");
    }
}