package com.amit.skillup.camunda.worker;

import com.amit.skillup.camunda.entity.PersonEntity;
import com.amit.skillup.camunda.repository.PersonRepository;
import com.amit.skillup.camunda.service.PersonService;
import io.camunda.zeebe.client.api.response.ActivatedJob;
import io.camunda.zeebe.client.api.worker.JobClient;
import io.camunda.zeebe.spring.client.annotation.JobWorker;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.Map;

@Slf4j
@Component
@RequiredArgsConstructor
public class PersonInfoWorker {

    private final PersonService personService;
    private final PersonRepository personRepository;

    @JobWorker(type = "save-person-info")
    public void handlePersonInfo(JobClient client, ActivatedJob job) {
        Map<String, Object> variables = job.getVariablesAsMap();
        log.info("Task listener triggered with variables: {}", variables);

        String email = (String) variables.get("email");

        /*
         * IDEMPOTENCY CHECK
         * -----------------
         * Problem: Zeebe may retry a job if the worker crashes after saving to DB
         * but before completing the job via newCompleteCommand(). This would cause
         * a duplicate PersonEntity insert on retry.
         *
         * Solution: Before saving, check if a record with the same email already
         * exists in H2. If yes — skip the save and proceed to complete the job.
         * This makes the worker safe to retry any number of times.
         */
        if (personRepository.existsByEmail(email)) {
            log.warn("Idempotency check: Person with email {} already exists. " +
                     "Skipping save — likely a Zeebe job retry.", email);
        } else {
            // First-time execution — build entity from process variables and save
            PersonEntity person = PersonEntity.builder()
                    .name((String) variables.get("name"))
                    .age(variables.get("age") != null ? ((Number) variables.get("age")).intValue() : 0)
                    .gender((String) variables.get("gender"))
                    .email(email)
                    .mobile(variables.get("mobile") != null ? String.valueOf(variables.get("mobile")) : null)
                    .address((String) variables.get("address"))
                    .city((String) variables.get("city"))
                    .state((String) variables.get("state"))
                    .country((String) variables.get("country"))
                    .build();

            personService.savePerson(person);
        }

        /*
         * Always complete the job — regardless of whether we saved or skipped.
         * This tells Zeebe the job is done so the process can continue.
         * Without this, Zeebe would retry indefinitely.
         */
        client.newCompleteCommand(job.getKey()).send().join();
    }
}
