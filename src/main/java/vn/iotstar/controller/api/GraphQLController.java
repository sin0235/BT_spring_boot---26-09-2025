package vn.iotstar.controller.api;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.graphql.data.method.annotation.Argument;
import org.springframework.graphql.data.method.annotation.MutationMapping;
import org.springframework.graphql.data.method.annotation.QueryMapping;
import org.springframework.stereotype.Controller;
import vn.iotstar.entity.Category;
import vn.iotstar.entity.Product;
import vn.iotstar.entity.User;
import vn.iotstar.service.CategoryService;
import vn.iotstar.service.ProductService;
import vn.iotstar.service.UserService;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Controller
public class GraphQLController {

    @Autowired
    private ProductService productService;
    
    @Autowired
    private CategoryService categoryService;
    
    @Autowired
    private UserService userService;

    // Product Queries
    @QueryMapping
    public List<Product> getAllProducts() {
        return productService.findAll();
    }

    @QueryMapping
    public List<Product> getProductsSortedByPrice() {
        return productService.findAllOrderByPriceAsc();
    }

    @QueryMapping
    public List<Product> getProductsByCategory(@Argument Integer categoryId) {
        Optional<Category> category = categoryService.findById(categoryId);
        if (category.isPresent()) {
            // Return products for the given category id
            return productService.findByCategoryId(category.get().getId());
        }
        return List.of();
    }

    @QueryMapping
    public Product getProduct(@Argument Integer id) {
        Optional<Product> product = productService.findById(id);
        return product.orElse(null);
    }

    // Category Queries
    @QueryMapping
    public List<Category> getAllCategories() {
        return categoryService.findAll();
    }

    @QueryMapping
    public Category getCategory(@Argument Integer id) {
        Optional<Category> category = categoryService.findById(id);
        return category.orElse(null);
    }

    // User Queries
    @QueryMapping
    public List<User> getAllUsers() {
        return userService.findAll();
    }

    @QueryMapping
    public User getUser(@Argument Integer id) {
        Optional<User> user = userService.findById(id);
        return user.orElse(null);
    }

    @QueryMapping
    public User getUserByEmail(@Argument String email) {
        Optional<User> user = userService.findByEmail(email);
        return user.orElse(null);
    }

    // User Mutations
    @MutationMapping
    public User createUser(@Argument Map<String, Object> input) {
        User user = new User();
        user.setFullname((String) input.get("fullname"));
        user.setEmail((String) input.get("email"));
        user.setPassword((String) input.get("password"));
        user.setPhone((String) input.get("phone"));

        // Handle categories if provided
        @SuppressWarnings("unchecked")
        List<Integer> categoryIds = (List<Integer>) input.get("categoryIds");
        if (categoryIds != null && !categoryIds.isEmpty()) {
            List<Category> categories = categoryService.findAllById(categoryIds);
            user.setCategories(categories);
        }

        return userService.save(user);
    }

    @MutationMapping
    public User updateUser(@Argument Integer id, @Argument Map<String, Object> input) {
        Optional<User> existingUser = userService.findById(id);
        if (existingUser.isPresent()) {
            User user = existingUser.get();
            user.setFullname((String) input.get("fullname"));
            user.setEmail((String) input.get("email"));
            if (input.get("password") != null) {
                user.setPassword((String) input.get("password"));
            }
            user.setPhone((String) input.get("phone"));

            // Handle categories if provided
            @SuppressWarnings("unchecked")
            List<Integer> categoryIds = (List<Integer>) input.get("categoryIds");
            if (categoryIds != null) {
                List<Category> categories = categoryService.findAllById(categoryIds);
                user.setCategories(categories);
            }

            return userService.update(user);
        }
        throw new RuntimeException("User not found with id: " + id);
    }

