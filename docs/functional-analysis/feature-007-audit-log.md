# Feature-007: Audit log voor gebruikersacties

## Doel
Als systeem wil ik elke relevante gebruikersactie als event vastleggen zodat beheerders een volledige audittrail kunnen raadplegen.

## Scope

In scope:
- Event publiceren bij inloggen en uitloggen van een gebruiker
- Event publiceren bij aanmaken, wijzigen en verwijderen van een resource
- Audit log consumer slaat events op in een audit-database
- Retry logica bij mislukte opslag
- Dead letter queue voor onverwerkte events

Out of scope:
- UI voor het raadplegen van de audit log
- Exporteren van audit logs naar externe systemen
- Real-time alerting op basis van audit events
- Compliancerapportage

## Requirements
- REQ-001: Bij elke inlogpoging (geslaagd of mislukt) wordt een UserLoginEvent gepubliceerd.
- REQ-002: Bij uitloggen wordt een UserLogoutEvent gepubliceerd.
- REQ-003: Bij het aanmaken van een resource wordt een ResourceCreatedEvent gepubliceerd.
- REQ-004: Bij het wijzigen van een resource wordt een ResourceUpdatedEvent gepubliceerd met het oude en nieuwe veld.
- REQ-005: Bij het verwijderen van een resource wordt een ResourceDeletedEvent gepubliceerd.
- REQ-006: De AuditLogConsumer verwerkt alle events en slaat ze op in de audit-database.
- REQ-007: Elk audit event bevat een correlationId, userId, timestamp en actieomschrijving.
- REQ-008: Bij een mislukte opslag wordt het event opnieuw geprobeerd met exponential backoff (max 3 pogingen).
- REQ-009: Events die na 3 pogingen niet verwerkt kunnen worden, gaan naar de dead letter queue.

## Business rules
- BR-001: Audit events zijn immutable — ze mogen nooit gewijzigd of verwijderd worden.
- BR-002: Elk event krijgt een unieke eventId (UUID) toegewezen bij publicatie.
- BR-003: Events zijn idempotent — dubbele verwerking heeft geen side-effects dankzij eventId deduplicatie.

## Non-functional
- NFR-001: Event publicatie mag de response tijd van de primaire API niet met meer dan 20ms verhogen.
- NFR-002: De consumer moet minimaal 500 events per seconde kunnen verwerken.
- NFR-003: Audit logs moeten minimaal 2 jaar bewaard worden.

## Events
- Event: UserLoginEvent, trigger: gebruiker logt in (geslaagd of mislukt), payload: eventId, userId, email, success, ipAddress, timestamp
- Event: UserLogoutEvent, trigger: gebruiker logt uit, payload: eventId, userId, timestamp
- Event: ResourceCreatedEvent, trigger: resource aangemaakt, payload: eventId, userId, resourceType, resourceId, timestamp
- Event: ResourceUpdatedEvent, trigger: resource gewijzigd, payload: eventId, userId, resourceType, resourceId, changedFields, timestamp
- Event: ResourceDeletedEvent, trigger: resource verwijderd, payload: eventId, userId, resourceType, resourceId, timestamp

## Queues / Topics
- Topic: `audit-user-actions`, producer: AuthService, consumer: AuditLogConsumer
- Topic: `audit-resource-actions`, producer: ResourceService, consumer: AuditLogConsumer
- DLQ: `audit-dlq`, consumer: DeadLetterHandler

## Error handling
- Dead letter queue: ja — `audit-dlq`
- Retry strategie: exponential backoff, 1s / 2s / 4s, max 3 pogingen
