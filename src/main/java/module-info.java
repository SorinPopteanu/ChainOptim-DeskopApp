module org.chainoptim.desktop {
    requires javafx.controls;
    requires javafx.fxml;
    requires javafx.web;

    requires org.controlsfx.controls;
    requires com.dlsc.formsfx;
    requires net.synedra.validatorfx;
    requires org.kordamp.ikonli.javafx;
    requires org.kordamp.bootstrapfx.core;
    requires eu.hansolo.tilesfx;
    requires com.almasb.fxgl.all;
    requires static lombok;

    opens org.chainoptim.desktop to javafx.fxml;
    opens org.chainoptim.desktop.core.user.controller to javafx.fxml;
    exports org.chainoptim.desktop;

}