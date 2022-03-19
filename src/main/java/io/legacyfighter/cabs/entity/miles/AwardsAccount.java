package io.legacyfighter.cabs.entity.miles;

import io.legacyfighter.cabs.common.BaseEntity;
import io.legacyfighter.cabs.entity.Client;
import io.legacyfighter.cabs.entity.Transit;
import org.hibernate.annotations.Fetch;
import org.hibernate.annotations.FetchMode;
import org.springframework.util.comparator.Comparators;

import javax.persistence.*;
import java.time.Instant;
import java.util.*;

@Entity
public class AwardsAccount extends BaseEntity {

    private static final int CLAIM_COUNT_THRESHOLD = 3;
    private static final int TRANSIT_COUNT_THRESHOLD = 15;

    private static final Comparator<AwardedMiles> FIRST_NON_EXPIRING_THEN_LATEST_TO_EXPIRE =
            Comparator.comparing(AwardedMiles::getExpirationDate, Comparators.nullsHigh())
                    .reversed()
                    .thenComparing(Comparators.nullsHigh());
    private static final Comparator<AwardedMiles> SOON_TO_EXPIRE_FIRST_THEN_NON_EXPIRING =
            Comparator.comparing(AwardedMiles::cantExpire)
                    .thenComparing(AwardedMiles::getExpirationDate, Comparators.nullsLow());
    private static final Comparator<AwardedMiles> LATEST_TO_EXPIRE_FIRST_THEN_NON_EXPIRING =
            Comparator.comparing(AwardedMiles::cantExpire)
                    .thenComparing(AwardedMiles::getDate);
    private static final Comparator<AwardedMiles> OLDEST_FIRST = Comparator.comparing(AwardedMiles::getDate);

    @OneToOne
    private Client client;

    @Column(nullable = false)
    private Instant date = Instant.now();

    @Column(nullable = false)
    private Boolean isActive = false;

    @Column(nullable = false)
    private Integer transactions = 0;

    @OneToMany(mappedBy = "account",
            cascade = CascadeType.ALL)
    @Fetch(value = FetchMode.JOIN)
    private List<AwardedMiles> miles = new ArrayList<>();

    public AwardsAccount() {
        // for JPA
    }

    private AwardsAccount(Client client, Instant date, boolean isActive) {
        this.client = client;
        this.date = date;
        this.isActive = isActive;
    }

    public Client getClient() {
        return this.client;
    }

    public Instant getDate() {
        return this.date;
    }

    public boolean isActive() {
        return Boolean.TRUE.equals(this.isActive);
    }

    public Integer getTransactions() {
        return this.transactions;
    }

    List<AwardedMiles> getMiles() {
        return Collections.unmodifiableList(new ArrayList<>(this.miles));
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        AwardsAccount that = (AwardsAccount) o;
        return Objects.equals(client, that.client) && Objects.equals(date, that.date) && Objects.equals(isActive, that.isActive) && Objects.equals(transactions, that.transactions) && Objects.equals(miles, that.miles);
    }

    public static AwardsAccount notActiveAccount(Client client, Instant createdAt) {
        return new AwardsAccount(client, createdAt, false);
    }

    public void activate() {
        this.isActive = true;
    }

    public void deactivate() {
        this.isActive = false;
    }

    public AwardedMiles addExpiringMiles(Integer milesAmount,
                                         Instant at,
                                         Instant expireAt,
                                         Transit transit) {
        Miles expiringMiles = ConstantUntil.constantUntil(milesAmount, expireAt);
        AwardedMiles awardedMiles = new AwardedMiles(transit, at, expiringMiles, this);

        this.miles.add(awardedMiles);
        this.transactions++;

        return awardedMiles;
    }

    public AwardedMiles addNonExpiringMiles(Integer milesAmount, Instant at) {
        Miles nonExpiringMiles = ConstantUntil.constantUntilForever(milesAmount);
        AwardedMiles awardedMiles = new AwardedMiles(null, at, nonExpiringMiles, this);

        this.miles.add(awardedMiles);
        this.transactions++;

        return awardedMiles;
    }

