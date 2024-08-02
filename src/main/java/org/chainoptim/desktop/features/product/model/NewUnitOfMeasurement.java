package org.chainoptim.desktop.features.product.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class NewUnitOfMeasurement {

    StandardUnit standardUnit;
    UnitMagnitude unitMagnitude;

    @JsonIgnore
    public String getFullName() {
        if (unitMagnitude == null || standardUnit == null) {
            return "";
        }
        return unitMagnitude.getName() + standardUnit.getName();
    }

    @JsonIgnore
    public String getAbbreviation() {
        return unitMagnitude.getAbbreviation() + standardUnit.getAbbreviation();
    }
}
