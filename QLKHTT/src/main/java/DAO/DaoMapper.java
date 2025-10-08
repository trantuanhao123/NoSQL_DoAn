/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package DAO;

import com.datastax.oss.driver.api.mapper.annotations.DaoFactory;
import com.datastax.oss.driver.api.mapper.annotations.Mapper;

/**
 *
 * @author HAO
 */
@Mapper
public interface DaoMapper {
    @DaoFactory
    CustomerDAO customerDAO();

    @DaoFactory
    ProductDAO productDAO();

    @DaoFactory
    OrderByCustomerDAO orderByCustomerDAO();

    @DaoFactory
    OrderByIdDAO orderByIdDAO();

    @DaoFactory
    LoyaltyAccountDAO loyaltyAccountDAO();
}