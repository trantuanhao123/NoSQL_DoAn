package KetNoiCSDL;

import com.datastax.oss.driver.api.core.CqlSession;
import java.net.InetSocketAddress;

public class KetNoICSDL {
    private static CqlSession session;
    private static final String KEYSPACE_NAME = "DoAnSQL";

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
            System.out.println("Ket Noi Cassandra thanh cong va chon keyspace: " + KEYSPACE_NAME);
        } catch (Exception e) {
            System.err.println("Loi Ket Noi Cassandra: " + e.getMessage());
            throw new RuntimeException("Could not connect to Cassandra", e);
        }
    }

    public static void close() {
        if (session != null && !session.isClosed()) {
            session.close();
            session = null;
            System.out.println("🔒 Đã đóng kết nối Cassandra.");
        }
    }
}
