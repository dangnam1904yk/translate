package com.translate.trans.model.Request;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@NoArgsConstructor
@Data
public class Part {
    private FileData fileData;
    private String text;

    public Part(String text) {
        this.text = text;
    }

    public Part(FileData fileData) {
        this.fileData = fileData;
    }
}