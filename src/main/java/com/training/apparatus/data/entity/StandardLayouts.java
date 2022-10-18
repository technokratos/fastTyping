package com.training.apparatus.data.entity;

import com.training.apparatus.data.dto.KeyLanguage;
import com.training.apparatus.data.dto.KeyboardLayout;

/**
 * @author Kulikov Denis
 * @since 07.10.2022
 */
public enum StandardLayouts {
    Russian(new KeyboardLayout("ЙЦЫК", KeyLanguage.RUS)), English(new KeyboardLayout("QWERTY", KeyLanguage.ENG));

    private final KeyboardLayout layout;


    StandardLayouts(KeyboardLayout layout) {
        this.layout = layout;
    }

    public KeyboardLayout getLayout() {
        return layout;
    }

    @Override
    public String toString() {
        return layout.name();
    }
}
