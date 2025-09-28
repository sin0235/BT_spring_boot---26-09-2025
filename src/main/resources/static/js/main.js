// Main JavaScript file for AJAX operations
// Category and Product Management System

$(document).ready(function() {
    // Initialize storage service
    storageService.init();
    
    // Load initial data
    loadCategories();
    loadProducts();
    
    // Setup pagination
    setupPagination();
    
    // Setup search functionality
    setupSearch();
});

// ==============================================
// STORAGE SERVICE
// ==============================================
const storageService = {
    init: function() {
        console.log('Storage service initialized');
    }
};

// ==============================================
// UTILITY FUNCTIONS
// ==============================================
function showMessage(type, message) {
    const alertClass = type === 'success' ? 'alert-success' : 'alert-danger';
    const icon = type === 'success' ? 'fas fa-check-circle' : 'fas fa-exclamation-circle';
    
    const alertHtml = `
        <div class="alert ${alertClass} alert-dismissible fade show" role="alert">
            <i class="${icon} me-2"></i>
            ${message}
            <button type="button" class="btn-close" data-bs-dismiss="alert"></button>
        </div>
    `;
    
    $('.alert').remove(); // Remove existing alerts
    $('.container').first().prepend(alertHtml);
    
    // Auto hide after 5 seconds
    setTimeout(function() {
        $('.alert').alert('close');
    }, 5000);
}

function formatCurrency(amount) {
    return new Intl.NumberFormat('vi-VN', {
        style: 'currency',
        currency: 'VND'
    }).format(amount);
}

function formatDate(dateString) {
    const date = new Date(dateString);
    return date.toLocaleDateString('vi-VN');
}

// ==============================================
// CATEGORY FUNCTIONS
// ==============================================
let currentCategoryPage = 0;
let currentCategorySize = 10;
let currentCategorySearch = '';

function loadCategories(page = 0, size = 10, search = '') {
    currentCategoryPage = page;
    currentCategorySize = size;
    currentCategorySearch = search;
    
    let url = `/api/category?page=${page}&size=${size}`;
    if (search) {
        url += `&q=${encodeURIComponent(search)}`;
    }
    
    console.debug('loadCategories called', { page: page, size: size, search: search });
    $.ajax({
        url: url,
        method: 'GET',
        dataType: 'json',
        success: function(response) {
            console.debug('loadCategories response', response);
            if (response.status === 'success') {
                        renderCategoriesTable(response.body.content);
                        renderCategoriesPagination(response.body);
                    } else {
                        showMessage('error', response.message);
                    }
        },
        error: function(xhr, status, error) {
            console.error('Error loading categories:', error);
            showMessage('error', 'Lỗi khi tải danh sách category');
        }
    });
}

function renderCategoriesTable(categories) {
    const tbody = $('#categoriesTableBody');
    tbody.empty();
    console.debug('renderCategoriesTable called, count=', categories ? categories.length : 0);
    
    if (categories.length === 0) {
        tbody.append(`
            <tr>
                <td colspan="5" class="text-center">Không có dữ liệu</td>
            </tr>
        `);
        return;
    }
    
    categories.forEach(function(category, index) {
        // Do not load category icon images; show a simple placeholder icon instead
        const icon = '<i class="fas fa-tags text-muted"></i>';
            
        const row = `
            <tr>
                <td>${currentCategoryPage * currentCategorySize + index + 1}</td>
                <td>${icon}</td>
                <td>${category.catename}</td>
                <td>${formatDate(category.createdAt)}</td>
                <td>
                    <button class="btn btn-sm btn-primary me-1" onclick="editCategory(${category.cateid})">
                        <i class="fas fa-edit"></i>
                    </button>
                    <button class="btn btn-sm btn-danger" onclick="deleteCategory(${category.cateid})">
                        <i class="fas fa-trash"></i>
                    </button>
                </td>
            </tr>
        `;
        tbody.append(row);
    });
}

