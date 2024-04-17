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
    requires org.java_websocket;

    opens org.chainoptim.desktop to javafx.fxml, gs.ui.javafx, gs.core, javafx.graphics;

    // Core
    // - Main
    opens org.chainoptim.desktop.core.main.controller to com.google.guice, javafx.fxml;
    opens org.chainoptim.desktop.core.main.service to com.google.guice, javafx.fxml;

    // - Context
    opens org.chainoptim.desktop.core.context to com.google.guice;

    // - Abstraction
    opens org.chainoptim.desktop.core.abstraction to com.google.guice, javafx.fxml;

    // - User
    opens org.chainoptim.desktop.core.user.controller to javafx.fxml, com.google.guice;
    opens org.chainoptim.desktop.core.user.service to com.google.guice;
    opens org.chainoptim.desktop.core.user.model to com.fasterxml.jackson.databind;
    opens org.chainoptim.desktop.core.user.dto to com.fasterxml.jackson.databind;

    // - Organization
    opens org.chainoptim.desktop.core.organization.controller to com.fasterxml.jackson.databind, com.google.guice, javafx.fxml;
    opens org.chainoptim.desktop.core.organization.service to com.google.guice;
    opens org.chainoptim.desktop.core.organization.dto to com.fasterxml.jackson.databind;
    opens org.chainoptim.desktop.core.organization.model to com.fasterxml.jackson.databind, com.google.guice;

    // - Overview
    opens org.chainoptim.desktop.core.overview.controller to com.google.guice, javafx.fxml;
    opens org.chainoptim.desktop.core.overview.model to com.fasterxml.jackson.databind;
    opens org.chainoptim.desktop.core.overview.service to com.google.guice;

    // - Notifications
    opens org.chainoptim.desktop.core.notification.controller to com.google.guice, javafx.fxml;
    opens org.chainoptim.desktop.core.notification.model to com.fasterxml.jackson.databind;
    opens org.chainoptim.desktop.core.notification.service to com.google.guice;

    // - Settings
    opens org.chainoptim.desktop.core.settings.controller to com.google.guice, javafx.fxml;
    opens org.chainoptim.desktop.core.settings.service to com.google.guice;
    opens org.chainoptim.desktop.core.settings.model to com.fasterxml.jackson.databind;
    opens org.chainoptim.desktop.core.settings.dto to com.fasterxml.jackson.databind;

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
    opens org.chainoptim.desktop.features.scanalysis.resourceallocation.dto to com.fasterxml.jackson.databind;
    opens org.chainoptim.desktop.features.scanalysis.factorygraph.model to com.fasterxml.jackson.databind, java.base;
    opens org.chainoptim.desktop.features.scanalysis.factorygraph.service to com.google.guice, javafx.web;
    opens org.chainoptim.desktop.features.scanalysis.productionhistory.model to com.fasterxml.jackson.databind, javafx.base;
    opens org.chainoptim.desktop.features.scanalysis.productionhistory.service to com.google.guice;
    opens org.chainoptim.desktop.features.scanalysis.productionhistory.dto to com.fasterxml.jackson.databind;
    opens org.chainoptim.desktop.features.scanalysis.productionperformance.model to com.fasterxml.jackson.databind, javafx.base;
    opens org.chainoptim.desktop.features.scanalysis.productionperformance.service to com.google.guice;

    // -- Supplier
    opens org.chainoptim.desktop.features.scanalysis.supply.model to com.fasterxml.jackson.databind, javafx.base;
    opens org.chainoptim.desktop.features.scanalysis.supply.service to com.google.guice;

    // Shared
    // - Common UI elements
    opens org.chainoptim.desktop.shared.common.uielements.select to javafx.fxml, com.google.guice;
    opens org.chainoptim.desktop.shared.common.uielements.performance to javafx.fxml, com.google.guice;
    opens org.chainoptim.desktop.shared.common.uielements.info to javafx.fxml, com.google.guice;
    opens org.chainoptim.desktop.shared.common.uielements.settings to javafx.fxml, com.google.guice;
    opens org.chainoptim.desktop.shared.common.uielements to com.google.guice;

    // - Enums
    opens org.chainoptim.desktop.shared.enums to com.fasterxml.jackson.databind;

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
    opens org.chainoptim.desktop.shared.confirmdialog.controller to com.fasterxml.jackson.databind, com.google.guice, javafx.fxml;

    // - Utils
    opens org.chainoptim.desktop.shared.util.resourceloader to com.google.guice;

    // - Search
    opens org.chainoptim.desktop.shared.search.model to com.fasterxml.jackson.databind, com.google.guice;
    opens org.chainoptim.desktop.shared.search.controller to com.google.guice, javafx.fxml;

    // - Caching
    opens org.chainoptim.desktop.shared.caching to com.google.guice;

    exports org.chainoptim.desktop;
}