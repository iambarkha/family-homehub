package com.aab.homehub.family;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface FamilyRepository extends JpaRepository<FamilyGroup, UUID> {

    @Query("SELECT CASE WHEN COUNT(f) > 0 THEN true ELSE false END FROM FamilyGroup f WHERE f.name = :name")
    public boolean familyGroupExistsByName(String name);

   /* @Modifying
    @Query("DELETE FROM FamilyGroup f WHERE f.id = :familyId AND f.userId = :userId")
    int deleteByFamilyIdAndUserId(
            @Param("familyId") String familyId,
            @Param("userId") String userId);*/
   boolean existsByName(String name);
}
