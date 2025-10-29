package com.djeno.lab1.persistence.repositories;

import com.djeno.lab1.persistence.models.App;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface AppRepository extends JpaRepository<App, Long> {

    Page<App> findByCategories_Id(Long categoryId, Pageable pageable);

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("update App app set app.owner.id = :unknownId where app.owner.id in :userIds")
    void reassignAppOwner(@Param("userIds") List<Long> userIds,
                               @Param("unknownId") Long unknownId);

    App findByFileId(String appId);
}
