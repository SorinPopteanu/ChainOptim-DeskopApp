package org.chainoptim.desktop.features.goods.stage.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class CreateStageDTO {
    private Integer productId;
    private Integer organizationId;
    private String name;
    private String description;
}
