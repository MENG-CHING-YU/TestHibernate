package com.example.model;

import java.math.BigDecimal;
import java.util.Date; // 使用 java.util.Date
import java.time.LocalDateTime; // 用於創建和更新時間戳

/**
 * ProductionSchedule Model 代表一個生產排程明細。
 */
public class ProductionSchedule {
    private int scheduleId;            // 排程明細主鍵ID
    private Integer workOrderId;       // 關聯的工單ID (可選，Integer 允許為 null)
    private String workOrderNumber;    // <-- 新增此行：工單編號 (用於顯示，可通過 JOIN 獲取)
    private int productId;             // 排程生產的產品ID
    private String productCode;        // 產品代碼 (用於顯示，可通過 JOIN 獲取)
    private String productName;        // 產品名稱 (用於顯示，可通過 JOIN 獲取)
    private Date scheduledDate;        // 計劃生產日期
    private String shift;              // 班次
    private BigDecimal plannedQuantity; // 計劃生產數量
    private BigDecimal actualQuantity;  // 實際生產數量 (可選)
    private String status;             // 排程狀態
    private String notes;              // 備註
    private LocalDateTime createDate;  // 創建時間
    private LocalDateTime updateDate;  // 最後更新時間

    // 預設構造函數
    public ProductionSchedule() {
    }

    // 帶所有參數的構造函數
    public ProductionSchedule(int scheduleId, Integer workOrderId, String workOrderNumber, int productId, String productCode, String productName, Date scheduledDate, String shift, BigDecimal plannedQuantity, BigDecimal actualQuantity, String status, String notes, LocalDateTime createDate, LocalDateTime updateDate) {
        this.scheduleId = scheduleId;
        this.workOrderId = workOrderId;
        this.workOrderNumber = workOrderNumber; // <-- 修正構造函數
        this.productId = productId;
        this.productCode = productCode;
        this.productName = productName;
        this.scheduledDate = scheduledDate;
        this.shift = shift;
        this.plannedQuantity = plannedQuantity;
        this.actualQuantity = actualQuantity;
        this.status = status;
        this.notes = notes;
        this.createDate = createDate;
        this.updateDate = updateDate;
    }

    // 帶必需參數的構造函數 (用於新增) - 這裡不需要 workOrderNumber，因為它不是直接輸入的
    public ProductionSchedule(Integer workOrderId, int productId, Date scheduledDate, String shift, BigDecimal plannedQuantity, String status, String notes) {
        this.workOrderId = workOrderId;
        this.productId = productId;
        this.scheduledDate = scheduledDate;
        this.shift = shift;
        this.plannedQuantity = plannedQuantity;
        this.status = status;
        this.notes = notes;
    }

    // --- Getters and Setters ---

    public int getScheduleId() {
        return scheduleId;
    }

    public void setScheduleId(int scheduleId) {
        this.scheduleId = scheduleId;
    }

    public Integer getWorkOrderId() {
        return workOrderId;
    }

    public void setWorkOrderId(Integer workOrderId) {
        this.workOrderId = workOrderId;
    }

    public String getWorkOrderNumber() { // <-- 新增此方法
        return workOrderNumber;
    }

    public void setWorkOrderNumber(String workOrderNumber) { // <-- 新增此方法
        this.workOrderNumber = workOrderNumber;
    }

    public int getProductId() {
        return productId;
    }

    public void setProductId(int productId) {
        this.productId = productId;
    }

    public String getProductCode() {
        return productCode;
    }

    public void setProductCode(String productCode) {
        this.productCode = productCode;
    }

    public String getProductName() {
        return productName;
    }

    public void setProductName(String productName) {
        this.productName = productName;
    }

    public Date getScheduledDate() {
        return scheduledDate;
    }

    public void setScheduledDate(Date scheduledDate) {
        this.scheduledDate = scheduledDate;
    }

    public String getShift() {
        return shift;
    }

    public void setShift(String shift) {
        this.shift = shift;
    }

    public BigDecimal getPlannedQuantity() {
        return plannedQuantity;
    }

    public void setPlannedQuantity(BigDecimal plannedQuantity) {
        this.plannedQuantity = plannedQuantity;
    }

    public BigDecimal getActualQuantity() {
        return actualQuantity;
    }

    public void setActualQuantity(BigDecimal actualQuantity) {
        this.actualQuantity = actualQuantity;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getNotes() {
        return notes;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }

    public LocalDateTime getCreateDate() {
        return createDate;
    }

    public void setCreateDate(LocalDateTime createDate) {
        this.createDate = createDate;
    }

    public LocalDateTime getUpdateDate() {
        return updateDate;
    }

    public void setUpdateDate(LocalDateTime updateDate) {
        this.updateDate = updateDate;
    }

    @Override
    public String toString() {
        return "ProductionSchedule{" +
                "scheduleId=" + scheduleId +
                ", workOrderId=" + workOrderId +
                ", workOrderNumber='" + workOrderNumber + '\'' + // <-- 修正 toString
                ", productId=" + productId +
                ", productCode='" + productCode + '\'' +
                ", productName='" + productName + '\'' +
                ", scheduledDate=" + scheduledDate +
                ", shift='" + shift + '\'' +
                ", plannedQuantity=" + plannedQuantity +
                ", actualQuantity=" + actualQuantity +
                ", status='" + status + '\'' +
                ", notes='" + notes + '\'' +
                ", createDate=" + createDate +
                ", updateDate=" + updateDate +
                '}';
    }
}
