    package com.example.service;

    import com.example.model.Product;
    import java.sql.SQLException;
    import java.util.List;

    /**
     * ProductService 介面定義了產品相關的業務操作。
     * 這些操作通常會利用 ProductDao 來與資料庫交互，並可能包含業務邏輯。
     */
    public interface ProductService {

        /**
         * 新增一個產品。
         * @param product 要新增的產品物件。
         * @return 新增產品後，資料庫自動生成的產品ID。
         * @throws SQLException 如果底層資料庫操作失敗。
         * @throws IllegalArgumentException 如果產品數據無效（例如，缺少必要的字段）。
         */
        int addProduct(Product product) throws SQLException, IllegalArgumentException;

        /**
         * 根據產品ID獲取產品資訊。
         * @param productId 產品的唯一識別ID。
         * @return 匹配指定ID的產品物件，如果未找到則返回 null。
         * @throws SQLException 如果底層資料庫操作失敗。
         */
        Product getProductById(int productId) throws SQLException;

        /**
         * 獲取所有產品的列表。
         * @return 包含所有產品物件的列表。
         * @throws SQLException 如果底層資料庫操作失敗。
         */
        List<Product> getAllProducts() throws SQLException;

        /**
         * 更新產品資訊。
         * @param product 包含更新資訊的產品物件 (必須包含有效的產品ID)。
         * @return 如果更新成功返回 true，否則返回 false。
         * @throws SQLException 如果底層資料庫操作失敗。
         * @throws IllegalArgumentException 如果產品數據無效或產品ID不存在。
         */
        boolean updateProduct(Product product) throws SQLException, IllegalArgumentException;

        /**
         * 刪除指定ID的產品。
         * @param productId 要刪除產品的ID。
         * @return 如果刪除成功返回 true，否則返回 false。
         * @throws SQLException 如果底層資料庫操作失敗。
         * @throws IllegalStateException 如果產品被其他數據（如 BOM 項目）引用而無法刪除。
         * @throws IllegalArgumentException 如果產品ID無效。
         */
        boolean deleteProduct(int productId) throws SQLException, IllegalStateException, IllegalArgumentException;
    }
    