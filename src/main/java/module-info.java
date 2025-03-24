module com.elite.cinema {
    requires javafx.controls;
    requires javafx.fxml;
    requires java.sql;
    requires org.jooq;

    requires transitive com.zaxxer.hikari;
    requires transitive javafx.graphics;
    requires MaterialFX;
    requires atlantafx.base;
    requires static lombok;
    requires org.apache.commons.lang3;
    requires javafx.base;
    requires org.kordamp.ikonli.core;
    requires org.kordamp.ikonli.materialdesign2;
    requires org.kordamp.ikonli.fontawesome5;
    requires org.kordamp.ikonli.javafx;
    requires jdk.compiler;
    requires javafx.swing;

    opens com.elite.cinema.models.tables.pojos to org.jooq, javafx.base;
    opens com.elite.cinema.models.tables.records to org.jooq;
    opens com.elite.cinema.controllers to javafx.fxml, javafx.base;
    opens com.elite.cinema.schedule to org.jooq;
    opens com.elite.cinema.db to org.jooq;

    exports com.elite.cinema;
    exports com.elite.cinema.models.tables.pojos;
    opens com.elite.cinema.ui to javafx.fxml;
    opens com.elite.cinema.controllers.user to javafx.fxml, javafx.base;
    opens com.elite.cinema.controllers.admin to javafx.fxml, javafx.base;
    opens com.elite.cinema.utils to javafx.fxml, net.sf.jasperreports.core;
    requires net.sf.jasperreports.core;
    requires net.sf.jasperreports.barcode4j;
    requires ecj;
}