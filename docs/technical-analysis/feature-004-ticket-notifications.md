# Technische Analyse: Feature-004: Ticket notificaties via events

## 1. Scope

### In scope
- Event publiceren bij aanmaken van een ticket.
- Event publiceren bij statuswijziging van een ticket (OPEN → IN_PROGRESS, IN_PROGRESS → CLOSED).
- E-mail notificatie versturen via een consumer.
- Retry logica bij mislukte notificaties.
- Dead letter queue voor onverwerkte events.
- Publicatie van `TicketCreatedEvent` op topic `ticket-created`.
- Publicatie van `TicketStatusChangedEvent` op topic `ticket-status-changed`.
- Verwerking van `TicketCreatedEvent` door `NotificationConsumer` voor bevestigings-e-mails.
- Verwerking van `TicketStatusChangedEvent` door `NotificationConsumer` voor statusupdate-e-mails.
- Implementatie van exponential backoff retry strategie (max 3 pogingen).
- Afhandeling van onverwerkbare events naar de dead letter queue `ticket-notifications-dlq`.
- Monitoring van de DLQ en genereren van alerts.
- Idempotentie van events.
- Beperking van de impact op de response tijd van de ticket API (< 50ms).
- Doorvoercapaciteit van de consumer (minimaal 100 events/seconde).

### Out of scope
- Push notificaties naar de browser.
- SMS notificaties.
- Notificaties bij het verwijderen van tickets.
- Gebruikersinstellingen voor notificatievoorkeuren.

## 2. Assumptions
- Er is een messaging broker (bv. Kafka, RabbitMQ) beschikbaar en geconfigureerd.
- Er is een e-mail verzendingsservice beschikbaar.
- De ticket API is in staat om events te publiceren na succesvolle business operaties.

## 3. Open Questions
- Welke specifieke e-mail templates worden gebruikt voor bevestigings- en statusupdate-e-mails?
- Hoe wordt de `createdByUserId` gemapt naar een e-mailadres?
- Wat is de exacte definitie van "statuswijziging" voor de `TicketStatusChangedEvent` (bv. alleen specifieke statusovergangen)?

## 4. Domain Model

### Entiteiten
| Entiteit | Velden | Constraints |
|---|---|---|
| Ticket | id: Long<br>ticketNumber: String<br>subject: String<br>priority: String<br>createdByUserId: String<br>status: String<br>createdAt: LocalDateTime<br>updatedAt: LocalDateTime | id: notNull<br>ticketNumber: notNull, minLength:1<br>subject: notNull, minLength:1<br>priority: notNull, minLength:1<br>createdByUserId: notNull, minLength:1<br>status: notNull, minLength:1<br>createdAt: notNull<br>updatedAt: notNull |
| TicketCreatedEvent | eventId: UUID<br>timestamp: LocalDateTime<br>ticketId: Long<br>ticketNumber: String<br>subject: String<br>priority: String<br>createdByUserId: String | eventId: notNull<br>timestamp: notNull<br>ticketId: notNull<br>ticketNumber: notNull, minLength:1<br>subject: notNull, minLength:1<br>priority: notNull, minLength:1<br>createdByUserId: notNull, minLength:1 |
| TicketStatusChangedEvent | eventId: UUID<br>timestamp: LocalDateTime<br>ticketId: Long<br>ticketNumber: String<br>previousStatus: String<br>newStatus: String<br>changedAt: LocalDateTime | eventId: notNull<br>timestamp: notNull<br>ticketId: notNull<br>ticketNumber: notNull, minLength:1<br>previousStatus: notNull, minLength:1<br>newStatus: notNull, minLength:1<br>changedAt: notNull |
| Notification | id: Long<br>recipientEmail: String<br>subject: String<br>body: String<br>status: String<br>retryCount: Integer<br>createdAt: LocalDateTime<br>updatedAt: LocalDateTime | id: notNull<br>recipientEmail: notNull, minLength:5<br>subject: notNull, minLength:1<br>body: notNull, minLength:1<br>status: notNull, minLength:1<br>retryCount: notNull, minValue:0<br>createdAt: notNull<br>updatedAt: notNull |
| DeadLetterMessage | id: Long<br>originalTopic: String<br>payload: String<br>errorMessage: String<br>processedAt: LocalDateTime<br>createdAt: LocalDateTime | id: notNull<br>originalTopic: notNull, minLength:1<br>payload: notNull, minLength:1<br>errorMessage: notNull, minLength:1<br>processedAt: notNull<br>createdAt: notNull |

### Events
| Event | Trigger | Payload velden |
|---|---|---|
| TicketCreatedEvent | Succesvolle aanmaak van een ticket in de TicketService. | ticketId, ticketNumber, subject, priority, createdByUserId |
| TicketStatusChangedEvent | Succesvolle wijziging van de status van een ticket in de TicketService. | ticketId, ticketNumber, previousStatus, newStatus, changedAt |

## 5. Messaging Design

