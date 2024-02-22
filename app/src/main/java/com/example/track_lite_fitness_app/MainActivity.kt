package com.example.track_lite_fitness_app

import android.app.DatePickerDialog
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.example.track_lite_fitness_app.R
import java.text.SimpleDateFormat
import java.util.ArrayList
import java.util.Calendar
import java.util.Locale
data class DataItem(
    var rawValue: String,
    var parsedData: ParsedData
)

data class ParsedData(
    var reps: String
)


class MainActivity : AppCompatActivity() {

    private lateinit var currentDate: Calendar
    private val myDataMap = hashMapOf<String, MutableList<DataItem>>()
    val dateFormat = SimpleDateFormat("MMM dd, yyyy", Locale.getDefault())
    private lateinit var verticalStack: LinearLayout

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val tvToday: TextView = findViewById(R.id.tvToday)
        val btnBack: Button = findViewById(R.id.btnBack)
        val btnForward: Button = findViewById(R.id.btnForward)
        val btnDateMenu: Button = findViewById(R.id.btnDateMenu)
        verticalStack = findViewById(R.id.verticalStack)

        currentDate = Calendar.getInstance()
        updateDateDisplay(tvToday)

        btnBack.setOnClickListener { navigateDate(-1) }
        btnForward.setOnClickListener { navigateDate(1) }
        btnDateMenu.setOnClickListener { showDatePickerDialog() }
    }

    private fun updateDateDisplay(tvToday: TextView) {
        verticalStack.removeAllViews()
        if(myDataMap[dateFormat.format(currentDate.time)] == null){
                myDataMap[dateFormat.format(currentDate.time)] =
                    mutableListOf(DataItem("", ParsedData("")))
        }
        myDataMap[dateFormat.format(currentDate.time)]?.forEach { _ ->
            createNewTextField(verticalStack, false)
        }
        tvToday.text = dateFormat.format(currentDate.time)
    }

    private fun navigateDate(offset: Int) {
        currentDate.add(Calendar.DAY_OF_MONTH, offset)
        updateDateDisplay(findViewById(R.id.tvToday))
    }

    private fun showDatePickerDialog() {
        val datePicker = DatePickerDialog(
            this,
            DatePickerDialog.OnDateSetListener { _, year, month, day ->
                currentDate.set(year, month, day)
                updateDateDisplay(findViewById(R.id.tvToday))
            },
            currentDate.get(Calendar.YEAR),
            currentDate.get(Calendar.MONTH),
            currentDate.get(Calendar.DAY_OF_MONTH)
        )
        datePicker.show()
    }

    private fun createNewTextField(container: LinearLayout, isNew: Boolean) {
        val editText = EditText(this).apply {
            layoutParams = ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
            )


            if (isNew) {
                myDataMap[dateFormat.format(currentDate.time)]?.add(DataItem("", ParsedData("")))
            }
            setText(myDataMap[dateFormat.format(currentDate.time)]?.get(container.childCount)?.rawValue)

            // Add a TextWatcher to detect when the text changes
            addTextChangedListener(object : TextWatcher {
                val index = container.childCount;
                override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

                override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}

                override fun afterTextChanged(s: Editable?) {
                    myDataMap[dateFormat.format(currentDate.time)]?.get(index)?.rawValue = s.toString()
                    if (!s.isNullOrEmpty() && index + 1 == myDataMap[dateFormat.format(currentDate.time)]?.size) {
                        createNewTextField(container, true)
                    } else if (s.isNullOrEmpty() && index + 2 == myDataMap[dateFormat.format(currentDate.time)]?.size) {
                        container.removeViewAt(container.childCount -1)
                        myDataMap[dateFormat.format(currentDate.time)]?.removeAt(container.childCount -1)
                    }
                }
            })
        }

        container.addView(editText)
    }
}