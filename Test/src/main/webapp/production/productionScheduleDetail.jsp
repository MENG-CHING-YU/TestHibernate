<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib uri="jakarta.tags.core" prefix="c" %>
<%@ taglib uri="jakarta.tags.fmt" prefix="fmt" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/functions" prefix="fn" %>

<!DOCTYPE html>
<html lang="zh-Hant">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <%-- 將 pageTitle 設置為請求屬性，用於此頁面標題 --%>
    <c:set var="pageTitle" value="生產排程明細" scope="request"/>
    <title>${pageTitle}</title>

    <%-- 包含必要的 CSS --%>
    <link rel="stylesheet" href="https://cdnjs.cloudflare.com/ajax/libs/font-awesome/5.15.3/css/all.min.css">
    <style>
        /* General Body and Layout */
        body {
            font-family: 'Segoe UI', Tahoma, Geneva, Verdana, sans-serif;
            margin: 0;
            padding: 0;
            background-color: #f4f7f6;
            color: #333;
            line-height: 1.6;
        }

        .wrapper {
            display: flex;
            flex-direction: column;
            min-height: 100vh;
        }

        .main-content {
            display: flex;
            flex: 1;
            width: 100%;
            max-width: 1400px;
            margin: 0 auto;
            background-color: #fff;
            box-shadow: 0 0 15px rgba(0, 0, 0, 0.05);
            border-radius: 8px;
            overflow: hidden;
            margin-top: 20px;
            margin-bottom: 20px;
        }

        .content-area {
            flex: 1;
            padding: 30px;
            background-color: #ffffff;
        }

        /* Headings */
        h2 {
            color: #2c3e50;
            border-bottom: 2px solid #e0e0e0;
            padding-bottom: 15px;
            margin-bottom: 25px;
            font-size: 1.8em;
        }

        /* Toolbar for Filters and Actions */
        .toolbar {
            display: flex;
            flex-wrap: wrap;
            gap: 15px;
            margin-bottom: 25px;
            align-items: center;
            background-color: #f9f9f9;
            padding: 15px;
            border-radius: 8px;
            border: 1px solid #e0e0e0;
        }

        .toolbar form {
            display: flex;
            gap: 10px;
            align-items: center;
        }

        .toolbar label {
            font-weight: bold;
            color: #555;
            white-space: nowrap;
        }

        /* Buttons */
        .btn {
            padding: 10px 20px;
            border: none;
            border-radius: 5px;
            cursor: pointer;
            font-size: 1em;
            transition: background-color 0.3s ease, transform 0.2s ease;
            text-decoration: none;
            text-align: center;
            display: inline-block;
            white-space: nowrap;
        }

        .btn-primary {
            background-color: #007bff;
            color: white;
        }

        .btn-primary:hover {
            background-color: #0056b3;
            transform: translateY(-2px);
        }

        .btn-secondary {
            background-color: #6c757d;
            color: white;
        }

        .btn-secondary:hover {
            background-color: #5a6268;
            transform: translateY(-2px);
        }

        .btn-info {
            background-color: #17a2b8;
            color: white;
        }

        .btn-info:hover {
            background-color: #138496;
            transform: translateY(-2px);
        }

        .btn-danger {
            background-color: #dc3545;
            color: white;
        }

        .btn-danger:hover {
            background-color: #c82333;
            transform: translateY(-2px);
        }

        .btn-success {
            background-color: #28a745;
            color: white;
        }

        .btn-success:hover {
            background-color: #218838;
            transform: translateY(-2px);
        }

        .btn-icon {
            background: none;
            border: none;
            font-size: 1.2em;
            cursor: pointer;
            padding: 5px;
            margin: 0 3px;
            color: #007bff;
            transition: color 0.3s ease, transform 0.2s ease;
            vertical-align: middle;
        }

        .btn-icon i {
            pointer-events: none; /* Allows click to pass through to button */
        }

        .btn-icon.view-icon { color: #17a2b8; }
        .btn-icon.edit-icon { color: #ffc107; }
        .btn-icon.delete-icon { color: #dc3545; }

        .btn-icon:hover {
            color: #0056b3;
            transform: scale(1.1);
        }
        .btn-icon.edit-icon:hover { color: #e0a800; }
        .btn-icon.delete-icon:hover { color: #c82333; }


        /* Form Elements */
        .data-form {
            background-color: #fdfdfd;
            padding: 30px;
            border-radius: 8px;
            box-shadow: 0 2px 10px rgba(0, 0, 0, 0.04);
        }

        .form-group {
            margin-bottom: 20px;
        }

        .form-group label {
            display: block;
            margin-bottom: 8px;
            font-weight: bold;
            color: #555;
        }

        .form-control {
            width: calc(100% - 22px); /* Account for padding and border */
            padding: 10px;
            border: 1px solid #ccc;
            border-radius: 5px;
            font-size: 1em;
            box-sizing: border-box; /* Include padding and border in the element's total width and height */
            transition: border-color 0.3s ease, box-shadow 0.3s ease;
        }

        .form-control:focus {
            border-color: #007bff;
            box-shadow: 0 0 0 0.2rem rgba(0, 123, 255, 0.25);
            outline: none;
        }

        .form-static-text {
            padding: 10px;
            border: 1px solid #e9ecef;
            border-radius: 5px;
            background-color: #e9ecef;
            color: #495057;
            margin-top: 0;
            margin-bottom: 0;
            font-weight: bold;
        }

        textarea.form-control {
            resize: vertical; /* Allow vertical resizing */
            min-height: 80px;
        }

        .form-actions {
            margin-top: 30px;
            display: flex;
            gap: 10px;
            justify-content: flex-end;
        }

        /* Data Table */
        .overflow-x-auto {
            overflow-x: auto;
            -webkit-overflow-scrolling: touch; /* Enable smooth scrolling on iOS */
        }

        .data-table {
            width: 100%;
            border-collapse: collapse;
            margin-top: 20px;
            box-shadow: 0 2px 8px rgba(0, 0, 0, 0.05);
            border-radius: 8px;
            overflow: hidden; /* Ensures border-radius applies to table */
        }

        .data-table thead {
            background-color: #007bff;
            color: white;
        }

        .data-table th,
        .data-table td {
            padding: 12px 15px;
            text-align: left;
            border-bottom: 1px solid #ddd;
        }

        .data-table th {
            font-weight: bold;
            white-space: nowrap; /* Prevent headers from wrapping */
        }

        .data-table tbody tr:nth-child(even) {
            background-color: #f8f8f8;
        }

        .data-table tbody tr:hover {
            background-color: #f1f1f1;
            cursor: pointer;
        }

        .data-table td.action-buttons {
            white-space: nowrap; /* Keep buttons on one line */
            text-align: center;
        }

        .text-center {
            text-align: center;
        }

        .py-4 {
            padding-top: 1.5rem;
            padding-bottom: 1.5rem;
        }

        /* Status Badges */
        .status-badge {
            display: inline-block;
            padding: 5px 10px;
            border-radius: 15px;
            font-weight: bold;
            color: white;
            font-size: 0.85em;
            text-transform: capitalize;
        }

        .status-Scheduled { background-color: #007bff; } /* Blue */
        .status-InProduction { background-color: #ffc107; color: #333; } /* Yellow */
        .status-Completed { background-color: #28a745; } /* Green */
        .status-Delayed { background-color: #dc3545; } /* Red */
        .status-Cancelled { background-color: #6c757d; } /* Gray */

        /* Messages */
        .alert {
            padding: 15px;
            margin-bottom: 20px;
            border: 1px solid transparent;
            border-radius: 5px;
            font-weight: bold;
        }

        .alert-success {
            color: #155724;
            background-color: #d4edda;
            border-color: #c3e6cb;
        }

        .alert-danger {
            color: #721c24;
            background-color: #f8d7da;
            border-color: #f5c6cb;
        }

        /* View Details Card */
        .view-details-card {
            background-color: #fdfdfd;
            padding: 30px;
            border-radius: 8px;
            box-shadow: 0 2px 10px rgba(0, 0, 0, 0.04);
            margin-top: 20px;
        }

        .view-details-card p {
            margin-bottom: 10px;
            font-size: 1.1em;
        }

        .view-details-card p strong {
            color: #2c3e50;
            display: inline-block;
            width: 120px; /* Align labels */
        }

        .mt-6 {
            margin-top: 1.5rem;
        }


        /* Custom Modals */
        .modal-overlay {
            position: fixed;
            top: 0;
            left: 0;
            width: 100%;
            height: 100%;
            background: rgba(0, 0, 0, 0.6);
            display: flex;
            justify-content: center;
            align-items: center;
            z-index: 1000;
            opacity: 0;
            visibility: hidden;
            transition: opacity 0.3s ease, visibility 0.3s ease;
        }

        .modal-overlay.open {
            opacity: 1;
            visibility: visible;
        }

        .modal-content {
            background: #fff;
            padding: 30px;
            border-radius: 8px;
            box-shadow: 0 5px 15px rgba(0, 0, 0, 0.3);
            width: 90%;
            max-width: 450px;
            transform: translateY(-50px);
            transition: transform 0.3s ease, opacity 0.3s ease;
            opacity: 0;
        }

        .modal-overlay.open .modal-content {
            transform: translateY(0);
            opacity: 1;
        }

        .modal-header {
            border-bottom: 1px solid #eee;
            padding-bottom: 15px;
            margin-bottom: 20px;
        }

        .modal-header h3 {
            margin: 0;
            color: #2c3e50;
            font-size: 1.5em;
        }

        .modal-body p {
            margin-bottom: 15px;
            font-size: 1.1em;
            color: #444;
        }

        .modal-body input[type="number"] {
            width: calc(100% - 20px);
            padding: 10px;
            border: 1px solid #ccc;
            border-radius: 5px;
            font-size: 1em;
            margin-top: 10px;
        }

        .modal-footer {
            margin-top: 25px;
            display: flex;
            justify-content: flex-end;
            gap: 10px;
        }


        /* Responsive Adjustments */
        @media (max-width: 768px) {
            .main-content {
                flex-direction: column;
                margin-top: 10px;
                margin-bottom: 10px;
                border-radius: 0;
            }

            .content-area {
                padding: 20px;
            }

            .toolbar {
                flex-direction: column;
                align-items: stretch;
            }

            .toolbar form {
                flex-direction: column;
                align-items: stretch;
                gap: 5px;
            }

            .toolbar .btn {
                width: 100%;
                margin-top: 10px;
            }

            .data-table thead {
                display: none; /* Hide table headers on small screens */
            }

            .data-table, .data-table tbody, .data-table tr, .data-table td {
                display: block;
                width: 100%;
            }

            .data-table tr {
                margin-bottom: 15px;
                border: 1px solid #ddd;
                border-radius: 8px;
                overflow: hidden;
                background-color: #fff;
                box-shadow: 0 2px 5px rgba(0,0,0,0.03);
            }

            .data-table td {
                text-align: right;
                padding-left: 50%;
                position: relative;
                border: none;
            }

            .data-table td::before {
                content: attr(data-label);
                position: absolute;
                left: 15px;
                width: calc(50% - 30px);
                text-align: left;
                font-weight: bold;
                color: #555;
            }

            .data-table td.action-buttons {
                text-align: left;
                padding-left: 15px;
                display: flex;
                flex-wrap: wrap;
                gap: 5px;
                justify-content: flex-start;
                padding-top: 10px;
                padding-bottom: 10px;
            }

            .data-table td.action-buttons::before {
                content: "操作";
                position: static;
                width: auto;
                margin-right: 10px;
            }

            .view-details-card p strong {
                width: 90px; /* Adjust for smaller screens */
            }
        }
    </style>
</head>
<body>
    <div class="wrapper">
        <%-- Removed direct header.jsp inclusion as per user's original request, assuming it's handled externally or not needed --%>

        <div class="main-content">
            <%-- Directly include sidebar.jsp --%>
            

            <div class="content-area">
                <section class="data-table-section">

                    <%-- 顯示成功或錯誤訊息 --%>
                    <c:if test="${not empty sessionScope.message}">
                        <div class="alert alert-success">
                            <c:out value="${sessionScope.message}"/>
                        </div>
                        <c:remove var="message" scope="session"/>
                    </c:if>
                    <c:if test="${not empty requestScope.errorMessage}">
                        <div class="alert alert-danger">
                            <c:out value="${requestScope.errorMessage}"/>
                        </div>
                        <c:remove var="errorMessage" scope="request"/>
                    </c:if>

                    <%-- 列出所有排程的介面 --%>
                    <c:choose>
                        <c:when test="${action eq 'list' or action eq 'byStatus' or action eq 'byDate' or empty action}">
                            <h2>生產排程明細列表</h2>
                            <div class="toolbar">
                                <%-- 狀態篩選表單 --%>
                                <form action="${pageContext.request.contextPath}/productionschedules" method="GET">
                                    <input type="hidden" name="action" value="byStatus">
                                    <label for="statusFilter">篩選狀態:</label>
                                    <select name="status" id="statusFilter" class="form-control">
                                        <option value="">所有狀態</option>
                                        <option value="Scheduled" <c:if test="${param.status eq 'Scheduled'}">selected</c:if>>已排程</option>
                                        <option value="In Production" <c:if test="${param.status eq 'In Production'}">selected</c:if>>生產中</option>
                                        <option value="Completed" <c:if test="${param.status eq 'Completed'}">selected</c:if>>已完成</option>
                                        <option value="Delayed" <c:if test="${param.status eq 'Delayed'}">selected</c:if>>延遲</option>
                                        <option value="Cancelled" <c:if test="${param.status eq 'Cancelled'}">selected</c:if>>已取消</option>
                                    </select>
                                    <button type="submit" class="btn btn-primary">篩選</button>
                                </form>

                                <%-- 日期篩選表單 --%>
                                <form action="${pageContext.request.contextPath}/productionschedules" method="GET">
                                    <input type="hidden" name="action" value="byDate">
                                    <label for="dateFilter">篩選日期:</label>
                                    <input type="date" name="scheduledDate" id="dateFilter" class="form-control" value="<fmt:formatDate value="${param.scheduledDate}" pattern="yyyy-MM-dd"/>">
                                    <button type="submit" class="btn btn-primary">篩選</button>
                                </form>

                                <%-- 新增生產排程和顯示全部排程按鈕 --%>
                                <a href="${pageContext.request.contextPath}/productionschedules?action=new" class="btn btn-primary">新增生產排程</a>
                                <a href="${pageContext.request.contextPath}/productionschedules?action=list" class="btn btn-secondary">顯示全部排程</a>
                            </div>

                            <div class="overflow-x-auto">
                                <table class="data-table">
                                    <thead>
                                        <tr>
                                            <th>排程ID</th>
                                            <th>工單編號</th>
                                            <th>產品代碼</th>
                                            <th>產品名稱</th>
                                            <th>計劃日期</th>
                                            <th>班次</th>
                                            <th>計劃數量</th>
                                            <th>實際數量</th>
                                            <th>狀態</th>
                                            <th>備註</th>
                                            <th>操作</th>
                                        </tr>
                                    </thead>
                                    <tbody>
                                        <c:forEach var="schedule" items="${schedules}">
                                            <tr>
                                                <td data-label="排程ID"><c:out value="${schedule.scheduleId}"/></td>
                                                <td data-label="工單編號">
                                                    <c:choose>
                                                        <c:when test="${not empty schedule.workOrderNumber}">
                                                            <c:out value="${schedule.workOrderNumber}"/>
                                                        </c:when>
                                                        <c:otherwise>
                                                            N/A
                                                        </c:otherwise>
                                                    </c:choose>
                                                </td>
                                                <td data-label="產品代碼"><c:out value="${schedule.productCode}"/></td>
                                                <td data-label="產品名稱"><c:out value="${schedule.productName}"/></td>
                                                <td data-label="計劃日期"><fmt:formatDate value="${schedule.scheduledDate}" pattern="yyyy-MM-dd"/></td>
                                                <td data-label="班次"><c:out value="${schedule.shift}"/></td>
                                                <td data-label="計劃數量"><fmt:formatNumber value="${schedule.plannedQuantity}" pattern="#,##0.####"/></td>
                                                <td data-label="實際數量">
                                                    <c:choose>
                                                        <c:when test="${not empty schedule.actualQuantity}">
                                                            <fmt:formatNumber value="${schedule.actualQuantity}" pattern="#,##0.####"/>
                                                        </c:when>
                                                        <c:otherwise>
                                                            待定
                                                        </c:otherwise>
                                                    </c:choose>
                                                </td>
                                                <td data-label="狀態">
                                                    <span class="status-badge status-<c:out value="${fn:replace(schedule.status, ' ', '')}"/>">
                                                        <c:out value="${schedule.status}"/>
                                                    </span>
                                                </td>
                                                <td data-label="備註"><c:out value="${schedule.notes}"/></td>
                                                <td data-label="操作" class="action-buttons">
                                                    <a href="${pageContext.request.contextPath}/productionschedules?action=view&id=<c:out value="${schedule.scheduleId}"/>" class="btn-icon view-icon" title="查看"><i class="fas fa-eye"></i></a>
                                                    <a href="${pageContext.request.contextPath}/productionschedules?action=edit&id=<c:out value="${schedule.scheduleId}"/>" class="btn-icon edit-icon" title="編輯"><i class="fas fa-edit"></i></a>
                                                    <button class="btn-icon delete-icon" title="刪除"
                                                            onclick="showConfirmModal('確定要刪除排程ID: <c:out value="${schedule.scheduleId}"/> 嗎？', function() { window.location.href='${pageContext.request.contextPath}/productionschedules?action=delete&id=<c:out value="${schedule.scheduleId}"/>'; });">
                                                        <i class="fas fa-trash"></i>
                                                    </button>
                                                    <c:if test="${schedule.status eq 'Scheduled' or schedule.status eq 'Delayed'}">
                                                        <button class="btn-icon" title="開始生產" onclick="window.location.href='${pageContext.request.contextPath}/productionschedules?action=startProduction&id=<c:out value="${schedule.scheduleId}"/>';"><i class="fas fa-play"></i></button>
                                                    </c:if>
                                                    <c:if test="${schedule.status eq 'In Production' or schedule.status eq 'Delayed'}">
                                                        <button class="btn-icon" title="完成排程" onclick="showPromptModal('請輸入實際完成數量:', function(actualQuantity) { window.location.href='${pageContext.request.contextPath}/productionschedules?action=completeSchedule&id=<c:out value="${schedule.scheduleId}"/>&actualQuantity=' + encodeURIComponent(actualQuantity); });"><i class="fas fa-check"></i></button>
                                                    </c:if>
                                                </td>
                                            </tr>
                                        </c:forEach>
                                        <c:if test="${empty schedules}">
                                            <tr>
                                                <td colspan="11" class="text-center py-4">沒有找到生產排程。</td>
                                            </tr>
                                        </c:if>
                                    </tbody>
                                </table>
                            </div>
                        </c:when>

                        <%-- 新增/編輯排程的表單 --%>
                        <c:when test="${action eq 'new' or action eq 'edit'}">
                            <h2>${action eq 'new' ? '新增生產排程' : '編輯生產排程'}</h2>
                            <form action="${pageContext.request.contextPath}/productionschedules" method="POST" class="data-form">
                                <input type="hidden" name="action" value="${action eq 'new' ? 'add' : 'update'}">
                                <c:if test="${action eq 'edit'}">
                                    <div class="form-group">
                                        <label>排程ID:</label>
                                        <p class="form-static-text"><c:out value="${schedule.scheduleId}"/></p>
                                        <input type="hidden" name="scheduleId" value="${schedule.scheduleId}">
                                    </div>
                                </c:if>

                                <div class="form-group">
                                    <label for="productId">產品:</label>
                                    <select id="productId" name="productId" class="form-control" required>
                                        <option value="">請選擇產品</option>
                                        <c:forEach var="product" items="${products}">
                                            <option value="${product.productId}" <c:if test="${schedule.productId eq product.productId}">selected</c:if>>
                                                <c:out value="${product.productCode}"/> - <c:out value="${product.productName}"/>
                                            </option>
                                        </c:forEach>
                                    </select>
                                </div>

                                <div class="form-group">
                                    <label for="workOrderId">工單編號 (可選):</label>
                                    <select id="workOrderId" name="workOrderId" class="form-control">
                                        <option value="">無工單關聯</option>
                                        <c:forEach var="workOrder" items="${workOrders}">
                                            <option value="${workOrder.workOrderId}" <c:if test="${schedule.workOrderId eq workOrder.workOrderId}">selected</c:if>>
                                                <c:out value="${workOrder.workOrderNumber}"/>
                                            </option>
                                        </c:forEach>
                                    </select>
                                </div>

                                <div class="form-group">
                                    <label for="scheduledDate">計劃日期:</label>
                                    <input type="date" id="scheduledDate" name="scheduledDate" class="form-control" value="<fmt:formatDate value="${schedule.scheduledDate}" pattern="yyyy-MM-dd"/>" required>
                                </div>

                                <div class="form-group">
                                    <label for="shift">班次:</label>
                                    <select id="shift" name="shift" class="form-control" required>
                                        <option value="">請選擇班次</option>
                                        <option value="Morning" <c:if test="${schedule.shift eq 'Morning'}">selected</c:if>>早班</option>
                                        <option value="Afternoon" <c:if test="${schedule.shift eq 'Afternoon'}">selected</c:if>>中班</option>
                                        <option value="Night" <c:if test="${schedule.shift eq 'Night'}">selected</c:if>>晚班</option>
                                        <option value="Shift A" <c:if test="${schedule.shift eq 'Shift A'}">selected</c:if>>A班</option>
                                        <option value="Shift B" <c:if test="${schedule.shift eq 'Shift B'}">selected</c:if>>B班</option>
                                    </select>
                                </div>

                                <div class="form-group">
                                    <label for="plannedQuantity">計劃數量:</label>
                                    <input type="number" step="0.0001" id="plannedQuantity" name="plannedQuantity" class="form-control" value="${schedule.plannedQuantity}" required min="0.0001">
                                </div>

                                <%-- 實際數量僅在編輯時顯示且可選 --%>
                                <c:if test="${action eq 'edit'}">
                                    <div class="form-group">
                                        <label for="actualQuantity">實際數量 (可選):</label>
                                        <input type="number" step="0.0001" id="actualQuantity" name="actualQuantity" class="form-control" value="${schedule.actualQuantity}" min="0">
                                    </div>
                                </c:if>

                                <div class="form-group">
                                    <label for="status">狀態:</label>
                                    <select id="status" name="status" class="form-control" required>
                                        <option value="Scheduled" <c:if test="${schedule.status eq 'Scheduled'}">selected</c:if>>已排程</option>
                                        <option value="In Production" <c:if test="${schedule.status eq 'In Production'}">selected</c:if>>生產中</option>
                                        <option value="Completed" <c:if test="${schedule.status eq 'Completed'}">selected</c:if>>已完成</option>
                                        <option value="Delayed" <c:if test="${schedule.status eq 'Delayed'}">selected</c:if>>延遲</option>
                                        <option value="Cancelled" <c:if test="${schedule.status eq 'Cancelled'}">selected</c:if>>已取消</option>
                                    </select>
                                </div>

                                <div class="form-group">
                                    <label for="notes">備註:</label>
                                    <textarea id="notes" name="notes" class="form-control" rows="3"><c:out value="${schedule.notes}"/></textarea>
                                </div>

                                <div class="form-actions">
                                    <button type="submit" class="btn btn-primary">保存</button>
                                    <a href="${pageContext.request.contextPath}/productionschedules?action=list" class="btn btn-secondary">取消</a>
                                </div>
                            </form>
                        </c:when>

                        <%-- 查看排程詳情的介面 --%>
                        <c:when test="${action eq 'view'}">
                            <h2>排程詳情</h2>
                            <div class="view-details-card">
                                <p><strong>排程ID:</strong> <c:out value="${schedule.scheduleId}"/></p>
                                <p><strong>工單編號:</strong>
                                    <c:choose>
                                        <c:when test="${not empty schedule.workOrderNumber}">
                                            <c:out value="${schedule.workOrderNumber}"/>
                                        </c:when>
                                        <c:otherwise>
                                            N/A
                                        </c:otherwise>
                                    </c:choose>
                                </p>
                                <p><strong>產品代碼:</strong> <c:out value="${schedule.productCode}"/></p>
                                <p><strong>產品名稱:</strong> <c:out value="${schedule.productName}"/></p>
                                <p><strong>計劃日期:</strong> <fmt:formatDate value="${schedule.scheduledDate}" pattern="yyyy-MM-dd"/></p>
                                <p><strong>班次:</strong> <c:out value="${schedule.shift}"/></p>
                                <p><strong>計劃數量:</strong> <fmt:formatNumber value="${schedule.plannedQuantity}" pattern="#,##0.####"/></p>
                                <p><strong>實際數量:</strong>
                                    <c:choose>
                                        <c:when test="${not empty schedule.actualQuantity}">
                                            <fmt:formatNumber value="${schedule.actualQuantity}" pattern="#,##0.####"/></c:when>
                                        <c:otherwise>
                                            待定
                                        </c:otherwise>
                                    </c:choose>
                                </p>
                                <p><strong>狀態:</strong>
                                    <span class="status-badge status-<c:out value="${fn:replace(schedule.status, ' ', '')}"/>">
                                        <c:out value="${schedule.status}"/>
                                    </span>
                                </p>
                                <p><strong>備註:</strong> <c:out value="${schedule.notes}"/></p>
                                <p><strong>創建時間:</strong> <fmt:formatDate value="${schedule.createDate}" pattern="yyyy-MM-dd HH:mm:ss"/></p>
                                <p><strong>更新時間:</strong> <fmt:formatDate value="${schedule.updateDate}" pattern="yyyy-MM-dd HH:mm:ss"/></p>

                                <div class="form-actions mt-6">
                                    <a href="${pageContext.request.contextPath}/productionschedules?action=edit&id=<c:out value="${schedule.scheduleId}"/>" class="btn btn-info">編輯</a>
                                    <button class="btn btn-danger"
                                            onclick="showConfirmModal('確定要刪除排程ID: <c:out value="${schedule.scheduleId}"/> 嗎？', function() { window.location.href='${pageContext.request.contextPath}/productionschedules?action=delete&id=<c:out value="${schedule.scheduleId}"/>'; });">
                                        刪除
                                    </button>
                                    <c:if test="${schedule.status eq 'Scheduled' or schedule.status eq 'Delayed'}">
                                        <a href="${pageContext.request.contextPath}/productionschedules?action=startProduction&id=<c:out value="${schedule.scheduleId}"/>" class="btn btn-primary">開始生產</a>
                                    </c:if>
                                    <c:if test="${schedule.status eq 'In Production' or schedule.status eq 'Delayed'}">
                                        <a href="javascript:void(0);" onclick="showPromptModal('請輸入實際完成數量:', function(actualQuantity) { window.location.href='${pageContext.request.contextPath}/productionschedules?action=completeSchedule&id=<c:out value="${schedule.scheduleId}"/>&actualQuantity=' + encodeURIComponent(actualQuantity); });" class="btn btn-success">完成排程</a>
                                    </c:if>
                                    <a href="${pageContext.request.contextPath}/productionschedules?action=list" class="btn btn-secondary">返回列表</a>
                                </div>
                            </div>
                        </c:when>
                    </c:choose>

                </section>
            </div>
        </div>

        <%-- 自定義模態框 HTML 結構 --%>
        <div id="confirmModal" class="modal-overlay">
            <div class="modal-content">
                <div class="modal-header">
                    <h3>確認</h3>
                </div>
                <div class="modal-body">
                    <p id="confirmMessage"></p>
                </div>
                <div class="modal-footer">
                    <button class="btn btn-danger" id="confirmOk">確定</button>
                    <button class="btn btn-secondary" id="confirmCancel">取消</button>
                </div>
            </div>
        </div>

        <div id="promptModal" class="modal-overlay">
            <div class="modal-content">
                <div class="modal-header">
                    <h3>輸入</h3>
                </div>
                <div class="modal-body">
                    <p id="promptMessage"></p>
                    <input type="number" id="promptInput" step="0.0001" min="0">
                </div>
                <div class="modal-footer">
                    <button class="btn btn-primary" id="promptOk">確定</button>
                    <button class="btn btn-secondary" id="promptCancel">取消</button>
                </div>
            </div>
        </div>


        <%-- JavaScript 函數，用於處理自定義模態框 --%>
        <script>
            // 通用模態框控制邏輯
            function closeModal(modalId) {
                document.getElementById(modalId).classList.remove('open');
            }

            // 確認模態框
            let confirmCallback = null;
            function showConfirmModal(message, callback) {
                document.getElementById('confirmMessage').innerText = message;
                document.getElementById('confirmModal').classList.add('open');
                confirmCallback = callback;
            }

            document.getElementById('confirmOk').onclick = function() {
                if (confirmCallback) {
                    confirmCallback();
                }
                closeModal('confirmModal');
            };

            document.getElementById('confirmCancel').onclick = function() {
                closeModal('confirmModal');
            };

            // 輸入模態框
            let promptCallback = null;
            function showPromptModal(message, callback) {
                document.getElementById('promptMessage').innerText = message;
                document.getElementById('promptInput').value = ''; // Clear previous input
                document.getElementById('promptModal').classList.add('open');
                promptCallback = callback;
            }

            document.getElementById('promptOk').onclick = function() {
                if (promptCallback) {
                    const inputValue = document.getElementById('promptInput').value;
                    if (inputValue.trim() === "") {
                        document.getElementById('promptMessage').innerText = "實際數量不能為空。請重新輸入:"; // Update message
                        return; // Keep modal open
                    }
                    if (isNaN(parseFloat(inputValue))) {
                        document.getElementById('promptMessage').innerText = "請輸入有效的數字。請重新輸入:";
                        return;
                    }
                    if (parseFloat(inputValue) < 0) {
                        document.getElementById('promptMessage').innerText = "實際數量不能為負數。請重新輸入:";
                        return;
                    }
                    promptCallback(inputValue);
                }
                closeModal('promptModal');
            };

            document.getElementById('promptCancel').onclick = function() {
                closeModal('promptModal');
            };

            // 關閉點擊外部區域的模態框
            window.onclick = function(event) {
                if (event.target == document.getElementById('confirmModal')) {
                    closeModal('confirmModal');
                }
                if (event.target == document.getElementById('promptModal')) {
                    closeModal('promptModal');
                }
            };
        </script>

        <%-- Removed direct footer.jsp inclusion as per user's original request, assuming it's handled externally or not needed --%>
    </div>
</body>
</html>