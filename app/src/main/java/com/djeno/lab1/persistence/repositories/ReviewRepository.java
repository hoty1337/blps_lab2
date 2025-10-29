package com.djeno.lab1.persistence.repositories;

import com.djeno.lab1.persistence.models.App;
import com.djeno.lab1.persistence.models.Review;
import com.djeno.lab1.persistence.models.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ReviewRepository extends JpaRepository<Review, Long> {

    boolean existsByUserAndApp(User user, App app);

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("update Review r set r.user.id = :unknownId where r.user.id in :userIds")
    void reassignReview(@Param("userIds") List<Long> userIds,
                              @Param("unknownId") Long unknownId);
}
