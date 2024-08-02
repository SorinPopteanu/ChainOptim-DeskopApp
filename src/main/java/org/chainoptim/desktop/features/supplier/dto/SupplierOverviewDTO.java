package org.chainoptim.desktop.features.supplier.dto;

import org.chainoptim.desktop.shared.search.dto.SmallEntityDTO;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class SupplierOverviewDTO {


    private List<SmallEntityDTO> suppliedComponents;
}
