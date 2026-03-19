package be.ap.student.tickets.service;

/**
 * Evaluates whether a support ticket should be escalated based on its
 * priority and the number of times it has been reopened.
 *
 * Deliberate SonarQube issue:
 *  - java:S112  RuntimeException should not be thrown — use a specific subclass.
 */
public class EscalationService {

    private static final int REOPEN_THRESHOLD = 3;

    /**
     * Returns true when the ticket warrants escalation.
     *
     * @param priority   ticket priority: "LOW", "MEDIUM", "HIGH", "CRITICAL"
     * @param reopenCount number of times the ticket has been reopened
     * @throws RuntimeException if the priority value is unrecognised   // java:S112
     */
    public boolean shouldEscalate(String priority, int reopenCount) {
        if (priority == null || priority.isBlank()) {
            throw new RuntimeException("Priority must not be blank");  // java:S112
        }
        return switch (priority.toUpperCase()) {
            case "CRITICAL" -> true;
            case "HIGH"     -> reopenCount >= 1;
            case "MEDIUM"   -> reopenCount >= REOPEN_THRESHOLD;
            case "LOW"      -> false;
            default -> throw new RuntimeException("Unknown priority: " + priority);  // java:S112
        };
    }

    /**
     * Returns a human-readable escalation message for the given priority.
     */
    public String escalationMessage(String priority) {
        return switch (priority.toUpperCase()) {
            case "CRITICAL" -> "Immediately escalate to on-call engineer.";
            case "HIGH"     -> "Escalate within 1 hour.";
            case "MEDIUM"   -> "Escalate within 4 hours.";
            case "LOW"      -> "No escalation required.";
            default -> throw new RuntimeException("Unknown priority: " + priority);  // java:S112
        };
    }
}
