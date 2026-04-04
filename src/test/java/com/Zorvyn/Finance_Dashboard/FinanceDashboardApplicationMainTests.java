package com.Zorvyn.Finance_Dashboard;

import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.times;

import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.springframework.boot.SpringApplication;

class FinanceDashboardApplicationMainTests {

    @Test
    void mainDelegatesToSpringApplicationRun() {
        String[] args = {"--spring.profiles.active=test"};

        try (MockedStatic<SpringApplication> springApplication = Mockito.mockStatic(SpringApplication.class)) {
            FinanceDashboardApplication.main(args);

            springApplication.verify(
                    () -> SpringApplication.run(eq(FinanceDashboardApplication.class), eq(args)),
                    times(1)
            );
        }
    }
}
