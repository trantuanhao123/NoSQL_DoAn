/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package DAO;

import MODELS.Customer;
import com.datastax.oss.driver.api.core.PagingIterable;
import com.datastax.oss.driver.api.mapper.annotations.Dao;
import com.datastax.oss.driver.api.mapper.annotations.Delete;
import com.datastax.oss.driver.api.mapper.annotations.Insert;
import com.datastax.oss.driver.api.mapper.annotations.Select;
import java.util.UUID;

/**
 *
 * @author HAO
 */
@Dao
public interface CustomerDAO {

    @Insert
    void save(Customer customer);

    @Select
    Customer findById(UUID customerId);

    @Select(customWhereClause = "email = :email")
    PagingIterable<Customer> findByEmail(String email);

    @Select(customWhereClause = "phone = :phone")
    PagingIterable<Customer> findByPhone(String phone);

    @Select
    PagingIterable<Customer> findAll();

    @Delete
    void delete(Customer customer);
}
