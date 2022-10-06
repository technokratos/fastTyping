package com.training.apparatus;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
@Disabled("It's need run postgres to start context. It's to hard for unit testing, " +
        "it lead to broken simple 'mvn clean install'. It's too simple for integration test.")
class ProjectVaadinApplicationTests {

    @Test
    @Disabled("It's need run postgres to start context. It's to hard for unit testing, " +
            "it lead to broken simple 'mvn clean install'. It's too simple for integration test.")
    void contextLoads() {
    }

}
