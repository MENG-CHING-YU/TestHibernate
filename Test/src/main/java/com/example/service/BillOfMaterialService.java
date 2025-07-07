    package com.example.service;

    import com.example.model.BillOfMaterial;
    import java.sql.SQLException;
    import java.util.List;

    /**
     * BillOfMaterialService 介面定義了產品用料清單（BOM）相關的業務操作。
     */
    public interface BillOfMaterialService {

        /**
         * 新增一個 BOM 項目。
         * @param bom 要新增的 BillOfMaterial 物件。
         * @return 新增 BOM 項目後，資料庫自動生成的 BOM ID。
         * @throws SQLException 如果底層資料庫操作失敗。
         * @throws IllegalArgumentException 如果 BOM 數據無效或產品/物料ID不存在。
         */
        int addBillOfMaterial(BillOfMaterial bom) throws SQLException, IllegalArgumentException;

        /**
         * 根據 BOM ID 獲取 BOM 項目資訊。
         * @param bomId BOM 項目的唯一識別ID。
         * @return 匹配指定ID的 BillOfMaterial 物件，如果未找到則返回 null。
         * @throws SQLException 如果底層資料庫操作失敗。
         */
        BillOfMaterial getBillOfMaterialById(int bomId) throws SQLException;

        /**
         * 獲取所有 BOM 項目的列表。
         * @return 包含所有 BillOfMaterial 物件的列表。
         * @throws SQLException 如果底層資料庫操作失敗。
         */
        List<BillOfMaterial> getAllBillOfMaterials() throws SQLException;

        /**
         * 根據產品ID獲取該產品的所有 BOM 項目列表。
         * @param productId 產品的ID。
         * @return 該產品的所有 BillOfMaterial 物件的列表。
         * @throws SQLException 如果底層資料庫操作失敗。
         * @throws IllegalArgumentException 如果產品ID無效。
         */
        List<BillOfMaterial> getBillOfMaterialsByProductId(int productId) throws SQLException, IllegalArgumentException;

        /**
         * 更新 BOM 項目資訊。
         * @param bom 包含更新資訊的 BillOfMaterial 物件 (必須包含有效的 BOM ID)。
         * @return 如果更新成功返回 true，否則返回 false。
         * @throws SQLException 如果底層資料庫操作失敗。
         * @throws IllegalArgumentException 如果 BOM 數據無效或 BOM ID 不存在。
         */
        boolean updateBillOfMaterial(BillOfMaterial bom) throws SQLException, IllegalArgumentException;

        /**
         * 刪除指定ID的 BOM 項目。
         * @param bomId 要刪除 BOM 項目的ID。
         * @return 如果刪除成功返回 true，否則返回 false。
         * @throws SQLException 如果底層資料庫操作失敗。
         * @throws IllegalArgumentException 如果 BOM ID 無效。
         */
        boolean deleteBillOfMaterial(int bomId) throws SQLException, IllegalArgumentException;

        /**
         * 刪除指定產品的所有 BOM 項目。
         * @param productId 要刪除其 BOM 項目的產品ID。
         * @return 如果刪除成功返回 true，否則返回 false。
         * @throws SQLException 如果底層資料庫操作失敗。
         * @throws IllegalArgumentException 如果產品ID無效。
         */
        boolean deleteBillOfMaterialsByProductId(int productId) throws SQLException, IllegalArgumentException;
    }
    