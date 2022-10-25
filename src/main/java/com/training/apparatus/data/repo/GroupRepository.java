package com.training.apparatus.data.repo;

import com.training.apparatus.data.entity.Group;
import com.training.apparatus.data.entity.User;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface GroupRepository extends JpaRepository<Group, Long> {

    Optional<Group> findByLink(String ling);

    @Query(value = "select g.link from groups g where g.user_id = :user_id", nativeQuery = true)
    Optional<String> findByUserId(@Param("user_id") long id);

    List<Group> findByManager(User manager);

    boolean existsByManager(User manager);
    boolean existsByManagerAndName(User manager, String name);
}
