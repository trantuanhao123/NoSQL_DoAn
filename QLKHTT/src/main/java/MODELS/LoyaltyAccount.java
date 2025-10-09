package MODELS;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

/**
 * POJO đại diện cho bản ghi trong bảng loyalty_accounts.
 * Không sử dụng Cassandra Mapper annotation.
 */
public class LoyaltyAccount {
    private UUID customerId;
    private long points;
    private String tier;
    private BigDecimal lifetimeSpent;
    private int orderCount;
    private Instant lastUpdated;

    // ✅ Constructors
    public LoyaltyAccount() {}

    public LoyaltyAccount(UUID customerId, long points, String tier, BigDecimal lifetimeSpent, int orderCount, Instant lastUpdated) {
        this.customerId = customerId;
        this.points = points;
        this.tier = tier;
        this.lifetimeSpent = lifetimeSpent;
        this.orderCount = orderCount;
        this.lastUpdated = lastUpdated;
    }

    // ✅ Getters / Setters
    public UUID getCustomerId() {
        return customerId;
    }

    public void setCustomerId(UUID customerId) {
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

    // ✅ toString() hỗ trợ debug
    @Override
    public String toString() {
        return "LoyaltyAccount{" +
                "customerId=" + customerId +
                ", points=" + points +
                ", tier='" + tier + '\'' +
                ", lifetimeSpent=" + lifetimeSpent +
                ", orderCount=" + orderCount +
                ", lastUpdated=" + lastUpdated +
                '}';
    }
}
