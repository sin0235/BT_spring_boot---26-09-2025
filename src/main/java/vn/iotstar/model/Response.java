package vn.iotstar.model;

import com.fasterxml.jackson.annotation.JsonInclude;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class Response<T> {
    
    private String status;
    private String message;
    private T body;
    private Long totalElements;
    private Integer totalPages;
    private Integer currentPage;
    private Integer pageSize;
    
    // Constructors
    public Response() {}
    
    public Response(String status, String message) {
        this.status = status;
        this.message = message;
    }
    
    public Response(String status, String message, T body) {
        this.status = status;
        this.message = message;
        this.body = body;
    }
    
    // Static factory methods for success responses
    public static <T> Response<T> success(String message) {
        return new Response<>("success", message);
    }
    
    public static <T> Response<T> success(String message, T body) {
        return new Response<>("success", message, body);
    }
    
    public static <T> Response<T> created(String message, T body) {
        return new Response<>("created", message, body);
    }
    
    // Static factory methods for error responses
    public static <T> Response<T> error(String message) {
        return new Response<>("error", message);
    }
    
    public static <T> Response<T> badRequest(String message) {
        return new Response<>("bad_request", message);
    }
    
    public static <T> Response<T> notFound(String message) {
        return new Response<>("not_found", message);
    }
    
    // Pagination support
    public Response<T> withPagination(Long totalElements, Integer totalPages, Integer currentPage, Integer pageSize) {
        this.totalElements = totalElements;
        this.totalPages = totalPages;
        this.currentPage = currentPage;
        this.pageSize = pageSize;
        return this;
    }
    
    // Getters and Setters
    public String getStatus() {
        return status;
    }
    
    public void setStatus(String status) {
        this.status = status;
    }
    
    public String getMessage() {
        return message;
    }
    
    public void setMessage(String message) {
        this.message = message;
    }
    
    public T getBody() {
        return body;
    }
    
    public void setBody(T body) {
        this.body = body;
    }
    
    public Long getTotalElements() {
        return totalElements;
    }
    
    public void setTotalElements(Long totalElements) {
        this.totalElements = totalElements;
    }
    
    public Integer getTotalPages() {
        return totalPages;
    }
    
    public void setTotalPages(Integer totalPages) {
        this.totalPages = totalPages;
    }
    
    public Integer getCurrentPage() {
        return currentPage;
    }
    
    public void setCurrentPage(Integer currentPage) {
        this.currentPage = currentPage;
    }
    
    public Integer getPageSize() {
        return pageSize;
    }
    
    public void setPageSize(Integer pageSize) {
        this.pageSize = pageSize;
    }
}
