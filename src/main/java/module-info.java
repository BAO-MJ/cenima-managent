module com.elite.cinema {
    requires javafx.controls;
    requires javafx.fxml;
    requires com.jfoenix;
    requires java.sql;
    requires org.jooq;

    requires transitive com.zaxxer.hikari;
    requires transitive javafx.graphics;
    requires MaterialFX;
    requires atlantafx.base;
    requires com.calendarfx.view;
    requires static lombok;
    requires org.apache.commons.lang3;
    requires javafx.base;
    requires org.kordamp.ikonli.core;
    requires org.kordamp.ikonli.materialdesign2;
    requires org.kordamp.ikonli.fontawesome5;
    requires org.kordamp.ikonli.javafx;
    requires org.threeten.extra;
    requires jdk.compiler;

    opens com.elite.cinema.models.tables.pojos to org.jooq, javafx.base;
    opens com.elite.cinema.models.tables.records to org.jooq;
    opens com.elite.cinema.controllers to javafx.fxml;
    opens com.elite.cinema.schedule to org.jooq;

    exports com.elite.cinema;
    opens com.elite.cinema.ui to javafx.fxml;
}