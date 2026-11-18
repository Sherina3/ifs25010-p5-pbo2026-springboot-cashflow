package org.delcom.ifs25010springbootcashflow;

import org.delcom.ifs25010springbootcashflow.domain.repository.IUserRepository;
import org.delcom.ifs25010springbootcashflow.usecase.AuthUseCase;
import org.delcom.ifs25010springbootcashflow.usecase.FinanceUseCase;
import org.delcom.ifs25010springbootcashflow.usecase.UserUseCase;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ApplicationContext;
import org.springframework.test.context.ActiveProfiles;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mockStatic;

/** Smoke test dengan profil test (H2): context naik, wiring benar, admin root ter-seed. */
@SpringBootTest
@ActiveProfiles("test")
class AppTest {

    @Autowired
    private ApplicationContext context;

    @Autowired
    private IUserRepository userRepository;

    @Test
    void contextLoadsAndBeansAreWired() {
        assertNotNull(context.getBean(AuthUseCase.class));
        assertNotNull(context.getBean(FinanceUseCase.class));
        assertNotNull(context.getBean(UserUseCase.class));
    }

    @Test
    void rootAdminIsSeeded() {
        assertTrue(userRepository.findByUsername("admin").orElseThrow().isAdmin());
    }

    @Test
    void mainStartsSpringApplication() throws Exception {
        String[] args = new String[0];
        try (MockedStatic<SpringApplication> spring = mockStatic(SpringApplication.class)) {
            App.main(args);
            spring.verify(() -> SpringApplication.run(App.class, args));
        }
    }
}
