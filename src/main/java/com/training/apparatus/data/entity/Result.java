package com.training.apparatus.data.entity;

import lombok.Getter;
import lombok.Setter;

import javax.persistence.*;
import java.time.LocalDateTime;
import java.util.Objects;

@Entity
@Table(name = "Results", indexes = {
        @Index(name = "ByUserAndTask_IDX", columnList = "user_id, task_id")
})
@Getter
@Setter
public class Result {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;

    @Column(name = "time", columnDefinition = "TIMESTAMP")
    private LocalDateTime time;

    private int speed;

    private double mistakes;

    private int totalSymbols;

    @ManyToOne(fetch = FetchType.LAZY)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    private Task task;

    public Result() {

    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Result result = (Result) o;
        return id == result.id && speed == result.speed && Double.compare(result.mistakes, mistakes) == 0 && Objects.equals(time, result.time);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, time, speed, mistakes);
    }
}
