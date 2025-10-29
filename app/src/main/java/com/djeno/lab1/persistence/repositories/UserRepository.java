package com.djeno.lab1.persistence.repositories;

import com.djeno.lab1.persistence.models.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByUsername(String username);
    Optional<User> findByEmail(String email);
    boolean existsByUsername(String username);
    boolean existsByEmail(String email);

    @Modifying
    @Query("""
         update User u
            set u.lastLogin = :now
          where u.username = :username
            and (u.lastLogin is null or u.lastLogin < :minAllowed)
         """)
    void touchLastLogin(@Param("username") String username,
                       @Param("now") LocalDateTime now,
                       @Param("minAllowed") LocalDateTime minAllowed);

    @Query("""
       SELECT u FROM User u
        WHERE ((u.lastLogin IS NULL OR u.lastLogin < :threshold) AND u.id != 1)
       """)
    List<User> findInactiveUsers(@Param("threshold") LocalDateTime threshold);
}
