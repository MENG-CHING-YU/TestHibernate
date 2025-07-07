    package com.example.dao.impl;

    import com.example.dao.MaterialDao;
    import com.example.dao.util.DBUtil;
    import com.example.model.Material;
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
     * MaterialDao 介面的 JDBC 實現類。
     * 負責 Material 實體與資料庫之間的數據交互。
     */
    public class MaterialDaoImpl implements MaterialDao {

        private static final Logger LOGGER = Logger.getLogger(MaterialDaoImpl.class.getName());

        @Override
        public int addMaterial(Material material) throws SQLException {
            String sql = "INSERT INTO materials (material_code, material_name, description, unit, unit_cost, current_stock, is_active, create_date, update_date) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)";
            int newMaterialId = -1;
            try (Connection conn = DBUtil.getConnection();
                 PreparedStatement pstmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

                pstmt.setString(1, material.getMaterialCode());
                pstmt.setString(2, material.getMaterialName());
                pstmt.setString(3, material.getDescription());
                pstmt.setString(4, material.getUnit());
                pstmt.setBigDecimal(5, material.getUnitCost());
                pstmt.setBigDecimal(6, material.getCurrentStock());
                pstmt.setBoolean(7, material.isActive());
                pstmt.setTimestamp(8, new Timestamp(System.currentTimeMillis())); // 設置創建時間
                pstmt.setTimestamp(9, new Timestamp(System.currentTimeMillis())); // 設置更新時間

                int affectedRows = pstmt.executeUpdate();

                if (affectedRows == 0) {
                    LOGGER.log(Level.WARNING, "新增物料失敗，沒有行受影響。物料代碼: {0}", material.getMaterialCode());
                    throw new SQLException("新增物料失敗，沒有行受影響。");
                }

                try (ResultSet generatedKeys = pstmt.getGeneratedKeys()) {
                    if (generatedKeys.next()) {
                        newMaterialId = generatedKeys.getInt(1);
                        material.setMaterialId(newMaterialId); // 將生成的ID設置回物料物件
                        LOGGER.log(Level.INFO, "成功新增物料: {0}，ID: {1}", new Object[]{material.getMaterialName(), newMaterialId});
                    } else {
                        LOGGER.log(Level.WARNING, "新增物料失敗，無法獲取生成的ID。物料代碼: {0}", material.getMaterialCode());
                        throw new SQLException("新增物料失敗，無法獲取生成的ID。");
                    }
                }
            } catch (SQLException e) {
                LOGGER.log(Level.SEVERE, "MaterialDaoImpl: 新增物料時發生錯誤: " + material.getMaterialName(), e);
                throw e;
            }
            return newMaterialId;
        }

        @Override
        public Material getMaterialById(int materialId) throws SQLException {
            Material material = null;
            String sql = "SELECT material_id, material_code, material_name, description, unit, unit_cost, current_stock, is_active, create_date, update_date FROM materials WHERE material_id = ?";
            try (Connection conn = DBUtil.getConnection();
                 PreparedStatement pstmt = conn.prepareStatement(sql)) {

                pstmt.setInt(1, materialId);

                try (ResultSet rs = pstmt.executeQuery()) {
                    if (rs.next()) {
                        material = new Material();
                        material.setMaterialId(rs.getInt("material_id"));
                        material.setMaterialCode(rs.getString("material_code"));
                        material.setMaterialName(rs.getString("material_name"));
                        material.setDescription(rs.getString("description"));
                        material.setUnit(rs.getString("unit"));
                        material.setUnitCost(rs.getBigDecimal("unit_cost"));
                        material.setCurrentStock(rs.getBigDecimal("current_stock"));
                        material.setActive(rs.getBoolean("is_active"));
                        material.setCreateDate(rs.getTimestamp("create_date"));
                        material.setUpdateDate(rs.getTimestamp("update_date"));
                        LOGGER.log(Level.INFO, "MaterialDaoImpl: 成功獲取物料，ID: {0}", materialId);
                    }
                }
            } catch (SQLException e) {
                LOGGER.log(Level.SEVERE, "MaterialDaoImpl: 根據ID獲取物料時發生錯誤: " + materialId, e);
                throw e;
            }
            return material;
        }

        @Override
        public List<Material> getAllMaterials() throws SQLException {
            List<Material> materials = new ArrayList<>();
            String sql = "SELECT material_id, material_code, material_name, description, unit, unit_cost, current_stock, is_active, create_date, update_date FROM materials";
            try (Connection conn = DBUtil.getConnection();
                 PreparedStatement pstmt = conn.prepareStatement(sql);
                 ResultSet rs = pstmt.executeQuery()) {

                while (rs.next()) {
                    Material material = new Material();
                    material.setMaterialId(rs.getInt("material_id"));
                    material.setMaterialCode(rs.getString("material_code"));
                    material.setMaterialName(rs.getString("material_name"));
                    material.setDescription(rs.getString("description"));
                    material.setUnit(rs.getString("unit"));
                    material.setUnitCost(rs.getBigDecimal("unit_cost"));
                    material.setCurrentStock(rs.getBigDecimal("current_stock"));
                    material.setActive(rs.getBoolean("is_active"));
                    material.setCreateDate(rs.getTimestamp("create_date"));
                    material.setUpdateDate(rs.getTimestamp("update_date"));
                    materials.add(material);
                }
                LOGGER.log(Level.INFO, "MaterialDaoImpl: 成功獲取 {0} 個物料。", materials.size());
            } catch (SQLException e) {
                LOGGER.log(Level.SEVERE, "MaterialDaoImpl: 獲取所有物料時發生錯誤。", e);
                throw e;
            }
            return materials;
        }

        @Override
        public boolean updateMaterial(Material material) throws SQLException {
            String sql = "UPDATE materials SET material_code = ?, material_name = ?, description = ?, unit = ?, unit_cost = ?, current_stock = ?, is_active = ?, update_date = ? WHERE material_id = ?";
            try (Connection conn = DBUtil.getConnection();
                 PreparedStatement pstmt = conn.prepareStatement(sql)) {

                pstmt.setString(1, material.getMaterialCode());
                pstmt.setString(2, material.getMaterialName());
                pstmt.setString(3, material.getDescription());
                pstmt.setString(4, material.getUnit());
                pstmt.setBigDecimal(5, material.getUnitCost());
                pstmt.setBigDecimal(6, material.getCurrentStock());
                pstmt.setBoolean(7, material.isActive());
                pstmt.setTimestamp(8, new Timestamp(System.currentTimeMillis())); // 更新修改時間
                pstmt.setInt(9, material.getMaterialId());

                int affectedRows = pstmt.executeUpdate();
                if (affectedRows > 0) {
                    LOGGER.log(Level.INFO, "MaterialDaoImpl: 成功更新物料，ID: {0}", material.getMaterialId());
                    return true;
                } else {
                    LOGGER.log(Level.WARNING, "MaterialDaoImpl: 更新物料失敗，ID: {0}，沒有找到匹配的記錄。", material.getMaterialId());
                    return false;
                }
            } catch (SQLException e) {
                LOGGER.log(Level.SEVERE, "MaterialDaoImpl: 更新物料時發生錯誤: " + material.getMaterialId(), e);
                throw e;
            }
        }

        @Override
        public boolean deleteMaterial(int materialId) throws SQLException {
            String sql = "DELETE FROM materials WHERE material_id = ?";
            try (Connection conn = DBUtil.getConnection();
                 PreparedStatement pstmt = conn.prepareStatement(sql)) {

                pstmt.setInt(1, materialId);
                int affectedRows = pstmt.executeUpdate();
                if (affectedRows > 0) {
                    LOGGER.log(Level.INFO, "MaterialDaoImpl: 成功刪除物料，ID: {0}", materialId);
                    return true;
                } else {
                    LOGGER.log(Level.WARNING, "MaterialDaoImpl: 刪除物料失敗，ID: {0}，沒有找到匹配的記錄。", materialId);
                    return false;
                }
            } catch (SQLException e) {
                LOGGER.log(Level.SEVERE, "MaterialDaoImpl: 刪除物料時發生錯誤: " + materialId, e);
                throw e;
            }
        }
    }
    