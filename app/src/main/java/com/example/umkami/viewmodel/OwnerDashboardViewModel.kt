package com.example.umkami.viewmodel

import android.content.Context
import android.location.Geocoder
import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.umkami.data.model.MenuItem
import com.example.umkami.data.model.ServiceItem
import com.example.umkami.data.model.Umkm
import com.example.umkami.data.repository.UmkmRepository
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.FirebaseStorage
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import android.util.Log
import kotlinx.coroutines.withContext
import java.io.IOException
import java.util.*

class OwnerDashboardViewModel : ViewModel() {

    private val repository = UmkmRepository()
    private val storage = FirebaseStorage.getInstance()
    private val TAG = "OwnerDashboardViewModel"

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
    
    private val _imageUrl = MutableStateFlow("")
    val imageUrl: StateFlow<String> = _imageUrl

    private val _lat = MutableStateFlow(-7.96) // Default to Malang, Indonesia
    val lat: StateFlow<Double> = _lat

    private val _lng = MutableStateFlow(112.63) // Default to Malang, Indonesia
    val lng: StateFlow<Double> = _lng

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
                            _imageUrl.value = result.imageUrl
                            _lat.value = result.lat
                            _lng.value = result.lng
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

    fun geocodeAddress(context: Context, address: String) {
        if (address.isBlank()) {
            _error.value = "Address cannot be empty."
            return
        }
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null
            try {
                withContext(Dispatchers.IO) {
                    val geocoder = Geocoder(context)
                    try {
                        val addresses = geocoder.getFromLocationName(address, 1)
                        if (addresses != null && addresses.isNotEmpty()) {
                            val location = addresses[0]
                            withContext(Dispatchers.Main) {
                                _lat.value = location.latitude
                                _lng.value = location.longitude
                            }
                        } else {
                            withContext(Dispatchers.Main) {
                                _error.value = "Address not found."
                            }
                        }
                    } catch (e: IOException) {
                        Log.e(TAG, "Geocoding failed: ${e.message}", e)
                        withContext(Dispatchers.Main) {
                            _error.value = "Geocoding failed. Check network connection."
                        }
                    }
                }
            } catch (e: Exception) {
                Log.e(TAG, "Geocoding failed: ${e.message}", e)
                withContext(Dispatchers.Main) {
                    _error.value = "An unexpected error occurred during geocoding."
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
            
            // Create the object to save, ensuring the latest imageUrl from the VM state is used.
            val umkmToSave = umkm.copy(imageUrl = _imageUrl.value)

            try {
                withContext(Dispatchers.IO) {
                    newUmkmId = repository.saveUmkm(umkmToSave, userId)
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
                    // Update the local state with the data that was actually saved.
                    _umkm.value = umkmToSave.copy(id = newUmkmId!!)
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

    fun uploadUmkmImage(imageUri: Uri, userId: String) {
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null
            try {
                // Use the userId for the path, which is stable and authorized
                val storageRef = storage.reference.child("umkm_images/${userId}/${UUID.randomUUID()}.jpg")
                val uploadTask = storageRef.putFile(imageUri).await()
                val downloadUrl = uploadTask.storage.downloadUrl.await().toString()
                _imageUrl.value = downloadUrl
                // Also update the umkm state if it exists
                _umkm.value = _umkm.value?.copy(imageUrl = downloadUrl)

            } catch (e: Exception) {
                _error.value = "Image upload failed: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    } // Correct closing brace for uploadUmkmImage function

    fun clearError() {
        _error.value = null
    }

    fun setLat(newLat: Double) {
        _lat.value = newLat
    }

    fun setLng(newLng: Double) {
        _lng.value = newLng
    }
} // Correct closing brace for OwnerDashboardViewModel class