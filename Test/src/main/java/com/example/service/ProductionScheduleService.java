package com.example.service;

import com.example.model.ProductionSchedule;

import java.math.BigDecimal;
import java.sql.SQLException;
import java.util.Date; // 使用 java.util.Date
import java.util.List;

/**
 * ProductionScheduleService 介面定義了生產排程明細相關的業務操作。
 */
public interface ProductionScheduleService {

    /**
     * 新增一個生產排程明細。
     * @param schedule 要新增的排程物件。
     * @return 新增排程後，資料庫自動生成的排程ID。
     * @throws SQLException 如果底層資料庫操作失敗。
     * @throws IllegalArgumentException 如果排程數據無效或相關產品/工單ID不存在。
     */
    int addProductionSchedule(ProductionSchedule schedule) throws SQLException, IllegalArgumentException;

    /**
     * 根據排程ID獲取排程明細資訊。
     * @param scheduleId 排程的唯一識別ID。
     * @return 匹配指定ID的排程物件，如果未找到則返回 null。
     * @throws SQLException 如果底層資料庫操作失敗。
     * @throws IllegalArgumentException 如果排程ID無效。
     */
    ProductionSchedule getProductionScheduleById(int scheduleId) throws SQLException, IllegalArgumentException;

    /**
     * 獲取所有生產排程明細的列表。
     * @return 包含所有 ProductionSchedule 物件的列表。
     * @throws SQLException 如果底層資料庫操作失敗。
     */
    List<ProductionSchedule> getAllProductionSchedules() throws SQLException;

    /**
     * 根據排程狀態獲取排程明細列表。
     * @param status 要篩選的排程狀態。
     * @return 匹配指定狀態的 ProductionSchedule 物件列表。
     * @throws SQLException 如果底層資料庫操作失敗。
     * @throws IllegalArgumentException 如果狀態字串無效。
     */
    List<ProductionSchedule> getProductionSchedulesByStatus(String status) throws SQLException, IllegalArgumentException;

    /**
     * 根據產品ID獲取相關的生產排程明細列表。
     * @param productId 產品的ID。
     * @return 該產品相關的所有 ProductionSchedule 物件的列表。
     * @throws SQLException 如果底層資料庫操作失敗。
     * @throws IllegalArgumentException 如果產品ID無效。
     */
    List<ProductionSchedule> getProductionSchedulesByProductId(int productId) throws SQLException, IllegalArgumentException;

    /**
     * 根據計劃生產日期獲取生產排程明細列表。
     * @param scheduledDate 要篩選的計劃生產日期。
     * @return 匹配指定日期的 ProductionSchedule 物件列表。
     * @throws SQLException 如果底層資料庫操作失敗。
     * @throws IllegalArgumentException 如果日期參數無效。
     */
    List<ProductionSchedule> getProductionSchedulesByDate(Date scheduledDate) throws SQLException, IllegalArgumentException;

    /**
     * 更新生產排程明細資訊。
     * @param schedule 包含更新資訊的排程物件 (必須包含有效的排程ID)。
     * @return 如果更新成功返回 true，否則返回 false。
     * @throws SQLException 如果底層資料庫操作失敗。
     * @throws IllegalArgumentException 如果排程數據無效或排程ID不存在。
     */
    boolean updateProductionSchedule(ProductionSchedule schedule) throws SQLException, IllegalArgumentException;

    /**
     * 刪除指定ID的生產排程明細。
     * @param scheduleId 要刪除排程的ID。
     * @return 如果刪除成功返回 true，否則返回 false。
     * @throws SQLException 如果底層資料庫操作失敗。
     * @throws IllegalArgumentException 如果排程ID無效。
     */
    boolean deleteProductionSchedule(int scheduleId) throws SQLException, IllegalArgumentException;

    /**
     * 更新排程的實際完成數量並將狀態設置為 "Completed"。
     * @param scheduleId 要更新的排程ID。
     * @param actualQuantity 實際生產數量。
     * @return 如果更新成功返回 true，否則返回 false。
     * @throws SQLException 如果資料庫操作失敗。
     * @throws IllegalArgumentException 如果排程ID或數量無效。
     * @throws IllegalStateException 如果排程狀態不允許完成。
     */
    boolean completeProductionSchedule(int scheduleId, BigDecimal actualQuantity) throws SQLException, IllegalArgumentException, IllegalStateException;

    /**
     * 將排程狀態設置為 "In Production"。
     * @param scheduleId 要更新的排程ID。
     * @return 如果更新成功返回 true，否則返回 false。
     * @throws SQLException 如果資料庫操作失敗。
     * @throws IllegalArgumentException 如果排程ID無效。
     * @throws IllegalStateException 如果排程狀態不允許開始生產。
     */
    boolean startProduction(int scheduleId) throws SQLException, IllegalArgumentException, IllegalStateException;
}
