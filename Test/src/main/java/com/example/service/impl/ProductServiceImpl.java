package com.example.service.impl;

import com.example.dao.ProductDao;
import com.example.dao.impl.ProductDaoImpl; // 引入 ProductDaoImpl 具體實現
import com.example.model.Product;
import com.example.service.ProductService; // 引入 ProductService 介面

import java.sql.SQLException; // 因為 ProductService 介面要求拋出此異常
import java.util.List;
import java.math.BigDecimal; // 用於檢查數值型別

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * ProductService 介面的實現類。
 * 負責產品相關的業務邏輯處理和異常轉換。
 */
public class ProductServiceImpl implements ProductService {

    private static final Logger LOGGER = LoggerFactory.getLogger(ProductServiceImpl.class);

    private ProductDao productDao; // 依賴 ProductDao

    // 透過建構子注入 ProductDao 實例
    public ProductServiceImpl() {
        this.productDao = new ProductDaoImpl(); // 這裡簡單地實例化，實際應用中會使用 DI 框架
    }

    // 如果您有其他方式提供 ProductDao，也可以使用 setter 注入或直接傳入
    public ProductServiceImpl(ProductDao productDao) {
        this.productDao = productDao;
    }

    @Override
    public int addProduct(Product product) throws SQLException, IllegalArgumentException {
        LOGGER.info("Service: 嘗試新增產品: {}", product.getProductName());

        // 1. 業務邏輯/參數驗證 (符合 IllegalArgumentException 需求)
        if (product == null) {
            throw new IllegalArgumentException("產品物件不能為空。");
        }
        if (product.getProductCode() == null || product.getProductCode().trim().isEmpty()) {
            throw new IllegalArgumentException("產品代碼為必填項。");
        }
        if (product.getProductName() == null || product.getProductName().trim().isEmpty()) {
            throw new IllegalArgumentException("產品名稱為必填項。");
        }
        if (product.getSellingPrice() == null || product.getSellingPrice().compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException("銷售價格不能為空且必須大於或等於零。");
        }
        if (product.getCost() == null || product.getCost().compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException("成本不能為空且必須大於或等於零。");
        }
        // 您可以根據需要添加更多驗證，例如 productCode 的唯一性檢查（可能需要額外的 DAO 方法）

        try {
            // 呼叫 DAO 層的方法。DAO 層的 addProduct 現在返回 Product，但我們需要 int。
            // 介面要求返回 int (ID)，所以從返回的 Product 物件中獲取 ID
            Product addedProduct = productDao.addProduct(product); // DAO層現在返回Product

            // 確保 ID 已被 Hibernate 自動回填
            if (addedProduct.getProductId() == null || addedProduct.getProductId() <= 0) {
                // 這是一個不太可能發生的情況，除非主鍵生成策略有問題
                LOGGER.error("Service: 新增產品成功，但無法獲取生成的ID: {}", product.getProductName());
                throw new SQLException("新增產品成功，但無法獲取生成的產品ID。");
            }

            LOGGER.info("Service: 成功新增產品: {}，ID: {}", product.getProductName(), addedProduct.getProductId());
            return addedProduct.getProductId(); // 返回 ID

        } catch (RuntimeException e) { // 捕捉 DAO 層拋出的 RuntimeException
            LOGGER.error("Service: 新增產品時發生資料庫錯誤。", e);
            // 將底層的 RuntimeException 包裝為 SQLException 拋出
            throw new SQLException("無法新增產品：資料庫操作失敗 - " + e.getMessage(), e);
        }
    }

    @Override
    public Product getProductById(int productId) throws SQLException {
        LOGGER.info("Service: 嘗試獲取ID為 {} 的產品。", productId);
        // 1. 業務邏輯/參數驗證
        if (productId <= 0) {
            throw new IllegalArgumentException("產品ID無效，必須是正整數。");
        }
        
        try {
            // DAO 層的 getProductById 仍然拋出 SQLException，直接傳遞
            Product product = productDao.getProductById(productId);
            if (product != null) {
                LOGGER.info("Service: 成功獲取ID為 {} 的產品。", productId);
            } else {
                LOGGER.warn("Service: 未找到ID為 {} 的產品。", productId);
            }
            return product;
        } catch (SQLException e) { // 捕捉 DAO 層的 SQLException，並重新拋出
            LOGGER.error("Service: 獲取ID為 {} 的產品時發生資料庫錯誤。", productId, e);
            throw e; // 直接重新拋出 SQLException
        }
    }

    @Override
    public List<Product> getAllProducts() throws SQLException {
        LOGGER.info("Service: 嘗試獲取所有產品。");
        try {
            // DAO 層的 getAllProducts 仍然拋出 SQLException，直接傳遞
            List<Product> products = productDao.getAllProducts();
            LOGGER.info("Service: 成功獲取 {} 個產品。", products.size());
            return products;
        } catch (SQLException e) { // 捕捉 DAO 層的 SQLException，並重新拋出
            LOGGER.error("Service: 獲取所有產品時發生資料庫錯誤。", e);
            throw e; // 直接重新拋出 SQLException
        }
    }

