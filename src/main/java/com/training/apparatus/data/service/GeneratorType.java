package com.training.apparatus.data.service;

import lombok.Getter;

/**
 * @author Kulikov Denis
 * @since 12.10.2022
 */
@Getter
public enum GeneratorType {
    LETTERS("generation.letters"), WORDS("generation.words");

    private final String key;

    GeneratorType(String key) {
        this.key = key;
    }
}
