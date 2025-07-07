package com.example.servlet;

import com.example.model.BillOfMaterial;
import com.example.model.Product;
import com.example.model.Material;
import com.example.service.BillOfMaterialService;
import com.example.service.MaterialService;
import com.example.service.ProductService;
import com.example.service.impl.BillOfMaterialServiceImpl;
import com.example.service.impl.MaterialServiceImpl;
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
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * BillOfMaterialServlet handles web requests for Bill of Materials (BOM).
 * Includes operations for listing, adding, viewing, editing, updating, and deleting BOM items.
 * URL Mapping: /bom/*
 */
@WebServlet("/bom/*") // Maps all requests starting with /bom/ to this servlet
public class BillOfMaterialServlet extends HttpServlet {

    private static final Logger LOGGER = Logger.getLogger(BillOfMaterialServlet.class.getName());
    private BillOfMaterialService bomService;
    private ProductService productService; // Required for product dropdown in JSP
    private MaterialService materialService; // Required for material dropdown in JSP

    @Override
    public void init() throws ServletException {
        super.init();
        // Initialize service instances
        this.bomService = new BillOfMaterialServiceImpl();
        this.productService = new ProductServiceImpl();
        this.materialService = new MaterialServiceImpl();
        LOGGER.info("BillOfMaterialServlet initialized.");
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        String action = request.getParameter("action"); // Get action from query parameter
        if (action == null || action.isEmpty()) {
            action = "list"; // Default action is to list all BOMs
        }
        LOGGER.log(Level.INFO, "BillOfMaterialServlet received GET request, action: {0}", action);

        try {
            switch (action) {
                case "new": // Display form for new BOM item
                    showNewBomForm(request, response);
                    break;
                case "edit": // Display form for editing BOM item
                    showEditBomForm(request, response);
                    break;
                case "view": // View a single BOM item's details
                    viewBomDetail(request, response);
                    break;
                case "delete": // Delete a BOM item
                    deleteBom(request, response);
                    break;
                case "byProduct": // List BOM items by specific product
                    listBomsByProduct(request, response);
                    break;
                case "list": // List all BOM items
                default:
                    listAllBoms(request, response);
                    break;
            }
        } catch (SQLException ex) {
            LOGGER.log(Level.SEVERE, "BillOfMaterialServlet encountered database error while processing GET request: " + action, ex);
            request.setAttribute("errorMessage", "資料庫操作失敗：" + ex.getMessage());
            RequestDispatcher dispatcher = request.getRequestDispatcher("/error.jsp"); // Assuming error.jsp exists
            dispatcher.forward(request, response);
        } catch (IllegalArgumentException ex) {
            LOGGER.log(Level.WARNING, "BillOfMaterialServlet received invalid parameter for GET request: " + action, ex);
            request.setAttribute("errorMessage", "請求參數無效：" + ex.getMessage());
            RequestDispatcher dispatcher = request.getRequestDispatcher("/error.jsp");
            dispatcher.forward(request, response);
        }
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        String action = request.getParameter("action"); // Get action from form parameter
        if (action == null || action.isEmpty()) {
            action = "list";
        }
        LOGGER.log(Level.INFO, "BillOfMaterialServlet received POST request, action: {0}", action);
        request.setCharacterEncoding("UTF-8"); // Ensure proper handling of Chinese characters

        try {
            switch (action) {
                case "add": // Submit for adding a new BOM item
                    insertBom(request, response);
                    break;
                case "update": // Submit for updating a BOM item
                    updateBom(request, response);
                    break;
                default:
                    response.sendError(HttpServletResponse.SC_NOT_FOUND);
                    LOGGER.log(Level.WARNING, "Unknown POST action: {0}", action);
                    break;
            }
        } catch (SQLException ex) {
            LOGGER.log(Level.SEVERE, "BillOfMaterialServlet encountered database error while processing POST request: " + action, ex);
            request.setAttribute("errorMessage", "資料庫操作失敗：" + ex.getMessage());
            handlePostErrorForward(request, response, action);
        } catch (IllegalArgumentException ex) {
            LOGGER.log(Level.WARNING, "BillOfMaterialServlet received invalid parameter for POST request: " + action, ex);
            request.setAttribute("errorMessage", "提交的數據格式無效：" + ex.getMessage());
            handlePostErrorForward(request, response, action);
        }
    }

