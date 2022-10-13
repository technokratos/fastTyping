package com.training.apparatus.data.text;

import com.google.common.io.CharStreams;
import com.training.apparatus.data.exceptions.TextGenerationException;
import com.training.apparatus.data.text.data.BaseText;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Random;
import java.util.Set;
import java.util.StringTokenizer;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.apache.commons.io.input.CharacterSetFilterReader;

import static java.nio.charset.StandardCharsets.UTF_8;

/**
 * @author Kulikov Denis
 * @since 11.10.2022
 */
public class TextUtils {
    public static final Random R = new Random(2);

    public static String readFileFromResources(BaseText baseText) {
        return readFileFromResources("texts/" + baseText.getFileName());
    }

    static String readFileFromResources(String fileName) {
        try {
            ClassLoader classLoader = TextUtils.class.getClassLoader();
            InputStream inputStream = classLoader.getResourceAsStream(fileName);
            if (inputStream == null) {
                throw new TextGenerationException("File %s is absent".formatted(fileName));
            }
            CharacterSetFilterReader filteredReader = new CharacterSetFilterReader(new InputStreamReader(inputStream, UTF_8), Stream.of('&', '#', '<', '>', '_', '`', '\'','/')
                    .map(it -> (int) it).collect(Collectors.toSet()));
            return CharStreams.toString(filteredReader);
        } catch (IOException e) {
            throw new TextGenerationException("File %s isn't accessed".formatted(fileName), e);
        }
    }

    public static Map<Character, Double> invertHistogram(List<WeightedSample<Character>> histogram) {
        List<WeightedSample<Character>> map = histogram.stream()
                .map(s -> new WeightedSample<>(s.sample, 1 / s.weight)).toList();
        double sum = map.stream().mapToDouble(s -> s.weight).sum();
        return map.stream().collect(Collectors.toMap(s -> s.sample, s -> s.weight / sum));

    }

    public static StringBuffer generateStringWithNgram(Map<String, List<WeightedSample<Character>>> letterNgramHistogram, int ngramsize, int length, Map<Character, Double> weights) {
        if (letterNgramHistogram.isEmpty()) {
            return new StringBuffer();
        }
        String firstNgram = getRandomKey(letterNgramHistogram.keySet());
        StringBuffer buffer = new StringBuffer(length);
        buffer.append(firstNgram);
        String current = firstNgram;
        for (int i = ngramsize; i < length; i++) {
            List<WeightedSample<Character>> weightedSamples = letterNgramHistogram.get(current);
            if (weightedSamples == null) {
                current = getRandomKey(letterNgramHistogram.keySet());
                weightedSamples = letterNgramHistogram.get(current);
            }
            weightedSamples = transformWeights(weightedSamples, weights);
            Character next = getNextRandomObject(weightedSamples);
            buffer.append(next);
            current = buffer.substring(i + 1 - ngramsize);

        }
        return buffer;
    }

    public static Map<String, List<WeightedSample<Character>>> letterNgramHistogram(String text, int ngramsize, Function<Character, Boolean> ignoreCharacters) {
        if (text.length() < 4) {
            return Collections.emptyMap();
        }
        String firstNgram = null;
        int count = 0;
        do {
            count++;
            char nextLetter = text.charAt(count);

            if (ignoreCharacters.apply(nextLetter)) {
                firstNgram = null;
            } else {
                if (firstNgram == null) {
                    firstNgram = Character.toString(nextLetter);
                } else {
                    firstNgram += nextLetter;
                }
            }
        } while (count < text.length() - 1 && (firstNgram == null || firstNgram != null && firstNgram.length() < ngramsize));

        if (firstNgram == null) {
            return Collections.emptyMap();
        }

        Map<String, Map<Character, Integer>> map = new HashMap<>();
        String current = firstNgram;
        for (int i = count + 1; i < text.length(); i++) {
//            if (count % 100000 == 0) {
//                System.out.println(i);
//            }
            char next = text.charAt(i);

            boolean ignore = ignoreCharacters.apply(next) || (current != null && current.endsWith(" "));
            if (!ignore && current != null && current.length() == ngramsize) {
                Map<Character, Integer> countMap = map.computeIfAbsent(current, character -> new HashMap<>());
                countMap.compute(next, (key, oldValue) -> (oldValue == null) ? 1 : oldValue + 1);
                current = current.substring(1, ngramsize) + next;
            } else if (ignore) {
                current = null;
            } else {
                current = (current == null) ? Character.toString(next) : (current + next);
            }

        }

        return normalize(map);
    }


