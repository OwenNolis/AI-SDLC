# Feature-007: Audit log voor gebruikersacties

## 1. Scope

### In Scope
*   Publiceren van een audit event bij succesvolle en mislukte inlogpogingen van een gebruiker.
*   Publiceren van een audit event bij het uitloggen van een gebruiker.
*   Publiceren van een audit event bij het aanmaken van een resource.
*   Publiceren van een audit event bij het wijzigen van een resource.
*   Publiceren van een audit event bij het verwijderen van een resource.
*   Implementatie van een audit log consumer die de gepubliceerde events ontvangt.
*   Opslag van de ontvangen audit events in een dedicated audit-database.
*   Implementatie van retry logica binnen de audit log consumer voor het geval van tijdelijke opslagfouten.
*   Configuratie van een dead letter queue (DLQ) voor audit events die na meerdere retries niet succesvol verwerkt kunnen worden.

### Out of Scope
*   Ontwikkeling van een gebruikersinterface (UI) voor het doorzoeken, filteren en visualiseren van de audit log.
*   Functionaliteit voor het exporteren van audit logs naar externe systemen (bijv. SIEM-systemen, cloud storage).
*   Implementatie van real-time alerting mechanismen gebaseerd op specifieke audit events.
*   Ontwikkeling van specifieke functionaliteit voor compliancerapportage (bijv. genereren van rapporten voor specifieke regelgeving).

## 2. Assumptions

*   Er is een bestaand berichtensysteem (bijv. Kafka, RabbitMQ) beschikbaar voor het publiceren en consumeren van audit events.
*   De structuur van de audit events (payload) is gedefinieerd en wordt gecommuniceerd tussen de event-producers en de audit log consumer.
*   Er is een geschikte database-technologie beschikbaar en geconfigureerd voor de audit-database.
*   Authenticatie en autorisatie voor het publiceren van events zijn reeds afgehandeld door de producerende services.
*   De infrastructuur voor het hosten van de audit log consumer en de audit-database is beschikbaar.

## 3. Open Questions

*   Welk berichtensysteem zal gebruikt worden voor de event-publicatie?
*   Wat is de exacte definitie van de audit event payload (bijv. welke velden moeten minimaal aanwezig zijn: timestamp, user ID, action type, resource ID, resource type, success/failure status, IP address, etc.)?
*   Welke database technologie zal gebruikt worden voor de audit-database en wat zijn de performance- en schaalbaarheidsvereisten?
*   Wat is de maximale retry-periode en het aantal retries voor de audit log consumer voordat een event naar de DLQ wordt gestuurd?
*   Hoe zal de DLQ geconfigureerd en gemonitord worden?
*   Zijn er specifieke beveiligingseisen voor de audit-database (bijv. encryptie, toegangscontrole)?
*   Hoe wordt omgegaan met het loggen van gevoelige informatie in de audit events?
*   Welke logging- en monitoringtools zullen gebruikt worden voor de audit log consumer en de audit-database?

## 4. Domain Model

### UserLoginEvent

| Veld | Type | Constraints | Testcases |
|---|---|---|---|
| eventId | UUID | notNull | missing, invalid_value |
| correlationId | UUID | notNull | missing, invalid_value |
| userId | String | notNull, minLength:1 | missing, empty, invalid_value |
| timestamp | LocalDateTime | notNull | missing, invalid_value |
| loginSuccess | Boolean | notNull | missing, invalid_value |
| createdAt | LocalDateTime | notNull | missing, invalid_value |

### UserLogoutEvent

| Veld | Type | Constraints | Testcases |
|---|---|---|---|
| eventId | UUID | notNull | missing, invalid_value |
| correlationId | UUID | notNull | missing, invalid_value |
| userId | String | notNull, minLength:1 | missing, empty, invalid_value |
| timestamp | LocalDateTime | notNull | missing, invalid_value |
| createdAt | LocalDateTime | notNull | missing, invalid_value |

### ResourceCreatedEvent

| Veld | Type | Constraints | Testcases |
|---|---|---|---|
| eventId | UUID | notNull | missing, invalid_value |
| correlationId | UUID | notNull | missing, invalid_value |
| userId | String | notNull, minLength:1 | missing, empty, invalid_value |
| timestamp | LocalDateTime | notNull | missing, invalid_value |
| resourceType | String | notNull, minLength:1 | missing, empty, invalid_value |
| resourceId | String | notNull, minLength:1 | missing, empty, invalid_value |
| createdAt | LocalDateTime | notNull | missing, invalid_value |

