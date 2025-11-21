package com.example.umkami.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.umkami.data.model.Umkm
import com.example.umkami.data.repository.UmkmRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class HomeViewModel : ViewModel() {

    private val repo = UmkmRepository()

    private val _umkmList = MutableStateFlow<List<Umkm>>(emptyList())
    val umkmList: StateFlow<List<Umkm>> get() = _umkmList

    init {
        viewModelScope.launch {
            _umkmList.value = repo.getUmkmFromFirebase()
        }
    }
}
