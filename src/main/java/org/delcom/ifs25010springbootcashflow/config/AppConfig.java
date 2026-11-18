package org.delcom.ifs25010springbootcashflow.config;

import org.delcom.ifs25010springbootcashflow.adapter.presenter.FinancePresenter;
import org.delcom.ifs25010springbootcashflow.adapter.repository.JdbcTransactionRepository;
import org.delcom.ifs25010springbootcashflow.adapter.repository.JdbcUserRepository;
import org.delcom.ifs25010springbootcashflow.adapter.repository.SchemaInitializer;
import org.delcom.ifs25010springbootcashflow.adapter.storage.FileStorage;
import org.delcom.ifs25010springbootcashflow.domain.repository.ITransactionRepository;
import org.delcom.ifs25010springbootcashflow.domain.repository.IUserRepository;
import org.delcom.ifs25010springbootcashflow.usecase.AuthUseCase;
import org.delcom.ifs25010springbootcashflow.usecase.FinanceUseCase;
import org.delcom.ifs25010springbootcashflow.usecase.UserUseCase;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.DependsOn;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import javax.sql.DataSource;
import java.io.IOException;

/**
 * Composition root Spring: merakit semua dependency antar-layer.
 */
@Configuration
public class AppConfig {

    @Bean
    public IUserRepository userRepository(DataSource dataSource) {
        SchemaInitializer.initialize(dataSource);
        return new JdbcUserRepository(dataSource);
    }

    @Bean
    @DependsOn("userRepository")
    public ITransactionRepository transactionRepository(DataSource dataSource) {
        return new JdbcTransactionRepository(dataSource);
    }

    @Bean
    public AuthUseCase authUseCase(IUserRepository userRepository) {
        return new AuthUseCase(userRepository);
    }

    @Bean
    public FinanceUseCase financeUseCase(ITransactionRepository transactionRepository) {
        return new FinanceUseCase(transactionRepository);
    }

    @Bean
    public UserUseCase userUseCase(IUserRepository userRepository) {
        return new UserUseCase(userRepository);
    }

    @Bean
    public FinancePresenter financePresenter() {
        return new FinancePresenter();
    }

    @Bean
    public FileStorage fileStorage(@Value("${app.upload-dir}") String uploadDir) throws IOException {
        return new FileStorage(uploadDir);
    }

    /** Membuat akun ADMIN dari ROOT_USERNAME / ROOT_PASSWORD saat aplikasi start. */
    @Bean
    public ApplicationRunner seedRootAdmin(
            AuthUseCase authUseCase,
            @Value("${app.root-username}") String rootUsername,
            @Value("${app.root-password}") String rootPassword
    ) {
        return args -> authUseCase.ensureRootAdmin(rootUsername, rootPassword);
    }

    /** Melayani file upload (foto profil dan bukti transaksi) di path /uploads/**. */
    @Bean
    public WebMvcConfigurer uploadResourceConfigurer(FileStorage fileStorage) {
        return new WebMvcConfigurer() {
            @Override
            public void addResourceHandlers(ResourceHandlerRegistry registry) {
                registry.addResourceHandler("/uploads/**").addResourceLocations(fileStorage.rootLocation());
            }
        };
    }
}
