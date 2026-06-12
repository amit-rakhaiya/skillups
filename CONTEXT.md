# Session Context — Amit Rakhaiya Interview Prep

## How to Resume (for any AI assistant)
Read this file completely before responding. Do not ask the user to re-explain anything here.
Use minimum tokens. Be concise and direct.

---

## Who
**Amit Rakhaiya** — Product Architect, 13+ yrs, Ahmedabad India
Email: amit.rakhaiya@nividous.com | amit.rakhaiya@gmail.com
Skills: Java, Spring Boot, Camunda BPM, Microservices, Kafka, AWS, Docker

---

## What We're Doing

**Two parallel tracks:**

### Track 1 — Interview Q&A (any technology)
- User pastes a question → AI explains it concisely
- If Amit approves the answer → AI appends it to the correct HTML file
- Each technology has its own HTML file in `D:\interview-poc\skillups\`
- If the topic has no HTML file yet → create one following the existing style
- After updating HTML → commit and remind user to `git push origin main`

### Track 2 — Camunda 8 POC (Personal Loan Approval)
- Live Spring Boot + Camunda 8 backend project
- Explain concepts + implement in POC when applicable

---

## Interview HTML Files

| File | Topic | Status |
|---|---|---|
| `index.html` | Home page — Amit's intro, search, topic tiles | ✅ Done |
| `spring-boot-interview.html` | Spring Boot | ✅ Exists |
| `apache-kafka-interview.html` | Apache Kafka | ✅ Exists |
| `microservices-interview.html` | Microservices | ✅ Exists |
| `camunda-interview-questions.html` | Camunda BPM (24 Qs) | ✅ Done |
| `java-interview-questions.html` | Java 11-21, Collections, Concurrency (13 Qs) | ✅ Done |
| `site.css` | Shared nav + styles | ✅ Done |

**Tag classes for Java page:**
- `.tag-java` · `.tag-java11` · `.tag-java14` · `.tag-java16` · `.tag-java17` · `.tag-java21` · `.tag-collections` · `.tag-concurrency`
- Use `<span class="q-tag tag-java">Java</span>` etc.

**Shared style rules (all HTML files must follow):**
- `<link rel="stylesheet" href="site.css">` in `<head>`
- Nav bar: `<nav class="site-nav">` immediately after `<body>`
- Nav links: Home, Spring Boot, Kafka, Microservices, Camunda (add new topics here too)
- Question card: `<div class="q-card" id="qN">` with `.q-num`, `.q-title`, `.q-tags`
- Code: `<pre><code>...</code></pre>`
- Tables: `<table class="data-table">`
- Interview tip: `<div class="interview-tip">`
- Accent color for Camunda: `#ea580c`

---

## Camunda POC — Project

**Repo:** https://github.com/amit-rakhaiya/skillups.git
**Local:** `D:\interview-poc\skillups\camunda\`
**Package:** `com.amit.skillup.camunda`

### Environment
| Item | Value |
|---|---|
| Java | 21 |
| Spring Boot | **3.2.5** (pinned — 4.x breaks Camunda 8.5.0) |
| Camunda | 8 Self-Managed, no Docker |
| Zeebe | localhost:26500 (gRPC) |
| Operate/Tasklist | localhost:8080 |
| App port | 8082 |
| DB | H2 in-memory (`jdbc:h2:mem:demodb`) |
| IDE | Eclipse + Lombok 1.18.30 |

### Key Files
```
camunda/src/main/
  java/com/amit/skillup/camunda/
    dto/Person.java                  (Java record)
    entity/PersonEntity.java
    repository/PersonRepository.java (existsByEmail for idempotency)
    service/PersonService.java
    worker/
      PersonInfoWorker.java          (save-person-info — task listener)
      BackgroundCheckWorker.java     (backgroundCheck — service task)
      ReminderWorker.java            (send-manager-reminder — timer)
      NotifySeniorManagerWorker.java (notify-senior-manager — escalation)
  resources/
    application.properties
    sample-process.bpmn
    eligibility.dmn                  (Decision_Eligibility, FIRST hit policy, age→eligibility)
    person.form                      (Form_0l7pypq)
    manager-review.form              (Form_manager_review — person fields readonly, approval radio)
```

### Process Flow (sample-process.bpmn)
```
Start → Loan Application (User Task, save-person-info listener)
      → Eligibility Check (Business Rule Task, DMN)
      → XOR: eligibility=false → Rejected End
             eligibility=true →
               AND Split:
                 ├─ SubProcess_ManagerReview
                 │    [Manager Review] ──2min timer──▶ Escalation End (MANAGER_ESCALATION)
                 │    Escalation Boundary → Notify Senior Manager → XOR Merge
                 │    Normal End → XOR Merge
                 │    XOR Merge → AND Join
                 └─ Background Check → AND Join
               AND Join → End
      Non-interrupting 24h timer on Loan Application → send-manager-reminder → End
```

### Known Gotchas
- Lombok annotationProcessorPath needs explicit `<version>1.18.30</version>`
- mobile field: use `String.valueOf()` not cast — Zeebe sends numbers as Long
- @JobWorker import: `io.camunda.zeebe.spring.client.annotation.JobWorker`
- AND join deadlock: always use XOR merge before AND join when escalation is involved
- Escalation must be End Event inside sub-process (not Intermediate Throw)

### Pending POC Tasks
1. Add approval XOR gateway after AND join (route on approval=approved/rejected)
2. Add default flow on eligibility XOR as safety net
3. Test escalation path end-to-end (let 2-min timer fire)

---

## Camunda Questions Already in HTML (do not re-add)
Q1 Web Apps, Q2 Cloud vs Self-Managed, Q3 C7 vs C8, Q4 BPM/Workflow/jBPM,
Q5 Job Worker, Q6 gRPC vs REST, Q7 Ways to Start Process, Q8 Task vs Activity,
Q9 User Task vs Manual Task, Q10 Gateways, Q11 DMN, Q12 Pool & Lane,
Q13 Intermediate Events, Q14 Boundary Events, Q15 Sub-process & Escalation,
Q16 Execution Scope, Q17 Process vs Multi-Instance Variables, Q18 JTA,
Q19 Idempotency, Q20 Business Key, Q21 Connectors, Q22 Custom Form Fields,
Q23 Business Objects, Q24 Suspended Process Start

---

## Workflow for New Questions

1. User asks question (any tech)
2. AI explains concisely
3. If approved:
   - Find or create the right HTML file
   - Append as next numbered `q-card` with proper tags, code, tables
   - Commit: `git commit -m "Add Q: <question summary>"`
   - Tell user to `git push origin main`
4. Update this CONTEXT.md if new topic HTML file was created
