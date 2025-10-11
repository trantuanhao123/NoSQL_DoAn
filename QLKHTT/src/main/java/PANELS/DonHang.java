/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/GUIForms/JPanel.java to edit this template
 */
package PANELS;

import DAO.CustomerDAO;
import DAO.ProductDAO;
import DAO.OrderService;
import KetNoiCSDL.KetNoICSDL;
import MODELS.Customer;
import MODELS.Product;
import MODELS.OrderItem;
import MODELS.OrderByCustomer;
import MODELS.OrderById;
import com.datastax.oss.driver.api.core.CqlSession;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.math.BigDecimal;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.*;
import javax.swing.*;
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
    private final Map<String, Product> productMap = new HashMap<>();
    private final CqlSession session;
    private final CustomerDAO customerDAO;
    private final ProductDAO productDAO;
    private final OrderService orderService;

    public DonHang() {
        initComponents();
        this.session = KetNoICSDL.getSession();
        this.customerDAO = new CustomerDAO(session);
        this.productDAO = new ProductDAO(session);
        this.orderService = new OrderService(session);
        
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
        // Bảng danh sách sản phẩm trong đơn hàng
        tblDanhSachSanPham.setModel(new DefaultTableModel(
                new Object[][]{},
                new String[]{"Tên sản phẩm", "Số lượng", "Đơn giá", "Thành tiền"}
        ) {
            @Override
            public boolean isCellEditable(int row, int col) {
                return col == 1; // Chỉ cho phép sửa số lượng
            }
        });

        // Bảng danh sách laptop có sẵn
        tblLaptop.setModel(new DefaultTableModel(
                new Object[][]{},
                new String[]{"Mã SP", "Model", "Giá"}
        ) {
            @Override
            public boolean isCellEditable(int row, int col) {
                return false;
            }
        });

        // Bảng danh sách đơn hàng
        tblDonHang.setModel(new DefaultTableModel(
                new Object[][]{},
                new String[]{"Mã đơn hàng", "Khách hàng", "Ngày đặt", "Tổng tiền", "Trạng thái"}
        ) {
            @Override
            public boolean isCellEditable(int row, int col) {
                return false;
            }
        });
    }

    // ======================== LOAD DATA ============================
    private void loadCustomers() {
        try {
            customerMap.clear();
            cboTenKH.removeAllItems();

            List<Customer> customers = customerDAO.findAll();
            for (Customer c : customers) {
                String name = c.getFullName() != null ? c.getFullName() : "(Không tên)";
                cboTenKH.addItem(name);
                customerMap.put(name, c.getCustomerId());
            }
            
            if (cboTenKH.getItemCount() > 0) {
                cboTenKH.setSelectedIndex(-1);
            }
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "❌ Lỗi tải khách hàng: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void loadProducts() {
        try {
            productMap.clear();
            DefaultTableModel model = (DefaultTableModel) tblLaptop.getModel();
            model.setRowCount(0);

            List<Product> products = productDAO.findAllProducts();
            for (Product p : products) {
                if (p.isAvailable()) { // Chỉ hiển thị sản phẩm còn hàng
                    model.addRow(new Object[]{
                        p.getProductId(),
                        p.getModel(),
                        p.getPrice()
                    });
                    productMap.put(p.getProductId(), p);
                }
            }
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "❌ Lỗi tải sản phẩm: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void loadOrders() {
        DefaultTableModel model = (DefaultTableModel) tblDonHang.getModel();
        model.setRowCount(0);

        SwingWorker<Void, Object[]> worker = new SwingWorker<>() {
            @Override
            protected Void doInBackground() {
                try {
                    DateTimeFormatter fmt = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm")
                            .withZone(ZoneId.systemDefault());

                    for (Map.Entry<String, UUID> entry : customerMap.entrySet()) {
                        List<OrderByCustomer> orders = orderService.getOrdersByCustomer(entry.getValue());
                        for (OrderByCustomer o : orders) {
                            publish(new Object[]{
                                o.getOrderId(),
                                entry.getKey(),
                                fmt.format(o.getOrderDate()),
                                String.format("%,.0f đ", o.getTotal()),
                                o.getStatus()
                            });
                        }
                    }
                } catch (Exception ex) {
                    SwingUtilities.invokeLater(() -> 
                        JOptionPane.showMessageDialog(DonHang.this, 
                            "❌ Lỗi tải đơn hàng: " + ex.getMessage())
                    );
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

            @Override
            protected void done() {
                System.out.println("✅ Đã tải " + model.getRowCount() + " đơn hàng");
            }
        };
        worker.execute();
    }

    // ======================== CRUD ============================
    private void handleAddOrder() {
        String customerName = (String) cboTenKH.getSelectedItem();
        if (customerName == null || !customerMap.containsKey(customerName)) {
            JOptionPane.showMessageDialog(this, "⚠️ Vui lòng chọn khách hàng!");
            return;
        }

        UUID customerId = customerMap.get(customerName);
        List<OrderItem> items = collectOrderItems();
        if (items.isEmpty()) {
            JOptionPane.showMessageDialog(this, "⚠️ Chưa có sản phẩm trong đơn hàng!");
            return;
        }

        try {
            orderService.createOrder(customerId, items);
            JOptionPane.showMessageDialog(this, "✅ Thêm đơn hàng thành công!");
            clearOrderForm();
            loadOrders();
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "❌ Lỗi thêm đơn hàng: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void handleEditOrder() {
        int row = tblDonHang.getSelectedRow();
        if (row == -1) {
            JOptionPane.showMessageDialog(this, "⚠️ Vui lòng chọn đơn hàng cần sửa!");
            return;
        }

        UUID orderId = (UUID) tblDonHang.getValueAt(row, 0);
        String customerName = tblDonHang.getValueAt(row, 1).toString();
        UUID customerId = customerMap.get(customerName);

        List<OrderItem> newItems = collectOrderItems();
        if (newItems.isEmpty()) {
            JOptionPane.showMessageDialog(this, "⚠️ Đơn hàng phải có ít nhất 1 sản phẩm!");
            return;
        }

        int confirm = JOptionPane.showConfirmDialog(this,
                "Cập nhật đơn hàng sẽ xóa đơn cũ và tạo đơn mới. Bạn có chắc chắn?",
                "Xác nhận cập nhật",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.WARNING_MESSAGE);
        
        if (confirm != JOptionPane.YES_OPTION) return;

        try {
            // Xóa đơn hàng cũ
            orderService.deleteOrder(orderId);
            // Tạo đơn hàng mới với các item đã sửa
            orderService.createOrder(customerId, newItems);
            
            JOptionPane.showMessageDialog(this, "✅ Cập nhật đơn hàng thành công!");
            clearOrderForm();
            loadOrders();
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "❌ Lỗi cập nhật đơn hàng: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void handleDeleteOrder() {
        int row = tblDonHang.getSelectedRow();
        if (row == -1) {
            JOptionPane.showMessageDialog(this, "⚠️ Vui lòng chọn đơn hàng cần xóa!");
            return;
        }

        UUID orderId = (UUID) tblDonHang.getValueAt(row, 0);

        int confirm = JOptionPane.showConfirmDialog(this,
                "Bạn có chắc chắn muốn xóa đơn hàng này?\nThao tác này không thể hoàn tác!",
                "Xác nhận xóa",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.WARNING_MESSAGE);
        
        if (confirm != JOptionPane.YES_OPTION) return;

        try {
            orderService.deleteOrder(orderId);
            JOptionPane.showMessageDialog(this, "🗑️ Xóa đơn hàng thành công!");
            clearOrderForm();
            loadOrders();
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "❌ Lỗi khi xóa đơn hàng: " + e.getMessage());
            e.printStackTrace();
        }
    }

    // ======================== DETAIL ============================
    private void showOrderDetails() {
        int row = tblDonHang.getSelectedRow();
        if (row == -1) return;

        UUID orderId = (UUID) tblDonHang.getValueAt(row, 0);
        String customerName = tblDonHang.getValueAt(row, 1).toString();
        
        cboTenKH.setSelectedItem(customerName);
        txtNgayDat.setText("Đang tải...");

        SwingWorker<OrderById, Void> worker = new SwingWorker<>() {
            @Override
            protected OrderById doInBackground() throws Exception {
                return orderService.getOrderById(orderId);
            }

            @Override
            protected void done() {
                try {
                    OrderById order = get();
                    if (order == null) {
                        txtNgayDat.setText("");
                        JOptionPane.showMessageDialog(DonHang.this, "⚠️ Không tìm thấy chi tiết đơn hàng!");
                        return;
                    }

                    DateTimeFormatter fmt = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss")
                            .withZone(ZoneId.systemDefault());
                    txtNgayDat.setText(fmt.format(order.getOrderDate()));

                    DefaultTableModel model = (DefaultTableModel) tblDanhSachSanPham.getModel();
                    model.setRowCount(0);
                    
                    for (OrderItem item : order.getItems()) {
                        BigDecimal total = item.getPrice().multiply(BigDecimal.valueOf(item.getQuantity()));
                        model.addRow(new Object[]{
                            item.getModel(),
                            item.getQuantity(),
                            String.format("%,.0f đ", item.getPrice()),
                            String.format("%,.0f đ", total)
                        });
                    }
                } catch (Exception e) {
                    txtNgayDat.setText("");
                    JOptionPane.showMessageDialog(DonHang.this, "❌ Lỗi hiển thị chi tiết: " + e.getMessage());
                    e.printStackTrace();
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
            String modelName = model.getValueAt(i, 0).toString();
            int qty = Integer.parseInt(model.getValueAt(i, 1).toString());
            
            // Lấy giá từ chuỗi đã format (loại bỏ " đ" và dấu phẩy)
            String priceStr = model.getValueAt(i, 2).toString()
                    .replace(" đ", "")
                    .replace(",", "");
            BigDecimal price = new BigDecimal(priceStr);
            
            // Tìm product_id từ model name
            String productId = findProductIdByModel(modelName);
            
            list.add(new OrderItem(productId, modelName, qty, price));
        }
        return list;
    }

    private String findProductIdByModel(String modelName) {
        for (Product p : productMap.values()) {
            if (p.getModel().equals(modelName)) {
                return p.getProductId();
            }
        }
        return UUID.randomUUID().toString(); // Fallback
    }

    private void addProductToOrder() {
        int row = tblLaptop.getSelectedRow();
        if (row == -1) return;

        DefaultTableModel src = (DefaultTableModel) tblLaptop.getModel();
        DefaultTableModel dest = (DefaultTableModel) tblDanhSachSanPham.getModel();

        String productId = src.getValueAt(row, 0).toString();
        String model = src.getValueAt(row, 1).toString();
        BigDecimal price = (BigDecimal) src.getValueAt(row, 2);

        // Kiểm tra xem sản phẩm đã có trong giỏ chưa
        for (int i = 0; i < dest.getRowCount(); i++) {
            if (dest.getValueAt(i, 0).equals(model)) {
                // Tăng số lượng
                int qty = Integer.parseInt(dest.getValueAt(i, 1).toString()) + 1;
                dest.setValueAt(qty, i, 1);
                
                BigDecimal total = price.multiply(BigDecimal.valueOf(qty));
                dest.setValueAt(String.format("%,.0f đ", total), i, 3);
                return;
            }
        }
        
        // Thêm sản phẩm mới
        dest.addRow(new Object[]{
            model,
            1,
            String.format("%,.0f đ", price),
            String.format("%,.0f đ", price)
        });
    }

    private void clearOrderForm() {
        ((DefaultTableModel) tblDanhSachSanPham.getModel()).setRowCount(0);
        txtNgayDat.setText("");
        if (cboTenKH.getItemCount() > 0) {
            cboTenKH.setSelectedIndex(-1);
        }
    }

    private void removeSelectedProduct() {
        int row = tblDanhSachSanPham.getSelectedRow();
        if (row != -1) {
            ((DefaultTableModel) tblDanhSachSanPham.getModel()).removeRow(row);
        } else {
            JOptionPane.showMessageDialog(this, "⚠️ Vui lòng chọn sản phẩm cần xóa!");
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
        btnRenew = new javax.swing.JButton();

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

        btnRenew.setText("Làm Mới");
        btnRenew.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnRenewActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout jPanel1Layout = new javax.swing.GroupLayout(jPanel1);
        jPanel1.setLayout(jPanel1Layout);
        jPanel1Layout.setHorizontalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(btnEditDH, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(btnDelDH, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addGap(18, 18, 18)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addComponent(btnAddDH, javax.swing.GroupLayout.DEFAULT_SIZE, 307, Short.MAX_VALUE)
                    .addComponent(btnNewDH, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jScrollPane2, javax.swing.GroupLayout.PREFERRED_SIZE, 490, javax.swing.GroupLayout.PREFERRED_SIZE))
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                            .addComponent(jLabel5, javax.swing.GroupLayout.DEFAULT_SIZE, 89, Short.MAX_VALUE)
                            .addComponent(txtNgay, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                        .addGap(18, 18, 18)
                        .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                            .addComponent(txtNgayDat, javax.swing.GroupLayout.DEFAULT_SIZE, 272, Short.MAX_VALUE)
                            .addComponent(cboTenKH, 0, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                        .addGap(18, 18, 18)
                        .addComponent(btnRenew))
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addComponent(jScrollPane3, javax.swing.GroupLayout.PREFERRED_SIZE, 492, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(jPanel1Layout.createSequentialGroup()
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                                .addComponent(btnLamMoi)
                                .addGap(18, 18, 18)
                                .addComponent(btnXoa))
                            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel1Layout.createSequentialGroup()
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                .addComponent(jScrollPane1, javax.swing.GroupLayout.PREFERRED_SIZE, 730, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addGap(0, 0, Short.MAX_VALUE))))))
        );
        jPanel1Layout.setVerticalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(jLabel5)
                            .addComponent(cboTenKH, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(btnRenew))
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
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(btnLamMoi)
                            .addComponent(btnXoa))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addComponent(jScrollPane1, javax.swing.GroupLayout.PREFERRED_SIZE, 387, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addComponent(jScrollPane3, javax.swing.GroupLayout.PREFERRED_SIZE, 433, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        add(jPanel1, java.awt.BorderLayout.PAGE_START);
    }// </editor-fold>//GEN-END:initComponents

    private void btnLamMoiActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnLamMoiActionPerformed
        loadOrders();
    }//GEN-LAST:event_btnLamMoiActionPerformed

    private void btnXoaActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnXoaActionPerformed
        removeSelectedProduct();
    }//GEN-LAST:event_btnXoaActionPerformed

    private void btnRenewActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnRenewActionPerformed
        loadCustomers();
        loadOrders();
        loadProducts();
    }//GEN-LAST:event_btnRenewActionPerformed


    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton btnAddDH;
    private javax.swing.JButton btnDelDH;
    private javax.swing.JButton btnEditDH;
    private javax.swing.JButton btnLamMoi;
    private javax.swing.JButton btnNewDH;
    private javax.swing.JButton btnRenew;
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
