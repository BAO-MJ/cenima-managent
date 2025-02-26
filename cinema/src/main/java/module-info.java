module com.mycompany.cinema {
    requires javafx.controls;
    requires javafx.fxml;
    requires com.jfoenix;
    opens com.mycompany.cinema to javafx.fxml;
    exports com.mycompany.cinema;
    requires java.sql;
}
