package com.example.service.impl;

import com.example.dao.ProductDao; // 用於驗證產品是否存在
import com.example.dao.WorkOrderDao; // 用於驗證工單是否存在
import com.example.dao.ProductionScheduleDao;
import com.example.dao.impl.ProductDaoImpl;
import com.example.dao.impl.WorkOrderDaoImpl;
import com.example.dao.impl.ProductionScheduleDaoImpl;
import com.example.model.Product;
import com.example.model.WorkOrder;
import com.example.model.ProductionSchedule;
import com.example.service.ProductionScheduleService;
import java.math.BigDecimal;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Date; // 使用 java.util.Date
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * ProductionScheduleService 介面的實現類。
 * 負責生產排程明細相關的業務邏輯。
 */
public class ProductionScheduleServiceImpl implements ProductionScheduleService {

    private static final Logger LOGGER = Logger.getLogger(ProductionScheduleServiceImpl.class.getName());
    private ProductionScheduleDao productionScheduleDao;
    private ProductDao productDao;
    private WorkOrderDao workOrderDao;

    // 定義有效的排程狀態
    private static final List<String> VALID_SCHEDULE_STATUSES = Arrays.asList("Scheduled", "In Production", "Completed", "Delayed", "Cancelled");

    public ProductionScheduleServiceImpl() {
        this.productionScheduleDao = new ProductionScheduleDaoImpl();
        this.productDao = new ProductDaoImpl();
        this.workOrderDao = new WorkOrderDaoImpl();
        LOGGER.info("ProductionScheduleServiceImpl 已初始化。");
    }

    /**
     * 允許通過構造函數注入 DAO 實例，方便測試。
     * @param productionScheduleDao ProductionScheduleDao 實例。
     * @param productDao ProductDao 實例。
     * @param workOrderDao WorkOrderDao 實例。
     */
    public ProductionScheduleServiceImpl(ProductionScheduleDao productionScheduleDao, ProductDao productDao, WorkOrderDao workOrderDao) {
        this.productionScheduleDao = productionScheduleDao;
        this.productDao = productDao;
        this.workOrderDao = workOrderDao;
        LOGGER.info("ProductionScheduleServiceImpl 已初始化，並通過構造函數注入 DAO。");
    }

    @Override
    public int addProductionSchedule(ProductionSchedule schedule) throws SQLException, IllegalArgumentException {
        // 數據驗證
        if (schedule == null || schedule.getProductId() <= 0 || schedule.getScheduledDate() == null ||
            schedule.getShift() == null || schedule.getShift().trim().isEmpty() ||
            schedule.getPlannedQuantity() == null || schedule.getPlannedQuantity().compareTo(BigDecimal.ZERO) <= 0 ||
            schedule.getStatus() == null || schedule.getStatus().trim().isEmpty()) {
            LOGGER.log(Level.WARNING, "新增生產排程時數據無效: 必填字段缺失或無效。");
            throw new IllegalArgumentException("產品、排程日期、班次、計劃數量和狀態為必填項，且計劃數量必須大於零。");
        }

        if (!VALID_SCHEDULE_STATUSES.contains(schedule.getStatus())) {
            LOGGER.log(Level.WARNING, "新增生產排程時狀態無效: {0}", schedule.getStatus());
            throw new IllegalArgumentException("無效的排程狀態: " + schedule.getStatus());
        }

        // 驗證產品是否存在
        Product product = productDao.getProductById(schedule.getProductId());
        if (product == null) {
            LOGGER.log(Level.WARNING, "新增生產排程失敗: 產品ID {0} 不存在。", schedule.getProductId());
            throw new IllegalArgumentException("指定的產品不存在。");
        }
        // 如果傳入了產品名稱或代碼，設定到排程物件中 (非持久化欄位)
        schedule.setProductName(product.getProductName());
        schedule.setProductCode(product.getProductCode());

        // 如果有關聯工單，驗證工單是否存在
        if (schedule.getWorkOrderId() != null && schedule.getWorkOrderId() > 0) {
            WorkOrder workOrder = workOrderDao.getWorkOrderById(schedule.getWorkOrderId());
            if (workOrder == null) {
                LOGGER.log(Level.WARNING, "新增生產排程失敗: 工單ID {0} 不存在。", schedule.getWorkOrderId());
                throw new IllegalArgumentException("指定的工單不存在。");
            }
            schedule.setWorkOrderNumber(workOrder.getWorkOrderNumber()); // 設置工單編號
        } else {
            // 如果 workOrderId 為 null 或 <= 0，確保物件中也是 null
            schedule.setWorkOrderId(null);
            schedule.setWorkOrderNumber(null);
        }

        LOGGER.log(Level.INFO, "Service: 嘗試新增生產排程，產品ID: {0}，日期: {1}", new Object[]{schedule.getProductId(), schedule.getScheduledDate()});
        return productionScheduleDao.addProductionSchedule(schedule);
    }

