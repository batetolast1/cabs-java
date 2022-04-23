package io.legacyfighter.cabs.transitanalyzer;

import org.neo4j.dbms.api.DatabaseManagementService;
import org.neo4j.dbms.api.DatabaseManagementServiceBuilder;
import org.neo4j.graphdb.GraphDatabaseService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.util.FileSystemUtils;

import javax.annotation.PreDestroy;
import java.io.IOException;
import java.nio.file.Path;

import static org.neo4j.configuration.GraphDatabaseSettings.DEFAULT_DATABASE_NAME;

@Configuration
class Neo4JConfig {

    @Value("${neo4j.db.path}")
    private String dbPath;

    @Bean
    DatabaseManagementService databaseManagementService() {
        return new DatabaseManagementServiceBuilder(Path.of("target/neo4j/", dbPath)).build();
    }

    @Bean
    GraphDatabaseService graphDatabaseService(DatabaseManagementService databaseManagementService) {
        return databaseManagementService.database(DEFAULT_DATABASE_NAME);
    }

    @Bean
    GraphTransitAnalyzer graphTransitAnalyzer(DatabaseManagementService databaseManagementService) {
        return new GraphTransitAnalyzer(databaseManagementService, databaseManagementService.database(DEFAULT_DATABASE_NAME));
    }

    @PreDestroy
    void cleanDbDir() {
        try {
            FileSystemUtils.deleteRecursively(Path.of("target/neo4j/", dbPath));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
