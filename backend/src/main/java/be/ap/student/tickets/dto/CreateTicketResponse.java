package be.ap.student.tickets.dto;

public class CreateTicketResponse {
    private final String ticketNumber;
    private final String status;

    public CreateTicketResponse(String ticketNumber, String status) {
        this.ticketNumber = ticketNumber;
        this.status = status;
    }

    public String getTicketNumber() { return ticketNumber; }
    public String getStatus() { return status; }
}