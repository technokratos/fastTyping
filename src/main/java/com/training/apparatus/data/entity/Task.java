package com.training.apparatus.data.entity;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

import javax.persistence.*;
import java.util.Objects;

@Entity
@Table(name = "Tasks", indexes = {
        @Index(name = "ByTypeAndLanguageIdx", columnList = "type, language"),
        @Index(name = "ByTypeAndLanguageAndNumberIdx", columnList = "type, language, number", unique = true)})
@Getter
@Setter
public class Task {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;

    @NotNull
    @Enumerated(EnumType.STRING)
    Type type;

    @NotNull
    @Enumerated(EnumType.STRING)
    Language language;

    @NotNull
    Long number;
    @NotNull
    String title;

    @NotNull
    @Size(max =  1000)
    private String text;

//    @OneToOne(fetch = FetchType.LAZY)
//    @JoinColumn(name = "blob_image_id")
//    BlobImage blobImage;
//    @ManyToOne
//    @JoinColumn(name = "user_id")
//    User user;


    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Task task = (Task) o;
        return id == task.id && type == task.type && language == task.language && Objects.equals(number, task.number) && Objects.equals(title, task.title);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, type, language, number, title);
    }
}
