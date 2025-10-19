package com.example.simpleexpensetracker

import android.os.Bundle
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.ui.semantics.text
// ✅ ADDED: Import for your ViewModel.
import com.example.simpleexpensetracker.ExpenseViewModel
import com.example.simpleexpensetracker.data.Expense
import com.example.simpleexpensetracker.databinding.ActivityAddExpenseBinding

class AddExpenseActivity : AppCompatActivity() {
    private lateinit var binding: ActivityAddExpenseBinding
    private val viewModel: ExpenseViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAddExpenseBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.btnSave.setOnClickListener {
            val amountText = binding.etAmount.text.toString()
            val category = binding.etCategory.text.toString()

            // Basic validation to prevent saving empty entries
            if (amountText.isNotBlank() && category.isNotBlank()) {
                val amount = amountText.toDoubleOrNull() ?: 0.0
                val note = binding.etNote.text.toString()

                val expense = Expense(
                    amount = amount,
                    category = category,
                    note = note,
                    date = System.currentTimeMillis()
                )
                viewModel.addExpense(expense)
                finish() // Close the activity and return to the previous screen
            }
        }
    }
}
