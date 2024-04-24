package org.chainoptim.desktop.shared.caching;

import org.chainoptim.desktop.shared.search.model.SearchParams;
import org.chainoptim.desktop.shared.util.JsonUtil;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

public class CacheKeyBuilder {

    private CacheKeyBuilder() {}

    public static String buildAdvancedSearchKey(String feature, String secondaryFeature, String secondaryId, SearchParams searchParams) {
        String key = feature + "/" + secondaryFeature + "/advanced/" + secondaryId +
                "?searchQuery=" + searchParams.getSearchQuery() +
                "&sortBy=" + searchParams.getSortOption() +
                "&ascending=" + searchParams.getAscending() +
                "&page=" + searchParams.getPage() +
                "&itemsPerPage=" + searchParams.getItemsPerPage();

        if (!searchParams.getFiltersProperty().isEmpty()) {
            String filtersJson;
            try {
                filtersJson = JsonUtil.getObjectMapper().writeValueAsString(searchParams.getFiltersProperty());
                filtersJson = URLEncoder.encode(filtersJson, StandardCharsets.UTF_8);
            } catch (Exception e) {
                throw new RuntimeException("Error encoding filters to JSON", e);
            }

            key += "&filters=" + filtersJson;
        }

        return key;
    }

    public static String buildSecondaryFeatureKey(String mainFeature, String secondaryFeature, Integer secondaryFeatureId) {
        return mainFeature + "/" + secondaryFeature + "/" + secondaryFeatureId.toString();
    }
}
