    package com.example.dao;

    import com.example.model.WorkOrder;
    import java.sql.SQLException;
    import java.util.List;

    /**
     * WorkOrderDao 介面定義了對 WorkOrder (生產工單) 資料實體的 CRUD (Create, Read, Update, Delete) 操作。
     */
    public interface WorkOrderDao {

        /**
         * 新增一個生產工單到資料庫。
         * @param workOrder 要新增的工單物件。
         * @return 新增工單後，資料庫自動生成的工單ID。
         * @throws SQLException 如果資料庫操作發生錯誤。
         */
        int addWorkOrder(WorkOrder workOrder) throws SQLException;

        /**
         * 根據工單ID從資料庫中獲取工單。
         * 查詢結果會包含產品名稱和產品代碼。
         * @param workOrderId 工單的唯一識別ID。
         * @return 匹配指定ID的工單物件，如果未找到則返回 null。
         * @throws SQLException 如果資料庫操作發生錯誤。
         */
        WorkOrder getWorkOrderById(int workOrderId) throws SQLException;

        /**
         * 從資料庫中獲取所有生產工單的列表。
         * 每個工單會包含其關聯的產品名稱和產品代碼。
         * @return 包含所有 WorkOrder 物件的列表。
         * @throws SQLException 如果資料庫操作發生錯誤。
         */
        List<WorkOrder> getAllWorkOrders() throws SQLException;

        /**
         * 根據工單狀態獲取生產工單列表。
         * @param status 要篩選的工單狀態。
         * @return 匹配指定狀態的 WorkOrder 物件列表。
         * @throws SQLException 如果資料庫操作發生錯誤。
         */
        List<WorkOrder> getWorkOrdersByStatus(String status) throws SQLException;

        /**
         * 根據產品ID獲取相關的生產工單列表。
         * @param productId 產品的ID。
         * @return 該產品相關的所有 WorkOrder 物件的列表。
         * @throws SQLException 如果資料庫操作發生錯誤。
         */
        List<WorkOrder> getWorkOrdersByProductId(int productId) throws SQLException;


        /**
         * 更新資料庫中現有的生產工單資訊。
         * @param workOrder 包含更新資訊的工單物件 (必須包含有效的工單ID)。
         * @return 如果更新成功返回 true，否則返回 false。
         * @throws SQLException 如果資料庫操作發生錯誤。
         */
        boolean updateWorkOrder(WorkOrder workOrder) throws SQLException;

        /**
         * 從資料庫中刪除指定ID的生產工單。
         * @param workOrderId 要刪除工單的ID。
         * @return 如果刪除成功返回 true，否則返回 false。
         * @throws SQLException 如果資料庫操作發生錯誤。
         */
        boolean deleteWorkOrder(int workOrderId) throws SQLException;
    }
    