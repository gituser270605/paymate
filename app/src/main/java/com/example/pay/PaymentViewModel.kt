package com.example.pay

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn

class PaymentViewModel : ViewModel() {
    private val _payments = MutableStateFlow<List<Payment>>(emptyList())
    
    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    private val _selectedCategory = MutableStateFlow<Category?>(null)
    val selectedCategory: StateFlow<Category?> = _selectedCategory.asStateFlow()

    val payments: StateFlow<List<Payment>> = combine(_payments, _searchQuery, _selectedCategory) { list, query, category ->
        list.filter { payment ->
            val matchesQuery = if (query.isEmpty()) true else payment.description.contains(query, ignoreCase = true)
            val matchesCategory = if (category == null) true else payment.category == category
            matchesQuery && matchesCategory
        }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    private val _budget = MutableStateFlow(0.0)
    val budget: StateFlow<Double> = _budget.asStateFlow()

    val totalSpend: StateFlow<Double> = _payments.map { list ->
        list.sumOf { it.amount }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = 0.0
    )

    fun addPayment(description: String, amount: Double, category: Category) {
        val newPayment = Payment(description = description, amount = amount, category = category)
        _payments.value = _payments.value + newPayment
    }

    fun deletePayment(id: Long) {
        _payments.value = _payments.value.filter { it.id != id }
    }

    fun updatePayment(id: Long, description: String, amount: Double, category: Category) {
        _payments.value = _payments.value.map {
            if (it.id == id) it.copy(description = description, amount = amount, category = category) else it
        }
    }

    fun getPaymentById(id: Long): Payment? {
        return _payments.value.find { it.id == id }
    }

    fun setBudget(amount: Double) {
        _budget.value = amount
    }

    fun updateSearchQuery(query: String) {
        _searchQuery.value = query
    }

    fun setSelectedCategory(category: Category?) {
        _selectedCategory.value = category
    }
}
