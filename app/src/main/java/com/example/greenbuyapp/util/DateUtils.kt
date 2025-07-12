package com.example.greenbuyapp.util

import java.text.SimpleDateFormat
import java.util.*

object DateUtils {
    
    /**
     * Format ISO datetime string to readable format
     * Input: "2025-06-21T14:59:42.551487"
     * Output: "21/06/2025 14:59"
     */
    fun formatDateTime(isoString: String): String {
        return try {
            // Parse ISO format
            val inputFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSSSS", Locale.getDefault())
            val outputFormat = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault())
            
            val date = inputFormat.parse(isoString)
            outputFormat.format(date ?: Date())
        } catch (e: Exception) {
            try {
                // Try alternative format without microseconds
                val inputFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.getDefault())
                val outputFormat = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault())
                
                val date = inputFormat.parse(isoString)
                outputFormat.format(date ?: Date())
            } catch (e2: Exception) {
                // Return original string if parsing fails
                isoString
            }
        }
    }
    
    /**
     * Format date to short format
     * Input: "2025-06-21T14:59:42.551487"
     * Output: "21/06/2025"
     */
    fun formatDate(isoString: String): String {
        return try {
            val inputFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSSSS", Locale.getDefault())
            val outputFormat = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
            
            val date = inputFormat.parse(isoString)
            outputFormat.format(date ?: Date())
        } catch (e: Exception) {
            try {
                val inputFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.getDefault())
                val outputFormat = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
                
                val date = inputFormat.parse(isoString)
                outputFormat.format(date ?: Date())
            } catch (e2: Exception) {
                isoString
            }
        }
    }
    
    /**
     * Format time to relative format (e.g., "2 giờ trước", "1 ngày trước")
     */
    fun formatRelativeTime(isoString: String): String {
        return try {
            val inputFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSSSS", Locale.getDefault())
            val date = inputFormat.parse(isoString) ?: return isoString
            
            val now = Date()
            val diffInMs = now.time - date.time
            val diffInMinutes = diffInMs / (1000 * 60)
            val diffInHours = diffInMs / (1000 * 60 * 60)
            val diffInDays = diffInMs / (1000 * 60 * 60 * 24)
            
            when {
                diffInMinutes < 1 -> "Vừa xong"
                diffInMinutes < 60 -> "${diffInMinutes} phút trước"
                diffInHours < 24 -> "${diffInHours} giờ trước"
                diffInDays < 7 -> "${diffInDays} ngày trước"
                else -> formatDate(isoString)
            }
        } catch (e: Exception) {
            formatDateTime(isoString)
        }
    }
} 