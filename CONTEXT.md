# Camunda POC — Session Context

> **Reload instruction for Claude:** Read this file at session start. Do NOT ask the user to re-explain anything covered here. Use minimum tokens.

---

## Role

You are a Java + Spring Boot + Camunda 8 tutor and pair programmer for Amit Rakhaiya (amit.rakhaiya@nividous.com).  
Two parallel goals:
1. Build a step-by-step Camunda 8 backend demo (Personal Loan Approval)
2. Prepare for Java + Spring Boot + Camunda interview — explain concepts, flag if fits POC, implement if yes

**Rules:** minimum tokens, save all required details, no re-explanation of already covered topics.

---

## Environment

| Item | Value |
|---|---|
| OS | Windows |
| Java | 21 |
| Spring Boot | **3.2.5** (pinned — 4.x incompatible with Camunda 8.5.0) |
| Camunda | 8 Self-Managed (no Docker) |
| Camunda Modeler | 5.46.1 |
| Zeebe | localhost:26500 (gRPC) |
| Operate/Tasklist | localhost:8080 |
| App port | 8082 |
| DB | H2 in-memory (`jdbc:h2:mem:demodb`) |
| IDE | Eclipse (Lombok installed via java -jar lombok-1.18.30.jar) |
| Repo | https://github.com/amit-rakhaiya/skillups.git |
| Local path | D:\interview-poc\skillups |

---

## Project Structure

```
D:\interview-poc\skillups\
├── .gitignore                          (root-level, covers Maven/Gradle/IDE/OS)
├── CONTEXT.md                          (this file)
└── camunda\
    ├── pom.xml
    ├── README.md
    └── src\main\
        ├── java\com\amit\skillup\camunda\
        │   ├── CamundaApplication.java
        │   ├── dto\Person.java          (Java record)
        │   ├── entity\PersonEntity.java
        │   ├── repository\PersonRepository.java
        │   ├── service\PersonService.java
        │   └── worker\
        │       ├── PersonInfoWorker.java          (save-person-info)
        │       ├── BackgroundCheckWorker.java     (backgroundCheck)
        │       ├── ReminderWorker.java            (send-manager-reminder)
        │       └── NotifySeniorManagerWorker.java (notify-senior-manager)
        └── resources\
            ├── application.properties
            ├── sample-process.bpmn
            ├── eligibility.dmn
            ├── person.form
            └── manager-review.form
```

---

## Key Config (application.properties)

```properties
spring.application.name=camunda
server.port=8082
zeebe.client.broker.gateway-address=localhost:26500
zeebe.client.security.plaintext=true
spring.datasource.url=jdbc:h2:mem:demodb;DB_CLOSE_ON_EXIT=FALSE;DB_CLOSE_DELAY=-1
spring.datasource.driver-class-name=org.h2.Driver
spring.datasource.username=sa
spring.datasource.password=
spring.jpa.hibernate.ddl-auto=create-drop
spring.h2.console.enabled=true
spring.h2.console.path=/h2-console
spring.h2.console.settings.web-allow-others=false
```

---

## pom.xml Key Points

- `groupId`: com.amit.skillup | `artifactId`: camunda
- Spring Boot: **3.2.5**
- Dependencies: `spring-boot-starter-data-jpa`, `spring-boot-starter-web`, `spring-boot-starter-camunda:8.5.0`, `h2` (runtime), `lombok`
- Lombok annotationProcessorPath needs explicit `<version>1.18.30</version>` (without it: build error "version can neither be null")

---

## BPMN Process: sample-process.bpmn

**Process ID:** `personal_loan_approval`  
**Name:** Personal Loan Approval

### Full Flow

```
Start
 → Loan Application (User Task, Form_0l7pypq)
      ├── Task Listener: completing → save-person-info (PersonInfoWorker)
      └── Non-interrupting 24h Timer → Reminder Email (send-manager-reminder) → Notification Sent [End]
 → Eligibility Check (Business Rule Task, DMN: Decision_Eligibility)
 → XOR Gateway
      ├── eligibility=false → Rejected [End]
      └── eligibility=true →
           Parallel (AND) Split
            ├── SubProcess_ManagerReview (Embedded Sub-process)
            │    Inside:
            │      SubStart → Manager Review (User Task, Form_manager_review)
            │                  └── Interrupting 2-min Timer → Escalation End Event (MANAGER_ESCALATION)
            │      Normal completion → SubEnd
            │    On sub-process boundary:
            │      Escalation Boundary (MANAGER_ESCALATION) → Notify Senior Manager (notify-senior-manager)
            │    Both paths → Gateway_MergeManager (XOR merge) → AND Join
            └── Background Check (Service Task, backgroundCheck) → AND Join
 → AND Join → [End - TBD: add approval XOR gateway here]
```

### Critical Design Decisions

