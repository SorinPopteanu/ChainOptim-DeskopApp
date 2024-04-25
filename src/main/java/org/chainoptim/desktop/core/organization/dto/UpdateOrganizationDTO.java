package org.chainoptim.desktop.core.organization.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class UpdateOrganizationDTO {

    private Integer id;
    private String name;
    private String address;
    private String contactInfo;
}
