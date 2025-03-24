package com.elite.cinema.controllers.admin;

import com.elite.cinema.controllers.MainController;
import com.elite.cinema.db.DailyRevenue;
import com.elite.cinema.db.DbSet;
import com.elite.cinema.utils.DateHelper;
import com.elite.cinema.utils.PriceFormatter;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleLongProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.chart.BarChart;
import javafx.scene.chart.PieChart;
import javafx.scene.chart.XYChart;
import javafx.scene.control.*;
import javafx.stage.FileChooser;
import javafx.util.Callback;
import org.jooq.types.ULong;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

public class RevenueReportController extends MainController
{

    // FXML Controls
    @FXML
    private DatePicker startDatePicker;
    @FXML
    private DatePicker endDatePicker;
    @FXML
    private ComboBox<String> reportTypeComboBox;
    @FXML
    private ComboBox<String> comparisonPeriodComboBox;

    @FXML
    private Label totalRevenueLabel;
    @FXML
    private Label totalTicketsLabel;
    @FXML
    private Label avgRevenuePerScreeningLabel;
    @FXML
    private Label statusLabel;

    @FXML
    private BarChart<String, Number> revenueChart;
    @FXML
    private PieChart movieRevenueChart;

    @FXML
    private TableView<DailyRevenue> revenueTable;
    @FXML
    private TableColumn<DailyRevenue, String> dateColumn;
    @FXML
    private TableColumn<DailyRevenue, String> movieColumn;
    @FXML
    private TableColumn<DailyRevenue, Integer> ticketsColumn;
    @FXML
    private TableColumn<DailyRevenue, Long> revenueColumn;
    @FXML
    private TableColumn<DailyRevenue, Double> occupancyRateColumn;

    @FXML
    private TableView<ComparisonRecord> comparisonTable;
    @FXML
    private TableColumn<ComparisonRecord, String> categoryColumn;
    @FXML
    private TableColumn<ComparisonRecord, String> currentPeriodColumn;
    @FXML
    private TableColumn<ComparisonRecord, String> previousPeriodColumn;
    @FXML
    private TableColumn<ComparisonRecord, String> differenceColumn;
    @FXML
    private TableColumn<ComparisonRecord, Double> percentageChangeColumn;

    private final ObservableList<DailyRevenue> revenueData = FXCollections.observableArrayList();
    private final ObservableList<ComparisonRecord> comparisonData = FXCollections.observableArrayList();

    public void initialize()
    {
        initializeControls();
        setupTableColumns();
    }

    @Override
    public void ready(Object params)
    {
        setDefaultDates();
        refreshData();
    }

    private void initializeControls()
    {
        // Initialize ComboBoxes with options
        reportTypeComboBox.setItems(FXCollections.observableArrayList("Daily", "Monthly", "Quarterly", "Yearly"));
        reportTypeComboBox.setValue("Daily");

        comparisonPeriodComboBox.setItems(FXCollections.observableArrayList("Previous Week", "Previous Month", "Previous Quarter", "Previous Year"));
        comparisonPeriodComboBox.setValue("Previous Week");

        // Connect table data
        revenueTable.setItems(revenueData);
        comparisonTable.setItems(comparisonData);

        startDatePicker.setOnAction(_ -> {
            if (startDatePicker.getValue() != null) {
                if (startDatePicker.getValue().isAfter(endDatePicker.getValue())) {
                    endDatePicker.setValue(startDatePicker.getValue());
                }
                else {
                    loadRevenueData();
                }
            }

            endDatePicker.setDayCellFactory(_ -> new DateCell() {
                @Override
                public void updateItem(LocalDate date, boolean empty) {
                    super.updateItem(date, empty);
                    setDisable(date.isBefore(startDatePicker.getValue()));
                }
            });

        });

        endDatePicker.setOnAction(_ -> loadRevenueData());
    }

