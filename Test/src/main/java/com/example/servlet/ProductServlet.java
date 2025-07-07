package com.example.servlet;

import com.example.model.Product;
import com.example.service.ProductService;
import com.example.service.impl.ProductServiceImpl;

import jakarta.servlet.RequestDispatcher;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.math.BigDecimal;
import java.sql.SQLException;
import java.util.List;

// 引入 SLF4J 進行日誌記錄
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * ProductServlet 處理產品主檔的網頁請求。
 * 包括列出產品、新增、編輯、更新和刪除產品。
 * URL 映射: /products
 */
@WebServlet("/products")
public class ProductServlet extends HttpServlet {

    // 使用 SLF4J 的 Logger
    private static final Logger LOGGER = LoggerFactory.getLogger(ProductServlet.class);
    private ProductService productService; // 依賴 ProductService

    @Override
    public void init() throws ServletException {
        super.init();
        // 初始化 ProductService 實例
        this.productService = new ProductServiceImpl();
        LOGGER.info("ProductServlet initialized.");
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        String action = request.getParameter("action");
        if (action == null || action.isEmpty()) {
            action = "list"; // 預設操作為列出所有產品
        }
        LOGGER.info("ProductServlet received GET request, action: {}", action);

        try {
            switch (action) {
                case "new": // 顯示新增產品表單
                    showNewProductForm(request, response);
                    break;
                case "edit": // 顯示編輯產品表單
                    showEditProductForm(request, response);
                    break;
                case "delete": // 刪除產品
                    deleteProduct(request, response);
                    break;
                case "byCategory": // 按類別篩選產品 (當前 Service 層未實現，這裡會退回顯示全部)
                    listProductsByCategory(request, response);
                    break;
                case "list": // 列出所有產品
                default:      // 任何其他未匹配的路徑，預設也列出所有產品
                    listAllProducts(request, response);
                    break;
            }
        } catch (SQLException ex) {
            LOGGER.error("ProductServlet encountered database error while processing GET request for action: {}", action, ex);
            request.setAttribute("errorMessage", "資料庫操作失敗：" + ex.getMessage());
            RequestDispatcher dispatcher = request.getRequestDispatcher("/error.jsp"); // 假設您有 error.jsp 頁面
            dispatcher.forward(request, response);
        } catch (IllegalArgumentException ex) {
            LOGGER.warn("ProductServlet received invalid parameter for GET request for action: {}", action, ex);
            request.setAttribute("errorMessage", "請求參數無效：" + ex.getMessage());
            RequestDispatcher dispatcher = request.getRequestDispatcher("/error.jsp");
            dispatcher.forward(request, response);
        }
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        String action = request.getParameter("action");
        if (action == null || action.isEmpty()) {
            action = "list"; // 預設操作 (防止 null)
        }
        LOGGER.info("ProductServlet received POST request, action: {}", action);
        request.setCharacterEncoding("UTF-8"); // 確保正確處理中文參數

        try {
            switch (action) {
                case "add": // 新增產品的提交
                    insertProduct(request, response);
                    break;
                case "update": // 更新產品的提交
                    updateProduct(request, response);
                    break;
                default:
                    response.sendError(HttpServletResponse.SC_NOT_FOUND);
                    LOGGER.warn("Unknown POST action: {}", action);
                    break;
            }
        } catch (SQLException ex) {
            LOGGER.error("ProductServlet encountered database error while processing POST request for action: {}", action, ex);
            request.setAttribute("errorMessage", "資料庫操作失敗：" + ex.getMessage());
            handlePostErrorForward(request, response, action); // 處理錯誤並嘗試回填表單
        } catch (IllegalArgumentException ex) {
            LOGGER.warn("ProductServlet received invalid parameter for POST request for action: {}", action, ex);
            request.setAttribute("errorMessage", "提交的數據格式無效：" + ex.getMessage());
            handlePostErrorForward(request, response, action); // 處理錯誤並嘗試回填表單
        } catch (IllegalStateException ex) { // 處理 deleteProduct 可能拋出的 IllegalStateException
            LOGGER.warn("ProductServlet encountered business rule violation for POST request (likely delete): {}", action, ex);
            request.setAttribute("errorMessage", "業務規則限制：" + ex.getMessage());
            handlePostErrorForward(request, response, action); // 處理錯誤並嘗試回填表單
        }
    }

