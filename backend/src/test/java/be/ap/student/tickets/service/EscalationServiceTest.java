package be.ap.student.tickets.service;

import org.junit.jupiter.api.Test;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

public class EscalationServiceTest {

    private final EscalationService escalationService = new EscalationService();

    // --- shouldEscalate tests ---

    @Test
    void shouldEscalate_criticalPriority_returnsTrue() {
        assertThat(escalationService.shouldEscalate("CRITICAL", 0)).isTrue();
        assertThat(escalationService.shouldEscalate("critical", 5)).isTrue();
    }

    @Test
    void shouldEscalate_highPriority_reopenCountOne_returnsTrue() {
        assertThat(escalationService.shouldEscalate("HIGH", 1)).isTrue();
    }

    @Test
    void shouldEscalate_highPriority_reopenCountZero_returnsFalse() {
        assertThat(escalationService.shouldEscalate("HIGH", 0)).isFalse();
    }

    @Test
    void shouldEscalate_mediumPriority_reopenCountThreshold_returnsTrue() {
        assertThat(escalationService.shouldEscalate("MEDIUM", 3)).isTrue();
        assertThat(escalationService.shouldEscalate("MEDIUM", 4)).isTrue();
    }

    @Test
    void shouldEscalate_mediumPriority_reopenCountBelowThreshold_returnsFalse() {
        assertThat(escalationService.shouldEscalate("MEDIUM", 2)).isFalse();
    }

    @Test
    void shouldEscalate_lowPriority_returnsFalse() {
        assertThat(escalationService.shouldEscalate("LOW", 0)).isFalse();
        assertThat(escalationService.shouldEscalate("low", 10)).isFalse();
    }

    @Test
    void shouldEscalate_nullPriority_throwsIllegalArgumentException() {
        assertThatThrownBy(() -> escalationService.shouldEscalate(null, 0))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Priority must not be blank");
    }

    @Test
    void shouldEscalate_blankPriority_throwsIllegalArgumentException() {
        assertThatThrownBy(() -> escalationService.shouldEscalate(" ", 0))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Priority must not be blank");
    }

    @Test
    void shouldEscalate_unknownPriority_throwsIllegalArgumentException() {
        assertThatThrownBy(() -> escalationService.shouldEscalate("UNKNOWN", 0))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Unknown priority: UNKNOWN");
    }

    // --- escalationMessage tests ---

    @Test
    void escalationMessage_criticalPriority_returnsCorrectMessage() {
        assertThat(escalationService.escalationMessage("CRITICAL")).isEqualTo("Immediately escalate to on-call engineer.");
        assertThat(escalationService.escalationMessage("critical")).isEqualTo("Immediately escalate to on-call engineer.");
    }

    @Test
    void escalationMessage_highPriority_returnsCorrectMessage() {
        assertThat(escalationService.escalationMessage("HIGH")).isEqualTo("Escalate within 1 hour.");
    }

    @Test
    void escalationMessage_mediumPriority_returnsCorrectMessage() {
        assertThat(escalationService.escalationMessage("MEDIUM")).isEqualTo("Escalate within 4 hours.");
    }

    @Test
    void escalationMessage_lowPriority_returnsCorrectMessage() {
        assertThat(escalationService.escalationMessage("LOW")).isEqualTo("No escalation required.");
    }

    @Test
    void escalationMessage_unknownPriority_throwsIllegalArgumentException() {
        assertThatThrownBy(() -> escalationService.escalationMessage("INVALID"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Unknown priority: INVALID");
    }
}
