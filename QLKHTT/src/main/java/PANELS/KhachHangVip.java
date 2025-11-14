package PANELS;

import DAO.CustomerDAO;
import DAO.LoyaltyAccountDAO;
import KetNoiCSDL.KetNoICSDL;
import MODELS.Customer;
import MODELS.LoyaltyAccount;
import SERVICE.LoyaltyAccountService;
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

public class KhachHangVip extends javax.swing.JPanel {

    // DAO Objects
    private LoyaltyAccountDAO loyaltyDAO;
    private CustomerDAO customerDAO;
    
    // Service Object
    private LoyaltyAccountService loyaltyService; // Thêm Service

    // Table and Data
    private DefaultTableModel tableModel;
    private List<LoyaltyAccount> currentLoyaltyList;
    private Map<String, String> customerNamesMap; // Key là String

    /**
     * Creates new form KhachHangVip
     */
    public KhachHangVip() {
        initComponents();

        // 1. Khởi tạo DAO và Service
        try {
            CqlSession session = KetNoICSDL.getSession();
            this.loyaltyDAO = new LoyaltyAccountDAO(session);
            this.customerDAO = new CustomerDAO(session);
            this.loyaltyService = new LoyaltyAccountService(session); // Khởi tạo Service
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
        btnDelKHV.addActionListener(this::btnDelKHVActionPerformed);
        
        // Kích hoạt các trường và nút
        txtTen.setEnabled(true);
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
        if (loyaltyDAO == null || customerDAO == null) {
            return;
        }

        try {
            currentLoyaltyList = loyaltyDAO.findAll();
            customerNamesMap = new HashMap<>(); 
            tableModel.setRowCount(0);

            for (LoyaltyAccount account : currentLoyaltyList) {
                Customer customer = customerDAO.findById(account.getCustomerId()); 
                String fullName = (customer != null) ? customer.getFullName() : "N/A (Không tìm thấy KH)";
                customerNamesMap.put(account.getCustomerId(), fullName);

                Object[] rowData = new Object[]{
                    fullName,
                    account.getPoints(),
                    account.getTier(),
                    account.getLifetimeSpent(),
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
        if (selectedRow < 0) {
            return;
        }
        selectedRow = tblKH.convertRowIndexToModel(selectedRow);
        LoyaltyAccount selectedAccount = currentLoyaltyList.get(selectedRow);

        txtTen.setText(customerNamesMap.get(selectedAccount.getCustomerId()));
        jtfDiem.setText(String.valueOf(selectedAccount.getPoints()));
        cboTier.setSelectedItem(selectedAccount.getTier());
        jtfTChiTieu.setText(selectedAccount.getLifetimeSpent().toPlainString());
        jtfTDonHang.setText(String.valueOf(selectedAccount.getOrderCount()));
        btnDelKHV.putClientProperty("selected_id", selectedAccount.getCustomerId());
    }

    private void clearForm() {
        txtTen.setText("");
        jtfDiem.setText("");
        jtfTChiTieu.setText("");
        jtfTDonHang.setText("");
        cboTier.setSelectedIndex(-1);
        btnDelKHV.putClientProperty("selected_id", null);
        tblKH.clearSelection();
    }

    // -------------------------------------------------------------------------
    // --- XỬ LÝ NÚT CRUD ---
    // -------------------------------------------------------------------------
    private void btnNewKHVActionPerformed(java.awt.event.ActionEvent evt) {
        clearForm();
    }

    private void btnDelKHVActionPerformed(java.awt.event.ActionEvent evt) {
        // Lấy ID (String)
        String customerId = (String) btnDelKHV.getClientProperty("selected_id");
        if (customerId == null) {
            JOptionPane.showMessageDialog(this, "Vui lòng chọn khách hàng cần xóa.", "Cảnh báo", JOptionPane.WARNING_MESSAGE);
            return;
        }

        int confirm = JOptionPane.showConfirmDialog(this,
                "Bạn có chắc chắn muốn xóa khách hàng VIP (" + txtTen.getText() + ")?\n"
                + "!!! CẢNH BÁO !!!\n"
                + "Thao tác này sẽ xóa TÀI KHOẢN LOYALTY và TẤT CẢ ĐƠN HÀNG của họ.\n"
                + "Tài khoản Customer (chính) sẽ được giữ lại.",
                "Xác nhận xóa", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);

        if (confirm == JOptionPane.YES_OPTION) {
            try {
                // *** THAY ĐỔI QUAN TRỌNG ***
                // Gọi Service để xóa (Bao gồm 3 bảng: loyalty, order_by_id, order_by_customer)
                loyaltyService.deleteLoyaltyAndOrders(customerId);
                
                JOptionPane.showMessageDialog(this, "Xóa khách hàng và các đơn hàng liên quan thành công!");
                loadDataToTable(); // Tải lại bảng
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
        btnDelKHV = new javax.swing.JButton();
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

        btnDelKHV.setIcon(new javax.swing.ImageIcon(getClass().getResource("/ICONS/Delete.png"))); // NOI18N
        btnDelKHV.setText("Xoá khách hàng");
        btnDelKHV.setMinimumSize(new java.awt.Dimension(100, 55));
        btnDelKHV.setPreferredSize(new java.awt.Dimension(50, 50));

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
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(btnDelKHV, javax.swing.GroupLayout.PREFERRED_SIZE, 376, javax.swing.GroupLayout.PREFERRED_SIZE)
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
                .addGap(395, 395, 395))
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
                .addComponent(btnDelKHV, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
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
                "Họ và Tên", "Điểm", "Hạng", "Tổng chi tiêu", "Tổng đơn hàng", "Ngày Tạo"
            }
        ));
        jScrollPane1.setViewportView(tblKH);

        jSplitPane1.setRightComponent(jScrollPane1);

        add(jSplitPane1, java.awt.BorderLayout.CENTER);
    }// </editor-fold>//GEN-END:initComponents

    private void jtfTChiTieuActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jtfTChiTieuActionPerformed

    }//GEN-LAST:event_jtfTChiTieuActionPerformed

    private void btnLamMoiActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnLamMoiActionPerformed
        loadDataToTable();
    }//GEN-LAST:event_btnLamMoiActionPerformed


    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton btnDelKHV;
    private javax.swing.JButton btnLamMoi;
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
