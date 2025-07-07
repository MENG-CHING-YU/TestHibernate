package com.example.model;

import java.math.BigDecimal;
import java.sql.Timestamp;

/**
 * 物料模型類，對應資料庫中的 'materials' 表。
 */
public class Material {
    private int materialId;
    private String materialCode;
    private String materialName;
    private String description;
    private String unit;
    private BigDecimal unitCost;
    private BigDecimal currentStock;
    private boolean isActive;
    private Timestamp createDate;
    private Timestamp updateDate;

    // 預設構造函數
    public Material() {
    }

    // 帶所有參數的構造函數
    public Material(int materialId, String materialCode, String materialName, String description,
                    String unit, BigDecimal unitCost, BigDecimal currentStock, boolean isActive,
                    Timestamp createDate, Timestamp updateDate) {
        this.materialId = materialId;
        this.materialCode = materialCode;
        this.materialName = materialName;
        this.description = description;
        this.unit = unit;
        this.unitCost = unitCost;
        this.currentStock = currentStock;
        this.isActive = isActive;
        this.createDate = createDate;
        this.updateDate = updateDate;
    }

    // --- Getter 和 Setter 方法 ---
    public int getMaterialId() {
        return materialId;
    }

    public void setMaterialId(int materialId) {
        this.materialId = materialId;
    }

    public String getMaterialCode() {
        return materialCode;
    }

    public void setMaterialCode(String materialCode) {
        this.materialCode = materialCode;
    }

    public String getMaterialName() {
        return materialName;
    }

    public void setMaterialName(String materialName) {
        this.materialName = materialName;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getUnit() {
        return unit;
    }

    public void setUnit(String unit) {
        this.unit = unit;
    }

    public BigDecimal getUnitCost() {
        return unitCost;
    }

    public void setUnitCost(BigDecimal unitCost) {
        this.unitCost = unitCost;
    }

    public BigDecimal getCurrentStock() {
        return currentStock;
    }

    public void setCurrentStock(BigDecimal currentStock) {
        this.currentStock = currentStock;
    }

    public boolean isActive() {
        return isActive;
    }

    public void setActive(boolean active) {
        isActive = active;
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
        return "Material{" +
               "materialId=" + materialId +
               ", materialCode='" + materialCode + '\'' +
               ", materialName='" + materialName + '\'' +
               ", unit='" + unit + '\'' +
               ", unitCost=" + unitCost +
               ", currentStock=" + currentStock +
               ", isActive=" + isActive +
               '}';
    }
}
