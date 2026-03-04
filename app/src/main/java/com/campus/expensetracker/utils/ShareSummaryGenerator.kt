package com.campus.expensetracker.utils

import android.content.Context
import android.content.Intent
import android.graphics.*
import android.net.Uri
import androidx.core.content.FileProvider
import java.io.File
import java.io.FileOutputStream

object ShareSummaryGenerator {

    fun generateSummaryImage(
        context: Context,
        tripName: String,
        totalExpense: Double,
        friendBalances: List<FriendBalanceData>
    ): Uri? {
        return try {
            // Calculate dimensions
            val width = 1080
            val headerHeight = 200
            val totalHeightHeight = 150
            val friendItemHeight = 180
            val balanceItemHeight = 180
            val padding = 60
            val spacing = 40
            
            val friendSectionHeight = friendBalances.size * friendItemHeight + spacing
            val balanceSectionHeight = friendBalances.size * balanceItemHeight + spacing
            
            val height = headerHeight + totalHeightHeight + padding + friendSectionHeight + padding + balanceSectionHeight + padding * 2
            
            // Create bitmap
            val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
            val canvas = Canvas(bitmap)
            
            // Background
            val backgroundPaint = Paint().apply {
                color = Color.parseColor("#FFFFFF")
            }
            canvas.drawRect(0f, 0f, width.toFloat(), height.toFloat(), backgroundPaint)
            
            // Header background
            val headerPaint = Paint().apply {
                color = Color.parseColor("#4CAF50")
            }
            canvas.drawRect(0f, 0f, width.toFloat(), headerHeight.toFloat(), headerPaint)
            
            // Trip name
            val titlePaint = Paint().apply {
                color = Color.WHITE
                textSize = 72f
                isAntiAlias = true
                textAlign = Paint.Align.CENTER
                typeface = Typeface.DEFAULT_BOLD
            }
            canvas.drawText(tripName, width / 2f, 120f, titlePaint)
            
            // Total expense section
            val totalBgPaint = Paint().apply {
                color = Color.parseColor("#E8F5E9")
            }
            canvas.drawRect(
                padding.toFloat(),
                headerHeight.toFloat(),
                (width - padding).toFloat(),
                (headerHeight + totalHeightHeight).toFloat(),
                totalBgPaint
            )
            
            val totalLabelPaint = Paint().apply {
                color = Color.parseColor("#2E7D32")
                textSize = 40f
                isAntiAlias = true
                textAlign = Paint.Align.CENTER
            }
            canvas.drawText("Total Expense", width / 2f, headerHeight + 60f, totalLabelPaint)
            
            val totalValuePaint = Paint().apply {
                color = Color.parseColor("#1B5E20")
                textSize = 80f
                isAntiAlias = true
                textAlign = Paint.Align.CENTER
                typeface = Typeface.DEFAULT_BOLD
            }
            canvas.drawText("₹${String.format("%.2f", totalExpense)}", width / 2f, headerHeight + 130f, totalValuePaint)
            
            var currentY = headerHeight + totalHeightHeight + padding
            
            // Friends section header
            val sectionTitlePaint = Paint().apply {
                color = Color.parseColor("#424242")
                textSize = 48f
                isAntiAlias = true
                textAlign = Paint.Align.LEFT
                typeface = Typeface.DEFAULT_BOLD
            }
            canvas.drawText("💰 Amount Paid", padding.toFloat(), currentY + 50f, sectionTitlePaint)
            currentY += spacing
            
            // Friend balances
            val friendNamePaint = Paint().apply {
                color = Color.parseColor("#212121")
                textSize = 40f
                isAntiAlias = true
                textAlign = Paint.Align.LEFT
                typeface = Typeface.DEFAULT
            }
            val friendAmountPaint = Paint().apply {
                color = Color.parseColor("#4CAF50")
                textSize = 40f
                isAntiAlias = true
                textAlign = Paint.Align.RIGHT
                typeface = Typeface.DEFAULT_BOLD
            }
            
            friendBalances.forEach { balance ->
                canvas.drawText(
                    balance.friendName,
                    padding.toFloat(),
                    currentY + 50f,
                    friendNamePaint
                )
                canvas.drawText(
                    "₹${String.format("%.2f", balance.paidAmount)}",
                    (width - padding).toFloat(),
                    currentY + 50f,
                    friendAmountPaint
                )
                
                // Divider line
                val dividerPaint = Paint().apply {
                    color = Color.parseColor("#E0E0E0")
                    strokeWidth = 3f
                }
                canvas.drawLine(
                    padding.toFloat(),
                    currentY + 80f,
                    (width - padding).toFloat(),
                    currentY + 80f,
                    dividerPaint
                )
                
                currentY += friendItemHeight
            }
            
            currentY += spacing - 40
            
            // Balance section header
            canvas.drawText("⚖️ Balance", padding.toFloat(), currentY + 50f, sectionTitlePaint)
            currentY += spacing
            
            // Balances
            val balanceReceivePaint = Paint().apply {
                color = Color.parseColor("#4CAF50")
                textSize = 36f
                isAntiAlias = true
                textAlign = Paint.Align.LEFT
                typeface = Typeface.DEFAULT
            }
            val balancePayPaint = Paint().apply {
                color = Color.parseColor("#F44336")
                textSize = 36f
                isAntiAlias = true
                textAlign = Paint.Align.LEFT
                typeface = Typeface.DEFAULT
            }
            val balanceSettledPaint = Paint().apply {
                color = Color.parseColor("#757575")
                textSize = 36f
                isAntiAlias = true
                textAlign = Paint.Align.LEFT
                typeface = Typeface.DEFAULT
            }
            
            friendBalances.forEach { balance ->
                val balanceText = when {
                    balance.balance > 0 -> "${balance.friendName} should receive ₹${String.format("%.2f", balance.balance)}"
                    balance.balance < 0 -> "${balance.friendName} owes ₹${String.format("%.2f", -balance.balance)}"
                    else -> "${balance.friendName} - Settled up"
                }
                
                val paint = when {
                    balance.balance > 0 -> balanceReceivePaint
                    balance.balance < 0 -> balancePayPaint
                    else -> balanceSettledPaint
                }
                
                canvas.drawText(balanceText, padding.toFloat(), currentY + 50f, paint)
                currentY += balanceItemHeight
            }
            
            // Save image
            val imagesDir = File(context.cacheDir, "shared_images")
            imagesDir.mkdirs()
            val imageFile = File(imagesDir, "trip_summary_${System.currentTimeMillis()}.png")
            
            FileOutputStream(imageFile).use { out ->
                bitmap.compress(Bitmap.CompressFormat.PNG, 100, out)
            }
            
            bitmap.recycle()
            
            // Get URI using FileProvider
            FileProvider.getUriForFile(
                context,
                "${context.packageName}.fileprovider",
                imageFile
            )
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    fun shareImage(context: Context, imageUri: Uri) {
        try {
            val shareIntent = Intent().apply {
                action = Intent.ACTION_SEND
                putExtra(Intent.EXTRA_STREAM, imageUri)
                type = "image/png"
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            }
            context.startActivity(Intent.createChooser(shareIntent, "Share expense summary"))
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    data class FriendBalanceData(
        val friendName: String,
        val paidAmount: Double,
        val balance: Double
    )
}
