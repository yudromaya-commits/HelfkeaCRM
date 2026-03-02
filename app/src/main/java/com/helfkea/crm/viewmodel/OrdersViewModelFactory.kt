
package com.helfkea.crm.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.helfkea.crm.repository.OrdersRepository

class OrdersViewModelFactory(
    private val ordersRepository: OrdersRepository
) : ViewModelProvider.Factory {

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(OrdersViewModel::class.java)) {
            return OrdersViewModel(ordersRepository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
