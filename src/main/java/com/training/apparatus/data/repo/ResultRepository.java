package com.training.apparatus.data.repo;

import com.training.apparatus.data.entity.Result;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface ResultRepository extends JpaRepository<Result, Long> {

    @Query("select avg(r.speed) from Result r where r.user.id = :id")
    Optional<Double> avgSpeed(@Param("id") long id);

    @Query("select avg(r.mistakes) from Result r where r.user.id = :id")
    Optional<Double> avgMistakes(@Param("id") long id);

    @Query("select count (r.user) from Result r where r.user.id = :id")
    int countResult(@Param("id") Long id);

}
