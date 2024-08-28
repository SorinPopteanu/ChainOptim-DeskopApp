package org.chainoptim.desktop.features.storage.warehouse.dto;

import org.chainoptim.desktop.shared.search.dto.SmallEntityDTO;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class WarehouseOverviewDTO {

    private List<SmallEntityDTO> compartments;
    private List<SmallEntityDTO> storedComponents;
    private List<SmallEntityDTO> storedProducts;
    private List<SmallEntityDTO> deliveredFromSuppliers;
    private List<SmallEntityDTO> deliveredToClients;
}
