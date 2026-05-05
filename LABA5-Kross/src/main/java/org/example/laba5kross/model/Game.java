package org.example.laba5kross.model;

import javafx.beans.property.*;

/**
 * Модель даних для настільної гри.
 * Використовує JavaFX Properties для автоматичного оновлення TableView.
 */
public class Game {

    private final StringProperty name;
    private final StringProperty site;
    private final StringProperty language;
    private final DoubleProperty price;
    private final StringProperty players;
    private final IntegerProperty minAge;
    private final IntegerProperty durationMinutes;
    private final StringProperty difficulty;
    private final DoubleProperty rating;
    private final StringProperty url;

    public Game(String name, String site, String language, double price,
                String players, int minAge, int durationMinutes,
                String difficulty, double rating, String url) {
        this.name             = new SimpleStringProperty(name);
        this.site             = new SimpleStringProperty(site);
        this.language         = new SimpleStringProperty(language);
        this.price            = new SimpleDoubleProperty(price);
        this.players          = new SimpleStringProperty(players);
        this.minAge           = new SimpleIntegerProperty(minAge);
        this.durationMinutes  = new SimpleIntegerProperty(durationMinutes);
        this.difficulty       = new SimpleStringProperty(difficulty);
        this.rating           = new SimpleDoubleProperty(rating);
        this.url              = new SimpleStringProperty(url);
    }

    // ── Getters (потрібні для PropertyValueFactory та CSV-експорту) ──────────

    public String getName()            { return name.get(); }
    public String getSite()            { return site.get(); }
    public String getLanguage()        { return language.get(); }
    public double getPrice()           { return price.get(); }
    public String getPlayers()         { return players.get(); }
    public int    getMinAge()          { return minAge.get(); }
    public int    getDurationMinutes() { return durationMinutes.get(); }
    public String getDifficulty()      { return difficulty.get(); }
    public double getRating()          { return rating.get(); }
    public String getUrl()             { return url.get(); }

    // ── Property accessors (потрібні для TableView bindings) ─────────────────

    public StringProperty  nameProperty()            { return name; }
    public StringProperty  siteProperty()            { return site; }
    public StringProperty  languageProperty()        { return language; }
    public DoubleProperty  priceProperty()           { return price; }
    public StringProperty  playersProperty()         { return players; }
    public IntegerProperty minAgeProperty()          { return minAge; }
    public IntegerProperty durationMinutesProperty() { return durationMinutes; }
    public StringProperty  difficultyProperty()      { return difficulty; }
    public DoubleProperty  ratingProperty()          { return rating; }
    public StringProperty  urlProperty()             { return url; }
}