package com.example.service;

import com.example.model.WorkOrder;
import java.sql.SQLException;
import java.util.Date; // <-- 修改為 java.util.Date
import java.util.List;

/**
 * WorkOrderService 介面定義了生產工單相關的業務操作。
 */
public interface WorkOrderService {

    /**
     * 新增一個生產工單。
     * @param workOrder 要新增的工單物件。
     * @return 新增工單後，資料庫自動生成的工單ID。
     * @throws SQLException 如果底層資料庫操作失敗。
     * @throws IllegalArgumentException 如果工單數據無效或相關產品ID不存在。
     */
    int addWorkOrder(WorkOrder workOrder) throws SQLException, IllegalArgumentException;

    /**
     * 根據工單ID獲取工單資訊。
     * @param workOrderId 工單的唯一識別ID。
     * @return 匹配指定ID的工單物件，如果未找到則返回 null。
     * @throws SQLException 如果底層資料庫操作失敗。
     */
    WorkOrder getWorkOrderById(int workOrderId) throws SQLException;

    /**
     * 獲取所有生產工單的列表。
     * @return 包含所有 WorkOrder 物件的列表。
     * @throws SQLException 如果底層資料庫操作失敗。
     */
    List<WorkOrder> getAllWorkOrders() throws SQLException;

    /**
     * 根據工單狀態獲取工單列表。
     * @param status 要篩選的工單狀態。
     * @return 匹配指定狀態的 WorkOrder 物件列表。
     * @throws SQLException 如果底層資料庫操作失敗。
     * @throws IllegalArgumentException 如果狀態字串無效。
     */
    List<WorkOrder> getWorkOrdersByStatus(String status) throws SQLException, IllegalArgumentException;

    /**
     * 根據產品ID獲取相關的生產工單列表。
     * @param productId 產品的ID。
     * @return 該產品相關的所有 WorkOrder 物件的列表。
     * @throws SQLException 如果底層資料庫操作失敗。
     * @throws IllegalArgumentException 如果產品ID無效。
     */
    List<WorkOrder> getWorkOrdersByProductId(int productId) throws SQLException, IllegalArgumentException;

    /**
     * 更新生產工單資訊。
     * @param workOrder 包含更新資訊的工單物件 (必須包含有效的工單ID)。
     * @return 如果更新成功返回 true，否則返回 false。
     * @throws SQLException 如果底層資料庫操作失敗。
     * @throws IllegalArgumentException 如果工單數據無效或工單ID不存在。
     */
    boolean updateWorkOrder(WorkOrder workOrder) throws SQLException, IllegalArgumentException;

    /**
     * 刪除指定ID的生產工單。
     * @param workOrderId 要刪除工單的ID。
     * @return 如果刪除成功返回 true，否則返回 false。
     * @throws SQLException 如果底層資料庫操作失敗。
     * @throws IllegalArgumentException 如果工單ID無效。
     */
    boolean deleteWorkOrder(int workOrderId) throws SQLException, IllegalArgumentException;

    /**
     * 設置工單的實際開始日期，並將狀態變更為 "In Progress"。
     * @param workOrderId 工單ID。
     * @param actualStartDate 實際開始日期。 (修改為 java.util.Date)
     * @return 如果更新成功返回 true，否則返回 false。
     * @throws SQLException 如果資料庫操作失敗。
     * @throws IllegalArgumentException 如果工單ID無效或日期無效。
     * @throws IllegalStateException 如果工單狀態不允許開始（例如已完成或已取消）。
     */
    boolean startWorkOrder(int workOrderId, Date actualStartDate) throws SQLException, IllegalArgumentException, IllegalStateException; // <-- 修改參數類型

    /**
     * 設置工單的實際完成日期，並將狀態變更為 "Completed"。
     * @param workOrderId 工單ID。
     * @param actualCompletionDate 實際完成日期。 (修改為 java.util.Date)
     * @return 如果更新成功返回 true，否則返回 false。
     * @throws SQLException 如果資料庫操作失敗。
     * @throws IllegalArgumentException 如果工單ID無效或日期無效。
     * @throws IllegalStateException 如果工單狀態不允許完成（例如尚未開始或已取消）。
     */
    boolean completeWorkOrder(int workOrderId, Date actualCompletionDate) throws SQLException, IllegalArgumentException, IllegalStateException; // <-- 修改參數類型
}
