package com.example.service.impl;

import com.example.dao.ProductDao; // 需要 ProductDao 來驗證產品是否存在
import com.example.dao.WorkOrderDao;
import com.example.dao.impl.ProductDaoImpl; // 引入 ProductDao 實現
import com.example.dao.impl.WorkOrderDaoImpl;
import com.example.model.Product;
import com.example.model.WorkOrder;
import com.example.service.WorkOrderService;
import java.math.BigDecimal;
import java.sql.SQLException;
// import java.time.LocalDate; // <-- 移除此行
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Date; // <-- 新增此行，使用 java.util.Date
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * WorkOrderService 介面的實現類。
 * 負責生產工單相關的業務邏輯，並調用 WorkOrderDao 進行數據庫操作。
 */
public class WorkOrderServiceImpl implements WorkOrderService {

    private static final Logger LOGGER = Logger.getLogger(WorkOrderServiceImpl.class.getName());
    private WorkOrderDao workOrderDao;
    private ProductDao productDao; // 用於驗證產品是否存在

    // 定義有效的工單狀態
    private static final List<String> VALID_STATUSES = Arrays.asList("Pending", "In Progress", "Completed", "Cancelled");

    public WorkOrderServiceImpl() {
        this.workOrderDao = new WorkOrderDaoImpl();
        this.productDao = new ProductDaoImpl();
        LOGGER.info("WorkOrderServiceImpl 已初始化。");
    }

    /**
     * 為了測試或特定場景，允許透過構造函數注入 DAO 實例。
     * @param workOrderDao WorkOrderDao 實例。
     * @param productDao ProductDao 實例。
     */
    public WorkOrderServiceImpl(WorkOrderDao workOrderDao, ProductDao productDao) {
        this.workOrderDao = workOrderDao;
        this.productDao = productDao;
        LOGGER.info("WorkOrderServiceImpl 已初始化，並通過構造函數注入 DAO。");
    }

    @Override
    public int addWorkOrder(WorkOrder workOrder) throws SQLException, IllegalArgumentException {
        // 業務邏輯：數據驗證
        if (workOrder == null || workOrder.getWorkOrderNumber() == null || workOrder.getWorkOrderNumber().trim().isEmpty() ||
            workOrder.getProductId() <= 0 || workOrder.getQuantity() == null ||
            workOrder.getQuantity().compareTo(BigDecimal.ZERO) <= 0 ||
            workOrder.getUnit() == null || workOrder.getUnit().trim().isEmpty() ||
            workOrder.getScheduledStartDate() == null || workOrder.getScheduledDueDate() == null ||
            workOrder.getStatus() == null || workOrder.getStatus().trim().isEmpty()) {
            LOGGER.log(Level.WARNING, "新增工單時數據無效: 必填字段缺失或無效。");
            throw new IllegalArgumentException("工單編號、產品、數量、單位、預計開始日期、預計完成日期和狀態為必填項，且數量必須大於零。");
        }

        if (!VALID_STATUSES.contains(workOrder.getStatus())) {
            LOGGER.log(Level.WARNING, "新增工單時狀態無效: {0}", workOrder.getStatus());
            throw new IllegalArgumentException("無效的工單狀態: " + workOrder.getStatus());
        }

        // 修正日期比較：使用 before/after 方法
        if (workOrder.getScheduledStartDate().after(workOrder.getScheduledDueDate())) {
            LOGGER.log(Level.WARNING, "新增工單時預計日期無效: 預計開始日期晚於預計完成日期。");
            throw new IllegalArgumentException("預計開始日期不能晚於預計完成日期。");
        }

        // 驗證產品是否存在
        Product product = productDao.getProductById(workOrder.getProductId());
        if (product == null) {
            LOGGER.log(Level.WARNING, "新增工單失敗: 產品ID {0} 不存在。", workOrder.getProductId());
            throw new IllegalArgumentException("指定的產品不存在。");
        }

        // 在這裡設置產品的名稱和單位，以便在工單物件中可以直接使用，即使它們不直接持久化到 work_orders 表
        workOrder.setProductName(product.getProductName());
        workOrder.setProductCode(product.getProductCode());
        // 工單的單位應來自於產品的單位，確保一致性
        workOrder.setUnit(product.getUnit());


        LOGGER.log(Level.INFO, "Service: 嘗試新增工單: {0}", workOrder.getWorkOrderNumber());
        return workOrderDao.addWorkOrder(workOrder);
    }

