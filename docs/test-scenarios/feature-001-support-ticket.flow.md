# Flow Tests — Feature-001 Support Ticket

## FLOW-001: Happy path — ticket aanmaken

Precondities:

- Gebruiker is ingelogd
- API beschikbaar

Stappen:

1) user: Navigeer naar /tickets/new
   - expected: Form zichtbaar met subject/description/priority, submit disabled
2) user: Vul subject "Cannot login to portal"
   - expected: subject valid
3) user: Vul description (>=20 chars)
   - expected: description valid
4) user: Kies priority "HIGH"
   - expected: priority valid, submit enabled
5) user: Klik Submit
   - expected: loading indicator
   - apiCalls: POST /api/tickets
6) system: API antwoord 201 met ticketNumber en status OPEN
   - expected: success view met ticketNumber + status OPEN

Observability:

- Correlation id aanwezig in logs; ticket created log bevat ticketNumber

## Variants

### Negative: subject te kort

- Vul subject "abc"
- expected: field error subject, submit disabled

### Negative: server error

- Bij 500 response:
- expected: generieke fout + correlation id zichtbaar in UI