### Topics / Queues
| Topic/Queue | Producer | Consumer | Beschrijving |
|---|---|---|---|
| `ticket-created` | TicketService | NotificationConsumer | Publiceert informatie over nieuw aangemaakte tickets. |
| `ticket-status-changed` | TicketService | NotificationConsumer | Publiceert informatie over statuswijzigingen van tickets. |
| `ticket-notifications-dlq` | NotificationRetryService | DeadLetterHandler | Ontvangt events die niet succesvol verwerkt konden worden na retries. |

### Event schema
```json
{
  "eventType": "{EventNaam}",
  "occurredAt": "ISO-8601 timestamp",
  "payload": {
    "{veld}": "{type}"
  }
}
```

### Error handling
- Dead letter queue: `ticket-notifications-dlq`
- Retry strategie: exponential backoff, max 3 pogingen (intervallen: 1s, 2s, 4s)
- DLQ monitoring: alert bij berichten in DLQ

## 6. Backend Design

### Lagen
- **Producer**: Publiceert events na succesvolle business operaties in de `ticket` module.
- **Consumer**: Verwerkt inkomende events in de `notification` module en voert acties uit (e-mail verzenden).
- **Repository**: Persisteert de verwerkte data (Tickets, Notifications) in de `ticket` en `notification` modules.
- **Common**: Bevat gedeelde functionaliteit zoals `DeadLetterHandler`, `IdempotencyChecker`, `DlqMonitor`.

### Klassen
| Klasse | Verantwoordelijkheid |
|---|---|
| TicketCreatedEventPublisher | Publiceert `TicketCreatedEvent` naar `ticket-created` topic. |
| TicketStatusChangedEventPublisher | Publiceert `TicketStatusChangedEvent` naar `ticket-status-changed` topic. |
| NotificationConsumer | Verwerkt inkomende `TicketCreatedEvent` en `TicketStatusChangedEvent` events. |
| NotificationService | Business logica voor het genereren en versturen van notificaties. |
| NotificationRetryService | Implementeert de retry logica voor mislukte notificaties. |
| NotificationDlqProducer | Publiceert mislukte events naar de `ticket-notifications-dlq`. |
| DeadLetterHandler | Verwerkt berichten uit de `ticket-notifications-dlq`. |
| IdempotencyChecker | Zorgt voor idempotente verwerking van events. |
| DlqMonitor | Monitort de DLQ en genereert alerts. |
| TicketCreatedEvent | Event payload klasse voor ticket creatie. |
| TicketStatusChangedEvent | Event payload klasse voor ticket status wijziging. |
| TicketRepository | Data-access operaties voor Ticket entiteiten. |
| NotificationRepository | Data-access operaties voor Notification entiteiten. |

## 7. Security & Privacy
- Events bevatten geen gevoelige persoonsgegevens tenzij versleuteld.
- Authenticatie via service-to-service credentials.
- E-mailadressen worden alleen gebruikt voor het versturen van notificaties en worden niet opgeslagen in de event payload.

## 8. Observability
- **Logging**: Elk gepubliceerd en ontvangen event met `correlationId` en `eventId`. Logging van succesvolle e-mail verzendingen, retries en DLQ plaatsingen.
- **Metrics**: Events per seconde (gepubliceerd en verwerkt), verwerkingstijd per event, DLQ grootte, retry count per event, e-mail verzendingssucces/falen ratio.
- **Alerting**: Alert bij DLQ-berichten, bij hoge verwerkingstijd (p95 > 5s), bij een hoog percentage mislukte e-mail verzendingen.

## 9. Performance & Scalability
- Consumer horizontaal schaalbaar via consumer groups voor de `ticket-created` en `ticket-status-changed` topics.
- Idempotente verwerking — dubbele events hebben geen side-effects.
- Backpressure mechanisme bij hoge load wordt geïmplementeerd door de messaging broker en de consumer configuratie.
- De `TicketService` publiceert events asynchroon om de response tijd van de ticket API te minimaliseren (< 50ms).

## 10. Test Strategy

### Unit tests
- `TicketCreatedEventPublisher`: verifieer dat events correct worden samengesteld en naar het juiste topic worden gepubliceerd.
- `TicketStatusChangedEventPublisher`: verifieer dat events correct worden samengesteld en naar het juiste topic worden gepubliceerd.
- `NotificationConsumer`: verifieer business logica bij event ontvangst, inclusief correcte aanroep van `NotificationService`.
- `NotificationService`: verifieer de logica voor het genereren van e-mail content en het aanroepen van `EmailSender`.
- `NotificationRetryService`: verifieer de retry logica en de correcte plaatsing in de DLQ na falen.
- `IdempotencyChecker`: verifieer dat dubbele events correct worden afgehandeld.

### Integration tests
- Producer → topic → consumer flow met embedded broker: test de volledige flow van event publicatie tot verwerking door de consumer.
- DLQ flow bij verwerkingsfouten: simuleer fouten in de `NotificationService` en verifieer dat events correct naar de DLQ gaan.
- Retry mechanisme bij tijdelijke fouten: simuleer tijdelijke fouten en verifieer dat de retry logica correct werkt.
- E-mail verzending simulatie: test de integratie met een gesimuleerde e-mail verzendingsservice.