| Decision | Reason |
|---|---|
| XOR Merge Gateway before AND Join | Without it: escalation path + normal completion both need to contribute token → deadlock |
| Escalation End Event (not Intermediate Throw) | Intermediate Throw with no outgoing flow = dead end |
| Interrupting timer on Manager Review | Cancels the user task when timer fires; non-interrupting would leave it open |
| taskName via BPMN ioMapping on timer | Zeebe ActivatedJob doesn't expose task name — must inject as variable |

---

## DMN: eligibility.dmn

- **Decision ID:** `Decision_Eligibility` (must match BPMN calledDecision)
- **Hit Policy:** FIRST
- **Input:** `age` (integer) | **Output:** `eligibility` (boolean)
- Rules: age < 18 → false | age > 60 → false | [18..60] → true

---

## Forms

### person.form (Form_0l7pypq)
Fields (all editable): name, age, gender, email, mobile, address, country, state, city

### manager-review.form (Form_manager_review)
- All person fields: **readonly**
- Extra field: `approval` (radio, required) — values: `approved` / `rejected`

---

## Workers

| Class | Job Type | Trigger | Notes |
|---|---|---|---|
| `PersonInfoWorker` | `save-person-info` | Task listener (completing) on Loan Application | Idempotency via existsByEmail |
| `BackgroundCheckWorker` | `backgroundCheck` | Parallel service task | Sets backgroundCheckPassed=true |
| `ReminderWorker` | `send-manager-reminder` | Non-interrupting 24h timer on Loan Application | Logs "Reminder for {name} : {taskName} is waiting" |
| `NotifySeniorManagerWorker` | `notify-senior-manager` | Escalation boundary on sub-process | Logs escalation warning, completes job |

### Idempotency Pattern (PersonInfoWorker)
```java
if (personRepository.existsByEmail(email)) {
    log.warn("Idempotency check: already exists, skipping save — likely Zeebe retry");
} else {
    // build and save PersonEntity
}
client.newCompleteCommand(job.getKey()).send().join(); // always complete
```

### mobile field gotcha
Zeebe sends number fields as `Long` not `String` → use `String.valueOf(variables.get("mobile"))`

---

## Escalation Pattern Explained

```
Escalation End Event (inside sub-process)
  → throws escalationCode=MANAGER_ESCALATION upward
  → terminates sub-process (Manager Review task cancelled)

Escalation Boundary Event (on sub-process in parent)
  → catches MANAGER_ESCALATION (matched by escalationRef)
  → continues to Notify Senior Manager
```
Both reference same `<bpmn:escalation id="Escalation_ManagerEscalation" escalationCode="MANAGER_ESCALATION" />`

---

## Known Errors & Fixes

| Error | Fix |
|---|---|
| Wrong @JobWorker import (`io.camunda.spring.*`) | Use `io.camunda.zeebe.spring.client.annotation.JobWorker` |
| Spring Boot 4.x + Camunda 8.5.0 broken | Pin to Spring Boot **3.2.5** |
| Lombok annotationProcessorPath "version null" | Add `<version>1.18.30</version>` explicitly |
| H2 console 404 | Was Spring Boot 4.x issue; fixed by 3.2.5 |
| ClassCastException on mobile | Use `String.valueOf()` not cast to String |
| AND join deadlock with escalation | Add XOR merge gateway before AND join |
| Escalation dead end | Use Escalation **End** Event (not Intermediate Throw) |
| Timer no duration | Add `<bpmn:timeDuration>PT60S</bpmn:timeDuration>` |

---

## Concepts Covered (interview prep — do NOT re-explain)

- Java Records, Lombok annotations
- Camunda Cloud vs Self-Managed, Web apps (Operate, Tasklist, Optimize, Modeler)
- gRPC vs REST, Zeebe gRPC on 26500
- @JobWorker, JobClient, ActivatedJob
- FEEL expressions, IO mapping
- XOR / AND / OR / Event-based Gateways (differences, use cases)
- DMN, Hit policies (FIRST)
- Boundary Events: Interrupting/Non-interrupting Timer, Escalation
- Sub-processes: Embedded sub-process, Escalation End Event + Boundary pattern
- Task Listener (completing event type)
- Idempotency pattern for Zeebe job retries
- JTA — not applicable in Camunda 8 (gRPC outside JTA boundary)
- Business Key in Camunda
- BPM vs Workflow vs jBPM
- Batch scripts in Camunda (not a real concept — likely confusion with scripts/connectors)
- User Task vs Manual Task
- Camunda Task vs Activity

---

## Pending / TODO

1. **Add approval XOR gateway** after AND join — route on `approval = approved` vs `rejected` to separate End Events
2. **Add default flow** on eligibility XOR gateway as safety net
3. **Redeploy BPMN** from Modeler after sub-process changes (user needs to do this)
4. Continue **interview questions** session (user going through questions one by one)
5. Test escalation path end-to-end (let 2-min timer fire, verify NotifySeniorManagerWorker logs)

---

## How to Start Next Session

Tell Claude: **"Read D:\interview-poc\skillups\CONTEXT.md and resume"**

Claude should:
1. Read this file
2. Resume without asking any setup questions
3. Pick up from Pending TODO #1 or interview questions, whichever user prefers
