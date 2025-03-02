module com.elite.cinema {
    requires javafx.controls;
    requires javafx.fxml;
    requires com.jfoenix;
    requires java.sql;
    requires org.jooq;
    requires transitive com.zaxxer.hikari;
    requires transitive javafx.graphics;
    opens com.elite.cinema to javafx.fxml;
    exports com.elite.cinema;
}
