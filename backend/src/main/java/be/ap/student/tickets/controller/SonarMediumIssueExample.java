package be.ap.student.tickets.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SonarMediumIssueExample {

    private static final Logger log = LoggerFactory.getLogger(SonarMediumIssueExample.class);

    public void doSomething() {
        // SonarQube java:S3776 - Cognitive Complexity of methods should not be too high
        int result = 0;
        for (int i = 0; i < 10; i++) {
            result += processI(i);
        }
        log.info("Result: {}", result); // SonarQube java:S106 - Replace this use of System.out by a logger.
    }

    private int processI(int i) {
        if (i % 2 == 0) {
            int innerResult = 0;
            for (int j = 0; j < 5; j++) {
                innerResult += processJ(i, j);
            }
            return innerResult;
        } else {
            return i;
        }
    }

    private int processJ(int i, int j) {
        if (j % 2 == 0) {
            int innerResult = 0;
            for (int k = 0; k < 3; k++) {
                innerResult += processK(i, j, k);
            }
            return innerResult;
        } else {
            return j;
        }
    }

    private int processK(int i, int j, int k) {
        if (k % 2 == 0) {
            return i + j + k;
        } else {
            return -(i + j + k);
        }
    }
}
