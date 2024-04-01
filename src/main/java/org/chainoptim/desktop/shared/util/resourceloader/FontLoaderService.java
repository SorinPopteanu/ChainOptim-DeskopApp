package org.chainoptim.desktop.shared.util.resourceloader;

import javafx.scene.text.Font;

public class FontLoaderService {

    private FontLoaderService() {}

    public static void loadRobotoFonts() {
        Font.loadFont(FontLoaderService.class.getResourceAsStream("/fonts/Roboto-Regular.ttf"), 14);
        Font.loadFont(FontLoaderService.class.getResourceAsStream("/fonts/Roboto-Bold.ttf"), 14);
    }
}
