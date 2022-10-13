package com.training.apparatus.data.service;

import com.training.apparatus.data.text.TextGenerator;
import com.training.apparatus.data.text.TextUtils;
import java.util.List;
import java.util.Map;

/**
 * @author Kulikov Denis
 * @since 12.10.2022
 */
public class TextByLetterGenerator implements TextGenerator {
    private final Map<String, List<TextUtils.WeightedSample<Character>>> letterNgramHistogram;
    private final int ngram;

    public TextByLetterGenerator(Map<String, List<TextUtils.WeightedSample<Character>>> letterNgramHistogram, int ngram) {
        this.letterNgramHistogram = letterNgramHistogram;
        this.ngram = ngram;
    }

    @Override
    public String generateString(int length, Map<Character, Double> weights) {
        return  TextUtils.generateStringWithNgram(letterNgramHistogram, ngram, length, weights).toString();
    }
}
