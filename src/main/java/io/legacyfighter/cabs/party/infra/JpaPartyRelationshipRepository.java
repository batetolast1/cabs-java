package io.legacyfighter.cabs.party.infra;

import io.legacyfighter.cabs.party.api.PartyId;
import io.legacyfighter.cabs.party.model.party.Party;
import io.legacyfighter.cabs.party.model.party.PartyRelationship;
import io.legacyfighter.cabs.party.model.party.PartyRelationshipRepository;
import org.springframework.stereotype.Repository;

import javax.persistence.EntityManager;
import java.util.List;
import java.util.Optional;

@Repository
public class JpaPartyRelationshipRepository implements PartyRelationshipRepository {

    private final EntityManager entityManager;

    public JpaPartyRelationshipRepository(EntityManager entityManager) {
        this.entityManager = entityManager;
    }

    @Override
    public PartyRelationship put(String partyRelationshipName, String partyARole, Party partyA, String partyBRole, Party partyB) {
        List<PartyRelationship> parties = entityManager.createQuery("" +
                "SELECT r " +
                "FROM PartyRelationship r " +
                "WHERE r.name = :name " +
                "AND (" +
                "(r.partyA = :partyA AND r.partyB = :partyB) " +
                "OR " +
                "(r.partyA = : partyB AND r.partyB = :partyA)" +
                ")", PartyRelationship.class)

                .setParameter("name", partyRelationshipName)
                .setParameter("partyA", partyA)
                .setParameter("partyB", partyB)
                .getResultList();

        PartyRelationship relationship;

        if (parties.isEmpty()) {
            relationship = new PartyRelationship();
            entityManager.persist(relationship);
        } else {
            relationship = parties.get(0);
        }

        relationship.setName(partyRelationshipName);
        relationship.setPartyA(partyA);
        relationship.setPartyB(partyB);
        relationship.setRoleA(partyARole);
        relationship.setRoleB(partyBRole);

        return relationship;
    }

    @Override
    public Optional<PartyRelationship> findRelationshipFor(PartyId id, String partyRelationshipName) {
        List<PartyRelationship> parties = entityManager.createQuery("" +
                "SELECT r " +
                "FROM PartyRelationship r " +
                "WHERE r.name = :name " +
                "AND (" +
                "r.partyA.id = :id " +
                "OR " +
                "r.partyB.id = :id" +
                ")", PartyRelationship.class)
                .setParameter("name", partyRelationshipName)
                .setParameter("id", id.toUUID())
                .getResultList();
        if (parties.isEmpty()) {
            return Optional.empty();
        }
        return Optional.of(parties.get(0));
    }
}
