package com.translate.trans.model.Request;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class FileData {
    @JsonProperty("mine_type")
    private String mime_type;
    @JsonProperty("file_uri")
    private String file_uri;
}
