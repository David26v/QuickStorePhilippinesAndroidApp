package com.example.quickstorephilippinesandroidapp.ui.admin.model


data class User(
    val id: String,
    val name: String,
    val email: String,
    val role: String,
    val isActive: Boolean,
    val createdAt: String,
    val lastLogin: String? = null,
    val authMethods: List<String> = emptyList()
)