package com.helfkea.crm.viewmodel

import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.helfkea.crm.model.*
import com.helfkea.crm.repository.TaskRepository
import com.helfkea.crm.utils.convertUriToBase64
import com.helfkea.crm.utils.getFileSize
import com.helfkea.crm.utils.getMimeType
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import kotlinx.coroutines.delay
import android.content.Context

data class AttachmentUiState(
    val uri: Uri,
    val fileName: String,
    val fileSize: Long,
    val fileType: String,
    val isUploading: Boolean = false,
    val uploadError: String? = null
)

class CreateTaskViewModel(
    private val repository: TaskRepository = TaskRepository()
) : ViewModel() {

    private val _name = MutableStateFlow("")
    val name: StateFlow<String> = _name.asStateFlow()

    private val _description = MutableStateFlow("")
    val description: StateFlow<String> = _description.asStateFlow()

    private val _status = MutableStateFlow(TaskStatus.ASSIGNED)
    val status: StateFlow<String> = _status.asStateFlow()

    private val _executorId = MutableStateFlow("")
    val executorId: StateFlow<String> = _executorId.asStateFlow()

    private val _executionDate = MutableStateFlow("")
    val executionDate: StateFlow<String> = _executionDate.asStateFlow()

    private val _important = MutableStateFlow(false)
    val important: StateFlow<Boolean> = _important.asStateFlow()

    private val _contragentId = MutableStateFlow<String?>(null)
    val contragentId: StateFlow<String?> = _contragentId.asStateFlow()

    private val _contragentName = MutableStateFlow<String?>(null)
    val contragentName: StateFlow<String?> = _contragentName.asStateFlow()

    private val _attachments = MutableStateFlow<List<AttachmentUiState>>(emptyList())
    val attachments: StateFlow<List<AttachmentUiState>> = _attachments.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _createTaskResult = MutableStateFlow<CreateTaskResponse?>(null)
    val createTaskResult: StateFlow<CreateTaskResponse?> = _createTaskResult.asStateFlow()

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()

    private val _users = MutableStateFlow<List<User>>(emptyList())
    val users: StateFlow<List<User>> = _users.asStateFlow()

    private val _isLoadingUsers = MutableStateFlow(false)
    val isLoadingUsers: StateFlow<Boolean> = _isLoadingUsers.asStateFlow()

    private val _contragents = MutableStateFlow<List<ContragentInTask>>(emptyList())
    val contragents: StateFlow<List<ContragentInTask>> = _contragents.asStateFlow()

    private val _isLoadingContragents = MutableStateFlow(false)
    val isLoadingContragents: StateFlow<Boolean> = _isLoadingContragents.asStateFlow()

    private val _showContragentDialog = MutableStateFlow(false)
    val showContragentDialog: StateFlow<Boolean> = _showContragentDialog.asStateFlow()

    // Обновление полей
    fun updateName(value: String) { _name.value = value }
    fun updateDescription(value: String) { _description.value = value }
    fun updateStatus(value: String) { _status.value = value }
    fun updateProducer(value: String) { _executorId.value = value }
    fun updateExecutionDate(value: String) { _executionDate.value = value }
    fun updateImportant(value: Boolean) { _important.value = value }

    fun setContragent(id: String, name: String) {
        _contragentId.value = id
        _contragentName.value = name
    }

    fun clearContragent() {
        _contragentId.value = null
        _contragentName.value = null
    }

    // Загрузка пользователей
    fun loadUsers() {
        viewModelScope.launch {
            _isLoadingUsers.value = true
            try {
                val usersList = repository.getUsers()
                val filteredUsers = usersList.filter { it.nameUser != "<Не указан>" }
                _users.value = filteredUsers
            } catch (e: Exception) {
                e.printStackTrace()
            } finally {
                _isLoadingUsers.value = false
            }
        }
    }

    // Загрузка контрагентов
    fun loadContragents() {
        viewModelScope.launch {
            _isLoadingContragents.value = true
            try {
                val contragentsList = repository.getContragentsForTaskSelection()
                _contragents.value = contragentsList
            } catch (e: Exception) {
                e.printStackTrace()
                _error.value = "Ошибка загрузки контрагентов: ${e.message}"
            } finally {
                _isLoadingContragents.value = false
            }
        }
    }

    // Управление диалогом контрагентов
    fun showContragentDialog() {
        _showContragentDialog.value = true
        loadContragents()
    }

    fun hideContragentDialog() {
        _showContragentDialog.value = false
    }

    fun selectContragent(contragent: ContragentInTask) {
        _contragentId.value = contragent.id
        _contragentName.value = contragent.name
        hideContragentDialog()
    }
    
    fun selectContragentById(id: String) {
        println("DEBUG: selectContragentById вызван с id='$id'")
        println("DEBUG: Текущий _contragentId перед установкой: ${_contragentId.value}")
        _contragentId.value = id
        println("DEBUG: _contragentId после установки: ${_contragentId.value}")
        
        // Пытаемся найти имя по ID
        viewModelScope.launch {
            try {
                if (_contragents.value.isEmpty()) {
                    println("DEBUG: Список контрагентов пуст, загружаем...")
                    loadContragents()
                    delay(500)
                }
                
                println("DEBUG: Ищем контрагента с id='$id' в списке из ${_contragents.value.size} элементов")
                val contragent = _contragents.value.find { it.id == id }
                if (contragent != null) {
                    _contragentName.value = contragent.name
                    println("DEBUG: Найдено имя контрагента по id='$id': '${contragent.name}'")
                } else {
                    println("DEBUG: Контрагент с id='$id' не найден в списке")
                    println("DEBUG: Первые 5 контрагентов: ${_contragents.value.take(5).map { "'${it.name}' (id=${it.id})" }}")
                }
            } catch (e: Exception) {
                println("DEBUG: Исключение при поиске имени контрагента по id='$id': ${e.message}")
                e.printStackTrace()
            }
        }
    }

    // Работа с вложениями
    fun addAttachment(uri: Uri, fileName: String, fileSize: Long, fileType: String) {
        val newAttachment = AttachmentUiState(
            uri = uri,
            fileName = fileName,
            fileSize = fileSize,
            fileType = fileType
        )
        _attachments.value = _attachments.value + newAttachment
    }

    fun removeAttachment(index: Int) {
        _attachments.value = _attachments.value.toMutableList().apply { removeAt(index) }
    }

    fun clearAttachments() {
        _attachments.value = emptyList()
    }

    // Создание задачи
    private val _taskCreated = MutableStateFlow(false)
    val taskCreated: StateFlow<Boolean> = _taskCreated.asStateFlow()

    // ДОБАВЛЯЕМ КОНТЕКСТ ДЛЯ РАБОТЫ С ФАЙЛАМИ
    private var currentContext: Context? = null

    fun setContext(context: Context) {
        currentContext = context
    }

    fun createTask(context: Context) {
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null
            _taskCreated.value = false

            try {
                // Конвертируем вложения в base64
                val attachmentRequests = mutableListOf<AttachmentRequest>()

                _attachments.value.forEach { attachment ->
                    val base64Data = convertUriToBase64(context, attachment.uri)
                    val mimeType = getMimeType(context, attachment.uri)
                    val fileSize = getFileSize(context, attachment.uri)

                    if (base64Data != null) {
                        attachmentRequests.add(
                            AttachmentRequest(
                                fileName = attachment.fileName,
                                fileData = base64Data,
                                fileType = mimeType
                            )
                        )
                    }
                }

                // Создаем запрос с файлами
                println("DEBUG: Перед созданием CreateTaskRequest:")
                println("DEBUG:   _contragentId.value = ${_contragentId.value}")
                println("DEBUG:   _contragentName.value = ${_contragentName.value}")
                
                val request = CreateTaskRequest(
                    name = _name.value,
                    description = _description.value,
                    status = _status.value,
                    executorId = _executorId.value.ifEmpty { null },
                    executionDate = _executionDate.value.ifEmpty { null },
                    important = if (_important.value) true else null,
                    contragentId = _contragentId.value,
                    attachments = if (attachmentRequests.isNotEmpty()) attachmentRequests else null,
                    comments = null
                )
                
                println("DEBUG: CreateTaskRequest создан:")
                println("DEBUG:   contragentId=${request.contragentId}")
                println("DEBUG:   contragentName=${_contragentName.value}")
                println("DEBUG:   name='${request.name}'")
                println("DEBUG:   description='${request.description}'")

                val result = repository.createTask(request)  // ★ ИСПОЛЬЗУЕМ createTask, а не createTaskWithFiles
                _createTaskResult.value = result

                if (result.success && result.id != null) {
                    _taskCreated.value = true
                    resetForm()
                } else {
                    _error.value = result.error ?: "Неизвестная ошибка при создании задачи"
                }

            } catch (e: Exception) {
                _error.value = "Ошибка при создании задачи: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun resetTaskCreated() {
        _taskCreated.value = false
    }

    fun updateContragentName(name: String) {
        println("DEBUG: updateContragentName вызван с name='$name'")
        _contragentName.value = name
        
        // Ищем контрагента по имени и устанавливаем его ID
        viewModelScope.launch {
            try {
                println("DEBUG: Поиск контрагента по имени '$name'")
                
                // Загружаем контрагентов если ещё не загружены
                if (_contragents.value.isEmpty()) {
                    println("DEBUG: Список контрагентов пуст, загружаем...")
                    loadContragents()
                    // Ждём немного для загрузки
                    delay(500)
                }
                
                println("DEBUG: Всего контрагентов в списке: ${_contragents.value.size}")
                println("DEBUG: Первые 5 контрагентов: ${_contragents.value.take(5).map { "'${it.name}'" }}")
                
                // Ищем контрагента по имени (точное совпадение)
                val contragent = _contragents.value.find { it.name == name }
                if (contragent != null) {
                    _contragentId.value = contragent.id
                    println("DEBUG: Найден контрагент по имени '$name': id=${contragent.id}")
                } else {
                    // Пробуем найти по частичному совпадению (без лишних пробелов)
                    val trimmedName = name.trim()
                    val contragentPartial = _contragents.value.find { 
                        it.name.trim() == trimmedName 
                    }
                    
                    if (contragentPartial != null) {
                        _contragentId.value = contragentPartial.id
                        println("DEBUG: Найден контрагент по обрезанному имени '$trimmedName': id=${contragentPartial.id}")
                    } else {
                        println("DEBUG: Контрагент с именем '$name' не найден в списке")
                        println("DEBUG: Попробуем поискать по части имени...")
                        
                        // Ищем по части имени
                        val matchingContragents = _contragents.value.filter { 
                            it.name.contains(name, ignoreCase = true) ||
                            name.contains(it.name, ignoreCase = true)
                        }
                        
                        if (matchingContragents.isNotEmpty()) {
                            println("DEBUG: Найдены похожие контрагенты: ${matchingContragents.map { "'${it.name}'" }}")
                            // Берём первый похожий
                            _contragentId.value = matchingContragents.first().id
                            println("DEBUG: Выбран первый похожий: id=${matchingContragents.first().id}")
                        } else {
                            println("DEBUG: Нет даже похожих контрагентов")
                            _contragentId.value = null
                        }
                    }
                }
            } catch (e: Exception) {
                println("DEBUG: Исключение при поиске контрагента по имени '$name': ${e.message}")
                e.printStackTrace()
                _contragentId.value = null
            }
        }
    }

    fun resetForm() {
        _name.value = ""
        _description.value = ""
        _status.value = TaskStatus.ASSIGNED
        _executorId.value = ""
        _executionDate.value = ""
        _important.value = false
        _contragentId.value = null
        _contragentName.value = null
        _attachments.value = emptyList()
        _createTaskResult.value = null
        _error.value = null
    }

    fun clearResult() {
        _createTaskResult.value = null
    }
}