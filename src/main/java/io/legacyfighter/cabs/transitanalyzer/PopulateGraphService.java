package io.legacyfighter.cabs.transitanalyzer;

import io.legacyfighter.cabs.entity.Transit;
import io.legacyfighter.cabs.repository.TransitRepository;
import org.springframework.stereotype.Service;

import static io.legacyfighter.cabs.entity.Transit.Status.COMPLETED;

@Service
class PopulateGraphService {

    private final TransitRepository transitRepository;
    private final GraphTransitAnalyzer graphTransitAnalyzer;

    PopulateGraphService(TransitRepository transitRepository,
                         GraphTransitAnalyzer graphTransitAnalyzer) {
        this.transitRepository = transitRepository;
        this.graphTransitAnalyzer = graphTransitAnalyzer;
    }

    void populate() {
        transitRepository
                .findAllByStatus(COMPLETED)
                .forEach(this::addToGraph);
    }

    private void addToGraph(Transit transit) {
        graphTransitAnalyzer.addTransitBetweenAddresses(
                transit.getClient().getId(),
                transit.getId(),
                transit.getFrom().getHash(),
                transit.getTo().getHash(),
                transit.getStarted(),
                transit.getCompleteAt());
    }
}
