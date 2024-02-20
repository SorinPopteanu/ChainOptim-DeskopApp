module org.chainoptim.desktop {
    // JavaFX
    requires javafx.controls;
    requires javafx.fxml;
    requires javafx.web;

    // Java utils
    requires java.prefs;
    requires org.json;

    // Http
    requires java.net.http;

    // Other libraries
    requires org.controlsfx.controls;
    requires com.dlsc.formsfx;
    requires net.synedra.validatorfx;
    requires org.kordamp.ikonli.javafx;
    requires org.kordamp.bootstrapfx.core;
    requires eu.hansolo.tilesfx;
    requires com.almasb.fxgl.all;
    requires static lombok;
    requires com.fasterxml.jackson.databind;
    requires com.fasterxml.jackson.datatype.jsr310;
    requires com.google.guice;

    opens org.chainoptim.desktop to javafx.fxml;
    opens org.chainoptim.desktop.core.user.controller to javafx.fxml, com.google.guice;
    opens org.chainoptim.desktop.core.user.view to javafx.fxml;
    opens org.chainoptim.desktop.core.user.repository to com.google.guice;
    opens org.chainoptim.desktop.core.user.model to com.fasterxml.jackson.databind;
    opens org.chainoptim.desktop.core.organization.model to com.fasterxml.jackson.databind;
    exports org.chainoptim.desktop;

}