    public void remove(int milesAmountToRemove,
                       Instant at,
                       int transitCount,
                       int claimCount,
                       Client.Type clientType,
                       boolean isSunday) {
        if (Boolean.FALSE.equals(this.isActive)) {
            throw new IllegalArgumentException("Awards account is not active, id = " + this.id);
        }
        if (milesAmountToRemove > this.calculateBalance(at)) {
            throw new IllegalArgumentException("Insufficient miles, id = " + this.id +
                    ", miles to remove requested = " + milesAmountToRemove);
        }

        List<AwardedMiles> awardedMilesList = sortAwardedMiles(transitCount, claimCount, clientType, isSunday);

        for (AwardedMiles awardedMiles : awardedMilesList) {
            if (milesAmountToRemove <= 0) {
                break;
            }

            if (awardedMiles.expired(at)) {
                Integer awardedMilesAmount = awardedMiles.getMilesAmount(at);

                if (awardedMilesAmount <= milesAmountToRemove) {
                    awardedMiles.removeAllMiles(at);
                } else {
                    awardedMiles.subtract(milesAmountToRemove, at);
                }

                milesAmountToRemove -= awardedMilesAmount;
            }
        }
    }

    private List<AwardedMiles> sortAwardedMiles(int transitCount, int claimCount, Client.Type clientType, boolean isSunday) {
        List<AwardedMiles> awardedMilesList = new ArrayList<>(this.miles);

        if (claimCount >= CLAIM_COUNT_THRESHOLD) {
            awardedMilesList.sort(FIRST_NON_EXPIRING_THEN_LATEST_TO_EXPIRE);
        } else if (clientType.equals(Client.Type.VIP)) {
            awardedMilesList.sort(SOON_TO_EXPIRE_FIRST_THEN_NON_EXPIRING);
        } else if (transitCount >= TRANSIT_COUNT_THRESHOLD && isSunday) {
            awardedMilesList.sort(SOON_TO_EXPIRE_FIRST_THEN_NON_EXPIRING);
        } else if (transitCount >= TRANSIT_COUNT_THRESHOLD) {
            awardedMilesList.sort(LATEST_TO_EXPIRE_FIRST_THEN_NON_EXPIRING);
        } else {
            awardedMilesList.sort(OLDEST_FIRST);
        }

        return awardedMilesList;
    }

    public Integer calculateBalance(Instant at) {
        return this.miles.stream()
                .filter(awardedMiles -> awardedMiles.expired(at))
                .map(awardedMiles -> awardedMiles.getMilesAmount(at))
                .reduce(0, Integer::sum);
    }

    public void moveMilesTo(AwardsAccount accountTo, Integer milesAmountToTransfer, Instant at) {
        if (Boolean.FALSE.equals(this.isActive)) {
            throw new IllegalArgumentException("Awards account is not active, id = " + this.id);
        }
        if (milesAmountToTransfer > this.calculateBalance(at)) {
            throw new IllegalArgumentException("Insufficient miles, id = " + this.id +
                    ", miles to transfer requested = " + milesAmountToTransfer);
        }

        List<AwardedMiles> awardedMilesList = new ArrayList<>(this.miles);
        for (AwardedMiles awardedMilesFrom : awardedMilesList) {
            if (milesAmountToTransfer <= 0) {
                break;
            }

            if (awardedMilesFrom.expired(at)) {
                Integer awardedMilesFromAmount = awardedMilesFrom.getMilesAmount(at);

                if (awardedMilesFromAmount <= milesAmountToTransfer) {
                    awardedMilesFrom.transferToAccount(accountTo);

                    this.miles.remove(awardedMilesFrom);
                    accountTo.miles.add(awardedMilesFrom);
                } else {
                    Miles newMiles = awardedMilesFrom.getMiles()
                            .subtract(awardedMilesFromAmount - milesAmountToTransfer, at);

                    AwardedMiles newAwardedMiles = new AwardedMiles(null, at, newMiles, accountTo);

                    accountTo.miles.add(newAwardedMiles);

                    awardedMilesFrom.subtract(milesAmountToTransfer, at);
                }

                milesAmountToTransfer -= awardedMilesFromAmount;
            }
        }

        this.transactions++;
        accountTo.transactions++;
    }
}
