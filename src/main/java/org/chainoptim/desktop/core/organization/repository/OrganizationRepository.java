package org.chainoptim.desktop.core.organization.repository;

import org.chainoptim.desktop.core.organization.model.Organization;

import java.util.Optional;

public interface OrganizationRepository {
    public Optional<Organization> getOrganizationById(Integer organizationId);
}
