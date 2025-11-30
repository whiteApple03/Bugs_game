package com.example.myapplication

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun AuthorsTab() {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(32.dp)
    ) {
//        item {
//            Column(horizontalAlignment = Alignment.CenterHorizontally) {
//            }
//        }
        item {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text("Разработчик", fontSize = 20.sp, style = MaterialTheme.typography.titleMedium) // Changed title text and style
                Spacer(modifier = Modifier.height(16.dp))
                Image(
                    painter = painterResource(id = R.drawable.author),
                    contentDescription = "Фото",
                    modifier = Modifier.size(96.dp),
                    colorFilter = ColorFilter.tint(LocalContentColor.current)
                )
                Spacer(modifier = Modifier.height(16.dp))
                Text("Петрушенко Юрий", fontSize = 18.sp)
            }
        }
        item {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text("Дизайнер", fontSize = 20.sp, style = MaterialTheme.typography.titleMedium)
                Spacer(modifier = Modifier.height(16.dp))
                Image(
                    painter = painterResource(id = R.drawable.author),
                    contentDescription = "Фото",
                    modifier = Modifier.size(96.dp),
                    colorFilter = ColorFilter.tint(LocalContentColor.current)
                )
                Spacer(modifier = Modifier.height(16.dp))
                Text("Петрушенко Юрий", fontSize = 18.sp)
            }
        }
    }
}
