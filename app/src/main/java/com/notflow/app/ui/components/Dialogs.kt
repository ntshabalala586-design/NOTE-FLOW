package com.notflow.app.ui.components

import android.app.DatePickerDialog
import android.app.TimePickerDialog
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Checkbox
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.notflow.app.data.model.CalendarTaskEntity
import com.notflow.app.data.model.GroceryItemEntity
import com.notflow.app.data.model.NoteEntity
import com.notflow.app.data.model.ShoppingTripEntity
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun NoteEditDialog(
    note: NoteEntity? = null,
    availableCategories: List<String>,
    onDismiss: () -> Unit,
    onSave: (id: Long, title: String, content: String, category: String, isPinned: Boolean) -> Unit,
    onAddCategory: (String) -> Unit
) {
    var title by remember { mutableStateOf(note?.title ?: "") }
    var content by remember { mutableStateOf(note?.content ?: "") }
    var selectedCategory by remember { mutableStateOf(note?.category ?: if (availableCategories.isNotEmpty()) availableCategories.first() else "General") }
    var isPinned by remember { mutableStateOf(note?.isPinned ?: false) }
    var showNewCategoryInput by remember { mutableStateOf(false) }
    var newCategoryText by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = if (note == null) "Create Note" else "Edit Note",
                style = MaterialTheme.typography.titleLarge
            )
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text("Title") },
                    singleLine = true,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("note_title_input")
                )

                OutlinedTextField(
                    value = content,
                    onValueChange = { content = it },
                    label = { Text("Note content") },
                    minLines = 4,
                    maxLines = 8,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("note_content_input")
                )

                Text(
                    text = "Category",
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.primary
                )

                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    availableCategories.forEach { cat ->
                        FilterChip(
                            selected = selectedCategory == cat,
                            onClick = { selectedCategory = cat },
                            label = { Text(cat) },
                            leadingIcon = if (selectedCategory == cat) {
                                { Icon(Icons.Default.Check, contentDescription = null) }
                            } else null
                        )
                    }
                    FilterChip(
                        selected = false,
                        onClick = { showNewCategoryInput = !showNewCategoryInput },
                        label = { Text("+ Category") }
                    )
                }

                if (showNewCategoryInput) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        OutlinedTextField(
                            value = newCategoryText,
                            onValueChange = { newCategoryText = it },
                            label = { Text("New Category") },
                            modifier = Modifier.weight(1f),
                            singleLine = true
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Button(
                            onClick = {
                                if (newCategoryText.isNotBlank()) {
                                    onAddCategory(newCategoryText.trim())
                                    selectedCategory = newCategoryText.trim()
                                    newCategoryText = ""
                                    showNewCategoryInput = false
                                }
                            }
                        ) {
                            Text("Add")
                        }
                    }
                }

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Checkbox(
                        checked = isPinned,
                        onCheckedChange = { isPinned = it }
                    )
                    Text(
                        text = "Pin note to top",
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (title.isNotBlank() || content.isNotBlank()) {
                        onSave(note?.id ?: 0L, title.ifBlank { "Untitled Note" }, content, selectedCategory, isPinned)
                        onDismiss()
                    }
                },
                modifier = Modifier.testTag("save_note_button")
            ) {
                Text("Save Note")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun GroceryEditDialog(
    item: GroceryItemEntity? = null,
    availableCategories: List<String>,
    onDismiss: () -> Unit,
    onSave: (id: Long, name: String, category: String, isChecked: Boolean) -> Unit,
    onAddCategory: (String) -> Unit
) {
    var name by remember { mutableStateOf(item?.name ?: "") }
    var selectedCategory by remember { mutableStateOf(item?.category ?: if (availableCategories.isNotEmpty()) availableCategories.first() else "Produce") }
    var isChecked by remember { mutableStateOf(item?.isChecked ?: false) }
    var showNewCategoryInput by remember { mutableStateOf(false) }
    var newCategoryText by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = if (item == null) "Add Grocery Item" else "Edit Grocery Item",
                style = MaterialTheme.typography.titleLarge
            )
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Item Name") },
                    singleLine = true,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("grocery_item_name_input")
                )

                Text(
                    text = "Category",
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.primary
                )

                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    availableCategories.forEach { cat ->
                        FilterChip(
                            selected = selectedCategory == cat,
                            onClick = { selectedCategory = cat },
                            label = { Text(cat) },
                            leadingIcon = if (selectedCategory == cat) {
                                { Icon(Icons.Default.Check, contentDescription = null) }
                            } else null
                        )
                    }
                    FilterChip(
                        selected = false,
                        onClick = { showNewCategoryInput = !showNewCategoryInput },
                        label = { Text("+ Category") }
                    )
                }

                if (showNewCategoryInput) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        OutlinedTextField(
                            value = newCategoryText,
                            onValueChange = { newCategoryText = it },
                            label = { Text("New Category") },
                            modifier = Modifier.weight(1f),
                            singleLine = true
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Button(
                            onClick = {
                                if (newCategoryText.isNotBlank()) {
                                    onAddCategory(newCategoryText.trim())
                                    selectedCategory = newCategoryText.trim()
                                    newCategoryText = ""
                                    showNewCategoryInput = false
                                }
                            }
                        ) {
                            Text("Add")
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (name.isNotBlank()) {
                        onSave(item?.id ?: 0L, name, selectedCategory, isChecked)
                        onDismiss()
                    }
                },
                modifier = Modifier.testTag("save_grocery_button")
            ) {
                Text("Save Item")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}

