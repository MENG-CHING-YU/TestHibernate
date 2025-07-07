    package com.example.dao;

    import com.example.model.BillOfMaterial;
    import java.sql.SQLException;
    import java.util.List;

    /**
     * BillOfMaterialDao 介面定義了對 BillOfMaterial (產品用料清單) 資料實體的 CRUD 操作。
     * 也包括獲取關聯產品和物料名稱的方法。
     */
    public interface BillOfMaterialDao {

        /**
         * 新增一個 BOM 項目到資料庫。
         * @param bom 要新增的 BillOfMaterial 物件。
         * @return 新增 BOM 項目後，資料庫自動生成的 BOM ID。
         * @throws SQLException 如果資料庫操作發生錯誤。
         */
        int addBillOfMaterial(BillOfMaterial bom) throws SQLException;

        /**
         * 根據 BOM ID 從資料庫中獲取 BOM 項目。
         * 查詢結果會包含產品名稱和物料名稱。
         * @param bomId BOM 項目的唯一識別ID。
         * @return 匹配指定ID的 BillOfMaterial 物件，如果未找到則返回 null。
         * @throws SQLException 如果資料庫操作發生錯誤。
         */
        BillOfMaterial getBillOfMaterialById(int bomId) throws SQLException;

        /**
         * 從資料庫中獲取所有 BOM 項目的列表。
         * 每個 BOM 項目會包含其關聯的產品名稱和物料名稱。
         * @return 包含所有 BillOfMaterial 物件的列表。
         * @throws SQLException 如果資料庫操作發生錯誤。
         */
        List<BillOfMaterial> getAllBillOfMaterials() throws SQLException;

        /**
         * 根據產品ID獲取該產品的所有 BOM 項目列表。
         * 每個 BOM 項目會包含其關聯的產品名稱和物料名稱。
         * @param productId 產品的ID。
         * @return 該產品的所有 BillOfMaterial 物件的列表。
         * @throws SQLException 如果資料庫操作發生錯誤。
         */
        List<BillOfMaterial> getBillOfMaterialsByProductId(int productId) throws SQLException;

        /**
         * 更新資料庫中現有的 BOM 項目資訊。
         * @param bom 包含更新資訊的 BillOfMaterial 物件 (必須包含有效的 BOM ID)。
         * @return 如果更新成功返回 true，否則返回 false。
         * @throws SQLException 如果資料庫操作發生錯誤。
         */
        boolean updateBillOfMaterial(BillOfMaterial bom) throws SQLException;

        /**
         * 從資料庫中刪除指定ID的 BOM 項目。
         * @param bomId 要刪除 BOM 項目的ID。
         * @return 如果刪除成功返回 true，否則返回 false。
         * @throws SQLException 如果資料庫操作發生錯誤。
         */
        boolean deleteBillOfMaterial(int bomId) throws SQLException;

        /**
         * 刪除指定產品的所有 BOM 項目。
         * @param productId 要刪除其 BOM 項目的產品ID。
         * @return 如果刪除成功返回 true，否則返回 false。
         * @throws SQLException 如果資料庫操作發生錯誤。
         */
        boolean deleteBillOfMaterialsByProductId(int productId) throws SQLException;
    }
    