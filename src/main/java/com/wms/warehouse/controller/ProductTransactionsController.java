package com.wms.warehouse.controller;

import com.wms.warehouse.model.ProductTransactions;
import com.wms.warehouse.repository.ProductTransactionsRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/transactions")
public class ProductTransactionsController {

    @Autowired
    private ProductTransactionsRepository productTransactionsRepository;

    @GetMapping
    public List<ProductTransactions> getAllTransactions() {
        return productTransactionsRepository.findAll();
    }

    @GetMapping("/product/{productId}")
    public List<ProductTransactions> getTransactionsByProductId(@PathVariable Long productId) {
        return productTransactionsRepository.findByProductId(productId);
    }

    @GetMapping("/action/{actionType}")
    public List<ProductTransactions> getTransactionsByActionType(@PathVariable String actionType) {
        return productTransactionsRepository.findByActionTypeIgnoreCase(actionType);
    }
} 
