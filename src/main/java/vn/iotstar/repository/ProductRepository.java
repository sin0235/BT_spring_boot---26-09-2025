package vn.iotstar.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.lang.NonNull;
import vn.iotstar.entity.Product;
import vn.iotstar.entity.Category;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

@Repository
public interface ProductRepository extends JpaRepository<Product, Integer> {
    
    // Tìm tất cả sản phẩm theo thứ tự ngày tạo giảm dần
    @Query("SELECT p FROM Product p ORDER BY p.createDate DESC")
    List<Product> findAllOrderByCreateDateDesc();
    
    // Tìm kiếm sản phẩm theo tên với phân trang
    Page<Product> findByTitleContainingIgnoreCase(String title, Pageable pageable);
    
    // Tìm tất cả sản phẩm với phân trang
    @NonNull
    Page<Product> findAll(@NonNull Pageable pageable);
    
    // Tìm sản phẩm theo category ID
    List<Product> findByCategoryId(Integer categoryId);

    // Tìm sản phẩm theo category ID với phân trang
    Page<Product> findByCategoryId(Integer categoryId, Pageable pageable);

    // Tìm kiếm sản phẩm theo tên và category
    @Query("SELECT p FROM Product p WHERE LOWER(p.title) LIKE LOWER(CONCAT('%', :title, '%')) AND p.category.id = :categoryId")
    Page<Product> findByTitleContainingIgnoreCaseAndCategoryId(
            @Param("title") String title,
            @Param("categoryId") Integer categoryId,
            Pageable pageable);
    
    // GraphQL specific queries
    // Tìm tất cả sản phẩm sắp xếp theo giá từ thấp đến cao
    @Query("SELECT p FROM Product p ORDER BY p.price ASC")
    List<Product> findAllOrderByPriceAsc();
    
    // Tìm sản phẩm theo user ID
    @Query("SELECT p FROM Product p WHERE p.user.id = :userId")
    List<Product> findByUserId(@Param("userId") Integer userId);
    
    // Tìm sản phẩm trong khoảng giá sắp xếp theo giá tăng dần
    @Query("SELECT p FROM Product p WHERE p.price BETWEEN :minPrice AND :maxPrice ORDER BY p.price ASC")
    List<Product> findByPriceRangeOrderByPrice(@Param("minPrice") BigDecimal minPrice, @Param("maxPrice") BigDecimal maxPrice);

    // Tìm sản phẩm theo khoảng giá
    @Query("SELECT p FROM Product p WHERE p.price BETWEEN :minPrice AND :maxPrice")
    List<Product> findByPriceRange(@Param("minPrice") BigDecimal minPrice, @Param("maxPrice") BigDecimal maxPrice);

    // Tìm sản phẩm theo khoảng giá với phân trang
    @Query("SELECT p FROM Product p WHERE p.price BETWEEN :minPrice AND :maxPrice")
    Page<Product> findByPriceRange(@Param("minPrice") BigDecimal minPrice, @Param("maxPrice") BigDecimal maxPrice, Pageable pageable);

    // Kiểm tra tên sản phẩm đã tồn tại
    boolean existsByTitleIgnoreCase(String title);

    // Kiểm tra tên sản phẩm đã tồn tại với ID khác (để update)
    @Query("SELECT COUNT(p) > 0 FROM Product p WHERE LOWER(p.title) = LOWER(:title) AND p.id != :id")
    boolean existsByTitleIgnoreCaseAndIdNot(@Param("title") String title, @Param("id") Integer id);
    
    // Đếm tổng số sản phẩm
    long count();
    
    // Đếm sản phẩm theo category
    long countByCategory(Category category);
    
    // Đếm sản phẩm theo category ID
    long countByCategoryId(Integer categoryId);

    // Đếm sản phẩm theo trạng thái
    long countByStatus(Boolean status);
    
    // Tìm sản phẩm hết hàng
    @Query("SELECT p FROM Product p WHERE p.quantity = 0")
    List<Product> findOutOfStockProducts();
    
    // Tìm sản phẩm sắp hết hàng (số lượng <= threshold)
    @Query("SELECT p FROM Product p WHERE p.quantity <= :threshold AND p.quantity > 0")
    List<Product> findLowStockProducts(@Param("threshold") Integer threshold);
    
    // Tìm sản phẩm có giảm giá
    @Query("SELECT p FROM Product p WHERE p.discount > 0")
    List<Product> findDiscountedProducts();
    
    // Tìm sản phẩm có giảm giá với phân trang
    @Query("SELECT p FROM Product p WHERE p.discount > 0")
    Page<Product> findDiscountedProducts(Pageable pageable);
}