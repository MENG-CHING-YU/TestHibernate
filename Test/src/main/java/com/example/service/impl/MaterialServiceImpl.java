    package com.example.service.impl;

    import com.example.dao.BillOfMaterialDao; // 需要檢查物料是否被BOM引用
    import com.example.dao.MaterialDao;
    import com.example.dao.impl.BillOfMaterialDaoImpl; // 引入 BillOfMaterialDao 實現
    import com.example.dao.impl.MaterialDaoImpl;
    import com.example.model.Material;
    import com.example.service.MaterialService;
    import java.sql.SQLException;
    import java.util.List;
    import java.util.logging.Level;
    import java.util.logging.Logger;
    import java.util.stream.Collectors;

    /**
     * MaterialService 介面的實現類。
     * 負責物料相關的業務邏輯，並調用 MaterialDao 進行數據庫操作。
     */
    public class MaterialServiceImpl implements MaterialService {

        private static final Logger LOGGER = Logger.getLogger(MaterialServiceImpl.class.getName());
        private MaterialDao materialDao;
        private BillOfMaterialDao billOfMaterialDao; // 需要檢查物料是否被BOM引用

        // 簡單的構造函數，直接實例化 DAO。在大型應用中，通常會使用依賴注入框架。
        public MaterialServiceImpl() {
            this.materialDao = new MaterialDaoImpl();
            this.billOfMaterialDao = new BillOfMaterialDaoImpl(); // 初始化 BillOfMaterialDao
            LOGGER.info("MaterialServiceImpl 已初始化。");
        }

        /**
         * 為了測試或特定場景，允許透過構造函數注入 DAO 實例。
         * @param materialDao MaterialDao 實例。
         * @param billOfMaterialDao BillOfMaterialDao 實例。
         */
        public MaterialServiceImpl(MaterialDao materialDao, BillOfMaterialDao billOfMaterialDao) {
            this.materialDao = materialDao;
            this.billOfMaterialDao = billOfMaterialDao;
            LOGGER.info("MaterialServiceImpl 已初始化，並通過構造函數注入 DAO。");
        }

        @Override
        public int addMaterial(Material material) throws SQLException, IllegalArgumentException {
            // 業務邏輯：數據驗證
            if (material == null || material.getMaterialName() == null || material.getMaterialName().trim().isEmpty() ||
                material.getMaterialCode() == null || material.getMaterialCode().trim().isEmpty() ||
                material.getUnit() == null || material.getUnit().trim().isEmpty() ||
                material.getUnitCost() == null) {
                LOGGER.log(Level.WARNING, "新增物料時數據無效: 物料名稱、物料代碼、單位或單位成本不能為空。");
                throw new IllegalArgumentException("物料名稱、物料代碼、單位和單位成本為必填項。");
            }
            if (material.getUnitCost().compareTo(java.math.BigDecimal.ZERO) < 0) {
                LOGGER.log(Level.WARNING, "新增物料時單位成本無效: 不能為負數。");
                throw new IllegalArgumentException("單位成本不能為負數。");
            }
            // 可以在此處添加更多驗證，例如檢查 materialCode 是否重複

            LOGGER.log(Level.INFO, "Service: 嘗試新增物料: {0}", material.getMaterialName());
            return materialDao.addMaterial(material);
        }

        @Override
        public Material getMaterialById(int materialId) throws SQLException {
            LOGGER.log(Level.INFO, "Service: 嘗試獲取物料，ID: {0}", materialId);
            return materialDao.getMaterialById(materialId);
        }

        @Override
        public List<Material> getAllMaterials() throws SQLException {
            LOGGER.info("Service: 嘗試獲取所有物料。");
            return materialDao.getAllMaterials();
        }

        @Override
        public boolean updateMaterial(Material material) throws SQLException, IllegalArgumentException {
            // 業務邏輯：數據驗證
            if (material == null || material.getMaterialId() <= 0 ||
                material.getMaterialName() == null || material.getMaterialName().trim().isEmpty() ||
                material.getMaterialCode() == null || material.getMaterialCode().trim().isEmpty() ||
                material.getUnit() == null || material.getUnit().trim().isEmpty() ||
                material.getUnitCost() == null) {
                LOGGER.log(Level.WARNING, "更新物料時數據無效: 物料ID、物料名稱、物料代碼、單位或單位成本不能為空。");
                throw new IllegalArgumentException("物料ID、物料名稱、物料代碼、單位和單位成本為必填項。");
            }
            if (material.getUnitCost().compareTo(java.math.BigDecimal.ZERO) < 0) {
                LOGGER.log(Level.WARNING, "更新物料時單位成本無效: 不能為負數。");
                throw new IllegalArgumentException("單位成本不能為負數。");
            }
            // 確保要更新的物料存在
            Material existingMaterial = materialDao.getMaterialById(material.getMaterialId());
            if (existingMaterial == null) {
                LOGGER.log(Level.WARNING, "更新物料失敗，物料ID不存在: {0}", material.getMaterialId());
                throw new IllegalArgumentException("要更新的物料不存在。");
            }

            LOGGER.log(Level.INFO, "Service: 嘗試更新物料，ID: {0}", material.getMaterialId());
            return materialDao.updateMaterial(material);
        }

        @Override
        public boolean deleteMaterial(int materialId) throws SQLException, IllegalStateException, IllegalArgumentException {
            // 業務邏輯：驗證 ID
            if (materialId <= 0) {
                LOGGER.log(Level.WARNING, "刪除物料時ID無效: {0}", materialId);
                throw new IllegalArgumentException("物料ID無效，必須大於0。");
            }
            // 檢查物料是否存在
            Material existingMaterial = materialDao.getMaterialById(materialId);
            if (existingMaterial == null) {
                LOGGER.log(Level.WARNING, "刪除物料失敗，物料ID不存在: {0}", materialId);
                return false; // 如果物料不存在，則認為刪除成功 (冪等性)
            }

            // 在刪除物料之前，檢查它是否被任何 BOM 項目引用
            // 由於 bill_of_materials 表的外鍵設置為 ON DELETE NO ACTION (或 RESTRICT)，
            // 如果存在引用，資料庫會阻止刪除，但我們在 Service 層提前檢查可以給出更友好的提示。
            List<com.example.model.BillOfMaterial> bomsReferencingMaterial = billOfMaterialDao.getAllBillOfMaterials()
                                                                                               .stream()
                                                                                               .filter(bom -> bom.getMaterialId() == materialId)
                                                                                               .collect(Collectors.toList());
            if (!bomsReferencingMaterial.isEmpty()) {
                LOGGER.log(Level.WARNING, "物料ID {0} 被 {1} 個 BOM 項目引用，無法刪除。", new Object[]{materialId, bomsReferencingMaterial.size()});
                throw new IllegalStateException("此物料被產品用料清單（BOM）引用，無法刪除。請先刪除所有相關的 BOM 項目。");
            }

            LOGGER.log(Level.INFO, "Service: 嘗試刪除物料，ID: {0}", materialId);
            return materialDao.deleteMaterial(materialId);
        }
    }
    