package org.chainoptim.desktop.features.goods.dto;

import lombok.*;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class UnitOfMeasurement {

    private Integer id;
    private String name;
    private LocalDateTime createdAt;
    private String unitType;
    private Integer organizationId;
}
