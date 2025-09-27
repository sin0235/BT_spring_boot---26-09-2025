package vn.iotstar.controller.api;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import vn.iotstar.entity.Category;
import vn.iotstar.entity.Product;
import vn.iotstar.model.Response;
import vn.iotstar.service.CategoryService;
import vn.iotstar.service.ProductService;
import vn.iotstar.service.StorageService;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/product")
@Tag(name = "Product API", description = "APIs for managing products")
public class ProductApiController {
    
    @Autowired
    private ProductService productService;
    
    @Autowired
    private CategoryService categoryService;
    
    @Autowired
    private StorageService storageService;
    
    @Operation(summary = "Get all products with pagination and search")
    @GetMapping
    public ResponseEntity<Response<Page<Product>>> getAllProducts(
            @Parameter(description = "Search query") @RequestParam(required = false) String q,
            @Parameter(description = "Category ID filter") @RequestParam(required = false) Integer categoryId,
            @Parameter(description = "Page number (0-based)") @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Page size") @RequestParam(defaultValue = "10") int size) {
        
        try {
            Pageable pageable = PageRequest.of(page, size);
            Page<Product> products;
            
            if (categoryId != null) {
                if (q != null && !q.trim().isEmpty()) {
                    products = productService.searchByNameAndCategory(q, categoryId, pageable);
                } else {
                    products = productService.findByCategoryId(categoryId, pageable);
                }
            } else if (q != null && !q.trim().isEmpty()) {
                products = productService.searchByName(q, pageable);
            } else {
                products = productService.findAll(pageable);
            }
            
            Response<Page<Product>> response = Response.success("Lấy danh sách sản phẩm thành công", products)
                    .withPagination(products.getTotalElements(), products.getTotalPages(), 
                                  page, size);
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Response.error("Lỗi server: " + e.getMessage()));
        }
    }
    
    @Operation(summary = "Get product by ID")
    @GetMapping("/{id}")
    public ResponseEntity<Response<Product>> getProductById(
            @Parameter(description = "Product ID") @PathVariable Integer id) {
        
        try {
            Optional<Product> product = productService.findById(id);
            
            if (product.isPresent()) {
                return ResponseEntity.ok(Response.success("Lấy thông tin sản phẩm thành công", product.get()));
            } else {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(Response.notFound("Không tìm thấy sản phẩm với ID: " + id));
            }
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Response.error("Lỗi server: " + e.getMessage()));
        }
    }
    
    @Operation(summary = "Add new product")
    @PostMapping
    public ResponseEntity<Response<Product>> addProduct(
            @Parameter(description = "Product title") @RequestParam String title,
            @Parameter(description = "Quantity") @RequestParam Integer quantity,
            @Parameter(description = "Description") @RequestParam(required = false) String description,
            @Parameter(description = "Price") @RequestParam BigDecimal price,
            @Parameter(description = "User ID") @RequestParam Integer userId,
            @Parameter(description = "Product image") @RequestParam(required = false) MultipartFile images) {

        try {
            // Validate required fields
            if (title == null || title.trim().isEmpty()) {
                return ResponseEntity.badRequest()
                        .body(Response.badRequest("Tên sản phẩm không được để trống"));
            }

            if (quantity == null || quantity < 0) {
                return ResponseEntity.badRequest()
                        .body(Response.badRequest("Số lượng phải >= 0"));
            }

            if (price == null || price.compareTo(BigDecimal.ZERO) <= 0) {
                return ResponseEntity.badRequest()
                        .body(Response.badRequest("Giá sản phẩm phải > 0"));
            }

            // Check if product title already exists
            if (productService.existsByTitle(title.trim())) {
                return ResponseEntity.badRequest()
                        .body(Response.badRequest("Tên sản phẩm đã tồn tại"));
            }

            Product product = new Product();
            product.setTitle(title.trim());
            product.setQuantity(quantity);
            product.setDescription(description);
            product.setPrice(price);

            // Handle image upload
            if (images != null && !images.isEmpty()) {
                try {
                    String imageFilename = storageService.store(images, "prod_");
                    product.setImages("/uploads/" + imageFilename);
                } catch (Exception e) {
                    return ResponseEntity.badRequest()
                            .body(Response.badRequest("Lỗi upload ảnh: " + e.getMessage()));
                }
            }

            Product savedProduct = productService.save(product);
            return ResponseEntity.status(HttpStatus.CREATED)
                    .body(Response.created("Thêm sản phẩm thành công", savedProduct));

        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Response.error("Lỗi server: " + e.getMessage()));
        }
    }
    
    @Operation(summary = "Update product")
    @PutMapping("/{id}")
    public ResponseEntity<Response<Product>> updateProduct(
            @Parameter(description = "Product ID") @PathVariable Integer id,
            @Parameter(description = "Product title") @RequestParam String title,
            @Parameter(description = "Quantity") @RequestParam Integer quantity,
            @Parameter(description = "Description") @RequestParam(required = false) String description,
            @Parameter(description = "Price") @RequestParam BigDecimal price,
            @Parameter(description = "Product image") @RequestParam(required = false) MultipartFile images) {

        try {
            // Validate product exists
            Optional<Product> existingProduct = productService.findById(id);
            if (!existingProduct.isPresent()) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(Response.notFound("Không tìm thấy sản phẩm với ID: " + id));
            }

            // Validate required fields
            if (title == null || title.trim().isEmpty()) {
                return ResponseEntity.badRequest()
                        .body(Response.badRequest("Tên sản phẩm không được để trống"));
            }

            if (quantity == null || quantity < 0) {
                return ResponseEntity.badRequest()
                        .body(Response.badRequest("Số lượng phải >= 0"));
            }

            if (price == null || price.compareTo(BigDecimal.ZERO) <= 0) {
                return ResponseEntity.badRequest()
                        .body(Response.badRequest("Giá sản phẩm phải > 0"));
            }

            // Check if product title already exists for other products
            if (productService.existsByTitleAndNotId(title.trim(), id)) {
                return ResponseEntity.badRequest()
                        .body(Response.badRequest("Tên sản phẩm đã tồn tại"));
            }

            Product product = existingProduct.get();
            product.setTitle(title.trim());
            product.setQuantity(quantity);
            product.setDescription(description);
            product.setPrice(price);

            // Handle image upload
            if (images != null && !images.isEmpty()) {
                try {
                    String imageFilename = storageService.store(images, "prod_");
                    product.setImages("/uploads/" + imageFilename);
                } catch (Exception e) {
                    return ResponseEntity.badRequest()
                            .body(Response.badRequest("Lỗi upload ảnh: " + e.getMessage()));
                }
            }

            Product updatedProduct = productService.save(product);
            return ResponseEntity.ok(Response.success("Cập nhật sản phẩm thành công", updatedProduct));

        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Response.error("Lỗi server: " + e.getMessage()));
        }
    }
    
    @Operation(summary = "Delete product")
    @DeleteMapping("/{id}")
    public ResponseEntity<Response<Void>> deleteProduct(
            @Parameter(description = "Product ID") @PathVariable Integer id) {

        try {
            Optional<Product> product = productService.findById(id);
            if (!product.isPresent()) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(Response.notFound("Không tìm thấy sản phẩm với ID: " + id));
            }

            productService.deleteById(id);
            return ResponseEntity.ok(Response.success("Xóa sản phẩm thành công"));

        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Response.error("Lỗi server: " + e.getMessage()));
        }
    }

    @Operation(summary = "Get products sorted by price")
    @GetMapping("/sorted-by-price")
    public ResponseEntity<Response<List<Product>>> getProductsSortedByPrice() {

        try {
            List<Product> products = productService.findAllOrderByPriceAsc();
            return ResponseEntity.ok(Response.success("Lấy danh sách sản phẩm theo giá thành công", products));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Response.error("Lỗi server: " + e.getMessage()));
        }
    }

    @Operation(summary = "Get all products of a specific category")
    @GetMapping("/category/{categoryId}")
    public ResponseEntity<Response<List<Product>>> getProductsByCategory(
            @Parameter(description = "Category ID") @PathVariable Integer categoryId) {

        try {
            List<Product> products = productService.findByCategoryId(categoryId);
            return ResponseEntity.ok(Response.success("Lấy danh sách sản phẩm của category thành công", products));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Response.error("Lỗi server: " + e.getMessage()));
        }
    }

}
