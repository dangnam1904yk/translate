package com.translate.trans.model.Request;

import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class RequestBodySend {
    private List<ContentText> contents;
    private GenerationConfig generationConfig;
}
