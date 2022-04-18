package io.legacyfighter.cabs.transitanalyzer;

import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.graphdb.factory.GraphDatabaseFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.util.FileSystemUtils;

import javax.annotation.PreDestroy;
import java.io.File;

@Configuration
class Neo4JConfig {

    @Value("${neo4j.db.file}")
    private String dbPath;

    @Bean
    GraphDatabaseService graphDatabaseService() {
        File storeDir = new File("db/" + dbPath);
        return new GraphDatabaseFactory().newEmbeddedDatabase(storeDir);
    }

    @Bean
    GraphTransitAnalyzer graphTransitAnalyzer() {
        return new GraphTransitAnalyzer(graphDatabaseService());
    }

    @PreDestroy
    void cleanDbDir() {
        FileSystemUtils.deleteRecursively(new File("db"));
    }
}
