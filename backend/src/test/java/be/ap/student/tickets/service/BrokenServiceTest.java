package be.ap.student.tickets.service;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

public class BrokenServiceTest {

    private final BrokenService brokenService = new BrokenService();

    @Test
    void processData_returnsProcessedStringWithPlaceholderCount() {
        String input = "testInput";
        String result = brokenService.processData(input);
        assertThat(result).isEqualTo("Processed: testInput0");
    }

    @Test
    void getItems_returnsListOfItems() {
        List<String> items = brokenService.getItems();
        assertThat(items).containsExactly("item1", "item2");
    }

    @Test
    void handleRequest_returnsHandledString() {
        String parameter = "testParam";
        String result = brokenService.handleRequest(parameter);
        assertThat(result).isEqualTo("Handled: testParam");
    }

    @Test
    void validateInput_returnsTransformedData() {
        String data = "rawData";
        String result = brokenService.validateInput(data);
        assertThat(result).isEqualTo("Transformed: rawData");
    }
}