    @Override
    public WorkOrder getWorkOrderById(int workOrderId) throws SQLException {
        if (workOrderId <= 0) {
            LOGGER.log(Level.WARNING, "獲取工單時ID無效: {0}", workOrderId);
            throw new IllegalArgumentException("工單ID無效，必須大於0。");
        }
        LOGGER.log(Level.INFO, "Service: 嘗試獲取工單，ID: {0}", workOrderId);
        return workOrderDao.getWorkOrderById(workOrderId);
    }

    @Override
    public List<WorkOrder> getAllWorkOrders() throws SQLException {
        LOGGER.info("Service: 嘗試獲取所有工單。");
        return workOrderDao.getAllWorkOrders();
    }

    @Override
    public List<WorkOrder> getWorkOrdersByStatus(String status) throws SQLException, IllegalArgumentException {
        if (status == null || status.trim().isEmpty() || !VALID_STATUSES.contains(status)) {
            LOGGER.log(Level.WARNING, "獲取工單時狀態無效或為空: {0}", status);
            throw new IllegalArgumentException("無效或為空的工單狀態。");
        }
        LOGGER.log(Level.INFO, "Service: 嘗試獲取狀態為 '{0}' 的工單。", status);
        return workOrderDao.getWorkOrdersByStatus(status);
    }

    @Override
    public List<WorkOrder> getWorkOrdersByProductId(int productId) throws SQLException, IllegalArgumentException {
        if (productId <= 0) {
            LOGGER.log(Level.WARNING, "獲取工單時產品ID無效: {0}", productId);
            throw new IllegalArgumentException("產品ID無效，必須大於0。");
        }
        // 可以選擇在這裡驗證產品是否存在，但通常 DAO 層的 JOIN 會處理這個。
        LOGGER.log(Level.INFO, "Service: 嘗試獲取產品ID {0} 的所有工單。", productId);
        return workOrderDao.getWorkOrdersByProductId(productId);
    }

    @Override
    public boolean updateWorkOrder(WorkOrder workOrder) throws SQLException, IllegalArgumentException {
        // 業務邏輯：數據驗證
        if (workOrder == null || workOrder.getWorkOrderId() <= 0 ||
            workOrder.getWorkOrderNumber() == null || workOrder.getWorkOrderNumber().trim().isEmpty() ||
            workOrder.getProductId() <= 0 || workOrder.getQuantity() == null ||
            workOrder.getQuantity().compareTo(BigDecimal.ZERO) <= 0 ||
            workOrder.getUnit() == null || workOrder.getUnit().trim().isEmpty() ||
            workOrder.getScheduledStartDate() == null || workOrder.getScheduledDueDate() == null ||
            workOrder.getStatus() == null || workOrder.getStatus().trim().isEmpty()) {
            LOGGER.log(Level.WARNING, "更新工單時數據無效: 必填字段缺失或無效。工單ID: {0}", workOrder.getWorkOrderId());
            throw new IllegalArgumentException("工單ID、工單編號、產品、數量、單位、預計開始日期、預計完成日期和狀態為必填項，且數量必須大於零。");
        }
        if (!VALID_STATUSES.contains(workOrder.getStatus())) {
            LOGGER.log(Level.WARNING, "更新工單時狀態無效: {0}", workOrder.getStatus());
            throw new IllegalArgumentException("無效的工單狀態: " + workOrder.getStatus());
        }
        // 修正日期比較：使用 before/after 方法
        if (workOrder.getScheduledStartDate().after(workOrder.getScheduledDueDate())) {
            LOGGER.log(Level.WARNING, "更新工單時預計日期無效: 預計開始日期晚於預計完成日期。");
            throw new IllegalArgumentException("預計開始日期不能晚於預計完成日期。");
        }

        // 確保要更新的工單存在
        WorkOrder existingWorkOrder = workOrderDao.getWorkOrderById(workOrder.getWorkOrderId());
        if (existingWorkOrder == null) {
            LOGGER.log(Level.WARNING, "更新工單失敗，工單ID不存在: {0}", workOrder.getWorkOrderId());
            throw new IllegalArgumentException("要更新的工單不存在。");
        }

        // 驗證產品是否存在 (如果用戶更改了產品ID)
        Product product = productDao.getProductById(workOrder.getProductId());
        if (product == null) {
            LOGGER.log(Level.WARNING, "更新工單失敗: 產品ID {0} 不存在。", workOrder.getProductId());
            throw new IllegalArgumentException("指定的產品不存在。");
        }
        // 更新單位為選定產品的單位
        workOrder.setUnit(product.getUnit());


        LOGGER.log(Level.INFO, "Service: 嘗試更新工單，ID: {0}", workOrder.getWorkOrderId());
        return workOrderDao.updateWorkOrder(workOrder);
    }