    /**
     * Handles error forwarding for POST requests, attempting to repopulate the form.
     * @param request HttpServletRequest
     * @param response HttpServletResponse
     * @param action The current action (e.g., "add", "update")
     * @throws ServletException If a servlet-specific error occurs
     * @throws IOException If an I/O error occurs
     */
    private void handlePostErrorForward(HttpServletRequest request, HttpServletResponse response, String action) throws ServletException, IOException {
        // Prepare data for dropdowns
        try {
            request.setAttribute("products", productService.getAllProducts());
            request.setAttribute("materials", materialService.getAllMaterials());
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error fetching dropdown data for BOM form.", e);
            request.setAttribute("errorMessage", request.getAttribute("errorMessage") + " (無法載入產品或物料列表)");
        }

        // Attempt to reconstruct the BOM object from request parameters for re-filling the form
        BillOfMaterial bom = new BillOfMaterial();
        try {
            String bomIdParam = request.getParameter("bomId");
            if (bomIdParam != null && !bomIdParam.isEmpty()) {
                bom.setBomId(parseIntParam(bomIdParam, "BOM ID"));
            }
            bom.setProductId(parseIntParam(request.getParameter("productId"), "產品ID"));
            bom.setMaterialId(parseIntParam(request.getParameter("materialId"), "物料ID"));
            bom.setQuantity(parseBigDecimal(request.getParameter("quantity"), "消耗數量"));

            // Populate product/material names for display if possible (from available lists or lookup)
            // This would typically involve looking up from the 'products' and 'materials' lists that are already fetched
            // For simplicity, we just set the IDs and let the JSP handle the pre-selection in dropdowns.

        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error repopulating BOM object for error forwarding.", e);
            // Add a general error message if repopulation itself fails badly
            if (request.getAttribute("errorMessage") == null) {
                 request.setAttribute("errorMessage", "重新載入表單時發生錯誤: " + e.getMessage());
            }
        }
        request.setAttribute("bom", bom); // Set the reconstructed BOM object
        request.setAttribute("action", action); // Ensure the correct action is displayed in JSP
        RequestDispatcher dispatcher = request.getRequestDispatcher("/production/bomList.jsp");
        dispatcher.forward(request, response);
    }

    /**
     * Retrieves all BOM items and forwards to bomList.jsp.
     * @param request HttpServletRequest
     * @param response HttpServletResponse
     * @throws SQLException If a database access error occurs
     * @throws ServletException If a servlet-specific error occurs
     * @throws IOException If an I/O error occurs
     */
    private void listAllBoms(HttpServletRequest request, HttpServletResponse response) throws SQLException, ServletException, IOException {
        List<BillOfMaterial> boms = bomService.getAllBillOfMaterials();
        LOGGER.log(Level.INFO, "Retrieved {0} BOM items from Service layer.", (boms != null ? boms.size() : "0"));

        request.setAttribute("boms", boms);
        request.setAttribute("action", "list"); // Set action for JSP to render list view
        request.setAttribute("pageTitle", "所有產品用料清單");
        RequestDispatcher dispatcher = request.getRequestDispatcher("/production/bomList.jsp");
        dispatcher.forward(request, response);
    }

    /**
     * Retrieves BOM items for a specific product and forwards to bomList.jsp.
     * @param request HttpServletRequest
     * @param response HttpServletResponse
     * @throws SQLException If a database access error occurs
     * @throws ServletException If a servlet-specific error occurs
     * @throws IOException If an I/O error occurs
     * @throws IllegalArgumentException If product ID is invalid
     */
    private void listBomsByProduct(HttpServletRequest request, HttpServletResponse response) throws SQLException, ServletException, IOException, IllegalArgumentException {
        String productIdParam = request.getParameter("id");
        int productId = parseIntParam(productIdParam, "產品ID");

        Product product = productService.getProductById(productId);
        if (product == null) {
            LOGGER.log(Level.WARNING, "Cannot list BOM for non-existent product ID: {0}", productId);
            request.setAttribute("errorMessage", "指定的產品不存在，無法查看其用料清單。");
            listAllBoms(request, response); // Fallback to list all BOMs or show error page
            return;
        }

        List<BillOfMaterial> boms = bomService.getBillOfMaterialsByProductId(productId);
        LOGGER.log(Level.INFO, "Retrieved {0} BOM items for product ID {1}.", new Object[]{boms.size(), productId});

        request.setAttribute("boms", boms);
        request.setAttribute("action", "listByProduct"); // Set action for JSP to render by-product view
        request.setAttribute("currentProductId", productId); // Pass product ID to JSP
        request.setAttribute("productName", product.getProductName()); // Pass product name to JSP for display
        request.setAttribute("pageTitle", "產品 '" + product.getProductName() + "' 的用料清單");
        RequestDispatcher dispatcher = request.getRequestDispatcher("/production/bomList.jsp");
        dispatcher.forward(request, response);
    }

