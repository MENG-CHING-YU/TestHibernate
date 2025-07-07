package com.example.model;

import java.math.BigDecimal;
import java.sql.Timestamp;
import java.time.LocalDateTime; // 新增：用於獲取當前日期時間

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist; // 新增：用於在持久化前觸發
import jakarta.persistence.PreUpdate;   // 新增：用於在更新前觸發
import jakarta.persistence.Table;

@Entity
@Table(name = "products")
public class Product {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "product_id")
    private Integer productId;

    @Column(name = "product_code", unique = true, nullable = false, length = 50)
    private String productCode;

    @Column(name = "product_name", nullable = false, length = 255)
    private String productName;

    @Column(name = "category", length = 100)
    private String category;

    @Column(name = "description", columnDefinition = "NVARCHAR(MAX)")
    private String description;

    @Column(name = "unit", length = 20)
    private String unit;

    @Column(name = "selling_price", precision = 10, scale = 2)
    private BigDecimal sellingPrice;

    @Column(name = "cost", precision = 10, scale = 2)
    private BigDecimal cost;

    @Column(name = "is_active")
    private boolean isActive; // 這裡保持 boolean

    @Column(name = "image_url", length = 500)
    private String imageUrl;

    // 將 create_date 設置為不可手動更新
    @Column(name = "create_date", nullable = false, updatable = false)
    private Timestamp createDate;

    // update_date 保持可更新，但同樣設置為不可為 NULL (與資料庫定義一致)
    @Column(name = "update_date", nullable = false)
    private Timestamp updateDate;

    public Product() {
    }

    // 完整的建構函數，包括時間戳
    public Product(Integer productId, String productCode, String productName, String category,
                   String description, String unit, BigDecimal sellingPrice, BigDecimal cost,
                   boolean isActive, String imageUrl, Timestamp createDate, Timestamp updateDate) {
        this.productId = productId;
        this.productCode = productCode;
        this.productName = productName;
        this.category = category;
        this.description = description;
        this.unit = unit;
        this.sellingPrice = sellingPrice;
        this.cost = cost;
        this.isActive = isActive;
        this.imageUrl = imageUrl;
        this.createDate = createDate;
        this.updateDate = updateDate;
    }

    // 簡化的建構函數，不包括 ID，通常用於新增時
    // 注意：在這個建構函數中，我們不再傳入 createDate 和 updateDate，因為它們將由 @PrePersist 自動設置
    public Product(String productCode, String productName, String category,
                   String description, String unit, BigDecimal sellingPrice, BigDecimal cost,
                   boolean isActive, String imageUrl) { // 移除 Timestamp 參數
        this(null, productCode, productName, category, description, unit, sellingPrice, cost, isActive, imageUrl, null, null); // 傳入 null，讓 @PrePersist 處理
    }

    // --- Getter 和 Setter 方法 ---

    public Integer getProductId() { return productId; }
    public void setProductId(Integer productId) { this.productId = productId; }

    public String getProductCode() { return productCode; }
    public void setProductCode(String productCode) { this.productCode = productCode; }

    public String getProductName() { return productName; }
    public void setProductName(String productName) { this.productName = productName; }

    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public String getUnit() { return unit; }
    public void setUnit(String unit) { this.unit = unit; }

    public BigDecimal getSellingPrice() { return sellingPrice; }
    public void setSellingPrice(BigDecimal sellingPrice) { this.sellingPrice = sellingPrice; }

    public BigDecimal getCost() { return cost; }
    public void setCost(BigDecimal cost) { this.cost = cost; }

    public boolean isActive() {
        return isActive;
    }

    // 新增：提供一個 getIsActive() 方法，以確保 EL 能夠正確解析
    public boolean getIsActive() {
        return isActive;
    }

    public void setActive(boolean active) {
        isActive = active;
    }

    public String getImageUrl() { return imageUrl; }
    public void setImageUrl(String imageUrl) { this.imageUrl = imageUrl; }

    public Timestamp getCreateDate() { return createDate; }
    // 通常不提供 setCreateDate，因為它由系統自動管理
    // 但如果您的 DAO 需要在某些特殊情況下手動設置，可以保留
    public void setCreateDate(Timestamp createDate) { this.createDate = createDate; }


    public Timestamp getUpdateDate() { return updateDate; }
    // 通常不提供 setUpdateDate，因為它由系統自動管理
    // 但如果您的 DAO 需要在某些特殊情況下手動設置，可以保留
    public void setUpdateDate(Timestamp updateDate) { this.updateDate = updateDate; }

    // ====== 新增：生命週期回呼方法 ======
    @PrePersist // 在實體被持久化（新增）到資料庫之前呼叫
    protected void onCreate() {
        LocalDateTime now = LocalDateTime.now();
        this.createDate = Timestamp.valueOf(now);
        this.updateDate = Timestamp.valueOf(now);
    }

    @PreUpdate // 在實體被更新到資料庫之前呼叫
    protected void onUpdate() {
        this.updateDate = Timestamp.valueOf(LocalDateTime.now());
    }
    // ===============================

    @Override
    public String toString() {
        return "Product{" +
               "productId=" + productId +
               ", productCode='" + productCode + '\'' +
               ", productName='" + productName + '\'' +
               ", category='" + category + '\'' +
               ", description='" + (description != null && description.length() > 50 ? description.substring(0, 50) + "..." : description) + '\'' +
               ", unit='" + unit + '\'' +
               ", sellingPrice=" + sellingPrice +
               ", cost=" + cost +
               ", isActive=" + isActive +
               ", imageUrl='" + imageUrl + '\'' +
               ", createDate=" + createDate +
               ", updateDate=" + updateDate +
               '}';
    }
}