    /**
     * 處理 POST 請求中發生錯誤時的轉發邏輯，嘗試回填表單數據。
     * This method attempts to reconstruct the Product object from the request parameters
     * and forwards back to the productList.jsp page with the error message and populated form data.
     *
     * @param request HttpServletRequest
     * @param response HttpServletResponse
     * @param action The current action being processed (e.g., "add", "update")
     * @throws ServletException If a servlet-specific error occurs
     * @throws IOException If an I/O error occurs
     */
    private void handlePostErrorForward(HttpServletRequest request, HttpServletResponse response, String action) throws ServletException, IOException {
        Product product = new Product(); // Initialize a new product object

        try {
            // Attempt to populate product from request parameters, including ID if updating
            String productIdParam = request.getParameter("productId");
            if (productIdParam != null && !productIdParam.isEmpty()) {
                product.setProductId(parseIntParam(productIdParam, "產品ID"));
            }
            product.setProductCode(request.getParameter("productCode"));
            product.setProductName(request.getParameter("productName"));
            product.setCategory(request.getParameter("category"));
            product.setDescription(request.getParameter("description"));
            product.setUnit(request.getParameter("unit"));
            // For numeric fields, handle potential NumberFormatException during repopulation
            try {
                // parseBigDecimal 會檢查空值和格式，但這裡我們只是從 request 獲取，Service 層會做嚴格驗證
                String sellingPriceStr = request.getParameter("sellingPrice");
                if (sellingPriceStr != null && !sellingPriceStr.trim().isEmpty()) {
                    product.setSellingPrice(new BigDecimal(sellingPriceStr));
                }
            } catch (NumberFormatException e) {
                LOGGER.debug("Failed to parse sellingPrice during error forwarding: {}", request.getParameter("sellingPrice"));
                // 這裡不拋出，讓 request.setAttribute("errorMessage") 處理
            }
            try {
                String costStr = request.getParameter("cost");
                if (costStr != null && !costStr.trim().isEmpty()) {
                    product.setCost(new BigDecimal(costStr));
                }
            } catch (NumberFormatException e) {
                LOGGER.debug("Failed to parse cost during error forwarding: {}", request.getParameter("cost"));
                // 這裡不拋出
            }
            product.setActive("on".equalsIgnoreCase(request.getParameter("isActive")) || "true".equalsIgnoreCase(request.getParameter("isActive")));
            product.setImageUrl(request.getParameter("imageUrl"));

        } catch (Exception e) { // 捕捉任何異常，防止在錯誤處理中再次出錯
            LOGGER.error("Error repopulating product object for error forwarding.", e);
            // 不將此錯誤傳遞給用戶，因為已經有主錯誤信息
        }

        request.setAttribute("product", product); // 將重新構建的產品對象設置回請求
        request.setAttribute("pageTitle", (action.equals("add") ? "新增" : "編輯") + "產品");
        // 根據 POST 的 action 來判斷應該轉發到哪個表單模式
        request.setAttribute("action", action.equals("add") ? "new" : "edit");
        RequestDispatcher dispatcher = request.getRequestDispatcher("/production/productList.jsp");
        dispatcher.forward(request, response);
    }

