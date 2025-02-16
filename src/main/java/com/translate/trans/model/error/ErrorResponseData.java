package com.translate.trans.model.error;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@NoArgsConstructor
@Data
public class ErrorResponseData {
    private int code;
    private String message;
    private String status;
}