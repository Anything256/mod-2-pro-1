package model

data class FormConfig(
    val fields: List<FormField>
)

data class FormField(
    val type: String,
    val label: String,
    val hint: String? = null,
    val required: Boolean = false,
    val options: List<String>? = null
)