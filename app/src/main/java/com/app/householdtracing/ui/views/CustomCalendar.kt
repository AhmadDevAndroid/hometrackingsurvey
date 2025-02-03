package com.app.householdtracing.ui.views

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.app.householdtracing.R
import com.app.householdtracing.ui.theme.HouseHoldTheme
import com.app.householdtracing.ui.theme.hintColor
import com.app.householdtracing.ui.theme.secondaryTextColorDark
import com.app.householdtracing.ui.viewmodels.SharedViewModel
import java.text.SimpleDateFormat
import java.time.LocalDate
import java.time.YearMonth
import java.time.ZoneId
import java.time.format.TextStyle
import java.util.Date
import java.util.Locale

@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun CalendarView(
    sharedVM: SharedViewModel
) {
    var selectedDate by remember { mutableStateOf(LocalDate.now()) }
    var currentMonth by remember { mutableStateOf(YearMonth.now()) }
    val dateFormat = SimpleDateFormat("dd / MMM / yyyy", Locale.getDefault())
    val today = LocalDate.now()

    Card(
        shape = RoundedCornerShape(14.dp),
        elevation = CardDefaults.cardElevation(6.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        modifier = Modifier
            .fillMaxWidth()
            .wrapContentHeight()
            .padding(HouseHoldTheme.dimens.grid_3_5)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            MonthYearNavigation(
                currentMonth = currentMonth,
                onMonthChange = { newMonth -> currentMonth = newMonth }
            )

            val weekDays = listOf("Mon", "Tue", "Wed", "Thu", "Fri", "Sat", "Sun")
            Row(
                modifier = Modifier.fillMaxWidth().padding(top = 16.dp),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                weekDays.forEach { day ->
                    Box(modifier = Modifier.weight(1f), contentAlignment = Alignment.Center) {
                        Text(
                            text = day,
                            textAlign = TextAlign.Center,
                            color = hintColor,
                            style = MaterialTheme.typography.displaySmall.copy(
                                lineHeight = 12.88.sp
                            )
                        )
                    }
                }
            }
            val daysInMonth = currentMonth.lengthOfMonth()
            val firstDayOfWeek =
                (currentMonth.atDay(1).dayOfWeek.value + 6) % 7

            Column(
                modifier = Modifier.padding(top = 16.dp)
            ) {
                var dayCounter = 1 - firstDayOfWeek
                for (row in 0 until 6) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        for (col in 0 until 7) {
                            val day = dayCounter++
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .aspectRatio(1f),
                                contentAlignment = Alignment.Center
                            ) {
                                if (day in 1..daysInMonth) {
                                    val date = currentMonth.atDay(day)
                                    DayItem(
                                        date = date,
                                        isToday = date == today,
                                        isSelected = date == selectedDate,
                                        isFutureDate = date > today,
                                        onClick = {
                                            if (date <= today) {
                                                selectedDate = date
                                                val formattedDate = dateFormat.format(
                                                    Date.from(
                                                        date.atStartOfDay(
                                                            ZoneId.systemDefault()
                                                        ).toInstant()
                                                    )
                                                )
                                                sharedVM.currentDate =
                                                    formattedDate
                                            }
                                        }
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun DayItem(
    date: LocalDate,
    isToday: Boolean,
    isSelected: Boolean,
    isFutureDate: Boolean,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .size(width = 32.26.dp, height = 30.dp)
            .background(
                color = when {
                    isSelected -> MaterialTheme.colorScheme.primary
                    isToday -> MaterialTheme.colorScheme.secondary
                    else -> Color.Transparent
                },
                shape = RoundedCornerShape(12.dp)
            )
            .clickable(enabled = !isFutureDate) { onClick() },
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = date.dayOfMonth.toString(),
            color = if (isFutureDate) Color.Gray else secondaryTextColorDark,
            style = MaterialTheme.typography.displaySmall.copy(
                lineHeight = 12.88.sp
            ),
            textAlign = TextAlign.Center
        )
    }
}

@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun MonthYearNavigation(
    currentMonth: YearMonth,
    onMonthChange: (YearMonth) -> Unit
) {
    val today = YearMonth.now()
    val minYear = today.year - 1
    val maxYear = today.year

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            IconButton(
                onClick = {
                    if (currentMonth.year > minYear || currentMonth.monthValue > 1) onMonthChange(
                        currentMonth.minusMonths(1)
                    )
                },
                modifier = Modifier.size(24.dp)
            ) {
                Icon(
                    painterResource(R.drawable.ic_backward),
                    contentDescription = "Previous Month",
                    tint = MaterialTheme.colorScheme.primary
                )
            }
            Text(
                text = currentMonth.month.getDisplayName(TextStyle.FULL, Locale.getDefault()),
                style = MaterialTheme.typography.titleMedium.copy(
                    fontSize = 15.sp,
                    lineHeight = 22.4.sp
                ),
                color = MaterialTheme.colorScheme.primary
            )
            IconButton(
                onClick = {
                    if (currentMonth.year < maxYear || currentMonth.monthValue < 12) onMonthChange(
                        currentMonth.plusMonths(1)
                    )
                },
                modifier = Modifier.size(24.dp)
            ) {
                Icon(
                    painterResource(R.drawable.ic_forward),
                    contentDescription = "Next Month",
                    tint = MaterialTheme.colorScheme.primary
                )
            }
        }

        Row(verticalAlignment = Alignment.CenterVertically) {
            IconButton(
                enabled = currentMonth.year > minYear,
                onClick = { if (currentMonth.year > minYear) onMonthChange(currentMonth.minusYears(1)) },
                modifier = Modifier.size(24.dp)
            ) {
                Icon(
                    painterResource(R.drawable.ic_backward),
                    contentDescription = "Previous Year",
                    tint = if (currentMonth.year > minYear) MaterialTheme.colorScheme.primary else Color.Gray
                )
            }
            Text(
                text = currentMonth.year.toString(),
                style = MaterialTheme.typography.titleMedium.copy(
                    fontSize = 15.sp,
                    lineHeight = 22.4.sp
                ),
                color = MaterialTheme.colorScheme.primary
            )
            IconButton(
                enabled = currentMonth.year < maxYear,
                onClick = { if (currentMonth.year < maxYear) onMonthChange(currentMonth.plusYears(1)) },
                modifier = Modifier.size(24.dp)
            ) {
                Icon(
                    painterResource(R.drawable.ic_forward),
                    contentDescription = "Next Year",
                    tint = if (currentMonth.year < maxYear) MaterialTheme.colorScheme.primary else Color.Gray
                )
            }
        }
    }
}