package com.example.model;

import java.math.BigDecimal;
import java.sql.Timestamp;

/**
 * 產品用料清單 (BOM) 項目模型類，對應資料庫中的 'bill_of_materials' 表。
 * 包含從 'products' 和 'materials' 表 JOIN 過來的產品名稱和物料名稱，以便直接顯示。
 */
public class BillOfMaterial {
    private int bomId;
    private int productId;
    private String productName; // 從 Product 表獲取，用於顯示
    private int materialId;
    private String materialName; // 從 Material 表獲取，用於顯示
    private String materialUnit; // 從 Material 表獲取，用於顯示 (物料的單位)
    private BigDecimal quantity; // 消耗數量
    private Timestamp createDate;
    private Timestamp updateDate;

    // 預設構造函數
    public BillOfMaterial() {
    }

    // 帶所有參數的構造函數
    public BillOfMaterial(int bomId, int productId, String productName, int materialId,
                          String materialName, String materialUnit, BigDecimal quantity,
                          Timestamp createDate, Timestamp updateDate) {
        this.bomId = bomId;
        this.productId = productId;
        this.productName = productName;
        this.materialId = materialId;
        this.materialName = materialName;
        this.materialUnit = materialUnit;
        this.quantity = quantity;
        this.createDate = createDate;
        this.updateDate = updateDate;
    }

    // --- Getter 和 Setter 方法 ---
    public int getBomId() {
        return bomId;
    }

    public void setBomId(int bomId) {
        this.bomId = bomId;
    }

    public int getProductId() {
        return productId;
    }

    public void setProductId(int productId) {
        this.productId = productId;
    }

    public String getProductName() {
        return productName;
    }

    public void setProductName(String productName) {
        this.productName = productName;
    }

    public int getMaterialId() {
        return materialId;
    }

    public void setMaterialId(int materialId) {
        this.materialId = materialId;
    }

    public String getMaterialName() {
        return materialName;
    }

    public void setMaterialName(String materialName) {
        this.materialName = materialName;
    }

    public String getMaterialUnit() {
        return materialUnit;
    }

    public void setMaterialUnit(String materialUnit) {
        this.materialUnit = materialUnit;
    }

    public BigDecimal getQuantity() {
        return quantity;
    }

    public void setQuantity(BigDecimal quantity) {
        this.quantity = quantity;
    }

    public Timestamp getCreateDate() {
        return createDate;
    }

    public void setCreateDate(Timestamp createDate) {
        this.createDate = createDate;
    }

    public Timestamp getUpdateDate() {
        return updateDate;
    }

    public void setUpdateDate(Timestamp updateDate) {
        this.updateDate = updateDate;
    }

    @Override
    public String toString() {
        return "BillOfMaterial{" +
               "bomId=" + bomId +
               ", productId=" + productId +
               ", productName='" + productName + '\'' +
               ", materialId=" + materialId +
               ", materialName='" + materialName + '\'' +
               ", materialUnit='" + materialUnit + '\'' +
               ", quantity=" + quantity +
               '}';
    }
}
