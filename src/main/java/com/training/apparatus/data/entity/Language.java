package com.training.apparatus.data.entity;

import com.training.apparatus.translation.TranslationProvider;
import java.util.Locale;

/**
 * @author Kulikov Denis
 * @since 07.10.2022
 */
public enum Language {
    Russian(TranslationProvider.LOCALE_RU), English(TranslationProvider.LOCALE_EN);

    private final Locale locale;

    Language(Locale locale) {
        this.locale = locale;
    }

    public Locale getLocale() {
        return locale;
    }
}
