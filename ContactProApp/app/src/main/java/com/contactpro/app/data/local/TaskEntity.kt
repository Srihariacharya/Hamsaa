package com.contactpro.app.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "tasks")
data class TaskEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val title: String,
    val description: String?,
    val dueDate: String?,
    val priority: String, // LOW, MEDIUM, HIGH
    val status: String,   // PENDING, IN_PROGRESS, COMPLETED
    val contactId: Long?,
    val userId: Long,
    val createdAt: Long = System.currentTimeMillis()
)
