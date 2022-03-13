package io.legacyfighter.cabs.entity;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.PropertyAccessor;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.legacyfighter.cabs.common.BaseEntity;

import javax.persistence.Entity;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

@Entity
public class ClaimsResolver extends BaseEntity {

    public enum WhoToAsk {

        ASK_DRIVER, ASK_CLIENT, ASK_NO_ONE;
    }

    private Long clientId;

    private String claimedTransitIds;

    public ClaimsResolver() {
    }

    public ClaimsResolver(Long clientId) {
        this.clientId = clientId;
    }

    public Result resolve(Claim claim,
                          int automaticRefundForVipThreshold,
                          int numberOfTransits,
                          int numberOfTransitsForClaimAutomaticRefund) {
        if (getClaimedTransitIds().contains(claim.getTransit().getId())) {
            return new Result(WhoToAsk.ASK_NO_ONE, Claim.Status.ESCALATED);
        }

        addNewClaimFor(claim.getTransit());

        if (numberOfClaims() <= 3) {
            return new Result(WhoToAsk.ASK_NO_ONE, Claim.Status.REFUNDED);
        }
        if (claim.getOwner().getType().equals(Client.Type.VIP)) {
            if (claim.getTransit().getPrice().toInt() < automaticRefundForVipThreshold) {
                return new Result(WhoToAsk.ASK_NO_ONE, Claim.Status.REFUNDED);
            } else {
                return new Result(WhoToAsk.ASK_DRIVER, Claim.Status.ESCALATED);
            }
        } else {
            if (numberOfTransits >= numberOfTransitsForClaimAutomaticRefund) {
                if (claim.getTransit().getPrice().toInt() < automaticRefundForVipThreshold) {
                    return new Result(WhoToAsk.ASK_NO_ONE, Claim.Status.REFUNDED);
                } else {
                    return new Result(WhoToAsk.ASK_CLIENT, Claim.Status.ESCALATED);
                }
            } else {
                return new Result(WhoToAsk.ASK_DRIVER, Claim.Status.ESCALATED);
            }
        }
    }

    private Set<Long> getClaimedTransitIds() {
        return JsonMapper.deserialize(claimedTransitIds);
    }

    private void addNewClaimFor(Transit transit) {
        Set<Long> transitIds = getClaimedTransitIds();
        transitIds.add(transit.getId());
        claimedTransitIds = JsonMapper.serialize(transitIds);
    }

    private int numberOfClaims() {
        return getClaimedTransitIds().size();
    }

    public static final class Result {

        private final WhoToAsk whoToAsk;
        private final Claim.Status decision;

        public Result(WhoToAsk whoToAsk, Claim.Status decision) {
            this.whoToAsk = whoToAsk;
            this.decision = decision;
        }

        public WhoToAsk getWhoToAsk() {
            return whoToAsk;
        }

        public Claim.Status getDecision() {
            return decision;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            Result result = (Result) o;
            return whoToAsk == result.whoToAsk && decision == result.decision;
        }

        @Override
        public int hashCode() {
            return Objects.hash(whoToAsk, decision);
        }
    }
}

final class JsonMapper {

    private static final ObjectMapper OBJECT_MAPPER;

    static {
        OBJECT_MAPPER = new ObjectMapper();
        OBJECT_MAPPER.setVisibility(PropertyAccessor.FIELD, JsonAutoDetect.Visibility.ANY);
    }

    private JsonMapper() {
    }

    static Set<Long> deserialize(String json) {
        if (json == null) {
            return new HashSet<>();
        }
        try {
            return OBJECT_MAPPER.readValue(json, OBJECT_MAPPER.getTypeFactory().constructCollectionType(Set.class, Long.class));
        } catch (JsonProcessingException e) {
            throw new IllegalArgumentException(e);
        }
    }

    static String serialize(Set<Long> transitIds) {
        try {
            return OBJECT_MAPPER.writeValueAsString(transitIds);
        } catch (JsonProcessingException e) {
            throw new IllegalArgumentException(e);
        }
    }
}
