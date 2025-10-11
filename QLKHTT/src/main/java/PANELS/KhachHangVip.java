/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/GUIForms/JPanel.java to edit this template
 */
package PANELS;

import DAO.CustomerDAO;
import DAO.LoyaltyAccountDAO;
import KetNoiCSDL.KetNoICSDL;
import MODELS.Customer;
import MODELS.LoyaltyAccount;
import com.datastax.oss.driver.api.core.CqlSession;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import javax.swing.JOptionPane;
import javax.swing.table.DefaultTableModel;

/**
 *
 * @author HAO
 */
public class KhachHangVip extends javax.swing.JPanel {
    private LoyaltyAccountDAO loyaltyDAO;
    private CustomerDAO customerDAO;
    private DefaultTableModel tableModel;

    // Lưu trữ danh sách LoyaltyAccount hiện tại và ánh xạ ID với tên
    private List<LoyaltyAccount> currentLoyaltyList;
    private Map<UUID, String> customerNamesMap; // Ma

    /**
     * Creates new form KhachHangVip
     */
     public KhachHangVip() {
        initComponents();
        
        // 1. Khởi tạo DAO
        try {
            CqlSession session = KetNoICSDL.getSession();
            this.loyaltyDAO = new LoyaltyAccountDAO(session);
            this.customerDAO = new CustomerDAO(session);
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Lỗi kết nối CSDL: " + e.getMessage(), "Lỗi", JOptionPane.ERROR_MESSAGE);
            e.printStackTrace();
            return; 
        }
        
        // 2. Cấu hình bảng và dữ liệu
        initTable();
        initTierComboBox();
        loadDataToTable();
        
        // 3. Thiết lập sự kiện click Table
        tblKH.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                tableClick();
            }
        });
        
        // 4. Gắn sự kiện cho các nút CRUD
        btnNewKHV.addActionListener(this::btnNewKHVActionPerformed);
        btnAddKHV.addActionListener(this::btnAddKHVActionPerformed);
        btnEditKHV.addActionListener(this::btnEditKHVActionPerformed);
        btnDelKHV.addActionListener(this::btnDelKHVActionPerformed);
    }
      private void initTierComboBox() {
        String[] tiers = new String[]{"Bronze", "Silver", "Gold", "Platinum", "Diamond"};
        cboTier.setModel(new javax.swing.DefaultComboBoxModel<>(tiers));
        cboTier.setSelectedIndex(-1);
    }

    private void initTable() {
        tableModel = (DefaultTableModel) tblKH.getModel();
        tableModel.setRowCount(0); 
    }
    public void loadDataToTable() {
        if (loyaltyDAO == null) return;
        
        try {
            currentLoyaltyList = loyaltyDAO.findAll();
            customerNamesMap = new HashMap<>(); // Khởi tạo map tên
            tableModel.setRowCount(0);

            for (LoyaltyAccount account : currentLoyaltyList) {
                // Lấy tên khách hàng từ bảng 'customers'
                Customer customer = customerDAO.findById(account.getCustomerId());
                String fullName = (customer != null) ? customer.getFullName() : "N/A (Không tìm thấy KH)";
                
                // Lưu vào Map để dùng khi click
                customerNamesMap.put(account.getCustomerId(), fullName);
                
                // Chuẩn bị dữ liệu cho Row
                Object[] rowData = new Object[]{
                    fullName,
                    account.getPoints(),
                    account.getTier(),
                    account.getLifetimeSpent(), // Giữ nguyên BigDecimal để sắp xếp/xử lý
                    account.getOrderCount(),
                    account.getLastUpdated()
                };
                tableModel.addRow(rowData);
            }
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Lỗi khi tải dữ liệu: " + e.getMessage(), "Lỗi", JOptionPane.ERROR_MESSAGE);
            e.printStackTrace();
        }
    }
    private void tableClick() {
        int selectedRow = tblKH.getSelectedRow();
        if (selectedRow < 0) return;

        // Lấy đối tượng LoyaltyAccount từ danh sách đã load
        LoyaltyAccount selectedAccount = currentLoyaltyList.get(selectedRow);

        // Đổ dữ liệu lên các JTextField/JComboBox
        
        // 1. Họ và tên (Lấy từ Map đã lưu)
        txtTen.setText(customerNamesMap.get(selectedAccount.getCustomerId()));

        // 2. Điểm
        jtfDiem.setText(String.valueOf(selectedAccount.getPoints()));

        // 3. Hạng/Tier
        cboTier.setSelectedItem(selectedAccount.getTier());

        // 4. Tổng chi tiêu
        jtfTChiTieu.setText(selectedAccount.getLifetimeSpent().toPlainString());

        // 5. Tổng đơn hàng
        jtfTDonHang.setText(String.valueOf(selectedAccount.getOrderCount()));

        // Lưu trữ ID khách hàng vào nút Sửa/Xóa (Client Property)
        btnEditKHV.putClientProperty("selected_id", selectedAccount.getCustomerId());
        btnDelKHV.putClientProperty("selected_id", selectedAccount.getCustomerId());
    }
    
    /**
     * Xóa trắng form nhập liệu.
     */
    private void clearForm() {
        txtTen.setText("");
        jtfDiem.setText("");
        jtfTChiTieu.setText("");
        jtfTDonHang.setText("");
        cboTier.setSelectedIndex(-1);
        btnEditKHV.putClientProperty("selected_id", null);
        btnDelKHV.putClientProperty("selected_id", null);
        tblKH.clearSelection();
    }
    
    // -------------------------------------------------------------------------
    // --- XỬ LÝ NÚT CRUD ---
    // -------------------------------------------------------------------------

    private void btnNewKHVActionPerformed(java.awt.event.ActionEvent evt) {                                        
        clearForm();
    }                                       

    private void btnAddKHVActionPerformed(java.awt.event.ActionEvent evt) {                                        
        try {
            // Yêu cầu: Khách hàng cần phải được tạo trong bảng `customers` trước
            // vì `loyalty_accounts` chỉ là thông tin phụ trợ.
            
            // 1. Validate và Lấy dữ liệu
            String fullName = txtTen.getText().trim();
            if (fullName.isEmpty() || jtfDiem.getText().isEmpty() || jtfTChiTieu.getText().isEmpty() || jtfTDonHang.getText().isEmpty() || cboTier.getSelectedIndex() == -1) {
                JOptionPane.showMessageDialog(this, "Vui lòng nhập đầy đủ thông tin.", "Lỗi", JOptionPane.WARNING_MESSAGE);
                return;
            }
            
            UUID newCustomerId = UUID.randomUUID(); 
            long points = Long.parseLong(jtfDiem.getText());
            String tier = (String) cboTier.getSelectedItem();
            BigDecimal lifetimeSpent = new BigDecimal(jtfTChiTieu.getText());
            int orderCount = Integer.parseInt(jtfTDonHang.getText());
            
            // 2. Thêm vào bảng Customers (Giả định thông tin cơ bản)
            Customer newCustomer = new Customer(
                newCustomerId, fullName, "default@email.com", "0000000000", 
                LocalDate.now(), "Unknown", "N/A", Instant.now(), "Active");
            customerDAO.save(newCustomer);
            
            // 3. Thêm vào bảng LoyaltyAccount
            LoyaltyAccount newAccount = new LoyaltyAccount(
                    newCustomerId, points, tier, lifetimeSpent, orderCount, Instant.now());
            loyaltyDAO.save(newAccount);
            
            JOptionPane.showMessageDialog(this, "Thêm khách hàng VIP thành công!");
            loadDataToTable();
            clearForm();
        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(this, "Dữ liệu Điểm, Chi tiêu, Đơn hàng phải là số hợp lệ.", "Lỗi nhập liệu", JOptionPane.WARNING_MESSAGE);
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Lỗi khi thêm: " + e.getMessage(), "Lỗi", JOptionPane.ERROR_MESSAGE);
            e.printStackTrace();
        }
    }                                       

    private void btnEditKHVActionPerformed(java.awt.event.ActionEvent evt) {                                        
        UUID customerId = (UUID) btnEditKHV.getClientProperty("selected_id");
        if (customerId == null) {
            JOptionPane.showMessageDialog(this, "Vui lòng chọn khách hàng cần sửa.", "Cảnh báo", JOptionPane.WARNING_MESSAGE);
            return;
        }

        try {
            // 1. Validate và Lấy thông tin mới
            String fullName = txtTen.getText().trim();
            long points = Long.parseLong(jtfDiem.getText());
            String tier = (String) cboTier.getSelectedItem();
            BigDecimal lifetimeSpent = new BigDecimal(jtfTChiTieu.getText());
            int orderCount = Integer.parseInt(jtfTDonHang.getText());
            
            // 2. Cập nhật thông tin Loyalty
            loyaltyDAO.updatePointsAndTier(customerId, points, tier);
            loyaltyDAO.updateSpending(customerId, lifetimeSpent, orderCount);
            
            // 3. Cập nhật tên khách hàng (trong CustomerDAO, ta cần thêm hàm updateFullName)
            // Vì CustomerDAO hiện tại chỉ có save/findById, ta giả định gọi update
            Customer existingCustomer = customerDAO.findById(customerId);
            if (existingCustomer != null) {
                existingCustomer.setFullName(fullName);
                // Vì Cassandra dùng Primary Key để xác định row, hàm save có thể dùng để update
                // Nếu muốn chỉ update tên: CustomerDAO cần thêm phương thức updateFullName
                customerDAO.save(existingCustomer); 
            }
            
            JOptionPane.showMessageDialog(this, "Cập nhật thông tin thành công!");
            loadDataToTable();
            clearForm();
        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(this, "Dữ liệu Điểm, Chi tiêu, Đơn hàng phải là số hợp lệ.", "Lỗi nhập liệu", JOptionPane.WARNING_MESSAGE);
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Lỗi khi cập nhật: " + e.getMessage(), "Lỗi", JOptionPane.ERROR_MESSAGE);
            e.printStackTrace();
        }
    }                                       

    private void btnDelKHVActionPerformed(java.awt.event.ActionEvent evt) {                                        
        UUID customerId = (UUID) btnDelKHV.getClientProperty("selected_id");
        if (customerId == null) {
            JOptionPane.showMessageDialog(this, "Vui lòng chọn khách hàng cần xóa.", "Cảnh báo", JOptionPane.WARNING_MESSAGE);
            return;
        }
        
        int confirm = JOptionPane.showConfirmDialog(this, 
                "Bạn có chắc chắn muốn xóa khách hàng VIP (" + txtTen.getText() + ")? Thao tác này sẽ xóa dữ liệu ở cả 2 bảng.", 
                "Xác nhận xóa", JOptionPane.YES_NO_OPTION);
        
        if (confirm == JOptionPane.YES_OPTION) {
            try {
                // 1. Xóa trong LoyaltyAccount
                loyaltyDAO.delete(customerId);
                // 2. Xóa trong Customers
                customerDAO.delete(customerId); 
                
                JOptionPane.showMessageDialog(this, "Xóa khách hàng thành công!");
                loadDataToTable();
                clearForm();
            } catch (Exception e) {
                JOptionPane.showMessageDialog(this, "Lỗi khi xóa: " + e.getMessage(), "Lỗi", JOptionPane.ERROR_MESSAGE);
                e.printStackTrace();
            }
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
        jLabel2 = new javax.swing.JLabel();
        jtfDiem = new javax.swing.JTextField();
        jLabel3 = new javax.swing.JLabel();
        jtfTChiTieu = new javax.swing.JTextField();
        jLabel4 = new javax.swing.JLabel();
        jtfTDonHang = new javax.swing.JTextField();
        jLabel5 = new javax.swing.JLabel();
        btnAddKHV = new javax.swing.JButton();
        btnEditKHV = new javax.swing.JButton();
        btnDelKHV = new javax.swing.JButton();
        btnNewKHV = new javax.swing.JButton();
        cboTier = new javax.swing.JComboBox<>();
        btnLamMoi = new javax.swing.JButton();
        txtTen = new javax.swing.JTextField();
        jScrollPane1 = new javax.swing.JScrollPane();
        tblKH = new javax.swing.JTable();

        setLayout(new java.awt.BorderLayout());

        jSplitPane1.setDividerLocation(220);
        jSplitPane1.setOrientation(javax.swing.JSplitPane.VERTICAL_SPLIT);

        jLabel1.setHorizontalAlignment(javax.swing.SwingConstants.TRAILING);
        jLabel1.setText("Họ và tên");

        jLabel2.setHorizontalAlignment(javax.swing.SwingConstants.TRAILING);
        jLabel2.setText("Điểm");
        jLabel2.setPreferredSize(new java.awt.Dimension(20, 22));

        jLabel3.setHorizontalAlignment(javax.swing.SwingConstants.TRAILING);
        jLabel3.setText("Tổng chi tiêu");
        jLabel3.setPreferredSize(new java.awt.Dimension(51, 16));
        jLabel3.setRequestFocusEnabled(false);

        jtfTChiTieu.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jtfTChiTieuActionPerformed(evt);
            }
        });

        jLabel4.setHorizontalAlignment(javax.swing.SwingConstants.TRAILING);
        jLabel4.setText("Tổng đơn hàng");

        jLabel5.setHorizontalAlignment(javax.swing.SwingConstants.TRAILING);
        jLabel5.setText("Hạng");

        btnAddKHV.setIcon(new javax.swing.ImageIcon(getClass().getResource("/ICONS/Add.png"))); // NOI18N
        btnAddKHV.setText("Thêm khách hàng");
        btnAddKHV.setEnabled(false);
        btnAddKHV.setPreferredSize(new java.awt.Dimension(50, 50));

        btnEditKHV.setIcon(new javax.swing.ImageIcon(getClass().getResource("/ICONS/Repair.png"))); // NOI18N
        btnEditKHV.setText("Sửa thông tin");
        btnEditKHV.setEnabled(false);
        btnEditKHV.setMaximumSize(new java.awt.Dimension(114, 57));
        btnEditKHV.setMinimumSize(new java.awt.Dimension(103, 31));
        btnEditKHV.setPreferredSize(new java.awt.Dimension(50, 50));

        btnDelKHV.setIcon(new javax.swing.ImageIcon(getClass().getResource("/ICONS/Delete.png"))); // NOI18N
        btnDelKHV.setText("Xoá khách hàng");
        btnDelKHV.setMinimumSize(new java.awt.Dimension(100, 55));
        btnDelKHV.setPreferredSize(new java.awt.Dimension(50, 50));

        btnNewKHV.setIcon(new javax.swing.ImageIcon(getClass().getResource("/ICONS/bookicon.png"))); // NOI18N
        btnNewKHV.setText("Tạo mới khách hàng");
        btnNewKHV.setEnabled(false);
        btnNewKHV.setPreferredSize(new java.awt.Dimension(50, 50));

        cboTier.setModel(new javax.swing.DefaultComboBoxModel<>(new String[] { "Item 1", "Item 2", "Item 3", "Item 4" }));

        btnLamMoi.setText("Làm Mới");
        btnLamMoi.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnLamMoiActionPerformed(evt);
            }
        });

        txtTen.setEnabled(false);

        javax.swing.GroupLayout jPanel1Layout = new javax.swing.GroupLayout(jPanel1);
        jPanel1.setLayout(jPanel1Layout);
        jPanel1Layout.setHorizontalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel1Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addComponent(btnDelKHV, javax.swing.GroupLayout.PREFERRED_SIZE, 161, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(btnEditKHV, javax.swing.GroupLayout.PREFERRED_SIZE, 182, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(btnAddKHV, javax.swing.GroupLayout.PREFERRED_SIZE, 205, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(btnNewKHV, javax.swing.GroupLayout.PREFERRED_SIZE, 205, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.CENTER)
                            .addComponent(jLabel2, javax.swing.GroupLayout.PREFERRED_SIZE, 67, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(jLabel5)
                            .addComponent(jLabel1, javax.swing.GroupLayout.PREFERRED_SIZE, 62, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                            .addComponent(txtTen)
                            .addComponent(jtfDiem)
                            .addComponent(cboTier, 0, 78, Short.MAX_VALUE))
                        .addGap(0, 0, 0)
                        .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                            .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                                .addComponent(jLabel3, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                .addComponent(jLabel4, javax.swing.GroupLayout.DEFAULT_SIZE, 99, Short.MAX_VALUE))
                            .addComponent(btnLamMoi))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                            .addComponent(jtfTDonHang, javax.swing.GroupLayout.DEFAULT_SIZE, 120, Short.MAX_VALUE)
                            .addComponent(jtfTChiTieu))))
                .addGap(0, 0, 0))
        );

        jPanel1Layout.linkSize(javax.swing.SwingConstants.HORIZONTAL, new java.awt.Component[] {jLabel2, jLabel5});

        jPanel1Layout.setVerticalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addGap(28, 28, 28)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel1)
                    .addComponent(jLabel3, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jtfTChiTieu, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(txtTen, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addGap(12, 12, 12)
                        .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(jLabel2, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(jtfDiem, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)))
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addGap(9, 9, 9)
                        .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(jLabel4)
                            .addComponent(jtfTDonHang, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))))
                .addGap(12, 12, 12)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel5)
                    .addComponent(cboTier, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(btnLamMoi))
                .addGap(29, 29, 29)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(btnDelKHV, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(btnEditKHV, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(btnAddKHV, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(btnNewKHV, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(17, 17, 17))
        );

        jSplitPane1.setTopComponent(jPanel1);

        tblKH.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {
                {null, null, null, null, null, null},
                {null, null, null, null, null, null},
                {null, null, null, null, null, null},
                {null, null, null, null, null, null}
            },
            new String [] {
                "Họ và Tên", "Điểm", "Hạng", "Tổng chi tiêu", "Tổng đơn hàng", "Created"
            }
        ));
        jScrollPane1.setViewportView(tblKH);

        jSplitPane1.setRightComponent(jScrollPane1);

        add(jSplitPane1, java.awt.BorderLayout.CENTER);
    }// </editor-fold>//GEN-END:initComponents

    private void jtfTChiTieuActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jtfTChiTieuActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_jtfTChiTieuActionPerformed

    private void btnLamMoiActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnLamMoiActionPerformed
        loadDataToTable();
    }//GEN-LAST:event_btnLamMoiActionPerformed


    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton btnAddKHV;
    private javax.swing.JButton btnDelKHV;
    private javax.swing.JButton btnEditKHV;
    private javax.swing.JButton btnLamMoi;
    private javax.swing.JButton btnNewKHV;
    private javax.swing.JComboBox<String> cboTier;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JLabel jLabel4;
    private javax.swing.JLabel jLabel5;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JSplitPane jSplitPane1;
    private javax.swing.JTextField jtfDiem;
    private javax.swing.JTextField jtfTChiTieu;
    private javax.swing.JTextField jtfTDonHang;
    private javax.swing.JTable tblKH;
    private javax.swing.JTextField txtTen;
    // End of variables declaration//GEN-END:variables
}
