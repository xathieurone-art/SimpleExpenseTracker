package com.example.simpleexpensetracker

import android.app.DatePickerDialog
import android.os.Bundle
import androidx.compose.foundation.clickable
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.simpleexpensetracker.ui.theme.SimpleExpenseTrackerTheme
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.*

data class Expense(
    val id: Int = 0,
    val amount: Double,
    val category: String,
    val note: String,
    val date: Long
)

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            SimpleExpenseTrackerTheme {
                ExpenseTrackerApp()
            }
        }
    }
}

@Composable
fun ExpenseTrackerApp() {
    var expenses by remember { mutableStateOf(listOf<Expense>()) }
    var amountInput by remember { mutableStateOf("") }
    var categoryInput by remember { mutableStateOf("") }
    var noteInput by remember { mutableStateOf("") }
    var selectedDate by remember { mutableStateOf(System.currentTimeMillis()) }

    val context = LocalContext.current
    val calendar = Calendar.getInstance()

    Scaffold(
        floatingActionButton = {
            FloatingActionButton(onClick = {
                if (amountInput.isNotEmpty() && categoryInput.isNotEmpty()) {
                    val expense = Expense(
                        id = expenses.size + 1,
                        amount = amountInput.toDouble(),
                        category = categoryInput,
                        note = noteInput,
                        date = selectedDate
                    )
                    expenses = expenses + expense
                    amountInput = ""
                    categoryInput = ""
                    noteInput = ""
                }
            }) {
                Icon(Icons.Default.Add, contentDescription = "Add Expense")
            }
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .padding(16.dp)
                .fillMaxSize()
        ) {
            Text(
                text = "Home / Dashboard",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(8.dp))

            val total = expenses.sumOf { it.amount }
            Text(
                text = "₱${NumberFormat.getNumberInstance().format(total)} (Today / Week / Month)",
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(16.dp))

            Row(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.weight(1f)) {
                    Text("Add / Edit Expense", fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.height(8.dp))

                    OutlinedTextField(
                        value = amountInput,
                        onValueChange = { amountInput = it },
                        label = { Text("Amount") },
                        modifier = Modifier.fillMaxWidth()
                    )

                    OutlinedTextField(
                        value = categoryInput,
                        onValueChange = { categoryInput = it },
                        label = { Text("Category") },
                        modifier = Modifier.fillMaxWidth()
                    )

                    OutlinedTextField(
                        value = noteInput,
                        onValueChange = { noteInput = it },
                        label = { Text("Note") },
                        modifier = Modifier.fillMaxWidth()
                    )

                    // Calendar Picker
                    val dateFormat = SimpleDateFormat("MMM dd, yyyy", Locale.getDefault())
                    val formattedDate = dateFormat.format(Date(selectedDate))

                    OutlinedTextField(
                        value = formattedDate,
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Date") },
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                calendar.timeInMillis = selectedDate
                                DatePickerDialog(
                                    context,
                                    { _, year, month, day ->
                                        calendar.set(year, month, day)
                                        selectedDate = calendar.timeInMillis
                                    },
                                    calendar.get(Calendar.YEAR),
                                    calendar.get(Calendar.MONTH),
                                    calendar.get(Calendar.DAY_OF_MONTH)
                                ).show()
                            }
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Button(
                        onClick = {
                            if (amountInput.isNotEmpty() && categoryInput.isNotEmpty()) {
                                val expense = Expense(
                                    id = expenses.size + 1,
                                    amount = amountInput.toDouble(),
                                    category = categoryInput,
                                    note = noteInput,
                                    date = selectedDate
                                )
                                expenses = expenses + expense
                                amountInput = ""
                                categoryInput = ""
                                noteInput = ""
                            }
                        },
                        modifier = Modifier.align(Alignment.End)
                    ) {
                        Text("Save")
                    }
                }

                Spacer(modifier = Modifier.width(16.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Text("All Expenses", fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.height(8.dp))

                    LazyColumn(modifier = Modifier.fillMaxHeight()) {
                        items(expenses) { expense ->
                            ExpenseItem(expense)
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun ExpenseItem(expense: Expense) {
    val dateFormat = SimpleDateFormat("MMM dd, yyyy", Locale.getDefault())
    val formattedDate = dateFormat.format(Date(expense.date))

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        elevation = CardDefaults.cardElevation(4.dp)
    ) {
        Row(
            modifier = Modifier
                .padding(8.dp)
                .fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column {
                Text(expense.category, fontWeight = FontWeight.Bold)
                if (expense.note.isNotEmpty()) {
                    Text(expense.note, style = MaterialTheme.typography.bodySmall)
                }
            }
            Column(horizontalAlignment = Alignment.End) {
                Text("₱${expense.amount}", fontWeight = FontWeight.Bold)
                Text(formattedDate, style = MaterialTheme.typography.bodySmall)
            }
        }
    }
}
