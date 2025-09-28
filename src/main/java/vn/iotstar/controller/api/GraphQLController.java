package vn.iotstar.controller.api;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.graphql.data.method.annotation.Argument;
import org.springframework.graphql.data.method.annotation.MutationMapping;
import org.springframework.graphql.data.method.annotation.QueryMapping;
import org.springframework.graphql.data.method.annotation.SchemaMapping;
import org.springframework.stereotype.Controller;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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

    private static final Logger logger = LoggerFactory.getLogger(GraphQLController.class);

    // Product Queries
    @QueryMapping
    public List<Product> getAllProducts() {
        return productService.findAll();
    }

    @QueryMapping
    public List<Product> getProductsSortedByPrice() {
        logger.debug("GraphQL getProductsSortedByPrice called");
        try {
            List<Product> products = productService.findAllOrderByPriceAsc();
            logger.debug("GraphQL getProductsSortedByPrice returning {} products", products == null ? 0 : products.size());
            return products;
        } catch (Exception e) {
            logger.error("Error in getProductsSortedByPrice", e);
            throw e;
        }
    }

    @QueryMapping
    public List<Product> getProductsByCategory(@Argument String categoryId) {
        try {
            Integer catId = Integer.parseInt(categoryId);
            Optional<Category> category = categoryService.findById(catId);
            if (category.isPresent()) {
                // Return products for the given category id
                return productService.findByCategoryId(category.get().getId());
            }
            return List.of();
        } catch (NumberFormatException e) {
            logger.error("Invalid categoryId format: {}", categoryId);
            throw new IllegalArgumentException("Invalid categoryId format: " + categoryId);
        }
    }

    @QueryMapping
    public Product getProduct(@Argument String id) {
        try {
            Integer productId = Integer.parseInt(id);
            Optional<Product> product = productService.findById(productId);
            return product.orElse(null);
        } catch (NumberFormatException e) {
            logger.error("Invalid product ID format: {}", id);
            throw new IllegalArgumentException("Invalid product ID format: " + id);
        }
    }

    // Category Queries
    @QueryMapping
    public List<Category> getAllCategories() {
        return categoryService.findAll();
    }

    @QueryMapping
    public List<Category> getAllCategoriesSorted(
            @Argument String sortBy,
            @Argument String sortDirection) {
        logger.debug("GraphQL getAllCategoriesSorted called with sortBy={}, sortDirection={}", sortBy, sortDirection);
        try {
            if (sortBy == null || sortBy.trim().isEmpty()) {
                sortBy = "id"; // default sort by id
            }
            if (sortDirection == null || sortDirection.trim().isEmpty()) {
                sortDirection = "asc"; // default sort direction
            }

            List<Category> categories = categoryService.findAllSorted(sortBy, sortDirection);
            logger.debug("GraphQL getAllCategoriesSorted returning {} categories", categories == null ? 0 : categories.size());
            return categories;
        } catch (Exception e) {
            logger.error("Error in getAllCategoriesSorted", e);
            throw e;
        }
    }

    @QueryMapping
    public Category getCategory(@Argument String id) {
        try {
            Integer categoryId = Integer.parseInt(id);
            Optional<Category> category = categoryService.findById(categoryId);
            return category.orElse(null);
        } catch (NumberFormatException e) {
            logger.error("Invalid category ID format: {}", id);
            throw new IllegalArgumentException("Invalid category ID format: " + id);
        }
    }

    // User Queries
    @QueryMapping
    public List<User> getAllUsers() {
        return userService.findAll();
    }

    @QueryMapping
    public User getUser(@Argument String id) {
        try {
            Integer userId = Integer.parseInt(id);
            Optional<User> user = userService.findById(userId);
            return user.orElse(null);
        } catch (NumberFormatException e) {
            logger.error("Invalid user ID format: {}", id);
            throw new IllegalArgumentException("Invalid user ID format: " + id);
        }
    }

    @QueryMapping
    public User getUserByEmail(@Argument String email) {
        Optional<User> user = userService.findByEmail(email);
        return user.orElse(null);
    }

    // User Mutations
    @MutationMapping
    public User createUser(@Argument Map<String, Object> input) {
        // Validate required fields
        String fullname = (String) input.get("fullname");
        String email = (String) input.get("email");
        String password = (String) input.get("password");

        if (fullname == null || fullname.trim().isEmpty()) {
            throw new IllegalArgumentException("Fullname is required");
        }
        if (email == null || email.trim().isEmpty()) {
            throw new IllegalArgumentException("Email is required");
        }
        if (password == null || password.trim().isEmpty()) {
            throw new IllegalArgumentException("Password is required");
        }

        // Validate email format and uniqueness
        if (userService.existsByEmail(email)) {
            throw new IllegalArgumentException("Email already exists");
        }

        User user = new User();
        user.setFullname(fullname.trim());
        user.setEmail(email.trim().toLowerCase());
        user.setPassword(password);
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
    public User updateUser(@Argument String id, @Argument Map<String, Object> input) {
        try {
            Integer userId = Integer.parseInt(id);
            Optional<User> existingUser = userService.findById(userId);
            if (existingUser.isPresent()) {
                User user = existingUser.get();

                // Validate and update fields if provided
                String fullname = (String) input.get("fullname");
                String email = (String) input.get("email");
                String password = (String) input.get("password");

                if (fullname != null) {
                    if (fullname.trim().isEmpty()) {
                        throw new IllegalArgumentException("Fullname cannot be empty");
                    }
                    user.setFullname(fullname.trim());
                }

                if (email != null) {
                    if (email.trim().isEmpty()) {
                        throw new IllegalArgumentException("Email cannot be empty");
                    }
                    // Check if email is already taken by another user
                    if (userService.existsByEmailAndIdNot(email, userId)) {
                        throw new IllegalArgumentException("Email already exists");
                    }
                    user.setEmail(email.trim().toLowerCase());
                }

                if (password != null) {
                    if (password.trim().isEmpty()) {
                        throw new IllegalArgumentException("Password cannot be empty");
                    }
                    user.setPassword(password);
                }

                String phone = (String) input.get("phone");
                if (phone != null) {
                    user.setPhone(phone);
                }

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
        } catch (NumberFormatException e) {
            logger.error("Invalid user ID format: {}", id);
            throw new IllegalArgumentException("Invalid user ID format: " + id);
        }
    }

    @MutationMapping
    public Boolean deleteUser(@Argument String id) {
        try {
            Integer userId = Integer.parseInt(id);
            userService.deleteById(userId);
            return true;
        } catch (NumberFormatException e) {
            logger.error("Invalid user ID format: {}", id);
            throw new IllegalArgumentException("Invalid user ID format: " + id);
        } catch (Exception e) {
            logger.error("Error deleting user with id: {}", id, e);
            return false;
        }
    }

    // Category Mutations
    @MutationMapping
    public Category createCategory(@Argument Map<String, Object> input) {
        // Validate required fields
        String name = (String) input.get("name");
        if (name == null || name.trim().isEmpty()) {
            throw new IllegalArgumentException("Category name is required");
        }

        // Validate category name uniqueness
        if (categoryService.existsByName(name)) {
            throw new IllegalArgumentException("Category name already exists");
        }

        Category category = new Category();
        category.setName(name.trim());
        category.setImages((String) input.get("images"));
        return categoryService.save(category);
    }

    @MutationMapping
    public Category updateCategory(@Argument String id, @Argument Map<String, Object> input) {
        try {
            Integer categoryId = Integer.parseInt(id);
            Optional<Category> existingCategory = categoryService.findById(categoryId);
            if (existingCategory.isPresent()) {
                Category category = existingCategory.get();

                // Validate and update fields if provided
                String name = (String) input.get("name");
                if (name != null) {
                    if (name.trim().isEmpty()) {
                        throw new IllegalArgumentException("Category name cannot be empty");
                    }
                    // Check if name is already taken by another category
                    if (categoryService.existsByNameAndNotId(name, categoryId)) {
                        throw new IllegalArgumentException("Category name already exists");
                    }
                    category.setName(name.trim());
                }

                String images = (String) input.get("images");
                if (images != null) {
                    category.setImages(images);
                }

                return categoryService.update(category);
            }
            throw new RuntimeException("Category not found with id: " + id);
        } catch (NumberFormatException e) {
            logger.error("Invalid category ID format: {}", id);
            throw new IllegalArgumentException("Invalid category ID format: " + id);
        }
    }

    @MutationMapping
    public Boolean deleteCategory(@Argument String id) {
        try {
            Integer categoryId = Integer.parseInt(id);
            categoryService.deleteById(categoryId);
            return true;
        } catch (NumberFormatException e) {
            logger.error("Invalid category ID format: {}", id);
            throw new IllegalArgumentException("Invalid category ID format: " + id);
        } catch (Exception e) {
            logger.error("Error deleting category with id: {}", id, e);
            return false;
        }
    }

    // Product Mutations
    @MutationMapping
    public Product createProduct(@Argument Map<String, Object> input) {
        // Validate required fields
        String title = (String) input.get("title");
        Object quantityObj = input.get("quantity");
        Object priceObj = input.get("price");
        Object userIdObj = input.get("userId");

        if (title == null || title.trim().isEmpty()) {
            throw new IllegalArgumentException("Product title is required");
        }
        if (quantityObj == null) {
            throw new IllegalArgumentException("Quantity is required");
        }
        if (priceObj == null) {
            throw new IllegalArgumentException("Price is required");
        }
        if (userIdObj == null) {
            throw new IllegalArgumentException("User ID is required");
        }

        // Validate title uniqueness
        if (productService.existsByTitle(title)) {
            throw new IllegalArgumentException("Product title already exists");
        }

        // Validate quantity
        Integer quantity;
        if (quantityObj instanceof Integer) {
            quantity = (Integer) quantityObj;
        } else {
            quantity = Integer.parseInt(quantityObj.toString());
        }
        if (quantity < 0) {
            throw new IllegalArgumentException("Quantity must be >= 0");
        }

        // Validate price
        BigDecimal price;
        if (priceObj instanceof Double) {
            price = BigDecimal.valueOf((Double) priceObj);
        } else if (priceObj instanceof Integer) {
            price = BigDecimal.valueOf((Integer) priceObj);
        } else {
            price = new BigDecimal(priceObj.toString());
        }
        if (price.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Price must be > 0");
        }

        // Validate user exists
        Integer userId = (Integer) userIdObj;
        Optional<User> user = userService.findById(userId);
        if (!user.isPresent()) {
            throw new IllegalArgumentException("User not found with id: " + userId);
        }

        Product product = new Product();
        product.setTitle(title.trim());
        product.setQuantity(quantity);
        product.setDescription((String) input.get("description"));
        product.setPrice(price);
        product.setUser(user.get());

        // Handle images if provided
        String images = (String) input.get("images");
        if (images != null) {
            product.setImages(images);
        }

        // Set category if provided
        Object categoryIdObj = input.get("categoryId");
        if (categoryIdObj != null) {
            Integer categoryId = (Integer) categoryIdObj;
            Optional<Category> category = categoryService.findById(categoryId);
            if (category.isPresent()) {
                product.setCategory(category.get());
            } else {
                throw new IllegalArgumentException("Category not found with id: " + categoryId);
            }
        }

        return productService.save(product);
    }

    @MutationMapping
    public Product updateProduct(@Argument String id, @Argument Map<String, Object> input) {
        try {
            Integer productId = Integer.parseInt(id);
            Optional<Product> existingProduct = productService.findById(productId);
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
        } catch (NumberFormatException e) {
            logger.error("Invalid product ID format: {}", id);
            throw new IllegalArgumentException("Invalid product ID format: " + id);
        }
    }

    @MutationMapping
    public Boolean deleteProduct(@Argument String id) {
        try {
            Integer productId = Integer.parseInt(id);
            productService.deleteById(productId);
            return true;
        } catch (NumberFormatException e) {
            logger.error("Invalid product ID format: {}", id);
            throw new IllegalArgumentException("Invalid product ID format: " + id);
        } catch (Exception e) {
            logger.error("Error deleting product with id: {}", id, e);
            return false;
        }
    }

    // Schema mapping to convert BigDecimal price to Double for GraphQL Float scalar
    @SchemaMapping(typeName = "Product", field = "price")
    public Double price(Product product) {
        if (product.getPrice() == null) return null;
        return product.getPrice().doubleValue();
    }
}