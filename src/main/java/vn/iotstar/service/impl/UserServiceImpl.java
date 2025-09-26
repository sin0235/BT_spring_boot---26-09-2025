package vn.iotstar.service.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import vn.iotstar.entity.User;
import vn.iotstar.repository.UserRepository;
import vn.iotstar.service.UserService;

import java.util.List;
import java.util.Optional;

@Service
public class UserServiceImpl implements UserService {

    @Autowired
    private UserRepository userRepository;

    @Override
    public List<User> findAll() {
        return userRepository.findAll();
    }

    @Override
    public Page<User> findAll(Pageable pageable) {
        return userRepository.findAll(pageable);
    }

    @Override
    public Optional<User> findById(Integer id) {
        return userRepository.findById(id);
    }

    @Override
    public Optional<User> findByEmail(String email) {
        return userRepository.findByEmail(email);
    }

    @Override
    public User save(User user) {
        return userRepository.save(user);
    }

    @Override
    public User update(User user) {
        return userRepository.save(user);
    }

    @Override
    public void deleteById(Integer id) {
        userRepository.deleteById(id);
    }

    @Override
    public boolean existsByEmail(String email) {
        return userRepository.existsByEmail(email);
    }

    @Override
    public boolean existsByEmailAndIdNot(String email, Integer id) {
        return userRepository.existsByEmailAndIdNot(email, id);
    }

    @Override
    public List<User> findByFullnameContaining(String name) {
        return userRepository.findByFullnameContaining(name);
    }

    @Override
    public Page<User> findByFullnameContaining(String name, Pageable pageable) {
        // Since UserRepository doesn't have this method, we'll implement it by filtering
        // For now, return all users if name is provided
        if (name == null || name.trim().isEmpty()) {
            return userRepository.findAll(pageable);
        }
        // This is a simple implementation - in production, you might want to add a custom query
        List<User> allUsers = userRepository.findAll();
        List<User> filteredUsers = allUsers.stream()
                .filter(user -> user.getFullname().toLowerCase().contains(name.toLowerCase()))
                .toList();

        // For pagination, we need to manually implement it
        int start = (int) pageable.getOffset();
        int end = Math.min((start + pageable.getPageSize()), filteredUsers.size());
        List<User> pageContent = filteredUsers.subList(start, end);

        return new org.springframework.data.domain.PageImpl<>(pageContent, pageable, filteredUsers.size());
    }

    @Override
    public Page<User> searchByName(String name, Pageable pageable) {
        if (name == null || name.trim().isEmpty()) {
            return userRepository.findAll(pageable);
        }
        return findByFullnameContaining(name, pageable);
    }
}