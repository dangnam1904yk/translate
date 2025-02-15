package com.translate.trans.model.Request;

import com.fasterxml.jackson.annotation.JsonKey;
import com.fasterxml.jackson.annotation.JsonRootName;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@NoArgsConstructor
@Data
public class GenerationConfig {

    private double temperature;
    private double topK;
    private double topP;
    private int maxOutputTokens;
    private String responseMimeType;
}
