package org.example.laba5kross.ui;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.concurrent.Task;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.*;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import org.kordamp.bootstrapfx.BootstrapFX;
import org.example.laba5kross.model.Game;
import org.example.laba5kross.model.SearchFilter;
import org.example.laba5kross.service.FileService;
import org.example.laba5kross.service.ParserService;
import org.example.laba5kross.service.SearchTask;

import java.io.File;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

/**
 * Головне вікно застосунку BoardGame Hunter.
 *
 * Відповідає виключно за побудову UI та делегує бізнес-логіку
 * сервісам ParserService і FileService.
 */
public class MainWindow extends Application {

    // ── Сервіси ───────────────────────────────────────────────────────────────
    private final ParserService parserService = new ParserService();
    private final FileService   fileService   = new FileService();

    // ── Дані таблиці ──────────────────────────────────────────────────────────
    private final ObservableList<Game> games = FXCollections.observableArrayList();

    // ── UI-компоненти, до яких потрібен доступ з кількох методів ─────────────
    private Stage          primaryStage;
    private TableView<Game> table       = new TableView<>();
    private TextArea        logArea     = new TextArea();
    private ProgressBar     progressBar = new ProgressBar(0);
    private Label           progressLabel = new Label("Готовий до пошуку");

    // Фільтри (sidebar)
    private ComboBox<String> gameCombo;
    private TextField        searchField;

    // Спінери додаткових атрибутів
    private Spinner<Integer> ageSpinner;
    private ComboBox<String> ageOpCombo;
    private Spinner<Integer> durationSpinner;
    private ComboBox<String> durationOpCombo;
    private ComboBox<String> difficultyCombo;
    private Spinner<Double>  ratingSpinner;
    private ComboBox<String> ratingOpCombo;

    // Поточний Task (щоб можна було скасувати)
    private SearchTask currentTask;

    @Override
    public void start(Stage stage) {
        this.primaryStage = stage;
        stage.setTitle("BoardGame Hunter — Пошук Настільних Ігор");

        BorderPane root = new BorderPane();

        root.setTop(buildHeader());
        root.setLeft(buildSidebar());
        root.setCenter(buildCenter());

        Scene scene = new Scene(root, 1550, 900);
        scene.getStylesheets().add(BootstrapFX.bootstrapFXStylesheet());
        scene.getStylesheets().add(getClass().getResource("/ua/kolodiuk/styles.css").toExternalForm());

        stage.setScene(scene);
        stage.show();

        log("Програму запущено. Оберіть гру та натисніть «Шукати».");
    }

    // ── Header ────────────────────────────────────────────────────────────────

    private HBox buildHeader() {
        HBox header = new HBox();
        header.setStyle("-fx-background-color: #1e3a8a; -fx-padding: 16 24;");
        header.setAlignment(Pos.CENTER_LEFT);

        Label title = new Label("♟  BoardGame Hunter");
        title.setStyle("-fx-font-size: 26px; -fx-font-weight: bold; -fx-text-fill: white;");

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        Label sub = new Label("Пошук настільних ігор в українських магазинах");
        sub.setStyle("-fx-font-size: 13px; -fx-text-fill: #93c5fd;");

        header.getChildren().addAll(title, spacer, sub);
        return header;
    }

    // ── Sidebar ───────────────────────────────────────────────────────────────

