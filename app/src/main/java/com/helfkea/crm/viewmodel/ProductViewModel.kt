package com.helfkea.crm.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.helfkea.crm.model.CartItem
import com.helfkea.crm.model.Product
import com.helfkea.crm.repository.ProductRepository
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class ProductViewModel : ViewModel() {
    private val repository = ProductRepository()

    // Все продукты
    private val _allProducts = MutableStateFlow<List<Product>>(emptyList())
    val allProducts: StateFlow<List<Product>> = _allProducts.asStateFlow()

    // Корзина
    private val _cartItems = MutableStateFlow<List<CartItem>>(emptyList())
    val cartItems: StateFlow<List<CartItem>> = _cartItems.asStateFlow()

    // Состояние загрузки
    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    // Ошибка
    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()

    // Поиск
    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    // Фильтр категорий
    private val _selectedCategory = MutableStateFlow<String?>(null)
    val selectedCategory: StateFlow<String?> = _selectedCategory.asStateFlow()

    // Отфильтрованные продукты
    val filteredProducts: StateFlow<List<Product>> = combine(
        _allProducts,
        _searchQuery,
        _selectedCategory
    ) { products, query, category ->
        var filtered = products

        // Поиск по названию и артикулу
        if (query.isNotBlank()) {
            val lowercaseQuery = query.lowercase()
            filtered = filtered.filter {
                it.name.lowercase().contains(lowercaseQuery) ||
                        it.article.lowercase().contains(lowercaseQuery)
            }
        }

        // Фильтр по категории
        category?.let {
            filtered = filtered.filter { it.category == category }
        }

        filtered
    }.stateIn(viewModelScope, SharingStarted.Lazily, emptyList())

    // Все категории
    val categories: StateFlow<List<String>> = _allProducts.map { products ->
        products.map { it.category }
            .distinct()
            .filter { it.isNotBlank() }
            .sorted()
    }.stateIn(viewModelScope, SharingStarted.Lazily, emptyList())

    // Общая сумма корзины
    val cartTotal: StateFlow<Double> = _cartItems.map { items ->
        items.sumOf { it.totalPrice }
    }.stateIn(viewModelScope, SharingStarted.Lazily, 0.0)

    // Общее количество товаров в корзине
    val cartItemsCount: StateFlow<Int> = _cartItems.map { items ->
        items.sumOf { it.quantity }
    }.stateIn(viewModelScope, SharingStarted.Lazily, 0)

    init {
        loadProducts()
    }

    fun loadProducts() {
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null
            try {
                val result = repository.getAllProducts()
                _allProducts.value = result
            } catch (e: Exception) {
                _error.value = "Ошибка загрузки товаров: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }

    // Работа с корзиной
    fun addToCart(product: Product) {
        val currentItems = _cartItems.value.toMutableList()
        val existingItem = currentItems.find { it.product.productId == product.productId }

        if (existingItem != null) {
            // Если товар уже в корзине, увеличиваем количество
            if (existingItem.canIncrease(product.stockCount)) {
                existingItem.quantity++
            }
        } else {
            // Добавляем новый товар в корзину
            val price = product.price ?: 0.0
            currentItems.add(CartItem(product = product, quantity = 1, pricePerUnit = price))
        }

        _cartItems.value = currentItems
    }

    fun removeFromCart(productId: String) {
        val currentItems = _cartItems.value.toMutableList()
        currentItems.removeAll { it.product.productId == productId }
        _cartItems.value = currentItems
    }

    fun updateCartItemQuantity(productId: String, newQuantity: Int) {
        if (newQuantity < 1) {
            removeFromCart(productId)
            return
        }

        val currentItems = _cartItems.value.toMutableList()
        val item = currentItems.find { it.product.productId == productId }

        item?.let {
            val maxQuantity = it.product.stockCount
            if (newQuantity <= maxQuantity) {
                it.quantity = newQuantity
                _cartItems.value = currentItems
            }
        }
    }

    fun increaseCartItemQuantity(productId: String) {
        val currentItems = _cartItems.value.toMutableList()
        val item = currentItems.find { it.product.productId == productId }

        item?.let {
            if (it.increaseQuantity(it.product.stockCount)) {
                _cartItems.value = currentItems
            }
        }
    }

    fun decreaseCartItemQuantity(productId: String) {
        val currentItems = _cartItems.value.toMutableList()
        val item = currentItems.find { it.product.productId == productId }

        item?.let {
            if (it.decreaseQuantity()) {
                _cartItems.value = currentItems
            } else {
                // Если количество стало 0, удаляем из корзины
                removeFromCart(productId)
            }
        }
    }

    fun clearCart() {
        _cartItems.value = emptyList()
    }

    // Поиск и фильтрация
    fun updateSearchQuery(query: String) {
        _searchQuery.value = query
    }

    fun updateSelectedCategory(category: String?) {
        _selectedCategory.value = category
    }

    fun clearFilters() {
        _searchQuery.value = ""
        _selectedCategory.value = null
    }

    // Получить товар по ID
    fun getProductById(productId: String): Product? {
        return _allProducts.value.find { it.productId == productId }
    }

    // Проверка, есть ли товар в корзине
    fun isInCart(productId: String): Boolean {
        return _cartItems.value.any { it.product.productId == productId }
    }

    // Получить количество товара в корзине
    fun getCartQuantity(productId: String): Int {
        return _cartItems.value.find { it.product.productId == productId }?.quantity ?: 0
    }
}