### ResourceUpdatedEvent

| Veld | Type | Constraints | Testcases |
|---|---|---|---|
| eventId | UUID | notNull | missing, invalid_value |
| correlationId | UUID | notNull | missing, invalid_value |
| userId | String | notNull, minLength:1 | missing, empty, invalid_value |
| timestamp | LocalDateTime | notNull | missing, invalid_value |
| resourceType | String | notNull, minLength:1 | missing, empty, invalid_value |
| resourceId | String | notNull, minLength:1 | missing, empty, invalid_value |
| oldFieldValue | String |  | invalid_value |
| newFieldValue | String |  | invalid_value |
| fieldName | String | notNull, minLength:1 | missing, empty, invalid_value |
| createdAt | LocalDateTime | notNull | missing, invalid_value |

### ResourceDeletedEvent

| Veld | Type | Constraints | Testcases |
|---|---|---|---|
| eventId | UUID | notNull | missing, invalid_value |
| correlationId | UUID | notNull | missing, invalid_value |
| userId | String | notNull, minLength:1 | missing, empty, invalid_value |
| timestamp | LocalDateTime | notNull | missing, invalid_value |
| resourceType | String | notNull, minLength:1 | missing, empty, invalid_value |
| resourceId | String | notNull, minLength:1 | missing, empty, invalid_value |
| createdAt | LocalDateTime | notNull | missing, invalid_value |

### AuditLog

| Veld | Type | Constraints | Testcases |
|---|---|---|---|
| id | Long | notNull | missing, invalid_value |
| eventId | UUID | notNull | missing, invalid_value, duplicate_per_day |
| correlationId | UUID | notNull | missing, invalid_value |
| userId | String | notNull, minLength:1 | missing, empty, invalid_value |
| timestamp | LocalDateTime | notNull | missing, invalid_value |
| actionDescription | String | notNull, minLength:1 | missing, empty, invalid_value |
| eventDetails | String |  | invalid_value |
| createdAt | LocalDateTime | notNull | missing, invalid_value |

## 5. Messaging Design

### 5.1 Topics

| Topic Naam       | Producer(s)         | Consumer(s)        | Beschrijving                                                              |
| :--------------- | :------------------ | :----------------- | :------------------------------------------------------------------------ |
| `user-events`    | `UserEventProducer` | `AuditLogConsumer` | Publiceert events gerelateerd aan gebruikersactiviteiten zoals login en logout. |
| `resource-events`| `ResourceEventProducer`| `AuditLogConsumer` | Publiceert events gerelateerd aan resource creatie, update en deletie.   |

### 5.2 Events

| Event Naam           | Trigger                                       | Payload Velden

## 6. Backend Design

De backend architectuur volgt een gelaagde structuur met de volgende componenten:

*   **Controller Layer:** Verantwoordelijk voor het afhandelen van inkomende requests en het routeren naar de juiste service.
*   **Service Layer:** Bevat de business logica en orkestreert de interactie tussen de repository en andere services.
*   **Repository Layer:** Verantwoordelijk voor de data-access logica, zoals interactie met de database.

Daarnaast zijn er specifieke modules voor het afhandelen van events en de audit log functionaliteit.

| Module        | Klasse                       | Verantwoordelijkheid

## 8. Security & Privacy

### 8.1 Authenticatie en Autorisatie

