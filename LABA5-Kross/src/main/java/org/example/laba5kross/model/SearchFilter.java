package org.example.laba5kross.model;


public class SearchFilter {

    public enum Operator { LESS, EQUAL, GREATER }

    private final String gameName;      // назва гри
    private final int    minAge;        // вік гравців
    private final Operator ageOp;
    private final int    duration;      // тривалість, хв
    private final Operator durationOp;
    private final String difficulty;    // легка / середня / складна / Будь-яка
    private final double rating;        // рейтинг 0..10
    private final Operator ratingOp;

    public SearchFilter(String gameName,
                        int minAge, Operator ageOp,
                        int duration, Operator durationOp,
                        String difficulty,
                        double rating, Operator ratingOp) {
        this.gameName   = gameName;
        this.minAge     = minAge;
        this.ageOp      = ageOp;
        this.duration   = duration;
        this.durationOp = durationOp;
        this.difficulty = difficulty;
        this.rating     = rating;
        this.ratingOp   = ratingOp;
    }

    /** Перевіряє, чи відповідає гра заданим фільтрам. */
    public boolean matches(Game g) {
        // Фільтр за назвою (якщо не "Всі ігри")
        if (!gameName.equals("Всі ігри") && !g.getName().equalsIgnoreCase(gameName))
            return false;

        // Фільтр за складністю
        if (!difficulty.equals("Будь-яка") && !g.getDifficulty().equalsIgnoreCase(difficulty))
            return false;

        // Фільтр за віком
        if (!compare(g.getMinAge(), minAge, ageOp))         return false;
        // Фільтр за тривалістю
        if (!compare(g.getDurationMinutes(), duration, durationOp)) return false;
        // Фільтр за рейтингом
        if (!compareDouble(g.getRating(), rating, ratingOp))        return false;

        return true;
    }

    private boolean compare(int value, int threshold, Operator op) {
        return switch (op) {
            case LESS    -> value < threshold;
            case EQUAL   -> value == threshold;
            case GREATER -> value > threshold;
        };
    }

    private boolean compareDouble(double value, double threshold, Operator op) {
        return switch (op) {
            case LESS    -> value < threshold;
            case EQUAL   -> Math.abs(value - threshold) < 0.001;
            case GREATER -> value > threshold;
        };
    }

    public String getGameName() { return gameName; }
}