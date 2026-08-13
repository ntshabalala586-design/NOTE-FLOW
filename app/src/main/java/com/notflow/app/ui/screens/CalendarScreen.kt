package com.notflow.app.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ChevronLeft
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.ShoppingBag
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.notflow.app.data.model.CalendarTaskEntity
import com.notflow.app.data.model.ShoppingTripEntity
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

data class CalendarDay(
    val dateMillis: Long,
    val dayNumber: Int,
    val isCurrentMonth: Boolean,
    val isToday: Boolean,
    val isSelected: Boolean,
    val hasTask: Boolean,
    val hasShoppingTrip: Boolean
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CalendarScreen(
    currentCalendar: Calendar,
    selectedDateMillis: Long,
    tasksForSelectedDate: List<CalendarTaskEntity>,
    shoppingTripsForSelectedDate: List<ShoppingTripEntity>,
    allTasks: List<CalendarTaskEntity>,
    allShoppingTrips: List<ShoppingTripEntity>,
    onSelectDate: (Long) -> Unit,
    onPreviousMonth: () -> Unit,
    onNextMonth: () -> Unit,
    onAddTaskClick: () -> Unit,
    onTaskClick: (CalendarTaskEntity) -> Unit,
    onTaskDelete: (CalendarTaskEntity) -> Unit,
    onTripClick: (ShoppingTripEntity) -> Unit
) {
    val monthYearFormat = remember { SimpleDateFormat("MMMM yyyy", Locale.getDefault()) }
    val dayHeaderFormat = remember { SimpleDateFormat("EEEE, d MMMM yyyy", Locale.getDefault()) }

    // Build Calendar Grid Days
    val daysInGrid = remember(currentCalendar.timeInMillis, selectedDateMillis, allTasks, allShoppingTrips) {
        val cal = currentCalendar.clone() as Calendar
        cal.set(Calendar.DAY_OF_MONTH, 1)

        val taskDates = allTasks.map { getStartOfDayMillis(it.date) }.toSet()
        val shoppingDates = allShoppingTrips.map { getStartOfDayMillis(it.date) }.toSet()
        val todayMillis = getStartOfDayMillis(System.currentTimeMillis())
        val selectedDayMillis = getStartOfDayMillis(selectedDateMillis)

        val firstDayOfWeek = cal.get(Calendar.DAY_OF_WEEK) // 1=Sun, 2=Mon...
        val offset = if (firstDayOfWeek == Calendar.SUNDAY) 6 else firstDayOfWeek - 2 // Monday start

        val monthDays = mutableListOf<CalendarDay>()

        // Previous month filler
        val prevCal = cal.clone() as Calendar
        prevCal.add(Calendar.MONTH, -1)
        val maxPrevDays = prevCal.getActualMaximum(Calendar.DAY_OF_MONTH)
        for (i in offset - 1 downTo 0) {
            val dayCal = prevCal.clone() as Calendar
            dayCal.set(Calendar.DAY_OF_MONTH, maxPrevDays - i)
            val time = dayCal.timeInMillis
            val startOfDay = getStartOfDayMillis(time)
            monthDays.add(
                CalendarDay(
                    dateMillis = time,
                    dayNumber = maxPrevDays - i,
                    isCurrentMonth = false,
                    isToday = startOfDay == todayMillis,
                    isSelected = startOfDay == selectedDayMillis,
                    hasTask = taskDates.contains(startOfDay),
                    hasShoppingTrip = shoppingDates.contains(startOfDay)
                )
            )
        }

        // Current month days
        val maxDays = cal.getActualMaximum(Calendar.DAY_OF_MONTH)
        for (day in 1..maxDays) {
            val dayCal = cal.clone() as Calendar
            dayCal.set(Calendar.DAY_OF_MONTH, day)
            val time = dayCal.timeInMillis
            val startOfDay = getStartOfDayMillis(time)
            monthDays.add(
                CalendarDay(
                    dateMillis = time,
                    dayNumber = day,
                    isCurrentMonth = true,
                    isToday = startOfDay == todayMillis,
                    isSelected = startOfDay == selectedDayMillis,
                    hasTask = taskDates.contains(startOfDay),
                    hasShoppingTrip = shoppingDates.contains(startOfDay)
                )
            )
        }

        // Next month filler to complete 35 or 42 cells
        val totalCells = if (monthDays.size > 35) 42 else 35
        val remaining = totalCells - monthDays.size
        val nextCal = cal.clone() as Calendar
        nextCal.add(Calendar.MONTH, 1)
        for (day in 1..remaining) {
            val dayCal = nextCal.clone() as Calendar
            dayCal.set(Calendar.DAY_OF_MONTH, day)
            val time = dayCal.timeInMillis
            val startOfDay = getStartOfDayMillis(time)
            monthDays.add(
                CalendarDay(
                    dateMillis = time,
                    dayNumber = day,
                    isCurrentMonth = false,
                    isToday = startOfDay == todayMillis,
                    isSelected = startOfDay == selectedDayMillis,
                    hasTask = taskDates.contains(startOfDay),
                    hasShoppingTrip = shoppingDates.contains(startOfDay)
                )
            )
        }

        monthDays
    }

    Scaffold(
        floatingActionButton = {
            FloatingActionButton(
                onClick = onAddTaskClick,
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary,
                modifier = Modifier.testTag("add_calendar_task_fab")
            ) {
                Icon(Icons.Default.Add, contentDescription = "Add Calendar Task")
            }
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 16.dp)
        ) {
            Spacer(modifier = Modifier.height(12.dp))

            // Month Header Card Tile
            Card(
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceContainer
                ),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.surfaceVariant),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = monthYearFormat.format(currentCalendar.time),
                            style = MaterialTheme.typography.headlineMedium,
                            fontWeight = FontWeight.Black,
                            color = MaterialTheme.colorScheme.onBackground
                        )

                        Row {
                            IconButton(onClick = onPreviousMonth) {
                                Icon(Icons.Default.ChevronLeft, contentDescription = "Previous Month")
                            }
                            IconButton(onClick = onNextMonth) {
                                Icon(Icons.Default.ChevronRight, contentDescription = "Next Month")
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // Days of week header (MON TUE WED THU FRI SAT SUN)
                    Row(modifier = Modifier.fillMaxWidth()) {
                        val daysOfWeek = listOf("MON", "TUE", "WED", "THU", "FRI", "SAT", "SUN")
                        daysOfWeek.forEach { day ->
                            Text(
                                text = day,
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.outline,
                                textAlign = TextAlign.Center,
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    // Calendar Month Grid
                    LazyVerticalGrid(
                        columns = GridCells.Fixed(7),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(260.dp)
                    ) {
                        items(daysInGrid) { day ->
                            CalendarDayCell(
                                day = day,
                                onClick = { onSelectDate(day.dateMillis) }
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Selected Date Header
            Text(
                text = dayHeaderFormat.format(Date(selectedDateMillis)),
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Black,
                color = MaterialTheme.colorScheme.primary
            )

            Text(
                text = "Scheduled Tasks & Trips",
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(10.dp))

            // Scheduled items for selected date
            val totalItems = tasksForSelectedDate.size + shoppingTripsForSelectedDate.size

            if (totalItems == 0) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                        .padding(vertical = 24.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "No tasks scheduled for this day.\nTap '+' to add a task.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.outline,
                        textAlign = TextAlign.Center
                    )
                }
            } else {
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                    modifier = Modifier
                        .fillMaxSize()
                        .weight(1f)
                ) {
                    items(shoppingTripsForSelectedDate) { trip ->
                        ShoppingTripCalendarItem(trip = trip, onClick = { onTripClick(trip) })
                    }
                    items(tasksForSelectedDate) { task ->
                        TaskCalendarItem(
                            task = task,
                            onClick = { onTaskClick(task) },
                            onDelete = { onTaskDelete(task) }
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun CalendarDayCell(
    day: CalendarDay,
    onClick: () -> Unit
) {
    val backgroundColor = when {
        day.isSelected -> MaterialTheme.colorScheme.primary
        day.isToday -> MaterialTheme.colorScheme.primaryContainer
        else -> Color.Transparent
    }

    val textColor = when {
        day.isSelected -> MaterialTheme.colorScheme.onPrimary
        day.isToday -> MaterialTheme.colorScheme.onPrimaryContainer
        !day.isCurrentMonth -> MaterialTheme.colorScheme.outline.copy(alpha = 0.4f)
        else -> MaterialTheme.colorScheme.onSurface
    }

    Box(
        modifier = Modifier
            .aspectRatio(1f)
            .padding(2.dp)
            .clip(CircleShape)
            .background(backgroundColor)
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = day.dayNumber.toString(),
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = if (day.isToday || day.isSelected) FontWeight.Bold else FontWeight.Normal,
                color = textColor
            )

            // Indicators
            Row(
                horizontalArrangement = Arrangement.spacedBy(2.dp),
                modifier = Modifier.padding(top = 2.dp)
            ) {
                if (day.hasShoppingTrip) {
                    Box(
                        modifier = Modifier
                            .size(5.dp)
                            .clip(CircleShape)
                            .background(if (day.isSelected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.secondary)
                    )
                }
                if (day.hasTask) {
                    Box(
                        modifier = Modifier
                            .size(5.dp)
                            .clip(CircleShape)
                            .background(if (day.isSelected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.tertiary)
                    )
                }
            }
        }
    }
}

@Composable
private fun ShoppingTripCalendarItem(
    trip: ShoppingTripEntity,
    onClick: () -> Unit
) {
    val amPm = if (trip.timeHour >= 12) "PM" else "AM"
    val hour12 = if (trip.timeHour % 12 == 0) 12 else trip.timeHour % 12
    val timeFormatted = String.format(Locale.getDefault(), "%d:%02d %s", hour12, trip.timeMinute, amPm)

    Card(
        onClick = onClick,
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.secondaryContainer
        ),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Surface(
                shape = CircleShape,
                color = MaterialTheme.colorScheme.onSecondaryContainer.copy(alpha = 0.15f),
                modifier = Modifier.padding(end = 12.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.ShoppingBag,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSecondaryContainer,
                    modifier = Modifier.padding(10.dp)
                )
            }
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = trip.title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSecondaryContainer
                )
                Text(
                    text = timeFormatted,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSecondaryContainer.copy(alpha = 0.8f)
                )
            }
        }
    }
}

@Composable
private fun TaskCalendarItem(
    task: CalendarTaskEntity,
    onClick: () -> Unit,
    onDelete: () -> Unit
) {
    val amPm = if (task.timeHour >= 12) "PM" else "AM"
    val hour12 = if (task.timeHour % 12 == 0) 12 else task.timeHour % 12
    val timeFormatted = String.format(Locale.getDefault(), "%d:%02d %s", hour12, task.timeMinute, amPm)

    Card(
        onClick = onClick,
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainer
        ),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.surfaceVariant),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Surface(
                shape = CircleShape,
                color = MaterialTheme.colorScheme.primaryContainer,
                modifier = Modifier.padding(end = 12.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Schedule,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onPrimaryContainer,
                    modifier = Modifier.padding(10.dp)
                )
            }
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = task.title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = timeFormatted,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.Bold
                )
                if (task.description.isNotBlank()) {
                    Text(
                        text = task.description,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            IconButton(onClick = onDelete) {
                Icon(
                    imageVector = Icons.Default.Delete,
                    contentDescription = "Delete Task",
                    tint = MaterialTheme.colorScheme.outline
                )
            }
        }
    }
}

private fun getStartOfDayMillis(timeMillis: Long): Long {
    val cal = Calendar.getInstance().apply {
        this.timeInMillis = timeMillis
        set(Calendar.HOUR_OF_DAY, 0)
        set(Calendar.MINUTE, 0)
        set(Calendar.SECOND, 0)
        set(Calendar.MILLISECOND, 0)
    }
    return cal.timeInMillis
}

private fun isSameDay(time1: Long, time2: Long): Boolean {
    val cal1 = Calendar.getInstance().apply { timeInMillis = time1 }
    val cal2 = Calendar.getInstance().apply { timeInMillis = time2 }
    return cal1.get(Calendar.YEAR) == cal2.get(Calendar.YEAR) &&
            cal1.get(Calendar.DAY_OF_YEAR) == cal2.get(Calendar.DAY_OF_YEAR)
}

private fun hasTaskOnDate(dateMillis: Long, tasks: List<CalendarTaskEntity>): Boolean {
    return tasks.any { isSameDay(it.date, dateMillis) }
}

private fun hasShoppingOnDate(dateMillis: Long, trips: List<ShoppingTripEntity>): Boolean {
    return trips.any { isSameDay(it.date, dateMillis) }
}

