package com.training.apparatus.data.exceptions;

import lombok.Getter;

/**
 * @author Kulikov Denis
 * @since 17.10.2022
 */
@Getter
public class ExceedTextSizeException extends Exception {
    private final String code;

    public ExceedTextSizeException(String code) {
        super(code);
        this.code = code;
    }
}
