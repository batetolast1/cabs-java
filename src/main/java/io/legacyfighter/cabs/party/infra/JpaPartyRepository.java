package io.legacyfighter.cabs.party.infra;

import io.legacyfighter.cabs.party.model.party.Party;
import io.legacyfighter.cabs.party.model.party.PartyRepository;
import org.springframework.stereotype.Repository;

import javax.persistence.EntityManager;
import java.util.UUID;

@Repository
public class JpaPartyRepository implements PartyRepository {

    private final EntityManager entityManager;

    public JpaPartyRepository(EntityManager entityManager) {
        this.entityManager = entityManager;
    }

    @Override
    public Party put(UUID id) {
        Party party = entityManager.find(Party.class, id);

        if (party == null) {
            party = new Party();
            party.setId(id);
            entityManager.persist(party);
        }
        return party;
    }
}