function renderCategoriesPagination(pageData) {
    const pagination = $('#categoriesPagination');
    pagination.empty();
    
    if (pageData.totalPages <= 1) return;
    
    // Previous button
    const prevDisabled = pageData.first ? 'disabled' : '';
    pagination.append(`
        <li class="page-item ${prevDisabled}">
            <a class="page-link" href="#" onclick="loadCategories(${pageData.number - 1}, ${currentCategorySize}, '${currentCategorySearch}')">Trước</a>
        </li>
    `);
    
    // Page numbers
    const start = Math.max(0, pageData.number - 2);
    const end = Math.min(pageData.totalPages, start + 5);
    
    for (let i = start; i < end; i++) {
        const active = i === pageData.number ? 'active' : '';
        pagination.append(`
            <li class="page-item ${active}">
                <a class="page-link" href="#" onclick="loadCategories(${i}, ${currentCategorySize}, '${currentCategorySearch}')">${i + 1}</a>
            </li>
        `);
    }
    
    // Next button
    const nextDisabled = pageData.last ? 'disabled' : '';
    pagination.append(`
        <li class="page-item ${nextDisabled}">
            <a class="page-link" href="#" onclick="loadCategories(${pageData.number + 1}, ${currentCategorySize}, '${currentCategorySearch}')">Sau</a>
        </li>
    `);
}

function createCategory() {
    const formData = new FormData();
    formData.append('categoryName', $('#categoryName').val());
    
    const iconFile = $('#categoryIcon')[0].files[0];
    if (iconFile) {
        formData.append('icon', iconFile);
    }
    
    $.ajax({
        url: '/api/category/addCategory',
        method: 'POST',
        data: formData,
        processData: false,
        contentType: false,
        success: function(response) {
            if (response.status === 'created' || response.status === 'success') {
                showMessage('success', response.message);
                $('#categoryModal').modal('hide');
                resetCategoryForm();
                loadCategories(0, currentCategorySize, currentCategorySearch);
            } else {
                showMessage('error', response.message);
            }
        },
        error: function(xhr, status, error) {
            console.error('Error creating category:', error);
            const errorMessage = xhr.responseJSON ? xhr.responseJSON.message : 'Lỗi khi thêm category';
            showMessage('error', errorMessage);
        }
    });
}

function editCategory(categoryId) {
    // Get category details
    $.ajax({
        url: `/api/category/${categoryId}`,
        method: 'GET',
        success: function(response) {
            if (response.status === 'success') {
                const category = response.body;
                $('#categoryId').val(category.cateid);
                $('#categoryName').val(category.catename);
                $('#categoryModalTitle').text('Sửa Category');
                $('#categoryModal').modal('show');
            } else {
                showMessage('error', response.message);
            }
        },
        error: function(xhr, status, error) {
            console.error('Error getting category:', error);
            showMessage('error', 'Lỗi khi lấy thông tin category');
        }
    });
}

function updateCategory() {
    const categoryId = $('#categoryId').val();
    const formData = new FormData();
    formData.append('categoryId', categoryId);
    formData.append('categoryName', $('#categoryName').val());
    
    const iconFile = $('#categoryIcon')[0].files[0];
    if (iconFile) {
        formData.append('icon', iconFile);
    }
    
    $.ajax({
        url: '/api/category/updateCategory',
        method: 'PUT',
        data: formData,
        processData: false,
        contentType: false,
        success: function(response) {
            if (response.status === 'success') {
                showMessage('success', response.message);
                $('#categoryModal').modal('hide');
                resetCategoryForm();
                loadCategories(currentCategoryPage, currentCategorySize, currentCategorySearch);
            } else {
                showMessage('error', response.message);
            }
        },
        error: function(xhr, status, error) {
            console.error('Error updating category:', error);
            const errorMessage = xhr.responseJSON ? xhr.responseJSON.message : 'Lỗi khi cập nhật category';
            showMessage('error', errorMessage);
        }
    });
}

