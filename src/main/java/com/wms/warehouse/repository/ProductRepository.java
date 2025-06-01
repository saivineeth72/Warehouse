package com.wms.warehouse.repository;

import com.wms.warehouse.model.Product;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import org.springframework.data.repository.query.Param;

import java.util.List;

@Repository
public interface ProductRepository extends JpaRepository<Product, Long> {
    List<Product> findByNameContainingIgnoreCase(String name);
    List<Product> findByQuantityLessThan(int threshold);
    List<Product> findByNameAndBrand(String name, String brand);

    @Query("SELECT p.quantity FROM Product p WHERE p.id = :productId")
    int getCurrentStock(@Param("productId") Long productId);

    @Query("SELECT p FROM Product p WHERE p.name = :name AND p.brand = :brand AND p.supplier.name = :supplierName")
    Product findByNameAndBrandAndSupplierName(@Param("name") String name, @Param("brand") String brand, @Param("supplierName") String supplierName);
}