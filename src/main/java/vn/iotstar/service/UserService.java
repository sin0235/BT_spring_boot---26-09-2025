package vn.iotstar.service;

import vn.iotstar.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import java.util.List;
import java.util.Optional;

public interface UserService {
    List<User> findAll();
    Page<User> findAll(Pageable pageable);
    Optional<User> findById(Integer id);
    Optional<User> findByEmail(String email);
    User save(User user);
    User update(User user);
    void deleteById(Integer id);
    boolean existsByEmail(String email);
    boolean existsByEmailAndIdNot(String email, Integer id);
    List<User> findByFullnameContaining(String name);
    Page<User> searchByName(String name, Pageable pageable);
}