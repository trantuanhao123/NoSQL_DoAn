package com.mycompany.doannosql;

import KetNoiCSDL.KetNoiCSDL;

public class DoAnNoSQL {
    public static void main(String[] args) {
        KetNoiCSDL ketNoi = new KetNoiCSDL("127.0.0.1", 9042, "datacenter1", "cassandra", "cassandra");

        System.out.println("Ket Noi Thanh Cong: " + (ketNoi.isConnected() ? 1 : 0));

        // Nếu kết nối thành công thì có thể dùng session để query
        if (ketNoi.isConnected()) {
            // ví dụ: ketNoi.getSession().execute("SELECT * FROM system.local");
        }

        ketNoi.close();
    }
}
