package org.chainoptim.desktop.features.productpipeline.repository;

import org.chainoptim.desktop.features.productpipeline.model.RawMaterial;

import java.util.List;
import java.util.Optional;

public interface RawMaterialRepository {

    public Optional<List<RawMaterial>> getRawMaterialsByOrganizationId(Integer organizationId);
}
