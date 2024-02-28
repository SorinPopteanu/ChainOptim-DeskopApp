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

    // Core
    // - Main
    opens org.chainoptim.desktop.core.main.controller to com.google.guice, javafx.fxml;
    opens org.chainoptim.desktop.core.main.service to com.google.guice, javafx.fxml;

    // - User
    opens org.chainoptim.desktop.core.user.controller to javafx.fxml, com.google.guice;
    opens org.chainoptim.desktop.core.user.repository to com.google.guice;
    opens org.chainoptim.desktop.core.user.model to com.fasterxml.jackson.databind;

    // - Organization
    opens org.chainoptim.desktop.core.organization.controller to javafx.fxml, com.google.guice;
    opens org.chainoptim.desktop.core.organization.repository to com.google.guice;
    opens org.chainoptim.desktop.core.organization.model to com.fasterxml.jackson.databind;

    // Features
    // - Product
    opens org.chainoptim.desktop.features.product.controller to javafx.fxml, com.google.guice;
    opens org.chainoptim.desktop.features.product.repository to com.google.guice;
    opens org.chainoptim.desktop.features.product.model to com.fasterxml.jackson.databind;

    // - Product pipeline
    opens org.chainoptim.desktop.features.productpipeline.model to com.fasterxml.jackson.databind;

    // - Factory
    opens org.chainoptim.desktop.features.factory.controller to javafx.fxml, com.google.guice;
    opens org.chainoptim.desktop.features.factory.repository to com.google.guice;
    opens org.chainoptim.desktop.features.factory.model to com.fasterxml.jackson.databind;

    // - Warehouse
    opens org.chainoptim.desktop.features.warehouse.controller to javafx.fxml, com.google.guice;
    opens org.chainoptim.desktop.features.warehouse.repository to com.google.guice;
    opens org.chainoptim.desktop.features.warehouse.model to com.fasterxml.jackson.databind;

    // - Supplier
    opens org.chainoptim.desktop.features.supplier.controller to javafx.fxml, com.google.guice;
    opens org.chainoptim.desktop.features.supplier.repository to com.google.guice;
    opens org.chainoptim.desktop.features.supplier.model to com.fasterxml.jackson.databind;

    // Shared
    // - Location
    opens org.chainoptim.desktop.shared.features.location.model to com.fasterxml.jackson.databind;

    // -  Fallback Manager
    opens org.chainoptim.desktop.shared.fallback to javafx.fxml, com.google.guice;

    exports org.chainoptim.desktop;
}