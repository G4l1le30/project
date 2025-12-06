package com.example.umkami.ui.screens

import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.example.umkami.data.model.MenuItem
import com.example.umkami.data.model.ServiceItem
import com.example.umkami.data.model.Umkm
import com.example.umkami.viewmodel.AuthViewModel
import com.example.umkami.viewmodel.OwnerDashboardViewModel
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.compose.GoogleMap
import com.google.maps.android.compose.Marker
import com.google.maps.android.compose.rememberCameraPositionState
import kotlinx.coroutines.delay

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OwnerDashboardScreen(
    navController: NavController,
    authViewModel: AuthViewModel,
    ownerDashboardViewModel: OwnerDashboardViewModel
) {
    val currentUser by authViewModel.currentUser.collectAsState()
    val umkm: Umkm? by ownerDashboardViewModel.umkm.collectAsState()
    val menuItemsFromVm: List<MenuItem> by ownerDashboardViewModel.menuItems.collectAsState()
    val serviceItemsFromVm: List<ServiceItem> by ownerDashboardViewModel.serviceItems.collectAsState()
    val isLoading: Boolean by ownerDashboardViewModel.isLoading.collectAsState()
    val context = LocalContext.current
    val imageUrl by ownerDashboardViewModel.imageUrl.collectAsState()


    var name by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var address by remember { mutableStateOf("") }
    var category by remember { mutableStateOf("Makanan") }
    var contact by remember { mutableStateOf("") }
    val lat by ownerDashboardViewModel.lat.collectAsState() // Collect lat from ViewModel
    val lng by ownerDashboardViewModel.lng.collectAsState() // Collect lng from ViewModel
    val menuItems = remember { mutableStateListOf<MenuItem>() }
    val serviceItems = remember { mutableStateListOf<ServiceItem>() }
    var imageUri by remember { mutableStateOf<Uri?>(null) }

    val imagePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let {
            imageUri = it
            currentUser?.uid?.let { userId ->
                ownerDashboardViewModel.uploadUmkmImage(it, userId)
            }
        }
    }


    LaunchedEffect(currentUser) {
        currentUser?.umkmId?.let {
            if (it.isNotBlank()) {
                ownerDashboardViewModel.loadUmkm(it)
            }
        }
    }

    LaunchedEffect(umkm) {
        umkm?.let {
            name = it.name
            description = it.description
            address = it.address
            category = it.category
            contact = it.contact
            ownerDashboardViewModel.setLat(it.lat)
            ownerDashboardViewModel.setLng(it.lng)
            if (it.imageUrl.isNotBlank()){
                imageUri = Uri.parse(it.imageUrl)
            }
        }
    }

    LaunchedEffect(menuItemsFromVm) {
        menuItems.clear()
        menuItems.addAll(menuItemsFromVm)
    }

    LaunchedEffect(address) {
        if (address.length > 5) { // Start geocoding after a few characters
            delay(1000L) // Debounce for 1 second
            ownerDashboardViewModel.geocodeAddress(context, address)
        }
    }

    LaunchedEffect(Unit) { // Use Unit as a constant key to launch this effect only once
        ownerDashboardViewModel.error.collect { errorMessage ->
            errorMessage?.let {
                Toast.makeText(context, it, Toast.LENGTH_LONG).show()
                ownerDashboardViewModel.clearError() // Clear the error after showing
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Owner Dashboard") },
                actions = {
                    Button(onClick = { authViewModel.logout() }) {
                        Text("Logout")
                    }
                }
            )
        }
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item {
                Text(
                    text = if (umkm == null) "Create Your UMKM Profile" else "Edit Your UMKM Profile",
                    style = MaterialTheme.typography.headlineSmall
                )
            }
            item {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    val displayImageUrl = imageUrl.takeIf { it.isNotEmpty() } ?: imageUri?.toString() ?: umkm?.imageUrl
                    AsyncImage(
                        model = displayImageUrl,
                        contentDescription = "UMKM Image",
                        modifier = Modifier
                            .size(120.dp)
                            .padding(8.dp),
                        contentScale = ContentScale.Crop
                    )

                    Button(onClick = { imagePickerLauncher.launch("image/*") }) {
                        Text("Upload Image")
                    }
                }
            }


            item {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Nama UMKM") },
                    modifier = Modifier.fillMaxWidth()
                )
            }

            item {
                OutlinedTextField(
                    value = description,
                    onValueChange = { description = it },
                    label = { Text("Deskripsi") },
                    modifier = Modifier.fillMaxWidth()
                )
            }

            item {
                OutlinedTextField(
                    value = address,
                    onValueChange = { address = it },
                    label = { Text("Alamat") },
                    modifier = Modifier.fillMaxWidth()
                )
            }

            item {
                OutlinedTextField(
                    value = contact,
                    onValueChange = { contact = it },
                    label = { Text("Kontak (Nomor Telepon)") },
                    modifier = Modifier.fillMaxWidth()
                )
            }

            item {
                Column(Modifier.fillMaxWidth()) {
                    Text("Kategori:", style = MaterialTheme.typography.bodyLarge)
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        RadioButton(
                            selected = category == "Makanan",
                            onClick = { category = "Makanan" }
                        )
                        Text("Makanan")
                        Spacer(Modifier.width(16.dp))
                        RadioButton(
                            selected = category == "Jasa",
                            onClick = { category = "Jasa" }
                        )
                        Text("Jasa")
                    }
                }
            }

            // Menu or Service editor
            if (category == "Makanan") {
                item {
                    Text("Menu", style = MaterialTheme.typography.headlineSmall)
                }
                itemsIndexed(menuItems) { index, item ->
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        OutlinedTextField(
                            value = item.name,
                            onValueChange = { menuItems[index] = item.copy(name = it) },
                            label = { Text("Nama Menu") },
                            modifier = Modifier.weight(1f)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        OutlinedTextField(
                            value = item.price.toString(),
                            onValueChange = { menuItems[index] = item.copy(price = it.toIntOrNull() ?: 0) },
                            label = { Text("Harga") },
                            modifier = Modifier.width(100.dp),
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                        )
                        IconButton(onClick = { menuItems.removeAt(index) }) {
                            Icon(Icons.Default.Delete, contentDescription = "Remove Menu Item")
                        }
                    }
                }
                item {
                    Button(onClick = { menuItems.add(MenuItem()) }) {
                        Text("Add Menu Item")
                    }
                }
            } else {
                item {
                    Text("Services", style = MaterialTheme.typography.headlineSmall)
                }
                itemsIndexed(serviceItems) { index, item ->
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        OutlinedTextField(
                            value = item.service,
                            onValueChange = { serviceItems[index] = item.copy(service = it) },
                            label = { Text("Nama Jasa") },
                            modifier = Modifier.weight(1f)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        OutlinedTextField(
                            value = item.price.toString(),
                            onValueChange = { serviceItems[index] = item.copy(price = it.toIntOrNull() ?: 0) },
                            label = { Text("Harga") },
                            modifier = Modifier.width(100.dp),
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                        )
                        IconButton(onClick = { serviceItems.removeAt(index) }) {
                            Icon(Icons.Default.Delete, contentDescription = "Remove Service Item")
                        }
                    }
                }
                item {
                    Button(onClick = { serviceItems.add(ServiceItem()) }) {
                        Text("Add Service Item")
                    }
                }
            }


            item {
                val markerState = com.google.maps.android.compose.rememberMarkerState(position = LatLng(lat, lng))
                val cameraPositionState = rememberCameraPositionState {
                    position = CameraPosition.fromLatLngZoom(LatLng(lat, lng), 15f)
                }

                LaunchedEffect(lat, lng) {
                    markerState.position = LatLng(lat, lng)
                    cameraPositionState.position = CameraPosition.fromLatLngZoom(LatLng(lat, lng), 15f)
                }
                
                GoogleMap(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(300.dp),
                    cameraPositionState = cameraPositionState
                ) {
                    Marker(
                        state = markerState,
                        draggable = true,
                        title = "Lokasi UMKM"
                    )
                }
                
                LaunchedEffect(markerState.dragState) {
                    if (markerState.dragState == com.google.maps.android.compose.DragState.END) {
                        ownerDashboardViewModel.setLat(markerState.position.latitude)
                        ownerDashboardViewModel.setLng(markerState.position.longitude)
                    }
                }
            }

            item {
                Button(
                    onClick = {
                        val umkmToSave = umkm?.copy(
                            name = name,
                            description = description,
                            address = address,
                            category = category,
                            contact = contact,
                            lat = lat, // Use lat from ViewModel
                            lng = lng, // Use lng from ViewModel
                            imageUrl = imageUrl.ifBlank { umkm?.imageUrl ?: "" }
                        ) ?: Umkm(
                            name = name,
                            description = description,
                            address = address,
                            category = category,
                            contact = contact,
                            imageUrl = imageUrl,
                            lat = lat, // Use lat from ViewModel
                            lng = lng // Use lng from ViewModel
                        )
                        currentUser?.uid?.let { userId ->
                            ownerDashboardViewModel.saveUmkm(umkmToSave, userId, menuItems.toList(), serviceItems.toList())
                            Toast.makeText(context, "Profile Saved!", Toast.LENGTH_SHORT).show()
                        }
                    },
                    modifier = Modifier.fillMaxWidth(),
                    enabled = !isLoading
                ) {
                    if (isLoading) {
                        CircularProgressIndicator(modifier = Modifier.size(24.dp))
                    } else {
                        Text("Save Profile")
                    }
                }
            }
        }
    }
}