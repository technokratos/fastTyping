package com.training.apparatus.data.entity;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.HashMap;
import java.util.Map;
import javax.persistence.AttributeConverter;
import javax.persistence.Converter;
import lombok.SneakyThrows;
import org.springframework.stereotype.Component;

/**
 * @author Kulikov Denis
 * @since 18.10.2022
 */

@Converter
@Component
public class SettingsConverter implements AttributeConverter<Map<User.Settings, String>, String> {

    private final ObjectMapper objectMapper = new ObjectMapper();
    @SneakyThrows
    @Override
    public String convertToDatabaseColumn(Map<User.Settings, String> settingsStringMap) {
        return objectMapper.writeValueAsString(settingsStringMap);
    }

    @SneakyThrows
    @Override
    public Map<User.Settings, String> convertToEntityAttribute(String json) {
        if (json == null) {
            return new HashMap<>();
        } else {
            TypeReference<HashMap<User.Settings, String>> typeRef
                    = new TypeReference<>() {
            };
            return objectMapper.readValue(json, typeRef);
        }
    }
}
