package com.example.ui.components.mocktests

import androidx.compose.runtime.Composable
import com.example.data.model.MistakeCategory
import com.example.data.model.MockTest

@Composable
fun MockTestDialogs(
    showAddEditDialog: Boolean,
    mockToEdit: MockTest?,
    onDismissAddEdit: () -> Unit,
    onSaveMock: (MockTest) -> Unit,
    showDetailDialog: Boolean,
    selectedMockTest: MockTest?,
    onDismissDetail: () -> Unit,
    onEditFromDetail: () -> Unit,
    onDeleteFromDetail: () -> Unit,
    onAddMistakeFromDetail: (questionText: String, wrongAns: String, correctAns: String, exp: String, category: MistakeCategory) -> Unit
) {
    if (showAddEditDialog) {
        MockTestForm(
            initialMock = mockToEdit,
            onDismiss = onDismissAddEdit,
            onSave = onSaveMock
        )
    }

    if (showDetailDialog && selectedMockTest != null) {
        MockTestDetails(
            mockTest = selectedMockTest,
            onDismiss = onDismissDetail,
            onEdit = onEditFromDetail,
            onDelete = onDeleteFromDetail,
            onAddMistake = onAddMistakeFromDetail
        )
    }
}
