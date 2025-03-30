package com.chtrembl.petstore.pet.repository;

import com.chtrembl.petstore.pet.model.Pet;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PetRepository extends JpaRepository<Pet, Long> {

    List<Pet> findByStatus(Pet.StatusEnum status);

    List<Pet> findByStatusIn(List<Pet.StatusEnum> statuses);

    List<Pet> findByTagsNameIn(List<String> tagNames);
}
