/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/GUIForms/JPanel.java to edit this template
 */
package PANELS;

import DAO.ProductDAO;
import KetNoiCSDL.KetNoICSDL;
import MODELS.Product;
import com.datastax.oss.driver.api.core.CqlSession;
import java.awt.Image;
import java.io.File;
import java.util.UUID;
import java.math.BigDecimal;
import java.time.Instant;
import javax.swing.ImageIcon;
import javax.swing.JFileChooser;
import javax.swing.JOptionPane;
import javax.swing.event.ListSelectionEvent;
import javax.swing.filechooser.FileNameExtensionFilter;
import javax.swing.table.DefaultTableModel;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.List;

/**
 *
 * @author HAO
 */
public class Laptop extends javax.swing.JPanel {

    private DefaultTableModel tblModel;
    private ProductDAO productDAO;
    private String personalImage;

    /**
     * Creates new form Laptop
     */
    public Laptop() {
        initComponents();
        init();
    }

     private void init() {
        tblModel = (DefaultTableModel) tblLaptop.getModel();
        tblModel.setColumnIdentifiers(new String[]{
            "ID", "Hãng", "Model", "CPU", "RAM (GB)", "Bộ nhớ", "Giá", "Còn hàng", "Hình ảnh", "Ngày tạo"
        });

        try {
            CqlSession session = KetNoICSDL.getSession();
            this.productDAO = new ProductDAO(session);
            loadProductsToTable();
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Lỗi kết nối CSDL: " + e.getMessage(), "Lỗi", JOptionPane.ERROR_MESSAGE);
            e.printStackTrace();
        }

        tblLaptop.getSelectionModel().addListSelectionListener((ListSelectionEvent event) -> {
            if (!event.getValueIsAdjusting() && tblLaptop.getSelectedRow() != -1) {
                showDetail();
            }
        });

        btnAddLP.addActionListener(e -> addProduct());
        btnEditLP.addActionListener(e -> updateProduct());
        btnDelLP.addActionListener(e -> deleteProduct());
        btnNewLP.addActionListener(e -> clearForm());
        btnImage.addActionListener(e -> chooseImage());
    }

