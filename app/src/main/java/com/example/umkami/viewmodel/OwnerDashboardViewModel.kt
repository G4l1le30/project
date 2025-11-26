import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.umkami.data.model.MenuItem
import com.example.umkami.data.model.ServiceItem
import com.example.umkami.data.model.Umkm
import com.example.umkami.data.repository.UmkmRepository
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.ktx.storage
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import java.util.*

class OwnerDashboardViewModel : ViewModel() {

    private val repository = UmkmRepository()
    private val storage = Firebase.storage

    private val _umkm = MutableStateFlow<Umkm?>(null)
    val umkm: StateFlow<Umkm?> = _umkm

    private val _menuItems = MutableStateFlow<List<MenuItem>>(emptyList())
    val menuItems: StateFlow<List<MenuItem>> = _menuItems

    private val _serviceItems = MutableStateFlow<List<ServiceItem>>(emptyList())
    val serviceItems: StateFlow<List<ServiceItem>> = _serviceItems

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error

    fun loadUmkm(umkmId: String) {
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null
            try {
                withContext(Dispatchers.IO) {
                    val result = repository.getUmkmById(umkmId)
                    if (result != null) {
                        val menu = repository.getUmkmMenu(result.id)
                        val services = repository.getUmkmServices(result.id)
                        withContext(Dispatchers.Main) {
                            _umkm.value = result
                            _menuItems.value = menu
                            _serviceItems.value = services
                        }
                    }
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    _error.value = e.message
                }
            } finally {
                withContext(Dispatchers.Main) {
                    _isLoading.value = false
                }
            }
        }
    }

    fun saveUmkm(umkm: Umkm, userId: String, menu: List<MenuItem>, services: List<ServiceItem>) {
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null
            var success = false
            var newUmkmId: String? = null
            var menuSaveSuccess = false
            var serviceSaveSuccess = false

            try {
                withContext(Dispatchers.IO) {
                    newUmkmId = repository.saveUmkm(umkm, userId)
                    if (newUmkmId?.isNotBlank() == true) {
                        menuSaveSuccess = repository.saveMenu(newUmkmId!!, menu)
                        serviceSaveSuccess = repository.saveServices(newUmkmId!!, services)
                        success = true
                    }
                }
            } catch (e: Exception) {
                _error.value = e.message
            } finally {
                if (success) {
                    _umkm.value = umkm.copy(id = newUmkmId!!)
                    if (menuSaveSuccess) {
                        _menuItems.value = menu
                    } else {
                        _error.value = "Failed to save menu."
                    }
                    if (serviceSaveSuccess) {
                        _serviceItems.value = services
                    } else {
                        _error.value = "Failed to save services."
                    }
                } else {
                    _error.value = _error.value ?: "Failed to save UMKM profile."
                }
                _isLoading.value = false
            }
        }
    }

    fun uploadUmkmImage(imageUri: Uri) {
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null
            try {
                val umkmId = _umkm.value?.id ?: throw IllegalStateException("UMKM ID is null")
                val storageRef = storage.reference.child("umkm_images/${umkmId}/${UUID.randomUUID()}.jpg")
                val uploadTask = storageRef.putFile(imageUri).await()
                val downloadUrl = uploadTask.storage.downloadUrl.await().toString()
                _umkm.value = _umkm.value?.copy(imageUrl = downloadUrl)
            } catch (e: Exception) {
                _error.value = "Image upload failed: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }
}