    private VBox buildSidebar() {
        VBox sidebar = new VBox(18);
        sidebar.setPadding(new Insets(24));
        sidebar.setPrefWidth(300);
        sidebar.setStyle("-fx-background-color: #1f2937;");

        // --- Основний пошук ---
        Label searchTitle = sideLabel("🔎 Пошук", "#93c5fd", 18);

        searchField = new TextField();
        searchField.setPromptText("Назва гри...");

        gameCombo = new ComboBox<>();
        gameCombo.getItems().addAll("Всі ігри", "Монополія", "Мафія", "Маджонг",
                "Го", "Рендзю", "Шахи", "Шашки");
        gameCombo.setValue("Всі ігри");
        gameCombo.setMaxWidth(Double.MAX_VALUE);
        // Синхронізуємо textField з combo
        gameCombo.valueProperty().addListener((obs, o, n) -> {
            if (!n.equals("Всі ігри")) searchField.setText(n);
            else searchField.clear();
        });

        // --- Додаткові фільтри ---
        Label filterTitle = sideLabel("⚙️ Додаткові фільтри", "#fcd34d", 15);

        // Вік
        Label ageLabel = sideLabel("Вік гравців:", "white", 13);
        ageOpCombo = operatorCombo();
        ageSpinner = new Spinner<>(0, 99, 6, 1);
        ageSpinner.setEditable(true);
        HBox ageRow = filterRow(ageOpCombo, ageSpinner);

        // Тривалість
        Label durLabel = sideLabel("Тривалість (хв):", "white", 13);
        durationOpCombo = operatorCombo();
        durationSpinner = new Spinner<>(5, 600, 60, 5);
        durationSpinner.setEditable(true);
        HBox durRow = filterRow(durationOpCombo, durationSpinner);

        // Складність
        Label diffLabel = sideLabel("Складність:", "white", 13);
        difficultyCombo = new ComboBox<>();
        difficultyCombo.getItems().addAll("Будь-яка", "Легка", "Середня", "Складна");
        difficultyCombo.setValue("Будь-яка");
        difficultyCombo.setMaxWidth(Double.MAX_VALUE);

        // Рейтинг
        Label ratingLabel = sideLabel("Рейтинг (0–5):", "white", 13);
        ratingOpCombo = operatorCombo();
        ratingSpinner = new Spinner<>(0.0, 5.0, 3.0, 0.1);
        ratingSpinner.setEditable(true);
        HBox ratingRow = filterRow(ratingOpCombo, ratingSpinner);

        // --- Кнопки ---
        Button btnSearch = new Button("🔍 Шукати");
        btnSearch.setMaxWidth(Double.MAX_VALUE);
        btnSearch.setStyle("-fx-font-size: 15px; -fx-padding: 10 0; " +
                "-fx-background-color: #2563eb; -fx-text-fill: white; " +
                "-fx-font-weight: bold; -fx-cursor: hand;");
        btnSearch.setOnAction(e -> startSearch());

        Button btnCancel = new Button("⏹ Зупинити");
        btnCancel.setMaxWidth(Double.MAX_VALUE);
        btnCancel.setStyle("-fx-font-size: 13px; -fx-background-color: #dc2626; " +
                "-fx-text-fill: white; -fx-cursor: hand;");
        btnCancel.setOnAction(e -> cancelSearch());

        Button btnExport = new Button("💾 Зберегти CSV");
        btnExport.setMaxWidth(Double.MAX_VALUE);
        btnExport.setStyle("-fx-font-size: 13px; -fx-background-color: #059669; " +
                "-fx-text-fill: white; -fx-cursor: hand;");
        btnExport.setOnAction(e -> exportCsv());

        Button btnImport = new Button("📂 Завантажити CSV");
        btnImport.setMaxWidth(Double.MAX_VALUE);
        btnImport.setStyle("-fx-font-size: 13px; -fx-background-color: #7c3aed; " +
                "-fx-text-fill: white; -fx-cursor: hand;");
        btnImport.setOnAction(e -> importCsv());

        Button btnClear = new Button("🗑 Очистити");
        btnClear.setMaxWidth(Double.MAX_VALUE);
        btnClear.setStyle("-fx-font-size: 13px; -fx-cursor: hand;");
        btnClear.setOnAction(e -> { games.clear(); log("Таблицю очищено."); });

        sidebar.getChildren().addAll(
                searchTitle, gameCombo, searchField,
                new Separator(),
                filterTitle,
                ageLabel, ageRow,
                durLabel, durRow,
                diffLabel, difficultyCombo,
                ratingLabel, ratingRow,
                new Separator(),
                btnSearch, btnCancel,
                new Separator(),
                btnExport, btnImport, btnClear
        );
        return sidebar;
    }

    // ── Center ────────────────────────────────────────────────────────────────

