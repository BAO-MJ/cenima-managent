package com.elite.cinema.controllers.admin;

import com.elite.cinema.controllers.MainController;
import com.elite.cinema.db.DbSet;
import com.elite.cinema.models.enums.RefreshmentsCategory;
import com.elite.cinema.models.tables.pojos.Refreshments;
import com.elite.cinema.utils.ComboBoxHelper;
import com.elite.cinema.utils.ImageHelper;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.stage.FileChooser;
import javafx.util.StringConverter;
import lombok.SneakyThrows;
import org.jooq.impl.DSL;
import org.jooq.types.UInteger;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;

import static com.elite.cinema.models.Tables.REFRESHMENTS;

public class RefreshmentsController extends MainController
{
    @FXML
    private TextField searchField, nameField;
    @FXML
    private ComboBox<RefreshmentsCategory> categoryComboBox, filterComboBox;
    @FXML
    private Spinner<Integer> priceSpinner;
    @FXML
    private TableView<Refreshments> refreshmentsTable;
    @FXML
    private TableColumn<Refreshments, String> nameColumn;
    @FXML
    private TableColumn<Refreshments, RefreshmentsCategory> categoryColumn;
    @FXML
    private TableColumn<Refreshments, Double> priceColumn;
    @FXML
    private Button saveButton, clearButton, deleteButton, refreshButton, addButton;
    @FXML
    private ImageView refreshmentImage;

    private ObservableList<Refreshments> refreshments;
    private Path currentImagePath;

    @FXML
    public void initialize() {
        setupControls();
        setupTableView();
        loadRefreshments();
        configureSearch();
    }

    private void setupControls() {
        // Setup category combo boxes
        categoryComboBox.setItems(FXCollections.observableArrayList(RefreshmentsCategory.values()));
        filterComboBox.setItems(FXCollections.observableArrayList(RefreshmentsCategory.values()));

        // Add "All" option to filter
        filterComboBox.getItems().addFirst(null);
        filterComboBox.setConverter(ComboBoxHelper.getStringConverter(RefreshmentsCategory::getLiteral, "All Categories"));

        // Setup spinners
        priceSpinner.setValueFactory(new SpinnerValueFactory.IntegerSpinnerValueFactory(1000, 100000, 1000, 1000));
    }

    private void setupTableView() {
        nameColumn.setCellValueFactory(new PropertyValueFactory<>("name"));
        categoryColumn.setCellValueFactory(new PropertyValueFactory<>("category"));
        priceColumn.setCellValueFactory(new PropertyValueFactory<>("price"));

        refreshments = FXCollections.observableArrayList();
        refreshmentsTable.setItems(refreshments);

        refreshmentsTable.getSelectionModel().selectedItemProperty().addListener((obs, oldSelection, newSelection) -> {
            if (newSelection != null) {
                populateForm(newSelection);
            }
        });
    }

    @FXML
    public void loadRefreshments() {
        var context = DbSet.getContext();
        var condition = DSL.noCondition();
        // Apply category filter if selected
        if (filterComboBox.getValue() != null) {
            condition = REFRESHMENTS.CATEGORY.eq(filterComboBox.getValue());
        }

        var refreshmentList = context.selectFrom(REFRESHMENTS).where(condition).fetchInto(Refreshments.class);
        refreshments.setAll(refreshmentList);
    }

    private void configureSearch() {
        searchField.textProperty().addListener((_, _, newValue) -> {
            if (newValue == null || newValue.isEmpty()) {
                loadRefreshments();
            } else {
                var filtered = refreshments.filtered(refreshment ->
                        refreshment.getName().toLowerCase().contains(newValue.toLowerCase()));
                refreshmentsTable.setItems(filtered);
            }
        });

        filterComboBox.valueProperty().addListener(_ -> loadRefreshments());
    }

    @FXML
    public void onChooseImage() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Select Refreshment Image");
        fileChooser.getExtensionFilters().addAll(
                new FileChooser.ExtensionFilter("Image Files", "*.png", "*.jpg", "*.jpeg")
        );

        File selectedFile = fileChooser.showOpenDialog(refreshmentImage.getScene().getWindow());
        if (selectedFile != null) {
            try {
                Image image = new Image(selectedFile.toURI().toString());
                refreshmentImage.setImage(image);
                currentImagePath = selectedFile.toPath();
            } catch (Exception e) {
                showAlert("Image Error", "Failed to load image", AlertType.ERROR);
            }
        }
    }

    @FXML
    @SneakyThrows
    public void saveRefreshment() {
        if (!validateForm()) {
            return;
        }

        var refreshment = refreshmentsTable.getSelectionModel().getSelectedItem();

        if (refreshment == null) {
            refreshment = new Refreshments();
        }

        refreshment.setName(nameField.getText());
        refreshment.setCategory(categoryComboBox.getValue());
        refreshment.setPrice(UInteger.valueOf(priceSpinner.getValue()));

        if (currentImagePath != null) {
            refreshment.setImage(Files.readAllBytes(currentImagePath));
        }

        DbSet.refreshments().merge(refreshment);

        loadRefreshments();
        clearForm();
        showAlert("Success", "Refreshment saved successfully", AlertType.INFORMATION);
    }

    @FXML
    public void deleteRefreshment() {
        Refreshments selectedRefreshment = refreshmentsTable.getSelectionModel().getSelectedItem();
        if (selectedRefreshment == null) {
            showAlert("Error", "Please select a refreshment to delete", AlertType.ERROR);
            return;
        }

        Alert confirmDelete = new Alert(Alert.AlertType.CONFIRMATION);
        confirmDelete.setTitle("Confirm Delete");
        confirmDelete.setHeaderText(null);
        confirmDelete.setContentText("Are you sure you want to delete: " + selectedRefreshment.getName() + "?");

        if (confirmDelete.showAndWait().orElse(ButtonType.CANCEL) == ButtonType.OK) {
            DbSet.refreshments().delete(selectedRefreshment);
            loadRefreshments();
            clearForm();
        }
    }

    @FXML
    public void clearForm() {
        nameField.clear();
        categoryComboBox.getSelectionModel().clearSelection();
        priceSpinner.getValueFactory().setValue(0);
        refreshmentsTable.getSelectionModel().clearSelection();
        refreshmentImage.setImage(null);
        currentImagePath = null;
    }

    private void populateForm(Refreshments refreshment) {
        nameField.setText(refreshment.getName());
        categoryComboBox.setValue(refreshment.getCategory());
        priceSpinner.getValueFactory().setValue(refreshment.getPrice().intValue());

        if (refreshment.getImage() != null) {
            try {
                refreshmentImage.setImage(ImageHelper.byteArrayToImage(refreshment.getImage()));
            } catch (Exception e) {
                // If image loading fails, clear the image
                refreshmentImage.setImage(null);
            }
        } else {
            refreshmentImage.setImage(null);
        }
    }

    private boolean validateForm() {
        if (nameField.getText().isEmpty()) {
            showAlert("Validation Error", "Please enter a name", AlertType.ERROR);
            return false;
        }

        if (categoryComboBox.getValue() == null) {
            showAlert("Validation Error", "Please select a category", AlertType.ERROR);
            return false;
        }

        return true;
    }

    private void showAlert(String title, String message, AlertType alertType) {
        Alert alert = new Alert(alertType);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}