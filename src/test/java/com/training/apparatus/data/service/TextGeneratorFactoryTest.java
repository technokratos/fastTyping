package com.training.apparatus.data.service;

import com.training.apparatus.data.text.TextGenerator;
import com.training.apparatus.data.text.TextUtils;
import com.training.apparatus.data.text.data.BaseText;
import com.training.apparatus.data.text.data.GenerationParameter;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class TextGeneratorFactoryTest {

    @Test
    void checkHistogram() {
        TextGeneratorFactory generatorFactory = new TextGeneratorFactory();

        TextGenerator letterGenerator = generatorFactory.textGenerator(new GenerationParameter(GeneratorType.LETTERS, 1, BaseText.Tolstoy));
        String baseText = TextUtils.readFileFromResources(BaseText.Tolstoy);
        List<TextUtils.WeightedSample<Character>> baseHistogram = TextUtils.letterHistogram(baseText);

        List<TextUtils.WeightedSample<Character>> genHistogram = TextUtils.letterHistogram(letterGenerator.generateString(10000));

        double correlation = getCorrelation(baseHistogram, genHistogram);
        Map<Character, Double> weights = TextUtils.invertHistogram(baseHistogram);

        List<TextUtils.WeightedSample<Character>> weightedHistogram = TextUtils.letterHistogram(letterGenerator.generateString(10000, weights));
        double weightedCorrelation = getCorrelation(baseHistogram, weightedHistogram);

//        assertTrue(correlation> weightedCorrelation);
    }

    @Test
    void checkWordGenerator() {
        TextGeneratorFactory generatorFactory = new TextGeneratorFactory();

        TextGenerator letterGenerator = generatorFactory.textGenerator(new GenerationParameter(GeneratorType.WORDS, 2, BaseText.Tolstoy));

        String text = letterGenerator.generateString(1000);
        assertFalse(text.contains("  "));

        boolean startWithUpperCase = Pattern.matches("^[А-Я].*", text.substring(0, 10));
        assertTrue(startWithUpperCase);
    }

    @Test
    void checkTextGenerator() {
        String text = TextUtils.readFileFromResources(BaseText.Tolstoy);
        Map<String, List<TextUtils.WeightedSample<Character>>> letterNgramHistogram = TextUtils.letterNgramHistogram(text, 1, c -> TextUtils.isLatinLetter(c) || c == '\n');
        List<TextUtils.WeightedSample<Character>> weightedSamples = letterNgramHistogram.getOrDefault(" ", Collections.emptyList());
        assertEquals(0, weightedSamples.stream().filter(w-> w.sample() == ' ').count());
    }

    private static double getCorrelation(List<TextUtils.WeightedSample<Character>> baseHistogram, List<TextUtils.WeightedSample<Character>> genHistogram) {
        double baseAverage = baseHistogram.stream().mapToDouble(TextUtils.WeightedSample::weight).sum();
        double genAverage = genHistogram.stream().mapToDouble(TextUtils.WeightedSample::weight).sum();
        Map<Character, Double> genMap = genHistogram.stream().collect(Collectors.toMap(TextUtils.WeightedSample::sample, TextUtils.WeightedSample::weight));
        double numerator = baseHistogram.stream().mapToDouble(s -> (s.weight() - baseAverage) * (genMap.getOrDefault(s.sample(), 0.0) - genAverage)).sum();
        double denominator = Math.sqrt(baseHistogram.stream().mapToDouble(s -> qrt(s.weight() - baseAverage) * qrt(genMap.getOrDefault(s.sample(), 0.0) - genAverage)).sum());
        return numerator/ denominator;
    }

    static double qrt(double a) {
        return a * a;
    }

}