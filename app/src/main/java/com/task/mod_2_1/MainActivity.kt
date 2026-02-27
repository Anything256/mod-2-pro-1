package com.module_2_1

import android.os.Bundle
import android.text.InputType
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import com.google.gson.Gson
import model.FormConfig

class MainActivity : AppCompatActivity() {

    private lateinit var formContainer: LinearLayout
    private lateinit var resultTextView: TextView
    private val viewMap = mutableMapOf<String, View>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        formContainer = findViewById(R.id.formContainer)
        resultTextView = findViewById(R.id.resultTextView)

        val formConfig = loadJsonForm()
        generateForm(formConfig)

        findViewById<Button>(R.id.submitButton).setOnClickListener {
            if (validateForm(formConfig)) {
                displayResults(formConfig)
                Toast.makeText(this, "Form Submitted!", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun loadJsonForm(): FormConfig {
        val inputStream = resources.openRawResource(R.raw.form_config)
        val json = inputStream.bufferedReader().use { it.readText() }
        return Gson().fromJson(json, FormConfig::class.java)
    }

    private fun generateForm(formConfig: FormConfig) {
        val padding = (8 * resources.displayMetrics.density).toInt()

        formConfig.fields.forEach { field ->
            if (field.type != "checkbox") {
                val label = TextView(this)
                label.text = field.label
                label.setPadding(0, padding, 0, 0)
                formContainer.addView(label)
            }

            when (field.type) {
                "text", "email", "phone", "number" -> {
                    val editText = EditText(this)
                    editText.hint = field.hint

                    editText.inputType = when (field.type) {
                        "email" -> InputType.TYPE_TEXT_VARIATION_EMAIL_ADDRESS
                        "phone" -> InputType.TYPE_CLASS_PHONE
                        "number" -> InputType.TYPE_CLASS_NUMBER
                        else -> InputType.TYPE_CLASS_TEXT
                    }

                    formContainer.addView(editText)
                    viewMap[field.label] = editText
                }

                "dropdown" -> {
                    val spinner = Spinner(this)
                    val adapter = ArrayAdapter(
                        this,
                        android.R.layout.simple_spinner_item,
                        field.options ?: emptyList()
                    )
                    adapter.setDropDownViewResource(
                        android.R.layout.simple_spinner_dropdown_item
                    )
                    spinner.adapter = adapter
                    spinner.setPadding(0, padding, 0, padding)

                    formContainer.addView(spinner)
                    viewMap[field.label] = spinner
                }

                "checkbox" -> {
                    val checkBox = CheckBox(this)
                    checkBox.text = field.label
                    val params = LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.MATCH_PARENT,
                        LinearLayout.LayoutParams.WRAP_CONTENT
                    )
                    params.setMargins(0, padding, 0, padding)
                    checkBox.layoutParams = params

                    formContainer.addView(checkBox)
                    viewMap[field.label] = checkBox
                }
            }
        }
    }

    private fun validateForm(formConfig: FormConfig): Boolean {
        for (field in formConfig.fields) {
            val view = viewMap[field.label]
            if (field.required) {
                when (view) {
                    is EditText -> {
                        if (view.text.isNullOrEmpty()) {
                            view.error = "${field.label} is required"
                            view.requestFocus()
                            return false
                        }
                    }
                    is Spinner -> {
                        if (view.selectedItem == null) {
                            Toast.makeText(this, "${field.label} is required", Toast.LENGTH_SHORT).show()
                            return false
                        }
                    }
                    is CheckBox -> {
                        if (!view.isChecked) {
                            Toast.makeText(this, "Please accept ${field.label}", Toast.LENGTH_SHORT).show()
                            return false
                        }
                    }
                }
            }
        }
        return true
    }

    private fun displayResults(formConfig: FormConfig) {
        val result = StringBuilder("Submitted Details:\n\n")
        for (field in formConfig.fields) {
            val value = when (val view = viewMap[field.label]) {
                is EditText -> view.text.toString()
                is Spinner -> view.selectedItem?.toString() ?: "None"
                is CheckBox -> if (view.isChecked) "Accepted" else "Not Accepted"
                else -> ""
            }
            result.append("${field.label}: $value\n")
        }
        resultTextView.text = result.toString()
        resultTextView.visibility = View.VISIBLE
    }
}
