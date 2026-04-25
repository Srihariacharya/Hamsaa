package com.contactpro.app.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.contactpro.app.data.local.AppDatabase
import com.contactpro.app.data.local.TaskEntity
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch

class TaskViewModel(application: Application) : AndroidViewModel(application) {
    private val taskDao = AppDatabase.getDatabase(application).taskDao()

    fun getTasks(userId: Long): Flow<List<TaskEntity>> = taskDao.getTasks(userId)

    fun addTask(task: TaskEntity) {
        viewModelScope.launch {
            taskDao.insertTask(task)
        }
    }

    fun updateTask(task: TaskEntity) {
        viewModelScope.launch {
            taskDao.updateTask(task)
        }
    }

    fun deleteTask(task: TaskEntity) {
        viewModelScope.launch {
            taskDao.deleteTask(task)
        }
    }

    fun toggleTaskCompletion(task: TaskEntity) {
        viewModelScope.launch {
            val newStatus = if (task.status == "COMPLETED") "PENDING" else "COMPLETED"
            taskDao.updateTask(task.copy(status = newStatus))
        }
    }

    fun getTasksByContact(contactId: Long): Flow<List<TaskEntity>> = taskDao.getTasksByContact(contactId)
}