    @Override
    public boolean deleteWorkOrder(int workOrderId) throws SQLException, IllegalArgumentException {
        if (workOrderId <= 0) {
            LOGGER.log(Level.WARNING, "刪除工單時ID無效: {0}", workOrderId);
            throw new IllegalArgumentException("工單ID無效，必須大於0。");
        }
        // 檢查工單是否存在
        WorkOrder existingWorkOrder = workOrderDao.getWorkOrderById(workOrderId);
        if (existingWorkOrder == null) {
            LOGGER.log(Level.WARNING, "刪除工單失敗，工單ID不存在: {0}", workOrderId);
            return false; // 如果工單不存在，則認為刪除成功 (冪等性)
        }

        LOGGER.log(Level.INFO, "Service: 嘗試刪除工單，ID: {0}", workOrderId);
        return workOrderDao.deleteWorkOrder(workOrderId);
    }

    @Override
    public boolean startWorkOrder(int workOrderId, Date actualStartDate) throws SQLException, IllegalArgumentException, IllegalStateException { // <-- 參數類型已修改
        if (workOrderId <= 0) {
            throw new IllegalArgumentException("工單ID無效，必須大於0。");
        }
        if (actualStartDate == null) {
            throw new IllegalArgumentException("實際開始日期不能為空。");
        }

        WorkOrder workOrder = workOrderDao.getWorkOrderById(workOrderId);
        if (workOrder == null) {
            throw new IllegalArgumentException("要開始的工單不存在。");
        }

        // 業務規則：只有 Pending 狀態的工單才能開始
        if (!"Pending".equalsIgnoreCase(workOrder.getStatus())) {
            throw new IllegalStateException("只有 '待處理' 狀態的工單才能開始。當前狀態: " + workOrder.getStatus());
        }

        // 修正日期比較：使用 before 方法
        if (actualStartDate.before(workOrder.getScheduledStartDate())) {
             LOGGER.log(Level.WARNING, "工單ID {0} 實際開始日期 {1} 早於計劃開始日期 {2}", new Object[]{workOrderId, actualStartDate, workOrder.getScheduledStartDate()});
             // 這裡可以選擇拋出 IllegalArgumentException 或僅記錄警告
             // throw new IllegalArgumentException("實際開始日期不能早於計劃開始日期。");
        }


        workOrder.setActualStartDate(actualStartDate);
        workOrder.setStatus("In Progress");
        workOrder.setUpdateDate(LocalDateTime.now()); // 更新時間

        LOGGER.log(Level.INFO, "Service: 嘗試開始工單，ID: {0}", workOrderId);
        return workOrderDao.updateWorkOrder(workOrder);
    }

    @Override
    public boolean completeWorkOrder(int workOrderId, Date actualCompletionDate) throws SQLException, IllegalArgumentException, IllegalStateException { // <-- 參數類型已修改
        if (workOrderId <= 0) {
            throw new IllegalArgumentException("工單ID無效，必須大於0。");
        }
        if (actualCompletionDate == null) {
            throw new IllegalArgumentException("實際完成日期不能為空。");
        }

        WorkOrder workOrder = workOrderDao.getWorkOrderById(workOrderId);
        if (workOrder == null) {
            throw new IllegalArgumentException("要完成的工單不存在。");
        }

        // 業務規則：只有 In Progress 狀態的工單才能完成
        if (!"In Progress".equalsIgnoreCase(workOrder.getStatus())) {
            throw new IllegalStateException("只有 '進行中' 狀態的工單才能完成。當前狀態: " + workOrder.getStatus());
        }

        // 確保實際完成日期不早於實際開始日期 (如果實際開始日期存在)
        if (workOrder.getActualStartDate() != null && actualCompletionDate.before(workOrder.getActualStartDate())) {
            LOGGER.log(Level.WARNING, "工單ID {0} 實際完成日期 {1} 早於實際開始日期 {2}", new Object[]{workOrderId, actualCompletionDate, workOrder.getActualStartDate()});
            throw new IllegalArgumentException("實際完成日期不能早於實際開始日期。");
        }

        workOrder.setActualCompletionDate(actualCompletionDate);
        workOrder.setStatus("Completed");
        workOrder.setUpdateDate(LocalDateTime.now()); // 更新時間

        LOGGER.log(Level.INFO, "Service: 嘗試完成工單，ID: {0}", workOrderId);
        return workOrderDao.updateWorkOrder(workOrder);
    }
}
