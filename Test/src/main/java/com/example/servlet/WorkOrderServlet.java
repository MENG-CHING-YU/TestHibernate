package com.example.servlet; // 根據您的專案結構，Servlet 通常放在 'servlet' 包下

import com.example.model.WorkOrder;
import com.example.model.Product; // 需要導入 Product 類別以獲取產品列表
import com.example.service.WorkOrderService;
import com.example.service.ProductService; // 導入 ProductService
import com.example.service.impl.WorkOrderServiceImpl;
import com.example.service.impl.ProductServiceImpl; // 導入 ProductServiceImpl

import jakarta.servlet.RequestDispatcher;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.math.BigDecimal;
import java.sql.SQLException;
import java.text.ParseException; // 用於日期解析
import java.text.SimpleDateFormat; // 用於日期格式化和解析
import java.util.Date; // <-- 使用 java.util.Date
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * WorkOrderServlet 處理生產工單的網頁請求。
 * 包括列出工單、新增、編輯、更新、刪除工單，以及開始/完成工單。
 * URL 映射: /workorders
 */
@WebServlet("/workorders") // 定義 Servlet 的 URL 映射
public class WorkOrderServlet extends HttpServlet {

    private static final Logger LOGGER = Logger.getLogger(WorkOrderServlet.class.getName());
    private WorkOrderService workOrderService;
    private ProductService productService; // 用於獲取產品列表，供下拉選單使用

    // 日期格式化器，用於解析和格式化 HTML date input 類型 (YYYY-MM-DD)
    private static final SimpleDateFormat DATE_FORMATTER = new SimpleDateFormat("yyyy-MM-dd");

    @Override
    public void init() throws ServletException {
        super.init();
        this.workOrderService = new WorkOrderServiceImpl();
        this.productService = new ProductServiceImpl(); // 初始化 ProductService
        LOGGER.info("WorkOrderServlet 已初始化。");
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        String action = request.getParameter("action"); // 從查詢參數獲取 action
        if (action == null || action.isEmpty()) {
            action = "list"; // 預設操作為列出所有工單
        }
        LOGGER.log(Level.INFO, "WorkOrderServlet 收到 GET 請求，動作: {0}", action);

        try {
            switch (action) {
                case "new": // 顯示新增工單表單
                    showNewWorkOrderForm(request, response);
                    break;
                case "edit": // 顯示編輯工單表單
                    showEditWorkOrderForm(request, response);
                    break;
                case "view": // 查看單個工單詳情
                    viewWorkOrderDetail(request, response);
                    break;
                case "delete": // 刪除工單
                    deleteWorkOrder(request, response);
                    break;
                case "byStatus": // 按狀態篩選工單
                    listWorkOrdersByStatus(request, response);
                    break;
                case "start": // 開始工單
                    startWorkOrder(request, response);
                    break;
                case "complete": // 完成工單
                    completeWorkOrder(request, response);
                    break;
                case "list": // 列出所有工單
                default:
                    listAllWorkOrders(request, response);
                    break;
            }
        } catch (SQLException ex) {
            LOGGER.log(Level.SEVERE, "WorkOrderServlet 處理 GET 請求時發生資料庫錯誤: " + action, ex);
            request.setAttribute("errorMessage", "資料庫操作失敗：" + ex.getMessage());
            RequestDispatcher dispatcher = request.getRequestDispatcher("/error.jsp");
            dispatcher.forward(request, response);
        } catch (IllegalArgumentException ex) {
            LOGGER.log(Level.WARNING, "WorkOrderServlet 收到 GET 請求的無效參數: " + action, ex);
            request.setAttribute("errorMessage", "請求參數無效：" + ex.getMessage());
            RequestDispatcher dispatcher = request.getRequestDispatcher("/error.jsp");
            dispatcher.forward(request, response);
        } catch (IllegalStateException ex) {
            LOGGER.log(Level.WARNING, "WorkOrderServlet 處理 GET 請求時發生業務邏輯錯誤: " + action, ex);
            request.setAttribute("errorMessage", "操作失敗：" + ex.getMessage());
            // 對於業務邏輯錯誤，通常返回到列表頁並顯示錯誤訊息
            response.sendRedirect(request.getContextPath() + "/workorders?action=list");
        }
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        String action = request.getParameter("action"); // 從表單參數獲取 action
        if (action == null || action.isEmpty()) {
            action = "list";
        }
        LOGGER.log(Level.INFO, "WorkOrderServlet 收到 POST 請求，動作: {0}", action);
        request.setCharacterEncoding("UTF-8"); // 確保正確處理中文參數

        try {
            switch (action) {
                case "add": // 提交新增工單
                    insertWorkOrder(request, response);
                    break;
                case "update": // 提交更新工單
                    updateWorkOrder(request, response);
                    break;
                default:
                    response.sendError(HttpServletResponse.SC_NOT_FOUND);
                    LOGGER.log(Level.WARNING, "未知的 POST 動作: {0}", action);
                    break;
            }
        } catch (SQLException ex) {
            LOGGER.log(Level.SEVERE, "WorkOrderServlet 處理 POST 請求時發生資料庫錯誤: " + action, ex);
            request.setAttribute("errorMessage", "資料庫操作失敗：" + ex.getMessage());
            handlePostErrorForward(request, response, action);
        } catch (IllegalArgumentException ex) {
            LOGGER.log(Level.WARNING, "WorkOrderServlet 收到 POST 請求的無效參數: " + action, ex);
            request.setAttribute("errorMessage", "提交的數據格式無效：" + ex.getMessage());
            handlePostErrorForward(request, response, action);
        }
    }

