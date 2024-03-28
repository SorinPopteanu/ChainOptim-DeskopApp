module org.chainoptim.desktop {
    // JavaFX
    requires javafx.controls;
    requires javafx.fxml;
    requires javafx.web;

    // Java utils
    requires java.prefs;
    requires org.json;
    requires java.base;
    requires java.desktop;

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
    requires gs.core;
    requires gs.ui.javafx;
    requires org.apache.commons.text;
    requires jdk.jsobject;

    opens org.chainoptim.desktop to javafx.fxml, gs.ui.javafx, gs.core;

    // Core
    // - Main
    opens org.chainoptim.desktop.core.main.controller to com.google.guice, javafx.fxml;
    opens org.chainoptim.desktop.core.main.service to com.google.guice, javafx.fxml;

    // - Abstraction
    opens org.chainoptim.desktop.core.abstraction to com.google.guice, javafx.fxml;

    // - User
    opens org.chainoptim.desktop.core.user.controller to javafx.fxml, com.google.guice;
    opens org.chainoptim.desktop.core.user.service to com.google.guice;
    opens org.chainoptim.desktop.core.user.model to com.fasterxml.jackson.databind;
    opens org.chainoptim.desktop.core.user.dto to com.fasterxml.jackson.databind;

    // - Organization
    opens org.chainoptim.desktop.core.organization.service to com.google.guice;
    opens org.chainoptim.desktop.core.organization.model to com.fasterxml.jackson.databind;
    opens org.chainoptim.desktop.core.organization.dto to com.fasterxml.jackson.databind;

    // Features
    // - Product
    opens org.chainoptim.desktop.features.product.controller to javafx.fxml, com.google.guice;
    opens org.chainoptim.desktop.features.product.service to com.google.guice;
    opens org.chainoptim.desktop.features.product.model to com.fasterxml.jackson.databind;
    opens org.chainoptim.desktop.features.product.dto to com.fasterxml.jackson.databind;
    opens org.chainoptim.desktop.features.product.controller.productproduction to com.google.guice, javafx.fxml;

    // - Product pipeline
    opens org.chainoptim.desktop.features.productpipeline.model to com.fasterxml.jackson.databind;
    opens org.chainoptim.desktop.features.productpipeline.service to com.google.guice;
    opens org.chainoptim.desktop.features.productpipeline.dto to com.fasterxml.jackson.databind;
    opens org.chainoptim.desktop.features.productpipeline.controller to com.google.guice, javafx.fxml;

    // - Factory
    opens org.chainoptim.desktop.features.factory.controller to javafx.fxml, com.google.guice;
    opens org.chainoptim.desktop.features.factory.service to com.google.guice;
    opens org.chainoptim.desktop.features.factory.model to com.fasterxml.jackson.databind;
    opens org.chainoptim.desktop.features.factory.dto to com.fasterxml.jackson.databind;
    opens org.chainoptim.desktop.features.factory.controller.factoryproduction to com.google.guice, javafx.fxml;

    // - Warehouse
    opens org.chainoptim.desktop.features.warehouse.controller to javafx.fxml, com.google.guice;
    opens org.chainoptim.desktop.features.warehouse.service to com.google.guice;
    opens org.chainoptim.desktop.features.warehouse.model to com.fasterxml.jackson.databind;
    opens org.chainoptim.desktop.features.warehouse.dto to com.fasterxml.jackson.databind;

    // - Supplier
    opens org.chainoptim.desktop.features.supplier.controller to javafx.fxml, com.google.guice;
    opens org.chainoptim.desktop.features.supplier.service to com.google.guice;
    opens org.chainoptim.desktop.features.supplier.model to com.fasterxml.jackson.databind, javafx.base;
    opens org.chainoptim.desktop.features.supplier.dto to com.fasterxml.jackson.databind;

    // - Client
    opens org.chainoptim.desktop.features.client.controller to javafx.fxml, com.google.guice;
    opens org.chainoptim.desktop.features.client.service to com.google.guice;
    opens org.chainoptim.desktop.features.client.model to com.fasterxml.jackson.databind, javafx.base;
    opens org.chainoptim.desktop.features.client.dto to com.fasterxml.jackson.databind;

    // - SC Analysis
    // -- Product
    opens org.chainoptim.desktop.features.scanalysis.productgraph.model to com.fasterxml.jackson.databind, java.base;
    opens org.chainoptim.desktop.features.scanalysis.productgraph.service to com.google.guice, javafx.web;

    // -- Factory
    opens org.chainoptim.desktop.features.scanalysis.resourceallocation.service to com.google.guice;
    opens org.chainoptim.desktop.features.scanalysis.resourceallocation.model to com.fasterxml.jackson.databind, javafx.base;
    opens org.chainoptim.desktop.features.scanalysis.factorygraph.model to com.fasterxml.jackson.databind, java.base;
    opens org.chainoptim.desktop.features.scanalysis.factorygraph.service to com.google.guice, javafx.web;

    // - Settings
    opens org.chainoptim.desktop.features.settings.controller to javafx.fxml, com.google.guice;

    // Shared
    // - Common UI elements
    opens org.chainoptim.desktop.shared.common.uielements to javafx.fxml, com.google.guice;

    // - Table
    opens org.chainoptim.desktop.shared.table to javafx.fxml, com.google.guice;

    // - Location
    opens org.chainoptim.desktop.shared.features.location.model to com.fasterxml.jackson.databind;
    opens org.chainoptim.desktop.shared.features.location.dto to com.fasterxml.jackson.databind;
    opens org.chainoptim.desktop.shared.features.location.service to com.google.guice;

    // -  Fallback Manager
    opens org.chainoptim.desktop.shared.fallback to javafx.fxml, com.google.guice;

    // - Confirm Dialogs
    opens org.chainoptim.desktop.shared.confirmdialog.model to com.fasterxml.jackson.databind;

    // - Utils
    opens org.chainoptim.desktop.shared.util.resourceloader to com.google.guice;

    // - Search
    opens org.chainoptim.desktop.shared.search.model to com.fasterxml.jackson.databind, com.google.guice;
    opens org.chainoptim.desktop.shared.search.controller to com.google.guice, javafx.fxml;

    exports org.chainoptim.desktop;
    opens org.chainoptim.desktop.core.organization.controller to com.fasterxml.jackson.databind, com.google.guice, javafx.fxml;
    opens org.chainoptim.desktop.shared.confirmdialog.controller to com.fasterxml.jackson.databind, com.google.guice, javafx.fxml;

}