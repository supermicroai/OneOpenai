package com.supersoft.oneapi.proxy.model.openai;

import lombok.Data;

import java.util.List;

@Data
public class Embedding {
    String object;
    int index;
    List<Double> embedding;
}
