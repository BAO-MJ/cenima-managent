package com.elite.cinema.controllers.admin;

import com.elite.cinema.App;
import com.elite.cinema.controllers.MainController;
import com.elite.cinema.db.DbSet;
import com.elite.cinema.models.enums.UsersType;
import com.elite.cinema.models.tables.pojos.Users;
import com.elite.cinema.utils.DateHelper;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.HBox;
import javafx.util.Callback;
import org.jooq.types.ULong;

import java.time.format.DateTimeFormatter;

public class MembersController extends MainController {

    // Staff controls
    @FXML private TextField staffSearchField;
    @FXML private TableView<Users> staffTable;
    @FXML private TableColumn<Users, Integer> staffIdColumn;
    @FXML private TableColumn<Users, String> staffNameColumn;
    @FXML private TableColumn<Users, String> staffEmailColumn;
    @FXML private TableColumn<Users, String> staffPasswordColumn;
    @FXML private TableColumn<Users, String> staffPhoneColumn;
    @FXML private TableColumn<Users, String> staffDobColumn;
    @FXML private TableColumn<Users, Void> staffActionsColumn;

    @FXML private TextField staffNameField;
    @FXML private TextField staffEmailField;
    @FXML private TextField staffPasswordField;
    @FXML private TextField staffPhoneField;
    @FXML private DatePicker staffDobPicker;

    // Customer controls
    @FXML private TextField customerSearchField;
    @FXML private TableView<Users> customerTable;
    @FXML private TableColumn<Users, String> customerIdColumn;
    @FXML private TableColumn<Users, String> customerNameColumn;
    @FXML private TableColumn<Users, String> customerPhoneColumn;
    @FXML private TableColumn<Users, String> customerDobColumn;
    @FXML private TableColumn<Users, Void> customerActionsColumn;

    @FXML private TextField customerNameField;
    @FXML private TextField customerPhoneField;
    @FXML private DatePicker customerDobPicker;

    @FXML private TabPane memberTab;
    @FXML private Tab staffTab, customerTab;

    @FXML private TitledPane staffForm, customerForm;

    private final ObservableList<Users> staffList = FXCollections.observableArrayList();
    private final ObservableList<Users> customerList = FXCollections.observableArrayList();
    private static final String EMAIL_REGEX = "^[A-Za-z0-9+_.-]+@(.+)$";
    private static final String PHONE_REGEX = "^\\d{10}$";

    private Users currentStaff;
    private Users currentCustomer;

    @Override
    public void ready(Object params) {
        staffTab.setDisable(App.user.getType() != UsersType.Admin);
        if (App.user.getType() != UsersType.Admin) {
            memberTab.getSelectionModel().selectLast();
        }

        setupStaffTable();
        setupCustomerTable();
        loadStaffData();
        loadCustomerData();
    }

    private void setupStaffTable() {
        staffIdColumn.setCellValueFactory(new PropertyValueFactory<>("id"));
        staffNameColumn.setCellValueFactory(new PropertyValueFactory<>("name"));
        staffEmailColumn.setCellValueFactory(new PropertyValueFactory<>("email"));
        staffPasswordColumn.setCellValueFactory(new PropertyValueFactory<>("password"));
        staffPhoneColumn.setCellValueFactory(new PropertyValueFactory<>("phoneNumber"));
        staffDobColumn.setCellValueFactory(cell -> {
            if (cell.getValue().getDob() != null) {
                return new SimpleStringProperty(DateHelper.formatDate(cell.getValue().getDob()));
            }
            return new SimpleStringProperty("");
        });

        setupActionColumn(staffActionsColumn, true);
        staffTable.setItems(staffList);
    }

    private void setupCustomerTable() {
        customerIdColumn.setCellValueFactory(new PropertyValueFactory<>("id"));
        customerNameColumn.setCellValueFactory(new PropertyValueFactory<>("name"));
        customerPhoneColumn.setCellValueFactory(new PropertyValueFactory<>("phoneNumber"));
        customerDobColumn.setCellValueFactory(cell -> {
            if (cell.getValue().getDob() != null) {
                return new SimpleStringProperty(DateHelper.formatDate(cell.getValue().getDob()));
            }
            return new SimpleStringProperty("");
        });

        setupActionColumn(customerActionsColumn, false);
        customerTable.setItems(customerList);
    }

    private void setupActionColumn(TableColumn<Users, Void> column, boolean isStaff) {
        Callback<TableColumn<Users, Void>, TableCell<Users, Void>> cellFactory = new Callback<>() {
            @Override
            public TableCell<Users, Void> call(TableColumn<Users, Void> param) {
                return new TableCell<>() {
                    private final Button editBtn = new Button("Edit");
                    private final Button deleteBtn = new Button("Delete");
                    private final HBox pane = new HBox(5, editBtn, deleteBtn);

                    {
                        editBtn.setOnAction(_ -> {
                            Users user = getTableView().getItems().get(getIndex());
                            if (isStaff) {
                                populateStaffForm(user);
                            } else {
                                populateCustomerForm(user);
                            }
                        });

                        deleteBtn.setOnAction(_ -> {
                            Users user = getTableView().getItems().get(getIndex());
                            deleteUser(user.getId());
                            if (isStaff) {
                                loadStaffData();
                            } else {
                                loadCustomerData();
                            }
                        });
                    }

                    @Override
                    protected void updateItem(Void item, boolean empty) {
                        super.updateItem(item, empty);
                        setGraphic(empty ? null : pane);
                    }
                };
            }
        };

        column.setCellFactory(cellFactory);
    }

