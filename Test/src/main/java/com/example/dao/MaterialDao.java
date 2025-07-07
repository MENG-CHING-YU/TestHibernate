    package com.example.dao;

    import com.example.model.Material;
    import java.sql.SQLException;
    import java.util.List;

    /**
     * MaterialDao 介面定義了對 Material (物料) 資料實體的 CRUD (Create, Read, Update, Delete) 操作。
     */
    public interface MaterialDao {

        /**
         * 新增一個物料到資料庫。
         * @param material 要新增的物料物件。
         * @return 新增物料後，資料庫自動生成的物料ID。
         * @throws SQLException 如果資料庫操作發生錯誤。
         */
        int addMaterial(Material material) throws SQLException;

        /**
         * 根據物料ID從資料庫中獲取物料。
         * @param materialId 物料的唯一識別ID。
         * @return 匹配指定ID的物料物件，如果未找到則返回 null。
         * @throws SQLException 如果資料庫操作發生錯誤。
         */
        Material getMaterialById(int materialId) throws SQLException;

        /**
         * 從資料庫中獲取所有物料的列表。
         * @return 包含所有物料物件的列表。
         * @throws SQLException 如果資料庫操作發生錯誤。
         */
        List<Material> getAllMaterials() throws SQLException;

        /**
         * 更新資料庫中現有的物料資訊。
         * @param material 包含更新資訊的物料物件 (必須包含有效的物料ID)。
         * @return 如果更新成功返回 true，否則返回 false。
         * @throws SQLException 如果資料庫操作發生錯誤。
         */
        boolean updateMaterial(Material material) throws SQLException;

        /**
         * 從資料庫中刪除指定ID的物料。
         * 注意：如果物料被 BOM 項目或其他數據引用，此操作可能因外鍵約束而失敗。
         * @param materialId 要刪除物料的ID。
         * @return 如果刪除成功返回 true，否則返回 false。
         * @throws SQLException 如果資料庫操作發生錯誤。
         */
        boolean deleteMaterial(int materialId) throws SQLException;
    }
    