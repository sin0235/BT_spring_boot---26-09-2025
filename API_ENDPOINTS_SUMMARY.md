# Tóm tắt API Endpoints đã tối ưu hóa

## 1. Product API (`/api/product`)

### Hiển thị tất cả product có price từ thấp đến cao
- **GET** `/api/product/sorted-by-price`
- **Mô tả**: Lấy danh sách tất cả sản phẩm được sắp xếp theo giá từ thấp đến cao
- **Response**: Danh sách sản phẩm được sắp xếp theo giá

### Lấy tất cả product của 01 category
- **GET** `/api/product/category/{categoryId}`
- **Mô tả**: Lấy danh sách tất cả sản phẩm thuộc một category cụ thể
- **Parameters**: 
  - `categoryId` (path): ID của category
- **Response**: Danh sách sản phẩm thuộc category

### CRUD Product
- **GET** `/api/product` - Lấy danh sách sản phẩm (có phân trang và tìm kiếm)
- **GET** `/api/product/{id}` - Lấy sản phẩm theo ID
- **POST** `/api/product` - Tạo sản phẩm mới
- **PUT** `/api/product/{id}` - Cập nhật sản phẩm
- **DELETE** `/api/product/{id}` - Xóa sản phẩm

## 2. User API (`/api/user`)

### CRUD User
- **GET** `/api/user` - Lấy danh sách user (có phân trang và tìm kiếm)
- **GET** `/api/user/{id}` - Lấy user theo ID
- **POST** `/api/user` - Tạo user mới
- **PUT** `/api/user/{id}` - Cập nhật user
- **DELETE** `/api/user/{id}` - Xóa user

## 3. Category API (`/api/category`)

### CRUD Category
- **GET** `/api/category` - Lấy danh sách category (có phân trang và tìm kiếm)
- **GET** `/api/category/{id}` - Lấy category theo ID
- **POST** `/api/category` - Tạo category mới
- **PUT** `/api/category/{id}` - Cập nhật category
- **DELETE** `/api/category/{id}` - Xóa category

## Các chức năng đã loại bỏ

### Product API
- ❌ Lấy sản phẩm theo khoảng giá
- ❌ Lấy sản phẩm có giảm giá
- ❌ Lấy sản phẩm hết hàng
- ❌ Lấy sản phẩm sắp hết hàng

### User API
- ❌ Lấy user theo email

### Category API
- ❌ Lấy sản phẩm theo category (đã chuyển sang Product API)

## Các chức năng còn lại (theo yêu cầu)

✅ **Hiển thị tất cả product có price từ thấp đến cao**
- Endpoint: `GET /api/product/sorted-by-price`

✅ **Lấy tất cả product của 01 category**
- Endpoint: `GET /api/product/category/{categoryId}`

✅ **CRUD bảng User**
- Endpoints: `GET`, `POST`, `PUT`, `DELETE /api/user`

✅ **CRUD bảng Product**
- Endpoints: `GET`, `POST`, `PUT`, `DELETE /api/product`

✅ **CRUD bảng Category**
- Endpoints: `GET`, `POST`, `PUT`, `DELETE /api/category`

✅ **Loại bỏ các chức năng không cần thiết**
- Đã loại bỏ các API endpoints không liên quan đến yêu cầu chính