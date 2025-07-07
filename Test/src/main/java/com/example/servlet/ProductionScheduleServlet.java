package com.example.servlet;

import com.example.model.ProductionSchedule;
import com.example.model.Product; // 需要 Product 類別來獲取產品列表
import com.example.model.WorkOrder; // 需要 WorkOrder 類別來獲取工單列表
import com.example.service.ProductionScheduleService;
import com.example.service.ProductService;
import com.example.service.WorkOrderService;
import com.example.service.impl.ProductionScheduleServiceImpl;
import com.example.service.impl.ProductServiceImpl;
import com.example.service.impl.WorkOrderServiceImpl;

import jakarta.servlet.RequestDispatcher;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.math.BigDecimal;
import java.sql.SQLException;
import java.text.ParseException;
import java.text.SimpleDateFormat; // 保持引入
import java.util.Date; // 使用 java.util.Date
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * ProductionScheduleServlet 處理生產排程明細的網頁請求。
 * 包括列出排程、新增、編輯、更新、刪除排程，以及開始/完成排程。
 * URL 映射: /productionschedules
 */
@WebServlet("/productionschedules")
public class ProductionScheduleServlet extends HttpServlet {

    private static final Logger LOGGER = Logger.getLogger(ProductionScheduleServlet.class.getName());
    private ProductionScheduleService productionScheduleService;
    private ProductService productService;
    private WorkOrderService workOrderService;