    /**
     * 處理 POST 請求中發生錯誤時的轉發邏輯，嘗試回填表單數據。
     *
     * @param request HttpServletRequest
     * @param response HttpServletResponse
     * @param action 當前正在處理的動作 (例如: "add", "update")
     * @throws ServletException 如果發生 Servlet 特定的錯誤
     * @throws IOException 如果發生 I/O 錯誤
     */
    private void handlePostErrorForward(HttpServletRequest request, HttpServletResponse response, String action) throws ServletException, IOException {
        // 準備產品列表，用於下拉選單回填
        try {
            request.setAttribute("products", productService.getAllProducts());
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "錯誤：無法載入產品列表以供表單回填。", e);
            request.setAttribute("errorMessage", (request.getAttribute("errorMessage") != null ? request.getAttribute("errorMessage") + " " : "") + " (無法載入產品列表)");
        }

        WorkOrder workOrder = new WorkOrder(); // 初始化一個新的工單物件
        try {
            String workOrderIdParam = request.getParameter("workOrderId");
            if (workOrderIdParam != null && !workOrderIdParam.isEmpty()) {
                workOrder.setWorkOrderId(parseIntParam(workOrderIdParam, "工單ID"));
            }
            workOrder.setWorkOrderNumber(request.getParameter("workOrderNumber"));
            workOrder.setProductId(parseIntParam(request.getParameter("productId"), "產品ID"));
            workOrder.setQuantity(parseBigDecimal(request.getParameter("quantity"), "數量"));
            workOrder.setUnit(request.getParameter("unit"));

            // 日期欄位回填，現在使用 parseUtilDate 方法
            workOrder.setScheduledStartDate(parseUtilDate(request.getParameter("scheduledStartDate"), "預計開始日期"));
            workOrder.setScheduledDueDate(parseUtilDate(request.getParameter("scheduledDueDate"), "預計完成日期"));
            workOrder.setActualStartDate(parseUtilDate(request.getParameter("actualStartDate"), "實際開始日期", true)); // true 允許為空
            workOrder.setActualCompletionDate(parseUtilDate(request.getParameter("actualCompletionDate"), "實際完成日期", true)); // true 允許為空

            workOrder.setStatus(request.getParameter("status"));
            workOrder.setNotes(request.getParameter("notes"));

        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "錯誤：回填工單物件時發生問題。", e);
            // Add a general error message if repopulation itself fails badly
            if (request.getAttribute("errorMessage") == null) {
                request.setAttribute("errorMessage", "重新載入表單時發生錯誤: " + e.getMessage());
            }
        }

        request.setAttribute("workOrder", workOrder); // 將重建的工單物件設置回請求
        request.setAttribute("action", action.equals("add") ? "new" : "edit"); // 設置動作以顯示正確的表單
        RequestDispatcher dispatcher = request.getRequestDispatcher("/production/workOrderList.jsp");
        dispatcher.forward(request, response);
    }


    /**
     * 從服務層獲取所有工單並轉發到 workOrderList.jsp。
     * @param request HttpServletRequest
     * @param response HttpServletResponse
     * @throws SQLException 如果發生資料庫存取錯誤
     * @throws ServletException 如果發生 Servlet 特定的錯誤
     * @throws IOException 如果發生 I/O 錯誤
     */
    private void listAllWorkOrders(HttpServletRequest request, HttpServletResponse response) throws SQLException, ServletException, IOException {
        List<WorkOrder> workOrders = workOrderService.getAllWorkOrders();
        LOGGER.log(Level.INFO, "從服務層獲取到 {0} 個工單。", (workOrders != null ? workOrders.size() : "0"));

        request.setAttribute("workOrders", workOrders);
        request.setAttribute("action", "list"); // 設置動作以顯示列表視圖
        request.setAttribute("pageTitle", "所有生產工單");
        RequestDispatcher dispatcher = request.getRequestDispatcher("/production/workOrderList.jsp");
        dispatcher.forward(request, response);
    }

    /**
     * 根據狀態從服務層獲取工單並轉發到 workOrderList.jsp。
     * @param request HttpServletRequest
     * @param response HttpServletResponse
     * @throws SQLException 如果發生資料庫存取錯誤
     * @throws ServletException 如果發生 Servlet 特定的錯誤
     * @throws IOException 如果發生 I/O 錯誤
     * @throws IllegalArgumentException 如果狀態參數無效
     */
    private void listWorkOrdersByStatus(HttpServletRequest request, HttpServletResponse response) throws SQLException, ServletException, IOException, IllegalArgumentException {
        String status = request.getParameter("status");
        List<WorkOrder> workOrders;
        if (status != null && !status.trim().isEmpty()) {
            workOrders = workOrderService.getWorkOrdersByStatus(status);
            LOGGER.log(Level.INFO, "獲取到狀態為 '{0}' 的 {1} 個工單。", new Object[]{status, workOrders.size()});
        } else {
            listAllWorkOrders(request, response); // 如果狀態為空，則顯示所有
            return;
        }

        request.setAttribute("workOrders", workOrders);
        request.setAttribute("action", "byStatus"); // 設置動作以顯示按狀態篩選視圖
        request.setAttribute("pageTitle", "生產工單 (狀態: " + status + ")");
        RequestDispatcher dispatcher = request.getRequestDispatcher("/production/workOrderList.jsp");
        dispatcher.forward(request, response);
    }

    /**
     * 顯示新增工單的表單，並準備產品列表供選擇。
     * @param request HttpServletRequest
     * @param response HttpServletResponse
     * @throws SQLException 如果發生資料庫存取錯誤
     * @throws ServletException 如果發生 Servlet 特定的錯誤
     * @throws IOException 如果發生 I/O 錯誤
     */
    private void showNewWorkOrderForm(HttpServletRequest request, HttpServletResponse response) throws SQLException, ServletException, IOException {
        List<Product> products = productService.getAllProducts(); // 獲取所有產品用於下拉選單
        request.setAttribute("products", products);
        request.setAttribute("action", "new"); // 設置動作以顯示新增表單
        request.setAttribute("pageTitle", "新增生產工單");
        RequestDispatcher dispatcher = request.getRequestDispatcher("/production/workOrderList.jsp");
        dispatcher.forward(request, response);
    }

    /**
     * 顯示編輯現有工單的表單，並填充表單字段和下拉選單。
     * @param request HttpServletRequest
     * @param response HttpServletResponse
     * @throws SQLException 如果發生資料庫存取錯誤
     * @throws ServletException 如果發生 Servlet 特定的錯誤
     * @throws IOException 如果發生 I/O 錯誤
     * @throws IllegalArgumentException 如果工單ID參數無效
     */
    private void showEditWorkOrderForm(HttpServletRequest request, HttpServletResponse response) throws SQLException, ServletException, IOException, IllegalArgumentException {
        String idParam = request.getParameter("id");
        int workOrderId = parseIntParam(idParam, "工單ID");

        WorkOrder workOrder = workOrderService.getWorkOrderById(workOrderId);
        if (workOrder != null) {
            List<Product> products = productService.getAllProducts(); // 獲取所有產品用於下拉選單
            request.setAttribute("products", products);
            request.setAttribute("workOrder", workOrder); // 設置工單物件到請求屬性
            request.setAttribute("action", "edit"); // 設置動作以顯示編輯表單
            request.setAttribute("pageTitle", "編輯生產工單");
            RequestDispatcher dispatcher = request.getRequestDispatcher("/production/workOrderList.jsp");
            dispatcher.forward(request, response);
            LOGGER.log(Level.INFO, "顯示編輯工單表單，工單ID: {0}", workOrderId);
        } else {
            LOGGER.log(Level.WARNING, "要編輯的工單未找到，ID: {0}", workOrderId);
            response.sendError(HttpServletResponse.SC_NOT_FOUND, "工單未找到，ID: " + workOrderId);
        }
    }

    /**
     * 查看單個工單的詳細資訊。
     * @param request HttpServletRequest
     * @param response HttpServletResponse
     * @throws SQLException 如果發生資料庫存取錯誤
     * @throws ServletException 如果發生 Servlet 特定的錯誤
     * @throws IOException 如果發生 I/O 錯誤
     * @throws IllegalArgumentException 如果工單ID參數無效
     */
    private void viewWorkOrderDetail(HttpServletRequest request, HttpServletResponse response) throws SQLException, ServletException, IOException, IllegalArgumentException {
        String idParam = request.getParameter("id");
        int workOrderId = parseIntParam(idParam, "工單ID");

        WorkOrder workOrder = workOrderService.getWorkOrderById(workOrderId);
        if (workOrder != null) {
            request.setAttribute("workOrder", workOrder);
            request.setAttribute("action", "view"); // 設置動作以顯示詳情視圖
            request.setAttribute("pageTitle", "工單詳情");
            RequestDispatcher dispatcher = request.getRequestDispatcher("/production/workOrderList.jsp");
            dispatcher.forward(request, response);
            LOGGER.log(Level.INFO, "顯示工單詳情，工單ID: {0}", workOrderId);
        } else {
            LOGGER.log(Level.WARNING, "要查看的工單未找到，ID: {0}", workOrderId);
            response.sendError(HttpServletResponse.SC_NOT_FOUND, "工單未找到，ID: " + workOrderId);
        }
    }

    /**
     * 將新工單插入資料庫。
     * @param request HttpServletRequest
     * @param response HttpServletResponse
     * @throws SQLException 如果發生資料庫存取錯誤
     * @throws IOException 如果發生 I/O 錯誤
     * @throws IllegalArgumentException 如果工單數據無效
     */
    private void insertWorkOrder(HttpServletRequest request, HttpServletResponse response) throws SQLException, IOException, IllegalArgumentException {
        WorkOrder newWorkOrder = populateWorkOrderFromRequest(request); // 輔助方法來填充工單物件

        workOrderService.addWorkOrder(newWorkOrder); // 調用服務層來新增工單
        LOGGER.log(Level.INFO, "成功新增工單: {0} (ID: {1})", new Object[]{newWorkOrder.getWorkOrderNumber(), newWorkOrder.getWorkOrderId()});
        request.getSession().setAttribute("message", "工單 '" + newWorkOrder.getWorkOrderNumber() + "' 已成功新增。");
        response.sendRedirect(request.getContextPath() + "/workorders?action=list"); // 新增成功後重定向到工單列表頁
    }

    /**
     * 更新資料庫中的現有工單。
     * @param request HttpServletRequest
     * @param response HttpServletResponse
     * @throws SQLException 如果發生資料庫存取錯誤
     * @throws IOException 如果發生 I/O 錯誤
     * @throws IllegalArgumentException 如果工單數據無效或工單ID缺失/無效
     */
    private void updateWorkOrder(HttpServletRequest request, HttpServletResponse response) throws SQLException, IOException, IllegalArgumentException {
        int workOrderId = parseIntParam(request.getParameter("workOrderId"), "工單ID");

        WorkOrder updatedWorkOrder = populateWorkOrderFromRequest(request);
        updatedWorkOrder.setWorkOrderId(workOrderId);

        workOrderService.updateWorkOrder(updatedWorkOrder); // 調用服務層來更新工單
        LOGGER.log(Level.INFO, "成功更新工單，ID: {0}", workOrderId);
        request.getSession().setAttribute("message", "工單 '" + updatedWorkOrder.getWorkOrderNumber() + "' 已成功更新。");
        response.sendRedirect(request.getContextPath() + "/workorders?action=list"); // 更新成功後重定向到工單列表頁
    }

    /**
     * 從資料庫中刪除工單。
     * @param request HttpServletRequest
     * @param response HttpServletResponse
     * @throws SQLException 如果發生資料庫存取錯誤
     * @throws IOException 如果發生 I/O 錯誤
     * @throws IllegalArgumentException 如果工單ID參數無效
     */
    private void deleteWorkOrder(HttpServletRequest request, HttpServletResponse response) throws SQLException, IOException, IllegalArgumentException {
        String idParam = request.getParameter("id");
        int workOrderId = parseIntParam(idParam, "工單ID");

        boolean deleted = false;
        try {
            deleted = workOrderService.deleteWorkOrder(workOrderId);
            if (deleted) {
                LOGGER.log(Level.INFO, "成功刪除工單，ID: {0}", workOrderId);
                request.getSession().setAttribute("message", "工單 '" + workOrderId + "' 已成功刪除。");
            } else {
                LOGGER.log(Level.WARNING, "刪除工單失敗或工單不存在，ID: {0}", workOrderId);
                request.getSession().setAttribute("errorMessage", "刪除工單失敗或工單不存在，ID: '" + workOrderId + "'。");
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "刪除工單ID {0} 時發生資料庫錯誤: {1}", new Object[]{workOrderId, e.getMessage()});
            request.getSession().setAttribute("errorMessage", "資料庫錯誤，無法刪除工單: " + e.getMessage());
        } catch (IllegalArgumentException e) {
            LOGGER.log(Level.WARNING, "刪除工單ID {0} 時參數無效: {1}", new Object[]{workOrderId, e.getMessage()});
            request.getSession().setAttribute("errorMessage", "參數錯誤，無法刪除工單: " + e.getMessage());
        }

        response.sendRedirect(request.getContextPath() + "/workorders?action=list"); // 重定向到工單列表頁
    }

    /**
     * 設置工單的實際開始日期，並將狀態變更為 "In Progress"。
     * @param request HttpServletRequest
     * @param response HttpServletResponse
     * @throws SQLException If a database access error occurs
     * @throws IOException If an I/O error occurs
     * @throws IllegalArgumentException If the work order ID or date parameter is invalid
     * @throws IllegalStateException If the work order status does not allow starting
     */
    private void startWorkOrder(HttpServletRequest request, HttpServletResponse response) throws SQLException, IOException, IllegalArgumentException, IllegalStateException {
        String idParam = request.getParameter("id");
        int workOrderId = parseIntParam(idParam, "工單ID");
        Date actualStartDate = new Date(); // 默認為當前日期

        try {
            workOrderService.startWorkOrder(workOrderId, actualStartDate);
            LOGGER.log(Level.INFO, "工單 ID {0} 已成功開始。", workOrderId);
            request.getSession().setAttribute("message", "工單已成功開始。");
        } catch (IllegalStateException e) {
            LOGGER.log(Level.WARNING, "無法開始工單 ID {0}: {1}", new Object[]{workOrderId, e.getMessage()});
            request.getSession().setAttribute("errorMessage", e.getMessage());
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "開始工單 ID {0} 時發生資料庫錯誤: {1}", new Object[]{workOrderId, e.getMessage()});
            request.getSession().setAttribute("errorMessage", "資料庫錯誤，無法開始工單: " + e.getMessage());
        } catch (IllegalArgumentException e) {
            LOGGER.log(Level.WARNING, "開始工單 ID {0} 時參數無效: {1}", new Object[]{workOrderId, e.getMessage()});
            request.getSession().setAttribute("errorMessage", "參數錯誤，無法開始工單: " + e.getMessage());
        }

        response.sendRedirect(request.getContextPath() + "/workorders?action=view&id=" + workOrderId); // 重定向回詳情頁
    }

    /**
     * 設置工單的實際完成日期，並將狀態變更為 "Completed"。
     * @param request HttpServletRequest
     * @param response HttpServletResponse
     * @throws SQLException If a database access error occurs
     * @throws IOException If an I/O error occurs
     * @throws IllegalArgumentException If the work order ID or date parameter is invalid
     * @throws IllegalStateException If the work order status does not allow completion
     */
    private void completeWorkOrder(HttpServletRequest request, HttpServletResponse response) throws SQLException, IOException, IllegalArgumentException, IllegalStateException {
        String idParam = request.getParameter("id");
        int workOrderId = parseIntParam(idParam, "工單ID");
        Date actualCompletionDate = new Date(); // 默認為當前日期

        try {
            workOrderService.completeWorkOrder(workOrderId, actualCompletionDate);
            LOGGER.log(Level.INFO, "工單 ID {0} 已成功完成。", workOrderId);
            request.getSession().setAttribute("message", "工單已成功完成。");
        } catch (IllegalStateException e) {
            LOGGER.log(Level.WARNING, "無法完成工單 ID {0}: {1}", new Object[]{workOrderId, e.getMessage()});
            request.getSession().setAttribute("errorMessage", e.getMessage());
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "完成工單 ID {0} 時發生資料庫錯誤: {1}", new Object[]{workOrderId, e.getMessage()});
            request.getSession().setAttribute("errorMessage", "資料庫錯誤，無法完成工單: " + e.getMessage());
        } catch (IllegalArgumentException e) {
            LOGGER.log(Level.WARNING, "完成工單 ID {0} 時參數無效: {1}", new Object[]{workOrderId, e.getMessage()});
            request.getSession().setAttribute("errorMessage", "參數錯誤，無法完成工單: " + e.getMessage());
        }

        response.sendRedirect(request.getContextPath() + "/workorders?action=view&id=" + workOrderId); // 重定向回詳情頁
    }


    /**
     * 從 HttpServletRequest 中提取參數並構建一個 WorkOrder 物件。
     * 此輔助方法簡化了新增/更新操作的參數解析和驗證。
     * @param request HttpServletRequest
     * @return 填充好的 WorkOrder 物件
     * @throws IllegalArgumentException 如果參數無效或缺少必填字段
     */
    private WorkOrder populateWorkOrderFromRequest(HttpServletRequest request) throws IllegalArgumentException {
        WorkOrder workOrder = new WorkOrder();

        String workOrderNumber = request.getParameter("workOrderNumber");
        if (workOrderNumber == null || workOrderNumber.trim().isEmpty()) {
            throw new IllegalArgumentException("工單編號為必填項。");
        }
        workOrder.setWorkOrderNumber(workOrderNumber);

        workOrder.setProductId(parseIntParam(request.getParameter("productId"), "產品ID"));
        workOrder.setQuantity(parseBigDecimal(request.getParameter("quantity"), "數量"));

        String unit = request.getParameter("unit");
        if (unit == null || unit.trim().isEmpty()) {
             throw new IllegalArgumentException("單位為必填項。");
        }
        workOrder.setUnit(unit);

        // 使用新的 parseUtilDate 方法
        workOrder.setScheduledStartDate(parseUtilDate(request.getParameter("scheduledStartDate"), "預計開始日期"));
        workOrder.setScheduledDueDate(parseUtilDate(request.getParameter("scheduledDueDate"), "預計完成日期"));
        workOrder.setStatus(request.getParameter("status"));
        workOrder.setNotes(request.getParameter("notes"));

        // actualStartDate 和 actualCompletionDate 允許為空
        workOrder.setActualStartDate(parseUtilDate(request.getParameter("actualStartDate"), "實際開始日期", true));
        workOrder.setActualCompletionDate(parseUtilDate(request.getParameter("actualCompletionDate"), "實際完成日期", true));

        return workOrder;
    }

    /**
     * 輔助方法：將字符串轉換為 BigDecimal 並處理潛在錯誤。
     * @param value 要轉換的字符串值。
     * @param fieldName 字段名稱，用於錯誤訊息。
     * @return 轉換後的 BigDecimal 值。
     * @throws IllegalArgumentException 如果值為空、格式無效或小於等於零。
     */
    private BigDecimal parseBigDecimal(String value, String fieldName) throws IllegalArgumentException {
        if (value == null || value.trim().isEmpty()) {
            throw new IllegalArgumentException("'" + fieldName + "' 字段不能為空。");
        }
        try {
            BigDecimal decimalValue = new BigDecimal(value);
            if (decimalValue.compareTo(BigDecimal.ZERO) <= 0) {
                throw new IllegalArgumentException("'" + fieldName + "' 字段必須大於零。");
            }
            return decimalValue;
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("數字格式無效: '" + fieldName + "' 字段必須是數字。", e);
        }
    }

    /**
     * 輔助方法：將字符串轉換為 int 並處理潛在錯誤。
     * @param value 要轉換的字符串值。
     * @param fieldName 字段名稱，用於錯誤訊息。
     * @return 轉換後的 int 值。
     * @throws IllegalArgumentException 如果值為空或格式無效。
     */
    private int parseIntParam(String value, String fieldName) throws IllegalArgumentException {
        if (value == null || value.trim().isEmpty()) {
            throw new IllegalArgumentException("'" + fieldName + "' 字段不能為空。");
        }
        try {
            return Integer.parseInt(value);
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("數字格式無效: '" + fieldName + "' 字段必須是整數。", e);
        }
    }

    /**
     * 輔助方法：將字符串轉換為 java.util.Date 並處理潛在錯誤。
     * @param value 要轉換的字符串值 (期望格式為 yyyy-MM-dd)。
     * @param fieldName 字段名稱，用於錯誤訊息。
     * @return 轉換後的 java.util.Date 值。
     * @throws IllegalArgumentException 如果值為空或格式無效。
     */
    private Date parseUtilDate(String value, String fieldName) throws IllegalArgumentException {
        return parseUtilDate(value, fieldName, false); // 預設不允許為空
    }

    /**
     * 輔助方法：將字符串轉換為 java.util.Date 並處理潛在錯誤，可選是否允許為空。
     * @param value 要轉換的字符串值 (期望格式為 yyyy-MM-dd)。
     * @param fieldName 字段名稱，用於錯誤訊息。
     * @param allowNull 是否允許值為空。
     * @return 轉換後的 java.util.Date 值，如果允許為空且值為空，則返回 null。
     * @throws IllegalArgumentException 如果值不允許為空且為空，或格式無效。
     */
    private Date parseUtilDate(String value, String fieldName, boolean allowNull) throws IllegalArgumentException {
        if (value == null || value.trim().isEmpty()) {
            if (allowNull) {
                return null;
            } else {
                throw new IllegalArgumentException("'" + fieldName + "' 字段不能為空。");
            }
        }
        try {
            // 注意：SimpleDateFormat 是非執行緒安全的，但在單一請求處理中，每次創建一個是安全的。
            // 為了更高的性能，可以使用 ThreadLocal<SimpleDateFormat> 或 joda-time/java.time (如果您能使用更高版本的 JSTL/EL)。
            return DATE_FORMATTER.parse(value);
        } catch (ParseException e) {
            throw new IllegalArgumentException("日期格式無效: '" + fieldName + "' 字段必須是YYYY-MM-DD 格式。", e);
        }
    }
}
