/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package DAO;

import MODELS.Product;
import com.datastax.oss.driver.api.core.PagingIterable;
import com.datastax.oss.driver.api.mapper.annotations.Dao;
import com.datastax.oss.driver.api.mapper.annotations.Delete;
import com.datastax.oss.driver.api.mapper.annotations.Insert;
import com.datastax.oss.driver.api.mapper.annotations.Select;
import com.datastax.oss.driver.api.mapper.annotations.Update;

/**
 *
 * @author HAO
 */

@Dao
public interface ProductDAO {

    @Insert
    void save(Product product);

    @Update
    void update(Product product);

    @Delete
    void delete(Product product);

    @Select
    Product findById(String productId);

    @Select
    PagingIterable<Product> findAll();

    @Select(customWhereClause = "brand = :brand")
    PagingIterable<Product> findByBrand(String brand);

    @Select(customWhereClause = "available = :available")
    PagingIterable<Product> findByAvailable(boolean available);
}
