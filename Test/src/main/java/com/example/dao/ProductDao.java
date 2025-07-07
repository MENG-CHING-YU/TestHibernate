package com.example.dao;

import com.example.model.Product;
import java.sql.SQLException;
import java.util.List;

public interface ProductDao {
    Product addProduct(Product product); // 不拋出 SQLException，回傳 Product
    Product getProductById(int productId) throws SQLException; // 拋出 SQLException
    List<Product> getAllProducts() throws SQLException; // 拋出 SQLException
    boolean updateProduct(Product product); // 不拋出 SQLException，回傳 boolean
    boolean deleteProduct(int productId); // 不拋出 SQLException，回傳 boolean
}