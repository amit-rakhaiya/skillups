package com.amit.skillup.camunda.service;

import com.amit.skillup.camunda.entity.PersonEntity;
import com.amit.skillup.camunda.repository.PersonRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class PersonService {

    private final PersonRepository personRepository;

    public void savePerson(PersonEntity person) {
        log.info("Saving person: {}", person);
        personRepository.save(person);
        log.info("Person saved successfully with id: {}", person.getId());
    }
}