    /**
     * Retrieves all products from the service layer and forwards them to productList.jsp.
     * @param request HttpServletRequest
     * @param response HttpServletResponse
     * @throws SQLException If a database access error occurs (propagated from Service)
     * @throws ServletException If a servlet-specific error occurs
     * @throws IOException If an I/O error occurs
     */
    private void listAllProducts(HttpServletRequest request, HttpServletResponse response) throws SQLException, ServletException, IOException {
        List<Product> products = productService.getAllProducts();
        LOGGER.info("Retrieved {} products from Service layer.", (products != null ? products.size() : "0"));

        request.setAttribute("products", products);
        request.setAttribute("pageTitle", "產品主檔列表");
        request.setAttribute("action", "list"); // Set action for JSP to render list view
        RequestDispatcher dispatcher = request.getRequestDispatcher("/production/productList.jsp");
        dispatcher.forward(request, response);
    }

    /**
     * Retrieves products filtered by category from the service layer and forwards them to productList.jsp.
     * NOTE: Current ProductService.getAllProducts() doesn't support filtering by category.
     * You would need to add a method like `productService.getProductsByCategory(category)` in ProductService.
     * For now, it will fetch all and let frontend potentially filter or show all.
     * @param request HttpServletRequest
     * @param response HttpServletResponse
     * @throws SQLException If a database access error occurs (propagated from Service)
     * @throws ServletException If a servlet-specific error occurs
     * @throws IOException If an I/O error occurs
     */
    private void listProductsByCategory(HttpServletRequest request, HttpServletResponse response) throws SQLException, ServletException, IOException {
        String category = request.getParameter("category");
        LOGGER.info("Attempting to list products by category: {}", category);

        // TODO: 如果需要實際按類別篩選，需要 Service 層提供相應方法
        // List<Product> products = productService.getProductsByCategory(category);
        
        // 目前 Service 層沒有按類別篩選的方法，所以暫時返回所有產品
        // 或者您可以在前端 JSP 頁面利用這個 category 參數進行前端篩選顯示
        List<Product> products = productService.getAllProducts(); 
        
        request.setAttribute("products", products);
        request.setAttribute("categoryFilter", category); // 將 category 傳回 JSP，可以在篩選框中回填
        request.setAttribute("pageTitle", "產品主檔列表 (類別: " + (category != null ? category : "所有") + ")");
        request.setAttribute("action", "list"); // 依然顯示列表，只是帶有篩選參數
        RequestDispatcher dispatcher = request.getRequestDispatcher("/production/productList.jsp");
        dispatcher.forward(request, response);
    }

    /**
     * Displays the form for adding a new product.
     * @param request HttpServletRequest
     * @param response HttpServletResponse
     * @throws ServletException If a servlet-specific error occurs
     * @throws IOException If an I/O error occurs
     */
    private void showNewProductForm(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        request.setAttribute("pageTitle", "新增產品");
        request.setAttribute("action", "new"); // Set action for JSP to render new form
        // 當顯示新表單時，product 對象應為空，以便 JSP 知道這是新增
        request.setAttribute("product", new Product()); 
        RequestDispatcher dispatcher = request.getRequestDispatcher("/production/productList.jsp");
        dispatcher.forward(request, response);
    }

    /**
     * Displays the form for editing an existing product.
     * @param request HttpServletRequest
     * @param response HttpServletResponse
     * @throws SQLException If a database access error occurs (propagated from Service)
     * @throws ServletException If a servlet-specific error occurs
     * @throws IOException If an I/O error occurs
     * @throws IllegalArgumentException If the product ID parameter is invalid
     */
    private void showEditProductForm(HttpServletRequest request, HttpServletResponse response) throws SQLException, ServletException, IOException, IllegalArgumentException {
        String idParam = request.getParameter("id");
        int productId = parseIntParam(idParam, "產品ID"); // Use helper method to parse ID

        Product product = productService.getProductById(productId);
        if (product != null) {
            request.setAttribute("product", product); // Set the product object to request attributes
            request.setAttribute("pageTitle", "編輯產品");
            request.setAttribute("action", "edit"); // Set action for JSP to render edit form
            RequestDispatcher dispatcher = request.getRequestDispatcher("/production/productList.jsp");
            dispatcher.forward(request, response);
            LOGGER.info("Displaying edit product form for Product ID: {}", productId);
        } else {
            LOGGER.warn("Product to edit not found, ID: {}", productId);
            // 設置錯誤消息並重定向到列表頁
            request.getSession().setAttribute("errorMessage", "編輯產品失敗：未找到ID為 " + productId + " 的產品。");
            response.sendRedirect(request.getContextPath() + "/products?action=list"); 
        }
    }

