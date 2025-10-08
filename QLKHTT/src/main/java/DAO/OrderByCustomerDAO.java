/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Interface.java to edit this template
 */
package DAO;

import MODELS.OrderByCustomer;
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
public interface OrderByCustomerDAO {

    @Insert
    void save(OrderByCustomer order);

    @Update // Bổ sung Update
    void update(OrderByCustomer order);

    @Delete // Bổ sung Delete
    void delete(OrderByCustomer order);

    @Select(customWhereClause = "customer_id = :customerId ALLOW FILTERING")
    PagingIterable<OrderByCustomer> findByCustomer(UUID customerId);

    @Select
    PagingIterable<OrderByCustomer> findByCustomerAndYearMonth(UUID customerId, String yearMonth);
}
