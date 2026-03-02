package com.helfkea.crm.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.helfkea.crm.model.CartItem
import com.helfkea.crm.model.ContragentInTask
import com.helfkea.crm.model.Order
import com.helfkea.crm.model.OrderProduct
import com.helfkea.crm.model.Product
import com.helfkea.crm.repository.OrderRepository
import com.helfkea.crm.repository.ProductRepository
import com.helfkea.crm.repository.TaskRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import android.util.Log

data class DealUiState(
    val isLoading: Boolean = false,
    val error: String? = null,
    val products: List<Product> = emptyList(),
    val filteredProducts: List<Product> = emptyList(),
    val cartItems: List<CartItem> = emptyList(),
    val selectedCategory: String? = null,
    val searchQuery: String = "",
    val orderComment: String = "",
    val selectedContragent: ContragentInTask? = null,
    val contragents: List<ContragentInTask> = emptyList(),
    val isLoadingContragents: Boolean = false,
    val isCreatingOrder: Boolean = false,
    val orderSuccess: Boolean? = null,
    val orderMessage: String? = null
)

class DealViewModel(
    private val productRepository: ProductRepository,
    private val orderRepository: OrderRepository,
    private val repository: TaskRepository = TaskRepository()
) : ViewModel() {

    private val _uiState = MutableStateFlow(DealUiState())
    val uiState: StateFlow<DealUiState> = _uiState.asStateFlow()

    init {
        loadProducts()
    }

    fun loadProducts() {
        Log.d("DealViewModel", "🔄 Вызов loadProducts()")
        _uiState.update { it.copy(isLoading = true, error = null) }

        viewModelScope.launch {
            try {
                Log.d("DealViewModel", "🚀 Запуск корутины для загрузки товаров")
                val products = productRepository.getAllProducts()
                Log.d("DealViewModel", "✅ Товары получены: ${products.size} шт")

                _uiState.update {
                    it.copy(
                        isLoading = false,
                        products = products,
                        filteredProducts = applyFilters(products, it.searchQuery, it.selectedCategory)
                    )
                }

            } catch (e: Exception) {
                Log.e("DealViewModel", "❌ Ошибка в корутине: ${e.message}", e)
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        error = "Ошибка загрузки: ${e.message}"
                    )
                }
            }
        }
    }

    // Загрузка контрагентов
    fun loadContragents() {
        _uiState.update { it.copy(isLoadingContragents = true) }

        viewModelScope.launch {
            try {
                // Используем существующий метод для получения контрагентов
                // (предполагаю, что у вас есть репозиторий с этим методом)
                val contragents = repository.getContragentsForTaskSelection() // ИЛИ orderRepository.getContragents()

                _uiState.update {
                    it.copy(
                        contragents = contragents,
                        isLoadingContragents = false
                    )
                }
            } catch (e: Exception) {
                Log.e("DealViewModel", "❌ Ошибка загрузки контрагентов: ${e.message}", e)
                _uiState.update {
                    it.copy(
                        error = "Ошибка загрузки клиентов: ${e.message}",
                        isLoadingContragents = false,
                        contragents = emptyList() // Очищаем список при ошибке
                    )
                }
            }
        }
    }

    // Выбор контрагента
    fun selectContragent(contragent: ContragentInTask?) {
        _uiState.update { it.copy(selectedContragent = contragent) }
    }

    // Очистка выбора контрагента
    fun clearContragent() {
        _uiState.update { it.copy(selectedContragent = null) }
    }

    fun addToCart(product: Product) {
        _uiState.update { currentState ->
            val existingItem = currentState.cartItems.find { it.product.productId == product.productId }
            val updatedCart = if (existingItem != null) {
                if (existingItem.quantity < product.stockCount) {
                    currentState.cartItems.map {
                        if (it.product.productId == product.productId) {
                            it.copy(quantity = it.quantity + 1)
                        } else it
                    }
                } else {
                    currentState.cartItems
                }
            } else {
                val price = product.price ?: 0.0
                currentState.cartItems + CartItem(
                    product = product,
                    quantity = 1,
                    pricePerUnit = price
                )
            }
            currentState.copy(cartItems = updatedCart)
        }
    }

    fun removeFromCart(productId: String) {
        _uiState.update { currentState ->
            val updatedCart = currentState.cartItems.filter { it.product.productId != productId }
            currentState.copy(cartItems = updatedCart)
        }
    }

    fun updateCartItemQuantity(productId: String, newQuantity: Int) {
        _uiState.update { currentState ->
            val product = currentState.products.find { it.productId == productId }
            val maxQuantity = product?.stockCount ?: 0

            val updatedCart = currentState.cartItems.map { item ->
                if (item.product.productId == productId) {
                    item.copy(quantity = newQuantity.coerceIn(1, maxQuantity))
                } else item
            }
            currentState.copy(cartItems = updatedCart)
        }
    }

    fun updateSearchQuery(query: String) {
        _uiState.update { currentState ->
            currentState.copy(
                searchQuery = query,
                filteredProducts = applyFilters(currentState.products, query, currentState.selectedCategory)
            )
        }
    }

    fun selectCategory(category: String?) {
        _uiState.update { currentState ->
            currentState.copy(
                selectedCategory = category,
                filteredProducts = applyFilters(currentState.products, currentState.searchQuery, category)
            )
        }
    }

    fun updateOrderComment(comment: String) {
        _uiState.update { it.copy(orderComment = comment) }
    }

    fun clearCart() {
        _uiState.update { it.copy(cartItems = emptyList()) }
    }

    fun clearOrderStatus() {
        _uiState.update { it.copy(
            orderSuccess = null,
            orderMessage = null,
            isCreatingOrder = false
        ) }
    }

    // Создание заказа
    fun createOrder() {
        val currentState = _uiState.value

        // Проверка корзины
        if (currentState.cartItems.isEmpty()) {
            _uiState.update {
                it.copy(
                    orderSuccess = false,
                    orderMessage = "Корзина пуста"
                )
            }
            return
        }

        // Проверка контрагента
        if (currentState.selectedContragent == null) {
            _uiState.update {
                it.copy(
                    orderSuccess = false,
                    orderMessage = "Выберите клиента для заказа"
                )
            }
            return
        }

        _uiState.update {
            it.copy(
                isCreatingOrder = true,
                orderSuccess = null,
                orderMessage = null
            )
        }

        viewModelScope.launch {
            try {
                val orderProducts = currentState.cartItems.map { item ->
                    OrderProduct(
                        productId = item.product.productId,
                        quantity = item.quantity,
                        price = item.pricePerUnit,
                        total = item.totalPrice
                    )
                }

                val order = Order(
                    clientId = currentState.selectedContragent.id, // Используем ID выбранного контрагента
                    comment = currentState.orderComment,
                    products = orderProducts
                )

                val response = orderRepository.createOrder(order)

                if (response.success) {
                    val orderNumber = response.numberOrder ?: response.guid ?: "N/A"
                    _uiState.update {
                        it.copy(
                            isCreatingOrder = false,
                            orderSuccess = true,
                            orderMessage = "Заказ успешно создан! Номер: $orderNumber",
                            cartItems = emptyList(),
                            orderComment = "",
                            selectedContragent = null // Очищаем выбранного контрагента
                        )
                    }
                } else {
                    val errorMsg = response.error ?: "Неизвестная ошибка"
                    _uiState.update {
                        it.copy(
                            isCreatingOrder = false,
                            orderSuccess = false,
                            orderMessage = "Ошибка создания заказа: $errorMsg"
                        )
                    }
                }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(
                        isCreatingOrder = false,
                        orderSuccess = false,
                        orderMessage = "Ошибка: ${e.message}"
                    )
                }
            }
        }
    }

    private fun applyFilters(
        products: List<Product>,
        searchQuery: String,
        category: String?
    ): List<Product> {
        return products.filter { product ->
            val matchesSearch = searchQuery.isEmpty() ||
                    product.name.contains(searchQuery, ignoreCase = true) ||
                    product.article.contains(searchQuery, ignoreCase = true)

            val matchesCategory = category == null ||
                    category == "Все" ||
                    product.category == category ||
                    (category == "Без категории" && (product.category.isEmpty() || product.category.isBlank()))

            matchesSearch && matchesCategory
        }
    }

    val categories: List<String>
        get() {
            val allCategories = uiState.value.products
                .map { it.category }
                .filter { it.isNotBlank() }
                .distinct()
                .sorted()

            return listOf("Все", "Без категории") + allCategories
        }

    val totalCartPrice: Double
        get() = uiState.value.cartItems.sumOf { it.totalPrice }

    val totalCartItems: Int
        get() = uiState.value.cartItems.sumOf { it.quantity }
}