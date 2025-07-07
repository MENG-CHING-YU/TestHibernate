package com.example.dao;

import com.example.model.ProductionSchedule;
import java.sql.SQLException;
import java.util.Date; // 使用 java.util.Date
import java.util.List;

/**
 * ProductionScheduleDao 介面定義了對 ProductionSchedule (生產排程明細) 資料實體的 CRUD 操作。
 */
public interface ProductionScheduleDao {

    /**
     * 新增一個生產排程明細到資料庫。
     * @param schedule 要新增的排程物件。
     * @return 新增排程後，資料庫自動生成的排程ID。
     * @throws SQLException 如果資料庫操作發生錯誤。
     */
    int addProductionSchedule(ProductionSchedule schedule) throws SQLException;

    /**
     * 根據排程ID從資料庫中獲取排程明細。
     * 查詢結果會包含產品名稱、產品代碼和工單編號。
     * @param scheduleId 排程的唯一識別ID。
     * @return 匹配指定ID的排程物件，如果未找到則返回 null。
     * @throws SQLException 如果資料庫操作發生錯誤。
     */
    ProductionSchedule getProductionScheduleById(int scheduleId) throws SQLException;

    /**
     * 從資料庫中獲取所有生產排程明細的列表。
     * 每個排程會包含其關聯的產品名稱、產品代碼和工單編號。
     * @return 包含所有 ProductionSchedule 物件的列表。
     * @throws SQLException 如果資料庫操作發生錯誤。
     */
    List<ProductionSchedule> getAllProductionSchedules() throws SQLException;

    /**
     * 根據排程狀態獲取生產排程明細列表。
     * @param status 要篩選的排程狀態。
     * @return 匹配指定狀態的 ProductionSchedule 物件列表。
     * @throws SQLException 如果資料庫操作發生錯誤。
     */
    List<ProductionSchedule> getProductionSchedulesByStatus(String status) throws SQLException;

    /**
     * 根據產品ID獲取相關的生產排程明細列表。
     * @param productId 產品的ID。
     * @return 該產品相關的所有 ProductionSchedule 物件的列表。
     * @throws SQLException 如果資料庫操作發生錯誤。
     */
    List<ProductionSchedule> getProductionSchedulesByProductId(int productId) throws SQLException;

    /**
     * 根據計劃生產日期獲取生產排程明細列表。
     * @param scheduledDate 要篩選的計劃生產日期。
     * @return 匹配指定日期的 ProductionSchedule 物件列表。
     * @throws SQLException 如果資料庫操作發生錯誤。
     */
    List<ProductionSchedule> getProductionSchedulesByDate(Date scheduledDate) throws SQLException;

    /**
     * 更新資料庫中現有的生產排程明細資訊。
     * @param schedule 包含更新資訊的排程物件 (必須包含有效的排程ID)。
     * @return 如果更新成功返回 true，否則返回 false。
     * @throws SQLException 如果資料庫操作發生錯誤。
     */
    boolean updateProductionSchedule(ProductionSchedule schedule) throws SQLException;

    /**
     * 從資料庫中刪除指定ID的生產排程明細。
     * @param scheduleId 要刪除排程的ID。
     * @return 如果刪除成功返回 true，否則返回 false。
     * @throws SQLException 如果資料庫操作發生錯誤。
     */
    boolean deleteProductionSchedule(int scheduleId) throws SQLException;
}
