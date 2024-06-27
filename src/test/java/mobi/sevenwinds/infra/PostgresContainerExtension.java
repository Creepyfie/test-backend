package mobi.sevenwinds.infra;

import org.junit.jupiter.api.extension.BeforeAllCallback;
import org.junit.jupiter.api.extension.Extension;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.testcontainers.containers.PostgreSQLContainer;

import java.util.List;

public class PostgresContainerExtension implements Extension, BeforeAllCallback, ExtensionContext.Store.CloseableResource {

    private static PostgreSQLContainer<?> container;

    @Override
    public void beforeAll(ExtensionContext extensionContext) throws InterruptedException {
        if (container == null) {
            container = new PostgreSQLContainer<>("postgres:13.2")
                    .withDatabaseName("dev_mem")
                    .withPassword("dev")
                    .withUsername("dev");
            container.setPortBindings(List.of("45533:5432"));
            container.start();
        }
    }

    @Override
    public void close() {
        container.stop();
    }
}