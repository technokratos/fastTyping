package com.training.apparatus.data.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.training.apparatus.data.dto.LessonDto;
import com.training.apparatus.data.entity.Language;
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

    private static final Map<Language, LessonDto[]> languageMap = new HashMap<>();
    static {
        ObjectMapper mapper = new ObjectMapper();
        try {
            languageMap.put(Language.Russian, getLessonDtos(mapper, "ru_RU.json"));
            languageMap.put(Language.English, getLessonDtos(mapper, "en_US.json"));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

    }


    private static LessonDto[] getLessonDtos(ObjectMapper mapper, String fileName) throws IOException {
        ClassLoader classLoader = LessonUtils.class.getClassLoader();
        InputStream inputStream = classLoader.getResourceAsStream(fileName);
        return mapper.readValue(inputStream, LessonDto[].class);
    }

    public static List<LessonDto> getByLanguage(Language language) {
        return Arrays.stream(languageMap.get(language)).toList();
    }


}