*   **Authenticatie:** De audit logs zelf bevatten geen directe authenticatie-informatie van de gebruiker die de actie uitvoert. De authenticatie van de gebruiker vindt plaats op het moment van de oorspronkelijke actie (inloggen, resource manipulatie). De `userId` in het audit event is een referentie naar de geauthenticeerde gebruiker.
*   **Autorisatie:** De audit log functionaliteit zelf vereist geen specifieke autorisatie. Echter, de toegang tot de audit logs (leesrechten) moet strikt gecontroleerd worden. Alleen geautoriseerde beheerders of security officers mogen audit logs inzien. Dit wordt geregeld via de bestaande autorisatiemechanismen van het systeem, gekoppeld aan specifieke rollen (bv. `AUDIT_VIEWER`).
*   **Privacy:**
    *   **Geen gevoelige data:** Audit events mogen geen direct gevoelige persoonsgegevens bevatten, behalve de `userId` die nodig is voor traceerbaarheid. Velden zoals wachtwoorden, creditcardnummers, etc. mogen *nooit* in audit events worden opgenomen.
    *   **Data minimalisatie:** Alleen de noodzakelijke informatie voor auditdoeleinden wordt gelogd.
    *   **Immutable logs:** De eis dat audit events immutable zijn, draagt bij aan de integriteit en betrouwbaarheid van de logs, wat essentieel is voor security-audits.

### 8.2 Beveiliging van de Audit Database

*   **Toegangscontrole:** De audit database moet beschermd worden met strikte toegangscontroles. Alleen de `AuditLogConsumer` mag schrijftoegang hebben. Leesrechten moeten beperkt blijven tot geautoriseerde systemen of gebruikers.
*   **Encryptie:**
    *   **At Rest:** De audit database moet versleuteld worden opgeslagen (encryption at rest) om te voldoen aan privacy-eisen en bescherming te bieden tegen fysieke datalekken.
    *   **In Transit:** Communicatie tussen de `AuditLogConsumer` en de audit database moet beveiligd zijn met TLS/SSL.
*   **Integriteit:** De immutable aard van de events en het gebruik van UUIDs voor `eventId` helpen de integriteit te waarborgen. Verdere maatregelen zoals cryptografische hashing van logbestanden kunnen overwogen worden voor hogere beveiligingseisen.

## 9. Observability

### 9.1 Logging

De `AuditLogConsumer` zal uitgebreid loggen om de verwerking van events en eventuele problemen te monitoren.

*   **Event Ontvangst:**
    *   **Log Level:** `INFO`
    *   **Bericht:** "Received event of type [EventType] with eventId: [eventId], userId: [userId], correlationId: [correlationId]"
    *   **Voorbeeld:** `INFO - Received event of type UserLoginEvent with eventId: a1b2c3d4-e5f6-7890-1234-567890abcdef, userId: user-123, correlationId: corr-abc123xyz`
*   **Event Verwerking (Succesvol):**
    *   **Log Level:** `INFO`
    *   **Bericht:** "Successfully processed and stored event [eventId] for userId: [userId]. Action: [ActionDescription]"
    *   **Voorbeeld:** `INFO - Successfully processed and stored event a1b2c3d4-e5f6-7890-1234-567890abcdef for userId: user-123. Action: User logged in successfully.`
*   **Event Verwerking (Mislukt - Poging X):**
    *   **Log Level:** `WARN`
    *   **Bericht:** "Failed to store event [eventId] for userId: [userId] (Attempt X of 3). Error: [ErrorMessage]"
    *   **Voorbeeld:** `WARN - Failed to store event a1b2c3d4-e5f6-7890-1234-567890abcdef for userId: user-123 (Attempt 1 of 3). Error: Database connection timed out.`
*   **Event Verwerking (Mislukt - Na 3 pogingen):**
    *   **Log Level:** `ERROR`
    *   **Bericht:** "Event [eventId] for userId: [userId] failed to store after 3 attempts. Moving to Dead Letter Queue. Error: [ErrorMessage]"
    *   **Voorbeeld:** `ERROR - Event a1b2c3d4-e5f6-7890-1234-567890abcdef for userId: user-123 failed to store after 3 attempts. Moving to Dead Letter Queue. Error: Persistent storage failure.`
*   **Dead Letter Queue (DLQ) Opslag:**
    *   **Log Level:** `ERROR`
    *   **Bericht:** "Event [eventId] moved to Dead Letter Queue."
    *   **Voorbeeld:** `ERROR - Event a1b2c3d4-e5f6-7890-1234-567890abcdef moved to Dead Letter Queue.`
*   **Consumer Start/Stop:**
    *   **Log Level:** `INFO`
    *   **Bericht:** "AuditLogConsumer started." / "AuditLogConsumer stopped."

### 9.2 Metrics

