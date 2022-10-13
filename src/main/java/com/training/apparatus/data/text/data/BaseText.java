package com.training.apparatus.data.text.data;

public enum BaseText {

    Tolstoy("tolstoy.txt");
    private final String fileName;
    BaseText(String fileName) {
        this.fileName = fileName;
    }

    public String getFileName() {
        return fileName;
    }
}