    private VBox buildCenter() {
        VBox center = new VBox(12);
        center.setPadding(new Insets(20));

        buildTable();

        // Прогрес
        progressBar.setMaxWidth(Double.MAX_VALUE);
        HBox progressBox = new HBox(12, progressBar, progressLabel);
        progressBox.setAlignment(Pos.CENTER_LEFT);
        HBox.setHgrow(progressBar, Priority.ALWAYS);

        // Лог
        logArea.setEditable(false);
        logArea.setPrefHeight(150);
        logArea.setStyle("-fx-font-family: 'Monospaced'; -fx-font-size: 12px;");

        VBox logBox = new VBox(6, new Label("📋 Журнал виконання:"), logArea);

        VBox.setVgrow(table, Priority.ALWAYS);
        center.getChildren().addAll(table, progressBox, logBox);
        return center;
    }

    // ── Table ─────────────────────────────────────────────────────────────────

    @SuppressWarnings("unchecked")
    private void buildTable() {
        TableColumn<Game, String>  colName     = col("Назва гри",        "name",            350);
        TableColumn<Game, String>  colSite     = col("Сайт",             "site",            120);
        TableColumn<Game, String>  colLang     = col("Мова",             "language",         80);
        TableColumn<Game, Double>  colPrice    = col("Ціна ₴",           "price",           100);
        TableColumn<Game, String>  colPlayers  = col("Гравців",          "players",          80);
        TableColumn<Game, Integer> colAge      = col("Вік",              "minAge",           60);
        TableColumn<Game, Integer> colDuration = col("Тривалість (хв)",  "durationMinutes", 120);
        TableColumn<Game, String>  colDiff     = col("Складність",       "difficulty",      100);
        TableColumn<Game, Double>  colRating   = col("Рейтинг",          "rating",           80);

        // Колонка URL (клікабельна)
        TableColumn<Game, String> colUrl = new TableColumn<>("URL");
        colUrl.setCellValueFactory(new PropertyValueFactory<>("url"));
        colUrl.setPrefWidth(200);
        colUrl.setCellFactory(tc -> new TableCell<>() {
            private final Hyperlink link = new Hyperlink();
            {
                link.setOnAction(e -> {
                    Game g = getTableRow().getItem();
                    if (g != null) getHostServices().showDocument(g.getUrl());
                });
            }
            @Override
            protected void updateItem(String url, boolean empty) {
                super.updateItem(url, empty);
                if (empty || url == null) { setGraphic(null); }
                else { link.setText(url); setGraphic(link); }
            }
        });

        table.getColumns().addAll(
                colName, colSite, colLang, colPrice, colPlayers,
                colAge, colDuration, colDiff, colRating, colUrl);
        table.setItems(games);
        table.setPlaceholder(new Label("Ігор не знайдено. Натисніть «Шукати»."));
    }

    // ── Дії ───────────────────────────────────────────────────────────────────

    private void startSearch() {
        SearchFilter filter = buildFilter();

        games.clear();
        progressBar.setProgress(0);
        progressLabel.setText("Починаємо пошук…");

        currentTask = new SearchTask(
                parserService,
                filter,
                game -> Platform.runLater(() -> games.add(game)),
                (msg, isError) -> Platform.runLater(() -> log((isError ? "⚠ " : "✔ ") + msg))
        );

        // Прив'язуємо прогрес до ProgressBar
        progressBar.progressProperty().bind(currentTask.progressProperty());
        progressLabel.textProperty().bind(currentTask.messageProperty());

        currentTask.setOnSucceeded(e -> {
            progressBar.progressProperty().unbind();
            progressLabel.textProperty().unbind();
            progressBar.setProgress(1.0);
            log("✅ Пошук завершено. Знайдено: " + games.size() + " ігор.");
        });
        currentTask.setOnFailed(e -> {
            progressBar.progressProperty().unbind();
            progressLabel.textProperty().unbind();
            log("❌ Помилка пошуку: " + currentTask.getException().getMessage());
        });

        Thread thread = new Thread(currentTask);
        thread.setDaemon(true);  // зупиняється разом із програмою
        thread.start();
        log("🔍 Пошук запущено: " + filter.getGameName());
    }

    private void cancelSearch() {
        if (currentTask != null && currentTask.isRunning()) {
            currentTask.cancel();
            log("⏹ Пошук скасовано користувачем.");
        }
    }

