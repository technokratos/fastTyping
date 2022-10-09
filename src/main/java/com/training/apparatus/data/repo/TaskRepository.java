package com.training.apparatus.data.repo;

import com.training.apparatus.data.entity.Language;
import com.training.apparatus.data.entity.Task;
import com.training.apparatus.data.entity.Type;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface TaskRepository extends JpaRepository<Task, Long> {

    Long countByTypeAndLanguage(Type type, Language language);

    List<Task> findByTypeAndLanguage(Type type, Language language);
    Optional<Task> findByNumberAndTypeAndLanguage(Type type, Language language, Long number);

}
