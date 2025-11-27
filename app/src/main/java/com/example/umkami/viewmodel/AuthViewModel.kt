package com.example.umkami.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.umkami.data.model.User
import com.example.umkami.data.repository.UmkmRepository
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

class AuthViewModel : ViewModel() {

    private val auth: FirebaseAuth = Firebase.auth
    private val database: FirebaseDatabase = Firebase.database
    private val usersRef = database.getReference("users")
    private val repository = UmkmRepository() // Added repository

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()

    private val _isAuthenticated = MutableStateFlow(auth.currentUser != null)
    val isAuthenticated: StateFlow<Boolean> = _isAuthenticated.asStateFlow()

    private val _isRegistered = MutableStateFlow(false)
    val isRegistered: StateFlow<Boolean> = _isRegistered.asStateFlow()

    private val _currentUser = MutableStateFlow<User?>(null)
    val currentUser: StateFlow<User?> = _currentUser.asStateFlow()

    private val authStateListener = FirebaseAuth.AuthStateListener { firebaseAuth ->
        val firebaseUser = firebaseAuth.currentUser
        _isAuthenticated.value = firebaseUser != null
        if (firebaseUser != null) {
            viewModelScope.launch {
                loadCurrentUser(firebaseUser.uid)
            }
        } else {
            _currentUser.value = null
        }
    }

    init {
        auth.addAuthStateListener(authStateListener)
    }

    override fun onCleared() {
        super.onCleared()
        auth.removeAuthStateListener(authStateListener)
    }

    fun topUpBalance(amount: Double) {
        val user = _currentUser.value
        if (user == null) {
            _error.value = "Silakan login terlebih dahulu."
            return
        }

        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null
            try {
                val result = repository.topUpBalance(user.uid, amount)
                if (result.isSuccess) {
                    // Refresh user data to show new balance
                    loadCurrentUser(user.uid)
                } else {
                    _error.value = result.exceptionOrNull()?.message ?: "Top-up gagal."
                }
            } catch (e: Exception) {
                _error.value = e.message ?: "Terjadi kesalahan saat top-up."
            } finally {
                _isLoading.value = false
            }
        }
    }
    
    fun login(email: String, password: String) {
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null
            try {
                auth.signInWithEmailAndPassword(email, password).await()
            } catch (e: Exception) {
                _error.value = e.message ?: "Login gagal!"
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun register(email: String, password: String, displayName: String, role: String) {
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null
            try {
                val result = auth.createUserWithEmailAndPassword(email, password).await()
                val firebaseUser = result.user
                if (firebaseUser != null) {
                    val newUser = User(
                        uid = firebaseUser.uid,
                        email = firebaseUser.email ?: "",
                        displayName = displayName,
                        address = "",
                        role = role,
                        umkmId = if (role == "owner") "" else null,
                        balance = 0.0 // Ensure new users start with 0 balance
                    )
                    usersRef.child(newUser.uid).setValue(newUser).await()
                    _currentUser.value = newUser
                    _isRegistered.value = true
                } else {
                    _error.value = "Pengguna Firebase tidak ditemukan setelah pendaftaran."
                }
            } catch (e: Exception) {
                _error.value = e.message ?: "Pendaftaran gagal!"
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun logout() {
        auth.signOut()
    }

    fun clearError() {
        _error.value = null
    }

    fun clearRegisteredFlag() {
        _isRegistered.value = false
    }

    fun updateUserAddress(newAddress: String) {
        val user = _currentUser.value
        if (user != null) {
            viewModelScope.launch {
                try {
                    usersRef.child(user.uid).child("address").setValue(newAddress).await()
                    _currentUser.value = user.copy(address = newAddress)
                    println("User address updated successfully!")
                } catch (e: Exception) {
                    _error.value = e.message ?: "Failed to update address."
                }
            }
        } else {
            _error.value = "No user logged in to update address."
        }
    }

    private suspend fun loadCurrentUser(uid: String) {
        _isLoading.value = true
        try {
            val dataSnapshot = usersRef.child(uid).get().await()
            if (dataSnapshot.exists()) {
                val user = dataSnapshot.getValue(User::class.java)
                _currentUser.value = user
            } else {
                _error.value = "User data not found in database for UID: $uid"
            }
        } catch (e: Exception) {
            _error.value = "Failed to load user data: ${e.message}"
        } finally {
            _isLoading.value = false
        }
    }
}
