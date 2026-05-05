package org.example.laba5kross.service;

import javafx.concurrent.Task;
import org.example.laba5kross.model.Game;
import org.example.laba5kross.model.SearchFilter;

import java.util.List;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

/**
 * JavaFX Task для виконання парсингу в окремому потоці.
 *
 * Прогрес оновлюється через updateProgress() / updateMessage(),
 * які автоматично передають значення у FX Application Thread.
 *
 * Знайдені результати повідомляються через callback onGameFound —
 * UI-компонент сам вирішує, як їх відобразити.
 */
public class SearchTask extends Task<Integer> {

    private final ParserService parser;
    private final SearchFilter  filter;
    private final Consumer<Game>          onGameFound;   // викликається per-game
    private final BiConsumer<String,Boolean> onSiteLog;  // (повідомлення, isError)

    public SearchTask(ParserService parser,
                      SearchFilter filter,
                      Consumer<Game> onGameFound,
                      BiConsumer<String, Boolean> onSiteLog) {
        this.parser      = parser;
        this.filter      = filter;
        this.onGameFound = onGameFound;
        this.onSiteLog   = onSiteLog;
    }

    @Override
    protected Integer call() throws Exception {
        String[] sites = parser.getSites();
        int totalFound = 0;

        for (int i = 0; i < sites.length; i++) {
            if (isCancelled()) break;

            String site = sites[i];
            updateMessage("Пошук на сайті: " + site + "…");
            updateProgress(i, sites.length);

            try {
                List<Game> found = parser.parseSite(site, filter);
                totalFound += found.size();

                for (Game g : found) {
                    // Callback виконується в ПОТОЧНОМУ (фоновому) потоці.
                    // UI має загорнути виклик у Platform.runLater сам.
                    onGameFound.accept(g);
                }

                onSiteLog.accept(
                        site + ": знайдено " + found.size() + " ігор", false);

            } catch (Exception e) {
                onSiteLog.accept(site + ": помилка — " + e.getMessage(), true);
            }
        }

        updateProgress(sites.length, sites.length);
        updateMessage("Пошук завершено. Всього знайдено: " + totalFound);
        return totalFound;
    }
}