    /**
     * Inserts a new product into the database.
     * @param request HttpServletRequest
     * @param response HttpServletResponse
     * @throws SQLException If a database access error occurs (propagated from Service)
     * @throws IOException If an I/O error occurs
     * @throws IllegalArgumentException If product data is invalid (propagated from Service)
     */
    private void insertProduct(HttpServletRequest request, HttpServletResponse response) throws SQLException, IOException, IllegalArgumentException {
        Product newProduct = populateProductFromRequest(request); // Use helper method to populate product object

        // addProduct 服務層現在返回 int (ID)，而不是 Product 物件
        int generatedId = productService.addProduct(newProduct); 
        newProduct.setProductId(generatedId); // 將生成的 ID 設定回 newProduct 物件 (用於日誌和訊息)

        LOGGER.info("Successfully added product: {} (ID: {})", newProduct.getProductName(), newProduct.getProductId());
        request.getSession().setAttribute("message", "產品 '" + newProduct.getProductName() + "' 已成功新增。");
        response.sendRedirect(request.getContextPath() + "/products?action=list"); // Redirect to product list page after successful add
    }

    /**
     * Updates an existing product in the database.
     * @param request HttpServletRequest
     * @param response HttpServletResponse
     * @throws SQLException If a database access error occurs (propagated from Service)
     * @throws IOException If an I/O error occurs
     * @throws IllegalArgumentException If product data is invalid or product ID is missing/invalid (propagated from Service)
     */
    private void updateProduct(HttpServletRequest request, HttpServletResponse response) throws SQLException, IOException, IllegalArgumentException {
        // Get product ID from the request, which is crucial for update operation
        int productId = parseIntParam(request.getParameter("productId"), "產品ID");

        Product updatedProduct = populateProductFromRequest(request); // Use helper method to populate product object
        updatedProduct.setProductId(productId); // Set the product ID from the form

        boolean success = productService.updateProduct(updatedProduct); // Call Service layer to update product
        
        if (success) {
            LOGGER.info("Successfully updated product, ID: {}", productId);
            request.getSession().setAttribute("message", "產品 '" + updatedProduct.getProductName() + "' 已成功更新。");
        } else {
            // 如果 updateProduct 返回 false，但沒有拋出異常，可能是因為產品不存在
            LOGGER.warn("Update product failed for ID: {}. Product might not exist.", productId);
            request.getSession().setAttribute("errorMessage", "更新產品失敗：ID為 " + productId + " 的產品可能不存在。");
        }
        
        response.sendRedirect(request.getContextPath() + "/products?action=list"); // Redirect to product list page after successful update
    }

    /**
     * Deletes a product from the database.
     * @param request HttpServletRequest
     * @param response HttpServletResponse
     * @throws SQLException If a database access error occurs (propagated from Service)
     * @throws IOException If an I/O error occurs
     * @throws IllegalArgumentException If the product ID parameter is invalid (propagated from Service)
     * @throws IllegalStateException If the product cannot be deleted due to business rules (propagated from Service)
     */
    private void deleteProduct(HttpServletRequest request, HttpServletResponse response) throws SQLException, IOException, IllegalArgumentException, IllegalStateException {
        String idParam = request.getParameter("id");
        int productId = parseIntParam(idParam, "產品ID"); // Use helper method to parse ID

        boolean deleted = productService.deleteProduct(productId); // Call Service layer to delete product
        
        if (deleted) {
            LOGGER.info("Successfully deleted product, ID: {}", productId);
            request.getSession().setAttribute("message", "產品 (ID: " + productId + ") 已成功刪除。");
        } else {
            LOGGER.warn("Failed to delete product or product did not exist, ID: {}", productId);
            request.getSession().setAttribute("errorMessage", "刪除產品失敗或產品不存在，ID: '" + productId + "'。");
        }
        
        response.sendRedirect(request.getContextPath() + "/products?action=list"); // Redirect to product list page after deletion attempt
    }

