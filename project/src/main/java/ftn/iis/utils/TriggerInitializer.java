package ftn.iis.utils;

import org.flywaydb.core.Flyway;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import javax.sql.DataSource;

@Component
public class TriggerInitializer {

    private final DataSource dataSource;

    public TriggerInitializer(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    @Scheduled(initialDelay = 5000)
    public void runFlywayAfterHibernate() {
        Flyway.configure()
                .dataSource(dataSource)
                .baselineOnMigrate(true)
                .load()
                .migrate();
    }
}