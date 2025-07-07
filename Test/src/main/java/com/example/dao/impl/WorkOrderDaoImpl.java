package com.example.dao.impl;

import com.example.dao.WorkOrderDao;
import com.example.dao.util.DBUtil; // 引入資料庫工具類
import com.example.model.WorkOrder;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Date; // 對應 SQL 的 DATE 類型
import java.sql.Timestamp; // 對應 SQL 的 DATETIME 類型
// import java.time.LocalDate; // 不再直接使用 LocalDate
import java.time.LocalDateTime; // 對應 DATETIME 的 Java 8+ 日期時間類
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * WorkOrderDao 介面的 JDBC 實現類。
 * 負責 WorkOrder 實體與資料庫之間的數據交互。
 */
public class WorkOrderDaoImpl implements WorkOrderDao {

    private static final Logger LOGGER = Logger.getLogger(WorkOrderDaoImpl.class.getName());

    @Override
    public int addWorkOrder(WorkOrder workOrder) throws SQLException {
        String sql = "INSERT INTO work_orders (work_order_number, product_id, quantity, unit, scheduled_start_date, scheduled_due_date, status, notes, create_date, update_date) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
        int newWorkOrderId = -1;
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            pstmt.setString(1, workOrder.getWorkOrderNumber());
            pstmt.setInt(2, workOrder.getProductId());
            pstmt.setBigDecimal(3, workOrder.getQuantity());
            pstmt.setString(4, workOrder.getUnit());
            // 將 java.util.Date 轉換為 java.sql.Date
            pstmt.setDate(5, new Date(workOrder.getScheduledStartDate().getTime()));
            pstmt.setDate(6, new Date(workOrder.getScheduledDueDate().getTime()));
            pstmt.setString(7, workOrder.getStatus());
            pstmt.setString(8, workOrder.getNotes());
            pstmt.setTimestamp(9, Timestamp.valueOf(LocalDateTime.now())); // 設置創建時間
            pstmt.setTimestamp(10, Timestamp.valueOf(LocalDateTime.now())); // 設置更新時間

            int affectedRows = pstmt.executeUpdate();

            if (affectedRows == 0) {
                LOGGER.log(Level.WARNING, "新增工單失敗，沒有行受影響。工單編號: {0}", workOrder.getWorkOrderNumber());
                throw new SQLException("新增工單失敗，沒有行受影響。");
            }

            try (ResultSet generatedKeys = pstmt.getGeneratedKeys()) {
                if (generatedKeys.next()) {
                    newWorkOrderId = generatedKeys.getInt(1);
                    workOrder.setWorkOrderId(newWorkOrderId); // 將生成的ID設置回工單物件
                    LOGGER.log(Level.INFO, "成功新增工單: {0}，ID: {1}", new Object[]{workOrder.getWorkOrderNumber(), newWorkOrderId});
                } else {
                    LOGGER.log(Level.WARNING, "新增工單失敗，無法獲取生成的ID。工單編號: {0}", workOrder.getWorkOrderNumber());
                    throw new SQLException("新增工單失敗，無法獲取生成的ID。");
                }
            }
        } catch (SQLException e) {
            // 檢查是否是唯一約束衝突錯誤 (例如，工單編號已存在)
            if (e.getSQLState().equals("23000") || e.getMessage().contains("UNIQUE KEY constraint")) {
                LOGGER.log(Level.WARNING, "工單編號已存在或唯一約束衝突：工單編號 {0}", workOrder.getWorkOrderNumber());
                throw new SQLException("此工單編號已存在。", e);
            }
            LOGGER.log(Level.SEVERE, "WorkOrderDaoImpl: 新增工單時發生錯誤: " + workOrder.getWorkOrderNumber(), e);
            throw e;
        }
        return newWorkOrderId;
    }

    @Override
    public WorkOrder getWorkOrderById(int workOrderId) throws SQLException {
        WorkOrder workOrder = null;
        // JOIN products 表以獲取產品名稱和產品代碼
        String sql = "SELECT wo.work_order_id, wo.work_order_number, wo.product_id, p.product_code, p.product_name, p.unit, wo.quantity, wo.scheduled_start_date, wo.scheduled_due_date, wo.actual_start_date, wo.actual_completion_date, wo.status, wo.notes, wo.create_date, wo.update_date " +
                     "FROM work_orders wo " +
                     "JOIN products p ON wo.product_id = p.product_id " +
                     "WHERE wo.work_order_id = ?";
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, workOrderId);

            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    workOrder = mapResultSetToWorkOrder(rs); // 使用輔助方法映射
                    LOGGER.log(Level.INFO, "WorkOrderDaoImpl: 成功獲取工單，ID: {0}", workOrderId);
                }
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "WorkOrderDaoImpl: 根據ID獲取工單時發生錯誤: " + workOrderId, e);
            throw e;
        }
        return workOrder;
    }

    @Override
    public List<WorkOrder> getAllWorkOrders() throws SQLException {
        List<WorkOrder> workOrders = new ArrayList<>();
        String sql = "SELECT wo.work_order_id, wo.work_order_number, wo.product_id, p.product_code, p.product_name, p.unit, wo.quantity, wo.scheduled_start_date, wo.scheduled_due_date, wo.actual_start_date, wo.actual_completion_date, wo.status, wo.notes, wo.create_date, wo.update_date " +
                     "FROM work_orders wo " +
                     "JOIN products p ON wo.product_id = p.product_id " +
                     "ORDER BY wo.create_date DESC"; // 依創建日期倒序
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql);
             ResultSet rs = pstmt.executeQuery()) {

            while (rs.next()) {
                workOrders.add(mapResultSetToWorkOrder(rs));
            }
            LOGGER.log(Level.INFO, "WorkOrderDaoImpl: 成功獲取 {0} 個工單。", workOrders.size());
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "WorkOrderDaoImpl: 獲取所有工單時發生錯誤。", e);
            throw e;
        }
        return workOrders;
    }

    @Override
    public List<WorkOrder> getWorkOrdersByStatus(String status) throws SQLException {
        List<WorkOrder> workOrders = new ArrayList<>();
        String sql = "SELECT wo.work_order_id, wo.work_order_number, wo.product_id, p.product_code, p.product_name, p.unit, wo.quantity, wo.scheduled_start_date, wo.scheduled_due_date, wo.actual_start_date, wo.actual_completion_date, wo.status, wo.notes, wo.create_date, wo.update_date " +
                     "FROM work_orders wo " +
                     "JOIN products p ON wo.product_id = p.product_id " +
                     "WHERE wo.status = ? ORDER BY wo.create_date DESC";
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, status);
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    workOrders.add(mapResultSetToWorkOrder(rs));
                }
            }
            LOGGER.log(Level.INFO, "WorkOrderDaoImpl: 成功獲取狀態為 '{0}' 的 {1} 個工單。", new Object[]{status, workOrders.size()});
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "WorkOrderDaoImpl: 根據狀態獲取工單時發生錯誤: " + status, e);
            throw e;
        }
        return workOrders;
    }

    @Override
    public List<WorkOrder> getWorkOrdersByProductId(int productId) throws SQLException {
        List<WorkOrder> workOrders = new ArrayList<>();
        String sql = "SELECT wo.work_order_id, wo.work_order_number, wo.product_id, p.product_code, p.product_name, p.unit, wo.quantity, wo.scheduled_start_date, wo.scheduled_due_date, wo.actual_start_date, wo.actual_completion_date, wo.status, wo.notes, wo.create_date, wo.update_date " +
                     "FROM work_orders wo " +
                     "JOIN products p ON wo.product_id = p.product_id " +
                     "WHERE wo.product_id = ? ORDER BY wo.create_date DESC";
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, productId);
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    workOrders.add(mapResultSetToWorkOrder(rs));
                }
            }
            LOGGER.log(Level.INFO, "WorkOrderDaoImpl: 成功獲取產品ID {0} 的 {1} 個工單。", new Object[]{productId, workOrders.size()});
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "WorkOrderDaoImpl: 根據產品ID獲取工單時發生錯誤: " + productId, e);
            throw e;
        }
        return workOrders;
    }


    @Override
    public boolean updateWorkOrder(WorkOrder workOrder) throws SQLException {
        String sql = "UPDATE work_orders SET work_order_number = ?, product_id = ?, quantity = ?, unit = ?, scheduled_start_date = ?, scheduled_due_date = ?, actual_start_date = ?, actual_completion_date = ?, status = ?, notes = ?, update_date = ? WHERE work_order_id = ?";
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, workOrder.getWorkOrderNumber());
            pstmt.setInt(2, workOrder.getProductId());
            pstmt.setBigDecimal(3, workOrder.getQuantity());
            pstmt.setString(4, workOrder.getUnit());
            pstmt.setDate(5, new Date(workOrder.getScheduledStartDate().getTime()));
            pstmt.setDate(6, new Date(workOrder.getScheduledDueDate().getTime()));
            // 對於可為空的日期，檢查 null 並設置
            pstmt.setDate(7, workOrder.getActualStartDate() != null ? new Date(workOrder.getActualStartDate().getTime()) : null);
            pstmt.setDate(8, workOrder.getActualCompletionDate() != null ? new Date(workOrder.getActualCompletionDate().getTime()) : null);
            pstmt.setString(9, workOrder.getStatus());
            pstmt.setString(10, workOrder.getNotes());
            pstmt.setTimestamp(11, Timestamp.valueOf(LocalDateTime.now())); // 更新修改時間
            pstmt.setInt(12, workOrder.getWorkOrderId());

            int affectedRows = pstmt.executeUpdate();
            if (affectedRows > 0) {
                LOGGER.log(Level.INFO, "WorkOrderDaoImpl: 成功更新工單，ID: {0}", workOrder.getWorkOrderId());
                return true;
            } else {
                LOGGER.log(Level.WARNING, "WorkOrderDaoImpl: 更新工單失敗，ID: {0}，沒有找到匹配的記錄。", workOrder.getWorkOrderId());
                return false;
            }
        } catch (SQLException e) {
            // 檢查是否是唯一約束衝突錯誤 (工單編號重複)
            if (e.getSQLState().equals("23000") || e.getMessage().contains("UNIQUE KEY constraint")) {
                LOGGER.log(Level.WARNING, "工單更新失敗，工單編號已存在或唯一約束衝突：工單編號 {0}", workOrder.getWorkOrderNumber());
                throw new SQLException("更新失敗：此工單編號已存在。", e);
            }
            LOGGER.log(Level.SEVERE, "WorkOrderDaoImpl: 更新工單時發生錯誤: " + workOrder.getWorkOrderNumber(), e);
            throw e;
        }
    }

    @Override
    public boolean deleteWorkOrder(int workOrderId) throws SQLException {
        String sql = "DELETE FROM work_orders WHERE work_order_id = ?";
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, workOrderId);
            int affectedRows = pstmt.executeUpdate();
            if (affectedRows > 0) {
                LOGGER.log(Level.INFO, "WorkOrderDaoImpl: 成功刪除工單，ID: {0}", workOrderId);
                return true;
            } else {
                LOGGER.log(Level.WARNING, "WorkOrderDaoImpl: 刪除工單失敗，ID: {0}，沒有找到匹配的記錄。", workOrderId);
                return false;
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "WorkOrderDaoImpl: 刪除工單時發生錯誤: " + workOrderId, e);
            throw e;
        }
    }

    /**
     * 輔助方法：將 ResultSet 的當前行映射到一個 WorkOrder 物件。
     * @param rs ResultSet 物件
     * @return 填充好的 WorkOrder 物件
     * @throws SQLException 如果讀取 ResultSet 時發生錯誤
     */
    private WorkOrder mapResultSetToWorkOrder(ResultSet rs) throws SQLException {
        WorkOrder workOrder = new WorkOrder();
        workOrder.setWorkOrderId(rs.getInt("work_order_id"));
        workOrder.setWorkOrderNumber(rs.getString("work_order_number"));
        workOrder.setProductId(rs.getInt("product_id"));
        workOrder.setProductCode(rs.getString("product_code")); // 從 JOIN 中獲取
        workOrder.setProductName(rs.getString("product_name")); // 從 JOIN 中獲取
        workOrder.setQuantity(rs.getBigDecimal("quantity"));
        workOrder.setUnit(rs.getString("unit")); // 從 JOIN 中獲取
        
        // 直接從 ResultSet 獲取 java.sql.Date，並設置到 WorkOrder 中 (其類型已改為 java.util.Date)
        // 因為 java.sql.Date 繼承自 java.util.Date，所以可以直接賦值。
        workOrder.setScheduledStartDate(rs.getDate("scheduled_start_date"));
        workOrder.setScheduledDueDate(rs.getDate("scheduled_due_date"));

        // 處理可為空的日期欄位
        workOrder.setActualStartDate(rs.getDate("actual_start_date"));
        workOrder.setActualCompletionDate(rs.getDate("actual_completion_date"));

        workOrder.setStatus(rs.getString("status"));
        workOrder.setNotes(rs.getString("notes"));
        workOrder.setCreateDate(rs.getTimestamp("create_date").toLocalDateTime());
        workOrder.setUpdateDate(rs.getTimestamp("update_date").toLocalDateTime());
        return workOrder;
    }
}
