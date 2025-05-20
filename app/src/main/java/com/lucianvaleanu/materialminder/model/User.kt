package com.lucianvaleanu.materialminder.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.time.Instant

@Entity(tableName = "user")
data class User(
    @PrimaryKey val id: Int,
    val email: String,
    val password: String,
    val createdAt: Instant
)