    @MutationMapping
    public Boolean deleteUser(@Argument Integer id) {
        try {
            userService.deleteById(id);
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    // Category Mutations
    @MutationMapping
    public Category createCategory(@Argument Map<String, Object> input) {
        Category category = new Category();
        category.setName((String) input.get("name"));
        category.setImages((String) input.get("images"));
        return categoryService.save(category);
    }

    @MutationMapping
    public Category updateCategory(@Argument Integer id, @Argument Map<String, Object> input) {
        Optional<Category> existingCategory = categoryService.findById(id);
        if (existingCategory.isPresent()) {
            Category category = existingCategory.get();
            category.setName((String) input.get("name"));
            category.setImages((String) input.get("images"));
            return categoryService.update(category);
        }
        throw new RuntimeException("Category not found with id: " + id);
    }

    @MutationMapping
    public Boolean deleteCategory(@Argument Integer id) {
        try {
            categoryService.deleteById(id);
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    // Product Mutations
    @MutationMapping
    public Product createProduct(@Argument Map<String, Object> input) {
        Product product = new Product();
        product.setTitle((String) input.get("title"));
        product.setQuantity((Integer) input.get("quantity"));
        product.setDescription((String) input.get("description"));

        // Handle images if provided
        String images = (String) input.get("images");
        if (images != null) {
            product.setImages(images);
        }

        // Convert price to BigDecimal
        Object priceObj = input.get("price");
        BigDecimal price;
        if (priceObj instanceof Double) {
            price = BigDecimal.valueOf((Double) priceObj);
        } else if (priceObj instanceof Integer) {
            price = BigDecimal.valueOf((Integer) priceObj);
        } else {
            price = new BigDecimal(priceObj.toString());
        }
        product.setPrice(price);

        // Set user
        Integer userId = (Integer) input.get("userId");
        Optional<User> user = userService.findById(userId);
        if (user.isPresent()) {
            product.setUser(user.get());
        } else {
            throw new RuntimeException("User not found with id: " + userId);
        }

        // Set category if provided
        Object categoryIdObj = input.get("categoryId");
        if (categoryIdObj != null) {
            Integer categoryId = (Integer) categoryIdObj;
            Optional<Category> category = categoryService.findById(categoryId);
            if (category.isPresent()) {
                product.setCategory(category.get());
            } else {
                throw new RuntimeException("Category not found with id: " + categoryId);
            }
        }

        return productService.save(product);
    }

    @MutationMapping
    public Product updateProduct(@Argument Integer id, @Argument Map<String, Object> input) {
        Optional<Product> existingProduct = productService.findById(id);
        if (existingProduct.isPresent()) {
            Product product = existingProduct.get();
            product.setTitle((String) input.get("title"));
            product.setQuantity((Integer) input.get("quantity"));
            product.setDescription((String) input.get("description"));

            // Handle images if provided
            String images = (String) input.get("images");
            if (images != null) {
                product.setImages(images);
            }

            // Convert price to BigDecimal
            Object priceObj = input.get("price");
            BigDecimal price;
            if (priceObj instanceof Double) {
                price = BigDecimal.valueOf((Double) priceObj);
            } else if (priceObj instanceof Integer) {
                price = BigDecimal.valueOf((Integer) priceObj);
            } else {
                price = new BigDecimal(priceObj.toString());
            }
            product.setPrice(price);

            // Update user if provided
            Object userIdObj = input.get("userId");
            if (userIdObj != null) {
                Integer userId = (Integer) userIdObj;
                Optional<User> user = userService.findById(userId);
                if (user.isPresent()) {
                    product.setUser(user.get());
                } else {
                    throw new RuntimeException("User not found with id: " + userId);
                }
            }

            // Update category if provided
            Object categoryIdObj = input.get("categoryId");
            if (categoryIdObj != null) {
                Integer categoryId = (Integer) categoryIdObj;
                Optional<Category> category = categoryService.findById(categoryId);
                if (category.isPresent()) {
                    product.setCategory(category.get());
                } else {
                    throw new RuntimeException("Category not found with id: " + categoryId);
                }
            }

            return productService.update(product);
        }
        throw new RuntimeException("Product not found with id: " + id);
    }

    @MutationMapping
    public Boolean deleteProduct(@Argument Integer id) {
        try {
            productService.deleteById(id);
            return true;
        } catch (Exception e) {
            return false;
        }
    }
}