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
