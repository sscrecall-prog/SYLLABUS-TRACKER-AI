package com.example.ui.components
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TestSwipe() {
    val state = rememberSwipeToDismissBoxState()
    SwipeToDismissBox(
        state = state,
        backgroundContent = {},
        content = {}
    )
}
