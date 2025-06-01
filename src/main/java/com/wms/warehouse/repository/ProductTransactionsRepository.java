package com.wms.warehouse.repository;

import com.wms.warehouse.model.ProductTransactions;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ProductTransactionsRepository extends JpaRepository<ProductTransactions, Long> {

    List<ProductTransactions> findByProductId(Long productId);

    List<ProductTransactions> findByActionTypeIgnoreCase(String actionType);
}