package com.amit.skillup.camunda.worker;

import com.amit.skillup.camunda.entity.PersonEntity;
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

    @JobWorker(type = "save-person-info")
    public void handlePersonInfo(JobClient client, ActivatedJob job) {
        Map<String, Object> variables = job.getVariablesAsMap();
        log.info("Task listener triggered with variables: {}", variables);

        PersonEntity person = PersonEntity.builder()
                .name((String) variables.get("name"))
                .age(variables.get("age") != null ? ((Number) variables.get("age")).intValue() : 0)
                .gender((String) variables.get("gender"))
                .email((String) variables.get("email"))
                .mobile(variables.get("mobile") != null ? String.valueOf(variables.get("mobile")) : null)
                .address((String) variables.get("address"))
                .city((String) variables.get("city"))
                .state((String) variables.get("state"))
                .country((String) variables.get("country"))
                .build();

        personService.savePerson(person);

        client.newCompleteCommand(job.getKey()).send().join();
    }
}
