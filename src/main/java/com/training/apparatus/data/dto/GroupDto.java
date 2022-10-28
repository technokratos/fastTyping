package com.training.apparatus.data.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * @author Kulikov Denis
 * @since 28.10.2022
 */
@Setter
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class GroupDto {
    private String name;
    private String link;
    private long count;
}
