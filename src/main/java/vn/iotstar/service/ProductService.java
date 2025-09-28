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
        logger.debug("findAll() called");
        try {
            List<Product> products = productRepository.findAllOrderByCreateDateDesc();
            logger.info("findAll() returned {} products", products == null ? 0 : products.size());
            return products;
        } catch (Exception e) {
            logger.error("Error in findAll()", e);
            throw e;
        }
    }
    
    public Page<Product> findAll(Pageable pageable) {
        logger.debug("findAll(pageable) called with page: {}, size: {}", pageable.getPageNumber(), pageable.getPageSize());
        try {
            Page<Product> products = productRepository.findAll(pageable);
            logger.info("findAll(pageable) returned {} products", products == null ? 0 : products.getTotalElements());
            return products;
        } catch (Exception e) {
            logger.error("Error in findAll(pageable)", e);
            throw e;
        }
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
        logger.debug("save() called for product: {}", product.getTitle());
        try {
            Product savedProduct = productRepository.save(product);
            logger.info("save() successful for product ID: {}", savedProduct.getId());
            return savedProduct;
        } catch (Exception e) {
            logger.error("Error in save() for product: {}", product.getTitle(), e);
            throw e;
        }
    }
    
    // Delete operations
    public void deleteById(Integer id) {
        logger.debug("deleteById() called for product ID: {}", id);
        try {
            productRepository.deleteById(id);
            logger.info("deleteById() successful for product ID: {}", id);
        } catch (Exception e) {
            logger.error("Error in deleteById() for product ID: {}", id, e);
            throw e;
        }
    }
    
    // Search operations with pagination
    public Page<Product> searchByName(String name, Pageable pageable) {
        logger.debug("searchByName() called with name: {}, page: {}, size: {}",
                name, pageable.getPageNumber(), pageable.getPageSize());
        try {
            Page<Product> products;
            if (name == null || name.trim().isEmpty()) {
                products = productRepository.findAll(pageable);
            } else {
                products = productRepository.findByTitleContainingIgnoreCase(name.trim(), pageable);
            }
            logger.info("searchByName() returned {} products", products == null ? 0 : products.getTotalElements());
            return products;
        } catch (Exception e) {
            logger.error("Error in searchByName() for name: {}", name, e);
            throw e;
        }
    }
    
    
    public List<Product> findByCategoryId(Integer categoryId) {
        logger.debug("findByCategoryId() called for category ID: {}", categoryId);
        try {
            List<Product> products = productRepository.findByCategoryId(categoryId);
            logger.info("findByCategoryId() returned {} products for category ID: {}", products == null ? 0 : products.size(), categoryId);
            return products;
        } catch (Exception e) {
            logger.error("Error in findByCategoryId() for category ID: {}", categoryId, e);
            throw e;
        }
    }

    public Page<Product> findByCategoryId(Integer categoryId, Pageable pageable) {
        logger.debug("findByCategoryId(pageable) called for category ID: {}, page: {}, size: {}",
                categoryId, pageable.getPageNumber(), pageable.getPageSize());
        try {
            Page<Product> products = productRepository.findByCategoryId(categoryId, pageable);
            logger.info("findByCategoryId(pageable) returned {} products for category ID: {}",
                    products == null ? 0 : products.getTotalElements(), categoryId);
            return products;
        } catch (Exception e) {
            logger.error("Error in findByCategoryId(pageable) for category ID: {}", categoryId, e);
            throw e;
        }
    }
    
    // Search by name and category
    public Page<Product> searchByNameAndCategory(String name, Integer categoryId, Pageable pageable) {
        logger.debug("searchByNameAndCategory() called with name: {}, categoryId: {}, page: {}, size: {}",
                name, categoryId, pageable.getPageNumber(), pageable.getPageSize());
        try {
            Page<Product> products;
            if (name == null || name.trim().isEmpty()) {
                products = productRepository.findByCategoryId(categoryId, pageable);
            } else {
                products = productRepository.findByTitleContainingIgnoreCaseAndCategoryId(name.trim(), categoryId, pageable);
            }
            logger.info("searchByNameAndCategory() returned {} products", products == null ? 0 : products.getTotalElements());
            return products;
        } catch (Exception e) {
            logger.error("Error in searchByNameAndCategory() for name: {}, categoryId: {}", name, categoryId, e);
            throw e;
        }
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