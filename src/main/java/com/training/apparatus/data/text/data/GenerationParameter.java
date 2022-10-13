package com.training.apparatus.data.text.data;

import com.training.apparatus.data.service.GeneratorType;

/**
 * @author Kulikov Denis
 * @since 12.10.2022
 */
public record GenerationParameter(GeneratorType generatorType, int ngram, BaseText baseText) {
}
