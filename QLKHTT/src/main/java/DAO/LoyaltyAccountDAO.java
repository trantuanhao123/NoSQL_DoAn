package DAO;
import KetNoiCSDL.KetNoICSDL;
import MODELS.LoyaltyAccount;
import com.datastax.oss.driver.api.core.CqlSession;
import com.datastax.oss.driver.api.core.cql.*;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.*;

public class LoyaltyAccountDAO {

    private final CqlSession session;

    public LoyaltyAccountDAO(CqlSession session) {
        this.session = session;
    }

    public void save(LoyaltyAccount account) {
        String query = """
            INSERT INTO loyalty_accounts (
                customer_id, points, tier, lifetime_spent, order_count, last_updated
            ) VALUES (?, ?, ?, ?, ?, ?)
        """;
        PreparedStatement ps = session.prepare(query);
        BoundStatement bs = ps.bind(
                account.getCustomerId(), // Đã là String từ Model
                account.getPoints(),
                account.getTier(),
                account.getLifetimeSpent(),
                account.getOrderCount(),
                account.getLastUpdated()
        );
        session.execute(bs);
    }

    public void updatePointsAndTier(String customerId, long points, String tier) {
        String query = """
            UPDATE loyalty_accounts
            SET points = ?, tier = ?, last_updated = ?
            WHERE customer_id = ?
        """;
        PreparedStatement ps = session.prepare(query);
        BoundStatement bs = ps.bind(points, tier, Instant.now(), customerId);
        session.execute(bs);
    }

    public void updateSpending(String customerId, BigDecimal lifetimeSpent, int orderCount) {
        String query = """
            UPDATE loyalty_accounts
            SET lifetime_spent = ?, order_count = ?, last_updated = ?
            WHERE customer_id = ?
        """;
        PreparedStatement ps = session.prepare(query);
        BoundStatement bs = ps.bind(lifetimeSpent, orderCount, Instant.now(), customerId);
        session.execute(bs);
    }

    public LoyaltyAccount findByCustomer(String customerId) {
        String query = "SELECT * FROM loyalty_accounts WHERE customer_id = ?";
        PreparedStatement ps = session.prepare(query);
        BoundStatement bs = ps.bind(customerId);
        Row row = session.execute(bs).one();
        return row != null ? mapRow(row) : null;
    }

    public void delete(String customerId) {
        String query = "DELETE FROM loyalty_accounts WHERE customer_id = ?";
        PreparedStatement ps = session.prepare(query);
        BoundStatement bs = ps.bind(customerId);
        session.execute(bs);
    }

    public List<LoyaltyAccount> findAll() {
        String query = "SELECT * FROM loyalty_accounts";
        ResultSet rs = session.execute(SimpleStatement.newInstance(query));
        List<LoyaltyAccount> list = new ArrayList<>();
        for (Row row : rs) {
            list.add(mapRow(row));
        }
        return list;
    }

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

    private LoyaltyAccount mapRow(Row row) {
        LoyaltyAccount a = new LoyaltyAccount();

        // THAY ĐỔI
        a.setCustomerId(row.getString("customer_id"));

        a.setPoints(row.getLong("points"));
        a.setTier(row.getString("tier"));
        a.setLifetimeSpent(row.getBigDecimal("lifetime_spent"));
        a.setOrderCount(row.getInt("order_count"));
        a.setLastUpdated(row.getInstant("last_updated"));
        return a;
    }
}
