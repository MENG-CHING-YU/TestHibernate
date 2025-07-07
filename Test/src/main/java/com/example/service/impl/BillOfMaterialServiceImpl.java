    package com.example.service.impl;

    import com.example.dao.BillOfMaterialDao;
    import com.example.dao.MaterialDao; // 需要 MaterialDao 來驗證物料是否存在
    import com.example.dao.ProductDao; // 需要 ProductDao 來驗證產品是否存在
    import com.example.dao.impl.BillOfMaterialDaoImpl;
    import com.example.dao.impl.MaterialDaoImpl; // 引入 MaterialDao 實現
    import com.example.dao.impl.ProductDaoImpl; // 引入 ProductDao 實現
    import com.example.model.BillOfMaterial;
    import com.example.model.Material;
    import com.example.model.Product;
    import com.example.service.BillOfMaterialService;
    import java.sql.SQLException;
    import java.util.List;
    import java.util.logging.Level;
    import java.util.logging.Logger;

    /**
     * BillOfMaterialService 介面的實現類。
     * 負責產品用料清單（BOM）相關的業務邏輯，並調用 BillOfMaterialDao 進行數據庫操作。
     */
    public class BillOfMaterialServiceImpl implements BillOfMaterialService {

        private static final Logger LOGGER = Logger.getLogger(BillOfMaterialServiceImpl.class.getName());
        private BillOfMaterialDao bomDao;
        private ProductDao productDao; // 用於驗證產品是否存在
        private MaterialDao materialDao; // 用於驗證物料是否存在

        public BillOfMaterialServiceImpl() {
            this.bomDao = new BillOfMaterialDaoImpl();
            this.productDao = new ProductDaoImpl();
            this.materialDao = new MaterialDaoImpl();
            LOGGER.info("BillOfMaterialServiceImpl 已初始化。");
        }

        /**
         * 為了測試或特定場景，允許透過構造函數注入 DAO 實例。
         * @param bomDao BillOfMaterialDao 實例。
         * @param productDao ProductDao 實例。
         * @param materialDao MaterialDao 實例。
         */
        public BillOfMaterialServiceImpl(BillOfMaterialDao bomDao, ProductDao productDao, MaterialDao materialDao) {
            this.bomDao = bomDao;
            this.productDao = productDao;
            this.materialDao = materialDao;
            LOGGER.info("BillOfMaterialServiceImpl 已初始化，並通過構造函數注入 DAO。");
        }

        @Override
        public int addBillOfMaterial(BillOfMaterial bom) throws SQLException, IllegalArgumentException {
            // 業務邏輯：數據驗證
            if (bom == null || bom.getProductId() <= 0 || bom.getMaterialId() <= 0 || bom.getQuantity() == null) {
                LOGGER.log(Level.WARNING, "新增 BOM 項目時數據無效: 產品ID、物料ID或數量不能為空/無效。");
                throw new IllegalArgumentException("產品、物料和消耗數量為必填項。");
            }
            if (bom.getQuantity().compareTo(java.math.BigDecimal.ZERO) <= 0) {
                LOGGER.log(Level.WARNING, "新增 BOM 項目時消耗數量無效: 必須大於零。");
                throw new IllegalArgumentException("消耗數量必須大於零。");
            }

            // 驗證產品和物料是否存在
            Product product = productDao.getProductById(bom.getProductId());
            if (product == null) {
                LOGGER.log(Level.WARNING, "新增 BOM 失敗: 產品ID {0} 不存在。", bom.getProductId());
                throw new IllegalArgumentException("指定的產品不存在。");
            }
            Material material = materialDao.getMaterialById(bom.getMaterialId());
            if (material == null) {
                LOGGER.log(Level.WARNING, "新增 BOM 失敗: 物料ID {0} 不存在。", bom.getMaterialId());
                throw new IllegalArgumentException("指定的物料不存在。");
            }

            LOGGER.log(Level.INFO, "Service: 嘗試新增 BOM 項目: 產品ID {0}, 物料ID {1}", new Object[]{bom.getProductId(), bom.getMaterialId()});
            // 設置產品名稱和物料單位，以便在後續操作中直接使用（儘管這裡不會持久化到BOM表）
            // bom.setProductName(product.getProductName()); // 這些是為返回給JSP時用的，不是持久化
            // bom.setMaterialUnit(material.getUnit());

            return bomDao.addBillOfMaterial(bom);
        }

        @Override
        public BillOfMaterial getBillOfMaterialById(int bomId) throws SQLException {
            LOGGER.log(Level.INFO, "Service: 嘗試獲取 BOM 項目，ID: {0}", bomId);
            return bomDao.getBillOfMaterialById(bomId);
        }

        @Override
        public List<BillOfMaterial> getAllBillOfMaterials() throws SQLException {
            LOGGER.info("Service: 嘗試獲取所有 BOM 項目。");
            return bomDao.getAllBillOfMaterials();
        }

        @Override
        public List<BillOfMaterial> getBillOfMaterialsByProductId(int productId) throws SQLException, IllegalArgumentException {
            if (productId <= 0) {
                LOGGER.log(Level.WARNING, "獲取產品 BOM 時產品ID無效: {0}", productId);
                throw new IllegalArgumentException("產品ID無效，必須大於0。");
            }
            LOGGER.log(Level.INFO, "Service: 嘗試獲取產品ID {0} 的所有 BOM 項目。", productId);
            return bomDao.getBillOfMaterialsByProductId(productId);
        }

        @Override
        public boolean updateBillOfMaterial(BillOfMaterial bom) throws SQLException, IllegalArgumentException {
            // 業務邏輯：數據驗證
            if (bom == null || bom.getBomId() <= 0 || bom.getProductId() <= 0 || bom.getMaterialId() <= 0 || bom.getQuantity() == null) {
                LOGGER.log(Level.WARNING, "更新 BOM 項目時數據無效: ID、產品ID、物料ID或數量不能為空/無效。");
                throw new IllegalArgumentException("BOM ID、產品、物料和消耗數量為必填項。");
            }
            if (bom.getQuantity().compareTo(java.math.BigDecimal.ZERO) <= 0) {
                LOGGER.log(Level.WARNING, "更新 BOM 項目時消耗數量無效: 必須大於零。");
                throw new IllegalArgumentException("消耗數量必須大於零。");
            }

            // 確保 BOM 項目存在
            BillOfMaterial existingBom = bomDao.getBillOfMaterialById(bom.getBomId());
            if (existingBom == null) {
                LOGGER.log(Level.WARNING, "更新 BOM 失敗，BOM ID不存在: {0}", bom.getBomId());
                throw new IllegalArgumentException("要更新的 BOM 項目不存在。");
            }
            // 驗證產品和物料是否存在（如果用戶嘗試更改它們）
            Product product = productDao.getProductById(bom.getProductId());
            if (product == null) {
                LOGGER.log(Level.WARNING, "更新 BOM 失敗: 產品ID {0} 不存在。", bom.getProductId());
                throw new IllegalArgumentException("指定的產品不存在。");
            }
            Material material = materialDao.getMaterialById(bom.getMaterialId());
            if (material == null) {
                LOGGER.log(Level.WARNING, "更新 BOM 失敗: 物料ID {0} 不存在。", bom.getMaterialId());
                throw new IllegalArgumentException("指定的物料不存在。");
            }

            LOGGER.log(Level.INFO, "Service: 嘗試更新 BOM 項目，ID: {0}", bom.getBomId());
            return bomDao.updateBillOfMaterial(bom);
        }

        @Override
        public boolean deleteBillOfMaterial(int bomId) throws SQLException, IllegalArgumentException {
            // 業務邏輯：驗證 ID
            if (bomId <= 0) {
                LOGGER.log(Level.WARNING, "刪除 BOM 項目時ID無效: {0}", bomId);
                throw new IllegalArgumentException("BOM ID無效，必須大於0。");
            }
            // 檢查 BOM 項目是否存在
            BillOfMaterial existingBom = bomDao.getBillOfMaterialById(bomId);
            if (existingBom == null) {
                LOGGER.log(Level.WARNING, "刪除 BOM 項目失敗，BOM ID不存在: {0}", bomId);
                return false; // 如果 BOM 項目不存在，則認為刪除成功 (冪等性)
            }

            LOGGER.log(Level.INFO, "Service: 嘗試刪除 BOM 項目，ID: {0}", bomId);
            return bomDao.deleteBillOfMaterial(bomId);
        }

        @Override
        public boolean deleteBillOfMaterialsByProductId(int productId) throws SQLException, IllegalArgumentException {
            if (productId <= 0) {
                LOGGER.log(Level.WARNING, "刪除產品所有 BOM 時產品ID無效: {0}", productId);
                throw new IllegalArgumentException("產品ID無效，必須大於0。");
            }
            // 確保產品存在（可選，因為外鍵通常會處理這個）
            Product existingProduct = productDao.getProductById(productId);
            if (existingProduct == null) {
                LOGGER.log(Level.WARNING, "刪除產品所有 BOM 失敗，產品ID不存在: {0}", productId);
                return false;
            }

            LOGGER.log(Level.INFO, "Service: 嘗試刪除產品ID {0} 的所有 BOM 項目。", productId);
            return bomDao.deleteBillOfMaterialsByProductId(productId);
        }
    }
    