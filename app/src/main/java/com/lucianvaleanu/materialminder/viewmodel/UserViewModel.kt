package com.lucianvaleanu.materialminder.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.lucianvaleanu.materialminder.model.User
import com.lucianvaleanu.materialminder.repository.database.dao.UserDAO
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class UserViewModel(private val dao: UserDAO) : ViewModel() {

    fun insertUsers(users: List<User>) {
        viewModelScope.launch(Dispatchers.IO) {
            dao.insertAll(users)
        }
    }

    suspend fun getAllUsers(): List<User> {
        return withContext(Dispatchers.IO) {
            dao.getAll()
        }
    }
}