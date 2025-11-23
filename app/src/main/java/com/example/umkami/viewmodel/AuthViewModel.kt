package com.example.umkami.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.umkami.data.model.User
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

    init {
        if (auth.currentUser != null) {
            loadCurrentUser(auth.currentUser!!.uid)
        }
    }

    fun login(email: String, password: String) {
        _isLoading.value = true
        _error.value = null
        _isAuthenticated.value = false

        viewModelScope.launch {
            try {
                auth.signInWithEmailAndPassword(email, password)
                    .addOnCompleteListener { task ->
                        _isLoading.value = false
                        if (task.isSuccessful) {
                            val uid = task.result?.user?.uid
                            if (uid != null) {
                                loadCurrentUser(uid)
                                _isAuthenticated.value = true
                            } else {
                                _error.value = "UID pengguna tidak ditemukan setelah login."
                            }
                        } else {
                            _error.value = task.exception?.message ?: "Login gagal!"
                        }
                    }
            } catch (e: Exception) {
                _isLoading.value = false
                _error.value = e.message ?: "Terjadi kesalahan tak terduga."
            }
        }
    }

    fun register(email: String, password: String) {
        _isLoading.value = true
        _error.value = null
        _isRegistered.value = false

        viewModelScope.launch {
            try {
                auth.createUserWithEmailAndPassword(email, password)
                    .addOnCompleteListener { task ->
                        _isLoading.value = false
                        if (task.isSuccessful) {
                            val firebaseUser = task.result?.user
                            if (firebaseUser != null) {
                                val newUser = User(
                                    uid = firebaseUser.uid,
                                    email = firebaseUser.email ?: "",
                                    displayName = firebaseUser.email?.substringBefore('@') ?: "Pengguna Baru",
                                    address = "" // Initialize address for new user
                                )
                                usersRef.child(newUser.uid).setValue(newUser)
                                    .addOnSuccessListener {
                                        _currentUser.value = newUser
                                        _isRegistered.value = true
                                    }
                                    .addOnFailureListener { dbError ->
                                        _error.value = dbError.message ?: "Gagal menyimpan data pengguna."
                                    }
                            } else {
                                _error.value = "Pengguna Firebase tidak ditemukan setelah pendaftaran."
                            }
                        } else {
                            _error.value = task.exception?.message ?: "Pendaftaran gagal!"
                        }
                    }
            } catch (e: Exception) {
                _isLoading.value = false
                _error.value = e.message ?: "Terjadi kesalahan tak terduga."
            }
        }
    }

    fun logout() {
        auth.signOut()
        _isAuthenticated.value = false
        _currentUser.value = null
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
                    _currentUser.value = user.copy(address = newAddress) // Update local state
                    println("User address updated successfully!")
                } catch (e: Exception) {
                    _error.value = e.message ?: "Failed to update address."
                }
            }
        } else {
            _error.value = "No user logged in to update address."
        }
    }

    private fun loadCurrentUser(uid: String) {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                val dataSnapshot = usersRef.child(uid).get().await()
                val user = dataSnapshot.getValue(User::class.java)
                _currentUser.value = user
            } catch (e: Exception) {
                _error.value = e.message ?: "Gagal memuat data pengguna."
            } finally {
                _isLoading.value = false
            }
        }
    }
}
