package org.chainoptim.desktop.shared.version;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

public class VersionManager {

    private static final String VERSION_FILE_NAME = "version.json";
    private static final Path versionFilePath;
    private static final ObjectMapper mapper = new ObjectMapper();

    public static String getCurrentVersion() {
        try {
            if (Files.exists(versionFilePath)) {
                byte[] jsonData = Files.readAllBytes(versionFilePath);
                ObjectNode node = mapper.readValue(jsonData, ObjectNode.class);
                return node.get("version").asText();
            } else {
                return "1.0.3";
            }
        } catch (IOException e) {
            e.printStackTrace();
            return "Error reading version";
        }
    }

    public static void updateVersion(String newVersion) {
        try {
            ObjectNode versionData = mapper.createObjectNode();
            versionData.put("version", newVersion);
            mapper.writeValue(Files.newOutputStream(versionFilePath), versionData);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    static {
        String osName = System.getProperty("os.name").toLowerCase();
        String userHome = System.getProperty("user.home");

        if (osName.contains("win")) {
            String appData = System.getenv("APPDATA");
            if (appData != null) {
                versionFilePath = Path.of(appData, "ChainOptim", VERSION_FILE_NAME);
            } else {
                versionFilePath = Path.of(userHome, "AppData", "Local", "ChainOptim", VERSION_FILE_NAME);
            }
        } else if (osName.contains("mac")) {
            versionFilePath = Path.of(userHome, "Library", "Application Support", "ChainOptim", VERSION_FILE_NAME);
        } else if (osName.contains("nix") || osName.contains("nux")) {
            versionFilePath = Path.of(userHome, ".chainoptim", VERSION_FILE_NAME);
        } else {
            versionFilePath = Path.of(userHome, "ChainOptim", VERSION_FILE_NAME);
        }

        try {
            Files.createDirectories(versionFilePath.getParent());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
