package vn.iotstar.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import vn.iotstar.entity.Category;
import vn.iotstar.entity.Product;
import vn.iotstar.repository.ProductRepository;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

@Service
@Transactional
public class ProductService {
    
    @Autowired
    private ProductRepository productRepository;

    private static final Logger logger = LoggerFactory.getLogger(ProductService.class);
    
    // Basic CRUD operations
    public List<Product> findAll() {
        return productRepository.findAllOrderByCreateDateDesc();
    }
    
    public Page<Product> findAll(Pageable pageable) {
        return productRepository.findAll(pageable);
    }
    
    // GraphQL specific methods
    public List<Product> findAllOrderByPriceAsc() {
        logger.debug("findAllOrderByPriceAsc() called");
        try {
            List<Product> products = productRepository.findAllOrderByPriceAsc();
            logger.info("findAllOrderByPriceAsc() returned {} products", products == null ? 0 : products.size());
            return products;
        } catch (Exception e) {
            logger.error("Error in findAllOrderByPriceAsc()", e);
            throw e;
        }
    }

    public List<Product> findByUserId(Integer userId) {
        return productRepository.findByUserId(userId);
    }
    
    public Optional<Product> findById(Integer id) {
        return productRepository.findById(id);
    }
    
    
    // Save operations
    public Product save(Product product) {
        return productRepository.save(product);
    }
    
    // Delete operations
    public void deleteById(Integer id) {
        productRepository.deleteById(id);
    }
    
    // Search operations with pagination
    public Page<Product> searchByName(String name, Pageable pageable) {
        if (name == null || name.trim().isEmpty()) {
            return productRepository.findAll(pageable);
        }
        return productRepository.findByTitleContainingIgnoreCase(name.trim(), pageable);
    }
    
    
    public List<Product> findByCategoryId(Integer categoryId) {
        return productRepository.findByCategoryId(categoryId);
    }

    public Page<Product> findByCategoryId(Integer categoryId, Pageable pageable) {
        return productRepository.findByCategoryId(categoryId, pageable);
    }
    
    // Search by name and category
    public Page<Product> searchByNameAndCategory(String name, Integer categoryId, Pageable pageable) {
        if (name == null || name.trim().isEmpty()) {
            return productRepository.findByCategoryId(categoryId, pageable);
        }
        return productRepository.findByTitleContainingIgnoreCaseAndCategoryId(name.trim(), categoryId, pageable);
    }
    
    

    

    // Validation operations
    public boolean existsByTitle(String title) {
        return productRepository.existsByTitleIgnoreCase(title);
    }

    public boolean existsByTitleAndNotId(String title, Integer id) {
        return productRepository.existsByTitleIgnoreCaseAndIdNot(title, id);
    }

    public boolean existsById(Integer id) {
        return productRepository.existsById(id);
    }

    public Product update(Product product) {
        return productRepository.save(product);
    }
    
    // Count operations
    public long count() {
        return productRepository.count();
    }

    public long countByCategoryId(Integer categoryId) {
        return productRepository.countByCategoryId(categoryId);
    }
    
    
    // Create and Update operations for API Controller
    public Product createProduct(Product product) {
        // Validate product title doesn't exist
        if (existsByTitle(product.getTitle())) {
            throw new IllegalArgumentException("Product title already exists");
        }

        // Validate required fields
        if (product.getQuantity() == null || product.getQuantity() < 0) {
            throw new IllegalArgumentException("Quantity must be >= 0");
        }

        if (product.getPrice() == null || product.getPrice().compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Price must be > 0");
        }

        return productRepository.save(product);
    }

    public Product updateProduct(Product product) {
        // Validate product exists
        if (!existsById(product.getId())) {
            throw new IllegalArgumentException("Product not found");
        }

        // Validate product title doesn't exist for other products
        if (existsByTitleAndNotId(product.getTitle(), product.getId())) {
            throw new IllegalArgumentException("Product title already exists");
        }

        // Validate required fields
        if (product.getQuantity() == null || product.getQuantity() < 0) {
            throw new IllegalArgumentException("Quantity must be >= 0");
        }

        if (product.getPrice() == null || product.getPrice().compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Price must be > 0");
        }

        return productRepository.save(product);
    }
}