package org.chainoptim.desktop.shared.search.model;

import org.chainoptim.desktop.shared.common.uielements.UIItem;
import org.chainoptim.desktop.shared.enums.Feature;
import org.chainoptim.desktop.shared.enums.FilterType;
import org.chainoptim.desktop.shared.search.filters.FilterOption;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class SearchOptionsConfiguration {

    private SearchOptionsConfiguration() {
    }

    private static final SearchOptions SUPPLIER_ORDER_OPTIONS = new SearchOptions(
        List.of(
            new FilterOption(
                    new UIItem("Order Date Start", "orderDateStart"),
                    new ArrayList<>(),
                    FilterType.DATE
            ),
            new FilterOption(
                    new UIItem("Quantity", "greaterThanQuantity"),
                    new ArrayList<>(),
                    FilterType.NUMBER
            ),
            new FilterOption(
                    new UIItem("Status", "status"),
                    List.of(new UIItem("Delivered", "DELIVERED"), new UIItem("Pending", "PENDING")),
                    FilterType.ENUM
            )
        ),
        Map.of(
            "orderDate", "Order Date",
            "estimatedDeliveryDate", "Estimated Delivery Date",
            "deliveryDate", "Delivery Date",
            "quantity", "Quantity"
        )
    );

    private static final SearchOptions FACTORY_INVENTORY_OPTIONS = new SearchOptions(
            List.of(
                    new FilterOption(
                            new UIItem("Created At Start", "createdAtStart"),
                            new ArrayList<>(),
                            FilterType.DATE
                    ),
                    new FilterOption(
                            new UIItem("Quantity", "greaterThanQuantity"),
                            new ArrayList<>(),
                            FilterType.NUMBER
                    )
            ),
            Map.of(
                    "createdAt", "Created At",
                    "updatedAt", "Updated At",
                    "quantity", "Quantity"
            )
    );

    private static final Map<Feature, SearchOptions> SEARCH_OPTIONS_MAP = Map.of(
            Feature.SUPPLIER_ORDER, SUPPLIER_ORDER_OPTIONS,
            Feature.FACTORY_INVENTORY, FACTORY_INVENTORY_OPTIONS
    );

    public static SearchOptions getSearchOptions(Feature feature) {
        return SEARCH_OPTIONS_MAP.get(feature);
    }
}