    @Override
    public boolean updateProduct(Product product) throws SQLException, IllegalArgumentException {
        LOGGER.info("Service: 嘗試更新ID為 {} 的產品。", product.getProductId());

        // 1. 業務邏輯/參數驗證 (符合 IllegalArgumentException 需求)
        if (product == null) {
            throw new IllegalArgumentException("產品物件不能為空。");
        }
        if (product.getProductId() == null || product.getProductId() <= 0) {
            throw new IllegalArgumentException("產品ID無效，無法更新。");
        }
        if (product.getProductCode() == null || product.getProductCode().trim().isEmpty()) {
            throw new IllegalArgumentException("產品代碼為必填項。");
        }
        if (product.getProductName() == null || product.getProductName().trim().isEmpty()) {
            throw new IllegalArgumentException("產品名稱為必填項。");
        }
        if (product.getSellingPrice() == null || product.getSellingPrice().compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException("銷售價格不能為空且必須大於或等於零。");
        }
        if (product.getCost() == null || product.getCost().compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException("成本不能為空且必須大於或等於零。");
        }
        // 您可以根據需要添加更多驗證

        try {
            // 檢查產品是否存在 (可選，但推薦，確保更新的是現有記錄)
            Product existingProduct = productDao.getProductById(product.getProductId());
            if (existingProduct == null) {
                LOGGER.warn("Service: 嘗試更新的產品 (ID: {}) 不存在。", product.getProductId());
                throw new IllegalArgumentException("要更新的產品不存在。產品ID: " + product.getProductId());
            }
            
            // DAO 層的 updateProduct 現在回傳 boolean
            boolean success = productDao.updateProduct(product);

            if (success) {
                LOGGER.info("Service: 成功更新ID為 {} 的產品。", product.getProductId());
            } else {
                // 這種情況通常不會發生，因為前面已經檢查了存在性
                LOGGER.warn("Service: 更新ID為 {} 的產品失敗，未找到匹配記錄 (DAO 返回 false)。", product.getProductId());
            }
            return success;
        } catch (SQLException e) { // 捕捉 getProductById 可能拋出的 SQLException
            LOGGER.error("Service: 更新產品時發生資料庫錯誤 (可能在檢查產品是否存在時)。", product.getProductId(), e);
            throw e; // 重新拋出
        } catch (RuntimeException e) { // 捕捉 DAO 層的 RuntimeException，並包裝
            LOGGER.error("Service: 更新產品時發生底層錯誤。", product.getProductId(), e);
            throw new SQLException("無法更新ID為 " + product.getProductId() + " 的產品：底層錯誤 - " + e.getMessage(), e);
        }
    }

    @Override
    public boolean deleteProduct(int productId) throws SQLException, IllegalStateException, IllegalArgumentException {
        LOGGER.info("Service: 嘗試刪除ID為 {} 的產品。", productId);

        // 1. 業務邏輯/參數驗證 (符合 IllegalArgumentException 需求)
        if (productId <= 0) {
            throw new IllegalArgumentException("產品ID無效，必須是正整數。");
        }

        try {
            // 2. 業務邏輯 (符合 IllegalStateException 需求)
            // 檢查產品是否存在，以及是否有相關聯的數據 (例如，BOM 條目、訂單明細)
            // 這可能需要額外的 DAO 方法來查詢關聯數據，例如 productDao.hasRelatedBOMs(productId)
            // 這裡假設如果相關數據存在，DAO 層的 deleteProduct 會因外鍵約束而失敗並拋出異常。
            // 更好的做法是 Service 層先檢查：
            // if (productDao.isProductUsedInBOM(productId)) { // 假設有這個方法
            //     throw new IllegalStateException("產品ID " + productId + " 被其他數據引用而無法刪除。");
            // }

            // 呼叫 DAO 層的方法，DAO 的 deleteProduct 回傳 boolean
            boolean success = productDao.deleteProduct(productId);

            if (success) {
                LOGGER.info("Service: 成功刪除ID為 {} 的產品。", productId);
            } else {
                LOGGER.warn("Service: 刪除ID為 {} 的產品失敗，可能未找到該產品。", productId);
                // 如果刪除失敗但沒有拋出異常，通常意味著沒有該ID的記錄。
                // 根據介面定義，"如果刪除成功返回 true，否則返回 false" 是合理的。
            }
            return success;

        } catch (RuntimeException e) { // 捕捉 DAO 層的 RuntimeException
            LOGGER.error("Service: 刪除ID為 {} 的產品時發生底層錯誤。", productId, e);
            // 可以檢查 RuntimeException 的 Cause (例如，Hibernate 的 ConstraintViolationException)
            // if (e.getCause() instanceof ConstraintViolationException) {
            //     throw new IllegalStateException("產品ID " + productId + " 被其他數據引用，無法刪除。", e);
            // }
            throw new SQLException("無法刪除ID為 " + productId + " 的產品：資料庫操作失敗 - " + e.getMessage(), e);
        }
    }
}