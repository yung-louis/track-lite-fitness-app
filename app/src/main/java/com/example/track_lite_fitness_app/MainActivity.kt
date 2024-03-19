package com.example.track_lite_fitness_app

import android.app.DatePickerDialog
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.ViewGroup
import android.widget.EditText
import android.widget.ImageButton
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.room.Room
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
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
    private lateinit var currentSelectedData: MutableList<DataItem>
    private val dateFormat = SimpleDateFormat("dd MMM yy", Locale.getDefault())
    private lateinit var verticalStack: LinearLayout
    private val gson = Gson()

    private lateinit var db: AppDatabase

    private lateinit var dataItemDao: DataItemDao

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        db = Room.databaseBuilder(
            applicationContext,
            AppDatabase::class.java, "database-name"
        ).build()

        dataItemDao = db.dataItemDao()

        val tvDate: TextView = findViewById(R.id.tvDate)
        val btnBack: ImageButton = findViewById(R.id.btnBack)
        val btnForward: ImageButton = findViewById(R.id.btnForward)
        val btnDateMenu: ImageButton = findViewById(R.id.btnDateMenu)
        val btnSettings: ImageButton = findViewById(R.id.btnSettings)
        verticalStack = findViewById(R.id.verticalStack)

        currentDate = Calendar.getInstance()
        updateDateDisplay(tvDate)
        getCurrentDayData()

        btnBack.setOnClickListener { navigateDate(-1) }
        btnForward.setOnClickListener { navigateDate(1) }
        btnDateMenu.setOnClickListener { showDatePickerDialog() }
        btnSettings.setOnClickListener { deleteAllData() }
    }

    @OptIn(DelicateCoroutinesApi::class)
    private fun getCurrentDayData() {
        GlobalScope.launch(Dispatchers.Default) {
            val formattedDate = dateFormat.format(currentDate.time)
            var currentDay = dataItemDao.getDataItemsByDateId(formattedDate)
            if(currentDay == null){
                dataItemDao.insertDataType(DataType(formattedDate, """[{"rawValue": "", "parsedData": { "reps": 0 } }]""".trimMargin()))
                currentDay = dataItemDao.getDataItemsByDateId(formattedDate)
            }
            val dataType = object : TypeToken<List<DataItem>>() {}.type
            currentSelectedData = gson.fromJson(currentDay?.json, dataType)
            withContext(Dispatchers.Main) {
                verticalStack.removeAllViews()
                currentSelectedData.forEach { _ ->
                    createNewTextField(verticalStack, false)
                }
            }
        }
    }

    @OptIn(DelicateCoroutinesApi::class)
    private fun deleteAllData() {
        GlobalScope.launch{
            dataItemDao.deleteAllData()
            getCurrentDayData()
        }
    }
    private suspend fun updateCurrentDay() {
        val formattedDate = dateFormat.format(currentDate.time)
        dataItemDao.updateDataType(DataType(formattedDate, gson.toJson(currentSelectedData)))
    }


    private fun updateDateDisplay(tvDate: TextView) {
        val formattedDate = dateFormat.format(currentDate.time)
        if(formattedDate == dateFormat.format(Calendar.getInstance().time)) {
            tvDate.text = "Today"
        } else {
            tvDate.text = formattedDate
        }
    }

    @OptIn(DelicateCoroutinesApi::class)
    private fun navigateDate(offset: Int) {
        currentDate.add(Calendar.DAY_OF_MONTH, offset)
        updateDateDisplay(findViewById(R.id.tvDate))
        getCurrentDayData()
    }

    private fun showDatePickerDialog() {
        val datePicker = DatePickerDialog(
            this, R.style.datePicker,
            DatePickerDialog.OnDateSetListener { _, year, month, day ->
                currentDate.set(year, month, day)
                updateDateDisplay(findViewById(R.id.tvDate))
                getCurrentDayData()
            },
            currentDate.get(Calendar.YEAR),
            currentDate.get(Calendar.MONTH),
            currentDate.get(Calendar.DAY_OF_MONTH)

        )
        datePicker.show()
    }

    @OptIn(DelicateCoroutinesApi::class)
    private fun createNewTextField(container: LinearLayout, isNew: Boolean) {
        val editText = EditText(this).apply {
            layoutParams = ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
            )
            if (isNew) {
                currentSelectedData.add(DataItem("", ParsedData("")))
            }
            setText(currentSelectedData[container.childCount].rawValue)

            // Add a TextWatcher to detect when the text changes
            addTextChangedListener(object : TextWatcher {
                val index = container.childCount
                override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

                override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}

                override fun afterTextChanged(s: Editable?) {
                    currentSelectedData[index].rawValue = s.toString()
                    if (!s.isNullOrEmpty() && index + 1 == currentSelectedData.size) {
                        createNewTextField(container, true)
                    } else if (s.isNullOrEmpty() && index + 2 == currentSelectedData.size) {
                        container.removeViewAt(container.childCount -1)
                        currentSelectedData.removeAt(container.childCount -1)
                    }
                    GlobalScope.launch {updateCurrentDay()}
                }
            })
        }

        container.addView(editText)
    }
}