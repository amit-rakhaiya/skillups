package com.amit.skillup.camunda.worker;

import io.camunda.zeebe.client.api.response.ActivatedJob;
import io.camunda.zeebe.client.api.worker.JobClient;
import io.camunda.zeebe.spring.client.annotation.JobWorker;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.Map;

@Slf4j
@Component
public class NotifySeniorManagerWorker {

    @JobWorker(type = "notify-senior-manager")
    public void notifySeniorManager(JobClient client, ActivatedJob job) {
        Map<String, Object> variables = job.getVariablesAsMap();
        String name = (String) variables.get("name");
        String email = (String) variables.get("email");

        log.warn("ESCALATION: Manager did not act in time for applicant: {} ({}) — notifying senior manager.", name, email);

        // In a real system: send email/Slack/Teams notification to senior manager
        // For POC: log is sufficient

        client.newCompleteCommand(job.getKey()).send().join();
        log.info("Senior manager notified. Escalation job completed for: {}", name);
    }
}
