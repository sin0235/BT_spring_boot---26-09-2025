package vn.iotstar.service;

import org.springframework.web.multipart.MultipartFile;

import java.nio.file.Path;

public interface StorageService {
    
    void init();
    
    String store(MultipartFile file);
    
    String store(MultipartFile file, String prefix);
    
    Path load(String filename);
    
    void delete(String filename);
    
    boolean exists(String filename);
}
