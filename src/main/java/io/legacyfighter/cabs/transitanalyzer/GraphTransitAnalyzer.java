package io.legacyfighter.cabs.transitanalyzer;

import io.legacyfighter.cabs.entity.events.TransitCompleted;
import org.neo4j.dbms.api.DatabaseManagementService;
import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.graphdb.Result;
import org.neo4j.graphdb.Transaction;
import org.springframework.transaction.event.TransactionalEventListener;

import javax.annotation.PreDestroy;
import java.time.Instant;
import java.util.List;

class GraphTransitAnalyzer {

    private final DatabaseManagementService databaseManagementService;

    private final GraphDatabaseService graphDatabaseService;

    GraphTransitAnalyzer(DatabaseManagementService databaseManagementService,
                         GraphDatabaseService graphDatabaseService) {
        this.databaseManagementService = databaseManagementService;
        this.graphDatabaseService = graphDatabaseService;
    }

    @PreDestroy
    void preDestroy() {
        databaseManagementService.shutdown();
    }

    @SuppressWarnings("unchecked")
    List<Long> analyze(Long clientId, Integer addressHash) {
        try (Transaction t = graphDatabaseService.beginTx()) {
            Result result = t.execute("MATCH p=(a:Address)-[:Transit*]->(:Address) " +
                    "WHERE a.hash = " + addressHash + " " +
                    "AND (ALL(x IN range(1, length(p)-1) WHERE ((relationships(p)[x]).clientId = " + clientId + ") AND 0 <= duration.inSeconds((relationships(p)[x-1]).completeAt, (relationships(p)[x]).started).nanoseconds < 900000000000)) " +
                    "AND length(p) >= 1 " +
                    "RETURN [x in nodes(p) | x.hash] AS hashes " +
                    "ORDER BY length(p) DESC " +
                    "LIMIT 1");

            return ((List<Long>) result.next().get("hashes"));
        }
    }

    void addTransitBetweenAddresses(Long clientId,
                                    Long transitId,
                                    Integer addressFromHash,
                                    Integer addressToHash,
                                    Instant started,
                                    Instant completeAt) {
        try (Transaction t = graphDatabaseService.beginTx()) {
            t.execute("MERGE (from:Address {hash: " + addressFromHash + "})");
            t.execute("MERGE (to:Address {hash: " + addressToHash + "})");
            t.execute("MATCH (from:Address {hash: " + addressFromHash + "}), (to:Address {hash: " + addressToHash + "}) " +
                    "CREATE (from)-[:Transit {clientId: " + clientId + ", transitId: " + transitId + ", " +
                    "started: datetime({epochSeconds: " + started.getEpochSecond() + ", nanosecond: " + started.getNano() + "}), completeAt: datetime({epochSeconds: " + completeAt.getEpochSecond() + ", nanosecond: " + completeAt.getNano() + "})}]->(to)");

            t.commit();
        }
    }

    @TransactionalEventListener
    @SuppressWarnings("unused")
    public void handle(TransitCompleted transitCompleted) {
        addTransitBetweenAddresses(
                transitCompleted.getClientId(),
                transitCompleted.getTransitId(),
                transitCompleted.getAddressFromHash(),
                transitCompleted.getAddressToHash(),
                transitCompleted.getStarted(),
                transitCompleted.getCompleteAt()
        );
    }
}
