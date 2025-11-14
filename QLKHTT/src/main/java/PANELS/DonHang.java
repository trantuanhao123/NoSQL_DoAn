/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/GUIForms/JPanel.java to edit this template
 */
package PANELS;

import DAO.CustomerDAO;
import DAO.OrderByCustomerDAO;
import DAO.ProductDAO;
import SERVICE.OrderService;
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

    private final Map<String, String> customerMap = new HashMap<>();
    private final Map<String, Product> productMap = new HashMap<>();
    private final CqlSession session;
    private final CustomerDAO customerDAO;
    private final ProductDAO productDAO;
    private final OrderService orderService;
    private final OrderByCustomerDAO orderByCustomerDAO; 
    
    private String selectedOrderId;

    public DonHang() {
        initComponents();
        this.session = KetNoICSDL.getSession();
        
        this.customerDAO = new CustomerDAO(session); 
        this.productDAO = new ProductDAO(session);
        this.orderService = new OrderService(session);
        
        // THÊM: Khởi tạo DAO
        this.orderByCustomerDAO = new OrderByCustomerDAO(session);
        
        setupTables();
        
        // Gán sự kiện
        btnAddDH.addActionListener(e -> handleAddOrder());
        btnEditDH.addActionListener(e -> handleEditOrder());
        btnDelDH.addActionListener(e -> handleDeleteOrder());
        btnNewDH.addActionListener(e -> clearOrderForm());
        
        btnRenew.addActionListener(e -> {
            loadCustomers();
            loadProducts();
            loadOrders(); 
        });
        
        btnLamMoi.addActionListener(e -> {
            loadOrders(); 
        }); 
        
        btnXoa.addActionListener(e -> removeSelectedProduct());

        tblDonHang.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting() && tblDonHang.getSelectedRow() != -1) {
                showOrderDetails();
            }
        });

        tblLaptop.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                addProductToOrder();
            }
        });
        
        // Tải dữ liệu ban đầu
        loadCustomers();
        loadProducts();
        loadOrders(); 
        
        txtNgayDat.setEnabled(false);
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
        tblSanPhamDaChon.setViewportView(tblDanhSachSanPham); // Gán JTable vào JScrollPane


        tblLaptop.setModel(new DefaultTableModel(
                new Object[][]{},
                new String[]{"Mã SP", "Model", "Giá"}
        ) {
            @Override
            public boolean isCellEditable(int row, int col) {
                return false;
            }
        });

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
            JOptionPane.showMessageDialog(this, "Loi tai khach hang: " + e.getMessage());
        }
    }

    private void loadProducts() {
        try {
            productMap.clear();
            DefaultTableModel model = (DefaultTableModel) tblLaptop.getModel();
            model.setRowCount(0);
            List<Product> products = productDAO.findAllProducts();
            for (Product p : products) {
                if (p.isAvailable()) { 
                    model.addRow(new Object[]{
                        p.getProductId(),
                        p.getModel(),
                        p.getPrice()
                    });
                    productMap.put(p.getProductId(), p);
                }
            }
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Loi tai san pham: " + e.getMessage());
        }
    }

    /**
     * SỬA LỖI: Thêm clearOrderForm() và gọi orderByCustomerDAO.findAll()
     */
    private void loadOrders() {
        // Sửa lỗi render: Xóa chi tiết (bảng sản phẩm, v.v.)
        // trước khi tải lại bảng đơn hàng.
        clearOrderForm(); 
        
        DefaultTableModel model = (DefaultTableModel) tblDonHang.getModel();
        model.setRowCount(0);

        SwingWorker<Void, Object[]> worker = new SwingWorker<>() {
            @Override
            protected Void doInBackground() {
                try {
                    DateTimeFormatter fmt = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm")
                            .withZone(ZoneId.systemDefault());
                            
                    // SỬA LỖI NULL: Gọi trực tiếp DAO.findAll()
                    // Lỗi "Unsupported null value" là do getOrdersByCustomer(null) gây ra.
                    List<OrderByCustomer> allOrders = orderByCustomerDAO.findAll();
                    
                    // Tạo map tạm để tra cứu tên khách hàng
                    Map<String, String> idToNameMap = new HashMap<>();
                    for (Map.Entry<String, String> entry : customerMap.entrySet()) {
                        idToNameMap.put(entry.getValue(), entry.getKey());
                    }

                    for (OrderByCustomer o : allOrders) {
                        String customerName = idToNameMap.getOrDefault(o.getCustomerId(), "(Khách hàng không rõ)");
                        publish(new Object[]{
                            o.getOrderId(),
                            customerName,
                            fmt.format(o.getOrderDate()),
                            String.format("%,.0f d", o.getTotal()),
                            o.getStatus()
                        });
                    }
                } catch (Exception ex) {
                    SwingUtilities.invokeLater(() -> 
                        JOptionPane.showMessageDialog(DonHang.this, 
                            "Loi tai don hang: " + ex.getMessage())
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
        };
        worker.execute();
    }

    // ======================== CRUD ============================
    private void handleAddOrder() {
        String orderId = txtMaDonHang.getText().trim();
        if (orderId.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Vui long nhap Ma Don Hang!");
            return;
        }

        String customerName = (String) cboTenKH.getSelectedItem();
        if (customerName == null || !customerMap.containsKey(customerName)) {
            JOptionPane.showMessageDialog(this, "Vui long chon khach hang!");
            return;
        }

        String customerId = customerMap.get(customerName);
        List<OrderItem> items = collectOrderItems();
        if (items.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Chua co san pham trong don hang!");
            return;
        }
        
        String status = cboStatus.getSelectedItem().toString();
        
        try {
            if (orderService.getOrderById(orderId) != null) {
                JOptionPane.showMessageDialog(this, "Ma Don Hang nay da ton tai!");
                return;
            }
            
            // Yêu cầu OrderService đã được sửa để nhận (orderId, customerId, items, status)
            orderService.createOrder(orderId, customerId, items, status);
            JOptionPane.showMessageDialog(this, "Them don hang thanh cong!");
            clearOrderForm();
            loadOrders();
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Loi them don hang: " + e.getMessage());
        }
    }

    /**
     * SỬA: Logic nút Sửa (Edit)
     * Dùng logic "Xóa và Tạo lại" để cập nhật CẢ SẢN PHẨM VÀ TRẠNG THÁI.
     */
    private void handleEditOrder() {
        if (selectedOrderId == null) {
            JOptionPane.showMessageDialog(this, "Vui long chon don hang can sua!");
            return;
        }
        
        // 1. Lấy Customer ID (An toàn nhất là từ Combobox đang chọn)
        String customerName = (String) cboTenKH.getSelectedItem();
        if (customerName == null || !customerMap.containsKey(customerName)) {
             JOptionPane.showMessageDialog(this, "Khach hang duoc chon khong hop le!");
             return;
        }
        String customerId = customerMap.get(customerName);

        // 2. Lấy danh sách sản phẩm MỚI từ bảng
        List<OrderItem> newItems = collectOrderItems();
        if (newItems.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Don hang phai co it nhat 1 san pham!");
            return;
        }
        
        // 3. Lấy trạng thái MỚI
        String newStatus = cboStatus.getSelectedItem().toString();

        int confirm = JOptionPane.showConfirmDialog(this,
                "Ban co chac chan muon CAP NHAT (xoa va tao lai) don hang " + selectedOrderId + "?\n"
                + "Danh sach san pham va trang thai se duoc luu lai.",
                "Xac nhan cap nhat",
                JOptionPane.YES_NO_OPTION);
        
        if (confirm != JOptionPane.YES_OPTION) return;

        try {
            // 4. Xóa đơn hàng cũ (Service sẽ lo rollback loyalty nếu cần)
            orderService.deleteOrder(selectedOrderId);
            
            // 5. Tạo lại đơn hàng với ID CŨ, nhưng sản phẩm MỚI và status MỚI
            // (Service sẽ lo update loyalty nếu status là COMPLETED)
            orderService.createOrder(selectedOrderId, customerId, newItems, newStatus);
            
            JOptionPane.showMessageDialog(this, "Cap nhat don hang thanh cong!");
            clearOrderForm();
            loadOrders();
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Loi cap nhat don hang: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void handleDeleteOrder() {
        if (selectedOrderId == null) {
            JOptionPane.showMessageDialog(this, "Vui long chon don hang can xoa!");
            return;
        }

        int confirm = JOptionPane.showConfirmDialog(this,
                "Ban co chac chan muon xoa don hang nay?\nThao tac nay khong a hoàn tac!",
                "Xac nhan xoa",
                JOptionPane.YES_NO_OPTION);
        
        if (confirm != JOptionPane.YES_OPTION) return;

        try {
            orderService.deleteOrder(selectedOrderId);
            JOptionPane.showMessageDialog(this, "Xoa don hang thanh cong!");
            clearOrderForm();
            loadOrders();
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Loi khi xoa don hang: " + e.getMessage());
        }
    }

    // ======================== DETAIL ============================
    private void showOrderDetails() {
        int row = tblDonHang.getSelectedRow();
        if (row == -1) return;

        selectedOrderId = tblDonHang.getValueAt(row, 0).toString();
        String customerName = tblDonHang.getValueAt(row, 1).toString();
        
        txtMaDonHang.setText(selectedOrderId);
        txtMaDonHang.setEnabled(false); 
        
        cboTenKH.setSelectedItem(customerName);
        txtNgayDat.setText("Dang tai...");

        SwingWorker<OrderById, Void> worker = new SwingWorker<>() {
            @Override
            protected OrderById doInBackground() throws Exception {
                return orderService.getOrderById(selectedOrderId); 
            }

            @Override
            protected void done() {
                try {
                    OrderById order = get();
                    if (order == null) {
                        txtNgayDat.setText("");
                        JOptionPane.showMessageDialog(DonHang.this, "Khong tim thay chi tiet don hang!");
                        return;
                    }

                    DateTimeFormatter fmt = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss")
                            .withZone(ZoneId.systemDefault());
                    txtNgayDat.setText(fmt.format(order.getOrderDate()));
                    
                    cboStatus.setSelectedItem(order.getStatus());

                    DefaultTableModel model = (DefaultTableModel) tblDanhSachSanPham.getModel();
                    model.setRowCount(0);
                    
                    for (OrderItem item : order.getItems()) {
                        BigDecimal total = item.getPrice().multiply(BigDecimal.valueOf(item.getQuantity()));
                        model.addRow(new Object[]{
                            item.getModel(),
                            item.getQuantity(),
                            String.format("%,.0f d", item.getPrice()),
                            String.format("%,.0f d", total)
                        });
                    }
                } catch (Exception e) {
                    txtNgayDat.setText("");
                    JOptionPane.showMessageDialog(DonHang.this, "Loi hien thi chi tiet: " + e.getMessage());
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
            
            String priceStr = model.getValueAt(i, 2).toString()
                    .replace(" d", "")
                    .replace(",", "");
            BigDecimal price = new BigDecimal(priceStr);
            
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
        return "UNKNOWN_ID"; // Fallback
    }

    private void addProductToOrder() {
        int row = tblLaptop.getSelectedRow();
        if (row == -1) return;

        DefaultTableModel src = (DefaultTableModel) tblLaptop.getModel();
        DefaultTableModel dest = (DefaultTableModel) tblDanhSachSanPham.getModel();

        String productId = src.getValueAt(row, 0).toString();
        String model = src.getValueAt(row, 1).toString();
        BigDecimal price = (BigDecimal) src.getValueAt(row, 2);

        for (int i = 0; i < dest.getRowCount(); i++) {
            if (dest.getValueAt(i, 0).equals(model)) {
                int qty = Integer.parseInt(dest.getValueAt(i, 1).toString()) + 1;
                dest.setValueAt(qty, i, 1);
                
                BigDecimal total = price.multiply(BigDecimal.valueOf(qty));
                dest.setValueAt(String.format("%,.0f d", total), i, 3);
                return;
            }
        }
        
        dest.addRow(new Object[]{
            model,
            1,
            String.format("%,.0f d", price),
            String.format("%,.0f d", price)
        });
    }

    private void clearOrderForm() {
        ((DefaultTableModel) tblDanhSachSanPham.getModel()).setRowCount(0);
        
        txtMaDonHang.setText("");
        txtMaDonHang.setEnabled(true);
        selectedOrderId = null;
        
        txtNgayDat.setText("");
        if (cboTenKH.getItemCount() > 0) {
            cboTenKH.setSelectedIndex(-1);
        }
        cboStatus.setSelectedIndex(0); 
        tblDonHang.clearSelection();
        txtMaDonHang.requestFocus(); 
    }

    private void removeSelectedProduct() {
        int row = tblDanhSachSanPham.getSelectedRow();
        if (row != -1) {
            ((DefaultTableModel) tblDanhSachSanPham.getModel()).removeRow(row);
        } else {
            JOptionPane.showMessageDialog(this, "Vui long chon san pham can xoa!");
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
        jScrollPane3 = new javax.swing.JScrollPane();
        tblDonHang = new javax.swing.JTable();
        tblSanPhamDaChon = new javax.swing.JScrollPane();
        tblDanhSachSanPham = new javax.swing.JTable();
        pnMain = new javax.swing.JPanel();
        btnLamMoi = new javax.swing.JButton();
        btnXoa = new javax.swing.JButton();
        txtNgay = new javax.swing.JLabel();
        txtMaDonHang = new javax.swing.JTextField();
        btnRenew = new javax.swing.JButton();
        jLabel5 = new javax.swing.JLabel();
        cboTenKH = new javax.swing.JComboBox<>();
        btnEditDH = new javax.swing.JButton();
        btnAddDH = new javax.swing.JButton();
        btnDelDH = new javax.swing.JButton();
        btnNewDH = new javax.swing.JButton();
        jScrollPane2 = new javax.swing.JScrollPane();
        tblLaptop = new javax.swing.JTable();
        cboStatus = new javax.swing.JComboBox<>();
        jLabel6 = new javax.swing.JLabel();
        jLabel7 = new javax.swing.JLabel();
        txtNgayDat = new javax.swing.JTextField();

        setLayout(new java.awt.BorderLayout());

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
        tblSanPhamDaChon.setViewportView(tblDanhSachSanPham);

        btnLamMoi.setText("Làm Mới Đơn Hàng");
        btnLamMoi.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnLamMoiActionPerformed(evt);
            }
        });

        btnXoa.setText("Xóa Sản Phẩm Đã Chọn");
        btnXoa.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnXoaActionPerformed(evt);
            }
        });

        txtNgay.setText("Ngày Đặt");

        btnRenew.setText("Làm Mới Tất Cả");
        btnRenew.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnRenewActionPerformed(evt);
            }
        });

        jLabel5.setText("Mã Khách Hàng");

        cboTenKH.setModel(new javax.swing.DefaultComboBoxModel<>(new String[] { "Item 1", "Item 2", "Item 3", "Item 4" }));

        btnEditDH.setIcon(new javax.swing.ImageIcon(getClass().getResource("/ICONS/Repair.png"))); // NOI18N
        btnEditDH.setText("Sửa thông tin");
        btnEditDH.setMaximumSize(new java.awt.Dimension(114, 57));
        btnEditDH.setMinimumSize(new java.awt.Dimension(103, 31));
        btnEditDH.setPreferredSize(new java.awt.Dimension(50, 50));

        btnAddDH.setIcon(new javax.swing.ImageIcon(getClass().getResource("/ICONS/Add.png"))); // NOI18N
        btnAddDH.setText("Thêm đơn hàng");
        btnAddDH.setPreferredSize(new java.awt.Dimension(50, 50));

        btnDelDH.setIcon(new javax.swing.ImageIcon(getClass().getResource("/ICONS/Delete.png"))); // NOI18N
        btnDelDH.setText("Xoá đơn hàng");
        btnDelDH.setMinimumSize(new java.awt.Dimension(100, 55));
        btnDelDH.setPreferredSize(new java.awt.Dimension(50, 50));
        btnDelDH.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnDelDHActionPerformed(evt);
            }
        });

        btnNewDH.setIcon(new javax.swing.ImageIcon(getClass().getResource("/ICONS/bookicon.png"))); // NOI18N
        btnNewDH.setText("Tạo mới đơn hàng");
        btnNewDH.setPreferredSize(new java.awt.Dimension(50, 50));

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

        cboStatus.setModel(new javax.swing.DefaultComboBoxModel<>(new String[] { "Hoàn Thành", "Chờ Xử Lý", "Hủy" }));

        jLabel6.setText("Trạng Thái");

        jLabel7.setText("Mã ĐH");

        javax.swing.GroupLayout pnMainLayout = new javax.swing.GroupLayout(pnMain);
        pnMain.setLayout(pnMainLayout);
        pnMainLayout.setHorizontalGroup(
            pnMainLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(pnMainLayout.createSequentialGroup()
                .addGroup(pnMainLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(pnMainLayout.createSequentialGroup()
                        .addGroup(pnMainLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                            .addComponent(jLabel5, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addComponent(txtNgay, javax.swing.GroupLayout.PREFERRED_SIZE, 89, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(pnMainLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                            .addComponent(cboTenKH, 0, 156, Short.MAX_VALUE)
                            .addComponent(txtNgayDat))
                        .addGap(10, 10, 10)
                        .addGroup(pnMainLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                            .addComponent(jLabel6, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addComponent(jLabel7, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                        .addGap(18, 18, 18)
                        .addGroup(pnMainLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                            .addComponent(txtMaDonHang, javax.swing.GroupLayout.DEFAULT_SIZE, 156, Short.MAX_VALUE)
                            .addComponent(cboStatus, 0, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)))
                    .addGroup(pnMainLayout.createSequentialGroup()
                        .addGroup(pnMainLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                            .addComponent(btnAddDH, javax.swing.GroupLayout.DEFAULT_SIZE, 189, Short.MAX_VALUE)
                            .addComponent(btnEditDH, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(pnMainLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                            .addComponent(btnDelDH, javax.swing.GroupLayout.DEFAULT_SIZE, 188, Short.MAX_VALUE)
                            .addComponent(btnNewDH, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(pnMainLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                            .addComponent(btnLamMoi, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addComponent(btnRenew, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addComponent(btnXoa, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(jScrollPane2))
        );
        pnMainLayout.setVerticalGroup(
            pnMainLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(pnMainLayout.createSequentialGroup()
                .addGroup(pnMainLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel5)
                    .addComponent(cboTenKH, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(cboStatus, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel6))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 12, Short.MAX_VALUE)
                .addGroup(pnMainLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(txtMaDonHang, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(txtNgay)
                    .addComponent(jLabel7)
                    .addComponent(txtNgayDat, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGroup(pnMainLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, pnMainLayout.createSequentialGroup()
                        .addComponent(btnLamMoi)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addComponent(btnXoa))
                    .addGroup(pnMainLayout.createSequentialGroup()
                        .addGap(10, 10, 10)
                        .addGroup(pnMainLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(btnEditDH, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(btnNewDH, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(btnRenew))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(pnMainLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(btnAddDH, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(btnDelDH, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))))
                .addGap(6, 6, 6))
            .addComponent(jScrollPane2, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.PREFERRED_SIZE, 0, Short.MAX_VALUE)
        );

        javax.swing.GroupLayout jPanel1Layout = new javax.swing.GroupLayout(jPanel1);
        jPanel1.setLayout(jPanel1Layout);
        jPanel1Layout.setHorizontalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addComponent(pnMain, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addComponent(jScrollPane3, javax.swing.GroupLayout.PREFERRED_SIZE, 547, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addComponent(tblSanPhamDaChon, javax.swing.GroupLayout.PREFERRED_SIZE, 586, javax.swing.GroupLayout.PREFERRED_SIZE)))
                .addGap(0, 0, Short.MAX_VALUE))
        );
        jPanel1Layout.setVerticalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addComponent(pnMain, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addComponent(tblSanPhamDaChon, javax.swing.GroupLayout.PREFERRED_SIZE, 433, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addContainerGap())
                    .addComponent(jScrollPane3)))
        );

        add(jPanel1, java.awt.BorderLayout.PAGE_START);
    }// </editor-fold>//GEN-END:initComponents

    private void btnLamMoiActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnLamMoiActionPerformed
  
    }//GEN-LAST:event_btnLamMoiActionPerformed

    private void btnXoaActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnXoaActionPerformed

    }//GEN-LAST:event_btnXoaActionPerformed

    private void btnRenewActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnRenewActionPerformed

    }//GEN-LAST:event_btnRenewActionPerformed

    private void btnDelDHActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnDelDHActionPerformed

    }//GEN-LAST:event_btnDelDHActionPerformed


    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton btnAddDH;
    private javax.swing.JButton btnDelDH;
    private javax.swing.JButton btnEditDH;
    private javax.swing.JButton btnLamMoi;
    private javax.swing.JButton btnNewDH;
    private javax.swing.JButton btnRenew;
    private javax.swing.JButton btnXoa;
    private javax.swing.JComboBox<String> cboStatus;
    private javax.swing.JComboBox<String> cboTenKH;
    private javax.swing.JLabel jLabel5;
    private javax.swing.JLabel jLabel6;
    private javax.swing.JLabel jLabel7;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JScrollPane jScrollPane2;
    private javax.swing.JScrollPane jScrollPane3;
    private javax.swing.JPanel pnMain;
    private javax.swing.JTable tblDanhSachSanPham;
    private javax.swing.JTable tblDonHang;
    private javax.swing.JTable tblLaptop;
    private javax.swing.JScrollPane tblSanPhamDaChon;
    private javax.swing.JTextField txtMaDonHang;
    private javax.swing.JLabel txtNgay;
    private javax.swing.JTextField txtNgayDat;
    // End of variables declaration//GEN-END:variables
}
