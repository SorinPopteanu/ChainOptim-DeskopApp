package org.chainoptim.desktop.features.production.factory.dto;

import org.chainoptim.desktop.shared.search.dto.SmallEntityDTO;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class FactoryOverviewDTO {

    private List<SmallEntityDTO> factoryStages;
    private List<SmallEntityDTO> manufacturedComponents;
    private List<SmallEntityDTO> manufacturedProducts;
    private List<SmallEntityDTO> deliveredFromSuppliers;
    private List<SmallEntityDTO> deliveredToClients;
}