    private void exportCsv() {
        if (games.isEmpty()) {
            showAlert("Немає даних для збереження!");
            return;
        }
        FileChooser chooser = new FileChooser();
        chooser.setTitle("Зберегти результати");
        chooser.setInitialFileName("boardgames_" +
                LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd_HH-mm")) + ".csv");
        chooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("CSV Files", "*.csv"));
        File file = chooser.showSaveDialog(primaryStage);

        if (file == null) return;

        // Файлова операція в окремому потоці
        Task<Void> saveTask = new Task<>() {
            @Override
            protected Void call() throws Exception {
                fileService.writeCsv(List.copyOf(games), file);
                return null;
            }
        };
        saveTask.setOnSucceeded(e -> log("💾 Збережено: " + file.getName() +
                " (" + games.size() + " рядків)"));
        saveTask.setOnFailed(e -> log("❌ Помилка збереження: " +
                saveTask.getException().getMessage()));

        new Thread(saveTask).start();
    }

    private void importCsv() {
        FileChooser chooser = new FileChooser();
        chooser.setTitle("Відкрити CSV");
        chooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("CSV Files", "*.csv"));
        File file = chooser.showOpenDialog(primaryStage);

        if (file == null) return;

        // Файлова операція в окремому потоці
        Task<List<Game>> loadTask = new Task<>() {
            @Override
            protected List<Game> call() throws IOException {
                return fileService.readCsv(file);
            }
        };
        loadTask.setOnSucceeded(e -> {
            games.clear();
            games.addAll(loadTask.getValue());
            log("📂 Завантажено: " + file.getName() + " (" + games.size() + " рядків)");
        });
        loadTask.setOnFailed(e -> log("❌ Помилка читання: " +
                loadTask.getException().getMessage()));

        new Thread(loadTask).start();
    }

    // ── Допоміжні методи UI ───────────────────────────────────────────────────

    private SearchFilter buildFilter() {
        String gameName = gameCombo.getValue();
        // Якщо поле вручну — пріоритет йому
        if (!searchField.getText().isBlank() && gameName.equals("Всі ігри"))
            gameName = searchField.getText().trim();

        return new SearchFilter(
                gameName,
                ageSpinner.getValue(), toOp(ageOpCombo.getValue()),
                durationSpinner.getValue(), toOp(durationOpCombo.getValue()),
                difficultyCombo.getValue(),
                ratingSpinner.getValue(), toOp(ratingOpCombo.getValue())
        );
    }

    private SearchFilter.Operator toOp(String symbol) {
        return switch (symbol) {
            case "<"  -> SearchFilter.Operator.LESS;
            case "="  -> SearchFilter.Operator.EQUAL;
            default   -> SearchFilter.Operator.GREATER;
        };
    }

    private ComboBox<String> operatorCombo() {
        ComboBox<String> cb = new ComboBox<>();
        cb.getItems().addAll("<", "=", ">");
        cb.setValue(">");
        cb.setPrefWidth(60);
        return cb;
    }

    private HBox filterRow(ComboBox<String> op, Spinner<?> spinner) {
        HBox row = new HBox(8, op, spinner);
        row.setAlignment(Pos.CENTER_LEFT);
        HBox.setHgrow(spinner, Priority.ALWAYS);
        spinner.setMaxWidth(Double.MAX_VALUE);
        return row;
    }

    private Label sideLabel(String text, String color, int size) {
        Label l = new Label(text);
        l.setStyle("-fx-text-fill: " + color + "; -fx-font-size: " + size + "px; " +
                "-fx-font-weight: bold;");
        return l;
    }

    private <T> TableColumn<Game, T> col(String title, String property, int width) {
        TableColumn<Game, T> c = new TableColumn<>(title);
        c.setCellValueFactory(new PropertyValueFactory<>(property));
        c.setPrefWidth(width);
        return c;
    }

    private void log(String message) {
        Platform.runLater(() -> {
            String time = LocalDateTime.now()
                    .format(DateTimeFormatter.ofPattern("HH:mm:ss"));
            logArea.appendText("[" + time + "] " + message + "\n");
        });
    }

    private void showAlert(String text) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("BoardGame Hunter");
        alert.setHeaderText(null);
        alert.setContentText(text);
        alert.initOwner(primaryStage);
        alert.show();
    }
}