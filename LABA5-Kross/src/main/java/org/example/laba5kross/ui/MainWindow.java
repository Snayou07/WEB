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


public class MainWindow extends Application {

    // ── Сервіси ───────────────────────────────────────────────────────────────
    private final ParserService parserService = new ParserService();
    private final FileService   fileService   = new FileService();

    // ── Дані таблиці ──────────────────────────────────────────────────────────
    private final ObservableList<Game> games = FXCollections.observableArrayList();

    // ── UI-компоненти, до яких потрібен доступ з кількох методів ─────────────
    private Stage           primaryStage;
    private TableView<Game> table         = new TableView<>();
    private TextArea        logArea       = new TextArea();
    private ProgressBar     progressBar   = new ProgressBar(0);
    private Label           progressLabel = new Label("Готовий до пошуку");

    // Фільтри (sidebar)
    private ComboBox<String>  gameCombo;
    private TextField         searchField;
    private Spinner<Integer>  ageSpinner;
    private ComboBox<String>  ageOpCombo;
    private Spinner<Integer>  durationSpinner;
    private ComboBox<String>  durationOpCombo;
    private ComboBox<String>  difficultyCombo;
    private Spinner<Double>   ratingSpinner;
    private ComboBox<String>  ratingOpCombo;

    // Поточний Task (щоб можна було скасувати)
    private SearchTask currentTask;

    // ─────────────────────────────────────────────────────────────────────────
    @Override
    public void start(Stage stage) {
        this.primaryStage = stage;
        stage.setTitle("BoardGame Hunter — Пошук Настільних Ігор");

        BorderPane root = new BorderPane();

        root.setTop(buildHeader());
        root.setLeft(buildSidebar());
        root.setCenter(buildCenter());

        Scene scene = new Scene(root, 1550, 900);

        // Bootstrap (загальна база)
        scene.getStylesheets().add(BootstrapFX.bootstrapFXStylesheet());

        // ════════════════════════════════════════════════════════════════════
        // ВАЖЛИВО: styles.css повинен лежати за цим шляхом:
        //   src/main/resources/ua/kolodiuk/styles.css
        // ════════════════════════════════════════════════════════════════════
        String cssPath = getClass().getResource("/ua/kolodiuk/styles.css") != null
                ? getClass().getResource("/ua/kolodiuk/styles.css").toExternalForm()
                : null;
        if (cssPath != null) {
            scene.getStylesheets().add(cssPath);
        }

        stage.setScene(scene);
        stage.show();

        log("Програму запущено. Оберіть гру та натисніть «Шукати».");
    }

    // ══════════════════════════════════════════════════════════════════════════
    // Header
    // ══════════════════════════════════════════════════════════════════════════
    private HBox buildHeader() {
        HBox header = new HBox();
        header.setStyle(
                "-fx-background-color: #0f172a;" +
                        "-fx-border-color: #1e3a8a;" +
                        "-fx-border-width: 0 0 2 0;" +
                        "-fx-padding: 16 28;"
        );
        header.setAlignment(Pos.CENTER_LEFT);
        header.setSpacing(12);

        Label icon = new Label("♟");
        icon.setStyle("-fx-font-size: 30px; -fx-text-fill: #3b82f6;");

        Label title = new Label("BoardGame Hunter");
        title.setStyle(
                "-fx-font-size: 22px;" +
                        "-fx-font-weight: bold;" +
                        "-fx-text-fill: #f1f5f9;" +
                        "-fx-font-family: 'Segoe UI', sans-serif;"
        );

        Label badge = new Label("BETA");
        badge.setStyle(
                "-fx-background-color: #1d4ed8;" +
                        "-fx-text-fill: white;" +
                        "-fx-font-size: 10px;" +
                        "-fx-font-weight: bold;" +
                        "-fx-padding: 2 6;" +
                        "-fx-background-radius: 4;"
        );

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        Label sub = new Label("Пошук настільних ігор в українських магазинах");
        sub.setStyle("-fx-font-size: 13px; -fx-text-fill: #475569;");

        // Лічильник результатів
        Label counter = new Label("0 ігор знайдено");
        counter.setStyle(
                "-fx-text-fill: #3b82f6;" +
                        "-fx-font-size: 13px;" +
                        "-fx-font-weight: bold;"
        );
        games.addListener((javafx.collections.ListChangeListener<Game>) c ->
                counter.setText(games.size() + " ігор знайдено")
        );

        header.getChildren().addAll(icon, title, badge, spacer, counter, sub);
        return header;
    }

