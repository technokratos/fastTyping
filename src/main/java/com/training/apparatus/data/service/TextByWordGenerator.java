package com.training.apparatus.data.service;

import com.training.apparatus.data.text.TextGenerator;
import com.training.apparatus.data.text.TextUtils;
import java.util.List;
import java.util.Map;

/**
 * @author Kulikov Denis
 * @since 12.10.2022
 */
public class TextByWordGenerator implements TextGenerator {
    private final Map<List<String>, List<TextUtils.WeightedSample<String>>> ngramHistogram;
    private final int ngram;

    public TextByWordGenerator(Map<List<String>, List<TextUtils.WeightedSample<String>>> ngramHistogram, int ngram) {
        this.ngramHistogram = ngramHistogram;
        this.ngram = ngram;
    }

    @Override
    public String generateString(int length, Map<Character, Double> weights) {
        return TextUtils.generateSentenceWithNgram(ngramHistogram, ngram, length, weights).toString();
    }
}
