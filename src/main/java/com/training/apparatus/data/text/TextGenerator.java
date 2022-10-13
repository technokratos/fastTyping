package com.training.apparatus.data.text;

import java.util.Collections;
import java.util.Map;

/**
 * @author Kulikov Denis
 * @since 12.10.2022
 */
public interface TextGenerator {


    String generateString(int length, Map<Character, Double> weights );
    default String generateString(int length) {
        return generateString(length, Collections.emptyMap());
    }

}