*   **`audit_events_received_total`**: Counter voor het aantal ontvangen audit events.
*   **`audit_events_processed_total`**: Counter voor het aantal succesvol verwerkte en opgeslagen audit events.
*   **`audit_events_failed_total`**: Counter voor het aantal audit events dat niet succesvol verwerkt kon worden na de maximale pogingen.
*   **`audit_events_in_dlq_total`**: Gauge voor het huidige aantal events in de Dead Letter Queue.
*   **`audit_event_processing_latency_seconds`**: Histogram/Summary voor de verwerkingstijd van een audit event (van ontvangst tot opslag).
*   **`audit_database_write_errors_total`**: Counter voor het aantal fouten bij het schrijven naar de audit database.
*   **`audit_consumer_throughput_events_per_second`**: Gauge/Meter om de doorvoer van de consumer te monitoren (moet > 500 zijn).

### 9.3 Correlation ID

*   **Gebruik:** De `correlationId` wordt bij elke event publicatie meegegeven. De `AuditLogConsumer` zal deze `correlationId` overnemen en opslaan in de audit log.
*   **Doel:** Dit maakt het mogelijk om alle gerelateerde acties van een specifieke transactie of gebruikerssessie te traceren, zelfs over verschillende services heen. Bij het debuggen van een probleem kan men zoeken op `correlationId` om alle events te zien die deel uitmaakten van die specifieke operatie.
*   **Implementatie:** De `correlationId` wordt gegenereerd door de service die de oorspronkelijke actie initieert en wordt doorgegeven via de event bus. De `AuditLogConsumer` leest deze `correlationId` uit het event en slaat deze op.

## 10. Performance & Scalability

### 10.1 Performance-eisen

*   **Event Publicatie:** De publicatie van een audit event mag de response tijd van de primaire API niet met meer dan 20ms verhogen. Dit impliceert dat de event publicatie asynchroon moet gebeuren, bijvoorbeeld via een message queue.
*   **Consumer Doorvoer:** De `AuditLogConsumer` moet minimaal 500 events per seconde kunnen verwerken. Dit is een kritieke eis voor de schaalbaarheid van het audit log systeem.
*   **Opslag:** De audit database moet in staat zijn om de verwachte hoeveelheid data te schrijven met acceptabele latency.

### 10.2 Database-indexen

Om efficiënte querying van audit logs mogelijk te maken, zijn de volgende indexen essentieel:

*   **`audit_events` tabel:**
    *   **`eventId` (Primary Key, UUID):** Zorgt voor unieke identificatie en snelle lookup van individuele events.
    *   **`timestamp` (B-tree index):** Cruciaal voor het filteren en sorteren van logs op datum/tijd. Dit is de meest voorkomende query-parameter.
    *   **`userId` (B-tree index):** Essentieel voor het opvragen van alle acties van een specifieke gebruiker.
    *   **`correlationId` (B-tree index):** Nodig voor het traceren van transacties over meerdere events heen.
    *   **`eventType` (B-tree index):** Nuttig voor het filteren op specifieke soorten acties (bv. alleen login events).
    *   **Gecombineerde indexen:**
        *   `(`userId`, `timestamp`)`: Voor het opvragen van de logs van een gebruiker binnen een bepaalde periode.
        *   `(`correlationId`, `timestamp`)`: Voor het opvragen van de logs van een transactie binnen een bepaalde periode.

### 10.3 Schaalbaarheid

*   **Asynchrone Verwerking:** Het gebruik van een message queue (bv. Kafka, RabbitMQ) voor event publicatie en de `AuditLogConsumer` die deze events verwerkt, zorgt voor ontkoppeling en schaalbaarheid. De producer (primaire API) wordt niet geblokkeerd door de audit log verwerking.
*   **Horizontale Schaalbaarheid van de Consumer:** De `AuditLogConsumer` moet horizontaal schaalbaar zijn. Dit betekent dat er meerdere instanties van de consumer kunnen draaien die parallel events uit de message queue verwerken. De message queue moet dit ondersteunen (bv. consumer groups in Kafka).
*   **Database Schaalbaarheid:**
    *   **Keuze van Database:** Een database die goed schaalt voor write-heavy workloads en grote hoeveelheden data is vereist. Opties zoals PostgreSQL met partitioning, Cassandra, of een managed database service met auto-scaling mogelijkheden kunnen overwogen worden.
    *   **Partitioning:** De audit log tabel kan gepartitioneerd worden op basis van `timestamp` (bv. per maand of per jaar) om query performance te verbeteren en het beheer van oude data (retentie) te vereenvoudigen.
    *   **Read Replicas:** Voor leesintensieve operaties (bv. rapportages) kunnen read replicas worden ingezet.