    public static StringBuffer generateSentenceWithNgram(Map<List<String>, List<WeightedSample<String>>> letterNgramHistogram, int ngramsize, int length, Map<Character, Double> weights) {
        if (letterNgramHistogram.isEmpty()) {
            return new StringBuffer();
        }
        Predicate<List<String>> filter = strings -> !Objects.equals(strings.get(0), " ") && Pattern.matches("^[А-Я].*", strings.get(0));
        LinkedList<String> firstNgram = new LinkedList<>(getRandomKeyWithList(letterNgramHistogram.keySet(), filter));

        StringBuffer buffer = new StringBuffer(length);
        boolean prevIsSpase;
        String firstWord = firstNgram.get(0);
        buffer.append(firstWord);
        prevIsSpase = firstWord.endsWith(" ");
        for (int i = 1; i < firstNgram.size(); i++) {
            if (!prevIsSpase) {
                buffer.append(" ");
            }
            buffer.append(firstNgram.get(i));
            prevIsSpase = firstWord.endsWith(" ");
        }

        LinkedList<String> current = firstNgram;
        for (int i = ngramsize; i < length; i++) {
            List<WeightedSample<String>> weightedSamples = letterNgramHistogram.get(current);
            if (weightedSamples == null) {
                current = new LinkedList<>(getRandomKeyWithList(letterNgramHistogram.keySet()));
                weightedSamples = letterNgramHistogram.get(current);
            }
            weightedSamples = transformWordWeights(weightedSamples, weights);
            String next = getNextRandomObject(weightedSamples);
            if (!prevIsSpase && !Objects.equals(next, " ") && !Pattern.matches("\\p{Punct}", next)) {
                buffer.append(" ");
            }
            buffer.append(next);
            current.pollFirst();
            current.add(next);
            prevIsSpase = buffer.charAt(buffer.length() - 1) == ' ';

        }
        return buffer;
    }

    public static List<WeightedSample<Character>> transformWeights(List<WeightedSample<Character>> histogram, Map<Character, Double> weights) {
        if (histogram.size() <= 1 || weights.isEmpty()) {
            return histogram;
        }
        List<WeightedSample<Character>> weightedPairs = histogram.stream().map(p -> {
            Double weight = weights.getOrDefault(p.sample(), 0.0);
            return new WeightedSample<>(p.sample(), (Double) p.weight() + weight - (Double) p.weight() * weight);
        }).toList();
        double sum = weightedPairs.stream().mapToDouble(WeightedSample::weight).sum();
        if (sum == 0.0) {
            return histogram;
        }
        return weightedPairs.stream().map(p -> new WeightedSample<>(p.sample(), p.weight() / sum)).toList();
    }

    public static List<WeightedSample<String>> transformWordWeights(List<WeightedSample<String>> histogram, Map<Character, Double> weights) {
        if (histogram.size() <= 1 || weights.isEmpty()) {
            return histogram;
        }
        List<WeightedSample<String>> weightedPairs = histogram.stream().map(p -> {
            if (p.sample().length() == 1) {
                return p;
            } else {
                String word = p.sample();
                double wordWeight = word.chars().mapToDouble(c -> weights.getOrDefault((char) c, 0.0)).sum() / word.length();
                return new WeightedSample<>(p.sample(), wordWeight + p.weight() - wordWeight * p.weight());
            }
        }).toList();
        double sum = weightedPairs.stream().mapToDouble(stringWeightedSample -> stringWeightedSample.weight()).sum();
        if (sum == 0.0) {
            return histogram;
        }
        return weightedPairs.stream().map(p -> new WeightedSample<>(p.sample(), p.weight() / sum)).toList();
    }

    public static <T> T getRandomKey(Set<T> keys) {
        int position = R.nextInt(keys.size());
        Iterator<T> iterator = keys.iterator();
        for (int i = 0; i < position - 1; i++) {
            iterator.next();
        }
        return iterator.next();
    }

    public static <T> List<T> getRandomKeyWithList(Set<List<T>> keys) {
        return getRandomKeyWithList(keys, null);
    }

    public static <T> List<T> getRandomKeyWithList(Set<List<T>> keys, Predicate<List<T>> keyFilter) {
        if (keyFilter != null) {
            List<List<T>> filteredList = keys.stream().filter(keyFilter).toList();
            if (filteredList.size() > 0) {
                int position = R.nextInt(filteredList.size());
                return filteredList.get(position);
            }
        }

        int position = R.nextInt(keys.size());
        Iterator<List<T>> iterator = keys.iterator();
        for (int i = 0; i < position - 1; i++) {
            iterator.next();
        }
        return iterator.next();
    }

