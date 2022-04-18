package io.legacyfighter.cabs.transitanalyzer;

import io.legacyfighter.cabs.entity.events.TransitCompleted;
import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.graphdb.Result;
import org.neo4j.graphdb.Transaction;
import org.springframework.transaction.event.TransactionalEventListener;

import javax.annotation.PreDestroy;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

class GraphTransitAnalyzer {

    private final GraphDatabaseService graphDatabaseService;

    GraphTransitAnalyzer(GraphDatabaseService graphDatabaseService) {
        this.graphDatabaseService = graphDatabaseService;
    }

    @PreDestroy
    void preDestroy() {
        if (graphDatabaseService != null) {
            graphDatabaseService.shutdown();
        }
    }

    List<Long> analyze(Long clientId, Integer addressHash) {
        try (Transaction t = graphDatabaseService.beginTx()) {
            Result result = graphDatabaseService.execute("MATCH p=(a:Address)-[:Transit*]->(:Address) " +
                    "WHERE a.hash = " + addressHash + " " +
                    "AND (ALL(x IN range(1, length(p)-1) WHERE ((relationships(p)[x]).clientId = " + clientId + ") AND 0 <= duration.inSeconds((relationships(p)[x-1]).completeAt, (relationships(p)[x]).started).nanoseconds < 900000000000)) " +
                    "AND length(p) >= 1 " +
                    "RETURN [x in nodes(p) | x.hash] AS hashes " +
                    "ORDER BY length(p) DESC " +
                    "LIMIT 1");

            t.success();

            return new ArrayList<>(((List<Long>) result.next().get("hashes")));
        }
    }

    void addTransitBetweenAddresses(Long clientId,
                                    Long transitId,
                                    Integer addressFromHash,
                                    Integer addressToHash,
                                    Instant started,
                                    Instant completeAt) {
        try (Transaction t = graphDatabaseService.beginTx()) {
            graphDatabaseService.execute("MERGE (from:Address {hash: " + addressFromHash + "})");
            graphDatabaseService.execute("MERGE (to:Address {hash: " + addressToHash + "})");
            graphDatabaseService.execute("MATCH (from:Address {hash: " + addressFromHash + "}), (to:Address {hash: " + addressToHash + "}) " +
                    "CREATE (from)-[:Transit {clientId: " + clientId + ", transitId: " + transitId + ", " +
                    "started: datetime({epochSeconds: " + started.getEpochSecond() + ", nanosecond: " + started.getNano() + "}), completeAt: datetime({epochSeconds: " + completeAt.getEpochSecond() + ", nanosecond: " + completeAt.getNano() + "})}]->(to)");

            t.success();
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
