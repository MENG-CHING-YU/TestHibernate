package com.example.dao.impl;

import com.example.dao.ProductionScheduleDao;
import com.example.dao.util.DBUtil; // 引入資料庫工具類
import com.example.model.ProductionSchedule;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Date; // 對應 SQL 的 DATE 類型
import java.sql.Timestamp; // 對應 SQL 的 DATETIME 類型
import java.time.LocalDateTime; // 對應 DATETIME 的 Java 8+ 日期時間類
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * ProductionScheduleDao 介面的 JDBC 實現類。
 * 負責 ProductionSchedule 實體與資料庫之間的數據交互。
 */
public class ProductionScheduleDaoImpl implements ProductionScheduleDao {

    private static final Logger LOGGER = Logger.getLogger(ProductionScheduleDaoImpl.class.getName());

    @Override
    public int addProductionSchedule(ProductionSchedule schedule) throws SQLException {
        String sql = "INSERT INTO production_schedules (work_order_id, product_id, scheduled_date, shift, planned_quantity, actual_quantity, status, notes, create_date, update_date) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
        int newScheduleId = -1;
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            if (schedule.getWorkOrderId() != null) {
                pstmt.setInt(1, schedule.getWorkOrderId());
            } else {
                pstmt.setNull(1, java.sql.Types.INTEGER); // 如果 workOrderId 為 null，設置為 SQL NULL
            }
            pstmt.setInt(2, schedule.getProductId());
            pstmt.setDate(3, new Date(schedule.getScheduledDate().getTime())); // java.util.Date 轉 java.sql.Date
            pstmt.setString(4, schedule.getShift());
            pstmt.setBigDecimal(5, schedule.getPlannedQuantity());
            
            if (schedule.getActualQuantity() != null) {
                pstmt.setBigDecimal(6, schedule.getActualQuantity());
            } else {
                pstmt.setNull(6, java.sql.Types.DECIMAL); // 如果 actualQuantity 為 null，設置為 SQL NULL
            }
            pstmt.setString(7, schedule.getStatus());
            pstmt.setString(8, schedule.getNotes());
            pstmt.setTimestamp(9, Timestamp.valueOf(LocalDateTime.now())); // 設置創建時間
            pstmt.setTimestamp(10, Timestamp.valueOf(LocalDateTime.now())); // 設置更新時間

            int affectedRows = pstmt.executeUpdate();

            if (affectedRows == 0) {
                LOGGER.log(Level.WARNING, "新增生產排程失敗，沒有行受影響。");
                throw new SQLException("新增生產排程失敗，沒有行受影響。");
            }

            try (ResultSet generatedKeys = pstmt.getGeneratedKeys()) {
                if (generatedKeys.next()) {
                    newScheduleId = generatedKeys.getInt(1);
                    schedule.setScheduleId(newScheduleId); // 將生成的ID設置回物件
                    LOGGER.log(Level.INFO, "成功新增生產排程，ID: {0}", newScheduleId);
                } else {
                    LOGGER.log(Level.WARNING, "新增生產排程失敗，無法獲取生成的ID。");
                    throw new SQLException("新增生產排程失敗，無法獲取生成的ID。");
                }
            }
        } catch (SQLException e) {
            // 檢查是否是唯一約束衝突錯誤 (例如，product_id, scheduled_date, shift 組合重複)
            if (e.getSQLState().equals("23000") || e.getMessage().contains("UQ_Schedule_Product_Date_Shift")) {
                LOGGER.log(Level.WARNING, "生產排程已存在或唯一約束衝突：產品ID {0}, 日期 {1}, 班次 {2}", new Object[]{schedule.getProductId(), schedule.getScheduledDate(), schedule.getShift()});
                throw new SQLException("同一天同一班次，該產品已存在排程。", e);
            }
            LOGGER.log(Level.SEVERE, "ProductionScheduleDaoImpl: 新增生產排程時發生錯誤。", e);
            throw e;
        }
        return newScheduleId;
    }

    @Override
    public ProductionSchedule getProductionScheduleById(int scheduleId) throws SQLException {
        ProductionSchedule schedule = null;
        // JOIN products 和 work_orders 表以獲取詳細信息
        String sql = "SELECT ps.schedule_id, ps.work_order_id, wo.work_order_number, " +
                     "ps.product_id, p.product_code, p.product_name, p.unit AS product_unit, " + // 獲取產品單位
                     "ps.scheduled_date, ps.shift, ps.planned_quantity, ps.actual_quantity, " +
                     "ps.status, ps.notes, ps.create_date, ps.update_date " +
                     "FROM production_schedules ps " +
                     "LEFT JOIN work_orders wo ON ps.work_order_id = wo.work_order_id " + // LEFT JOIN 允許 work_order_id 為 NULL
                     "JOIN products p ON ps.product_id = p.product_id " +
                     "WHERE ps.schedule_id = ?";
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, scheduleId);

            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    schedule = mapResultSetToProductionSchedule(rs);
                    LOGGER.log(Level.INFO, "ProductionScheduleDaoImpl: 成功獲取生產排程，ID: {0}", scheduleId);
                }
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "ProductionScheduleDaoImpl: 根據ID獲取生產排程時發生錯誤: " + scheduleId, e);
            throw e;
        }
        return schedule;
    }

    @Override
    public List<ProductionSchedule> getAllProductionSchedules() throws SQLException {
        List<ProductionSchedule> schedules = new ArrayList<>();
        String sql = "SELECT ps.schedule_id, ps.work_order_id, wo.work_order_number, " +
                     "ps.product_id, p.product_code, p.product_name, p.unit AS product_unit, " + // 獲取產品單位
                     "ps.scheduled_date, ps.shift, ps.planned_quantity, ps.actual_quantity, " +
                     "ps.status, ps.notes, ps.create_date, ps.update_date " +
                     "FROM production_schedules ps " +
                     "LEFT JOIN work_orders wo ON ps.work_order_id = wo.work_order_id " +
                     "JOIN products p ON ps.product_id = p.product_id " +
                     "ORDER BY ps.scheduled_date DESC, ps.shift, ps.product_id"; // 依日期、班次、產品排序
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql);
             ResultSet rs = pstmt.executeQuery()) {

            while (rs.next()) {
                schedules.add(mapResultSetToProductionSchedule(rs));
            }
            LOGGER.log(Level.INFO, "ProductionScheduleDaoImpl: 成功獲取 {0} 個生產排程。", schedules.size());
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "ProductionScheduleDaoImpl: 獲取所有生產排程時發生錯誤。", e);
            throw e;
        }
        return schedules;
    }

    @Override
    public List<ProductionSchedule> getProductionSchedulesByStatus(String status) throws SQLException {
        List<ProductionSchedule> schedules = new ArrayList<>();
        String sql = "SELECT ps.schedule_id, ps.work_order_id, wo.work_order_number, " +
                     "ps.product_id, p.product_code, p.product_name, p.unit AS product_unit, " +
                     "ps.scheduled_date, ps.shift, ps.planned_quantity, ps.actual_quantity, " +
                     "ps.status, ps.notes, ps.create_date, ps.update_date " +
                     "FROM production_schedules ps " +
                     "LEFT JOIN work_orders wo ON ps.work_order_id = wo.work_order_id " +
                     "JOIN products p ON ps.product_id = p.product_id " +
                     "WHERE ps.status = ? ORDER BY ps.scheduled_date DESC, ps.shift, ps.product_id";
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, status);
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    schedules.add(mapResultSetToProductionSchedule(rs));
                }
            }
            LOGGER.log(Level.INFO, "ProductionScheduleDaoImpl: 成功獲取狀態為 '{0}' 的 {1} 個生產排程。", new Object[]{status, schedules.size()});
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "ProductionScheduleDaoImpl: 根據狀態獲取生產排程時發生錯誤: " + status, e);
            throw e;
        }
        return schedules;
    }

    @Override
    public List<ProductionSchedule> getProductionSchedulesByProductId(int productId) throws SQLException {
        List<ProductionSchedule> schedules = new ArrayList<>();
        String sql = "SELECT ps.schedule_id, ps.work_order_id, wo.work_order_number, " +
                     "ps.product_id, p.product_code, p.product_name, p.unit AS product_unit, " +
                     "ps.scheduled_date, ps.shift, ps.planned_quantity, ps.actual_quantity, " +
                     "ps.status, ps.notes, ps.create_date, ps.update_date " +
                     "FROM production_schedules ps " +
                     "LEFT JOIN work_orders wo ON ps.work_order_id = wo.work_order_id " +
                     "JOIN products p ON ps.product_id = p.product_id " +
                     "WHERE ps.product_id = ? ORDER BY ps.scheduled_date DESC, ps.shift";
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, productId);
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    schedules.add(mapResultSetToProductionSchedule(rs));
                }
            }
            LOGGER.log(Level.INFO, "ProductionScheduleDaoImpl: 成功獲取產品ID {0} 的 {1} 個生產排程。", new Object[]{productId, schedules.size()});
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "ProductionScheduleDaoImpl: 根據產品ID獲取生產排程時發生錯誤: " + productId, e);
            throw e;
        }
        return schedules;
    }

    @Override
    public List<ProductionSchedule> getProductionSchedulesByDate(java.util.Date scheduledDate) throws SQLException {
        List<ProductionSchedule> schedules = new ArrayList<>();
        String sql = "SELECT ps.schedule_id, ps.work_order_id, wo.work_order_number, " +
                     "ps.product_id, p.product_code, p.product_name, p.unit AS product_unit, " +
                     "ps.scheduled_date, ps.shift, ps.planned_quantity, ps.actual_quantity, " +
                     "ps.status, ps.notes, ps.create_date, ps.update_date " +
                     "FROM production_schedules ps " +
                     "LEFT JOIN work_orders wo ON ps.work_order_id = wo.work_order_id " +
                     "JOIN products p ON ps.product_id = p.product_id " +
                     "WHERE ps.scheduled_date = ? ORDER BY ps.shift, ps.product_id";
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setDate(1, new Date(scheduledDate.getTime())); // 將 java.util.Date 轉換為 java.sql.Date
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    schedules.add(mapResultSetToProductionSchedule(rs));
                }
            }
            LOGGER.log(Level.INFO, "ProductionScheduleDaoImpl: 成功獲取日期為 '{0}' 的 {1} 個生產排程。", new Object[]{scheduledDate, schedules.size()});
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "ProductionScheduleDaoImpl: 根據日期獲取生產排程時發生錯誤: " + scheduledDate, e);
            throw e;
        }
        return schedules;
    }

    @Override
    public boolean updateProductionSchedule(ProductionSchedule schedule) throws SQLException {
        String sql = "UPDATE production_schedules SET work_order_id = ?, product_id = ?, scheduled_date = ?, shift = ?, planned_quantity = ?, actual_quantity = ?, status = ?, notes = ?, update_date = ? WHERE schedule_id = ?";
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            if (schedule.getWorkOrderId() != null) {
                pstmt.setInt(1, schedule.getWorkOrderId());
            } else {
                pstmt.setNull(1, java.sql.Types.INTEGER);
            }
            pstmt.setInt(2, schedule.getProductId());
            pstmt.setDate(3, new Date(schedule.getScheduledDate().getTime()));
            pstmt.setString(4, schedule.getShift());
            pstmt.setBigDecimal(5, schedule.getPlannedQuantity());
            if (schedule.getActualQuantity() != null) {
                pstmt.setBigDecimal(6, schedule.getActualQuantity());
            } else {
                pstmt.setNull(6, java.sql.Types.DECIMAL);
            }
            pstmt.setString(7, schedule.getStatus());
            pstmt.setString(8, schedule.getNotes());
            pstmt.setTimestamp(9, Timestamp.valueOf(LocalDateTime.now())); // 更新修改時間
            pstmt.setInt(10, schedule.getScheduleId());

            int affectedRows = pstmt.executeUpdate();
            if (affectedRows > 0) {
                LOGGER.log(Level.INFO, "ProductionScheduleDaoImpl: 成功更新生產排程，ID: {0}", schedule.getScheduleId());
                return true;
            } else {
                LOGGER.log(Level.WARNING, "ProductionScheduleDaoImpl: 更新生產排程失敗，ID: {0}，沒有找到匹配的記錄。", schedule.getScheduleId());
                return false;
            }
        } catch (SQLException e) {
            // 檢查是否是唯一約束衝突錯誤
            if (e.getSQLState().equals("23000") || e.getMessage().contains("UQ_Schedule_Product_Date_Shift")) {
                LOGGER.log(Level.WARNING, "生產排程更新失敗，唯一約束衝突：產品ID {0}, 日期 {1}, 班次 {2}", new Object[]{schedule.getProductId(), schedule.getScheduledDate(), schedule.getShift()});
                throw new SQLException("更新失敗：同一天同一班次，該產品已存在排程。", e);
            }
            LOGGER.log(Level.SEVERE, "ProductionScheduleDaoImpl: 更新生產排程時發生錯誤: " + schedule.getScheduleId(), e);
            throw e;
        }
    }

    @Override
    public boolean deleteProductionSchedule(int scheduleId) throws SQLException {
        String sql = "DELETE FROM production_schedules WHERE schedule_id = ?";
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, scheduleId);
            int affectedRows = pstmt.executeUpdate();
            if (affectedRows > 0) {
                LOGGER.log(Level.INFO, "ProductionScheduleDaoImpl: 成功刪除生產排程，ID: {0}", scheduleId);
                return true;
            } else {
                LOGGER.log(Level.WARNING, "ProductionScheduleDaoImpl: 刪除生產排程失敗，ID: {0}，沒有找到匹配的記錄。", scheduleId);
                return false;
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "ProductionScheduleDaoImpl: 刪除生產排程時發生錯誤: " + scheduleId, e);
            throw e;
        }
    }

    /**
     * 輔助方法：將 ResultSet 的當前行映射到一個 ProductionSchedule 物件。
     * @param rs ResultSet 物件
     * @return 填充好的 ProductionSchedule 物件
     * @throws SQLException 如果讀取 ResultSet 時發生錯誤
     */
    private ProductionSchedule mapResultSetToProductionSchedule(ResultSet rs) throws SQLException {
        ProductionSchedule schedule = new ProductionSchedule();
        schedule.setScheduleId(rs.getInt("schedule_id"));
        
        // 處理可為空的 work_order_id
        int workOrderId = rs.getInt("work_order_id");
        if (rs.wasNull()) { // 檢查 last-read column 是否為 SQL NULL
            schedule.setWorkOrderId(null);
        } else {
            schedule.setWorkOrderId(workOrderId);
        }
        
        // 直接獲取 work_order_number，如果 SQL 為 NULL，getString 會返回 Java null
        // 針對 LEFT JOIN 後可能為 NULL 的情況，getString() 會正確返回 null。
        schedule.setWorkOrderNumber(rs.getString("work_order_number")); 

        schedule.setProductId(rs.getInt("product_id"));
        schedule.setProductCode(rs.getString("product_code"));
        schedule.setProductName(rs.getString("product_name"));
        schedule.setScheduledDate(rs.getDate("scheduled_date")); // 直接獲取 java.util.Date
        schedule.setShift(rs.getString("shift"));
        schedule.setPlannedQuantity(rs.getBigDecimal("planned_quantity"));
        
        // 處理可為空的 actual_quantity
        BigDecimal actualQuantity = rs.getBigDecimal("actual_quantity");
        if (rs.wasNull()) {
            schedule.setActualQuantity(null);
        } else {
            schedule.setActualQuantity(actualQuantity);
        }
        
        schedule.setStatus(rs.getString("status"));
        schedule.setNotes(rs.getString("notes"));
        schedule.setCreateDate(rs.getTimestamp("create_date").toLocalDateTime());
        schedule.setUpdateDate(rs.getTimestamp("update_date").toLocalDateTime());
        return schedule;
    }
}
