/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 */

package com.mycompany.doannosql;

import java.net.InetSocketAddress;
import com.datastax.oss.driver.api.core.CqlSession;
import com.datastax.oss.driver.api.core.cql.ResultSet;
import com.datastax.oss.driver.api.core.cql.Row;
/**
 *
 * @author HAO
 */

public class DoAnNoSQL {

    public static void main(String[] args) {
        int connected = 0; // 0 = false, 1 = true

        try (CqlSession session = CqlSession.builder()
                .addContactPoint(new InetSocketAddress("127.0.0.1", 9042))
                .withLocalDatacenter("datacenter1")
                .withAuthCredentials("cassandra", "cassandra")
                .withKeyspace("buoi04")
                .build()) {

            connected = 1;

            // Query bảng hotels
            ResultSet rs = session.execute("SELECT * FROM hotels");
            System.out.println("Dữ liệu bảng hotels:");
            for (Row row : rs) {
                System.out.println(
                        "hotel_id=" + row.getString("hotel_id") +
                        ", name=" + row.getString("name") +
                        ", address=" + row.getString("address") +
                        ", phone=" + row.getString("phone")
                );
            }

        } catch (Exception e) {
            connected = 0;
            e.printStackTrace();
        }

        System.out.println("Kết nối Cassandra thành công? " + connected);
    }
}