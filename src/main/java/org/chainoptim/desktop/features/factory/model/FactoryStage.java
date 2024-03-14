package org.chainoptim.desktop.features.factory.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Data;
import org.chainoptim.desktop.features.productpipeline.model.Stage;

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