    public static Map<List<String>, List<WeightedSample<String>>> wordNgramHistogram(String text, int ngramsize, Function<Character, Boolean> ignoreCharacters) {
        if (text.length() == 0) {
            return Collections.emptyMap();
        }
        LinkedList<String> firstNgram = null;
        StringTokenizer tokenizer = new StringTokenizer(text, " ,.;-<>/\n\r()", true);

        do {
            String nextWord = tokenizer.nextToken();

            if (nextWord.chars().anyMatch(c -> ignoreCharacters.apply((char) c))) {
                firstNgram = null;
            } else {
                if (firstNgram == null) {
                    firstNgram = new LinkedList<>();
                }
                firstNgram.add(nextWord);
            }
        } while (tokenizer.hasMoreElements() && (firstNgram == null || firstNgram != null && firstNgram.size() < ngramsize));

        if (firstNgram == null) {
            return Collections.emptyMap();
        }

        Map<List<String>, Map<String, Integer>> map = new HashMap<>();

        LinkedList<String> current = firstNgram;
        while (tokenizer.hasMoreElements()) {
//            if (count % 100000 == 0) {
//                System.out.println(i);
//            }
            String next = tokenizer.nextToken();

            boolean ignore = next.chars().anyMatch(c -> ignoreCharacters.apply((char) c)) || (next.equals(" "));
            if (!ignore && current.size() == ngramsize) {
                Map<String, Integer> countMap = map.computeIfAbsent(new ArrayList<>(current), key -> new HashMap<>());
                countMap.compute(next, (key, oldValue) -> (oldValue == null) ? 1 : oldValue + 1);
                current.pollFirst();
                current.add(next);
            } else if (ignore) {
                // current  = new LinkedList<>();
            } else {
                current.add(next);
            }

        }

        return normalize(map);
    }

    public static <T, R> Map<T, List<WeightedSample<R>>> normalize(Map<T, Map<R, Integer>> map) {
        return map.entrySet().stream().collect(Collectors.toMap(Map.Entry::getKey, entry -> {
            Map<R, Integer> nextWordCountMap = entry.getValue();
            return normalizeIntegerMap(nextWordCountMap);
        }));
    }

    private static <K> List<WeightedSample<K>> normalizeIntegerMap(Map<K, Integer> map) {
        int amount = map.values().stream().mapToInt(e -> e).sum();
        return map.entrySet().stream().map(frequencyEntry -> new WeightedSample<>(frequencyEntry.getKey(), (double) frequencyEntry.getValue() / amount)).collect(Collectors.toList());
    }

    public static boolean hasLatinLetter(String next) {
        return next.chars().anyMatch(c -> isLatinLetter((char) c));
    }

    public static StringBuffer generateStringByLetterHistogram(List<WeightedSample<Character>> histogram, int length) {
        StringBuffer string = new StringBuffer();
        for (int i = 0; i < length; i++) {
            string.append(getNextRandomObject(histogram));
        }
        return string;
    }

    public static List<WeightedSample<Character>> letterHistogram(String text) {
        return letterHistogram(text, TextUtils::isLatinLetter);
    }

    public static List<WeightedSample<Character>> letterHistogram(String text, Function<Character, Boolean> ignore) {
        Map<Character, Integer> characterCountMap = new HashMap<>();
        for (int i = 0; i < text.length(); i++) {
            char c = text.charAt(i);
//            if (i % 1000000 == 0) {
//                System.out.println("Processed " + i);
//            }
            if (!ignore.apply(c)) {
                characterCountMap.compute(c, (character, oldValue) -> (oldValue == null) ? 1 : oldValue + 1);
            }
        }

        int count = characterCountMap.values().stream().mapToInt(it -> it).sum();
        Map<Character, Double> histogramMap = new HashMap<>(characterCountMap.size());
        characterCountMap.forEach((character, amount) ->
                histogramMap.put(character, (double) amount / count)
        );

        System.out.println(characterCountMap);

        System.out.println(histogramMap);
        return histogramMap.entrySet().stream().map(e -> new WeightedSample<>(e.getKey(), e.getValue())).toList();
    }

    private static Character getNextCharacter(List<WeightedSample<Character>> histogram) {
        double r = R.nextDouble();
        double sum = 0;
        for (WeightedSample<Character> weightedSample : histogram) {
            sum += weightedSample.weight();
            if (sum > r) {
                return weightedSample.sample();
            }
        }
        return null;
    }

    private static <T> T getNextRandomObject(List<WeightedSample<T>> histogram) {
        double r = R.nextDouble();
        double sum = 0;
        for (WeightedSample<T> weightedSample : histogram) {
            sum += weightedSample.weight();
            if (sum > r) {
                return weightedSample.sample();
            }
        }
        return null;
    }

    public static boolean isLatinLetter(char c) {
        return (c >= 'A' && c <= 'Z') || (c >= 'a' && c <= 'z');
    }

    public record WeightedSample<T>(T sample, double weight) {
    }
}
