package vn.iotstar.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import vn.iotstar.entity.Category;
import vn.iotstar.repository.CategoryRepository;

import java.util.List;
import java.util.Optional;

@Service
@Transactional
public class CategoryService {
    
    @Autowired
    private CategoryRepository categoryRepository;
    
    // Basic CRUD operations
    public List<Category> findAll() {
        return categoryRepository.findAll();
    }

    public Page<Category> findAll(Pageable pageable) {
        return categoryRepository.findAll(pageable);
    }

    public List<Category> findAllSorted(String sortBy, String sortDirection) {
        Sort sort = sortDirection.equalsIgnoreCase("desc") ?
            Sort.by(sortBy).descending() : Sort.by(sortBy).ascending();
        return categoryRepository.findAll(sort);
    }

    public Optional<Category> findById(Integer id) {
        return categoryRepository.findById(id);
    }

    public List<Category> findAllById(List<Integer> ids) {
        return categoryRepository.findAllById(ids);
    }

    // Save operations
    public Category save(Category category) {
        return categoryRepository.save(category);
    }

    // Delete operations
    public void deleteById(Integer id) {
        categoryRepository.deleteById(id);
    }

    // Additional methods for GraphQL
    public Category update(Category category) {
        return categoryRepository.save(category);
    }

    public boolean existsById(Integer id) {
        return categoryRepository.existsById(id);
    }

    public boolean existsByName(String name) {
        return categoryRepository.existsByNameIgnoreCase(name);
    }

    public boolean existsByNameAndNotId(String name, Integer id) {
        return categoryRepository.existsByNameIgnoreCaseAndIdNot(name, id);
    }
    
    // Count operations
    public long count() {
        return categoryRepository.count();
    }

    public long countAllCategories() {
        return categoryRepository.count();
    }

    public Page<Category> searchByName(String name, Pageable pageable) {
        if (name == null || name.trim().isEmpty()) {
            return categoryRepository.findAll(pageable);
        }
        return categoryRepository.findByNameContainingIgnoreCase(name.trim(), pageable);
    }
    
    
    // Create and Update operations for AdminController
    public Category createCategory(Category category) {
        // Validate category name doesn't exist
        if (existsByName(category.getName())) {
            throw new IllegalArgumentException("Category name already exists");
        }
        return categoryRepository.save(category);
    }

    public Category updateCategory(Category category) {
        // Validate category exists
        if (!existsById(category.getId())) {
            throw new IllegalArgumentException("Category not found");
        }
        // Validate category name doesn't exist for other categories
        if (existsByNameAndNotId(category.getName(), category.getId())) {
            throw new IllegalArgumentException("Category name already exists");
        }
        return categoryRepository.save(category);
    }
}