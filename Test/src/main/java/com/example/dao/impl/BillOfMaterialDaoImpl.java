    package com.example.dao.impl;

    import com.example.dao.BillOfMaterialDao;
    import com.example.dao.util.DBUtil;
    import com.example.model.BillOfMaterial;
    import java.sql.Connection;
    import java.sql.PreparedStatement;
    import java.sql.ResultSet;
    import java.sql.SQLException;
    import java.sql.Statement;
    import java.sql.Timestamp;
    import java.util.ArrayList;
    import java.util.List;
    import java.util.logging.Level;
    import java.util.logging.Logger;

    /**
     * BillOfMaterialDao 介面的 JDBC 實現類。
     * 負責 BillOfMaterial 實體與資料庫之間的數據交互。
     */
    public class BillOfMaterialDaoImpl implements BillOfMaterialDao {

        private static final Logger LOGGER = Logger.getLogger(BillOfMaterialDaoImpl.class.getName());

        @Override
        public int addBillOfMaterial(BillOfMaterial bom) throws SQLException {
            String sql = "INSERT INTO bill_of_materials (product_id, material_id, quantity, create_date, update_date) VALUES (?, ?, ?, ?, ?)";
            int newBomId = -1;
            try (Connection conn = DBUtil.getConnection();
                 PreparedStatement pstmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

                pstmt.setInt(1, bom.getProductId());
                pstmt.setInt(2, bom.getMaterialId());
                pstmt.setBigDecimal(3, bom.getQuantity());
                pstmt.setTimestamp(4, new Timestamp(System.currentTimeMillis())); // 設置創建時間
                pstmt.setTimestamp(5, new Timestamp(System.currentTimeMillis())); // 設置更新時間

                int affectedRows = pstmt.executeUpdate();

                if (affectedRows == 0) {
                    LOGGER.log(Level.WARNING, "新增 BOM 項目失敗，沒有行受影響。產品ID: {0}, 物料ID: {1}", new Object[]{bom.getProductId(), bom.getMaterialId()});
                    throw new SQLException("新增 BOM 項目失敗，沒有行受影響。");
                }

                try (ResultSet generatedKeys = pstmt.getGeneratedKeys()) {
                    if (generatedKeys.next()) {
                        newBomId = generatedKeys.getInt(1);
                        bom.setBomId(newBomId); // 將生成的ID設置回 BOM 物件
                        LOGGER.log(Level.INFO, "成功新增 BOM 項目，ID: {0}", newBomId);
                    } else {
                        LOGGER.log(Level.WARNING, "新增 BOM 項目失敗，無法獲取生成的ID。產品ID: {0}, 物料ID: {1}", new Object[]{bom.getProductId(), bom.getMaterialId()});
                        throw new SQLException("新增 BOM 項目失敗，無法獲取生成的ID。");
                    }
                }
            } catch (SQLException e) {
                // 檢查是否是唯一約束衝突錯誤 (例如，產品和物料的組合已存在)
                if (e.getSQLState().equals("23000") || e.getMessage().contains("UNIQUE KEY constraint")) {
                    LOGGER.log(Level.WARNING, "BOM 項目已存在或唯一約束衝突：產品ID {0}, 物料ID {1}", new Object[]{bom.getProductId(), bom.getMaterialId()});
                    throw new SQLException("此產品-物料組合的 BOM 項目已存在。", e);
                }
                LOGGER.log(Level.SEVERE, "BillOfMaterialDaoImpl: 新增 BOM 項目時發生錯誤: 產品ID {0}, 物料ID {1}", new Object[]{bom.getProductId(), bom.getMaterialId()});
                LOGGER.log(Level.SEVERE, "Exception stack trace:", e);
                throw e;
            }
            return newBomId;
        }

        @Override
        public BillOfMaterial getBillOfMaterialById(int bomId) throws SQLException {
            BillOfMaterial bom = null;
            // JOIN products 和 materials 表以獲取產品名稱、物料名稱和物料單位
            String sql = "SELECT bom.bom_id, bom.product_id, p.product_name, bom.material_id, m.material_name, m.unit AS material_unit, bom.quantity, bom.create_date, bom.update_date " +
                         "FROM bill_of_materials bom " +
                         "JOIN products p ON bom.product_id = p.product_id " +
                         "JOIN materials m ON bom.material_id = m.material_id " +
                         "WHERE bom.bom_id = ?";
            try (Connection conn = DBUtil.getConnection();
                 PreparedStatement pstmt = conn.prepareStatement(sql)) {

                pstmt.setInt(1, bomId);

                try (ResultSet rs = pstmt.executeQuery()) {
                    if (rs.next()) {
                        bom = new BillOfMaterial();
                        bom.setBomId(rs.getInt("bom_id"));
                        bom.setProductId(rs.getInt("product_id"));
                        bom.setProductName(rs.getString("product_name"));
                        bom.setMaterialId(rs.getInt("material_id"));
                        bom.setMaterialName(rs.getString("material_name"));
                        bom.setMaterialUnit(rs.getString("material_unit"));
                        bom.setQuantity(rs.getBigDecimal("quantity"));
                        bom.setCreateDate(rs.getTimestamp("create_date"));
                        bom.setUpdateDate(rs.getTimestamp("update_date"));
                        LOGGER.log(Level.INFO, "BillOfMaterialDaoImpl: 成功獲取 BOM 項目，ID: {0}", bomId);
                    }
                }
            } catch (SQLException e) {
                LOGGER.log(Level.SEVERE, "BillOfMaterialDaoImpl: 根據ID獲取 BOM 項目時發生錯誤: " + bomId, e);
                throw e;
            }
            return bom;
        }

        @Override
        public List<BillOfMaterial> getAllBillOfMaterials() throws SQLException {
            List<BillOfMaterial> boms = new ArrayList<>();
            String sql = "SELECT bom.bom_id, bom.product_id, p.product_name, bom.material_id, m.material_name, m.unit AS material_unit, bom.quantity, bom.create_date, bom.update_date " +
                         "FROM bill_of_materials bom " +
                         "JOIN products p ON bom.product_id = p.product_id " +
                         "JOIN materials m ON bom.material_id = m.material_id";
            try (Connection conn = DBUtil.getConnection();
                 PreparedStatement pstmt = conn.prepareStatement(sql);
                 ResultSet rs = pstmt.executeQuery()) {

                while (rs.next()) {
                    BillOfMaterial bom = new BillOfMaterial();
                    bom.setBomId(rs.getInt("bom_id"));
                    bom.setProductId(rs.getInt("product_id"));
                    bom.setProductName(rs.getString("product_name"));
                    bom.setMaterialId(rs.getInt("material_id"));
                    bom.setMaterialName(rs.getString("material_name"));
                    bom.setMaterialUnit(rs.getString("material_unit"));
                    bom.setQuantity(rs.getBigDecimal("quantity"));
                    bom.setCreateDate(rs.getTimestamp("create_date"));
                    bom.setUpdateDate(rs.getTimestamp("update_date"));
                    boms.add(bom);
                }
                LOGGER.log(Level.INFO, "BillOfMaterialDaoImpl: 成功獲取 {0} 個 BOM 項目。", boms.size());
            } catch (SQLException e) {
                LOGGER.log(Level.SEVERE, "BillOfMaterialDaoImpl: 獲取所有 BOM 項目時發生錯誤。", e);
                throw e;
            }
            return boms;
        }

        @Override
        public List<BillOfMaterial> getBillOfMaterialsByProductId(int productId) throws SQLException {
            List<BillOfMaterial> boms = new ArrayList<>();
            String sql = "SELECT bom.bom_id, bom.product_id, p.product_name, bom.material_id, m.material_name, m.unit AS material_unit, bom.quantity, bom.create_date, bom.update_date " +
                         "FROM bill_of_materials bom " +
                         "JOIN products p ON bom.product_id = p.product_id " +
                         "JOIN materials m ON bom.material_id = m.material_id " +
                         "WHERE bom.product_id = ?";
            try (Connection conn = DBUtil.getConnection();
                 PreparedStatement pstmt = conn.prepareStatement(sql)) {

                pstmt.setInt(1, productId);
                try (ResultSet rs = pstmt.executeQuery()) {
                    while (rs.next()) {
                        BillOfMaterial bom = new BillOfMaterial();
                        bom.setBomId(rs.getInt("bom_id"));
                        bom.setProductId(rs.getInt("product_id"));
                        bom.setProductName(rs.getString("product_name"));
                        bom.setMaterialId(rs.getInt("material_id"));
                        bom.setMaterialName(rs.getString("material_name"));
                        bom.setMaterialUnit(rs.getString("material_unit"));
                        bom.setQuantity(rs.getBigDecimal("quantity"));
                        bom.setCreateDate(rs.getTimestamp("create_date"));
                        bom.setUpdateDate(rs.getTimestamp("update_date"));
                        boms.add(bom);
                    }
                }
                LOGGER.log(Level.INFO, "BillOfMaterialDaoImpl: 成功獲取產品ID {0} 的 {1} 個 BOM 項目。", new Object[]{productId, boms.size()});
            } catch (SQLException e) {
                LOGGER.log(Level.SEVERE, "BillOfMaterialDaoImpl: 根據產品ID獲取 BOM 項目時發生錯誤: " + productId, e);
                throw e;
            }
            return boms;
        }

        @Override
        public boolean updateBillOfMaterial(BillOfMaterial bom) throws SQLException {
            String sql = "UPDATE bill_of_materials SET product_id = ?, material_id = ?, quantity = ?, update_date = ? WHERE bom_id = ?";
            try (Connection conn = DBUtil.getConnection();
                 PreparedStatement pstmt = conn.prepareStatement(sql)) {

                pstmt.setInt(1, bom.getProductId());
                pstmt.setInt(2, bom.getMaterialId());
                pstmt.setBigDecimal(3, bom.getQuantity());
                pstmt.setTimestamp(4, new Timestamp(System.currentTimeMillis())); // 更新修改時間
                pstmt.setInt(5, bom.getBomId());

                int affectedRows = pstmt.executeUpdate();
                if (affectedRows > 0) {
                    LOGGER.log(Level.INFO, "BillOfMaterialDaoImpl: 成功更新 BOM 項目，ID: {0}", bom.getBomId());
                    return true;
                } else {
                    LOGGER.log(Level.WARNING, "BillOfMaterialDaoImpl: 更新 BOM 項目失敗，ID: {0}，沒有找到匹配的記錄。", bom.getBomId());
                    return false;
                }
            } catch (SQLException e) {
                // 檢查是否是唯一約束衝突錯誤
                if (e.getSQLState().equals("23000") || e.getMessage().contains("UNIQUE KEY constraint")) {
                     LOGGER.log(Level.WARNING, "BOM 項目更新失敗，此產品-物料組合已存在：產品ID {0}, 物料ID {1}", new Object[]{bom.getProductId(), bom.getMaterialId()});
                     throw new SQLException("更新失敗：此產品-物料組合的 BOM 項目已存在。", e);
                 }
                LOGGER.log(Level.SEVERE, "BillOfMaterialDaoImpl: 更新 BOM 項目時發生錯誤: " + bom.getBomId(), e);
                throw e;
            }
        }

        @Override
        public boolean deleteBillOfMaterial(int bomId) throws SQLException {
            String sql = "DELETE FROM bill_of_materials WHERE bom_id = ?";
            try (Connection conn = DBUtil.getConnection();
                 PreparedStatement pstmt = conn.prepareStatement(sql)) {

                pstmt.setInt(1, bomId);
                int affectedRows = pstmt.executeUpdate();
                if (affectedRows > 0) {
                    LOGGER.log(Level.INFO, "BillOfMaterialDaoImpl: 成功刪除 BOM 項目，ID: {0}", bomId);
                    return true;
                } else {
                    LOGGER.log(Level.WARNING, "BillOfMaterialDaoImpl: 刪除 BOM 項目失敗，ID: {0}，沒有找到匹配的記錄。", bomId);
                    return false;
                }
            } catch (SQLException e) {
                LOGGER.log(Level.SEVERE, "BillOfMaterialDaoImpl: 刪除 BOM 項目時發生錯誤: " + bomId, e);
                throw e;
            }
        }

        @Override
        public boolean deleteBillOfMaterialsByProductId(int productId) throws SQLException {
            String sql = "DELETE FROM bill_of_materials WHERE product_id = ?";
            try (Connection conn = DBUtil.getConnection();
                 PreparedStatement pstmt = conn.prepareStatement(sql)) {

                pstmt.setInt(1, productId);
                int affectedRows = pstmt.executeUpdate();
                LOGGER.log(Level.INFO, "BillOfMaterialDaoImpl: 成功刪除產品ID {0} 的 {1} 個 BOM 項目。", new Object[]{productId, affectedRows});
                return affectedRows > 0;
            } catch (SQLException e) {
                LOGGER.log(Level.SEVERE, "BillOfMaterialDaoImpl: 根據產品ID刪除 BOM 項目時發生錯誤: " + productId, e);
                throw e;
            }
        }
    }
    