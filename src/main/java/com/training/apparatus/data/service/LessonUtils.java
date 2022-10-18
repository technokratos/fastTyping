package com.training.apparatus.data.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.training.apparatus.data.dto.KeyLanguage;
import com.training.apparatus.data.dto.LessonDto;
import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author Kulikov Denis
 * @since 07.10.2022
 */
public class LessonUtils {

    private static final Map<KeyLanguage, LessonDto[]> languageMap = new HashMap<>();
    static {
        ObjectMapper mapper = new ObjectMapper();
        try {
            languageMap.put(KeyLanguage.RUS, getLessonDtos(mapper, "ru_RU.json"));
            languageMap.put(KeyLanguage.ENG, getLessonDtos(mapper, "en_US.json"));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

    }


    private static LessonDto[] getLessonDtos(ObjectMapper mapper, String fileName) throws IOException {
        ClassLoader classLoader = LessonUtils.class.getClassLoader();
        InputStream inputStream = classLoader.getResourceAsStream(fileName);
        return mapper.readValue(inputStream, LessonDto[].class);
    }

    public static List<LessonDto> getByLanguage(KeyLanguage language) {
        return Arrays.stream(languageMap.get(language)).toList();
    }


}
