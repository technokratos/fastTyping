package com.training.apparatus.keyboards;

import lombok.Getter;

@Getter
public enum CommandMode {
    Full("typing.full"), Short("typing.short"), Icon("typing.icon");
    private final String key;

    CommandMode(String key) {
        this.key = key;
    }

}
