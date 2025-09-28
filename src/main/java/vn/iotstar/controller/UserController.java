package vn.iotstar.controller;

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
import vn.iotstar.entity.Category;
import vn.iotstar.entity.User;
import vn.iotstar.service.CategoryService;
import vn.iotstar.service.UserService;

import jakarta.validation.Valid;
import java.util.Optional;

@Controller
@RequestMapping("/users")
public class UserController {

    @Autowired
    private UserService userService;

    @Autowired
    private CategoryService categoryService;

    // Redirect từ root về users
    @GetMapping("/")
    public String redirectToUsers() {
        return "redirect:/users";
    }

    @GetMapping
    public String listUsers(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "id") String sortBy,
            @RequestParam(defaultValue = "desc") String sortDir,
            @RequestParam(required = false) String keyword,
            Model model) {

        // Tạo Pageable object
        Sort sort = sortDir.equalsIgnoreCase("desc") ?
            Sort.by(sortBy).descending() : Sort.by(sortBy).ascending();
        Pageable pageable = PageRequest.of(page, size, sort);

        // Tìm kiếm với phân trang
        Page<User> userPage;
        if (keyword != null && !keyword.trim().isEmpty()) {
            userPage = userService.searchByName(keyword, pageable);
        } else {
            userPage = userService.findAll(pageable);
        }

        model.addAttribute("userPage", userPage);
        model.addAttribute("users", userPage.getContent());
        model.addAttribute("currentPage", page);
        model.addAttribute("totalPages", userPage.getTotalPages());
        model.addAttribute("totalItems", userPage.getTotalElements());
        model.addAttribute("pageSize", size);
        model.addAttribute("sortBy", sortBy);
        model.addAttribute("sortDir", sortDir);
        model.addAttribute("reverseSortDir", sortDir.equals("asc") ? "desc" : "asc");
        model.addAttribute("keyword", keyword);

        return "users/list";
    }

    @GetMapping("/new")
    public String newUserForm(Model model) {
        model.addAttribute("user", new User());
        model.addAttribute("isEdit", false);
        model.addAttribute("categories", categoryService.findAll());
        return "users/form";
    }

    @GetMapping("/view/{id}")
    public String viewUser(@PathVariable Integer id, Model model) {
        Optional<User> userOpt = userService.findById(id);
        if (userOpt.isPresent()) {
            User user = userOpt.get();
            model.addAttribute("user", user);
            return "users/view";
        }
        return "redirect:/users";
    }

    @GetMapping("/edit/{id}")
    public String editUserForm(@PathVariable Integer id, Model model) {
        Optional<User> userOpt = userService.findById(id);
        if (userOpt.isPresent()) {
            User user = userOpt.get();
            model.addAttribute("user", user);
            model.addAttribute("isEdit", true);
            model.addAttribute("categories", categoryService.findAll());
            return "users/form";
        }
        return "redirect:/users";
    }

    @PostMapping("/save")
    public String saveUser(@Valid @ModelAttribute User user,
                          BindingResult result,
                          RedirectAttributes redirectAttributes,
                          Model model) {

        // Kiểm tra validation errors
        if (result.hasErrors()) {
            model.addAttribute("isEdit", user.getId() != null);
            model.addAttribute("categories", categoryService.findAll());
            return "users/form";
        }

        // Kiểm tra email user đã tồn tại
        try {
            if (user.getId() == null) {
                // Tạo mới
                if (userService.existsByEmail(user.getEmail())) {
                    model.addAttribute("error", "Email đã tồn tại!");
                    model.addAttribute("isEdit", false);
                    model.addAttribute("categories", categoryService.findAll());
                    return "users/form";
                }
            } else {
                // Cập nhật
                if (userService.existsByEmailAndIdNot(user.getEmail(), user.getId())) {
                    model.addAttribute("error", "Email đã tồn tại!");
                    model.addAttribute("isEdit", true);
                    model.addAttribute("categories", categoryService.findAll());
                    return "users/form";
                }
            }

            userService.save(user);
            redirectAttributes.addFlashAttribute("successMessage",
                user.getId() == null ? "Tạo user thành công!" : "Cập nhật user thành công!");

        } catch (IllegalArgumentException e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
        }

        return "redirect:/users";
    }

    @PostMapping("/delete/{id}")
    public String deleteUser(@PathVariable Integer id, RedirectAttributes redirectAttributes) {
        try {
            Optional<User> userOpt = userService.findById(id);
            if (userOpt.isPresent()) {
                userService.deleteById(id);
                redirectAttributes.addFlashAttribute("successMessage", "Xóa user thành công!");
            } else {
                redirectAttributes.addFlashAttribute("errorMessage", "User không tồn tại!");
            }
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Lỗi khi xóa user: " + e.getMessage());
        }

        return "redirect:/users";
    }
}
