/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package KetNoiCSDL;
import java.net.InetSocketAddress;
import com.datastax.oss.driver.api.core.CqlSession;
/**
 *
 * @author HAO
 */
public class KetNoiCSDL {
    private CqlSession session;
    private boolean connected = false;

    // Hàm khởi tạo
    public KetNoiCSDL(String host, int port, String datacenter, String username, String password) {
        try {
            this.session = CqlSession.builder()
                    .addContactPoint(new InetSocketAddress(host, port))
                    .withLocalDatacenter(datacenter)
                    .withAuthCredentials(username, password)
                    .build();
            connected = true;
        } catch (Exception e) {
            connected = false;
            e.printStackTrace(); // nếu không muốn in lỗi thì bỏ dòng này
        }
    }

    // Kiểm tra kết nối
    public boolean isConnected() {
        return connected;
    }

    // Lấy session (để query Cassandra)
    public CqlSession getSession() {
        return session;
    }

    // Đóng kết nối
    public void close() {
        if (session != null) {
            session.close();
            connected = false;
        }
    }
}

