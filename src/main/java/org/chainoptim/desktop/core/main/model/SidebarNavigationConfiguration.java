package org.chainoptim.desktop.core.main.model;

import org.chainoptim.desktop.core.main.service.NavigationService;

import lombok.Getter;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Getter
public class SidebarNavigationConfiguration {

    private SidebarNavigationConfiguration() {}

    private static final SidebarSection[] SECTIONS = new SidebarSection[] {
            SidebarSection.builder()
                    .name("Overview")
                    .subsections(new ArrayList<>())
                    .build(),
            SidebarSection.builder()
                    .name("Organization")
                    .subsections(new ArrayList<>())
                    .build(),
            SidebarSection.builder()
                    .name("Goods")
                    .isExpanded(false)
                    .subsections(List.of(
                            SidebarSubsection.builder()
                                    .name("Products")
                                    .build(),
                            SidebarSubsection.builder()
                                    .name("Stages")
                                    .build(),
                            SidebarSubsection.builder()
                                    .name("Components")
                                    .build()
                    ))
                    .build(),
            SidebarSection.builder()
                    .name("Supply")
                    .isExpanded(false)
                    .subsections(List.of(
                            SidebarSubsection.builder()
                                    .name("Suppliers")
                                    .build(),
                            SidebarSubsection.builder()
                                    .name("Supplier Orders")
                                    .build(),
                            SidebarSubsection.builder()
                                    .name("Supplier Shipments")
                                    .build(),
                            SidebarSubsection.builder()
                                    .name("Performances")
                                    .build()
                    ))
                    .build(),
            SidebarSection.builder()
                    .name("Production")
                    .isExpanded(false)
                    .subsections(List.of(
                            SidebarSubsection.builder()
                                    .name("Factories")
                                    .build(),
                            SidebarSubsection.builder()
                                    .name("Factory Stages")
                                    .build(),
                            SidebarSubsection.builder()
                                    .name("Factory Inventory")
                                    .build(),
                            SidebarSubsection.builder()
                                    .name("Performances")
                                    .build()
                    ))
                    .build(),
            SidebarSection.builder()
                    .name("Storage")
                    .isExpanded(false)
                    .subsections(List.of(
                            SidebarSubsection.builder()
                                    .name("Warehouses")
                                    .build(),
                            SidebarSubsection.builder()
                                    .name("Warehouse Inventory")
                                    .build(),
                            SidebarSubsection.builder()
                                    .name("Evaluations")
                                    .build()
                    ))
                    .build(),
            SidebarSection.builder()
                    .name("Demand")
                    .isExpanded(false)
                    .subsections(List.of(
                            SidebarSubsection.builder()
                                    .name("Clients")
                                    .build(),
                            SidebarSubsection.builder()
                                    .name("Client Orders")
                                    .build(),
                            SidebarSubsection.builder()
                                    .name("Client Shipments")
                                    .build(),
                            SidebarSubsection.builder()
                                    .name("Evaluations")
                                    .build()
                    ))
                    .build(),
            SidebarSection.builder()
                    .name("Settings")
                    .subsections(new ArrayList<>())
                    .build()
    };

    public static SidebarSection[] getSidebarSections(NavigationService navigationService) {
        for (SidebarSection section : SECTIONS) {
            section.setIconPath(ICONS_PATH + BUTTON_ICON_MAP.get(section.getName()));
            section.setAction(() -> navigationService.switchView(
                    section.getSubsections().getFirst().getName(), true
            ));

            section.getSubsections().forEach(subsection -> {
                subsection.setIconPath(ICONS_PATH + BUTTON_ICON_MAP.get(subsection.getName()));
                subsection.setAction(() -> navigationService.switchView(
                        subsection.getName(), false
                ));
            });
        }

        return SECTIONS;
    }

    private static final Map<String, String> BUTTON_ICON_MAP = Map.ofEntries(
            Map.entry("Overview", "globe-solid.png"),
            Map.entry("Organization", "building-solid.png"),
            Map.entry("Goods", "box-solid.png"),
            Map.entry("Supply", "truck-arrow-right-solid.png"),
            Map.entry("Production", "industry-solid.png"),
            Map.entry("Storage", "warehouse-solid.png"),
            Map.entry("Demand", "universal-access-solid.png"),
            Map.entry("Settings", "gear-solid.png"),

            Map.entry("Account", "user-solid.png"),
            Map.entry("Back", "arrow-left-solid.png"),
            Map.entry("Toggle", "bars-solid.png"),
            Map.entry("Logout", "right-from-bracket-solid.png")
    );

    private static final String ICONS_PATH = "/img/";

    public static String getButtonIconPath(String buttonName) {
        return ICONS_PATH + BUTTON_ICON_MAP.get(buttonName);
    }
}