    private void loadProductsToTable() {
        tblModel.setRowCount(0);
        try {
            List<Product> products = productDAO.findAllProducts();
            for (Product p : products) {
                tblModel.addRow(new Object[]{
                    p.getProductId(),
                    p.getBrand(),
                    p.getModel(),
                    p.getCpu(),
                    p.getRam(),
                    p.getStorage(),
                    p.getPrice(),
                    p.isAvailable(),
                    p.getImage(),
                    p.getCreatedAt()
                });
            }
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Lỗi khi tải dữ liệu: " + e.getMessage(), "Lỗi", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void showDetail() {
        int selectedRow = tblLaptop.getSelectedRow();
        if (selectedRow >= 0) {
            txtHang.setText(tblModel.getValueAt(selectedRow, 1).toString());
            txtModel.setText(tblModel.getValueAt(selectedRow, 2).toString());
            txtCpu.setText(tblModel.getValueAt(selectedRow, 3).toString());
            txtRam.setText(tblModel.getValueAt(selectedRow, 4).toString());
            txtBoNho.setText(tblModel.getValueAt(selectedRow, 5).toString());
            txtGia.setText(tblModel.getValueAt(selectedRow, 6).toString());
            chkConHang.setSelected((boolean) tblModel.getValueAt(selectedRow, 7));

            Object imageValue = tblModel.getValueAt(selectedRow, 8);
            personalImage = (imageValue != null) ? imageValue.toString() : null;
            updateImageLabel(personalImage);
        }
    }

    private void updateImageLabel(String imageName) {
        if (imageName == null || imageName.isEmpty()) {
            lbHinh.setIcon(null);
            lbHinh.setText("Không có hình");
            return;
        }

        ImageIcon icon = null;
        File file = new File("src/main/resources/IMAGES/" + imageName);
        if (file.exists()) {
            icon = new ImageIcon(file.getAbsolutePath());
        }

        if (icon != null) {
            Image img = icon.getImage().getScaledInstance(lbHinh.getWidth(), lbHinh.getHeight(), Image.SCALE_SMOOTH);
            lbHinh.setIcon(new ImageIcon(img));
            lbHinh.setText(null);
        } else {
            lbHinh.setIcon(null);
            lbHinh.setText("Ảnh không tồn tại");
        }
    }

    private Product getProductFromForm() {
        try {
            String brand = txtHang.getText().trim();
            String model = txtModel.getText().trim();
            String cpu = txtCpu.getText().trim();
            int ram = Integer.parseInt(txtRam.getText().trim());
            String storage = txtBoNho.getText().trim();
            BigDecimal price = new BigDecimal(txtGia.getText().trim());
            boolean available = chkConHang.isSelected();

            if (brand.isEmpty() || model.isEmpty() || cpu.isEmpty() || storage.isEmpty()) {
                JOptionPane.showMessageDialog(this, "Vui lòng nhập đầy đủ thông tin.", "Cảnh báo", JOptionPane.WARNING_MESSAGE);
                return null;
            }

            Product p = new Product();
            p.setBrand(brand);
            p.setModel(model);
            p.setCpu(cpu);
            p.setRam(ram);
            p.setStorage(storage);
            p.setPrice(price);
            p.setAvailable(available);
            p.setImage(personalImage);
            return p;
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Lỗi định dạng: " + e.getMessage(), "Lỗi", JOptionPane.ERROR_MESSAGE);
            return null;
        }
    }

    private void addProduct() {
        Product product = getProductFromForm();
        if (product == null) return;

        product.setProductId(UUID.randomUUID().toString());
        product.setCreatedAt(Instant.now());

        try {
            productDAO.insertProduct(product);
            JOptionPane.showMessageDialog(this, "Thêm sản phẩm thành công!");
            loadProductsToTable();
            clearForm();
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Lỗi khi thêm sản phẩm: " + e.getMessage(), "Lỗi", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void updateProduct() {
        int selectedRow = tblLaptop.getSelectedRow();
        if (selectedRow < 0) {
            JOptionPane.showMessageDialog(this, "Chọn sản phẩm để cập nhật.");
            return;
        }

        Product product = getProductFromForm();
        if (product == null) return;

        String productId = tblModel.getValueAt(selectedRow, 0).toString();
        Instant createdAt = (Instant) tblModel.getValueAt(selectedRow, 9);
        product.setProductId(productId);
        product.setCreatedAt(createdAt);

        try {
            productDAO.updateProduct(product);
            JOptionPane.showMessageDialog(this, "Cập nhật thành công!");
            loadProductsToTable();
            clearForm();
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Lỗi khi cập nhật: " + e.getMessage(), "Lỗi", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void deleteProduct() {
        int selectedRow = tblLaptop.getSelectedRow();
        if (selectedRow < 0) {
            JOptionPane.showMessageDialog(this, "Chọn sản phẩm để xóa.");
            return;
        }

        String productId = tblModel.getValueAt(selectedRow, 0).toString();
        int confirm = JOptionPane.showConfirmDialog(this, "Xóa sản phẩm này?", "Xác nhận", JOptionPane.YES_NO_OPTION);
        if (confirm != JOptionPane.YES_OPTION) return;

        try {
            productDAO.deleteProductById(productId);
            JOptionPane.showMessageDialog(this, "Đã xóa sản phẩm!");
            loadProductsToTable();
            clearForm();
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Lỗi khi xóa: " + e.getMessage(), "Lỗi", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void clearForm() {
        txtHang.setText("");
        txtModel.setText("");
        txtCpu.setText("");
        txtRam.setText("");
        txtBoNho.setText("");
        txtGia.setText("");
        chkConHang.setSelected(false);
        lbHinh.setIcon(null);
        lbHinh.setText("Không có hình");
        personalImage = null;
        tblLaptop.clearSelection();
    }

    private void chooseImage() {
        JFileChooser fc = new JFileChooser();
        fc.setFileFilter(new FileNameExtensionFilter("Image Files", "jpg", "png", "jpeg", "gif"));
        if (fc.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
            File src = fc.getSelectedFile();
            File destDir = new File("src/main/resources/IMAGES");
            if (!destDir.exists()) destDir.mkdirs();
            Path destPath = destDir.toPath().resolve(src.getName());
            try {
                Files.copy(src.toPath(), destPath, StandardCopyOption.REPLACE_EXISTING);
                personalImage = src.getName();
                updateImageLabel(personalImage);
            } catch (Exception e) {
                JOptionPane.showMessageDialog(this, "Lỗi khi sao chép ảnh: " + e.getMessage());
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
        txtModel = new javax.swing.JTextField();
        jLabel2 = new javax.swing.JLabel();
        txtHang = new javax.swing.JTextField();
        jLabel3 = new javax.swing.JLabel();
        txtCpu = new javax.swing.JTextField();
        jLabel4 = new javax.swing.JLabel();
        txtRam = new javax.swing.JTextField();
        jLabel5 = new javax.swing.JLabel();
        txtGia = new javax.swing.JTextField();
        jLabel6 = new javax.swing.JLabel();
        txtBoNho = new javax.swing.JTextField();
        jLabel7 = new javax.swing.JLabel();
        chkConHang = new javax.swing.JCheckBox();
        btnAddLP = new javax.swing.JButton();
        btnEditLP = new javax.swing.JButton();
        btnDelLP = new javax.swing.JButton();
        btnNewLP = new javax.swing.JButton();
        btnImage = new javax.swing.JButton();
        lbHinh = new javax.swing.JLabel();
        jScrollPane1 = new javax.swing.JScrollPane();
        tblLaptop = new javax.swing.JTable();

        setLayout(new java.awt.BorderLayout());

        jSplitPane1.setDividerLocation(280);
        jSplitPane1.setOrientation(javax.swing.JSplitPane.VERTICAL_SPLIT);

        jLabel1.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
        jLabel1.setText("Model");

        txtModel.setPreferredSize(new java.awt.Dimension(100, 22));

        jLabel2.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
        jLabel2.setText("Hãng");
        jLabel2.setPreferredSize(new java.awt.Dimension(20, 22));

        jLabel3.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
        jLabel3.setText("CPU");
        jLabel3.setPreferredSize(new java.awt.Dimension(51, 16));
        jLabel3.setRequestFocusEnabled(false);

        jLabel4.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
        jLabel4.setText("RAM");

        jLabel5.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
        jLabel5.setText("Giá");

        txtGia.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                txtGiaActionPerformed(evt);
            }
        });

        jLabel6.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
        jLabel6.setText("Bộ nhớ");

        jLabel7.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
        jLabel7.setText("Còn hàng ?");

        chkConHang.setText("Có");
        chkConHang.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                chkConHangActionPerformed(evt);
            }
        });

        btnAddLP.setIcon(new javax.swing.ImageIcon(getClass().getResource("/ICONS/Add.png"))); // NOI18N
        btnAddLP.setText("Thêm Laptop");
        btnAddLP.setPreferredSize(new java.awt.Dimension(50, 50));

        btnEditLP.setIcon(new javax.swing.ImageIcon(getClass().getResource("/ICONS/Repair.png"))); // NOI18N
        btnEditLP.setText("Sửa thông tin");
        btnEditLP.setMaximumSize(new java.awt.Dimension(114, 57));
        btnEditLP.setMinimumSize(new java.awt.Dimension(103, 31));
        btnEditLP.setPreferredSize(new java.awt.Dimension(50, 50));

        btnDelLP.setIcon(new javax.swing.ImageIcon(getClass().getResource("/ICONS/Delete.png"))); // NOI18N
        btnDelLP.setText("Xoá Laptop");
        btnDelLP.setMinimumSize(new java.awt.Dimension(100, 55));
        btnDelLP.setPreferredSize(new java.awt.Dimension(50, 50));

        btnNewLP.setIcon(new javax.swing.ImageIcon(getClass().getResource("/ICONS/bookicon.png"))); // NOI18N
        btnNewLP.setText("Tạo mới Laptop");
        btnNewLP.setPreferredSize(new java.awt.Dimension(50, 50));

        btnImage.setText("Chọn hình");

        javax.swing.GroupLayout jPanel1Layout = new javax.swing.GroupLayout(jPanel1);
        jPanel1.setLayout(jPanel1Layout);
        jPanel1Layout.setHorizontalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel1Layout.createSequentialGroup()
                .addGap(12, 12, 12)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.CENTER)
                            .addComponent(jLabel2, javax.swing.GroupLayout.PREFERRED_SIZE, 56, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(jLabel5)
                            .addComponent(jLabel1, javax.swing.GroupLayout.PREFERRED_SIZE, 57, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(jLabel7))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(jPanel1Layout.createSequentialGroup()
                                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                    .addComponent(txtHang, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                    .addComponent(chkConHang, javax.swing.GroupLayout.PREFERRED_SIZE, 187, javax.swing.GroupLayout.PREFERRED_SIZE)
                                    .addComponent(txtModel, javax.swing.GroupLayout.PREFERRED_SIZE, 160, javax.swing.GroupLayout.PREFERRED_SIZE))
                                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                                    .addComponent(jLabel4)
                                    .addComponent(jLabel3, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                    .addGroup(javax.swing.GroupLayout.Alignment.LEADING, jPanel1Layout.createSequentialGroup()
                                        .addGap(25, 25, 25)
                                        .addComponent(jLabel6))))
                            .addComponent(txtGia, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING, false)
                            .addComponent(txtRam, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, 122, Short.MAX_VALUE)
                            .addComponent(txtCpu, javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(txtBoNho)))
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addComponent(btnNewLP, javax.swing.GroupLayout.PREFERRED_SIZE, 205, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(btnAddLP, javax.swing.GroupLayout.PREFERRED_SIZE, 205, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addComponent(btnDelLP, javax.swing.GroupLayout.PREFERRED_SIZE, 161, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(btnEditLP, javax.swing.GroupLayout.PREFERRED_SIZE, 182, javax.swing.GroupLayout.PREFERRED_SIZE)))
                .addGap(12, 12, 12)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addGap(12, 12, 12)
                        .addComponent(lbHinh, javax.swing.GroupLayout.PREFERRED_SIZE, 251, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel1Layout.createSequentialGroup()
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(btnImage)
                        .addGap(96, 96, 96))))
        );

        jPanel1Layout.linkSize(javax.swing.SwingConstants.HORIZONTAL, new java.awt.Component[] {chkConHang, txtGia, txtHang, txtModel});

        jPanel1Layout.linkSize(javax.swing.SwingConstants.HORIZONTAL, new java.awt.Component[] {jLabel2, jLabel5, jLabel7});

        jPanel1Layout.setVerticalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addGap(28, 28, 28)
                        .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(txtModel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(jLabel1)
                            .addComponent(jLabel3, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(txtCpu, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)))
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel1Layout.createSequentialGroup()
                        .addContainerGap()
                        .addComponent(btnImage)))
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(jPanel1Layout.createSequentialGroup()
                                .addGap(12, 12, 12)
                                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                                    .addComponent(jLabel2, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                    .addComponent(txtHang, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                    .addComponent(jLabel4)))
                            .addGroup(jPanel1Layout.createSequentialGroup()
                                .addGap(9, 9, 9)
                                .addComponent(txtRam, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)))
                        .addGap(12, 12, 12)
                        .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(jLabel5)
                            .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                                .addComponent(jLabel6)
                                .addComponent(txtGia, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                            .addComponent(txtBoNho, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addGap(12, 12, 12)
                        .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(jLabel7)
                            .addComponent(chkConHang))
                        .addGap(23, 23, 23)
                        .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(btnNewLP, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(btnAddLP, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(btnDelLP, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(btnEditLP, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addGap(9, 9, 9)
                        .addComponent(lbHinh, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addContainerGap())))
        );

        jSplitPane1.setTopComponent(jPanel1);

        tblLaptop.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {
                {null, null, null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null, null, null}
            },
            new String [] {
                "ID Laptop", "Hãng", "Model", "CPU", "RAM", "Bộ nhớ", "Giá", "Created", "TrạngThái", "Hình"
            }
        ));
        jScrollPane1.setViewportView(tblLaptop);

        jSplitPane1.setRightComponent(jScrollPane1);

        add(jSplitPane1, java.awt.BorderLayout.CENTER);
    }// </editor-fold>//GEN-END:initComponents

    private void chkConHangActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_chkConHangActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_chkConHangActionPerformed

    private void txtGiaActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_txtGiaActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_txtGiaActionPerformed


    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton btnAddLP;
    private javax.swing.JButton btnDelLP;
    private javax.swing.JButton btnEditLP;
    private javax.swing.JButton btnImage;
    private javax.swing.JButton btnNewLP;
    private javax.swing.JCheckBox chkConHang;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JLabel jLabel4;
    private javax.swing.JLabel jLabel5;
    private javax.swing.JLabel jLabel6;
    private javax.swing.JLabel jLabel7;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JSplitPane jSplitPane1;
    private javax.swing.JLabel lbHinh;
    private javax.swing.JTable tblLaptop;
    private javax.swing.JTextField txtBoNho;
    private javax.swing.JTextField txtCpu;
    private javax.swing.JTextField txtGia;
    private javax.swing.JTextField txtHang;
    private javax.swing.JTextField txtModel;
    private javax.swing.JTextField txtRam;
    // End of variables declaration//GEN-END:variables
}
