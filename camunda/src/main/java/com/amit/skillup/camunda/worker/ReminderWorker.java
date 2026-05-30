package com.amit.skillup.camunda.worker;

import io.camunda.zeebe.client.api.response.ActivatedJob;
import io.camunda.zeebe.client.api.worker.JobClient;
import io.camunda.zeebe.spring.client.annotation.JobWorker;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class ReminderWorker {

    @JobWorker(type = "send-manager-reminder")
    public void sendReminder(JobClient client, ActivatedJob job) {
        String name = (String) job.getVariablesAsMap().get("name");
        String taskName = (String) job.getVariablesAsMap().get("taskName");

        // TODO: replace with actual email service (e.g. SendGrid, JavaMailSender)
        log.info("Reminder for {} : {} is waiting for your attention", name, taskName);

        client.newCompleteCommand(job.getKey()).send().join();
    }
}
