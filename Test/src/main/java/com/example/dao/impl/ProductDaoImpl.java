package com.example.dao.impl;

import com.example.dao.ProductDao;
import com.example.model.Product;

import java.sql.SQLException; // 仍然需要，因為 getProductById 和 getAllProducts 拋出
import java.util.ArrayList;
import java.util.List;

// Hibernate 7.x (Jakarta Persistence) related imports
import org.hibernate.HibernateException;
import org.hibernate.Session;
import org.hibernate.Transaction;
import org.hibernate.query.Query;

// Your custom Hibernate utility
import com.example.dao.util.HibernateUtil;

// SLF4J for logging
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * ProductDao 介面的 Hibernate 實現類。
 * 負責產品實體的 CRUD 操作，嚴格遵循 ProductDao 介面的方法簽名和異常宣告。
 */
public class ProductDaoImpl implements ProductDao {

    private static final Logger LOGGER = LoggerFactory.getLogger(ProductDaoImpl.class);

    @Override
    public Product addProduct(Product product) { // 介面定義: 回傳 Product，不拋出 SQLException
        Session session = null;
        Transaction transaction = null;
        try {
            session = HibernateUtil.openSession();
            transaction = session.beginTransaction();

            session.persist(product); // 對於新實體使用 persist

            transaction.commit();
            LOGGER.info("成功新增產品: {} (ID: {})", product.getProductName(), product.getProductId());
            return product; // 回傳帶有自動生成 ID 的產品物件

        } catch (HibernateException e) {
            if (transaction != null) {
                HibernateUtil.rollbackTransaction(transaction);
            }
            LOGGER.error("新增產品時發生 Hibernate 錯誤: {}", product.getProductName(), e);
            // 介面沒有宣告 SQLException，所以拋出 RuntimeException
            throw new RuntimeException("新增產品失敗：資料庫操作失敗 - " + e.getMessage(), e);
        } catch (Exception e) {
            if (transaction != null) {
                HibernateUtil.rollbackTransaction(transaction);
            }
            LOGGER.error("新增產品時發生未預期錯誤: {}", product.getProductName(), e);
            throw new RuntimeException("新增產品時發生未預期錯誤: " + e.getMessage(), e);
        } finally {
            if (session != null && session.isOpen()) {
                session.close();
            }
        }
    }

    @Override
    public Product getProductById(int productId) throws SQLException { // 介面定義: 拋出 SQLException
        Session session = null;
        try {
            session = HibernateUtil.openSession();
            Product product = session.get(Product.class, productId);

            if (product != null) {
                LOGGER.info("成功取得產品，ID: {}", productId);
            } else {
                LOGGER.warn("未找到 ID 為 {} 的產品。", productId);
            }
            return product;

        } catch (HibernateException e) {
            LOGGER.error("取得產品時發生 Hibernate 錯誤，ID: {}", productId, e);
            throw new SQLException("取得 ID 為 " + productId + " 的產品失敗：" + e.getMessage(), e); // 包裝為 SQLException
        } catch (Exception e) {
            LOGGER.error("取得產品時發生未預期錯誤，ID: {}", productId, e);
            throw new SQLException("取得 ID 為 " + productId + " 的產品時發生未預期錯誤：" + e.getMessage(), e); // 包裝為 SQLException
        } finally {
            if (session != null && session.isOpen()) {
                session.close();
            }
        }
    }

    @Override
    public List<Product> getAllProducts() throws SQLException { // 介面定義: 拋出 SQLException
        Session session = null;
        List<Product> products = new ArrayList<>();
        try {
            session = HibernateUtil.openSession();
            Query<Product> query = session.createQuery("FROM Product", Product.class);
            products = query.list();

            LOGGER.info("成功取得 {} 個產品。", products.size());
            return products;

        } catch (HibernateException e) {
            LOGGER.error("取得所有產品時發生 Hibernate 錯誤。", e);
            throw new SQLException("取得所有產品失敗：" + e.getMessage(), e); // 包裝為 SQLException
        } catch (Exception e) {
            LOGGER.error("取得所有產品時發生未預期錯誤。", e);
            throw new SQLException("取得所有產品時發生未預期錯誤：" + e.getMessage(), e); // 包裝為 SQLException
        } finally {
            if (session != null && session.isOpen()) {
                session.close();
            }
        }
    }

    @Override
    public boolean updateProduct(Product product) { // 介面定義: 回傳 boolean，不拋出 SQLException
        Session session = null;
        Transaction transaction = null;
        try {
            session = HibernateUtil.openSession();
            transaction = session.beginTransaction();

            session.merge(product); // merge 用於更新或保存分離的物件

            transaction.commit();

            LOGGER.info("成功更新產品，ID: {}", product.getProductId());
            return true; // 更新成功

        } catch (HibernateException e) {
            if (transaction != null) {
                HibernateUtil.rollbackTransaction(transaction);
            }
            LOGGER.error("更新產品時發生 Hibernate 錯誤，ID: {}", product.getProductId(), e);
            // 介面沒有宣告 SQLException，所以拋出 RuntimeException
            throw new RuntimeException("更新產品 ID " + product.getProductId() + " 失敗：" + e.getMessage(), e);
        } catch (Exception e) {
            if (transaction != null) {
                HibernateUtil.rollbackTransaction(transaction);
            }
            LOGGER.error("更新產品時發生未預期錯誤，ID: {}", product.getProductId(), e);
            throw new RuntimeException("更新產品 ID " + product.getProductId() + " 時發生未預期錯誤：" + e.getMessage(), e);
        } finally {
            if (session != null && session.isOpen()) {
                session.close();
            }
        }
    }

    @Override
    public boolean deleteProduct(int productId) { // 介面定義: 回傳 boolean，不拋出 SQLException
        Session session = null;
        Transaction transaction = null;
        try {
            session = HibernateUtil.openSession();
            transaction = session.beginTransaction();

            Product product = session.get(Product.class, productId);
            boolean deleted = false;

            if (product != null) {
                session.remove(product);
                transaction.commit();
                LOGGER.info("成功刪除產品，ID: {}", productId);
                deleted = true;
            } else {
                // 如果產品未找到，不需要提交，直接回傳 false
                if (transaction.isActive()) {
                     transaction.rollback(); // 如果 transaction 仍活躍，則回滾
                }
                LOGGER.warn("刪除產品失敗。未找到 ID 為 {} 的產品。", productId);
                deleted = false;
            }
            return deleted;

        } catch (HibernateException e) {
            if (transaction != null) {
                HibernateUtil.rollbackTransaction(transaction);
            }
            LOGGER.error("刪除產品時發生 Hibernate 錯誤，ID: {}", productId, e);
            // 介面沒有宣告 SQLException，所以拋出 RuntimeException
            throw new RuntimeException("刪除產品 ID " + productId + " 失敗：" + e.getMessage(), e);
        } catch (Exception e) {
            if (transaction != null) {
                HibernateUtil.rollbackTransaction(transaction);
            }
            LOGGER.error("刪除產品時發生未預期錯誤，ID: {}", productId, e);
            throw new RuntimeException("刪除產品 ID " + productId + " 時發生未預期錯誤：" + e.getMessage(), e);
        } finally {
            if (session != null && session.isOpen()) {
                session.close();
            }
        }
    }
}