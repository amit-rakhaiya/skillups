package com.amit.skillup.camunda.repository;

import com.amit.skillup.camunda.entity.PersonEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface PersonRepository extends JpaRepository<PersonEntity, Long> {

    /**
     * Idempotency check — used to detect if a person record already exists
     * for a given email before saving. Prevents duplicate inserts on Zeebe job retries.
     */
    boolean existsByEmail(String email);
}
