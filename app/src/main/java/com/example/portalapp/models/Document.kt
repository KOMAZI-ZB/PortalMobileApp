package com.example.portalapp.models

data class Document(
    val id: Int,
    val title: String,
    val fileUrl: String,
    val uploadedAt: String,
    val uploadedBy: String,
    val uploadedByUserName: String,
    val moduleId: Int?,
    val source: String = ""
)