    // ══════════════════════════════════════════════════════════════════════════
    // Sidebar
    // ══════════════════════════════════════════════════════════════════════════
    private VBox buildSidebar() {
        VBox sidebar = new VBox(14);
        sidebar.setPadding(new Insets(20));
        sidebar.setPrefWidth(290);
        sidebar.getStyleClass().add("sidebar-root");
        sidebar.setStyle("-fx-background-color: #1e293b; -fx-border-color: #334155; -fx-border-width: 0 1 0 0;");

        // ── Секція: пошук ──────────────────────────────────────────────────
        Label searchTitle = sideLabel("🔎  ПОШУК", "#3b82f6", 12);
        searchTitle.setStyle(searchTitle.getStyle() +
                "-fx-letter-spacing: 1.5; -fx-padding: 0 0 4 0;");

        searchField = new TextField();
        searchField.setPromptText("Назва гри...");
        searchField.getStyleClass().add("text-field");

        gameCombo = new ComboBox<>();
        gameCombo.getItems().addAll(
                "Всі ігри", "Монополія", "Мафія", "Маджонг",
                "Го", "Рендзю", "Шахи", "Шашки"
        );
        gameCombo.setValue("Всі ігри");
        gameCombo.setMaxWidth(Double.MAX_VALUE);
        gameCombo.getStyleClass().add("combo-box");
        gameCombo.valueProperty().addListener((obs, o, n) -> {
            if (!n.equals("Всі ігри")) searchField.setText(n);
            else searchField.clear();
        });

        // ── Секція: фільтри ────────────────────────────────────────────────
        Label filterTitle = sideLabel("⚙  ФІЛЬТРИ", "#f59e0b", 12);
        filterTitle.setStyle(filterTitle.getStyle() +
                "-fx-letter-spacing: 1.5; -fx-padding: 4 0 4 0;");

        // Вік
        Label ageLabel = sideLabel("Вік гравців", "#94a3b8", 12);
        ageOpCombo  = operatorCombo();
        ageSpinner  = new Spinner<>(0, 99, 6, 1);
        ageSpinner.setEditable(true);
        HBox ageRow = filterRow(ageOpCombo, ageSpinner);

        // Тривалість
        Label durLabel = sideLabel("Тривалість (хв)", "#94a3b8", 12);
        durationOpCombo = operatorCombo();
        durationSpinner = new Spinner<>(5, 600, 60, 5);
        durationSpinner.setEditable(true);
        HBox durRow = filterRow(durationOpCombo, durationSpinner);

        // Складність
        Label diffLabel = sideLabel("Складність", "#94a3b8", 12);
        difficultyCombo = new ComboBox<>();
        difficultyCombo.getItems().addAll("Будь-яка", "Легка", "Середня", "Складна");
        difficultyCombo.setValue("Будь-яка");
        difficultyCombo.setMaxWidth(Double.MAX_VALUE);
        difficultyCombo.getStyleClass().add("combo-box");

        // Рейтинг
        Label ratingLabel = sideLabel("Рейтинг (0–5)", "#94a3b8", 12);
        ratingOpCombo = operatorCombo();
        ratingSpinner = new Spinner<>(0.0, 5.0, 3.0, 0.1);
        ratingSpinner.setEditable(true);
        HBox ratingRow = filterRow(ratingOpCombo, ratingSpinner);

        // ── Кнопки ────────────────────────────────────────────────────────
        Button btnSearch = styledButton("🔍   Шукати", "btn-search",
                "-fx-background-color: #2563eb; -fx-text-fill: white; " +
                        "-fx-font-size: 14px; -fx-font-weight: bold; " +
                        "-fx-padding: 11 0; -fx-background-radius: 8; -fx-cursor: hand;"
        );
        btnSearch.setMaxWidth(Double.MAX_VALUE);
        btnSearch.setOnAction(e -> startSearch());

        Button btnCancel = styledButton("⏹   Зупинити", "btn-danger",
                "-fx-background-color: #dc2626; -fx-text-fill: white; " +
                        "-fx-background-radius: 8; -fx-cursor: hand; " +
                        "-fx-font-size: 13px; -fx-padding: 9 0;"
        );
        btnCancel.setMaxWidth(Double.MAX_VALUE);
        btnCancel.setOnAction(e -> cancelSearch());

        Button btnExport = styledButton("💾   Зберегти CSV", "btn-success",
                "-fx-background-color: #059669; -fx-text-fill: white; " +
                        "-fx-background-radius: 8; -fx-cursor: hand; " +
                        "-fx-font-size: 13px; -fx-padding: 9 0;"
        );
        btnExport.setMaxWidth(Double.MAX_VALUE);
        btnExport.setOnAction(e -> exportCsv());

        Button btnImport = styledButton("📂   Завантажити CSV", "btn-purple",
                "-fx-background-color: #7c3aed; -fx-text-fill: white; " +
                        "-fx-background-radius: 8; -fx-cursor: hand; " +
                        "-fx-font-size: 13px; -fx-padding: 9 0;"
        );
        btnImport.setMaxWidth(Double.MAX_VALUE);
        btnImport.setOnAction(e -> importCsv());

        Button btnClear = styledButton("🗑   Очистити таблицю", "btn-neutral",
                "-fx-background-color: #334155; -fx-text-fill: #e2e8f0; " +
                        "-fx-background-radius: 8; -fx-cursor: hand; " +
                        "-fx-font-size: 13px; -fx-padding: 9 0;"
        );
        btnClear.setMaxWidth(Double.MAX_VALUE);
        btnClear.setOnAction(e -> { games.clear(); log("Таблицю очищено."); });

        sidebar.getChildren().addAll(
                searchTitle, gameCombo, searchField,
                divider(),
                filterTitle,
                ageLabel,    ageRow,
                durLabel,    durRow,
                diffLabel,   difficultyCombo,
                ratingLabel, ratingRow,
                divider(),
                btnSearch, btnCancel,
                divider(),
                btnExport, btnImport, btnClear
        );
        return sidebar;
    }

