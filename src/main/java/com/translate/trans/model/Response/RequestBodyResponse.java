package com.translate.trans.model.Response;

import java.util.List;

import com.google.gson.annotations.SerializedName;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class RequestBodyResponse {

    @SerializedName("candidates")
    public List<Candidate> candidates;

    @SerializedName("usageMetadata")
    public UsageMetadata usageMetadata;

    @SerializedName("modelVersion")
    public String modelVersion;
}
