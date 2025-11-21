package com.example.umkami.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.Card
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.example.umkami.data.model.Umkm

@Composable
fun HomeScreen(umkmList: List<Umkm>) {
    LazyColumn(
        modifier = Modifier.fillMaxSize().padding(16.dp)
    ) {
        items(umkmList.size) { index ->
            UmkmItem(umkm = umkmList[index])
        }
    }
}

@Composable
fun UmkmItem(umkm: Umkm) {
    Card(
        modifier = Modifier.fillMaxWidth().padding(8.dp)
    ) {
        Column(modifier = Modifier.padding(12.dp)) {

            AsyncImage(
                model = umkm.imageUrl,
                contentDescription = umkm.name,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(180.dp)
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(text = umkm.name)
            Text(text = umkm.category)
            Text(text = umkm.address)
        }
    }
}
