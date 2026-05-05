package com.example.kotlin_nfc_manager

import android.util.Patterns

/**
 * Intelligent parser to convert raw NDEF data into structured business objects.
 */
object NfcDataParser {

    enum class DataType { TEXT, URL, EMAIL, PHONE, VCARD, UNKNOWN }

    data class ParsedResult(
        val type: DataType,
        val displayValue: String,
        val label: String
    )

    fun parse(rawContent: String?): ParsedResult {
        if (rawContent == null || rawContent.isEmpty()) {
            return ParsedResult(DataType.UNKNOWN, "No data found", "Empty")
        }

        return when {
            rawContent.contains("BEGIN:VCARD", true) -> 
                ParsedResult(DataType.VCARD, "Contact Business Card", "vCard")
            
            Patterns.EMAIL_ADDRESS.matcher(rawContent).matches() -> 
                ParsedResult(DataType.EMAIL, rawContent, "Email Address")
            
            Patterns.WEB_URL.matcher(rawContent).matches() -> 
                ParsedResult(DataType.URL, rawContent, "Web Link")
            
            Patterns.PHONE.matcher(rawContent).matches() -> 
                ParsedResult(DataType.PHONE, rawContent, "Phone Number")
            
            else -> ParsedResult(DataType.TEXT, rawContent, "Text Message")
        }
    }
}
