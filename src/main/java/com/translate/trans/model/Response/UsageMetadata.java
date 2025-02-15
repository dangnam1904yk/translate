package com.translate.trans.model.Response;

import com.google.gson.annotations.SerializedName;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class UsageMetadata {
    @SerializedName("promptTokenCount")
    public int promptTokenCount;

    @SerializedName("candidatesTokenCount")
    public int candidatesTokenCount;

    @SerializedName("totalTokenCount")
    public int totalTokenCount;
}