    @Override
    public ProductionSchedule getProductionScheduleById(int scheduleId) throws SQLException, IllegalArgumentException {
        if (scheduleId <= 0) {
            LOGGER.log(Level.WARNING, "獲取生產排程時ID無效: {0}", scheduleId);
            throw new IllegalArgumentException("排程ID無效，必須大於0。");
        }
        LOGGER.log(Level.INFO, "Service: 嘗試獲取生產排程，ID: {0}", scheduleId);
        return productionScheduleDao.getProductionScheduleById(scheduleId);
    }

    @Override
    public List<ProductionSchedule> getAllProductionSchedules() throws SQLException {
        LOGGER.info("Service: 嘗試獲取所有生產排程。");
        return productionScheduleDao.getAllProductionSchedules();
    }

    @Override
    public List<ProductionSchedule> getProductionSchedulesByStatus(String status) throws SQLException, IllegalArgumentException {
        if (status == null || status.trim().isEmpty() || !VALID_SCHEDULE_STATUSES.contains(status)) {
            LOGGER.log(Level.WARNING, "獲取生產排程時狀態無效或為空: {0}", status);
            throw new IllegalArgumentException("無效或為空的排程狀態。");
        }
        LOGGER.log(Level.INFO, "Service: 嘗試獲取狀態為 '{0}' 的生產排程。", status);
        return productionScheduleDao.getProductionSchedulesByStatus(status);
    }

    @Override
    public List<ProductionSchedule> getProductionSchedulesByProductId(int productId) throws SQLException, IllegalArgumentException {
        if (productId <= 0) {
            LOGGER.log(Level.WARNING, "獲取生產排程時產品ID無效: {0}", productId);
            throw new IllegalArgumentException("產品ID無效，必須大於0。");
        }
        // 驗證產品是否存在
        Product product = productDao.getProductById(productId);
        if (product == null) {
            LOGGER.log(Level.WARNING, "獲取生產排程失敗: 產品ID {0} 不存在。", productId);
            throw new IllegalArgumentException("指定的產品不存在。");
        }
        LOGGER.log(Level.INFO, "Service: 嘗試獲取產品ID {0} 的所有生產排程。", productId);
        return productionScheduleDao.getProductionSchedulesByProductId(productId);
    }

    @Override
    public List<ProductionSchedule> getProductionSchedulesByDate(Date scheduledDate) throws SQLException, IllegalArgumentException {
        if (scheduledDate == null) {
            LOGGER.log(Level.WARNING, "獲取生產排程時日期無效: 日期不能為空。");
            throw new IllegalArgumentException("排程日期不能為空。");
        }
        LOGGER.log(Level.INFO, "Service: 嘗試獲取日期為 '{0}' 的所有生產排程。", scheduledDate);
        return productionScheduleDao.getProductionSchedulesByDate(scheduledDate);
    }

    @Override
    public boolean updateProductionSchedule(ProductionSchedule schedule) throws SQLException, IllegalArgumentException {
        // 數據驗證
        if (schedule == null || schedule.getScheduleId() <= 0 || schedule.getProductId() <= 0 || schedule.getScheduledDate() == null ||
            schedule.getShift() == null || schedule.getShift().trim().isEmpty() ||
            schedule.getPlannedQuantity() == null || schedule.getPlannedQuantity().compareTo(BigDecimal.ZERO) <= 0 ||
            schedule.getStatus() == null || schedule.getStatus().trim().isEmpty()) {
            LOGGER.log(Level.WARNING, "更新生產排程時數據無效: 必填字段缺失或無效。排程ID: {0}", schedule.getScheduleId());
            throw new IllegalArgumentException("排程ID、產品、排程日期、班次、計劃數量和狀態為必填項，且計劃數量必須大於零。");
        }

        if (!VALID_SCHEDULE_STATUSES.contains(schedule.getStatus())) {
            LOGGER.log(Level.WARNING, "更新生產排程時狀態無效: {0}", schedule.getStatus());
            throw new IllegalArgumentException("無效的排程狀態: " + schedule.getStatus());
        }

        // 確保要更新的排程存在
        ProductionSchedule existingSchedule = productionScheduleDao.getProductionScheduleById(schedule.getScheduleId());
        if (existingSchedule == null) {
            LOGGER.log(Level.WARNING, "更新生產排程失敗，排程ID不存在: {0}", schedule.getScheduleId());
            throw new IllegalArgumentException("要更新的生產排程不存在。");
        }

        // 驗證產品是否存在
        Product product = productDao.getProductById(schedule.getProductId());
        if (product == null) {
            LOGGER.log(Level.WARNING, "更新生產排程失敗: 產品ID {0} 不存在。", schedule.getProductId());
            throw new IllegalArgumentException("指定的產品不存在。");
        }
        // 如果傳入了產品名稱或代碼，設定到排程物件中 (非持久化欄位)
        schedule.setProductName(product.getProductName());
        schedule.setProductCode(product.getProductCode());

        // 如果有關聯工單，驗證工單是否存在
        if (schedule.getWorkOrderId() != null && schedule.getWorkOrderId() > 0) {
            WorkOrder workOrder = workOrderDao.getWorkOrderById(schedule.getWorkOrderId());
            if (workOrder == null) {
                LOGGER.log(Level.WARNING, "更新生產排程失敗: 工單ID {0} 不存在。", schedule.getWorkOrderId());
                throw new IllegalArgumentException("指定的工單不存在。");
            }
            schedule.setWorkOrderNumber(workOrder.getWorkOrderNumber()); // 設置工單編號
        } else {
            schedule.setWorkOrderId(null);
            schedule.setWorkOrderNumber(null);
        }

        LOGGER.log(Level.INFO, "Service: 嘗試更新生產排程，ID: {0}", schedule.getScheduleId());
        return productionScheduleDao.updateProductionSchedule(schedule);
    }

