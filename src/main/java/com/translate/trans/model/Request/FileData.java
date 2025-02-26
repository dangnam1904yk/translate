package com.translate.trans.model.Request;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class FileData {
    @JsonProperty("mineType")
    private String mimeType;
    @JsonProperty("fileUri")
    private String fileUri;
}
