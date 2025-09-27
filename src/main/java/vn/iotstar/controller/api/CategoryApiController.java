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

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/category")
@Tag(name = "Category API", description = "APIs for managing categories")
public class CategoryApiController {
    
    @Autowired
    private CategoryService categoryService;

    @Autowired
    private ProductService productService;

    @Autowired
    private StorageService storageService;
    
    @Operation(summary = "Get all categories with pagination and search")
    @GetMapping
    public ResponseEntity<Response<Page<Category>>> getAllCategories(
            @Parameter(description = "Search query") @RequestParam(required = false) String q,
            @Parameter(description = "Page number (0-based)") @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Page size") @RequestParam(defaultValue = "10") int size) {
        
        try {
            Pageable pageable = PageRequest.of(page, size);
            Page<Category> categories;
            
            if (q != null && !q.trim().isEmpty()) {
                categories = categoryService.searchByName(q, pageable);
            } else {
                categories = categoryService.findAll(pageable);
            }
            
            Response<Page<Category>> response = Response.success("Lấy danh sách category thành công", categories)
                    .withPagination(categories.getTotalElements(), categories.getTotalPages(), 
                                  page, size);
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Response.error("Lỗi server: " + e.getMessage()));
        }
    }
    
    @Operation(summary = "Get category by ID")
    @GetMapping("/{id}")
    public ResponseEntity<Response<Category>> getCategoryById(
            @Parameter(description = "Category ID") @PathVariable Integer id) {
        
        try {
            Optional<Category> category = categoryService.findById(id);
            
            if (category.isPresent()) {
                return ResponseEntity.ok(Response.success("Lấy thông tin category thành công", category.get()));
            } else {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(Response.notFound("Không tìm thấy category với ID: " + id));
            }
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Response.error("Lỗi server: " + e.getMessage()));
        }
    }
    
    @Operation(summary = "Add new category")
    @PostMapping
    public ResponseEntity<Response<Category>> addCategory(
            @Parameter(description = "Category name") @RequestParam String name,
            @Parameter(description = "Category images") @RequestParam(required = false) String images) {

        try {
            // Validate category name
            if (name == null || name.trim().isEmpty()) {
                return ResponseEntity.badRequest()
                        .body(Response.badRequest("Tên category không được để trống"));
            }

            // Check if category name already exists
            if (categoryService.existsByName(name.trim())) {
                return ResponseEntity.badRequest()
                        .body(Response.badRequest("Tên category đã tồn tại"));
            }

            Category category = new Category();
            category.setName(name.trim());
            category.setImages(images);

            Category savedCategory = categoryService.save(category);
            return ResponseEntity.status(HttpStatus.CREATED)
                    .body(Response.created("Thêm category thành công", savedCategory));

        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Response.error("Lỗi server: " + e.getMessage()));
        }
    }
    
    @Operation(summary = "Update category")
    @PutMapping("/{id}")
    public ResponseEntity<Response<Category>> updateCategory(
            @Parameter(description = "Category ID") @PathVariable Integer id,
            @Parameter(description = "Category name") @RequestParam String name,
            @Parameter(description = "Category images") @RequestParam(required = false) String images) {

        try {
            // Validate category exists
            Optional<Category> existingCategory = categoryService.findById(id);
            if (!existingCategory.isPresent()) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(Response.notFound("Không tìm thấy category với ID: " + id));
            }

            // Validate category name
            if (name == null || name.trim().isEmpty()) {
                return ResponseEntity.badRequest()
                        .body(Response.badRequest("Tên category không được để trống"));
            }

            // Check if category name already exists for other categories
            if (categoryService.existsByNameAndNotId(name.trim(), id)) {
                return ResponseEntity.badRequest()
                        .body(Response.badRequest("Tên category đã tồn tại"));
            }

            Category category = existingCategory.get();
            category.setName(name.trim());
            category.setImages(images);

            Category updatedCategory = categoryService.save(category);
            return ResponseEntity.ok(Response.success("Cập nhật category thành công", updatedCategory));

        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Response.error("Lỗi server: " + e.getMessage()));
        }
    }

    @Operation(summary = "Delete category")
    @DeleteMapping("/{id}")
    public ResponseEntity<Response<Void>> deleteCategory(
            @Parameter(description = "Category ID") @PathVariable Integer id) {

        try {
            Optional<Category> category = categoryService.findById(id);
            if (!category.isPresent()) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(Response.notFound("Không tìm thấy category với ID: " + id));
            }

            categoryService.deleteById(id);
            return ResponseEntity.ok(Response.success("Xóa category thành công"));

        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Response.error("Lỗi server: " + e.getMessage()));
        }
    }

}
