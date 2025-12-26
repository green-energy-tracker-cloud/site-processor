package com.green.energy.tracker.cloud.site_processor;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.test.context.TestPropertySource;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;

@ExtendWith(MockitoExtension.class)
class SiteProcessorApplicationTest {

    @Test
    void main_shouldCallSpringApplicationRun() {
        // Arrange: We want to mock the static method SpringApplication.run()
        try (MockedStatic<SpringApplication> mocked = mockStatic(SpringApplication.class)) {
            // We expect the run method to be called, and when it is, we return a mock context.
            mocked.when(() -> SpringApplication.run(any(Class.class), any(String[].class)))
                    .thenReturn(mock(ConfigurableApplicationContext.class));

            // Act: Call the main method of our application
            SiteProcessorApplication.main(new String[]{"arg1", "arg2"});

            // Assert: Verify that SpringApplication.run was indeed called with the correct class and arguments.
            mocked.verify(() -> SpringApplication.run(SiteProcessorApplication.class, new String[]{"arg1", "arg2"}));
        }
    }
}