    // ══════════════════════════════════════════════════════════════════════════
    // Center
    // ══════════════════════════════════════════════════════════════════════════
    private VBox buildCenter() {
        VBox center = new VBox(14);
        center.setPadding(new Insets(20));
        center.setStyle("-fx-background-color: #0f172a;");

        buildTable();

        // Прогрес-бар
        progressBar.setMaxWidth(Double.MAX_VALUE);
        progressBar.setPrefHeight(10);
        progressLabel.setStyle("-fx-text-fill: #64748b; -fx-font-size: 12px;");

        HBox progressBox = new HBox(14, progressBar, progressLabel);
        progressBox.setAlignment(Pos.CENTER_LEFT);
        HBox.setHgrow(progressBar, Priority.ALWAYS);

        // Лог
        logArea.setEditable(false);
        logArea.setPrefHeight(140);
        logArea.setStyle(
                "-fx-font-family: 'Cascadia Code', 'Consolas', monospace;" +
                        "-fx-font-size: 12px;"
        );

        Label logTitle = new Label("📋  Журнал виконання");
        logTitle.setStyle("-fx-text-fill: #475569; -fx-font-size: 12px; -fx-font-weight: bold;");
        VBox logBox = new VBox(6, logTitle, logArea);

        VBox.setVgrow(table, Priority.ALWAYS);
        center.getChildren().addAll(table, progressBox, logBox);
        return center;
    }