    @Override
    public boolean deleteProductionSchedule(int scheduleId) throws SQLException, IllegalArgumentException {
        if (scheduleId <= 0) {
            LOGGER.log(Level.WARNING, "刪除生產排程時ID無效: {0}", scheduleId);
            throw new IllegalArgumentException("排程ID無效，必須大於0。");
        }
        // 檢查排程是否存在
        ProductionSchedule existingSchedule = productionScheduleDao.getProductionScheduleById(scheduleId);
        if (existingSchedule == null) {
            LOGGER.log(Level.WARNING, "刪除生產排程失敗，排程ID不存在: {0}", scheduleId);
            return false; // 如果排程不存在，則認為刪除成功 (冪等性)
        }

        LOGGER.log(Level.INFO, "Service: 嘗試刪除生產排程，ID: {0}", scheduleId);
        return productionScheduleDao.deleteProductionSchedule(scheduleId);
    }

    @Override
    public boolean completeProductionSchedule(int scheduleId, BigDecimal actualQuantity) throws SQLException, IllegalArgumentException, IllegalStateException {
        if (scheduleId <= 0) {
            throw new IllegalArgumentException("排程ID無效，必須大於0。");
        }
        if (actualQuantity == null || actualQuantity.compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException("實際數量無效，不能為空或小於零。");
        }

        ProductionSchedule schedule = productionScheduleDao.getProductionScheduleById(scheduleId);
        if (schedule == null) {
            throw new IllegalArgumentException("要完成的生產排程不存在。");
        }

        // 業務規則：只有 'In Production' 或 'Delayed' 的排程才能完成
        if (!"In Production".equalsIgnoreCase(schedule.getStatus()) && !"Delayed".equalsIgnoreCase(schedule.getStatus())) {
            throw new IllegalStateException("只有 '生產中' 或 '延遲' 狀態的排程才能完成。當前狀態: " + schedule.getStatus());
        }

        // 確保實際數量不小於計劃數量 (可選業務規則，如果實際允許生產更多)
        if (actualQuantity.compareTo(schedule.getPlannedQuantity()) < 0) {
            LOGGER.log(Level.WARNING, "排程ID {0}: 實際生產數量 {1} 小於計劃數量 {2}。這可能是一個警告，但也允許完成。", new Object[]{scheduleId, actualQuantity, schedule.getPlannedQuantity()});
            // 根據業務需求，這裡可以拋出異常，或者僅記錄警告並允許完成
        }

        schedule.setActualQuantity(actualQuantity);
        schedule.setStatus("Completed");
        schedule.setUpdateDate(LocalDateTime.now());

        LOGGER.log(Level.INFO, "Service: 嘗試完成生產排程，ID: {0}，實際數量: {1}", new Object[]{scheduleId, actualQuantity});
        return productionScheduleDao.updateProductionSchedule(schedule);
    }

    @Override
    public boolean startProduction(int scheduleId) throws SQLException, IllegalArgumentException, IllegalStateException {
        if (scheduleId <= 0) {
            throw new IllegalArgumentException("排程ID無效，必須大於0。");
        }

        ProductionSchedule schedule = productionScheduleDao.getProductionScheduleById(scheduleId);
        if (schedule == null) {
            throw new IllegalArgumentException("要開始生產的排程不存在。");
        }

        // 業務規則：只有 'Scheduled' 或 'Delayed' 的排程才能開始生產
        if (!"Scheduled".equalsIgnoreCase(schedule.getStatus()) && !"Delayed".equalsIgnoreCase(schedule.getStatus())) {
            throw new IllegalStateException("只有 '已排程' 或 '延遲' 狀態的排程才能開始生產。當前狀態: " + schedule.getStatus());
        }

        schedule.setStatus("In Production");
        schedule.setUpdateDate(LocalDateTime.now());

        LOGGER.log(Level.INFO, "Service: 嘗試開始生產排程，ID: {0}", scheduleId);
        return productionScheduleDao.updateProductionSchedule(schedule);
    }
}
