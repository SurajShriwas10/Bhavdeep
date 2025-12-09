package com.suraj.moneymanager2.Repository;
import com.suraj.moneymanager2.Entity.CategoryEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;

public interface CategoryRepository extends JpaRepository<CategoryEntity , Long> {

    //select * from category where profile_id = 1
   List<CategoryEntity> findByProfileId(Long profileId);


   Optional<CategoryEntity> findByIdAndProfileId(Long id , Long profileId);


   List<CategoryEntity> findByTypeAndProfileId(String type , Long profileId);


   Boolean existsByNameAndProfileId(String name , Long profileId);



}
