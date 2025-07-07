    package com.example.service;

    import com.example.model.Material;
    import java.sql.SQLException;
    import java.util.List;

    /**
     * MaterialService 介面定義了物料相關的業務操作。
     * 這些操作通常會利用 MaterialDao 來與資料庫交互，並可能包含業務邏輯。
     */
    public interface MaterialService {

        /**
         * 新增一個物料。
         * @param material 要新增的物料物件。
         * @return 新增物料後，資料庫自動生成的物料ID。
         * @throws SQLException 如果底層資料庫操作失敗。
         * @throws IllegalArgumentException 如果物料數據無效（例如，缺少必要的字段）。
         */
        int addMaterial(Material material) throws SQLException, IllegalArgumentException;

        /**
         * 根據物料ID獲取物料資訊。
         * @param materialId 物料的唯一識別ID。
         * @return 匹配指定ID的物料物件，如果未找到則返回 null。
         * @throws SQLException 如果底層資料庫操作失敗。
         */
        Material getMaterialById(int materialId) throws SQLException;

        /**
         * 獲取所有物料的列表。
         * @return 包含所有物料物件的列表。
         * @throws SQLException 如果底層資料庫操作失敗。
         */
        List<Material> getAllMaterials() throws SQLException;

        /**
         * 更新物料資訊。
         * @param material 包含更新資訊的物料物件 (必須包含有效的物料ID)。
         * @return 如果更新成功返回 true，否則返回 false。
         * @throws SQLException 如果底層資料庫操作失敗。
         * @throws IllegalArgumentException 如果物料數據無效或物料ID不存在。
         */
        boolean updateMaterial(Material material) throws SQLException, IllegalArgumentException;

        /**
         * 刪除指定ID的物料。
         * @param materialId 要刪除物料的ID。
         * @return 如果刪除成功返回 true，否則返回 false。
         * @throws SQLException 如果底層資料庫操作失敗。
         * @throws IllegalStateException 如果物料被其他數據（如 BOM 項目）引用而無法刪除。
         * @throws IllegalArgumentException 如果物料ID無效。
         */
        boolean deleteMaterial(int materialId) throws SQLException, IllegalStateException, IllegalArgumentException;
    }
    