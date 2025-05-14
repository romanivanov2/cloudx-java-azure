package com.chtrembl.petstore.pet.service;

import com.chtrembl.petstore.pet.model.Pet;
import com.chtrembl.petstore.pet.repository.PetRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional
public class PetService {
    private final PetRepository petRepository;

    @Autowired
    public PetService(PetRepository petRepository) {
        this.petRepository = petRepository;
    }

    public List<Pet> findPetsByStatus(List<String> statusList) {
        List<Pet.StatusEnum> statuses = statusList.stream()
                .map(Pet.StatusEnum::fromValue)
                .collect(Collectors.toList());
        return petRepository.findByStatusIn(statuses);
    }

}
