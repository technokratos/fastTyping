package com.training.apparatus.data.exceptions;

import java.io.IOException;

/**
 * @author Kulikov Denis
 * @since 12.10.2022
 */
public class TextGenerationException extends RuntimeException {
    public TextGenerationException(String message) {
        super(message);
    }

    public TextGenerationException(String message, IOException e) {
        super(message, e);
    }
}
