/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package MODELS;

import com.datastax.oss.driver.api.mapper.annotations.CqlName;
import com.datastax.oss.driver.api.mapper.annotations.Entity;
import com.datastax.oss.driver.api.mapper.annotations.PartitionKey;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

/**
 *
 * @author HAO
 */
@Entity
@CqlName("loyalty_accounts")
public class LoyaltyAccount {
    @PartitionKey
    @CqlName("customer_id")
    private UUID customerId;

    @CqlName("points")
    private long points;

    @CqlName("tier")
    private String tier;

    @CqlName("lifetime_spent")
    private BigDecimal lifetimeSpent;

    @CqlName("order_count")
    private int orderCount;

    @CqlName("last_updated")
    private Instant lastUpdated;

    // Constructors
    public LoyaltyAccount() {}

    public LoyaltyAccount(UUID customerId, long points, String tier, BigDecimal lifetimeSpent, int orderCount, Instant lastUpdated) {
        this.customerId = customerId;
        this.points = points;
        this.tier = tier;
        this.lifetimeSpent = lifetimeSpent;
        this.orderCount = orderCount;
        this.lastUpdated = lastUpdated;
    }

    // Getters and Setters
    public UUID getCustomerId() { return customerId; }
    public void setCustomerId(UUID customerId) { this.customerId = customerId; }
    public long getPoints() { return points; }
    public void setPoints(long points) { this.points = points; }
    public String getTier() { return tier; }
    public void setTier(String tier) { this.tier = tier; }
    public BigDecimal getLifetimeSpent() { return lifetimeSpent; }
    public void setLifetimeSpent(BigDecimal lifetimeSpent) { this.lifetimeSpent = lifetimeSpent; }
    public int getOrderCount() { return orderCount; }
    public void setOrderCount(int orderCount) { this.orderCount = orderCount; }
    public Instant getLastUpdated() { return lastUpdated; }
    public void setLastUpdated(Instant lastUpdated) { this.lastUpdated = lastUpdated; }
}
