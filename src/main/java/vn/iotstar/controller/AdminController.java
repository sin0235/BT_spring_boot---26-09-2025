package vn.iotstar.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/admin")
public class AdminController {
    
    @GetMapping("/dashboard")
    public String dashboard(Model model) {
        model.addAttribute("title", "Admin Dashboard - Category, Product & User Management");
        return "admin/dashboard";
    }
    
    @GetMapping("")
    public String adminHome() {
        return "redirect:/admin/dashboard";
    }
}
