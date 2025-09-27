# Hệ thống quản lý cửa hàng - GraphQL + Spring Boot 3

Hệ thống quản lý cửa hàng sử dụng GraphQL API và Spring Boot 3 với giao diện web hiện đại.

## Tính năng

### GraphQL API
- **Hiển thị tất cả sản phẩm theo giá từ thấp đến cao**
- **Lấy tất cả sản phẩm của một danh mục**
- **CRUD đầy đủ cho User, Product, Category**
- **AJAX rendering trên các trang JSP/HTML**

### Giao diện Web
- Dashboard hiển thị thống kê và sản phẩm theo giá
- Quản lý sản phẩm (thêm, sửa, xóa, tìm kiếm)
- Quản lý danh mục
- Quản lý người dùng
- Responsive design với Bootstrap 5

## Cài đặt và Chạy

### 1. Yêu cầu hệ thống
- Java 17+
- MySQL 8.0+
- Maven 3.6+

### 2. Cấu hình Database
1. Tạo database MySQL:
```sql
CREATE DATABASE IF NOT EXISTS ShopDatabase;
```

2. Chạy file SQL để tạo bảng và dữ liệu mẫu:
```bash
mysql -u root -p ShopDatabase < database_setup.sql
```

3. Cập nhật thông tin kết nối database trong file `application.properties`:
```properties
spring.datasource.url=jdbc:mysql://localhost:3306/ShopDatabase
spring.datasource.username=your_username
spring.datasource.password=your_password
```

### 3. Build và chạy
```bash
mvn clean package -DskipTests
java -jar target/btweb-0.0.1-SNAPSHOT.jar
```

Hoặc chạy trực tiếp:
```bash
mvn spring-boot:run
```

### 3. Chạy ứng dụng
```bash
mvn spring-boot:run
```

Ứng dụng sẽ chạy trên: http://localhost:8080

## Cấu trúc Database

### Bảng Category
- `id`: ID danh mục (Primary Key)
- `name`: Tên danh mục
- `images`: Hình ảnh danh mục

### Bảng User
- `id`: ID người dùng (Primary Key)
- `fullname`: Họ tên đầy đủ
- `email`: Email (Unique)
- `password`: Mật khẩu
- `phone`: Số điện thoại

### Bảng Product
- `id`: ID sản phẩm (Primary Key)
- `title`: Tên sản phẩm
- `quantity`: Số lượng
- `description`: Mô tả
- `price`: Giá sản phẩm
- `images`: Hình ảnh sản phẩm
- `category_id`: ID danh mục (Foreign Key)
- `userid`: ID người dùng (Foreign Key)

### Bảng UserCategory (Many-to-Many)
- `userid`: ID người dùng
- `categoryid`: ID danh mục
- `created_date`: Ngày tạo

## GraphQL API

### GraphQL Endpoint
```
POST http://localhost:8080/graphql
```

### Các Query chính

#### 1. Lấy tất cả sản phẩm theo giá (thấp đến cao)
```graphql
query {
  getProductsSortedByPrice {
    id
    title
    price
    quantity
    images
    description
    user {
      id
      fullname
    }
    category {
      id
      name
    }
  }
}
```

#### 2. Lấy sản phẩm theo danh mục
```graphql
query {
  getProductsByCategory(categoryId: 1) {
    id
    title
    price
    quantity
    user {
      fullname
    }
  }
}
```

#### 3. Lấy tất cả danh mục
```graphql
query {
  getAllCategories {
    id
    name
    images
    users {
      id
      fullname
    }
  }
}
```

### Các Mutation chính

#### 1. Tạo sản phẩm mới
```graphql
mutation {
  createProduct(input: {
    title: "iPhone 15 Pro"
    quantity: 50
    description: "Điện thoại iPhone 15 Pro màu xanh, 256GB"
    price: 28990000
    images: "iphone15_pro.jpg"
    userId: 1
    categoryId: 1
  }) {
    id
    title
    price
    quantity
    images
    user {
      fullname
    }
    category {
      name
    }
  }
}
```

#### 2. Cập nhật sản phẩm

## Static default image

The project uses a default image fallback for cases where an image fails to load. Place a `default_image.png` file in one of these locations:

