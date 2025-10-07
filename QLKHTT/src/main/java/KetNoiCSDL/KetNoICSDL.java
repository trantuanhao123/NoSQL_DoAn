package KetNoiCSDL;

import com.datastax.oss.driver.api.core.CqlSession;
import java.net.InetSocketAddress;

public class KetNoICSDL {
    private static CqlSession session;

    public static void connect() {
        session = CqlSession.builder()
                .addContactPoint(new InetSocketAddress("127.0.0.1", 9042))
                .withAuthCredentials("cassandra", "cassandra")
                .withLocalDatacenter("datacenter1")
                .build();
        System.out.println("✅ Kết nối Cassandra thành công!");
    }

    public static void close() {
        if (session != null) session.close();
        System.out.println("🔒 Đã đóng kết nối Cassandra.");
    }

    public static void main(String[] args) {
        connect();
        close();
    }
}