    /**
     * Extracts parameters from HttpServletRequest and constructs a Product object.
     * This helper method simplifies parameter parsing and validation for add/update operations.
     * @param request HttpServletRequest
     * @return Populated Product object
     * @throws IllegalArgumentException If parameters are invalid or missing required fields
     */
    private Product populateProductFromRequest(HttpServletRequest request) throws IllegalArgumentException {
        Product product = new Product();

        // Product Code
        String productCode = request.getParameter("productCode");
        if (productCode == null || productCode.trim().isEmpty()) {
            throw new IllegalArgumentException("產品代碼為必填項。");
        }
        product.setProductCode(productCode);

        // Product Name
        String productName = request.getParameter("productName");
        if (productName == null || productName.trim().isEmpty()) {
            throw new IllegalArgumentException("產品名稱為必填項。");
        }
        product.setProductName(productName);

        // Category (optional)
        product.setCategory(request.getParameter("category"));

        // Unit
        String unit = request.getParameter("unit");
        if (unit == null || unit.trim().isEmpty()) {
            throw new IllegalArgumentException("單位為必填項。");
        }
        product.setUnit(unit);

        // Selling Price
        product.setSellingPrice(parseBigDecimal(request.getParameter("sellingPrice"), "銷售價格"));

        // Cost (optional)
        String costStr = request.getParameter("cost");
        if (costStr != null && !costStr.trim().isEmpty()) {
            product.setCost(parseBigDecimal(costStr, "成本"));
        } else {
            product.setCost(null); // If empty, set to null
        }

        // isActive checkbox: "on" if checked, null if unchecked.
        product.setActive("on".equalsIgnoreCase(request.getParameter("isActive")) || "true".equalsIgnoreCase(request.getParameter("isActive")));

        // Image URL (optional)
        product.setImageUrl(request.getParameter("imageUrl"));

        // Description (optional)
        product.setDescription(request.getParameter("description"));

        return product;
    }

    /**
     * Helper method: Converts a string to BigDecimal and handles potential errors.
     * @param value The string value to convert.
     * @param fieldName The name of the field, used for error messages.
     * @return The converted BigDecimal value.
     * @throws IllegalArgumentException If the value is empty, invalid format, or negative.
     */
    private BigDecimal parseBigDecimal(String value, String fieldName) throws IllegalArgumentException {
        if (value == null || value.trim().isEmpty()) {
            throw new IllegalArgumentException("'" + fieldName + "' field cannot be empty.");
        }
        try {
            BigDecimal decimalValue = new BigDecimal(value);
            if (decimalValue.compareTo(BigDecimal.ZERO) < 0) {
                throw new IllegalArgumentException("'" + fieldName + "' field cannot be negative.");
            }
            return decimalValue;
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("Invalid number format: '" + fieldName + "' field must be a number.", e);
        }
    }

    /**
     * Helper method: Converts a string to int and handles potential errors.
     * @param value The string value to convert.
     * @param fieldName The name of the field, used for error messages.
     * @return The converted int value.
     * @throws IllegalArgumentException If the value is empty or invalid format.
     */
    private int parseIntParam(String value, String fieldName) throws IllegalArgumentException {
        if (value == null || value.trim().isEmpty()) {
            throw new IllegalArgumentException("'" + fieldName + "' field cannot be empty.");
        }
        try {
            return Integer.parseInt(value);
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("Invalid number format: '" + fieldName + "' field must be an integer.", e);
        }
    }
}