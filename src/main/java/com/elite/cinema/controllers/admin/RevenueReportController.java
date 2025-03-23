package com.elite.cinema.controllers.admin;

import com.elite.cinema.controllers.MainController;
import com.elite.cinema.db.DailyRevenue;
import com.elite.cinema.db.DbSet;
import com.elite.cinema.models.tables.pojos.Movies;
import com.elite.cinema.utils.ComboBoxHelper;
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
import org.jooq.*;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.stream.Collectors;

import static com.elite.cinema.models.Tables.*;
import static org.jooq.impl.DSL.*;

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
    private ComboBox<Movies> movieFilterComboBox;
    @FXML
    private ComboBox<String> comparisonPeriodComboBox;

    @FXML
    private Label totalRevenueLabel;
    @FXML
    private Label totalTicketsLabel;
    @FXML
    private Label avgRevenuePerScreeningLabel;
    @FXML
    private Label popularMovieLabel;
    @FXML
    private Label statusLabel;

    @FXML
    private BarChart<String, Number> revenueChart;
    @FXML
    private PieChart movieRevenueChart;
    @FXML
    private BarChart<String, Number> comparisonChart;

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
    private TableColumn<ComparisonRecord, Long> currentPeriodColumn;
    @FXML
    private TableColumn<ComparisonRecord, Long> previousPeriodColumn;
    @FXML
    private TableColumn<ComparisonRecord, Long> differenceColumn;
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
        loadFilters();
        refreshData();
    }

    private void initializeControls()
    {
        // Initialize ComboBoxes with options
        reportTypeComboBox.setItems(FXCollections.observableArrayList(
                "Daily", "Weekly", "Monthly"
        ));
        reportTypeComboBox.setValue("Daily");

        comparisonPeriodComboBox.setItems(FXCollections.observableArrayList(
                "Previous Week", "Previous Month", "Previous Quarter", "Previous Year"
        ));
        comparisonPeriodComboBox.setValue("Previous Week");

        // Connect table data
        revenueTable.setItems(revenueData);
        comparisonTable.setItems(comparisonData);
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
        currentPeriodColumn.setCellValueFactory(cellData -> new SimpleLongProperty(cellData.getValue().currentPeriod()).asObject());
        previousPeriodColumn.setCellValueFactory(cellData -> new SimpleLongProperty(cellData.getValue().previousPeriod()).asObject());
        differenceColumn.setCellValueFactory(cellData -> new SimpleLongProperty(cellData.getValue().difference()).asObject());
        percentageChangeColumn.setCellValueFactory(cellData -> new SimpleDoubleProperty(cellData.getValue().percentageChange()).asObject());

        // Format currency cells
        currentPeriodColumn.setCellFactory(getMoneyTableCellFactory());
        previousPeriodColumn.setCellFactory(getMoneyTableCellFactory());
        differenceColumn.setCellFactory(getMoneyTableCellFactory());

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
                    } else if (item > 0)
                    {
                        setStyle("-fx-text-fill: green;");
                    } else
                    {
                        setStyle("-fx-text-fill: black;");
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

    private void loadFilters()
    {
        // Load movies for filter
        List<Movies> movies = DbSet.movies().findAll().stream().sorted((a, b) -> a.getTitle().compareToIgnoreCase(b.getTitle())).collect(Collectors.toList());

        movieFilterComboBox.setItems(FXCollections.observableArrayList(movies));
        movieFilterComboBox.setConverter(ComboBoxHelper.getStringConverter(Movies::getTitle));
    }

    private void setDefaultDates()
    {
        LocalDate endDate = LocalDate.now();
        LocalDate startDate = endDate.minusMonths(1);

        startDatePicker.setValue(startDate);
        endDatePicker.setValue(endDate);
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
            loadRevenueData();
            updateSummaryStats();
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
        var dailyRevenues = DbSet.revenues().dailyMovieRevenue(startDate, endDate);

        var stream = dailyRevenues.stream();

        // Apply filters if selected
        Movies selectedMovie = movieFilterComboBox.getValue();
        if (selectedMovie != null)
        {
            stream = stream.filter(rev -> rev.movieId().equals(selectedMovie.getId()));
        }

        revenueData.setAll(stream.collect(Collectors.toList()));
    }

    private void updateSummaryStats()
    {
        var revenues = DbSet.revenues().dailyMovieRevenue(startDatePicker.getValue(), endDatePicker.getValue());

        long totalRevenue = 0;
        long totalTickets = 0;

        for (var revenue: revenues)
        {
            totalRevenue += revenue.revenue();
            totalTickets += revenue.ticketsSold();
        }

        long averageRevenuePerScreening = totalRevenue / revenues.size();

        totalRevenueLabel.setText(PriceFormatter.format(totalRevenue));
        totalTicketsLabel.setText(String.valueOf(totalTickets));
        avgRevenuePerScreeningLabel.setText(PriceFormatter.format(averageRevenuePerScreening));

//        var popularMovie = context.select(MOVIES.TITLE)
//                .from(MOVIES)
//                .join(SCREENINGS).on(MOVIES.ID.eq(SCREENINGS.MOVIE_ID))
//                .join(RESERVATIONS).on(SCREENINGS.ID.eq(RESERVATIONS.SCREENING_ID))
//                .where(validReservation())
//                .orderBy(count(RESERVATIONS.reservedSeats()))
//                .limit(1)
//                .fetchAny();
//
//        popularMovieLabel.setText(popularMovie != null ? popularMovie.get(MOVIES.TITLE) : "N/A");
    }

    private void createCharts()
    {
        // Revenue trend chart
        XYChart.Series<String, Number> revenueSeries = new XYChart.Series<>();
        revenueSeries.setName("Revenue");

        Map<LocalDate, Long> revenueByDate = revenueData.stream()
                .collect(Collectors.groupingBy(
                        DailyRevenue::screeningDate,
                        Collectors.summingLong(DailyRevenue::revenue)
                ));

        revenueByDate.entrySet().stream()
                .sorted(Map.Entry.comparingByKey())
                .forEach(entry ->
                        revenueSeries.getData().add(new XYChart.Data<>(DateHelper.formatDate(entry.getKey()), entry.getValue()))
                );

        revenueChart.getData().clear();
        revenueChart.getData().add(revenueSeries);

        // Movie revenue pie chart
        ObservableList<PieChart.Data> movieData = FXCollections.observableArrayList();

        Map<String, Long> revenueByMovie = revenueData.stream()
                .collect(Collectors.groupingBy(
                        DailyRevenue::movieTitle,
                        Collectors.summingLong(DailyRevenue::revenue)
                ));

        revenueByMovie.forEach((movie, revenue) -> movieData.add(new PieChart.Data(movie, revenue)));
        movieRevenueChart.setData(movieData);
    }

    @FXML
    private void applyFilters()
    {
        generateReport();
    }

    @FXML
    private void resetFilters()
    {
        movieFilterComboBox.setValue(null);
        generateReport();
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
        LocalDate currentStart = startDatePicker.getValue();
        LocalDate currentEnd = endDatePicker.getValue();

        LocalDate previousStart, previousEnd;
        long daysDifference = ChronoUnit.DAYS.between(currentStart, currentEnd) + 1;

        previousEnd = switch (periodType)
        {
            case "Previous Week" ->
            {
                previousStart = currentStart.minusWeeks(1);
                yield currentEnd.minusWeeks(1);
            }
            case "Previous Month" ->
            {
                previousStart = currentStart.minusMonths(1);
                yield currentEnd.minusMonths(1);
            }
            case "Previous Quarter" ->
            {
                previousStart = currentStart.minusMonths(3);
                yield currentEnd.minusMonths(3);
            }
            case "Previous Year" ->
            {
                previousStart = currentStart.minusYears(1);
                yield currentEnd.minusYears(1);
            }
            default ->
            {
                previousStart = currentStart.minusDays(daysDifference);
                yield currentStart.minusDays(1);
            }
        };

        loadComparisonData(currentStart, currentEnd, previousStart, previousEnd);
        createComparisonChart();
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
        comparisonData.add(createComparisonRecord("Total Revenue", currentStats.totalRevenue(), previousStats.totalRevenue()));
        comparisonData.add(createComparisonRecord("Tickets Sold", currentStats.ticketsSold(), previousStats.ticketsSold()));
        comparisonData.add(createComparisonRecord("Avg Ticket Price", currentStats.avgTicketPrice(), previousStats.avgTicketPrice()));
        comparisonData.add(createComparisonRecord("Screenings Count", currentStats.screeningsCount(), previousStats.screeningsCount()));
        comparisonData.add(createComparisonRecord("Avg Revenue per Screening", currentStats.avgRevenuePerScreening(), previousStats.avgRevenuePerScreening()));
    }

    private ComparisonRecord createComparisonRecord(String category, long currentValue, long previousValue)
    {
        long difference = currentValue - previousValue;
        double percentChange = previousValue == 0 ? 0 : ((double)difference / previousValue) * 100;

        return new ComparisonRecord(category, currentValue, previousValue, difference, percentChange);
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

    private void createComparisonChart()
    {
        XYChart.Series<String, Number> currentSeries = new XYChart.Series<>();
        currentSeries.setName("Current Period");

        XYChart.Series<String, Number> previousSeries = new XYChart.Series<>();
        previousSeries.setName("Previous Period");

        for (ComparisonRecord record : comparisonData)
        {
            currentSeries.getData().add(new XYChart.Data<>(record.category(), record.currentPeriod()));
            previousSeries.getData().add(new XYChart.Data<>(record.category(), record.previousPeriod()));
        }

        var series = new ArrayList<XYChart.Series<String, Number>>();
        series.add(currentSeries);
        series.add(previousSeries);

        comparisonChart.getData().setAll(series);
    }

    @FXML
    private void refreshData()
    {
        loadFilters();
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

    public record ComparisonRecord(String category, long currentPeriod, long previousPeriod, long difference, double percentageChange) {}
}