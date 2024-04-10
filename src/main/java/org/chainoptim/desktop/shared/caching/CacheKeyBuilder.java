package org.chainoptim.desktop.shared.caching;

import org.chainoptim.desktop.shared.search.model.SearchParams;

public class CacheKeyBuilder {

    private CacheKeyBuilder() {}

    public static String buildAdvancedSearchKey(String feature, Integer organizationId, SearchParams searchParams) {
        return feature + "/organization/advanced/" + organizationId.toString()
                + "?searchQuery=" + searchParams.getSearchQuery()
                + "&sortBy=" + searchParams.getSortOption()
                + "&ascending=" + searchParams.getAscending()
                + "&page=" + searchParams.getPage()
                + "&itemsPerPage=" + searchParams.getItemsPerPage();
    }

    public static String buildSecondaryFeatureKey(String mainFeature, String secondaryFeature, Integer secondaryFeatureId) {
        return mainFeature + "/" + secondaryFeature + "/" + secondaryFeatureId.toString();
    }
}
