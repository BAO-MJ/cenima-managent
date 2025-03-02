module com.elite.cinema {
    requires javafx.controls;
    requires javafx.fxml;
    requires com.jfoenix;
    opens com.elite.cinema to javafx.fxml;
    exports com.elite.cinema;
    requires java.sql;
}
