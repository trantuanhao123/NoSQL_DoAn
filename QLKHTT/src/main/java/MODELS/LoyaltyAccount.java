package MODELS;

import java.math.BigDecimal;
import java.time.Instant;

public class LoyaltyAccount {

    private String customerId;

    private long points;
    private String tier;
    private BigDecimal lifetimeSpent;
    private int orderCount;
    private Instant lastUpdated;

    public LoyaltyAccount() {
    }

    public LoyaltyAccount(String customerId, long points, String tier, BigDecimal lifetimeSpent, int orderCount, Instant lastUpdated) {
        this.customerId = customerId;
        this.points = points;
        this.tier = tier;
        this.lifetimeSpent = lifetimeSpent;
        this.orderCount = orderCount;
        this.lastUpdated = lastUpdated;
    }

    public String getCustomerId() {
        return customerId;
    }

    public void setCustomerId(String customerId) {
        this.customerId = customerId;
    }

    public long getPoints() {
        return points;
    }

    public void setPoints(long points) {
        this.points = points;
    }

    public String getTier() {
        return tier;
    }

    public void setTier(String tier) {
        this.tier = tier;
    }

    public BigDecimal getLifetimeSpent() {
        return lifetimeSpent;
    }

    public void setLifetimeSpent(BigDecimal lifetimeSpent) {
        this.lifetimeSpent = lifetimeSpent;
    }

    public int getOrderCount() {
        return orderCount;
    }

    public void setOrderCount(int orderCount) {
        this.orderCount = orderCount;
    }

    public Instant getLastUpdated() {
        return lastUpdated;
    }

    public void setLastUpdated(Instant lastUpdated) {
        this.lastUpdated = lastUpdated;
    }

    @Override
    public String toString() {
        return "LoyaltyAccount{"
                + "customerId=" + customerId
                + ", points=" + points
                + ", tier='" + tier + '\''
                + ", lifetimeSpent=" + lifetimeSpent
                + ", orderCount=" + orderCount
                + ", lastUpdated=" + lastUpdated
                + '}';
    }
}