*   **Data Retentie:** De eis van 2 jaar data retentie moet meegenomen worden in de database capaciteitsplanning. Regelmatige archivering of verwijdering van oude logs (conform beleid) is noodzakelijk.
*   **Idempotentie en Deduplicatie:** Het gebruik van `eventId` voor idempotentie en deduplicatie voorkomt dat dubbele verwerking leidt tot inconsistente data, wat cruciaal is voor de betrouwbaarheid van een schaalbaar systeem.
*   **Dead Letter Queue (DLQ):** De DLQ is een mechanisme om te voorkomen dat falende events de verwerking van andere events blokkeren. Een apart proces kan de DLQ monitoren en proberen de events later opnieuw te verwerken of te analyseren. Dit draagt bij aan de veerkracht en schaalbaarheid van het systeem.

## 11. Teststrategie

### Unit Tests

*   **Audit Log Service:**
    *   Test de `logAction` methode met verschillende gebruikersrollen en actietypes om te verifiëren dat de juiste gegevens worden vastgelegd (gebruiker ID, actie, timestamp, details).
    *   Test de foutafhandeling van de `logAction` methode, bijvoorbeeld wanneer de databaseverbinding faalt of ongeldige invoer wordt verstrekt.
    *   Test de methoden voor het ophalen van audit logs, zoals `getLogsByUser(userId)` en `getLogsByAction(actionType)`, om te verifiëren dat de juiste filters correct werken en de verwachte resultaten worden geretourneerd.
    *   Test de methoden voor het filteren van logs op datum/tijd bereik.
    *   Test de beveiligingsaspecten van de log-ophalingsmethoden, zoals het controleren of een gebruiker alleen zijn eigen logs kan inzien (indien van toepassing op basis van autorisatie).
*   **Audit Log Repository:**
    *   Test de interactie met de database voor het opslaan van audit log entries. Gebruik mock-objecten voor de database om de logica van de repository te isoleren.
    *   Test de query's voor het ophalen van audit log entries op basis van verschillende criteria (gebruiker ID, actietype, datum/tijd bereik).
    *   Test de foutafhandeling van de repository-methoden, bijvoorbeeld bij databasefouten.
*   **Controller/API Endpoints:**
    *   Test de API endpoints die verantwoordelijk zijn voor het triggeren van audit logging (bijvoorbeeld `POST /api/users/{userId}/actions`).
    *   Verifieer dat de juiste data wordt doorgestuurd naar de Audit Log Service.
    *   Test de validatie van de inputparameters voor deze endpoints.
    *   Test de autorisatie- en authenticatiecontroles op deze endpoints.

### Integratietests

*   **Audit Log Service met Repository:**
    *   Test de volledige flow van het loggen van een actie: een aanroep naar de `logAction` methode van de service moet resulteren in een correct opgeslagen entry in de database via de repository.
    *   Test het ophalen van logs via de service, waarbij de repository daadwerkelijk data uit de database ophaalt.
*   **API Gateway/Controller met Audit Log Service:**
    *   Test de integratie tussen de API endpoints en de Audit Log Service. Een succesvolle aanroep naar een endpoint dat een audit-relevante actie triggert, moet leiden tot een correcte log-entry die kan worden geverifieerd via de Audit Log Service of de repository.
*   **Integratie met Authenticatie/Autorisatie Service:**
    *   Verifieer dat de Audit Log Service correct de gebruikersidentiteit en autorisatie-informatie ontvangt van de authenticatie/autorisatie service bij het loggen van acties.
    *   Test scenario's waarbij de authenticatie/autorisatie faalt en hoe dit de audit logging beïnvloedt (bijvoorbeeld geen log entry of een specifieke foutmelding).

### End-to-End (E2E) Tests

