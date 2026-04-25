package com.contactpro.contactpro.controller;

import org.springframework.web.bind.annotation.*;
import java.util.List;
import java.util.stream.Collectors;

import com.contactpro.contactpro.model.Task;
import com.contactpro.contactpro.model.User;
import com.contactpro.contactpro.dto.TaskRequest;
import com.contactpro.contactpro.dto.TaskResponse;
import com.contactpro.contactpro.repository.TaskRepository;
import com.contactpro.contactpro.repository.UserRepository;

@RestController
@RequestMapping("/api/tasks")
@CrossOrigin(origins = "http://localhost:5173")
public class TaskController {

    private final TaskRepository taskRepository;
    private final UserRepository userRepository;

    public TaskController(TaskRepository taskRepository, UserRepository userRepository) {
        this.taskRepository = taskRepository;
        this.userRepository = userRepository;
    }

    @PostMapping
    public TaskResponse createTask(@RequestBody TaskRequest request) {
        User user = userRepository.findById(request.getUserId())
                .orElseThrow(() -> new RuntimeException("User not found"));

        Task task = new Task();
        task.setTitle(request.getTitle());
        task.setDescription(request.getDescription());
        task.setDueDate(request.getDueDate());
        task.setStatus(request.getStatus() != null ? request.getStatus() : "pending");
        task.setUser(user);

        Task saved = taskRepository.save(task);
        return new TaskResponse(saved.getId(), saved.getTitle(), saved.getDescription(), saved.getDueDate(), saved.getStatus());
    }

    @GetMapping("/user/{userId}")
    public List<TaskResponse> getTasksByUser(@PathVariable Long userId) {
        return taskRepository.findByUserIdOrderByDueDateAsc(userId)
                .stream()
                .map(t -> new TaskResponse(t.getId(), t.getTitle(), t.getDescription(), t.getDueDate(), t.getStatus()))
                .collect(Collectors.toList());
    }

    @PatchMapping("/{taskId}/status")
    public TaskResponse updateTaskStatus(@PathVariable Long taskId, @RequestParam String status) {
        Task task = taskRepository.findById(taskId)
                .orElseThrow(() -> new RuntimeException("Task not found"));
        task.setStatus(status);
        Task updated = taskRepository.save(task);
        return new TaskResponse(updated.getId(), updated.getTitle(), updated.getDescription(), updated.getDueDate(), updated.getStatus());
    }

    @DeleteMapping("/{taskId}")
    public String deleteTask(@PathVariable Long taskId) {
        taskRepository.deleteById(taskId);
        return "Task deleted successfully";
    }
}
