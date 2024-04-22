package org.chainoptim.desktop.shared.result;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class Error {
    private Date timestamp;
    private String message;
    private String details;
}
