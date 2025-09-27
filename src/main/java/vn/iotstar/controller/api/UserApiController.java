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
import vn.iotstar.entity.Category;
import vn.iotstar.entity.User;
import vn.iotstar.model.Response;
import vn.iotstar.service.CategoryService;
import vn.iotstar.service.UserService;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/user")
@Tag(name = "User API", description = "APIs for managing users")
public class UserApiController {

    @Autowired
    private UserService userService;

    @Autowired
    private CategoryService categoryService;

    @Operation(summary = "Get all users with pagination and search")
    @GetMapping
    public ResponseEntity<Response<Page<User>>> getAllUsers(
            @Parameter(description = "Search query") @RequestParam(required = false) String q,
            @Parameter(description = "Page number (0-based)") @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Page size") @RequestParam(defaultValue = "10") int size) {

        try {
            Pageable pageable = PageRequest.of(page, size);
            Page<User> users;

            if (q != null && !q.trim().isEmpty()) {
                // Search by fullname for now
                users = userService.findByFullnameContaining(q, pageable);
            } else {
                users = userService.findAll(pageable);
            }

            Response<Page<User>> response = Response.success("Lấy danh sách user thành công", users)
                    .withPagination(users.getTotalElements(), users.getTotalPages(),
                                  page, size);

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Response.error("Lỗi server: " + e.getMessage()));
        }
    }

    @Operation(summary = "Get user by ID")
    @GetMapping("/{id}")
    public ResponseEntity<Response<User>> getUserById(
            @Parameter(description = "User ID") @PathVariable Integer id) {

        try {
            Optional<User> user = userService.findById(id);

            if (user.isPresent()) {
                return ResponseEntity.ok(Response.success("Lấy thông tin user thành công", user.get()));
            } else {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(Response.notFound("Không tìm thấy user với ID: " + id));
            }
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Response.error("Lỗi server: " + e.getMessage()));
        }
    }

    @Operation(summary = "Add new user")
    @PostMapping
    public ResponseEntity<Response<User>> addUser(
            @Parameter(description = "Full name") @RequestParam String fullname,
            @Parameter(description = "Email") @RequestParam String email,
            @Parameter(description = "Password") @RequestParam String password,
            @Parameter(description = "Phone") @RequestParam(required = false) String phone,
            @Parameter(description = "Category IDs") @RequestParam(required = false) List<Integer> categoryIds) {

        try {
            // Validate required fields
            if (fullname == null || fullname.trim().isEmpty()) {
                return ResponseEntity.badRequest()
                        .body(Response.badRequest("Họ tên không được để trống"));
            }

            if (email == null || email.trim().isEmpty()) {
                return ResponseEntity.badRequest()
                        .body(Response.badRequest("Email không được để trống"));
            }

            if (password == null || password.trim().isEmpty()) {
                return ResponseEntity.badRequest()
                        .body(Response.badRequest("Mật khẩu không được để trống"));
            }

            // Check if email already exists
            if (userService.existsByEmail(email.trim())) {
                return ResponseEntity.badRequest()
                        .body(Response.badRequest("Email đã tồn tại"));
            }

            User user = new User();
            user.setFullname(fullname.trim());
            user.setEmail(email.trim());
            user.setPassword(password.trim());
            user.setPhone(phone);

        // Handle categories if provided
        if (categoryIds != null && !categoryIds.isEmpty()) {
            List<Category> categories = categoryService.findAllById(categoryIds);
            user.setCategories(categories);
        }

            User savedUser = userService.save(user);
            return ResponseEntity.status(HttpStatus.CREATED)
                    .body(Response.created("Thêm user thành công", savedUser));

        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Response.error("Lỗi server: " + e.getMessage()));
        }
    }

    @Operation(summary = "Update user")
    @PutMapping("/{id}")
    public ResponseEntity<Response<User>> updateUser(
            @Parameter(description = "User ID") @PathVariable Integer id,
            @Parameter(description = "Full name") @RequestParam String fullname,
            @Parameter(description = "Email") @RequestParam String email,
            @Parameter(description = "Password") @RequestParam(required = false) String password,
            @Parameter(description = "Phone") @RequestParam(required = false) String phone,
            @Parameter(description = "Category IDs") @RequestParam(required = false) List<Integer> categoryIds) {

        try {
            // Validate user exists
            Optional<User> existingUser = userService.findById(id);
            if (!existingUser.isPresent()) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(Response.notFound("Không tìm thấy user với ID: " + id));
            }

            // Validate required fields
            if (fullname == null || fullname.trim().isEmpty()) {
                return ResponseEntity.badRequest()
                        .body(Response.badRequest("Họ tên không được để trống"));
            }

            if (email == null || email.trim().isEmpty()) {
                return ResponseEntity.badRequest()
                        .body(Response.badRequest("Email không được để trống"));
            }

            // Check if email already exists for other users
            if (userService.existsByEmailAndIdNot(email.trim(), id)) {
                return ResponseEntity.badRequest()
                        .body(Response.badRequest("Email đã tồn tại"));
            }

            User user = existingUser.get();
            user.setFullname(fullname.trim());
            user.setEmail(email.trim());
            if (password != null && !password.trim().isEmpty()) {
                user.setPassword(password.trim());
            }
            user.setPhone(phone);

            // Handle categories if provided
            if (categoryIds != null) {
                List<Category> categories = categoryService.findAllById(categoryIds);
                user.setCategories(categories);
            }

            User updatedUser = userService.save(user);
            return ResponseEntity.ok(Response.success("Cập nhật user thành công", updatedUser));

        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Response.error("Lỗi server: " + e.getMessage()));
        }
    }

    @Operation(summary = "Delete user")
    @DeleteMapping("/{id}")
    public ResponseEntity<Response<Void>> deleteUser(
            @Parameter(description = "User ID") @PathVariable Integer id) {

        try {
            Optional<User> user = userService.findById(id);
            if (!user.isPresent()) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(Response.notFound("Không tìm thấy user với ID: " + id));
            }

            userService.deleteById(id);
            return ResponseEntity.ok(Response.success("Xóa user thành công"));

        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Response.error("Lỗi server: " + e.getMessage()));
        }
    }

}
