package com.translate.trans.model.Request;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonKey;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@NoArgsConstructor
@Data
public class RequestModel {
    private List<ContentText> contents;
}
