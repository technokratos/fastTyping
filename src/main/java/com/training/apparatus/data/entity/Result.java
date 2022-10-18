package com.training.apparatus.data.entity;

import java.time.LocalDateTime;
import java.util.Objects;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "Results")
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
