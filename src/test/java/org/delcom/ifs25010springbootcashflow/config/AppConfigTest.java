package org.delcom.ifs25010springbootcashflow.config;

import org.delcom.ifs25010springbootcashflow.adapter.storage.FileStorage;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.springframework.mock.web.MockServletContext;
import org.springframework.web.context.support.GenericWebApplicationContext;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;

import java.io.IOException;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertTrue;

class AppConfigTest {

    @TempDir
    Path tempDir;

    @Test
    void registersUploadsResourceHandler() throws IOException {
        AppConfig config = new AppConfig();
        FileStorage storage = config.fileStorage(tempDir.toString());
        ResourceHandlerRegistry registry =
                new ResourceHandlerRegistry(new GenericWebApplicationContext(), new MockServletContext());

        config.uploadResourceConfigurer(storage).addResourceHandlers(registry);

        assertTrue(registry.hasMappingForPattern("/uploads/**"));
    }
}