    private void loadStaffData() {
        staffList.setAll(DbSet.users().fetchByType(UsersType.Staff));
        staffTable.setItems(staffList);
    }

    private void loadCustomerData() {
        customerList.setAll(DbSet.users().fetchByType(UsersType.Customer));
        customerTable.setItems(customerList);
    }

    private void populateStaffForm(Users user) {
        currentStaff = user;
        staffNameField.setText(user.getName());
        staffEmailField.setText(user.getEmail());
        staffPasswordField.setText(user.getPassword());
        staffPhoneField.setText(user.getPhoneNumber());
        staffDobPicker.setValue(user.getDob());
        staffForm.setExpanded(true);
    }

    private void populateCustomerForm(Users user) {
        currentCustomer = user;
        customerNameField.setText(user.getName());
        customerPhoneField.setText(user.getPhoneNumber());
        customerDobPicker.setValue(user.getDob());
        customerForm.setExpanded(true);
    }

    @FXML
    private void onStaffSearch() {
        String query = staffSearchField.getText().toLowerCase();
        if (query.isEmpty()) {
            loadStaffData();
            return;
        }

        staffTable.setItems(staffList.filtered(user -> user.getName().toLowerCase().contains(query) ||
                user.getEmail().toLowerCase().contains(query) ||
                user.getPhoneNumber().contains(query))
        );
    }

    @FXML
    private void onCustomerSearch() {
        String query = customerSearchField.getText().toLowerCase();
        if (query.isEmpty()) {
            loadCustomerData();
            return;
        }

        customerTable.setItems(customerList.filtered(user -> user.getName().toLowerCase().contains(query) ||
                user.getPhoneNumber().contains(query))
        );
    }

    @FXML
    private void onAddStaffClicked() {
        clearStaffForm();
        staffForm.setExpanded(true);
    }

    @FXML
    private void onAddCustomerClicked() {
        clearCustomerForm();
        customerForm.setExpanded(true);
    }

    @FXML
    private void onSaveStaff() {
        if (staffNameField.getText().isEmpty() || staffEmailField.getText().isEmpty() || staffPasswordField.getText().isEmpty() ||
                staffPhoneField.getText().isEmpty()) {
            showAlert("Error", "All fields are required", Alert.AlertType.ERROR);
            return;
        }

        if (!staffEmailField.getText().matches(EMAIL_REGEX)) {
            showAlert("Error", "Invalid email format", Alert.AlertType.ERROR);
            return;
        }

        if (staffPasswordField.getText().length() < 8 || staffPasswordField.getText().length() > 50) {
            showAlert("Error", "Password must be between 8 to 50 characters long", Alert.AlertType.ERROR);
            return;
        }

        if (!staffPhoneField.getText().matches(PHONE_REGEX)) {
            showAlert("Error", "Phone must be 10 digits", Alert.AlertType.ERROR);
            return;
        }

        currentStaff.setName(staffNameField.getText());
        currentStaff.setEmail(staffEmailField.getText());
        currentStaff.setPassword(staffPasswordField.getText());
        currentStaff.setPhoneNumber(staffPhoneField.getText());
        currentStaff.setDob(staffDobPicker.getValue());
        DbSet.users().merge(currentStaff);

        showAlert("Success", "Staff updated successfully", Alert.AlertType.INFORMATION);

        clearStaffForm();
        loadStaffData();
    }

    @FXML
    private void onSaveCustomer() {
        if (customerNameField.getText().isEmpty() || customerPhoneField.getText().isEmpty()) {
            showAlert("Error", "All fields are required", Alert.AlertType.ERROR);
            return;
        }

        if (!customerPhoneField.getText().matches(PHONE_REGEX)) {
            showAlert("Error", "Phone must be 10 digits", Alert.AlertType.ERROR);
            return;
        }

        currentCustomer.setName(customerNameField.getText());
        currentCustomer.setPhoneNumber(customerPhoneField.getText());
        currentCustomer.setDob(customerDobPicker.getValue());

        DbSet.users().merge(currentCustomer);
        showAlert("Success", "Customer updated successfully", Alert.AlertType.INFORMATION);

        clearCustomerForm();
        loadCustomerData();
    }

    @FXML
    private void onCancelStaff() {
        clearStaffForm();
    }

    @FXML
    private void onCancelCustomer() {
        clearCustomerForm();
    }

    private void clearStaffForm() {
        staffNameField.clear();
        staffEmailField.clear();
        staffPasswordField.clear();
        staffPhoneField.clear();
        staffDobPicker.setValue(null);

        currentStaff = new Users();
        currentStaff.setType(UsersType.Staff);
    }

    private void clearCustomerForm() {
        customerNameField.clear();
        customerPhoneField.clear();
        customerDobPicker.setValue(null);

        currentCustomer = new Users();
        currentCustomer.setType(UsersType.Customer);
    }

    private void deleteUser(ULong userId) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Confirm Delete");
        alert.setHeaderText(null);
        alert.setContentText("Are you sure you want to delete this user?");

        alert.showAndWait().ifPresent(result -> {
            if (result == ButtonType.OK) {
                DbSet.users().deleteById(userId);
                showAlert("Success", "User deleted successfully", Alert.AlertType.INFORMATION);
            }
        });
    }

    private void showAlert(String title, String message, Alert.AlertType alertType) {
        Alert alert = new Alert(alertType);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}