    // ══════════════════════════════════════════════════════════════════════════
    // Table
    // ══════════════════════════════════════════════════════════════════════════
    @SuppressWarnings("unchecked")
    private void buildTable() {
        TableColumn<Game, String>  colName     = col("Назва гри",       "name",            340);
        TableColumn<Game, String>  colSite     = col("Сайт",            "site",            110);
        TableColumn<Game, String>  colLang     = col("Мова",            "language",         75);
        TableColumn<Game, Double>  colPrice    = col("Ціна ₴",          "price",            95);
        TableColumn<Game, String>  colPlayers  = col("Гравців",         "players",          75);
        TableColumn<Game, Integer> colAge      = col("Вік",             "minAge",           55);
        TableColumn<Game, Integer> colDuration = col("Тривал. (хв)",    "durationMinutes", 105);
        TableColumn<Game, String>  colDiff     = col("Складність",      "difficulty",       95);
        TableColumn<Game, Double>  colRating   = col("Рейтинг",         "rating",           75);

        // Колонка рейтингу — кольорове підсвічування
        colRating.setCellFactory(tc -> new TableCell<>() {
            @Override
            protected void updateItem(Double val, boolean empty) {
                super.updateItem(val, empty);
                if (empty || val == null) { setText(null); setStyle(""); return; }
                setText(String.format("%.1f", val));
                String color = val >= 4.5 ? "#34d399"
                        : val >= 3.5 ? "#fbbf24"
                        :              "#f87171";
                setStyle("-fx-text-fill: " + color + "; -fx-font-weight: bold;");
            }
        });

        // Клікабельний URL
        TableColumn<Game, String> colUrl = new TableColumn<>("URL");
        colUrl.setCellValueFactory(new PropertyValueFactory<>("url"));
        colUrl.setPrefWidth(190);
        colUrl.setCellFactory(tc -> new TableCell<>() {
            private final Hyperlink link = new Hyperlink();
            {
                link.setOnAction(e -> {
                    Game g = getTableRow().getItem();
                    if (g != null) getHostServices().showDocument(g.getUrl());
                });
                link.setStyle("-fx-text-fill: #60a5fa; -fx-font-size: 12px;");
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
                colAge, colDuration, colDiff, colRating, colUrl
        );
        table.setItems(games);
        table.setPlaceholder(new Label("Ігор не знайдено. Натисніть «Шукати»."));
        table.setStyle(
                "-fx-background-color: #1e293b;" +
                        "-fx-border-color: #334155;" +
                        "-fx-border-radius: 10;" +
                        "-fx-background-radius: 10;"
        );
    }

    // ══════════════════════════════════════════════════════════════════════════
    // Дії
    // ══════════════════════════════════════════════════════════════════════════
    private void startSearch() {
        SearchFilter filter = buildFilter();

        games.clear();
        progressBar.setProgress(0);
        progressLabel.setText("Починаємо пошук…");

        currentTask = new SearchTask(
                parserService,
                filter,
                game -> Platform.runLater(() -> games.add(game)),
                (msg, isError) -> Platform.runLater(() -> log((isError ? "⚠  " : "✔  ") + msg))
        );

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
        thread.setDaemon(true);
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
        if (games.isEmpty()) { showAlert("Немає даних для збереження!"); return; }
        FileChooser chooser = new FileChooser();
        chooser.setTitle("Зберегти результати");
        chooser.setInitialFileName("boardgames_" +
                LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd_HH-mm")) + ".csv");
        chooser.getExtensionFilters().add(
                new FileChooser.ExtensionFilter("CSV Files", "*.csv"));
        File file = chooser.showSaveDialog(primaryStage);
        if (file == null) return;

        Task<Void> saveTask = new Task<>() {
            @Override protected Void call() throws Exception {
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
        chooser.getExtensionFilters().add(
                new FileChooser.ExtensionFilter("CSV Files", "*.csv"));
        File file = chooser.showOpenDialog(primaryStage);
        if (file == null) return;

        Task<List<Game>> loadTask = new Task<>() {
            @Override protected List<Game> call() throws IOException {
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

    // ══════════════════════════════════════════════════════════════════════════
    // Helpers
    // ══════════════════════════════════════════════════════════════════════════
    private SearchFilter buildFilter() {
        String gameName = gameCombo.getValue();
        if (!searchField.getText().isBlank() && gameName.equals("Всі ігри"))
            gameName = searchField.getText().trim();

        return new SearchFilter(
                gameName,
                ageSpinner.getValue(),      toOp(ageOpCombo.getValue()),
                durationSpinner.getValue(), toOp(durationOpCombo.getValue()),
                difficultyCombo.getValue(),
                ratingSpinner.getValue(),   toOp(ratingOpCombo.getValue())
        );
    }

    private SearchFilter.Operator toOp(String symbol) {
        return switch (symbol) {
            case "<" -> SearchFilter.Operator.LESS;
            case "=" -> SearchFilter.Operator.EQUAL;
            default  -> SearchFilter.Operator.GREATER;
        };
    }

    private ComboBox<String> operatorCombo() {
        ComboBox<String> cb = new ComboBox<>();
        cb.getItems().addAll("<", "=", ">");
        cb.setValue(">");
        cb.setPrefWidth(60);
        cb.getStyleClass().add("combo-box");
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
        l.setStyle(
                "-fx-text-fill: " + color + ";" +
                        "-fx-font-size: " + size + "px;" +
                        "-fx-font-weight: bold;"
        );
        return l;
    }

    private <T> TableColumn<Game, T> col(String title, String property, int width) {
        TableColumn<Game, T> c = new TableColumn<>(title);
        c.setCellValueFactory(new PropertyValueFactory<>(property));
        c.setPrefWidth(width);
        return c;
    }

    /** Допоміжний метод для кнопок з CSS-класом і inline-стилем */
    private Button styledButton(String text, String cssClass, String inlineStyle) {
        Button b = new Button(text);
        b.getStyleClass().add(cssClass);
        b.setStyle(inlineStyle);
        return b;
    }

    /** Тонкий роздільник */
    private Separator divider() {
        Separator sep = new Separator();
        sep.setStyle("-fx-border-color: #1e3a5f; -fx-opacity: 0.6;");
        return sep;
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