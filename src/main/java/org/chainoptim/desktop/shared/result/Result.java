package org.chainoptim.desktop.shared.result;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class Result<T> {

    private T data;
    private Error error;

    private int statusCode;
}