function deleteCategory(categoryId) {
    if (!confirm('Bạn có chắc chắn muốn xóa category này?')) {
        return;
    }
    
    $.ajax({
        url: `/api/category/deleteCategory?categoryId=${categoryId}`,
        method: 'DELETE',
        success: function(response) {
            if (response.status === 'success') {
                showMessage('success', response.message);
                loadCategories(currentCategoryPage, currentCategorySize, currentCategorySearch);
            } else {
                showMessage('error', response.message);
            }
        },
        error: function(xhr, status, error) {
            console.error('Error deleting category:', error);
            const errorMessage = xhr.responseJSON ? xhr.responseJSON.message : 'Lỗi khi xóa category';
            showMessage('error', errorMessage);
        }
    });
}

function resetCategoryForm() {
    $('#categoryId').val('');
    $('#categoryName').val('');
    $('#categoryIcon').val('');
    $('#categoryModalTitle').text('Thêm Category');
}

// ==============================================
// PRODUCT FUNCTIONS
// ==============================================
let currentProductPage = 0;
let currentProductSize = 10;
let currentProductSearch = '';
let currentProductCategoryFilter = '';

function loadProducts(page = 0, size = 10, search = '', categoryId = '') {
    currentProductPage = page;
    currentProductSize = size;
    currentProductSearch = search;
    currentProductCategoryFilter = categoryId;
    
    let url = `/api/product?page=${page}&size=${size}`;
    if (search) {
        url += `&q=${encodeURIComponent(search)}`;
    }
    if (categoryId) {
        url += `&categoryId=${categoryId}`;
    }
    
    console.debug('loadProducts called', { page: page, size: size, search: search, categoryId: categoryId });
    $.ajax({
        url: url,
        method: 'GET',
        dataType: 'json',
        success: function(response) {
            console.debug('loadProducts response', response);
            if (response.status === 'success') {
                renderProductsTable(response.body.content);
                renderProductsPagination(response.body);
            } else {
                showMessage('error', response.message);
            }
        },
        error: function(xhr, status, error) {
            console.error('Error loading products:', error);
            showMessage('error', 'Lỗi khi tải danh sách sản phẩm');
        }
    });
}

function renderProductsTable(products) {
    const tbody = $('#productsTableBody');
    tbody.empty();
    console.debug('renderProductsTable called, count=', products ? products.length : 0, 'first=', products && products.length ? products[0] : null);
    
    if (products.length === 0) {
        tbody.append(`
            <tr>
                <td colspan="8" class="text-center">Không có dữ liệu</td>
            </tr>
        `);
        return;
    }
    
    products.forEach(function(product, index) {
        // Do not load product images in tables; use placeholder
        const image = '<i class="fas fa-box text-muted"></i>';
            
        const statusBadge = product.status ? 
            '<span class="badge bg-success">Hoạt động</span>' : 
            '<span class="badge bg-danger">Ngừng bán</span>';
            
        const row = `
            <tr>
                <td>${currentProductPage * currentProductSize + index + 1}</td>
                <td>${image}</td>
                <td>${product.productName}</td>
                <td>${product.categoryName || 'N/A'}</td>
                <td>${formatCurrency(product.unitPrice)}</td>
                <td>${product.quantity}</td>
                <td>${product.discount}%</td>
                <td>${statusBadge}</td>
                <td>
                    <button class="btn btn-sm btn-info me-1" onclick="viewProduct(${product.productId})">
                        <i class="fas fa-eye"></i>
                    </button>
                    <button class="btn btn-sm btn-primary me-1" onclick="editProduct(${product.productId})">
                        <i class="fas fa-edit"></i>
                    </button>
                    <button class="btn btn-sm btn-danger" onclick="deleteProduct(${product.productId})">
                        <i class="fas fa-trash"></i>
                    </button>
                </td>
            </tr>
        `;
        tbody.append(row);
    });
}

