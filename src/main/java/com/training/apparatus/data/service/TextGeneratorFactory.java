package com.training.apparatus.data.service;

import com.training.apparatus.data.exceptions.TextGenerationException;
import com.training.apparatus.data.text.TextGenerator;
import com.training.apparatus.data.text.TextUtils;
import com.training.apparatus.data.text.data.GenerationParameter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.springframework.stereotype.Service;

/**
 * @author Kulikov Denis
 * @since 12.10.2022
 */
@Service
public class TextGeneratorFactory {
    private final Map<GenerationParameter, TextGenerator> generators = new HashMap<>();

    public TextGenerator textGenerator(GenerationParameter parameter) {
        if (parameter.ngram() < 1 || parameter.ngram() > 5) {
            throw new TextGenerationException("Ngram is limited in from 1 to 5");
        }

        TextGenerator textGenerator = generators.get(parameter);
        if (textGenerator != null) {
            return textGenerator;
        } else  {
            String text = TextUtils.readFileFromResources(parameter.baseText());
            final TextGenerator generator;
            if (parameter.generatorType() == GeneratorType.LETTERS) {
                Map<String, List<TextUtils.WeightedSample<Character>>> letterNgramHistogram = TextUtils.letterNgramHistogram(text, parameter.ngram(), c -> TextUtils.isLatinLetter(c) || c == '\n');
                generator = new TextByLetterGenerator(letterNgramHistogram, parameter.ngram());
                generators.put(parameter, generator);
            } else if (parameter.generatorType() == GeneratorType.WORDS){
                Map<List<String>, List<TextUtils.WeightedSample<String>>> ngramHistogram = TextUtils.wordNgramHistogram(text, parameter.ngram(), c -> TextUtils.isLatinLetter(c) || c == '\n');
                generator = new TextByWordGenerator(ngramHistogram, parameter.ngram());
            } else {
                throw new IllegalStateException("Unsupported type" + parameter.generatorType());
            }
            generators.put(parameter, generator);
            return generator;
        }

    }

}
