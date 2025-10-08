/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Interface.java to edit this template
 */
package DAO;

import MODELS.OrderById;
import com.datastax.oss.driver.api.core.PagingIterable;
import com.datastax.oss.driver.api.mapper.annotations.Dao;
import com.datastax.oss.driver.api.mapper.annotations.Delete;
import com.datastax.oss.driver.api.mapper.annotations.Insert;
import com.datastax.oss.driver.api.mapper.annotations.Select;
import com.datastax.oss.driver.api.mapper.annotations.Update;
import java.util.UUID;

/**
 *
 * @author HAO
 */
@Dao
public interface OrderByIdDAO {
    @Insert
    void save(OrderById order);

    @Update // Bổ sung Update
    void update(OrderById order);

    @Delete // Bổ sung Delete
    void delete(OrderById order);
    
    @Select
    OrderById findById(UUID orderId);

    @Select(customWhereClause = "status = :status")
    PagingIterable<OrderById> findByStatus(String status);
}