@Composable
fun GroceryCategoryDialog(
    initialName: String = "",
    titleText: String = "Add Grocery Category",
    onDismiss: () -> Unit,
    onConfirm: (String) -> Unit
) {
    var categoryName by remember { mutableStateOf(initialName) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(titleText, style = MaterialTheme.typography.titleLarge) },
        text = {
            OutlinedTextField(
                value = categoryName,
                onValueChange = { categoryName = it },
                label = { Text("Category Name") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )
        },
        confirmButton = {
            Button(
                onClick = {
                    if (categoryName.isNotBlank()) {
                        onConfirm(categoryName.trim())
                        onDismiss()
                    }
                }
            ) {
                Text("Save")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}

@Composable
fun ShoppingTripEditDialog(
    trip: ShoppingTripEntity? = null,
    onDismiss: () -> Unit,
    onSave: (
        id: Long,
        title: String,
        date: Long,
        timeHour: Int,
        timeMinute: Int,
        description: String,
        reminderMinutesBefore: Int,
        isReminderEnabled: Boolean
    ) -> Unit
) {
    val context = LocalContext.current
    val calendar = remember {
        Calendar.getInstance().apply {
            if (trip != null) {
                timeInMillis = trip.date
                set(Calendar.HOUR_OF_DAY, trip.timeHour)
                set(Calendar.MINUTE, trip.timeMinute)
            }
        }
    }

    var title by remember { mutableStateOf(trip?.title ?: "") }
    var description by remember { mutableStateOf(trip?.description ?: "") }
    var selectedDateMillis by remember { mutableLongStateOf(calendar.timeInMillis) }
    var timeHour by remember { mutableIntStateOf(calendar.get(Calendar.HOUR_OF_DAY)) }
    var timeMinute by remember { mutableIntStateOf(calendar.get(Calendar.MINUTE)) }
    var reminderMinutesBefore by remember { mutableIntStateOf(trip?.reminderMinutesBefore ?: 30) }
    var isReminderEnabled by remember { mutableStateOf(trip?.isReminderEnabled ?: true) }

    val dateFormat = remember { SimpleDateFormat("EEEE, d MMMM yyyy", Locale.getDefault()) }
    val timeFormat = remember { SimpleDateFormat("h:mm a", Locale.getDefault()) }

    var showReminderMenu by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = if (trip == null) "Schedule Shopping Trip" else "Edit Shopping Trip",
                style = MaterialTheme.typography.titleLarge
            )
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text("Trip Title") },
                    placeholder = { Text("e.g. Weekend Grocery Trip") },
                    singleLine = true,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("trip_title_input")
                )

                OutlinedTextField(
                    value = description,
                    onValueChange = { description = it },
                    label = { Text("Description / Notes (Optional)") },
                    minLines = 2,
                    modifier = Modifier.fillMaxWidth()
                )

                // Date Picker Trigger
                OutlinedButton(
                    onClick = {
                        val currentCal = Calendar.getInstance().apply { timeInMillis = selectedDateMillis }
                        DatePickerDialog(
                            context,
                            { _, y, m, d ->
                                val newCal = Calendar.getInstance()
                                newCal.set(y, m, d)
                                selectedDateMillis = newCal.timeInMillis
                            },
                            currentCal.get(Calendar.YEAR),
                            currentCal.get(Calendar.MONTH),
                            currentCal.get(Calendar.DAY_OF_MONTH)
                        ).show()
                    },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(Icons.Default.CalendarToday, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(dateFormat.format(Date(selectedDateMillis)))
                }

                // Time Picker Trigger
                OutlinedButton(
                    onClick = {
                        TimePickerDialog(
                            context,
                            { _, h, m ->
                                timeHour = h
                                timeMinute = m
                            },
                            timeHour,
                            timeMinute,
                            false
                        ).show()
                    },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(Icons.Default.Schedule, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    val cal = Calendar.getInstance().apply {
                        set(Calendar.HOUR_OF_DAY, timeHour)
                        set(Calendar.MINUTE, timeMinute)
                    }
                    Text(timeFormat.format(cal.time))
                }

                // Reminder Switch & Options
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Notifications, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Enable Reminder", style = MaterialTheme.typography.bodyMedium)
                    }
                    Switch(
                        checked = isReminderEnabled,
                        onCheckedChange = { isReminderEnabled = it }
                    )
                }

                if (isReminderEnabled) {
                    Box(modifier = Modifier.fillMaxWidth()) {
                        OutlinedButton(
                            onClick = { showReminderMenu = true },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(
                                text = when (reminderMinutesBefore) {
                                    0 -> "At time of event"
                                    15 -> "15 minutes before"
                                    30 -> "30 minutes before"
                                    60 -> "1 hour before"
                                    1440 -> "1 day before"
                                    else -> "$reminderMinutesBefore minutes before"
                                }
                            )
                        }
                        DropdownMenu(
                            expanded = showReminderMenu,
                            onDismissRequest = { showReminderMenu = false }
                        ) {
                            DropdownMenuItem(
                                text = { Text("At time of event") },
                                onClick = { reminderMinutesBefore = 0; showReminderMenu = false }
                            )
                            DropdownMenuItem(
                                text = { Text("15 minutes before") },
                                onClick = { reminderMinutesBefore = 15; showReminderMenu = false }
                            )
                            DropdownMenuItem(
                                text = { Text("30 minutes before") },
                                onClick = { reminderMinutesBefore = 30; showReminderMenu = false }
                            )
                            DropdownMenuItem(
                                text = { Text("1 hour before") },
                                onClick = { reminderMinutesBefore = 60; showReminderMenu = false }
                            )
                            DropdownMenuItem(
                                text = { Text("1 day before") },
                                onClick = { reminderMinutesBefore = 1440; showReminderMenu = false }
                            )
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (title.isNotBlank()) {
                        onSave(
                            trip?.id ?: 0L,
                            title,
                            selectedDateMillis,
                            timeHour,
                            timeMinute,
                            description,
                            reminderMinutesBefore,
                            isReminderEnabled
                        )
                        onDismiss()
                    }
                },
                modifier = Modifier.testTag("save_trip_button")
            ) {
                Text("Save Shopping Trip")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}

@Composable
fun CalendarTaskEditDialog(
    task: CalendarTaskEntity? = null,
    initialDateMillis: Long = System.currentTimeMillis(),
    onDismiss: () -> Unit,
    onSave: (
        id: Long,
        title: String,
        description: String,
        date: Long,
        timeHour: Int,
        timeMinute: Int,
        reminderMinutesBefore: Int,
        isReminderEnabled: Boolean
    ) -> Unit
) {
    val context = LocalContext.current
    var title by remember { mutableStateOf(task?.title ?: "") }
    var description by remember { mutableStateOf(task?.description ?: "") }
    var selectedDateMillis by remember { mutableLongStateOf(task?.date ?: initialDateMillis) }
    var timeHour by remember { mutableIntStateOf(task?.timeHour ?: 10) }
    var timeMinute by remember { mutableIntStateOf(task?.timeMinute ?: 0) }
    var reminderMinutesBefore by remember { mutableIntStateOf(task?.reminderMinutesBefore ?: 30) }
    var isReminderEnabled by remember { mutableStateOf(task?.isReminderEnabled ?: true) }

    val dateFormat = remember { SimpleDateFormat("EEEE, d MMMM yyyy", Locale.getDefault()) }
    val timeFormat = remember { SimpleDateFormat("h:mm a", Locale.getDefault()) }
    var showReminderMenu by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = if (task == null) "New Calendar Task" else "Edit Task",
                style = MaterialTheme.typography.titleLarge
            )
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text("Task Title") },
                    placeholder = { Text("e.g. Buy groceries / Pay bills") },
                    singleLine = true,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("task_title_input")
                )

                OutlinedTextField(
                    value = description,
                    onValueChange = { description = it },
                    label = { Text("Description") },
                    minLines = 2,
                    modifier = Modifier.fillMaxWidth()
                )

                // Date Picker
                OutlinedButton(
                    onClick = {
                        val currentCal = Calendar.getInstance().apply { timeInMillis = selectedDateMillis }
                        DatePickerDialog(
                            context,
                            { _, y, m, d ->
                                val newCal = Calendar.getInstance()
                                newCal.set(y, m, d)
                                selectedDateMillis = newCal.timeInMillis
                            },
                            currentCal.get(Calendar.YEAR),
                            currentCal.get(Calendar.MONTH),
                            currentCal.get(Calendar.DAY_OF_MONTH)
                        ).show()
                    },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(Icons.Default.CalendarToday, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(dateFormat.format(Date(selectedDateMillis)))
                }

                // Time Picker
                OutlinedButton(
                    onClick = {
                        TimePickerDialog(
                            context,
                            { _, h, m ->
                                timeHour = h
                                timeMinute = m
                            },
                            timeHour,
                            timeMinute,
                            false
                        ).show()
                    },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(Icons.Default.Schedule, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    val cal = Calendar.getInstance().apply {
                        set(Calendar.HOUR_OF_DAY, timeHour)
                        set(Calendar.MINUTE, timeMinute)
                    }
                    Text(timeFormat.format(cal.time))
                }

                // Reminder Settings
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Notifications, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Reminder", style = MaterialTheme.typography.bodyMedium)
                    }
                    Switch(
                        checked = isReminderEnabled,
                        onCheckedChange = { isReminderEnabled = it }
                    )
                }

                if (isReminderEnabled) {
                    Box(modifier = Modifier.fillMaxWidth()) {
                        OutlinedButton(
                            onClick = { showReminderMenu = true },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(
                                text = when (reminderMinutesBefore) {
                                    0 -> "At time of task"
                                    15 -> "15 minutes before"
                                    30 -> "30 minutes before"
                                    60 -> "1 hour before"
                                    1440 -> "1 day before"
                                    else -> "$reminderMinutesBefore minutes before"
                                }
                            )
                        }
                        DropdownMenu(
                            expanded = showReminderMenu,
                            onDismissRequest = { showReminderMenu = false }
                        ) {
                            DropdownMenuItem(text = { Text("At time of task") }, onClick = { reminderMinutesBefore = 0; showReminderMenu = false })
                            DropdownMenuItem(text = { Text("15 minutes before") }, onClick = { reminderMinutesBefore = 15; showReminderMenu = false })
                            DropdownMenuItem(text = { Text("30 minutes before") }, onClick = { reminderMinutesBefore = 30; showReminderMenu = false })
                            DropdownMenuItem(text = { Text("1 hour before") }, onClick = { reminderMinutesBefore = 60; showReminderMenu = false })
                            DropdownMenuItem(text = { Text("1 day before") }, onClick = { reminderMinutesBefore = 1440; showReminderMenu = false })
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (title.isNotBlank()) {
                        onSave(
                            task?.id ?: 0L,
                            title,
                            description,
                            selectedDateMillis,
                            timeHour,
                            timeMinute,
                            reminderMinutesBefore,
                            isReminderEnabled
                        )
                        onDismiss()
                    }
                },
                modifier = Modifier.testTag("save_calendar_task_button")
            ) {
                Text("Save Task")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}
