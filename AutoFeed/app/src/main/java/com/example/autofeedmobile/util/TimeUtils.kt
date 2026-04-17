package com.example.autofeedmobile.util

fun formatTimeOnly(timeString: String?): String {
    if (timeString == null) return "N/A"
    return try {
        // Expected format "HH:mm:ss" or just "HH:mm"
        if (timeString.contains(":")) {
            val parts = timeString.split(":")
            if (parts.size >= 2) {
                "${parts[0]}:${parts[1]}"
            } else {
                timeString
            }
        } else {
            timeString
        }
    } catch (e: Exception) {
        "N/A"
    }
}

fun formatDate(dateString: String?): String {
    if (dateString.isNullOrEmpty()) return "N/A"
    return try {
        // Try to handle ISO format YYYY-MM-DD...
        val datePart = if (dateString.contains("T")) {
            dateString.split("T")[0]
        } else {
            dateString
        }
        
        val parts = datePart.split("-")
        if (parts.size == 3) {
            // parts[0] is year, parts[1] is month, parts[2] is day
            "${parts[2]}-${parts[1]}-${parts[0]}"
        } else {
            dateString
        }
    } catch (e: Exception) {
        dateString ?: "N/A"
    }
}