    /**
     * Displays the form for adding a new BOM item, populating product and material dropdowns.
     * @param request HttpServletRequest
     * @param response HttpServletResponse
     * @throws SQLException If a database access error occurs
     * @throws ServletException If a servlet-specific error occurs
     * @throws IOException If an I/O error occurs
     */
    private void showNewBomForm(HttpServletRequest request, HttpServletResponse response) throws SQLException, ServletException, IOException {
        // Fetch all products and materials for dropdowns
        List<Product> products = productService.getAllProducts();
        List<Material> materials = materialService.getAllMaterials();

        request.setAttribute("products", products);
        request.setAttribute("materials", materials);
        request.setAttribute("action", "new"); // Set action for JSP to render new form
        request.setAttribute("pageTitle", "新增產品用料");
        RequestDispatcher dispatcher = request.getRequestDispatcher("/production/bomList.jsp");
        dispatcher.forward(request, response);
    }

    /**
     * Displays the form for editing an existing BOM item, populating form fields and dropdowns.
     * @param request HttpServletRequest
     * @param response HttpServletResponse
     * @throws SQLException If a database access error occurs
     * @throws ServletException If a servlet-specific error occurs
     * @throws IOException If an I/O error occurs
     * @throws IllegalArgumentException If BOM ID is invalid
     */
    private void showEditBomForm(HttpServletRequest request, HttpServletResponse response) throws SQLException, ServletException, IOException, IllegalArgumentException {
        String bomIdParam = request.getParameter("id");
        int bomId = parseIntParam(bomIdParam, "BOM ID");

        BillOfMaterial bom = bomService.getBillOfMaterialById(bomId);
        if (bom != null) {
            // Fetch all products and materials for dropdowns (even if already selected)
            List<Product> products = productService.getAllProducts();
            List<Material> materials = materialService.getAllMaterials();

            request.setAttribute("bom", bom);
            request.setAttribute("products", products);
            request.setAttribute("materials", materials);
            request.setAttribute("action", "edit"); // Set action for JSP to render edit form
            request.setAttribute("pageTitle", "編輯產品用料");
            RequestDispatcher dispatcher = request.getRequestDispatcher("/production/bomList.jsp");
            dispatcher.forward(request, response);
            LOGGER.log(Level.INFO, "Displaying edit BOM form for BOM ID: {0}", bomId);
        } else {
            LOGGER.log(Level.WARNING, "BOM item to edit not found, ID: {0}", bomId);
            response.sendError(HttpServletResponse.SC_NOT_FOUND, "產品用料項目未找到，ID: " + bomId);
        }
    }

    /**
     * Views details of a single BOM item.
     * @param request HttpServletRequest
     * @param response HttpServletResponse
     * @throws SQLException If a database access error occurs
     * @throws ServletException If a servlet-specific error occurs
     * @throws IOException If an I/O error occurs
     * @throws IllegalArgumentException If BOM ID is invalid
     */
    private void viewBomDetail(HttpServletRequest request, HttpServletResponse response) throws SQLException, ServletException, IOException, IllegalArgumentException {
        String bomIdParam = request.getParameter("id");
        int bomId = parseIntParam(bomIdParam, "BOM ID");

        BillOfMaterial bom = bomService.getBillOfMaterialById(bomId);
        if (bom != null) {
            request.setAttribute("bom", bom);
            request.setAttribute("action", "view"); // Set action for JSP to render view details
            request.setAttribute("pageTitle", "產品用料詳情");
            RequestDispatcher dispatcher = request.getRequestDispatcher("/production/bomList.jsp");
            dispatcher.forward(request, response);
            LOGGER.log(Level.INFO, "Displaying BOM detail for BOM ID: {0}", bomId);
        } else {
            LOGGER.log(Level.WARNING, "BOM item to view not found, ID: {0}", bomId);
            response.sendError(HttpServletResponse.SC_NOT_FOUND, "產品用料項目未找到，ID: " + bomId);
        }
    }

