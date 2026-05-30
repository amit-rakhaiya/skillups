package com.amit.skillup.camunda.worker;

import io.camunda.zeebe.client.api.response.ActivatedJob;
import io.camunda.zeebe.client.api.worker.JobClient;
import io.camunda.zeebe.spring.client.annotation.JobWorker;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.Map;

@Slf4j
@Component
public class BackgroundCheckWorker {

    @JobWorker(type = "backgroundCheck")
    public void handleBackgroundCheck(JobClient client, ActivatedJob job) {
        Map<String, Object> variables = job.getVariablesAsMap();
        String name = (String) variables.get("name");
        String email = (String) variables.get("email");

        // TODO: integrate with actual background check service
        log.info("Running background check for: {} ({})", name, email);

        // Simulate check passed
        boolean backgroundCheckPassed = true;

        client.newCompleteCommand(job.getKey())
                .variable("backgroundCheckPassed", backgroundCheckPassed)
                .send()
                .join();

        log.info("Background check completed for: {} — passed: {}", name, backgroundCheckPassed);
    }
}
