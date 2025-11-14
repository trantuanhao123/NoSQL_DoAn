/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/GUIForms/JPanel.java to edit this template
 */
package PANELS;

import DAO.CustomerDAO;
import KetNoiCSDL.KetNoICSDL;
import MODELS.Customer;
import com.datastax.oss.driver.api.core.CqlSession;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.List;
import java.util.UUID;
import javax.swing.JOptionPane;
import javax.swing.event.ListSelectionEvent;
import javax.swing.table.DefaultTableModel;

/**
 *
 * @author HAO
 */
public class KhachHang extends javax.swing.JPanel {

    private DefaultTableModel tblModel;
    private CustomerDAO customerDAO;
    private String selectedCustomerId;

    public KhachHang() {
        initComponents();
        init();
    }

    private void init() {
        CqlSession session = KetNoICSDL.getSession();
        customerDAO = new CustomerDAO(session);
        tblModel = (DefaultTableModel) tblKH.getModel();
        tblModel.setRowCount(0);
        loadDataToTable();
        tblKH.getSelectionModel().addListSelectionListener((ListSelectionEvent event) -> {
            if (!event.getValueIsAdjusting() && tblKH.getSelectedRow() != -1) {
                showDetail();
            }
        });
        btnAddKH.addActionListener(e -> addCustomer());
        btnEditKH.addActionListener(e -> updateCustomer());
        btnDelKH.addActionListener(e -> deleteCustomer());
        btnNewKH.addActionListener(e -> clearForm());

        // THÊM: Đặt trạng thái ban đầu cho form
        clearForm();
    }

    private void loadDataToTable() {
        tblModel.setRowCount(0);
        try {
            List<Customer> customers = customerDAO.findAll();
            for (Customer c : customers) {
                tblModel.addRow(new Object[]{
                    c.getCustomerId(), // Này đã là String
                    c.getFullName(),
                    c.getEmail(),
                    c.getPhone(),
                    c.getDob(),
                    c.getGender(),
                    c.getAddress(),
                    c.getCreatedAt() != null ? c.getCreatedAt().atZone(ZoneId.systemDefault()).toLocalDate() : null,
                    c.getStatus()
                });
            }
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Lỗi tải dữ liệu: " + e.getMessage(), "Lỗi", JOptionPane.ERROR_MESSAGE);
            e.printStackTrace();
        }
    }

    private void showDetail() {
        int selectedRow = tblKH.getSelectedRow();
        if (selectedRow >= 0) {
            selectedCustomerId = (String) tblModel.getValueAt(selectedRow, 0);

            txtMaKH.setText(selectedCustomerId);
            txtMaKH.setEnabled(false);

            txtHoTen.setText(tblModel.getValueAt(selectedRow, 1).toString());
            txtEmail.setText(valueOrEmpty(tblModel.getValueAt(selectedRow, 2)));
            txtSDT.setText(valueOrEmpty(tblModel.getValueAt(selectedRow, 3)));
            txtNgaySinh.setText(valueOrEmpty(tblModel.getValueAt(selectedRow, 4)));
            cboGioiTinh.setSelectedItem(valueOrEmpty(tblModel.getValueAt(selectedRow, 5)));
            txtDiaChi.setText(valueOrEmpty(tblModel.getValueAt(selectedRow, 6)));

            String status = valueOrEmpty(tblModel.getValueAt(selectedRow, 8));
            jCheckBox1.setSelected(status.equalsIgnoreCase("Active"));
        }
    }

    private String valueOrEmpty(Object val) {
        return val != null ? val.toString() : "";
    }

    private void clearForm() {
        selectedCustomerId = null;
        txtMaKH.setText("");
        txtMaKH.setEnabled(true);

        txtHoTen.setText("");
        txtEmail.setText("");
        txtSDT.setText("");
        txtDiaChi.setText("");
        txtNgaySinh.setText("");
        cboGioiTinh.setSelectedIndex(0);
        jCheckBox1.setSelected(true);
        tblKH.clearSelection();
        txtMaKH.requestFocus();
    }

    private Customer getModelFromForm() {
        String fullName = txtHoTen.getText().trim();
        String email = txtEmail.getText().trim();
        String phone = txtSDT.getText().trim();
        String address = txtDiaChi.getText().trim();
        String dobString = txtNgaySinh.getText().trim();

        if (fullName.isEmpty() || phone.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Họ tên và SĐT không được trống!", "Cảnh báo", JOptionPane.WARNING_MESSAGE);
            return null;
        }
        LocalDate dob = null;
        if (dobString != null && !dobString.isEmpty()) {
            try {
                dob = LocalDate.parse(dobString, DateTimeFormatter.ISO_LOCAL_DATE);
            } catch (DateTimeParseException e) {
                JOptionPane.showMessageDialog(this, "Ngày sinh sai định dạng (YYYY-MM-DD).", "Lỗi", JOptionPane.ERROR_MESSAGE);
                return null;
            }
        }
        Customer c = new Customer();
        c.setFullName(fullName);
        c.setEmail(email.isEmpty() ? null : email);
        c.setPhone(phone);
        c.setDob(dob);
        c.setGender(cboGioiTinh.getSelectedItem().toString());
        c.setAddress(address.isEmpty() ? null : address);
        c.setStatus(jCheckBox1.isSelected() ? "Active" : "Inactive");
        return c;
    }