    private void setupTableColumns()
    {
        // Revenue table columns
        dateColumn.setCellValueFactory(cellData -> new SimpleStringProperty(DateHelper.formatDate(cellData.getValue().screeningDate())));
        movieColumn.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().movieTitle()));
        ticketsColumn.setCellValueFactory(cellData -> new SimpleIntegerProperty(cellData.getValue().ticketsSold()).asObject());
        revenueColumn.setCellValueFactory(cellData -> new SimpleLongProperty(cellData.getValue().revenue()).asObject());
        occupancyRateColumn.setCellValueFactory(cellData -> new SimpleDoubleProperty(cellData.getValue().occupancyRate()).asObject());

        // Format currency in revenue column
        revenueColumn.setCellFactory(getMoneyTableCellFactory());

        // Format percentage in occupancy column
        occupancyRateColumn.setCellFactory(_ -> new TableCell<>()
        {
            @Override
            protected void updateItem(Double item, boolean empty)
            {
                super.updateItem(item, empty);
                if (empty || item == null)
                {
                    setText(null);
                } else
                {
                    setText(String.format("%.1f%%", item * 100));
                }
            }
        });

        // Comparison table columns
        categoryColumn.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().category()));
        currentPeriodColumn.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().currentPeriod()));
        previousPeriodColumn.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().previousPeriod()));
        differenceColumn.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().difference()));
        percentageChangeColumn.setCellValueFactory(cellData -> new SimpleDoubleProperty(cellData.getValue().percentageChange()).asObject());

        // Format percentage in change column
        percentageChangeColumn.setCellFactory(_ -> new TableCell<>()
        {
            @Override
            protected void updateItem(Double item, boolean empty)
            {
                super.updateItem(item, empty);
                if (empty || item == null)
                {
                    setText(null);
                } else
                {
                    setText(String.format("%.1f%%", item));
                    if (item < 0)
                    {
                        setStyle("-fx-text-fill: red;");
                    }
                    else if (item > 0)
                    {
                        setStyle("-fx-text-fill: green;");
                    }
                    else
                    {
                        setStyle("-fx-text-fill: white;");
                    }
                }
            }
        });
    }

    private <T> Callback<TableColumn<T, Long>, TableCell<T, Long>> getMoneyTableCellFactory()
    {
        return _ -> new TableCell<>()
        {
            @Override
            protected void updateItem(Long item, boolean empty)
            {
                super.updateItem(item, empty);
                if (empty || item == null)
                {
                    setText(null);
                } else
                {
                    setText(PriceFormatter.format(item));
                }
            }
        };
    }

    private void setDefaultDates()
    {
        LocalDate endDate = LocalDate.now();
        LocalDate startDate = endDate.minusMonths(1);

        startDatePicker.setValue(startDate);
        endDatePicker.setValue(endDate);
        loadRevenueData();
    }

    @FXML
    private void generateReport()
    {
        if (startDatePicker.getValue() == null || endDatePicker.getValue() == null)
        {
            showAlert("Error", "Please select both start and end dates.", Alert.AlertType.ERROR);
            return;
        }

        if (endDatePicker.getValue().isBefore(startDatePicker.getValue()))
        {
            showAlert("Error", "End date cannot be before start date.", Alert.AlertType.ERROR);
            return;
        }

        statusLabel.setText("Generating report...");

        try
        {
            createCharts();
            statusLabel.setText("Report generated successfully");
        }
        catch (Exception e)
        {
            statusLabel.setText("Error generating report: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void loadRevenueData()
    {
        LocalDate startDate = startDatePicker.getValue();
        LocalDate endDate = endDatePicker.getValue();

        revenueData.setAll(DbSet.revenues().dailyMovieRevenue(startDate, endDate));
        updateSummaryStats(startDate, endDate);
    }

    private void updateSummaryStats(LocalDate startDate, LocalDate endDate)
    {
        var revenues = DbSet.revenues().dailyMovieRevenue(startDate, endDate);

        long totalRevenue = 0;
        long totalTickets = 0;

        for (var revenue: revenues)
        {
            totalRevenue += revenue.revenue();
            totalTickets += revenue.ticketsSold();
        }

        long averageRevenuePerScreening = revenues.isEmpty() ? 0 : totalRevenue / revenues.size();

        totalRevenueLabel.setText(PriceFormatter.format(totalRevenue));
        totalTicketsLabel.setText(String.valueOf(totalTickets));
        avgRevenuePerScreeningLabel.setText(PriceFormatter.format(averageRevenuePerScreening));
    }

    private void createCharts() {
        var revenues = DbSet.revenues().dailyMovieRevenue();
        // Get selected report type
        String reportType = reportTypeComboBox.getValue();

        // Revenue trend chart
        XYChart.Series<String, Number> revenueSeries = new XYChart.Series<>();
        revenueSeries.setName("Revenue");

        // Group data based on selected period type
        Map<String, Long> revenueByPeriod = new LinkedHashMap<>();

        for (var revenue : revenues) {
            String periodKey = switch (reportType) {
                case "Monthly" -> DateTimeFormatter.ofPattern("yyyy-MM").format(revenue.screeningDate());
                case "Quarterly" -> {
                    int quarter = (revenue.screeningDate().getMonthValue() - 1) / 3 + 1;
                    yield revenue.screeningDate().getYear() + " Q" + quarter;
                }
                case "Yearly" -> String.valueOf(revenue.screeningDate().getYear());
                default -> DateHelper.formatDate(revenue.screeningDate()); // Daily is default
            };

            revenueByPeriod.merge(periodKey, revenue.revenue(), Long::sum);
        }

        // Add data to chart series
        revenueByPeriod.forEach((key, value) -> revenueSeries.getData().add(new XYChart.Data<>(key, value)));

        revenueChart.getData().clear();
        revenueChart.getData().add(revenueSeries);

        // Movie revenue pie chart
        ObservableList<PieChart.Data> movieData = FXCollections.observableArrayList();

        Map<ULong, Long> revenueByMovie = revenueData.stream()
                .collect(Collectors.groupingBy(
                        DailyRevenue::movieId,
                        Collectors.summingLong(DailyRevenue::revenue)
                ));

        revenueByMovie.forEach((movie, revenue) -> movieData.add(
                new PieChart.Data(Objects.requireNonNull(DbSet.movies().findById(movie)).getTitle(), revenue)));
        movieRevenueChart.setData(movieData);
    }

    @FXML
    private void exportToCsv()
    {
        if (revenueData.isEmpty())
        {
            showAlert("Error", "No data to export", Alert.AlertType.ERROR);
            return;
        }

        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Save CSV File");
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("CSV Files", "*.csv"));
        fileChooser.setInitialFileName("revenue_report.csv");

        File file = fileChooser.showSaveDialog(revenueTable.getScene().getWindow());
        if (file != null)
        {
            try (PrintWriter writer = new PrintWriter(new FileWriter(file)))
            {
                // Write header
                writer.println("Date,Movie,Tickets Sold,Revenue,Occupancy Rate");

                // Write data
                for (DailyRevenue record : revenueData)
                {
                    writer.printf("%s,\"%s\",%d,%d,%.2f%%\n",
                            DateHelper.formatDate(record.screeningDate()),
                            record.movieTitle(),
                            record.ticketsSold(),
                            record.revenue(),
                            record.occupancyRate() * 100);
                }

                statusLabel.setText("Report exported to " + file.getName());
                showAlert("Success", "Report exported successfully", Alert.AlertType.INFORMATION);
            }
            catch (IOException e)
            {
                statusLabel.setText("Error exporting report: " + e.getMessage());
                showAlert("Error", "Failed to export report: " + e.getMessage(), Alert.AlertType.ERROR);
            }
        }
    }

    @FXML
    private void printReport()
    {
        showAlert("Information", "Print functionality not implemented", Alert.AlertType.INFORMATION);
    }

    @FXML
    private void compareWithPrevious()
    {
        if (startDatePicker.getValue() == null || endDatePicker.getValue() == null)
        {
            showAlert("Error", "Please select both start and end dates.", Alert.AlertType.ERROR);
            return;
        }

        String periodType = comparisonPeriodComboBox.getValue();

        LocalDate previousStart, previousEnd;
        LocalDate currentStart, currentEnd = LocalDate.now();

        switch (periodType)
        {
            case "Previous Week" ->
            {
                currentStart = currentEnd.minusDays(currentEnd.getDayOfWeek().getValue() - 2);
                previousStart = currentStart.minusWeeks(1);
                previousEnd = previousStart.plusDays(6);
            }
            case "Previous Month" ->
            {
                currentStart = currentEnd.minusDays(currentEnd.getDayOfMonth() - 1);
                previousStart = currentStart.minusMonths(1);
                previousEnd = previousStart.plusMonths(1).minusDays(1);
            }
            case "Previous Quarter" ->
            {
                currentStart = currentEnd.minusDays(currentEnd.getDayOfMonth() - 1);
                int currentQuarter = (currentStart.getMonthValue() - 1) / 3 + 1;
                previousStart = currentStart.minusMonths(3);
                previousEnd = previousStart.withMonth((currentQuarter - 1) * 3 + 3).minusDays(1);
            }
            case "Previous Year" ->
            {
                currentStart = currentEnd.minusDays(currentEnd.getDayOfYear() - 1);
                previousStart = currentStart.minusYears(1);
                previousEnd = previousStart.plusYears(1).minusDays(1);
            }
            default -> throw new IllegalStateException("Unexpected value: " + periodType);
        }

        loadComparisonData(currentStart, currentEnd, previousStart, previousEnd);
    }

    private void loadComparisonData(LocalDate currentStart, LocalDate currentEnd,
                                    LocalDate previousStart, LocalDate previousEnd)
    {
        // Load current period stats
        PeriodStats currentStats = loadPeriodStats(currentStart, currentEnd);

        // Load previous period stats
        PeriodStats previousStats = loadPeriodStats(previousStart, previousEnd);

        // Calculate comparisons
        comparisonData.clear();
        comparisonData.add(createComparisonMoneyRecord("Total Revenue", currentStats.totalRevenue(), previousStats.totalRevenue()));
        comparisonData.add(createComparisonRecord("Tickets Sold", currentStats.ticketsSold(), previousStats.ticketsSold()));
        comparisonData.add(createComparisonMoneyRecord("Avg Ticket Price", currentStats.avgTicketPrice(), previousStats.avgTicketPrice()));
        comparisonData.add(createComparisonRecord("Screenings Count", currentStats.screeningsCount(), previousStats.screeningsCount()));
        comparisonData.add(createComparisonMoneyRecord("Avg Revenue per Screening", currentStats.avgRevenuePerScreening(), previousStats.avgRevenuePerScreening()));
    }

    private ComparisonRecord createComparisonRecord(String category, long currentValue, long previousValue)
    {
        long difference = currentValue - previousValue;
        double percentChange = previousValue == 0 ? 0 : ((double)difference / previousValue) * 100;

        return new ComparisonRecord(category, String.valueOf(currentValue), String.valueOf(previousValue), String.valueOf(difference), percentChange);
    }

    private ComparisonRecord createComparisonMoneyRecord(String category, long currentValue, long previousValue)
    {
        long difference = currentValue - previousValue;
        double percentChange = previousValue == 0 ? 0 : ((double)difference / previousValue) * 100;

        return new ComparisonRecord(category, PriceFormatter.format(currentValue), PriceFormatter.format(previousValue), PriceFormatter.format(difference), percentChange);
    }

    private record PeriodStats(long totalRevenue, long ticketsSold, long avgTicketPrice, long screeningsCount, long avgRevenuePerScreening) {}

    private PeriodStats loadPeriodStats(LocalDate startDate, LocalDate endDate)
    {
        var revenues = DbSet.revenues().dailyMovieRevenue(startDate, endDate);

        long totalRevenue = 0;
        long totalTickets = 0;

        for (var revenue: revenues)
        {
            totalRevenue += revenue.revenue();
            totalTickets += revenue.ticketsSold();
        }

        // Avg Ticket Price
        long avgTicketPrice = 0L;
        if (totalTickets > 0)
        {
            avgTicketPrice = totalRevenue / totalTickets;
        }

        // Avg Revenue per Screening
        long avgRevenuePerScreening = 0;
        if (!revenues.isEmpty())
        {
            avgRevenuePerScreening = totalRevenue / revenues.size();
        }

        return new PeriodStats(totalRevenue, totalTickets, avgTicketPrice, revenues.size(), avgRevenuePerScreening);
    }

    @FXML
    private void refreshData()
    {
        generateReport();
    }

    private void showAlert(String title, String message, Alert.AlertType alertType)
    {
        Alert alert = new Alert(alertType);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    public record ComparisonRecord(String category, String currentPeriod, String previousPeriod, String difference, double percentageChange) {}
}