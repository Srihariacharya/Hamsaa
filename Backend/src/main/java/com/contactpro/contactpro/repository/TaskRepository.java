package com.contactpro.contactpro.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

import com.contactpro.contactpro.model.Task;

@Repository
public interface TaskRepository extends JpaRepository<Task, Long> {
    List<Task> findByUserIdOrderByDueDateAsc(Long userId);

    long countByUserId(Long userId);

    long countByUserIdAndStatus(Long userId, String status);
}