    @Override
    public void init() throws ServletException {
        super.init();
        this.productionScheduleService = new ProductionScheduleServiceImpl();
        this.productService = new ProductServiceImpl();
        this.workOrderService = new WorkOrderServiceImpl();
        LOGGER.info("ProductionScheduleServlet 已初始化。");
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        String action = request.getParameter("action");
        if (action == null || action.isEmpty()) {
            action = "list"; // 預設操作為列出所有排程
        }
        LOGGER.log(Level.INFO, "ProductionScheduleServlet 收到 GET 請求，動作: {0}", action);

        // 不再需要設定 activeSidebarLink，因為 main_layout.jsp 已移除
        // request.setAttribute("activeSidebarLink", "productionSchedules");

        try {
            switch (action) {
                case "new":
                    showNewProductionScheduleForm(request, response);
                    break;
                case "edit":
                    showEditProductionScheduleForm(request, response);
                    break;
                case "view":
                    viewProductionScheduleDetail(request, response);
                    break;
                case "delete":
                    deleteProductionSchedule(request, response);
                    break;
                case "byStatus":
                    listProductionSchedulesByStatus(request, response);
                    break;
                case "byDate":
                    listProductionSchedulesByDate(request, response);
                    break;
                case "startProduction":
                    startProductionSchedule(request, response);
                    break;
                case "completeSchedule":
                    completeProductionSchedule(request, response);
                    break;
                case "list":
                default:
                    listAllProductionSchedules(request, response);
                    break;
            }
        } catch (SQLException ex) {
            LOGGER.log(Level.SEVERE, "ProductionScheduleServlet 處理 GET 請求時發生資料庫錯誤: " + action, ex);
            request.setAttribute("errorMessage", "資料庫操作失敗：" + ex.getMessage());
            // 直接轉發到錯誤頁面或包含錯誤訊息的當前頁面
            RequestDispatcher dispatcher = request.getRequestDispatcher("/error.jsp"); // 或者轉發回 productionScheduleDetail.jsp 顯示錯誤
            dispatcher.forward(request, response);
        } catch (IllegalArgumentException ex) {
            LOGGER.log(Level.WARNING, "ProductionScheduleServlet 收到 GET 請求的無效參數: " + action, ex);
            request.setAttribute("errorMessage", "請求參數無效：" + ex.getMessage());
            // 直接轉發到錯誤頁面或包含錯誤訊息的當前頁面
            RequestDispatcher dispatcher = request.getRequestDispatcher("/error.jsp"); // 或者轉發回 productionScheduleDetail.jsp 顯示錯誤
            dispatcher.forward(request, response);
        } catch (IllegalStateException ex) {
            LOGGER.log(Level.WARNING, "ProductionScheduleServlet 處理 GET 請求時發生業務邏輯錯誤: " + action, ex);
            // 對於業務邏輯錯誤，通常重定向到列表頁並顯示錯誤訊息
            request.getSession().setAttribute("errorMessage", "操作失敗：" + ex.getMessage());
            response.sendRedirect(request.getContextPath() + "/productionschedules?action=list");
        }
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        String action = request.getParameter("action");
        if (action == null || action.isEmpty()) {
            action = "list";
        }
        LOGGER.log(Level.INFO, "ProductionScheduleServlet 收到 POST 請求，動作: {0}", action);
        request.setCharacterEncoding("UTF-8"); // 確保正確處理中文參數

        try {
            switch (action) {
                case "add":
                    insertProductionSchedule(request, response);
                    break;
                case "update":
                    updateProductionSchedule(request, response);
                    break;
                default:
                    response.sendError(HttpServletResponse.SC_NOT_FOUND);
                    LOGGER.log(Level.WARNING, "未知的 POST 動作: {0}", action);
                    break;
            }
        } catch (SQLException ex) {
            LOGGER.log(Level.SEVERE, "ProductionScheduleServlet 處理 POST 請求時發生資料庫錯誤: " + action, ex);
            request.setAttribute("errorMessage", "資料庫操作失敗：" + ex.getMessage());
            handlePostErrorForward(request, response, action);
        } catch (IllegalArgumentException ex) {
            LOGGER.log(Level.WARNING, "ProductionScheduleServlet 收到 POST 請求的無效參數: " + action, ex);
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
        // 準備產品列表和工單列表，用於下拉選單回填
        try {
            request.setAttribute("products", productService.getAllProducts());
            request.setAttribute("workOrders", workOrderService.getAllWorkOrders());
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "錯誤：無法載入產品或工單列表以供表單回填。", e);
            request.setAttribute("errorMessage", (request.getAttribute("errorMessage") != null ? request.getAttribute("errorMessage") + " " : "") + " (無法載入列表)");
        }

        ProductionSchedule schedule = new ProductionSchedule();
        try {
            String scheduleIdParam = request.getParameter("scheduleId");
            if (scheduleIdParam != null && !scheduleIdParam.isEmpty()) {
                schedule.setScheduleId(parseIntParam(scheduleIdParam, "排程ID"));
            }
            // 處理 workOrderId，允許為空
            String workOrderIdParam = request.getParameter("workOrderId");
            if (workOrderIdParam != null && !workOrderIdParam.trim().isEmpty()) {
                schedule.setWorkOrderId(parseIntParam(workOrderIdParam, "工單ID"));
            } else {
                schedule.setWorkOrderId(null);
            }

            schedule.setProductId(parseIntParam(request.getParameter("productId"), "產品ID"));
            schedule.setScheduledDate(parseUtilDate(request.getParameter("scheduledDate"), "排程日期", false)); // 確保使用新的 SimpleDateFormat 實例
            schedule.setShift(request.getParameter("shift"));
            schedule.setPlannedQuantity(parseBigDecimal(request.getParameter("plannedQuantity"), "計劃數量"));
            
            // 實際數量可為空
            String actualQuantityParam = request.getParameter("actualQuantity");
            if (actualQuantityParam != null && !actualQuantityParam.trim().isEmpty()) {
                schedule.setActualQuantity(parseBigDecimal(actualQuantityParam, "實際數量"));
            } else {
                schedule.setActualQuantity(null);
            }
            
            schedule.setStatus(request.getParameter("status"));
            schedule.setNotes(request.getParameter("notes"));

        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "錯誤：回填 ProductionSchedule 物件時發生問題。", e);
            if (request.getAttribute("errorMessage") == null) {
                request.setAttribute("errorMessage", "重新載入表單時發生錯誤: " + e.getMessage());
            }
        }

