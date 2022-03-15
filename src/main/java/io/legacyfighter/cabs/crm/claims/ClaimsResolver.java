package io.legacyfighter.cabs.crm.claims;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.legacyfighter.cabs.common.BaseEntity;
import io.legacyfighter.cabs.entity.Client;

import javax.persistence.Entity;
import java.util.HashSet;
import java.util.Set;

import static com.fasterxml.jackson.annotation.JsonAutoDetect.Visibility.ANY;
import static com.fasterxml.jackson.annotation.PropertyAccessor.FIELD;
import static io.legacyfighter.cabs.crm.claims.ClaimsResolver.WhoToAsk.*;
import static io.legacyfighter.cabs.crm.claims.Status.ESCALATED;
import static io.legacyfighter.cabs.crm.claims.Status.REFUNDED;

@Entity
class ClaimsResolver extends BaseEntity {

    public static class Result {

        public WhoToAsk whoToAsk;
        public Status decision;

        Result(WhoToAsk whoToAsk, Status decision) {
            this.whoToAsk = whoToAsk;
            this.decision = decision;
        }
    }

    ClaimsResolver(Long clientId) {
        this.clientId = clientId;
    }

    public ClaimsResolver() {
    }

    public enum WhoToAsk {
        ASK_DRIVER, ASK_CLIENT, ASK_NOONE
    }

    private Long clientId;

    private String claimedTransitsIds;

    Result resolve(Claim claim, Client.Type clientType, double automaticRefundForVipThreshold, int numberOfTransits, double noOfTransitsForClaimAutomaticRefund) {
        Long transitId = claim.getTransitId();
        if (getClaimedTransitsIds().contains(transitId)) {
            return new Result(ASK_NOONE, ESCALATED);
        }
        addNewClaimFor(claim.getTransitId());
        if (numberOfClaims() <= 3) {
            return new Result(ASK_NOONE, REFUNDED);
        }
        if (clientType.equals(Client.Type.VIP)) {
            if (claim.getTransitPrice().toInt() < automaticRefundForVipThreshold) {
                return new Result(ASK_NOONE, REFUNDED);
            } else {
                return new Result(ASK_DRIVER, Status.ESCALATED);
            }
        } else {
            if (numberOfTransits >= noOfTransitsForClaimAutomaticRefund) {
                if (claim.getTransitPrice().toInt() < automaticRefundForVipThreshold) {
                    return new Result(ASK_NOONE, Status.REFUNDED);
                } else {
                    return new Result(ASK_CLIENT, Status.ESCALATED);
                }
            } else {
                return new Result(ASK_DRIVER, Status.ESCALATED);
            }
        }
    }

    private void addNewClaimFor(Long transitId) {
        Set<Long> transitsIds = getClaimedTransitsIds();
        transitsIds.add(transitId);
        claimedTransitsIds = JsonMapper.serialize(transitsIds);
    }

    private Set<Long> getClaimedTransitsIds() {
        return JsonMapper.deserialize(claimedTransitsIds);
    }

    private int numberOfClaims() {
        return getClaimedTransitsIds().size();
    }

}

class JsonMapper {

    private static ObjectMapper objectMapper;

    static {
        objectMapper = new ObjectMapper();
        objectMapper.setVisibility(FIELD, ANY);
    }

    static Set<Long> deserialize(String json) {
        if (json == null) {
            return new HashSet<>();
        }
        try {
            return objectMapper.readValue(json, objectMapper.getTypeFactory().constructCollectionType(Set.class, Long.class));
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }
    static String serialize(Set<Long> transitsIds) {
        try {
            return objectMapper.writeValueAsString(transitsIds);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }

}