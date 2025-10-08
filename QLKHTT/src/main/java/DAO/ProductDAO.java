/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package DAO;

import MODELS.Product;
import com.datastax.oss.driver.api.core.PagingIterable;
import com.datastax.oss.driver.api.mapper.annotations.Dao;
import com.datastax.oss.driver.api.mapper.annotations.Insert;
import com.datastax.oss.driver.api.mapper.annotations.Select;

/**
 *
 * @author HAO
 */

@Dao
public interface ProductDAO {
    @Insert
    void save(Product product);

    @Select
    Product findById(String productId);

    @Select(customWhereClause = "brand = :brand")
    PagingIterable<Product> findByBrand(String brand);

    @Select(customWhereClause = "available = :available")
    PagingIterable<Product> findByAvailable(boolean available);
}
