package vn.iotstar.controller;

import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import vn.iotstar.entity.Product;
import vn.iotstar.entity.Category;
import vn.iotstar.entity.User;
import vn.iotstar.service.ProductService;
import vn.iotstar.service.CategoryService;
import vn.iotstar.service.UserService;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

@Controller
@RequestMapping("/products")
public class ProductController {

    @Autowired
    private ProductService productService;

    @Autowired
    private CategoryService categoryService;

    @Autowired
    private UserService userService;

    @GetMapping
    public String listProducts(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String search,
            @RequestParam(required = false) Integer categoryId,
            Model model) {

        Pageable pageable = PageRequest.of(page, size, Sort.by("createDate").descending());
        Page<Product> productPage;

        if (categoryId != null) {
            if (search != null && !search.trim().isEmpty()) {
                productPage = productService.searchByNameAndCategory(search, categoryId, pageable);
            } else {
                productPage = productService.findByCategoryId(categoryId, pageable);
            }
        } else if (search != null && !search.trim().isEmpty()) {
            productPage = productService.searchByName(search, pageable);
        } else {
            productPage = productService.findAll(pageable);
        }

        List<Category> categories = categoryService.findAll();

        model.addAttribute("products", productPage.getContent());
        model.addAttribute("currentPage", page);
        model.addAttribute("totalPages", productPage.getTotalPages());
        model.addAttribute("totalElements", productPage.getTotalElements());
        model.addAttribute("search", search);
        model.addAttribute("categoryId", categoryId);
        model.addAttribute("categories", categories);

        return "products/list";
    }

    @GetMapping("/new")
    public String showAddForm(Model model) {
        Product product = new Product();
        List<Category> categories = categoryService.findAll();
        List<User> users = userService.findAll();

        model.addAttribute("product", product);
        model.addAttribute("categories", categories);
        model.addAttribute("users", users);
        model.addAttribute("isEdit", false);

        return "products/form";
    }

    @GetMapping("/edit/{id}")
    public String showEditForm(@PathVariable Integer id, Model model, RedirectAttributes redirectAttributes) {
        Optional<Product> product = productService.findById(id);

        if (product.isPresent()) {
            List<Category> categories = categoryService.findAll();
            List<User> users = userService.findAll();

            model.addAttribute("product", product.get());
            model.addAttribute("categories", categories);
            model.addAttribute("users", users);
            model.addAttribute("isEdit", true);

            return "products/form";
        } else {
            redirectAttributes.addFlashAttribute("errorMessage", "Không tìm thấy sản phẩm với ID: " + id);
            return "redirect:/products";
        }
    }

    @PostMapping("/save")
    public String saveProduct(@Valid @ModelAttribute("product") Product product,
                             BindingResult bindingResult,
                             Model model,
                             RedirectAttributes redirectAttributes) {

        // Check validation errors
        if (bindingResult.hasErrors()) {
            List<Category> categories = categoryService.findAll();
            List<User> users = userService.findAll();

            model.addAttribute("categories", categories);
            model.addAttribute("users", users);
            model.addAttribute("isEdit", product.getId() != null);

            return "products/form";
        }

        try {
            // Set category and user objects from IDs
            if (product.getCategoryId() != null) {
                Optional<Category> category = categoryService.findById(product.getCategoryId());
                if (category.isPresent()) {
                    product.setCategory(category.get());
                }
            }

            if (product.getUserId() != null) {
                Optional<User> user = userService.findById(product.getUserId());
                if (user.isPresent()) {
                    product.setUser(user.get());
                }
            }

            // Validate business rules
            if (product.getQuantity() != null && product.getQuantity() < 0) {
                bindingResult.rejectValue("quantity", "error.quantity", "Số lượng phải >= 0");
            }

            if (product.getPrice() != null && product.getPrice().compareTo(BigDecimal.ZERO) <= 0) {
                bindingResult.rejectValue("price", "error.price", "Giá phải > 0");
            }

            if (product.getDiscount() != null && (product.getDiscount() < 0 || product.getDiscount() > 100)) {
                bindingResult.rejectValue("discount", "error.discount", "Giảm giá phải từ 0-100");
            }

            // Validate required fields
            if (product.getCategoryId() == null) {
                bindingResult.rejectValue("categoryId", "error.categoryId", "Vui lòng chọn danh mục");
            }

            if (product.getUserId() == null) {
                bindingResult.rejectValue("userId", "error.userId", "Vui lòng chọn người tạo");
            }

            if (bindingResult.hasErrors()) {
                List<Category> categories = categoryService.findAll();
                List<User> users = userService.findAll();

                model.addAttribute("categories", categories);
                model.addAttribute("users", users);
                model.addAttribute("isEdit", product.getId() != null);

                return "products/form";
            }

            // Check if title already exists
            if (product.getId() == null) {
                // New product
                if (productService.existsByTitle(product.getTitle())) {
                    bindingResult.rejectValue("title", "error.title", "Tên sản phẩm đã tồn tại");
                    List<Category> categories = categoryService.findAll();
                    List<User> users = userService.findAll();

                    model.addAttribute("categories", categories);
                    model.addAttribute("users", users);
                    model.addAttribute("isEdit", false);

                    return "products/form";
                }
            } else {
                // Update existing product
                if (productService.existsByTitleAndNotId(product.getTitle(), product.getId())) {
                    bindingResult.rejectValue("title", "error.title", "Tên sản phẩm đã tồn tại");
                    List<Category> categories = categoryService.findAll();
                    List<User> users = userService.findAll();

                    model.addAttribute("categories", categories);
                    model.addAttribute("users", users);
                    model.addAttribute("isEdit", true);

                    return "products/form";
                }
            }

            Product savedProduct = productService.save(product);
            redirectAttributes.addFlashAttribute("successMessage",
                product.getId() == null ? "Thêm sản phẩm thành công!" : "Cập nhật sản phẩm thành công!");

            return "redirect:/products";

        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Lỗi khi lưu sản phẩm: " + e.getMessage());
            return "redirect:/products";
        }
    }

    @GetMapping("/delete/{id}")
    public String deleteProduct(@PathVariable Integer id, RedirectAttributes redirectAttributes) {
        try {
            Optional<Product> product = productService.findById(id);

            if (product.isPresent()) {
                productService.deleteById(id);
                redirectAttributes.addFlashAttribute("successMessage", "Xóa sản phẩm thành công!");
            } else {
                redirectAttributes.addFlashAttribute("errorMessage", "Không tìm thấy sản phẩm với ID: " + id);
            }

        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Lỗi khi xóa sản phẩm: " + e.getMessage());
        }

        return "redirect:/products";
    }

    @GetMapping("/view/{id}")
    public String viewProduct(@PathVariable Integer id, Model model, RedirectAttributes redirectAttributes) {
        Optional<Product> product = productService.findById(id);

        if (product.isPresent()) {
            model.addAttribute("product", product.get());
            return "products/view";
        } else {
            redirectAttributes.addFlashAttribute("errorMessage", "Không tìm thấy sản phẩm với ID: " + id);
            return "redirect:/products";
        }
    }
}
