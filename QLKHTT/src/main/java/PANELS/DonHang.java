/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/GUIForms/JPanel.java to edit this template
 */
package PANELS;

import DAO.CustomerDAO;
import DAO.DaoMapper;
import DAO.DaoMapperBuilder;
import DAO.OrderService;
import DAO.ProductDAO;
import KetNoiCSDL.KetNoICSDL;
import MODELS.OrderByCustomer;
import MODELS.OrderById;
import MODELS.OrderItem;
import com.datastax.oss.driver.api.core.CqlSession;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.UUID;
import java.math.BigDecimal;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.swing.JOptionPane;
import javax.swing.SwingWorker;
import javax.swing.table.DefaultTableModel;

/**
 *
 * @author HAO
 */

public class DonHang extends javax.swing.JPanel {

    /**
     * Creates new form DonHang
     */
    private final Map<String, UUID> customerMap = new HashMap<>();
    private final CqlSession session;

    public DonHang() {
        initComponents();
        this.session = KetNoICSDL.getSession(); 
        setupTables();
        loadCustomers();
        loadProducts();
        loadOrders();
        
        btnAddDH.addActionListener(e -> handleAddOrder());
        btnEditDH.addActionListener(e -> handleEditOrder());
        btnDelDH.addActionListener(e -> handleDeleteOrder());
        btnNewDH.addActionListener(e -> clearOrderForm());
        btnLamMoi.addActionListener(e -> loadOrders());
        btnXoa.addActionListener(e -> removeSelectedProduct());

        // Khi chọn đơn hàng => xem chi tiết
        tblDonHang.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting() && tblDonHang.getSelectedRow() != -1) {
                showOrderDetails();
            }
        });

        // Khi click sản phẩm => thêm vào giỏ
        tblLaptop.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                addProductToOrder();
            }
        });
    }
     // ======================== SETUP ============================
    private void setupTables() {
        tblDanhSachSanPham.setModel(new DefaultTableModel(
                new Object[][]{},
                new String[]{"Tên sản phẩm", "Số lượng", "Đơn giá", "Thành tiền"}
        ) {
            @Override
            public boolean isCellEditable(int row, int col) {
                return col == 1;
            }
        });

        tblLaptop.setModel(new DefaultTableModel(
                new Object[][]{},
                new String[]{"Mã SP", "Model", "Giá"}
        ));
    }

    // ======================== LOAD DATA ============================
    private void loadCustomers() {
        try {
            DaoMapper mapper = new DaoMapperBuilder(session).build();
            CustomerDAO customerDAO = mapper.customerDAO();

            customerMap.clear();
            cboTenKH.removeAllItems();

            customerDAO.findAllWithFiltering().forEach(c -> {
                String name = c.getFullName() != null ? c.getFullName() : "(Không tên)";
                cboTenKH.addItem(name);
                customerMap.put(name, c.getCustomerId());
            });
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Lỗi tải khách hàng: " + e.getMessage());
        }
    }

    private void loadProducts() {
        try {
            DaoMapper mapper = new DaoMapperBuilder(session).build();
            ProductDAO productDAO = mapper.productDAO();

            DefaultTableModel model = (DefaultTableModel) tblLaptop.getModel();
            model.setRowCount(0);

            productDAO.findAll().forEach(p ->
                model.addRow(new Object[]{p.getProductId(), p.getModel(), p.getPrice()})
            );
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Lỗi tải sản phẩm: " + e.getMessage());
        }
    }

    private void loadOrders() {
        DefaultTableModel model = new DefaultTableModel(
                new Object[]{"Mã đơn hàng", "Khách hàng", "Ngày đặt", "Tổng tiền", "Trạng thái"}, 0
        );
        tblDonHang.setModel(model);

        SwingWorker<Void, Object[]> worker = new SwingWorker<>() {
            @Override
            protected Void doInBackground() {
                try {
                    OrderService service = new OrderService(session);
                    for (Map.Entry<String, UUID> entry : customerMap.entrySet()) {
                        List<OrderByCustomer> orders = service.getOrdersByCustomer(entry.getValue());
                        for (OrderByCustomer o : orders) {
                            publish(new Object[]{
                                    o.getOrderId(), entry.getKey(), o.getOrderDate(), o.getTotal(), o.getStatus()
                            });
                        }
                    }
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
                return null;
            }

            @Override
            protected void process(List<Object[]> chunks) {
                for (Object[] row : chunks) {
                    model.addRow(row);
                }
            }
        };
        worker.execute();
    }

    // ======================== CRUD ============================
    private void handleAddOrder() {
        String customerName = (String) cboTenKH.getSelectedItem();
        if (customerName == null || !customerMap.containsKey(customerName)) {
            JOptionPane.showMessageDialog(this, "Vui lòng chọn khách hàng!");
            return;
        }

        UUID customerId = customerMap.get(customerName);
        List<OrderItem> items = collectOrderItems();
        if (items.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Chưa có sản phẩm trong đơn hàng!");
            return;
        }

        try {
            OrderService service = new OrderService(session);
            service.createOrder(customerId, items);
            JOptionPane.showMessageDialog(this, "✅ Thêm đơn hàng thành công!");
            clearOrderForm();
            loadOrders();
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "❌ Lỗi thêm đơn hàng: " + e.getMessage());
        }
    }

    private void handleEditOrder() {
        int row = tblDonHang.getSelectedRow();
        if (row == -1) {
            JOptionPane.showMessageDialog(this, "Vui lòng chọn đơn hàng cần sửa!");
            return;
        }

        UUID orderId = UUID.fromString(tblDonHang.getValueAt(row, 0).toString());
        String customerName = tblDonHang.getValueAt(row, 1).toString();
        UUID customerId = customerMap.get(customerName);

        List<OrderItem> newItems = collectOrderItems();
        if (newItems.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Đơn hàng phải có ít nhất 1 sản phẩm!");
            return;
        }

        try {
            OrderService service = new OrderService(session);
            service.deleteOrder(orderId, customerId);
            service.createOrder(customerId, newItems);
            JOptionPane.showMessageDialog(this, "✅ Cập nhật đơn hàng thành công!");
            loadOrders();
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "❌ Lỗi cập nhật đơn hàng: " + e.getMessage());
        }
    }

    private void handleDeleteOrder() {
        int row = tblDonHang.getSelectedRow();
        if (row == -1) {
            JOptionPane.showMessageDialog(this, "Vui lòng chọn đơn hàng cần xóa!");
            return;
        }

        UUID orderId = UUID.fromString(tblDonHang.getValueAt(row, 0).toString());
        String customerName = tblDonHang.getValueAt(row, 1).toString();
        UUID customerId = customerMap.get(customerName);

        int confirm = JOptionPane.showConfirmDialog(this,
                "Bạn có chắc chắn muốn xóa đơn hàng này?", "Xác nhận", JOptionPane.YES_NO_OPTION);
        if (confirm != JOptionPane.YES_OPTION) return;

        try {
            OrderService service = new OrderService(session);
            service.deleteOrder(orderId, customerId);
            JOptionPane.showMessageDialog(this, "🗑️ Xóa đơn hàng thành công!");
            loadOrders();
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "❌ Lỗi khi xóa đơn hàng: " + e.getMessage());
        }
    }

    // ======================== DETAIL ============================
    private void showOrderDetails() {
        int row = tblDonHang.getSelectedRow();
        if (row == -1) return;

        UUID orderId = UUID.fromString(tblDonHang.getValueAt(row, 0).toString());
        String customerName = tblDonHang.getValueAt(row, 1).toString();
        cboTenKH.setSelectedItem(customerName);
        txtNgayDat.setText("Đang tải...");

        SwingWorker<OrderById, Void> worker = new SwingWorker<>() {
            @Override
            protected OrderById doInBackground() throws Exception {
                OrderService service = new OrderService(session);
                return service.getOrderById(orderId);
            }

            @Override
            protected void done() {
                try {
                    OrderById order = get();
                    if (order == null) return;

                    DateTimeFormatter fmt = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss")
                            .withZone(ZoneId.systemDefault());
                    txtNgayDat.setText(fmt.format(order.getOrderDate()));

                    DefaultTableModel model = (DefaultTableModel) tblDanhSachSanPham.getModel();
                    model.setRowCount(0);
                    for (OrderItem item : order.getItems()) {
                        BigDecimal total = item.getPrice().multiply(BigDecimal.valueOf(item.getQuantity()));
                        model.addRow(new Object[]{item.getModel(), item.getQuantity(), item.getPrice(), total});
                    }
                } catch (Exception e) {
                    JOptionPane.showMessageDialog(DonHang.this, "Lỗi hiển thị chi tiết: " + e.getMessage());
                }
            }
        };
        worker.execute();
    }

    // ======================== HELPERS ============================
    private List<OrderItem> collectOrderItems() {
        List<OrderItem> list = new ArrayList<>();
        DefaultTableModel model = (DefaultTableModel) tblDanhSachSanPham.getModel();
        for (int i = 0; i < model.getRowCount(); i++) {
            String name = model.getValueAt(i, 0).toString();
            int qty = Integer.parseInt(model.getValueAt(i, 1).toString());
            BigDecimal price = new BigDecimal(model.getValueAt(i, 2).toString());
            list.add(new OrderItem(UUID.randomUUID().toString(), name, qty, price));
        }
        return list;
    }

    private void addProductToOrder() {
        int row = tblLaptop.getSelectedRow();
        if (row == -1) return;

        DefaultTableModel src = (DefaultTableModel) tblLaptop.getModel();
        DefaultTableModel dest = (DefaultTableModel) tblDanhSachSanPham.getModel();

        String model = src.getValueAt(row, 1).toString();
        BigDecimal price = new BigDecimal(src.getValueAt(row, 2).toString());

        for (int i = 0; i < dest.getRowCount(); i++) {
            if (dest.getValueAt(i, 0).equals(model)) {
                int qty = Integer.parseInt(dest.getValueAt(i, 1).toString()) + 1;
                dest.setValueAt(qty, i, 1);
                dest.setValueAt(price.multiply(BigDecimal.valueOf(qty)), i, 3);
                return;
            }
        }
        dest.addRow(new Object[]{model, 1, price, price});
    }

    private void clearOrderForm() {
        ((DefaultTableModel) tblDanhSachSanPham.getModel()).setRowCount(0);
        txtNgayDat.setText("");
        cboTenKH.setSelectedIndex(-1);
    }

    private void removeSelectedProduct() {
        int row = tblDanhSachSanPham.getSelectedRow();
        if (row != -1) {
            ((DefaultTableModel) tblDanhSachSanPham.getModel()).removeRow(row);
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

        jPanel1 = new javax.swing.JPanel();
        btnAddDH = new javax.swing.JButton();
        btnEditDH = new javax.swing.JButton();
        btnDelDH = new javax.swing.JButton();
        btnNewDH = new javax.swing.JButton();
        jLabel5 = new javax.swing.JLabel();
        cboTenKH = new javax.swing.JComboBox<>();
        jScrollPane2 = new javax.swing.JScrollPane();
        tblLaptop = new javax.swing.JTable();
        txtNgay = new javax.swing.JLabel();
        txtNgayDat = new javax.swing.JTextField();
        jScrollPane3 = new javax.swing.JScrollPane();
        tblDonHang = new javax.swing.JTable();
        jScrollPane1 = new javax.swing.JScrollPane();
        tblDanhSachSanPham = new javax.swing.JTable();
        btnLamMoi = new javax.swing.JButton();
        btnXoa = new javax.swing.JButton();

        setLayout(new java.awt.BorderLayout());

        btnAddDH.setIcon(new javax.swing.ImageIcon(getClass().getResource("/ICONS/Add.png"))); // NOI18N
        btnAddDH.setText("Thêm đơn hàng");
        btnAddDH.setPreferredSize(new java.awt.Dimension(50, 50));

        btnEditDH.setIcon(new javax.swing.ImageIcon(getClass().getResource("/ICONS/Repair.png"))); // NOI18N
        btnEditDH.setText("Sửa thông tin");
        btnEditDH.setMaximumSize(new java.awt.Dimension(114, 57));
        btnEditDH.setMinimumSize(new java.awt.Dimension(103, 31));
        btnEditDH.setPreferredSize(new java.awt.Dimension(50, 50));

        btnDelDH.setIcon(new javax.swing.ImageIcon(getClass().getResource("/ICONS/Delete.png"))); // NOI18N
        btnDelDH.setText("Xoá đơn hàng");
        btnDelDH.setMinimumSize(new java.awt.Dimension(100, 55));
        btnDelDH.setPreferredSize(new java.awt.Dimension(50, 50));

        btnNewDH.setIcon(new javax.swing.ImageIcon(getClass().getResource("/ICONS/bookicon.png"))); // NOI18N
        btnNewDH.setText("Tạo mới đơn hàng");
        btnNewDH.setPreferredSize(new java.awt.Dimension(50, 50));

        jLabel5.setText("Mã Khách Hàng");

        cboTenKH.setModel(new javax.swing.DefaultComboBoxModel<>(new String[] { "Item 1", "Item 2", "Item 3", "Item 4" }));

        tblLaptop.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {
                {null, null, null, null},
                {null, null, null, null},
                {null, null, null, null},
                {null, null, null, null}
            },
            new String [] {
                "Title 1", "Title 2", "Title 3", "Title 4"
            }
        ));
        jScrollPane2.setViewportView(tblLaptop);

        txtNgay.setText("Ngày Đặt");

        tblDonHang.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {
                {null, null, null, null},
                {null, null, null, null},
                {null, null, null, null},
                {null, null, null, null}
            },
            new String [] {
                "Title 1", "Title 2", "Title 3", "Title 4"
            }
        ));
        jScrollPane3.setViewportView(tblDonHang);

        tblDanhSachSanPham.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {
                {null, null, null, null}
            },
            new String [] {
                "Mã SP", "Model", "Số lượng", "Giá"
            }
        ));
        jScrollPane1.setViewportView(tblDanhSachSanPham);

        btnLamMoi.setText("Làm Mới");
        btnLamMoi.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnLamMoiActionPerformed(evt);
            }
        });

        btnXoa.setText("Xóa");
        btnXoa.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnXoaActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout jPanel1Layout = new javax.swing.GroupLayout(jPanel1);
        jPanel1.setLayout(jPanel1Layout);
        jPanel1Layout.setHorizontalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(btnEditDH, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addComponent(btnDelDH, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                        .addGap(18, 18, 18)
                        .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                            .addComponent(btnAddDH, javax.swing.GroupLayout.DEFAULT_SIZE, 307, Short.MAX_VALUE)
                            .addComponent(btnNewDH, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)))
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addContainerGap()
                        .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(jScrollPane3)
                            .addGroup(jPanel1Layout.createSequentialGroup()
                                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                                    .addComponent(jLabel5, javax.swing.GroupLayout.DEFAULT_SIZE, 89, Short.MAX_VALUE)
                                    .addComponent(txtNgay, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                                .addGap(18, 18, 18)
                                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                                    .addComponent(txtNgayDat, javax.swing.GroupLayout.DEFAULT_SIZE, 272, Short.MAX_VALUE)
                                    .addComponent(cboTenKH, 0, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                                .addGap(0, 252, Short.MAX_VALUE)))))
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addGap(3, 3, 3)
                        .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(jScrollPane2, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.PREFERRED_SIZE, 678, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(btnLamMoi, javax.swing.GroupLayout.Alignment.TRAILING)))
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel1Layout.createSequentialGroup()
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(btnXoa)))
                .addContainerGap())
            .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel1Layout.createSequentialGroup()
                    .addContainerGap(648, Short.MAX_VALUE)
                    .addComponent(jScrollPane1, javax.swing.GroupLayout.PREFERRED_SIZE, 590, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addContainerGap(86, Short.MAX_VALUE)))
        );
        jPanel1Layout.setVerticalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(jLabel5)
                            .addComponent(cboTenKH, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(txtNgayDat, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(txtNgay))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(btnEditDH, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(btnAddDH, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addGap(20, 20, 20)
                        .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(btnNewDH, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(btnDelDH, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)))
                    .addComponent(jScrollPane2, javax.swing.GroupLayout.PREFERRED_SIZE, 186, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addComponent(btnLamMoi)
                        .addGap(18, 18, 18)
                        .addComponent(btnXoa))
                    .addComponent(jScrollPane3, javax.swing.GroupLayout.PREFERRED_SIZE, 433, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
            .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel1Layout.createSequentialGroup()
                    .addContainerGap(196, Short.MAX_VALUE)
                    .addComponent(jScrollPane1, javax.swing.GroupLayout.PREFERRED_SIZE, 435, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addContainerGap()))
        );

        add(jPanel1, java.awt.BorderLayout.PAGE_START);
    }// </editor-fold>//GEN-END:initComponents

    private void btnLamMoiActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnLamMoiActionPerformed
        ((DefaultTableModel) tblDanhSachSanPham.getModel()).setRowCount(0);
    }//GEN-LAST:event_btnLamMoiActionPerformed

    private void btnXoaActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnXoaActionPerformed
        int selectedRow = tblDanhSachSanPham.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this, "Vui lòng chọn sản phẩm cần xóa!");
            return;
        }

        DefaultTableModel model = (DefaultTableModel) tblDanhSachSanPham.getModel();
        model.removeRow(selectedRow);
    }//GEN-LAST:event_btnXoaActionPerformed


    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton btnAddDH;
    private javax.swing.JButton btnDelDH;
    private javax.swing.JButton btnEditDH;
    private javax.swing.JButton btnLamMoi;
    private javax.swing.JButton btnNewDH;
    private javax.swing.JButton btnXoa;
    private javax.swing.JComboBox<String> cboTenKH;
    private javax.swing.JLabel jLabel5;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JScrollPane jScrollPane2;
    private javax.swing.JScrollPane jScrollPane3;
    private javax.swing.JTable tblDanhSachSanPham;
    private javax.swing.JTable tblDonHang;
    private javax.swing.JTable tblLaptop;
    private javax.swing.JLabel txtNgay;
    private javax.swing.JTextField txtNgayDat;
    // End of variables declaration//GEN-END:variables
}