### E2E tests
- Volledige event flow van trigger (ticket aanmaak/statuswijziging) tot verwerkte data in de database (Notification entiteit) en de ontvangst van een gesimuleerde e-mail.
- Test de DLQ monitoring en alerting functionaliteit.

## 11. Traceability Matrix

| REQ | Producer/Consumer | Tests |
|---|---|---|
| REQ-001 | TicketService, TicketCreatedEventPublisher | Test dat bij het aanmaken van een ticket via de API, een TicketCreatedEvent wordt gepubliceerd op het juiste topic. |
| REQ-002 | TicketService, TicketStatusChangedEventPublisher | Test dat bij het wijzigen van de status van een ticket via de API, een TicketStatusChangedEvent wordt gepubliceerd op het juiste topic. |
| REQ-003 | NotificationConsumer, NotificationService, EmailSender | Test dat de NotificationConsumer een TicketCreatedEvent verwerkt en een bevestigings-e-mail verstuurt naar de correcte ontvanger. |
| REQ-004 | NotificationConsumer, NotificationService, EmailSender | Test dat de NotificationConsumer een TicketStatusChangedEvent verwerkt en een statusupdate-e-mail verstuurt. |
| REQ-005 | NotificationRetryService, EmailSender | Test dat bij een mislukte e-mailbezorging, de NotificationRetryService het event opnieuw probeert te verwerken met exponential backoff (max 3 pogingen). |
| REQ-006 | NotificationRetryService, NotificationDlqProducer | Test dat events die na 3 pogingen niet verwerkt kunnen worden, correct naar de dead letter queue worden gestuurd. |
| REQ-007 | DlqMonitor | Test dat de DlqMonitor detecteert wanneer er berichten in de DLQ terechtkomen en een alert genereert. |
| REQ-008 | TicketCreatedEvent | Test dat een TicketCreatedEvent de verplichte velden (ticketId, ticketNumber, subject, priority, createdByUserId) bevat. |
| REQ-009 | TicketStatusChangedEvent | Test dat een TicketStatusChangedEvent de verplichte velden (ticketId, ticketNumber, previousStatus, newStatus, changedAt) bevat. |
| REQ-010 | IdempotencyChecker, NotificationConsumer | Test dat het verwerken van dubbele events geen ongewenste side-effects veroorzaakt. |
| REQ-011 | TicketController, TicketCreatedEventPublisher, TicketStatusChangedEventPublisher | Meet de response tijd van de ticket API bij het aanmaken en updaten van tickets om te verifiëren dat de event publicatie de tijd niet significant verhoogt. |
| REQ-012 | NotificationConsumer | Voer een load test uit op de NotificationConsumer om te verifiëren dat deze minimaal 100 events per seconde kan verwerken. |
| REQ-013 | TicketService, TicketCreatedEventPublisher | Test dat een TicketCreatedEvent wordt gepubliceerd na een succesvolle ticket aanmaak operatie. |
| REQ-014 | TicketService, TicketStatusChangedEventPublisher | Test dat een TicketStatusChangedEvent wordt gepubliceerd na een succesvolle statuswijziging van een ticket. |
| REQ-015 | NotificationConsumer, NotificationService, EmailSender | Test dat de NotificationConsumer een TicketCreatedEvent verwerkt en een bevestigings-e-mail stuurt naar de aanmaker. |
| REQ-016 | NotificationConsumer, NotificationService, EmailSender | Test dat de NotificationConsumer een TicketStatusChangedEvent verwerkt en een statusupdate-e-mail stuurt. |
| REQ-017 | NotificationRetryService, EmailSender | Test dat bij een mislukte e-mailbezorging, de NotificationRetryService de retry strategie met exponential backoff (max 3 pogingen) toepast. |
| REQ-018 | NotificationRetryService, NotificationDlqProducer | Test dat events die na 3 pogingen niet verwerkt kunnen worden, naar de dead letter queue worden gestuurd. |
| REQ-019 | DlqMonitor | Test dat de DlqMonitor een alert genereert wanneer er berichten in de dead letter queue aanwezig zijn. |
| REQ-020 | TicketCreatedEvent | Test dat de payload van een TicketCreatedEvent de velden ticketId, ticketNumber, subject, priority en createdByUserId bevat. |
| REQ-021 | TicketStatusChangedEvent | Test dat de payload van een TicketStatusChangedEvent de velden ticketId, ticketNumber, previousStatus, newStatus en changedAt bevat. |
| REQ-022 | TicketService, TicketCreatedEventPublisher, TicketStatusChangedEventPublisher | Test dat de TicketService events publiceert op de `ticket-created` en `ticket-status-changed` topics. |
| REQ-023 | NotificationConsumer, MessagingConfig | Test dat de NotificationConsumer events consumeert van de `ticket-created` en `ticket-status-changed` topics. |
| REQ-024 | DeadLetterHandler, MessagingConfig | Test dat de DeadLetterHandler events consumeert van de `ticket-notifications-dlq`. |
| REQ-025 | NotificationRetryService | Test dat de retry strategie voor mislukte notificaties de gespecificeerde intervallen van 1s, 2s en 4s hanteert. |
