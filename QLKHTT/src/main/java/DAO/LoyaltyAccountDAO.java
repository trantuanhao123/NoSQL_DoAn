package DAO;

import KetNoiCSDL.KetNoICSDL;
import MODELS.LoyaltyAccount;
import com.datastax.oss.driver.api.core.CqlSession;
import com.datastax.oss.driver.api.core.cql.*;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.*;

/**
 * DAO thuần Cassandra cho bảng loyalty_accounts.
 */
public class LoyaltyAccountDAO {

    private final CqlSession session;

    public LoyaltyAccountDAO(CqlSession session) {
        this.session = session;
    }

    // ✅ Thêm mới (hoặc ghi đè)
    public void save(LoyaltyAccount account) {
        String query = """
            INSERT INTO loyalty_accounts (
                customer_id, points, tier, lifetime_spent, order_count, last_updated
            ) VALUES (?, ?, ?, ?, ?, ?)
        """;
        PreparedStatement ps = session.prepare(query);
        BoundStatement bs = ps.bind(
                account.getCustomerId(),
                account.getPoints(),
                account.getTier(),
                account.getLifetimeSpent(),
                account.getOrderCount(),
                account.getLastUpdated()
        );
        session.execute(bs);
    }

    // ✅ Cập nhật điểm & tier
    public void updatePointsAndTier(UUID customerId, long points, String tier) {
        String query = """
            UPDATE loyalty_accounts
            SET points = ?, tier = ?, last_updated = ?
            WHERE customer_id = ?
        """;
        PreparedStatement ps = session.prepare(query);
        BoundStatement bs = ps.bind(points, tier, Instant.now(), customerId);
        session.execute(bs);
    }

    // ✅ Cập nhật tổng chi tiêu & số đơn hàng
    public void updateSpending(UUID customerId, BigDecimal lifetimeSpent, int orderCount) {
        String query = """
            UPDATE loyalty_accounts
            SET lifetime_spent = ?, order_count = ?, last_updated = ?
            WHERE customer_id = ?
        """;
        PreparedStatement ps = session.prepare(query);
        BoundStatement bs = ps.bind(lifetimeSpent, orderCount, Instant.now(), customerId);
        session.execute(bs);
    }

    // ✅ Tìm theo customer_id
    public LoyaltyAccount findByCustomer(UUID customerId) {
        String query = "SELECT * FROM loyalty_accounts WHERE customer_id = ?";
        PreparedStatement ps = session.prepare(query);
        BoundStatement bs = ps.bind(customerId);
        Row row = session.execute(bs).one();
        return row != null ? mapRow(row) : null;
    }

    // ✅ Xóa
    public void delete(UUID customerId) {
        String query = "DELETE FROM loyalty_accounts WHERE customer_id = ?";
        PreparedStatement ps = session.prepare(query);
        BoundStatement bs = ps.bind(customerId);
        session.execute(bs);
    }

    // ✅ Lấy toàn bộ
    public List<LoyaltyAccount> findAll() {
        String query = "SELECT * FROM loyalty_accounts";
        ResultSet rs = session.execute(SimpleStatement.newInstance(query));
        List<LoyaltyAccount> list = new ArrayList<>();
        for (Row row : rs) list.add(mapRow(row));
        return list;
    }

    // ✅ Lấy theo tier (Silver, Gold, Platinum, v.v.)
    // ⚠️ Cassandra không hỗ trợ filter tùy ý, nên cần ALLOW FILTERING
    public List<LoyaltyAccount> findByTier(String tier) {
        String query = "SELECT * FROM loyalty_accounts WHERE tier = ? ALLOW FILTERING";
        PreparedStatement ps = session.prepare(query);
        BoundStatement bs = ps.bind(tier);
        ResultSet rs = session.execute(bs);

        List<LoyaltyAccount> list = new ArrayList<>();
        for (Row row : rs) {
            list.add(mapRow(row));
        }
        return list;
    }

    // ✅ Map Row → LoyaltyAccount
    private LoyaltyAccount mapRow(Row row) {
        LoyaltyAccount a = new LoyaltyAccount();
        a.setCustomerId(row.getUuid("customer_id"));
        a.setPoints(row.getLong("points"));
        a.setTier(row.getString("tier"));
        a.setLifetimeSpent(row.getBigDecimal("lifetime_spent"));
        a.setOrderCount(row.getInt("order_count"));
        a.setLastUpdated(row.getInstant("last_updated"));
        return a;
    }

    // ✅ Test nhanh
    public static void main(String[] args) {
        try (CqlSession session = KetNoICSDL.getSession()) {
            LoyaltyAccountDAO dao = new LoyaltyAccountDAO(session);

            // Test thêm mới
            UUID id = UUID.randomUUID();
            dao.save(new LoyaltyAccount(id, 1200, "Gold", new BigDecimal("3500000"), 4, Instant.now()));

            // Test tìm theo tier
            System.out.println("🔍 Danh sách Gold Members:");
            dao.findByTier("Gold").forEach(a ->
                    System.out.println(" - " + a.getCustomerId() + " | Points: " + a.getPoints()));

            // Xóa test
            dao.delete(id);
        }
    }
}
