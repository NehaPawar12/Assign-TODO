package com.example.assigntodo

import java.util.UUID

data class Users(
    val id : String = UUID.randomUUID().toString(),
    val userType : String? = null,
    val userId : String? = null,
    val userName : String? = null,
    val userEmail : String? = null,
    val userPassword : String? = null,
    val userImage : String? = null
)