- `src/main/resources/static/images/default_image.png` (recommended — served from classpath)
- project root `default_image.png` (convenient during development; WebConfig maps this path as a fallback)

Templates and client JS use `/images/default_image.png` as the fallback URL when an image can't be loaded.
```graphql
mutation {
  updateProduct(id: 1, input: {
    title: "iPhone 15 Pro Max"
    quantity: 30
    price: 32000000
    images: "iphone15_pro_max.jpg"
    categoryId: 1
  }) {
    id
    title
    price
    quantity
    images
    category {
      name
    }
  }
}
```

#### 3. Xóa sản phẩm
```graphql
mutation {
  deleteProduct(id: 1)
}
```

## REST API Endpoints

### Product API
- `GET /api/product` - Lấy tất cả sản phẩm (có phân trang)
- `GET /api/product/{id}` - Lấy sản phẩm theo ID
- `GET /api/product/sorted-by-price` - Lấy sản phẩm theo giá
- `POST /api/product` - Tạo sản phẩm mới
- `PUT /api/product/{id}` - Cập nhật sản phẩm
- `DELETE /api/product/{id}` - Xóa sản phẩm

### Category API
- `GET /api/category` - Lấy tất cả danh mục
- `GET /api/category/{id}` - Lấy danh mục theo ID
- `POST /api/category` - Tạo danh mục mới
- `PUT /api/category/{id}` - Cập nhật danh mục
- `DELETE /api/category/{id}` - Xóa danh mục

### User API
- `GET /api/user` - Lấy tất cả người dùng
- `GET /api/user/{id}` - Lấy người dùng theo ID
- `POST /api/user` - Tạo người dùng mới
- `PUT /api/user/{id}` - Cập nhật người dùng
- `DELETE /api/user/{id}` - Xóa người dùng

## Giao diện Web

### Dashboard (`/`)
- Hiển thị thống kê tổng quan
- Danh sách sản phẩm theo giá từ thấp đến cao
- Các liên kết nhanh đến các trang quản lý

### Quản lý sản phẩm (`/products`)
- Hiển thị danh sách sản phẩm với tìm kiếm và sắp xếp
- Modal thêm/sửa sản phẩm
- Phân trang
- Xóa sản phẩm

## Cấu trúc dự án

```
src/main/java/vn/iotstar/
├── BtwebApplication.java          # Main application class
├── controller/
│   ├── HomeController.java        # Home page controller
│   ├── api/
│   │   ├── GraphQLController.java # GraphQL resolvers
│   │   ├── ProductApiController.java  # REST API for products
│   │   ├── CategoryApiController.java # REST API for categories
│   │   └── UserApiController.java     # REST API for users
│   └── admin/
│       └── AdminController.java   # Admin controllers
├── entity/                        # JPA entities
│   ├── User.java
│   ├── Product.java
│   └── Category.java
├── repository/                    # JPA repositories
├── service/                      # Business logic services
├── model/                        # Response models
└── config/                       # Configuration classes

src/main/resources/
├── templates/                    # HTML templates
│   ├── dashboard.html            # Main dashboard
│   └── products/list.html        # Product management
├── static/                       # Static resources
├── graphql/                      # GraphQL schema
└── application.properties        # Configuration
```

## Công nghệ sử dụng

- **Backend**: Spring Boot 3, Spring Data JPA, GraphQL
- **Database**: MySQL
- **Frontend**: HTML5, Bootstrap 5, JavaScript, jQuery
- **Build Tool**: Maven
- **Documentation**: Swagger/OpenAPI

## Tính năng bảo mật

- Validation dữ liệu đầu vào
- Xử lý lỗi toàn diện
- Response format chuẩn
- CORS configuration

## Hỗ trợ và Phát triển

Để phát triển thêm tính năng:

1. **Thêm GraphQL schema** trong `src/main/resources/graphql/schema.graphqls`
2. **Tạo resolver methods** trong `GraphQLController.java`
3. **Tạo REST API** trong các controller tương ứng
4. **Cập nhật giao diện** trong các template HTML

## License

Dự án này được phát triển cho mục đích học tập và demo.
