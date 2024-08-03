package org.chainoptim.desktop.core.map;

import org.chainoptim.desktop.core.map.model.SupplyChainMap;
import org.chainoptim.desktop.features.scanalysis.factorygraph.service.JavaConnector;
import org.chainoptim.desktop.shared.util.DataReceiver;
import javafx.concurrent.Worker;
import javafx.scene.web.WebView;
import netscape.javascript.JSObject;

import java.util.Objects;

public class MapController implements DataReceiver<SupplyChainMap> {

    SupplyChainMap supplyChainMap;

    private WebView webView;
    private JavaConnector javaConnector;

    @Override
    public void setData(SupplyChainMap data) {
        this.supplyChainMap = data;

        if (supplyChainMap == null) {
            return;
        }

        webView = new WebView();
        webView.getEngine().loadContent(Objects.requireNonNull(getClass().getResource("/html/supplychainmap.html")).toExternalForm());
        webView.getEngine().getLoadWorker().stateProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue == Worker.State.SUCCEEDED) {
                JSObject jsObject = (JSObject) webView.getEngine().executeScript("window");
                javaConnector = new JavaConnector();
                jsObject.setMember("javaConnector", javaConnector);
            }
        });
    }
}