*   **Gebruikersactie en Audit Log Verificatie:**
    *   Simuleer een volledige gebruikersworkflow, bijvoorbeeld het aanmaken van een nieuwe gebruiker, het wijzigen van een wachtwoord, of het verwijderen van een record.
    *   Na het uitvoeren van de actie, log in als een beheerder of een gebruiker met de juiste rechten en navigeer naar de audit log interface.
    *   Verifieer dat de specifieke actie van de gebruiker correct is vastgelegd in de audit logs, inclusief de juiste gebruiker, actietype, timestamp en relevante details.
*   **Audit Log Filtering en Zoeken:**
    *   Voer een reeks gebruikersacties uit.
    *   Gebruik de audit log interface om te filteren op specifieke gebruikers, actietypes, of datum/tijd bereiken.
    *   Verifieer dat de getoonde logs overeenkomen met de ingestelde filters.
*   **Beveiligingsscenario's:**
    *   Test dat niet-geautoriseerde gebruikers geen toegang hebben tot de audit logs.
    *   Test dat gebruikers alleen de logs kunnen zien waar ze recht op hebben (indien van toepassing).
*   **Foutscenario's in de UI:**
    *   Simuleer situaties waarin de audit log service niet beschikbaar is en verifieer hoe de UI hierop reageert (bijvoorbeeld een duidelijke foutmelding aan de gebruiker).

## 12. Traceability Matrix

| REQ | Backend | Frontend | Tests |
|-----|---------|----------|-------|
| REQ-001 | UserEventProducer, EventPublisher |  | Verifieer dat na een succesvolle en mislukte inlogpoging een UserLoginEvent wordt gepubliceerd. |
| REQ-002 | UserEventProducer, EventPublisher |  | Verifieer dat na uitloggen een UserLogoutEvent wordt gepubliceerd. |
| REQ-003 | ResourceEventProducer, EventPublisher |  | Verifieer dat na het aanmaken van een resource een ResourceCreatedEvent wordt gepubliceerd. |
| REQ-004 | ResourceEventProducer, EventPublisher |  | Verifieer dat na het wijzigen van een resource een ResourceUpdatedEvent wordt gepubliceerd met de oude en nieuwe veldwaarden. |
| REQ-005 | ResourceEventProducer, EventPublisher |  | Verifieer dat na het verwijderen van een resource een ResourceDeletedEvent wordt gepubliceerd. |
| REQ-006 | AuditLogConsumer, AuditLogService, AuditLogRepository |  | Verifieer dat de AuditLogConsumer alle ontvangen events verwerkt en opslaat in de audit-database. |
| REQ-007 | AuditLogConsumer, AuditLogService, AuditLogRepository, AuditLogEntity |  | Verifieer dat elk opgeslagen audit event de velden correlationId, userId, timestamp en actieomschrijving bevat. |
| REQ-008 | RetryTemplateConfig, AuditLogPersistenceService |  | Simuleer een mislukte opslag en verifieer dat het event opnieuw wordt geprobeerd met exponential backoff (max 3 pogingen). |
| REQ-009 | DlqPublisher, RetryTemplateConfig, AuditLogPersistenceService |  | Simuleer een scenario waarbij een event na 3 pogingen niet verwerkt kan worden en verifieer dat het naar de dead letter queue gaat. |
| REQ-010 | AuditLogRepository |  | Verifieer dat audit events niet gewijzigd of verwijderd kunnen worden uit de audit-database. |
| REQ-011 | UUIDGenerator, UserEventProducer, ResourceEventProducer |  | Verifieer dat elk gepubliceerd event een unieke eventId (UUID) heeft. |
| REQ-012 | AuditLogIdempotencyChecker, AuditLogConsumer |  | Verifieer dat het dubbel verwerken van een event met dezelfde eventId geen extra side-effects heeft (bijvoorbeeld door het controleren van de audit-database op duplicaten). |
| REQ-013 | EventPublisher |  | Meet de response tijd van de primaire API tijdens event publicatie en verifieer dat deze niet meer dan 20ms toeneemt. |
| REQ-014 | EventConsumer, AuditLogConsumer |  | Voer een load test uit om te verifiëren dat de consumer minimaal 500 events per seconde kan verwerken. |
| REQ-015 | AuditLogRepository |  | Verifieer dat de configuratie van de audit-database ingesteld is op een bewaartermijn van minimaal 2 jaar. |

