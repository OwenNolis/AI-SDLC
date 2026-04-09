# Feature-004: Ticket notificaties via events

## Doel
Als systeem wil ik bij elke statuswijziging van een ticket een notificatie-event publiceren zodat externe systemen (e-mail, Slack) op de hoogte gebracht kunnen worden.

## Scope

In scope:
- Event publiceren bij aanmaken van een ticket
- Event publiceren bij statuswijziging van een ticket (OPEN → IN_PROGRESS, IN_PROGRESS → CLOSED)
- E-mail notificatie versturen via een consumer
- Retry logica bij mislukte notificaties
- Dead letter queue voor onverwerkte events

Out of scope:
- Push notificaties naar de browser
- SMS notificaties
- Notificaties bij het verwijderen van tickets
- Gebruikersinstellingen voor notificatievoorkeuren

## Requirements
- REQ-001: Bij het aanmaken van een ticket wordt een TicketCreatedEvent gepubliceerd op het topic `ticket-created`.
- REQ-002: Bij een statuswijziging van een ticket wordt een TicketStatusChangedEvent gepubliceerd op het topic `ticket-status-changed`.
- REQ-003: De NotificationConsumer verwerkt TicketCreatedEvents en verstuurt een bevestigings-e-mail naar de aanmaker.
- REQ-004: De NotificationConsumer verwerkt TicketStatusChangedEvents en verstuurt een statusupdate-e-mail.
- REQ-005: Bij een mislukte e-mailbezorging wordt het event opnieuw geprobeerd met exponential backoff (max 3 pogingen).
- REQ-006: Events die na 3 pogingen niet verwerkt kunnen worden, worden naar de dead letter queue gestuurd.
- REQ-007: De DLQ wordt gemonitord en er wordt een alert gegenereerd als er berichten in de DLQ terechtkomen.

## Business rules
- BR-001: Een TicketCreatedEvent bevat altijd het ticketId, ticketNumber, subject, priority en createdByUserId.
- BR-002: Een TicketStatusChangedEvent bevat altijd het ticketId, ticketNumber, previousStatus, newStatus en changedAt.
- BR-003: Events zijn idempotent — dubbele verwerking heeft geen side-effects.

## Non-functional
- NFR-001: Event publicatie mag de response tijd van de ticket API niet met meer dan 50ms verhogen.
- NFR-002: De consumer moet minimaal 100 events per seconde kunnen verwerken.

## Events
- Event: TicketCreatedEvent, trigger: ticket succesvol aangemaakt, payload: ticketId, ticketNumber, subject, priority, createdByUserId, createdAt
- Event: TicketStatusChangedEvent, trigger: ticket status gewijzigd, payload: ticketId, ticketNumber, previousStatus, newStatus, changedAt

## Queues / Topics
- Topic: `ticket-created`, producer: TicketService, consumer: NotificationConsumer
- Topic: `ticket-status-changed`, producer: TicketService, consumer: NotificationConsumer
- DLQ: `ticket-notifications-dlq`, consumer: DeadLetterHandler

## Error handling
- Dead letter queue: ja — `ticket-notifications-dlq`
- Retry strategie: exponential backoff, 1s / 2s / 4s, max 3 pogingen
