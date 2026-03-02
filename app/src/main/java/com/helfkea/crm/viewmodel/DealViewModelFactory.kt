
package com.helfkea.crm.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.helfkea.crm.repository.OrderRepository
import com.helfkea.crm.repository.ProductRepository

class DealViewModelFactory(
    private val productRepository: ProductRepository,
    private val orderRepository: OrderRepository
) : ViewModelProvider.Factory {

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(DealViewModel::class.java)) {
            return DealViewModel(productRepository, orderRepository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