    private void addCustomer() {
        Customer c = getModelFromForm();
        if (c == null) {
            return;
        }
        String customerId = txtMaKH.getText().trim();
        if (customerId.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Mã Khách Hàng không được trống!", "Cảnh báo", JOptionPane.WARNING_MESSAGE);
            return;
        }
        if (customerDAO.findById(customerId) != null) {
            JOptionPane.showMessageDialog(this, "Mã Khách Hàng này đã tồn tại!", "Lỗi", JOptionPane.ERROR_MESSAGE);
            return;
        }
        try {
            c.setCustomerId(customerId);
            c.setCreatedAt(Instant.now());
            customerDAO.save(c);
            JOptionPane.showMessageDialog(this, "Thêm khách hàng thành công!");
            loadDataToTable();
            clearForm();
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Lỗi thêm khách hàng: " + e.getMessage(), "Lỗi", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void updateCustomer() {
        if (selectedCustomerId == null) {
            JOptionPane.showMessageDialog(this, "Vui lòng chọn khách hàng cần cập nhật!");
            return;
        }
        Customer c = getModelFromForm();
        if (c == null) {
            return;
        }
        try {
            c.setCustomerId(selectedCustomerId);
            Customer old = customerDAO.findById(selectedCustomerId);
            c.setCreatedAt(old != null ? old.getCreatedAt() : Instant.now());
            customerDAO.save(c);
            JOptionPane.showMessageDialog(this, "Cập nhật thành công!");
            loadDataToTable();
            clearForm();
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Lỗi cập nhật: " + e.getMessage(), "Lỗi", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void deleteCustomer() {
        if (selectedCustomerId == null) {
            JOptionPane.showMessageDialog(this, "Chưa chọn khách hàng để xóa!");
            return;
        }

        int confirm = JOptionPane.showConfirmDialog(this, "Xóa khách hàng này?", "Xác nhận", JOptionPane.YES_NO_OPTION);
        if (confirm != JOptionPane.YES_OPTION) {
            return;
        }

        try {
            // THAY ĐỔI: Truyền ID kiểu String
            customerDAO.delete(selectedCustomerId);
            JOptionPane.showMessageDialog(this, "Xóa thành công!");
            loadDataToTable();
            clearForm();
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Lỗi xóa: " + e.getMessage(), "Lỗi", JOptionPane.ERROR_MESSAGE);
        }
    }

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jSplitPane1 = new javax.swing.JSplitPane();
        jPanel1 = new javax.swing.JPanel();
        jLabel1 = new javax.swing.JLabel();
        txtHoTen = new javax.swing.JTextField();
        jLabel2 = new javax.swing.JLabel();
        txtSDT = new javax.swing.JTextField();
        jLabel3 = new javax.swing.JLabel();
        txtEmail = new javax.swing.JTextField();
        jLabel4 = new javax.swing.JLabel();
        jLabel5 = new javax.swing.JLabel();
        txtDiaChi = new javax.swing.JTextField();
        jLabel6 = new javax.swing.JLabel();
        jCheckBox1 = new javax.swing.JCheckBox();
        btnAddKH = new javax.swing.JButton();
        btnEditKH = new javax.swing.JButton();
        btnDelKH = new javax.swing.JButton();
        btnNewKH = new javax.swing.JButton();
        cboGioiTinh = new javax.swing.JComboBox<>();
        jLabel7 = new javax.swing.JLabel();
        txtNgaySinh = new javax.swing.JTextField();
        btnRenew = new javax.swing.JButton();
        jLabel8 = new javax.swing.JLabel();
        txtMaKH = new javax.swing.JTextField();
        jScrollPane1 = new javax.swing.JScrollPane();
        tblKH = new javax.swing.JTable();

        setLayout(new java.awt.BorderLayout());

        jSplitPane1.setDividerLocation(280);
        jSplitPane1.setOrientation(javax.swing.JSplitPane.VERTICAL_SPLIT);

        jLabel1.setHorizontalAlignment(javax.swing.SwingConstants.TRAILING);
        jLabel1.setText("Họ và tên");

        txtHoTen.setPreferredSize(new java.awt.Dimension(100, 22));

        jLabel2.setHorizontalAlignment(javax.swing.SwingConstants.TRAILING);
        jLabel2.setText("SDT");
        jLabel2.setPreferredSize(new java.awt.Dimension(20, 22));

        jLabel3.setHorizontalAlignment(javax.swing.SwingConstants.TRAILING);
        jLabel3.setText("Email");
        jLabel3.setPreferredSize(new java.awt.Dimension(51, 16));
        jLabel3.setRequestFocusEnabled(false);

        jLabel4.setHorizontalAlignment(javax.swing.SwingConstants.TRAILING);
        jLabel4.setText("Giới Tính");

        jLabel5.setHorizontalAlignment(javax.swing.SwingConstants.TRAILING);
        jLabel5.setText("Địa chỉ");

        txtDiaChi.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                txtDiaChiActionPerformed(evt);
            }
        });

        jLabel6.setHorizontalAlignment(javax.swing.SwingConstants.TRAILING);
        jLabel6.setText("Trạng Thái");

        jCheckBox1.setText("Có");
        jCheckBox1.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jCheckBox1ActionPerformed(evt);
            }
        });

        btnAddKH.setIcon(new javax.swing.ImageIcon(getClass().getResource("/ICONS/Add.png"))); // NOI18N
        btnAddKH.setText("Thêm khách hàng");
        btnAddKH.setPreferredSize(new java.awt.Dimension(50, 50));

        btnEditKH.setIcon(new javax.swing.ImageIcon(getClass().getResource("/ICONS/Repair.png"))); // NOI18N
        btnEditKH.setText("Sửa thông tin");
        btnEditKH.setMaximumSize(new java.awt.Dimension(114, 57));
        btnEditKH.setMinimumSize(new java.awt.Dimension(103, 31));
        btnEditKH.setPreferredSize(new java.awt.Dimension(50, 50));

        btnDelKH.setIcon(new javax.swing.ImageIcon(getClass().getResource("/ICONS/Delete.png"))); // NOI18N
        btnDelKH.setText("Xoá khách hàng");
        btnDelKH.setMinimumSize(new java.awt.Dimension(100, 55));
        btnDelKH.setPreferredSize(new java.awt.Dimension(50, 50));

        btnNewKH.setIcon(new javax.swing.ImageIcon(getClass().getResource("/ICONS/bookicon.png"))); // NOI18N
        btnNewKH.setText("Tạo mới khách hàng");
        btnNewKH.setPreferredSize(new java.awt.Dimension(50, 50));

        cboGioiTinh.setModel(new javax.swing.DefaultComboBoxModel<>(new String[] { "Nam", "Nữ" }));

        jLabel7.setHorizontalAlignment(javax.swing.SwingConstants.TRAILING);
        jLabel7.setText("Ngày Sinh");

        txtNgaySinh.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                txtNgaySinhActionPerformed(evt);
            }
        });

        btnRenew.setText("Làm Mới Bảng");
        btnRenew.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnRenewActionPerformed(evt);
            }
        });

        jLabel8.setText("Mã Khách Hàng");

        javax.swing.GroupLayout jPanel1Layout = new javax.swing.GroupLayout(jPanel1);
        jPanel1.setLayout(jPanel1Layout);
        jPanel1Layout.setHorizontalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addGap(0, 0, 0)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addComponent(btnDelKH, javax.swing.GroupLayout.PREFERRED_SIZE, 161, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(btnEditKH, javax.swing.GroupLayout.PREFERRED_SIZE, 182, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(btnAddKH, javax.swing.GroupLayout.PREFERRED_SIZE, 205, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(btnNewKH, javax.swing.GroupLayout.PREFERRED_SIZE, 205, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING, false)
                            .addComponent(jLabel2, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addComponent(jLabel5, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addComponent(jLabel7, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addComponent(jLabel1, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                        .addGap(12, 12, 12)
                        .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(jPanel1Layout.createSequentialGroup()
                                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                    .addComponent(txtDiaChi, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                    .addComponent(txtHoTen, javax.swing.GroupLayout.PREFERRED_SIZE, 164, javax.swing.GroupLayout.PREFERRED_SIZE)
                                    .addComponent(txtSDT, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                                .addGap(31, 31, 31)
                                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                                    .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                                        .addGroup(jPanel1Layout.createSequentialGroup()
                                            .addComponent(jLabel6)
                                            .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                            .addComponent(jCheckBox1, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                                        .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel1Layout.createSequentialGroup()
                                            .addComponent(jLabel3, javax.swing.GroupLayout.PREFERRED_SIZE, 78, javax.swing.GroupLayout.PREFERRED_SIZE)
                                            .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                            .addComponent(txtEmail, javax.swing.GroupLayout.PREFERRED_SIZE, 109, javax.swing.GroupLayout.PREFERRED_SIZE)))
                                    .addGroup(jPanel1Layout.createSequentialGroup()
                                        .addComponent(jLabel4)
                                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                                        .addComponent(cboGioiTinh, javax.swing.GroupLayout.PREFERRED_SIZE, 109, javax.swing.GroupLayout.PREFERRED_SIZE)))
                                .addGap(18, 18, 18)
                                .addComponent(jLabel8)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                                .addComponent(txtMaKH))
                            .addGroup(jPanel1Layout.createSequentialGroup()
                                .addComponent(txtNgaySinh, javax.swing.GroupLayout.PREFERRED_SIZE, 164, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addGap(50, 50, 50)
                                .addComponent(btnRenew)))))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        jPanel1Layout.linkSize(javax.swing.SwingConstants.HORIZONTAL, new java.awt.Component[] {txtDiaChi, txtHoTen, txtSDT});

        jPanel1Layout.linkSize(javax.swing.SwingConstants.HORIZONTAL, new java.awt.Component[] {jLabel3, jLabel6});

        jPanel1Layout.linkSize(javax.swing.SwingConstants.HORIZONTAL, new java.awt.Component[] {btnAddKH, btnDelKH, btnEditKH});

        jPanel1Layout.setVerticalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addGap(10, 10, 10)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(txtHoTen, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel1)
                    .addComponent(jLabel3, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(txtEmail, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel8)
                    .addComponent(txtMaKH, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addGap(12, 12, 12)
                        .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(jLabel2, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(txtSDT, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)))
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addGap(9, 9, 9)
                        .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(jLabel4)
                            .addComponent(cboGioiTinh, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))))
                .addGap(12, 12, 12)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                        .addComponent(jLabel5)
                        .addComponent(txtDiaChi, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                        .addComponent(jLabel6)
                        .addComponent(jCheckBox1)))
                .addGap(10, 10, 10)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                        .addComponent(txtNgaySinh, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addComponent(jLabel7))
                    .addComponent(btnRenew))
                .addGap(9, 9, 9)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(btnDelKH, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(btnEditKH, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(btnAddKH, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(btnNewKH, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(0, 0, 0))
        );

        jPanel1Layout.linkSize(javax.swing.SwingConstants.VERTICAL, new java.awt.Component[] {jLabel1, jLabel2, jLabel5, txtDiaChi, txtHoTen, txtSDT});

        jPanel1Layout.linkSize(javax.swing.SwingConstants.VERTICAL, new java.awt.Component[] {jLabel3, jLabel4, jLabel6});

        jPanel1Layout.linkSize(javax.swing.SwingConstants.VERTICAL, new java.awt.Component[] {jCheckBox1, txtEmail});

        jPanel1Layout.linkSize(javax.swing.SwingConstants.VERTICAL, new java.awt.Component[] {btnAddKH, btnDelKH, btnEditKH});

        jSplitPane1.setTopComponent(jPanel1);

        tblKH.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {
                {null, null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null, null}
            },
            new String [] {
                "Mã KH", "Họ và Tên", "Email", "SDT", "Ngày Sinh", "Giới Tính", "Địa chỉ", "Ngày Tạo", "Trạng Thái"
            }
        ));
        jScrollPane1.setViewportView(tblKH);

        jSplitPane1.setBottomComponent(jScrollPane1);

        add(jSplitPane1, java.awt.BorderLayout.CENTER);
    }// </editor-fold>//GEN-END:initComponents

    private void txtDiaChiActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_txtDiaChiActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_txtDiaChiActionPerformed

    private void jCheckBox1ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jCheckBox1ActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_jCheckBox1ActionPerformed

    private void txtNgaySinhActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_txtNgaySinhActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_txtNgaySinhActionPerformed

    private void btnRenewActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnRenewActionPerformed
        loadDataToTable();
    }//GEN-LAST:event_btnRenewActionPerformed


    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton btnAddKH;
    private javax.swing.JButton btnDelKH;
    private javax.swing.JButton btnEditKH;
    private javax.swing.JButton btnNewKH;
    private javax.swing.JButton btnRenew;
    private javax.swing.JComboBox<String> cboGioiTinh;
    private javax.swing.JCheckBox jCheckBox1;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JLabel jLabel4;
    private javax.swing.JLabel jLabel5;
    private javax.swing.JLabel jLabel6;
    private javax.swing.JLabel jLabel7;
    private javax.swing.JLabel jLabel8;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JSplitPane jSplitPane1;
    private javax.swing.JTable tblKH;
    private javax.swing.JTextField txtDiaChi;
    private javax.swing.JTextField txtEmail;
    private javax.swing.JTextField txtHoTen;
    private javax.swing.JTextField txtMaKH;
    private javax.swing.JTextField txtNgaySinh;
    private javax.swing.JTextField txtSDT;
    // End of variables declaration//GEN-END:variables
}
