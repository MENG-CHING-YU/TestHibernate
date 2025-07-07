package com.example.model;

import java.math.BigDecimal;
import java.util.Date; // 改用 java.util.Date
import java.time.LocalDateTime; // 用於創建和更新時間戳，因為資料庫時間戳通常是精確到秒

/**
 * WorkOrder Model 代表一個生產工單。
 */
public class WorkOrder {
    private int workOrderId;            // 工單主鍵ID
    private String workOrderNumber;     // 工單編號 (唯一，用戶可讀)
    private int productId;              // 對應的產品ID (FK)
    private String productCode;         // 冗餘存儲或通過 JOIN 獲取，方便顯示
    private String productName;         // 冗餘存儲或通過 JOIN 獲取，方便顯示
    private BigDecimal quantity;        // 計劃生產的數量
    private String unit;                // 產品單位，冗餘存儲或通過 JOIN 獲取
    private Date scheduledStartDate;    // 計劃開始日期 (改為 java.util.Date)
    private Date scheduledDueDate;      // 計劃預計完成日期 (改為 java.util.Date)
    private Date actualStartDate;       // 實際開始日期 (可選) (改為 java.util.Date)
    private Date actualCompletionDate;  // 實際完成日期 (可選) (改為 java.util.Date)
    private String status;              // 工單狀態 (例如: Pending, In Progress, Completed, Cancelled)
    private String notes;               // 備註
    private LocalDateTime createDate;   // 創建時間
    private LocalDateTime updateDate;   // 最後更新時間

    // 預設構造函數
    public WorkOrder() {
    }

    // 帶所有參數的構造函數 (通常用於從資料庫讀取)
    public WorkOrder(int workOrderId, String workOrderNumber, int productId, String productCode, String productName, BigDecimal quantity, String unit, Date scheduledStartDate, Date scheduledDueDate, Date actualStartDate, Date actualCompletionDate, String status, String notes, LocalDateTime createDate, LocalDateTime updateDate) {
        this.workOrderId = workOrderId;
        this.workOrderNumber = workOrderNumber;
        this.productId = productId;
        this.productCode = productCode;
        this.productName = productName;
        this.quantity = quantity;
        this.unit = unit;
        this.scheduledStartDate = scheduledStartDate;
        this.scheduledDueDate = scheduledDueDate;
        this.actualStartDate = actualStartDate;
        this.actualCompletionDate = actualCompletionDate;
        this.status = status;
        this.notes = notes;
        this.createDate = createDate;
        this.updateDate = updateDate;
    }

    // 帶必需參數的構造函數 (通常用於新增時)
    public WorkOrder(String workOrderNumber, int productId, BigDecimal quantity, String unit, Date scheduledStartDate, Date scheduledDueDate, String status, String notes) {
        this.workOrderNumber = workOrderNumber;
        this.productId = productId;
        this.quantity = quantity;
        this.unit = unit;
        this.scheduledStartDate = scheduledStartDate;
        this.scheduledDueDate = scheduledDueDate;
        this.status = status;
        this.notes = notes;
    }

    // --- Getters and Setters ---

    public int getWorkOrderId() {
        return workOrderId;
    }

    public void setWorkOrderId(int workOrderId) {
        this.workOrderId = workOrderId;
    }

    public String getWorkOrderNumber() {
        return workOrderNumber;
    }

    public void setWorkOrderNumber(String workOrderNumber) {
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

    public BigDecimal getQuantity() {
        return quantity;
    }

    public void setQuantity(BigDecimal quantity) {
        this.quantity = quantity;
    }

    public String getUnit() {
        return unit;
    }

    public void setUnit(String unit) {
        this.unit = unit;
    }

    public Date getScheduledStartDate() { // 返回 Date
        return scheduledStartDate;
    }

    public void setScheduledStartDate(Date scheduledStartDate) { // 接受 Date
        this.scheduledStartDate = scheduledStartDate;
    }

    public Date getScheduledDueDate() { // 返回 Date
        return scheduledDueDate;
    }

    public void setScheduledDueDate(Date scheduledDueDate) { // 接受 Date
        this.scheduledDueDate = scheduledDueDate;
    }

    public Date getActualStartDate() { // 返回 Date
        return actualStartDate;
    }

    public void setActualStartDate(Date actualStartDate) { // 接受 Date
        this.actualStartDate = actualStartDate;
    }

    public Date getActualCompletionDate() { // 返回 Date
        return actualCompletionDate;
    }

    public void setActualCompletionDate(Date actualCompletionDate) { // 接受 Date
        this.actualCompletionDate = actualCompletionDate;
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
        return "WorkOrder{" +
                "workOrderId=" + workOrderId +
                ", workOrderNumber='" + workOrderNumber + '\'' +
                ", productId=" + productId +
                ", productCode='" + productCode + '\'' +
                ", productName='" + productName + '\'' +
                ", quantity=" + quantity +
                ", unit='" + unit + '\'' +
                ", scheduledStartDate=" + scheduledStartDate +
                ", scheduledDueDate=" + scheduledDueDate +
                ", actualStartDate=" + actualStartDate +
                ", actualCompletionDate=" + actualCompletionDate +
                ", status='" + status + '\'' +
                ", notes='" + notes + '\'' +
                ", createDate=" + createDate +
                ", updateDate=" + updateDate +
                '}';
    }
}
