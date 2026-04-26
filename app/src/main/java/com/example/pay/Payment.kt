package com.example.pay

import java.util.Date

enum class Category(val displayName: String) {
    FOOD("Food"),
    TRANSPORT("Transport"),
    SHOPPING("Shopping"),
    BILLS("Bills"),
    ENTERTAINMENT("Entertainment"),
    OTHER("Other");
    
    companion object {
        fun fromDisplayName(name: String): Category? = values().find { it.displayName == name }
    }
}

data class Payment(
    val id: Long = System.currentTimeMillis(),
    val description: String,
    val amount: Double,
    val category: Category = Category.OTHER,
    val date: Date = Date()
)
