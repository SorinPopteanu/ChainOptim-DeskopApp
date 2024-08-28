package org.chainoptim.desktop.features.goods.productgraph.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ProductProductionGraph {

    private Integer id;
    private Integer productId;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private ProductGraph productGraph;
}
