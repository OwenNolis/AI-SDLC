package be.ap.student.tickets.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public class CreateTicketRequest {

    @NotBlank
    @Size(min = 5, max = 120)
    private String subject;

    @NotBlank
    @Size(min = 20, max = 2000)
    private String description;

    @NotNull
    private String priority;

    public CreateTicketRequest() {}

    public String getSubject() { return subject; }
    public void setSubject(String subject) { this.subject = subject; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public String getPriority() { return priority; }
    public void setPriority(String priority) { this.priority = priority; }
}