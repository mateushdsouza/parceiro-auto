package br.com.parceiroauto.confg;

import org.flywaydb.core.Flyway;

public class FlyWayconfg {
    public static void migrate() {
        Flyway flyway = Flyway.configure()
                .dataSource(
                        "jdbc:postgresql://localhost:5432/parceiroauto",
                        "postgres",
                        "1234"
                )
                .baselineOnMigrate(true)
                .load():

        flyway.migrate();
    }
}