    /**
     * Inserts a new BOM item into the database.
     * @param request HttpServletRequest
     * @param response HttpServletResponse
     * @throws SQLException If a database access error occurs
     * @throws IOException If an I/O error occurs
     * @throws IllegalArgumentException If BOM data is invalid
     */
    private void insertBom(HttpServletRequest request, HttpServletResponse response) throws SQLException, IOException, IllegalArgumentException {
        BillOfMaterial newBom = populateBomFromRequest(request); // Helper method to populate BOM object

        try {
            bomService.addBillOfMaterial(newBom); // Call Service layer to add BOM item
            LOGGER.log(Level.INFO, "Successfully added BOM item: Product ID {0}, Material ID {1}", new Object[]{newBom.getProductId(), newBom.getMaterialId()});
            request.getSession().setAttribute("message", "產品用料項目已成功新增。");
        } catch (SQLException e) {
            // Catch specific SQL error for unique constraint violation (product_id, material_id)
            if (e.getMessage() != null && e.getMessage().contains("UNIQUE KEY constraint")) { // Adjust message based on actual SQL Server error for unique constraint
                 throw new IllegalArgumentException("此產品和物料組合的用料清單項目已存在，請勿重複添加。", e);
            }
            throw e; // Re-throw other SQL exceptions
        }
        response.sendRedirect(request.getContextPath() + "/bom?action=list"); // Redirect to BOM list page
    }

    /**
     * Updates an existing BOM item in the database.
     * @param request HttpServletRequest
     * @param response HttpServletResponse
     * @throws SQLException If a database access error occurs
     * @throws IOException If an I/O error occurs
     * @throws IllegalArgumentException If BOM data is invalid or BOM ID is missing/invalid
     */
    private void updateBom(HttpServletRequest request, HttpServletResponse response) throws SQLException, IOException, IllegalArgumentException {
        int bomId = parseIntParam(request.getParameter("bomId"), "BOM ID");

        BillOfMaterial updatedBom = populateBomFromRequest(request);
        updatedBom.setBomId(bomId);

        try {
            bomService.updateBillOfMaterial(updatedBom); // Call Service layer to update BOM item
            LOGGER.log(Level.INFO, "Successfully updated BOM item, ID: {0}", bomId);
            request.getSession().setAttribute("message", "產品用料項目已成功更新。");
        } catch (SQLException e) {
             if (e.getMessage() != null && e.getMessage().contains("UNIQUE KEY constraint")) { // Adjust message based on actual SQL Server error for unique constraint
                 throw new IllegalArgumentException("更新失敗：此產品和物料組合的用料清單項目已存在。", e);
             }
             throw e; // Re-throw other SQL exceptions
        }
        response.sendRedirect(request.getContextPath() + "/bom?action=list"); // Redirect to BOM list page
    }

    /**
     * Deletes a BOM item from the database.
     * @param request HttpServletRequest
     * @param response HttpServletResponse
     * @throws SQLException If a database access error occurs
     * @throws IOException If an I/O error occurs
     * @throws IllegalArgumentException If BOM ID is invalid
     */
    private void deleteBom(HttpServletRequest request, HttpServletResponse response) throws SQLException, IOException, IllegalArgumentException {
        String bomIdParam = request.getParameter("id");
        int bomId = parseIntParam(bomIdParam, "BOM ID");

        boolean deleted = bomService.deleteBillOfMaterial(bomId);
        if (deleted) {
            LOGGER.log(Level.INFO, "Successfully deleted BOM item, ID: {0}", bomId);
            request.getSession().setAttribute("message", "產品用料項目已成功刪除。");
        } else {
            LOGGER.log(Level.WARNING, "Failed to delete BOM item or item did not exist, ID: {0}", bomId);
            request.getSession().setAttribute("errorMessage", "刪除產品用料項目失敗或項目不存在，ID: '" + bomId + "'。");
        }
        response.sendRedirect(request.getContextPath() + "/bom?action=list"); // Redirect to BOM list page
    }

    /**
     * Helper method to extract parameters from HttpServletRequest and construct a BillOfMaterial object.
     * @param request HttpServletRequest
     * @return Populated BillOfMaterial object
     * @throws IllegalArgumentException If parameters are invalid or missing required fields
     */
    private BillOfMaterial populateBomFromRequest(HttpServletRequest request) throws IllegalArgumentException {
        BillOfMaterial bom = new BillOfMaterial();

        int productId = parseIntParam(request.getParameter("productId"), "產品ID");
        bom.setProductId(productId);

        int materialId = parseIntParam(request.getParameter("materialId"), "物料ID");
        bom.setMaterialId(materialId);

        bom.setQuantity(parseBigDecimal(request.getParameter("quantity"), "消耗數量"));

        // Product Name and Material Name/Unit are populated by the Service/DAO layer when fetching,
        // not directly from form submission for add/update.
        // If needed for immediate display after add/update, perform lookup.

        return bom;
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
            if (decimalValue.compareTo(BigDecimal.ZERO) <= 0) { // Quantity must be positive
                throw new IllegalArgumentException("'" + fieldName + "' field must be greater than zero.");
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