        request.setAttribute("schedule", schedule);
        request.setAttribute("action", action.equals("add") ? "new" : "edit");
        // 直接轉發到 productionScheduleDetail.jsp
        RequestDispatcher dispatcher = request.getRequestDispatcher("/production/productionScheduleDetail.jsp");
        dispatcher.forward(request, response);
    }


    /**
     * 從服務層獲取所有生產排程並轉發到 productionScheduleDetail.jsp。
     * @param request HttpServletRequest
     * @param response HttpServletResponse
     * @throws SQLException 如果發生資料庫存取錯誤
     * @throws ServletException 如果發生 Servlet 特定的錯誤
     * @throws IOException 如果發生 I/O 錯誤
     */
    private void listAllProductionSchedules(HttpServletRequest request, HttpServletResponse response) throws SQLException, ServletException, IOException {
        List<ProductionSchedule> schedules = productionScheduleService.getAllProductionSchedules();
        LOGGER.log(Level.INFO, "從服務層獲取到 {0} 個生產排程。", (schedules != null ? schedules.size() : "0"));

        request.setAttribute("schedules", schedules);
        request.setAttribute("action", "list");
        // pageTitle 在 JSP 內部設定
        // request.setAttribute("pageTitle", "生產排程明細列表"); 
        // 直接轉發到 productionScheduleDetail.jsp
        RequestDispatcher dispatcher = request.getRequestDispatcher("/production/productionScheduleDetail.jsp");
        dispatcher.forward(request, response);
    }

    /**
     * 根據狀態從服務層獲取生產排程並轉發到 productionScheduleDetail.jsp。
     * @param request HttpServletRequest
     * @param response HttpServletResponse
     * @throws SQLException 如果發生資料庫存取錯誤
     * @throws ServletException 如果發生 Servlet 特定的錯誤
     * @throws IOException 如果發生 I/O 錯誤
     * @throws IllegalArgumentException 如果狀態參數無效
     */
    private void listProductionSchedulesByStatus(HttpServletRequest request, HttpServletResponse response) throws SQLException, ServletException, IOException, IllegalArgumentException {
        String status = request.getParameter("status");
        List<ProductionSchedule> schedules;
        if (status != null && !status.trim().isEmpty()) {
            schedules = productionScheduleService.getProductionSchedulesByStatus(status);
            LOGGER.log(Level.INFO, "獲取到狀態為 '{0}' 的 {1} 個生產排程。", new Object[]{status, schedules.size()});
        } else {
            listAllProductionSchedules(request, response); // 如果狀態為空，則顯示所有排程
            return;
        }

        request.setAttribute("schedules", schedules);
        request.setAttribute("action", "byStatus");
        // pageTitle 在 JSP 內部設定
        // request.setAttribute("pageTitle", "生產排程明細 (狀態: " + status + ")");
        // 直接轉發到 productionScheduleDetail.jsp
        RequestDispatcher dispatcher = request.getRequestDispatcher("/production/productionScheduleDetail.jsp");
        dispatcher.forward(request, response);
    }

    /**
     * 根據日期從服務層獲取生產排程並轉發到 productionScheduleDetail.jsp。
     * @param request HttpServletRequest
     * @param response HttpServletResponse
     * @throws SQLException 如果發生資料庫存取錯誤
     * @throws ServletException 如果發生 Servlet 特定的錯誤
     * @throws IOException 如果發生 I/O 錯誤
     * @throws IllegalArgumentException 如果日期參數無效
     */
    private void listProductionSchedulesByDate(HttpServletRequest request, HttpServletResponse response) throws SQLException, ServletException, IOException, IllegalArgumentException {
        String dateParam = request.getParameter("scheduledDate");
        Date scheduledDate = parseUtilDate(dateParam, "排程日期", false); // 不允許為空
        List<ProductionSchedule> schedules = productionScheduleService.getProductionSchedulesByDate(scheduledDate);
        LOGGER.log(Level.INFO, "獲取到日期為 '{0}' 的 {1} 個生產排程。", new Object[]{scheduledDate, schedules.size()});

        request.setAttribute("schedules", schedules);
        request.setAttribute("action", "byDate");
        // pageTitle 在 JSP 內部設定
        // request.setAttribute("pageTitle", "生產排程明細 (日期: " + dateParam + ")");
        // 直接轉發到 productionScheduleDetail.jsp
        RequestDispatcher dispatcher = request.getRequestDispatcher("/production/productionScheduleDetail.jsp");
        dispatcher.forward(request, response);
    }


    /**
     * 顯示新增生產排程的表單，並準備產品和工單列表供選擇。
     * @param request HttpServletRequest
     * @param response HttpServletResponse
     * @throws SQLException 如果發生資料庫存取錯誤
     * @throws ServletException 如果發生 Servlet 特定的錯誤
     * @throws IOException 如果發生 I/O 錯誤
     */
    private void showNewProductionScheduleForm(HttpServletRequest request, HttpServletResponse response) throws SQLException, ServletException, IOException {
        List<Product> products = productService.getAllProducts();
        List<WorkOrder> workOrders = workOrderService.getAllWorkOrders(); // 獲取所有工單用於下拉選單
        request.setAttribute("products", products);
        request.setAttribute("workOrders", workOrders);
        request.setAttribute("action", "new");
        // pageTitle 在 JSP 內部設定
        // request.setAttribute("pageTitle", "新增生產排程明細");
        // 直接轉發到 productionScheduleDetail.jsp
        RequestDispatcher dispatcher = request.getRequestDispatcher("/production/productionScheduleDetail.jsp");
        dispatcher.forward(request, response);
    }

    /**
     * 顯示編輯現有生產排程的表單，並填充表單字段。
     * @param request HttpServletRequest
     * @param response HttpServletResponse
     * @throws SQLException 如果發生資料庫存取錯誤
     * @throws ServletException 如果發生 Servlet 特定的錯誤
     * @throws IOException 如果發生 I/O 錯誤
     * @throws IllegalArgumentException 如果排程ID參數無效
     */
    private void showEditProductionScheduleForm(HttpServletRequest request, HttpServletResponse response) throws SQLException, ServletException, IOException, IllegalArgumentException {
        String idParam = request.getParameter("id");
        int scheduleId = parseIntParam(idParam, "排程ID");

        ProductionSchedule schedule = productionScheduleService.getProductionScheduleById(scheduleId);
        if (schedule != null) {
            List<Product> products = productService.getAllProducts();
            List<WorkOrder> workOrders = workOrderService.getAllWorkOrders();
            request.setAttribute("products", products);
            request.setAttribute("workOrders", workOrders);
            request.setAttribute("schedule", schedule);
            request.setAttribute("action", "edit");
            // pageTitle 在 JSP 內部設定
            // request.setAttribute("pageTitle", "編輯生產排程明細");
            // 直接轉發到 productionScheduleDetail.jsp
            RequestDispatcher dispatcher = request.getRequestDispatcher("/production/productionScheduleDetail.jsp");
            dispatcher.forward(request, response);
            LOGGER.log(Level.INFO, "顯示編輯生產排程表單，ID: {0}", scheduleId);
        } else {
            LOGGER.log(Level.WARNING, "要編輯的生產排程未找到，ID: {0}", scheduleId);
            request.setAttribute("errorMessage", "要編輯的生產排程未找到。");
            // 直接轉發到錯誤頁面或包含錯誤訊息的當前頁面
            RequestDispatcher dispatcher = request.getRequestDispatcher("/error.jsp"); // 或者轉發回 productionScheduleDetail.jsp 顯示錯誤
            dispatcher.forward(request, response);
        }
    }

    /**
     * 查看單個生產排程的詳細資訊。
     * @param request HttpServletRequest
     * @param response HttpServletResponse
     * @throws SQLException 如果發生資料庫存取錯誤
     * @throws ServletException 如果發生 Servlet 特定的錯誤
     * @throws IOException 如果發生 I/O 錯誤
     * @throws IllegalArgumentException 如果排程ID參數無效
     */
    private void viewProductionScheduleDetail(HttpServletRequest request, HttpServletResponse response) throws SQLException, ServletException, IOException, IllegalArgumentException {
        String idParam = request.getParameter("id");
        int scheduleId = parseIntParam(idParam, "排程ID");

        ProductionSchedule schedule = productionScheduleService.getProductionScheduleById(scheduleId);
        if (schedule != null) {
            request.setAttribute("schedule", schedule);
            request.setAttribute("action", "view");
            // pageTitle 在 JSP 內部設定
            // request.setAttribute("pageTitle", "生產排程明細詳情");
            // 直接轉發到 productionScheduleDetail.jsp
            RequestDispatcher dispatcher = request.getRequestDispatcher("/production/productionScheduleDetail.jsp");
            dispatcher.forward(request, response);
            LOGGER.log(Level.INFO, "顯示生產排程詳情，ID: {0}", scheduleId);
        } else {
            LOGGER.log(Level.WARNING, "要查看的生產排程未找到，ID: {0}", scheduleId);
            request.setAttribute("errorMessage", "要查看的生產排程未找到。");
            // 直接轉發到錯誤頁面或包含錯誤訊息的當前頁面
            RequestDispatcher dispatcher = request.getRequestDispatcher("/error.jsp"); // 或者轉發回 productionScheduleDetail.jsp 顯示錯誤
            dispatcher.forward(request, response);
        }
    }

    /**
     * 將新生產排程插入資料庫。
     * @param request HttpServletRequest
     * @param response HttpServletResponse
     * @throws SQLException 如果發生資料庫存取錯誤
     * @throws IOException 如果發生 I/O 錯誤
     * @throws IllegalArgumentException 如果排程數據無效
     */
    private void insertProductionSchedule(HttpServletRequest request, HttpServletResponse response) throws SQLException, IOException, IllegalArgumentException {
        ProductionSchedule newSchedule = populateProductionScheduleFromRequest(request);

        productionScheduleService.addProductionSchedule(newSchedule);
        LOGGER.log(Level.INFO, "成功新增生產排程，ID: {0}", newSchedule.getScheduleId());
        request.getSession().setAttribute("message", "生產排程已成功新增。");
        response.sendRedirect(request.getContextPath() + "/productionschedules?action=list");
    }

    /**
     * 更新資料庫中的現有生產排程。
     * @param request HttpServletRequest
     * @param response HttpServletResponse
     * @throws SQLException 如果發生資料庫存取錯誤
     * @throws IOException 如果發生 I/O 錯誤
     * @throws IllegalArgumentException 如果排程數據無效或排程ID缺失/無效
     */
    private void updateProductionSchedule(HttpServletRequest request, HttpServletResponse response) throws SQLException, IOException, IllegalArgumentException {
        int scheduleId = parseIntParam(request.getParameter("scheduleId"), "排程ID");

        ProductionSchedule updatedSchedule = populateProductionScheduleFromRequest(request);
        updatedSchedule.setScheduleId(scheduleId);

        productionScheduleService.updateProductionSchedule(updatedSchedule);
        LOGGER.log(Level.INFO, "成功更新生產排程，ID: {0}", scheduleId);
        request.getSession().setAttribute("message", "生產排程已成功更新。");
        response.sendRedirect(request.getContextPath() + "/productionschedules?action=list");
    }

    /**
     * 從資料庫中刪除生產排程。
     * @param request HttpServletRequest
     * @param response HttpServletResponse
     * @throws SQLException 如果發生資料庫存取錯誤
     * @throws IOException 如果發生 I/O 錯誤
     * @throws IllegalArgumentException 如果排程ID參數無效
     */
    private void deleteProductionSchedule(HttpServletRequest request, HttpServletResponse response) throws SQLException, IOException, IllegalArgumentException {
        String idParam = request.getParameter("id");
        int scheduleId = parseIntParam(idParam, "排程ID");

        boolean deleted = false;
        try {
            deleted = productionScheduleService.deleteProductionSchedule(scheduleId);
            if (deleted) {
                LOGGER.log(Level.INFO, "成功刪除生產排程，ID: {0}", scheduleId);
                request.getSession().setAttribute("message", "生產排程已成功刪除。");
            } else {
                LOGGER.log(Level.WARNING, "刪除生產排程失敗或排程不存在，ID: {0}", scheduleId);
                request.getSession().setAttribute("errorMessage", "刪除生產排程失敗或排程不存在，ID: '" + scheduleId + "'。");
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "刪除排程ID {0} 時發生資料庫錯誤: {1}", new Object[]{scheduleId, e.getMessage()});
            request.getSession().setAttribute("errorMessage", "資料庫錯誤，無法刪除排程: " + e.getMessage());
        } catch (IllegalArgumentException e) {
            LOGGER.log(Level.WARNING, "刪除排程ID {0} 時參數無效: {1}", new Object[]{scheduleId, e.getMessage()});
            request.getSession().setAttribute("errorMessage", "參數錯誤，無法刪除排程: " + e.getMessage());
        }

        response.sendRedirect(request.getContextPath() + "/productionschedules?action=list");
    }

    /**
     * 將排程狀態設置為 "In Production"。
     * @param request HttpServletRequest
     * @param response HttpServletResponse
     * @throws SQLException If a database access error occurs
     * @throws IOException If an I/O error occurs
     * @throws IllegalArgumentException If the schedule ID is invalid
     * @throws IllegalStateException If the schedule status does not allow starting
     */
    private void startProductionSchedule(HttpServletRequest request, HttpServletResponse response) throws SQLException, IOException, IllegalArgumentException, IllegalStateException {
        String idParam = request.getParameter("id");
        int scheduleId = parseIntParam(idParam, "排程ID");

        try {
            productionScheduleService.startProduction(scheduleId);
            LOGGER.log(Level.INFO, "生產排程 ID {0} 已成功開始生產。", scheduleId);
            request.getSession().setAttribute("message", "生產排程已成功開始生產。");
        } catch (IllegalStateException e) {
            LOGGER.log(Level.WARNING, "無法開始生產排程 ID {0}: {1}", new Object[]{scheduleId, e.getMessage()});
            request.getSession().setAttribute("errorMessage", e.getMessage());
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "開始生產排程 ID {0} 時發生資料庫錯誤: {1}", new Object[]{scheduleId, e.getMessage()});
            request.getSession().setAttribute("errorMessage", "資料庫錯誤，無法開始生產排程: " + e.getMessage());
        } catch (IllegalArgumentException e) {
            LOGGER.log(Level.WARNING, "開始生產排程 ID {0} 時參數無效: {1}", new Object[]{scheduleId, e.getMessage()});
            request.getSession().setAttribute("errorMessage", "參數錯誤，無法開始生產排程: " + e.getMessage());
        }

        response.sendRedirect(request.getContextPath() + "/productionschedules?action=view&id=" + scheduleId);
    }

    /**
     * 更新排程的實際完成數量並將狀態設置為 "Completed"。
     * @param request HttpServletRequest
     * @param response HttpServletResponse
     * @throws SQLException If a database access error occurs
     * @throws IOException If an I/O error occurs
     * @throws IllegalArgumentException If the schedule ID or actual quantity is invalid
     * @throws IllegalStateException If the schedule status does not allow completion
     */
    private void completeProductionSchedule(HttpServletRequest request, HttpServletResponse response) throws SQLException, IOException, IllegalArgumentException, IllegalStateException {
        String idParam = request.getParameter("id");
        int scheduleId = parseIntParam(idParam, "排程ID");
        String actualQuantityParam = request.getParameter("actualQuantity"); // 獲取實際數量參數

        BigDecimal actualQuantity = null;
        if (actualQuantityParam != null && !actualQuantityParam.trim().isEmpty()) {
            actualQuantity = parseBigDecimal(actualQuantityParam, "實際數量");
        } else {
             throw new IllegalArgumentException("完成排程需要提供實際生產數量。");
        }

        try {
            productionScheduleService.completeProductionSchedule(scheduleId, actualQuantity);
            LOGGER.log(Level.INFO, "生產排程 ID {0} 已成功完成，實際數量: {1}", new Object[]{scheduleId, actualQuantity});
            request.getSession().setAttribute("message", "生產排程已成功完成。");
        } catch (IllegalStateException e) {
            LOGGER.log(Level.WARNING, "無法完成生產排程 ID {0}: {1}", new Object[]{scheduleId, e.getMessage()});
            request.getSession().setAttribute("errorMessage", e.getMessage());
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "完成生產排程 ID {0} 時發生資料庫錯誤: {1}", new Object[]{scheduleId, e.getMessage()});
            request.getSession().setAttribute("errorMessage", "資料庫錯誤，無法完成生產排程: " + e.getMessage());
        } catch (IllegalArgumentException e) {
            LOGGER.log(Level.WARNING, "完成生產排程 ID {0} 時參數無效: {1}", new Object[]{scheduleId, e.getMessage()});
            request.getSession().setAttribute("errorMessage", "參數錯誤，無法完成生產排程: " + e.getMessage());
        }

        response.sendRedirect(request.getContextPath() + "/productionschedules?action=view&id=" + scheduleId);
    }


    /**
     * 從 HttpServletRequest 中提取參數並構建一個 ProductionSchedule 物件。
     * 此輔助方法簡化了新增/更新操作的參數解析和驗證。
     * @param request HttpServletRequest
     * @return 填充好的 ProductionSchedule 物件
     * @throws IllegalArgumentException 如果參數無效或缺少必填字段
     */
    private ProductionSchedule populateProductionScheduleFromRequest(HttpServletRequest request) throws IllegalArgumentException {
        ProductionSchedule schedule = new ProductionSchedule();

        // workOrderId 是可選的，可能為空
        String workOrderIdParam = request.getParameter("workOrderId");
        if (workOrderIdParam != null && !workOrderIdParam.trim().isEmpty()) {
            schedule.setWorkOrderId(parseIntParam(workOrderIdParam, "工單ID"));
        } else {
            schedule.setWorkOrderId(null); // 確保如果為空，則設置為 null
        }
        
        schedule.setProductId(parseIntParam(request.getParameter("productId"), "產品ID"));
        schedule.setScheduledDate(parseUtilDate(request.getParameter("scheduledDate"), "排程日期", false)); // 確保使用新的 SimpleDateFormat 實例
        
        String shift = request.getParameter("shift");
        if (shift == null || shift.trim().isEmpty()) {
            throw new IllegalArgumentException("班次為必填項。");
        }
        schedule.setShift(shift);

        schedule.setPlannedQuantity(parseBigDecimal(request.getParameter("plannedQuantity"), "計劃數量"));
        
        // actualQuantity 是可選的，可能為空
        String actualQuantityParam = request.getParameter("actualQuantity");
        if (actualQuantityParam != null && !actualQuantityParam.trim().isEmpty()) {
            schedule.setActualQuantity(parseBigDecimal(actualQuantityParam, "實際數量"));
        } else {
            schedule.setActualQuantity(null); // 確保如果為空，則設置為 null
        }

        String status = request.getParameter("status");
        if (status == null || status.trim().isEmpty()) {
            throw new IllegalArgumentException("狀態為必填項。");
        }
        schedule.setStatus(status);
        schedule.setNotes(request.getParameter("notes"));

        return schedule;
    }

    /**
     * 輔助方法：將字符串轉換為 BigDecimal 並處理潛在錯誤。
     * @param value 要轉換的字符串值。
     * @param fieldName 字段名稱，用於錯誤訊息。
     * @return 轉換後的 BigDecimal 值。
     * @throws IllegalArgumentException 如果值為空、格式無效或小於零（對於數量）。
     */
    private BigDecimal parseBigDecimal(String value, String fieldName) throws IllegalArgumentException {
        if (value == null || value.trim().isEmpty()) {
            throw new IllegalArgumentException("'" + fieldName + "' 字段不能為空。");
        }
        try {
            BigDecimal decimalValue = new BigDecimal(value);
            // 對於數量，通常不允許負數，但允許為零（例如實際數量可能為零）
            if (decimalValue.compareTo(BigDecimal.ZERO) < 0) { // < 0 表示負數
                throw new IllegalArgumentException("'" + fieldName + "' 字段不能為負數。");
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
     * 輔助方法：將字符串轉換為 java.util.Date 並處理潛在錯誤，可選是否允許為空。
     * @param value 要轉換的字符串值 (期望格式為YYYY-MM-DD)。
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
            // 每次調用時創建新的 SimpleDateFormat 實例以確保執行緒安全
            SimpleDateFormat dateFormatter = new SimpleDateFormat("yyyy-MM-dd");
            return dateFormatter.parse(value);
        } catch (ParseException e) {
            throw new IllegalArgumentException("日期格式無效: '" + fieldName + "' 字段必須是YYYY-MM-DD 格式。", e);
        }
    }
}
