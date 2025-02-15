package com.translate.trans.model.Response;

import com.google.gson.annotations.SerializedName;
import com.translate.trans.model.Request.ContentText;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class Candidate {
    @SerializedName("content")
    public ContentText content;

    @SerializedName("finishReason")
    public String finishReason;

    @SerializedName("index")
    public int index;
}
