package org.chainoptim.desktop.features.production.model;

import lombok.Data;
import org.chainoptim.desktop.features.goods.stage.model.Stage;

@Data
public class FactoryStage {

    private Integer id;
    private Float capacity;
    private Float duration;
    private Integer priority;
    private Float minimumRequiredCapacity;
    private Factory factory;
    private Stage stage;
}
