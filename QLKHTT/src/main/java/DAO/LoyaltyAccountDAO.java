/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package DAO;

import MODELS.LoyaltyAccount;
import com.datastax.oss.driver.api.core.PagingIterable;
import com.datastax.oss.driver.api.mapper.annotations.Dao;
import com.datastax.oss.driver.api.mapper.annotations.Insert;
import com.datastax.oss.driver.api.mapper.annotations.Select;
import java.util.UUID;

/**
 *
 * @author HAO
 */

@Dao
public interface LoyaltyAccountDAO {
    @Insert
    void save(LoyaltyAccount loyaltyAccount);

    @Select
    LoyaltyAccount findById(UUID customerId);

    @Select(customWhereClause = "tier = :tier")
    PagingIterable<LoyaltyAccount> findByTier(String tier);
}