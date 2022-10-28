package com.training.apparatus.data.repo;

import com.training.apparatus.data.dto.GroupDto;
import com.training.apparatus.data.entity.Group;
import com.training.apparatus.data.entity.User;
import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface GroupRepository extends JpaRepository<Group, Long> {

    Optional<Group> findByLink(String ling);

    @Query(value = "select g.link from groups g where g.user_id = :user_id", nativeQuery = true)
    Optional<String> findByUserId(@Param("user_id") long id);

    List<Group> findByManager(User manager);
    Page<Group> findByManager(User manager, Pageable pageable);

    boolean existsByManager(User manager);
    boolean existsByManagerAndName(User manager, String name);

    Long countByManager(User manager);


    @Query(value = "select new com.training.apparatus.data.dto.GroupDto( " +
            "g.name, " +
            "g.link, " +
            "count(u.id)) " +
            "from  Group g " +
            "left join User u on u.group.id = g.id " +
            "where g.manager.id = :id " +
            "group by g.id",
            countQuery = "select " +
                    "count(*) " +
                    "from  Group g " +
                    "where g.manager.id = :id")
    Page<GroupDto> findGroupDtoByManager(@Param("id") Long managerId, Pageable pageable);
}
