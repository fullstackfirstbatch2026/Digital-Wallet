package com.digitalwallet;

import org.junit.jupiter.api.Test;

/**
 * Placeholder test so "mvn package" has something to run without needing a database.
 * (A @SpringBootTest would require MySQL to be running.)
 */
class DigitalWalletApplicationTests {

    @Test
    void sanityCheck() {
        org.junit.jupiter.api.Assertions.assertEquals(4, 2 + 2);
    }
}
