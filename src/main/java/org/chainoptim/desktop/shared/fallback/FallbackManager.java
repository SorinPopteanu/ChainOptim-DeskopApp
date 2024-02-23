package org.chainoptim.desktop.shared.fallback;

import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;


public class FallbackManager {

    private final StringProperty errorMessage = new SimpleStringProperty("");
    private final BooleanProperty isLoading = new SimpleBooleanProperty(false);
    private final BooleanProperty noOrganization = new SimpleBooleanProperty(false);
    private final BooleanProperty noResults = new SimpleBooleanProperty(false);

    public String getErrorMessage() {
        return errorMessage.get();
    }

    public void setErrorMessage(String errorMessage) {
        this.errorMessage.set(errorMessage);
    }

    public StringProperty errorMessageProperty() {
        return errorMessage;
    }

    public boolean isLoading() {
        return isLoading.get();
    }

    public void setLoading(boolean isLoading) {
        this.isLoading.set(isLoading);
    }

    public BooleanProperty isLoadingProperty() {
        return isLoading;
    }

    public boolean isNoOrganization() {
        return noOrganization.get();
    }

    public void setNoOrganization(boolean noOrganization) {
        this.noOrganization.set(noOrganization);
    }

    public BooleanProperty noOrganizationProperty() {
        return noOrganization;
    }

    public boolean isNoResults() {
        return noResults.get();
    }

    public void setNoResults(boolean noResults) {
        this.noResults.set(noResults);
    }

    public BooleanProperty noResultsProperty() {
        return noResults;
    }
}
