package vn.iotstar.service;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import vn.iotstar.entity.Product;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

public interface IProductService {
    
    // Basic CRUD operations
    List<Product> findAll();
    Page<Product> findAll(Pageable pageable);
    Optional<Product> findById(Integer id);
    Product save(Product product);
    void deleteById(Integer id);
    
    // GraphQL specific methods
    List<Product> findAllOrderByPriceAsc();
    List<Product> findByUserId(Integer userId);
    
    // Search operations with pagination
    Page<Product> searchByName(String name, Pageable pageable);
    List<Product> findByCategoryId(Integer categoryId);
    Page<Product> findByCategoryId(Integer categoryId, Pageable pageable);
    Page<Product> searchByNameAndCategory(String name, Integer categoryId, Pageable pageable);
    
    // Price range operations
    List<Product> findByPriceRange(BigDecimal minPrice, BigDecimal maxPrice);
    List<Product> findByPriceRangeOrderByPrice(BigDecimal minPrice, BigDecimal maxPrice);
    Page<Product> findByPriceRange(BigDecimal minPrice, BigDecimal maxPrice, Pageable pageable);

    // Specialized product queries
    List<Product> findDiscountedProducts();
    List<Product> findOutOfStockProducts();
    List<Product> findLowStockProducts(Integer threshold);
    
    // Validation operations
    boolean existsById(Integer id);
    boolean existsByTitle(String title);
    boolean existsByTitleAndNotId(String title, Integer id);

    Product update(Product product);
    
    // Count operations
    long count();
    long countByCategoryId(Integer categoryId);
    
    // Create and Update operations for API Controller
    Product createProduct(Product product);
    Product updateProduct(Product product);
}