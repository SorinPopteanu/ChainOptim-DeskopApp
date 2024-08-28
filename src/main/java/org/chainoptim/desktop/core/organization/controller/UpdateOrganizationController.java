package org.chainoptim.desktop.core.organization.controller;

import org.chainoptim.desktop.core.context.TenantContext;
import org.chainoptim.desktop.core.main.service.NavigationService;
import org.chainoptim.desktop.core.main.service.NavigationServiceImpl;
import org.chainoptim.desktop.core.organization.dto.UpdateOrganizationDTO;
import org.chainoptim.desktop.core.organization.model.Organization;
import org.chainoptim.desktop.core.organization.service.OrganizationService;
import org.chainoptim.desktop.shared.common.uielements.forms.FormField;
import org.chainoptim.desktop.shared.common.uielements.forms.ValidationException;
import org.chainoptim.desktop.shared.enums.OperationOutcome;
import org.chainoptim.desktop.shared.fallback.FallbackManager;
import org.chainoptim.desktop.shared.httphandling.Result;
import org.chainoptim.desktop.shared.toast.controller.ToastManager;
import org.chainoptim.desktop.shared.toast.model.ToastInfo;
import org.chainoptim.desktop.shared.util.resourceloader.CommonViewsLoader;
import com.google.inject.Inject;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.layout.StackPane;

import java.net.URL;
import java.util.ResourceBundle;

public class UpdateOrganizationController implements Initializable {

    // Services
    private final OrganizationService organizationService;
    private final NavigationService navigationService;
    private final CommonViewsLoader commonViewsLoader;
    private final ToastManager toastManager;
    private final FallbackManager fallbackManager;

    // State
    private Organization organization;

    // FXML
    @FXML
    private StackPane fallbackContainer;
    @FXML
    private FormField<String> nameFormField;
    @FXML
    private FormField<String> addressFormField;
    @FXML
    private FormField<String> contactInfoFormField;

    @Inject
    public UpdateOrganizationController(
            OrganizationService organizationService,
            NavigationService navigationService,
            CommonViewsLoader commonViewsLoader,
            ToastManager toastManager,
            FallbackManager fallbackManager) {
        this.organizationService = organizationService;
        this.navigationService = navigationService;
        this.commonViewsLoader = commonViewsLoader;
        this.toastManager = toastManager;
        this.fallbackManager = fallbackManager;
    }

    @FXML
    public void initialize(URL location, ResourceBundle resources) {
        this.organization = TenantContext.getCurrentUser().getOrganization();
        commonViewsLoader.loadFallbackManager(fallbackContainer);
        initializeFormFields();
    }

    private void initializeFormFields() {
        nameFormField.initialize(String::new, "Name", true, organization.getName(), "Your input is not valid");
        addressFormField.initialize(String::new, "Address", true, organization.getAddress(), "Your input is not valid");
        contactInfoFormField.initialize(String::new, "Contact Info", true, organization.getContactInfo(), "Your input is not valid");
    }

    @FXML
    private void handleSubmit() {
        UpdateOrganizationDTO organizationDTO = getUpdateOrganizationDTO();
        if (organizationDTO == null) return;
        System.out.println(organizationDTO);

        fallbackManager.reset();
        fallbackManager.setLoading(true);

        organizationService.updateOrganization(organizationDTO)
                .thenApply(this::handleUpdateOrganizationResponse)
                .exceptionally(this::handleUpdateOrganizationException);
    }

    private UpdateOrganizationDTO getUpdateOrganizationDTO() {
        UpdateOrganizationDTO organizationDTO = new UpdateOrganizationDTO();
        organizationDTO.setId(organization.getId());
        organizationDTO.setId(organizationDTO.getId());
        try {
            organizationDTO.setName(nameFormField.handleSubmit());
            organizationDTO.setAddress(addressFormField.handleSubmit());
            organizationDTO.setContactInfo(contactInfoFormField.handleSubmit());
        } catch (ValidationException e) {
            return null;
        }

        return organizationDTO;
    }

    private Result<Organization> handleUpdateOrganizationResponse(Result<Organization> result) {
        Platform.runLater(() -> {
            fallbackManager.setLoading(false);
            if (result.getError() != null) {
                toastManager.addToast(new ToastInfo(
                        "Error", "Failed to update organization.", OperationOutcome.ERROR));
                return;
            }
            toastManager.addToast(new ToastInfo(
                    "Success", "Organization updated successfully.", OperationOutcome.SUCCESS));

            // Manage navigation, invalidating previous warehouse cache
            String organizationPage = "Organization";
            NavigationServiceImpl.invalidateViewCache(organizationPage);
            navigationService.switchView(organizationPage, true, null);
        });
        return result;
    }

    private Result<Organization> handleUpdateOrganizationException(Throwable ex) {
        Platform.runLater(() -> toastManager.addToast(new ToastInfo(
                "An error occurred.", "Failed to update organization.", OperationOutcome.ERROR)));
        return new Result<>();
    }
}

