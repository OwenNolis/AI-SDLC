package be.ap.student.tickets.controller;

public class SonarMediumIssueExample {
    public void doSomething() {
        // SonarQube java:S3776 - Cognitive Complexity of methods should not be too high
        int result = 0;
        for (int i = 0; i < 10; i++) {
            if (i % 2 == 0) {
                for (int j = 0; j < 5; j++) {
                    if (j % 2 == 0) {
                        for (int k = 0; k < 3; k++) {
                            if (k % 2 == 0) {
                                result += i + j + k;
                            } else {
                                result -= i + j + k;
                            }
                        }
                    } else {
                        result += j;
                    }
                }
            } else {
                result += i;
            }
        }
        System.out.println(result);
    }
}
