package com.training.apparatus.data.repo;

import com.training.apparatus.data.entity.Language;
import com.training.apparatus.data.entity.Result;
import com.training.apparatus.data.entity.Task;
import com.training.apparatus.data.entity.Type;
import com.training.apparatus.data.entity.User;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ResultRepository extends JpaRepository<Result, Long> {

    @Query("select avg(r.speed) from Result r where r.user.id = :id")
    Optional<Double> avgSpeed(@Param("id") long id);

    @Query("select avg(r.mistakes) from Result r where r.user.id = :id")
    Optional<Double> avgMistakes(@Param("id") long id);

    @Query("select count (r.user) from Result r where r.user.id = :id")
    int countResult(@Param("id") Long id);

    @Query(value = "select r.* " +
            "from results r " +
            "inner join tasks t " +
            "on r.task_id = t.id " +
            "inner join users u " +
            "on r.user_id = u.id " +
            "where t.type = :type and t.language = :language and t.number = :number and u.email = :email ", nativeQuery = true)
    Optional<Result> findResultByTaskAndUser(@Param("type") Type type, @Param("language") Language language, @Param("number") Long number, @Param("email") String email);

    List<Result> findByTaskAndUserOrderById(Task task, User auth);
    Long countByTaskAndUserOrderById(Task task, User auth);
}