function renderProductsPagination(pageData) {
    const pagination = $('#productsPagination');
    pagination.empty();
    
    if (pageData.totalPages <= 1) return;
    
    // Similar pagination logic as categories
    const prevDisabled = pageData.first ? 'disabled' : '';
    pagination.append(`
        <li class="page-item ${prevDisabled}">
            <a class="page-link" href="#" onclick="loadProducts(${pageData.number - 1}, ${currentProductSize}, '${currentProductSearch}', '${currentProductCategoryFilter}')">Trước</a>
        </li>
    `);
    
    const start = Math.max(0, pageData.number - 2);
    const end = Math.min(pageData.totalPages, start + 5);
    
    for (let i = start; i < end; i++) {
        const active = i === pageData.number ? 'active' : '';
        pagination.append(`
            <li class="page-item ${active}">
                <a class="page-link" href="#" onclick="loadProducts(${i}, ${currentProductSize}, '${currentProductSearch}', '${currentProductCategoryFilter}')">${i + 1}</a>
            </li>
        `);
    }
    
    const nextDisabled = pageData.last ? 'disabled' : '';
    pagination.append(`
        <li class="page-item ${nextDisabled}">
            <a class="page-link" href="#" onclick="loadProducts(${pageData.number + 1}, ${currentProductSize}, '${currentProductSearch}', '${currentProductCategoryFilter}')">Sau</a>
        </li>
    `);
}

function createProduct() {
    const formData = new FormData();
    formData.append('productName', $('#productName').val());
    formData.append('quantity', $('#productQuantity').val());
    formData.append('unitPrice', $('#productUnitPrice').val());
    formData.append('description', $('#productDescription').val());
    formData.append('discount', $('#productDiscount').val() || '0');
    formData.append('status', $('#productStatus').val());
    formData.append('categoryId', $('#productCategory').val());
    
    const imageFile = $('#productImages')[0].files[0];
    if (imageFile) {
        formData.append('images', imageFile);
    }
    
    $.ajax({
        url: '/api/product',
        method: 'POST',
        data: formData,
        processData: false,
        contentType: false,
        success: function(response) {
            if (response.status === 'created' || response.status === 'success') {
                showMessage('success', response.message);
                $('#productModal').modal('hide');
                resetProductForm();
                loadProducts(0, currentProductSize, currentProductSearch, currentProductCategoryFilter);
            } else {
                showMessage('error', response.message);
            }
        },
        error: function(xhr, status, error) {
            console.error('Error creating product:', error);
            const errorMessage = xhr.responseJSON ? xhr.responseJSON.message : 'Lỗi khi thêm sản phẩm';
            showMessage('error', errorMessage);
        }
    });
}

function editProduct(productId) {
    $.ajax({
        url: `/api/product/${productId}`,
        method: 'GET',
        success: function(response) {
            if (response.status === 'success') {
                const product = response.body;
                $('#productId').val(product.productId);
                $('#productName').val(product.productName);
                $('#productQuantity').val(product.quantity);
                $('#productUnitPrice').val(product.unitPrice);
                $('#productDescription').val(product.description);
                $('#productDiscount').val(product.discount);
                $('#productStatus').val(product.status.toString());
                $('#productCategory').val(product.categoryId);
                $('#productModalTitle').text('Sửa Sản phẩm');
                $('#productModal').modal('show');
            } else {
                showMessage('error', response.message);
            }
        },
        error: function(xhr, status, error) {
            console.error('Error getting product:', error);
            showMessage('error', 'Lỗi khi lấy thông tin sản phẩm');
        }
    });
}

function updateProduct() {
    const productId = $('#productId').val();
    const formData = new FormData();
    formData.append('productName', $('#productName').val());
    formData.append('quantity', $('#productQuantity').val());
    formData.append('unitPrice', $('#productUnitPrice').val());
    formData.append('description', $('#productDescription').val());
    formData.append('discount', $('#productDiscount').val() || '0');
    formData.append('status', $('#productStatus').val());
    formData.append('categoryId', $('#productCategory').val());
    
    const imageFile = $('#productImages')[0].files[0];
    if (imageFile) {
        formData.append('images', imageFile);
    }
    
    $.ajax({
        url: `/api/product/${productId}`,
        method: 'PUT',
        data: formData,
        processData: false,
        contentType: false,
        success: function(response) {
            if (response.status === 'success') {
                showMessage('success', response.message);
                $('#productModal').modal('hide');
                resetProductForm();
                loadProducts(currentProductPage, currentProductSize, currentProductSearch, currentProductCategoryFilter);
            } else {
                showMessage('error', response.message);
            }
        },
        error: function(xhr, status, error) {
            console.error('Error updating product:', error);
            const errorMessage = xhr.responseJSON ? xhr.responseJSON.message : 'Lỗi khi cập nhật sản phẩm';
            showMessage('error', errorMessage);
        }
    });
}

