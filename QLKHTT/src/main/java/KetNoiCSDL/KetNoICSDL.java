package KetNoiCSDL;

import com.datastax.oss.driver.api.core.CqlSession;
import com.datastax.oss.driver.api.core.cql.ResultSet;
import com.datastax.oss.driver.api.core.cql.Row;
import java.net.InetSocketAddress;

public class KetNoICSDL {
    private static CqlSession session;
    private static final String KEYSPACE_NAME = "QLKHTT";

    private KetNoICSDL() {}

    public static CqlSession getSession() {
        if (session == null || session.isClosed()) {
            synchronized (KetNoICSDL.class) {
                if (session == null || session.isClosed()) {
                    connect();
                }
            }
        }
        return session;
    }

    private static void connect() {
        try {
            session = CqlSession.builder()
                    .addContactPoint(new InetSocketAddress("127.0.0.1", 9042))
                    .withAuthCredentials("cassandra", "cassandra")
                    .withLocalDatacenter("datacenter1")
                    .withKeyspace(KEYSPACE_NAME)
                    .build();
            System.out.println("Ket noi Cassandra thanh cong va chon keyspace: " + KEYSPACE_NAME);
        } catch (Exception e) {
            System.err.println("Loi ket noi Cassandra: " + e.getMessage());
            throw new RuntimeException("Khong the ket noi den Cassandra", e);
        }
    }

    public static void close() {
        if (session != null && !session.isClosed()) {
            session.close();
            session = null;
            System.out.println("Da dong ket noi Cassandra.");
        }
    }

    // Ham main de test ket noi va truy van bang customers
    public static void main(String[] args) {
        System.out.println("Dang thu ket noi toi Cassandra...");

        try {
            // Mo ket noi
            CqlSession testSession = KetNoICSDL.getSession();

            // In thong tin cluster va keyspace hien tai
            System.out.println("Thong tin Cluster: " + testSession.getMetadata().getClusterName().orElse("Khong ro"));
            System.out.println("Keyspace hien tai: " + testSession.getKeyspace().orElse(null));

            // Thu truy van phien ban Cassandra
            testSession.execute("SELECT release_version FROM system.local")
                    .forEach(row -> System.out.println("Phien ban Cassandra: " + row.getString("release_version")));

            // Thu truy van du lieu bang customers
            System.out.println("\nDang truy van du lieu tu bang customers...");
            ResultSet rs = testSession.execute("SELECT * FROM customers;");

            for (Row row : rs) {
                System.out.println("--------------------------------------");
                System.out.println("Customer ID: " + row.getUuid("customer_id"));
                System.out.println("Full Name  : " + row.getString("full_name"));
                System.out.println("Email      : " + row.getString("email"));
                System.out.println("Phone      : " + row.getString("phone"));
                System.out.println("DOB        : " + row.getLocalDate("dob"));
                System.out.println("Gender     : " + row.getString("gender"));
                System.out.println("Address    : " + row.getString("address"));
                System.out.println("Created At : " + row.getInstant("created_at"));
                System.out.println("Status     : " + row.getString("status"));
            }

        } catch (Exception e) {
            System.err.println("Loi khi test ket noi hoac truy van: " + e.getMessage());
            e.printStackTrace();
        } finally {
            // Dong ket noi
            KetNoICSDL.close();
        }
    }
}
