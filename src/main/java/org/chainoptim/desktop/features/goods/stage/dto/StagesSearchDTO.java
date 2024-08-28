package org.chainoptim.desktop.features.goods.stage.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
public class StagesSearchDTO {
    private Integer id;
    private String name;
    private LocalDateTime createdAt;
}