function deleteProduct(productId) {
    if (!confirm('Bạn có chắc chắn muốn xóa sản phẩm này?')) {
        return;
    }
    
    $.ajax({
        url: `/api/product/${productId}`,
        method: 'DELETE',
        success: function(response) {
            if (response.status === 'success') {
                showMessage('success', response.message);
                loadProducts(currentProductPage, currentProductSize, currentProductSearch, currentProductCategoryFilter);
            } else {
                showMessage('error', response.message);
            }
        },
        error: function(xhr, status, error) {
            console.error('Error deleting product:', error);
            const errorMessage = xhr.responseJSON ? xhr.responseJSON.message : 'Lỗi khi xóa sản phẩm';
            showMessage('error', errorMessage);
        }
    });
}

function viewProduct(productId) {
    $.ajax({
        url: `/api/product/${productId}`,
        method: 'GET',
        success: function(response) {
            if (response.status === 'success') {
                const product = response.body;
                // Display product details in a modal or redirect to view page
                alert(`Sản phẩm: ${product.productName}\nGiá: ${formatCurrency(product.unitPrice)}\nSố lượng: ${product.quantity}`);
            } else {
                showMessage('error', response.message);
            }
        },
        error: function(xhr, status, error) {
            console.error('Error getting product:', error);
            showMessage('error', 'Lỗi khi lấy thông tin sản phẩm');
        }
    });
}

function resetProductForm() {
    $('#productId').val('');
    $('#productName').val('');
    $('#productQuantity').val('');
    $('#productUnitPrice').val('');
    $('#productDescription').val('');
    $('#productDiscount').val('0');
    $('#productStatus').val('true');
    $('#productCategory').val('');
    $('#productImages').val('');
    $('#productModalTitle').text('Thêm Sản phẩm');
}

function loadCategoriesForSelect() {
    $.ajax({
        url: '/api/category?size=1000',
        method: 'GET',
        success: function(response) {
            if (response.status === 'success') {
                const select = $('#productCategory');
                select.empty();
                select.append('<option value="">Chọn category</option>');
                
                response.body.content.forEach(function(category) {
                    select.append(`<option value="${category.cateid}">${category.catename}</option>`);
                });
            }
        },
        error: function(xhr, status, error) {
            console.error('Error loading categories for select:', error);
        }
    });
}

// ==============================================
// SEARCH AND PAGINATION SETUP
// ==============================================
function setupSearch() {
    // Category search
    $('#categorySearch').on('input', function() {
        const search = $(this).val();
        loadCategories(0, currentCategorySize, search);
    });
    
    // Product search
    $('#productSearch').on('input', function() {
        const search = $(this).val();
        loadProducts(0, currentProductSize, search, currentProductCategoryFilter);
    });
    
    // Product category filter
    $('#productCategoryFilter').on('change', function() {
        const categoryId = $(this).val();
        loadProducts(0, currentProductSize, currentProductSearch, categoryId);
    });
}

function setupPagination() {
    // Page size handlers
    $('#categoryPageSize').on('change', function() {
        const size = parseInt($(this).val());
        loadCategories(0, size, currentCategorySearch);
    });
    
    $('#productPageSize').on('change', function() {
        const size = parseInt($(this).val());
        loadProducts(0, size, currentProductSearch, currentProductCategoryFilter);
    });
}

// ==============================================
// MODAL HANDLERS
// ==============================================
function openCategoryModal() {
    resetCategoryForm();
    $('#categoryModal').modal('show');
}

function openProductModal() {
    resetProductForm();
    loadCategoriesForSelect();
    $('#productModal').modal('show');
}

function saveCategoryForm() {
    const categoryId = $('#categoryId').val();
    if (categoryId) {
        updateCategory();
    } else {
        createCategory();
    }
}

function saveProductForm() {
    const productId = $('#productId').val();
    if (productId) {
        updateProduct();
    } else {
        createProduct();
    }
}
