package com.flowtask.app.util

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Matrix
import android.net.Uri
import androidx.core.content.FileProvider
import android.media.ExifInterface
import java.io.File
import java.io.FileOutputStream
import java.io.InputStream
import kotlin.math.min

object ImageUtils {

    fun createInstantImageFile(context: Context): File {
        val instantsDir = File(context.filesDir, "instants")
        if (!instantsDir.exists()) {
            instantsDir.mkdirs()
        }
        return File(instantsDir, "instant_${System.currentTimeMillis()}.jpg")
    }

    fun getUriForFile(context: Context, file: File): Uri {
        return FileProvider.getUriForFile(
            context,
            "${context.packageName}.fileprovider",
            file
        )
    }

    /**
     * Ensures the image is oriented correctly and cropped to exact 1:1 square ratio.
     */
    fun processAndCropToSquare(file: File) {
        if (!file.exists()) return

        try {
            val boundsOptions = BitmapFactory.Options().apply {
                inJustDecodeBounds = true
            }
            BitmapFactory.decodeFile(file.absolutePath, boundsOptions)
            val origW = boundsOptions.outWidth
            val origH = boundsOptions.outHeight
            if (origW <= 0 || origH <= 0) return

            // Decode bitmap with reasonable scale if huge
            val maxDimension = 2048
            var sampleSize = 1
            var w = origW
            var h = origH
            while (w > maxDimension || h > maxDimension) {
                sampleSize *= 2
                w /= 2
                h /= 2
            }

            val decodeOptions = BitmapFactory.Options().apply {
                inSampleSize = sampleSize
                inPreferredConfig = Bitmap.Config.ARGB_8888
            }

            val decodedBitmap = BitmapFactory.decodeFile(file.absolutePath, decodeOptions) ?: return

            // Handle rotation from EXIF if needed
            val orientation = try {
                val exif = ExifInterface(file.absolutePath)
                exif.getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_NORMAL)
            } catch (e: Exception) {
                ExifInterface.ORIENTATION_NORMAL
            }

            val rotationDegrees = when (orientation) {
                ExifInterface.ORIENTATION_ROTATE_90 -> 90f
                ExifInterface.ORIENTATION_ROTATE_180 -> 180f
                ExifInterface.ORIENTATION_ROTATE_270 -> 270f
                else -> 0f
            }

            val rotatedBitmap = if (rotationDegrees != 0f) {
                val matrix = Matrix().apply { postRotate(rotationDegrees) }
                val rot = Bitmap.createBitmap(decodedBitmap, 0, 0, decodedBitmap.width, decodedBitmap.height, matrix, true)
                if (rot != decodedBitmap) decodedBitmap.recycle()
                rot
            } else {
                decodedBitmap
            }

            // Crop to exact 1:1 square
            val currentWidth = rotatedBitmap.width
            val currentHeight = rotatedBitmap.height
            val squareSize = min(currentWidth, currentHeight)
            val cropX = (currentWidth - squareSize) / 2
            val cropY = (currentHeight - squareSize) / 2

            val croppedBitmap = Bitmap.createBitmap(
                rotatedBitmap,
                cropX.coerceAtLeast(0),
                cropY.coerceAtLeast(0),
                squareSize.coerceAtMost(rotatedBitmap.width - cropX),
                squareSize.coerceAtMost(rotatedBitmap.height - cropY)
            )

            if (croppedBitmap != rotatedBitmap) {
                rotatedBitmap.recycle()
            }

            FileOutputStream(file).use { out ->
                croppedBitmap.compress(Bitmap.CompressFormat.JPEG, 92, out)
                out.flush()
            }
            croppedBitmap.recycle()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    /**
     * Backward-compatible alias for processAndCropToSquare.
     */
    fun processAndCropTo43(file: File) {
        processAndCropToSquare(file)
    }

    /**
     * Copy an image from a content URI to a local file and crop to 1:1 square.
     */
    fun copyAndProcessUri(context: Context, sourceUri: Uri, destFile: File): Boolean {
        return try {
            context.contentResolver.openInputStream(sourceUri)?.use { input ->
                FileOutputStream(destFile).use { output ->
                    input.copyTo(output)
                    output.flush()
                }
            }
            processAndCropToSquare(destFile)
            true
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }

    /**
     * Decodes a downsampled Bitmap for UI preview.
     */
    fun loadSampledBitmap(filePath: String, reqWidth: Int = 400, reqHeight: Int = 533): Bitmap? {
        val file = File(filePath)
        if (!file.exists()) return null

        return try {
            val options = BitmapFactory.Options().apply {
                inJustDecodeBounds = true
            }
            BitmapFactory.decodeFile(filePath, options)

            var inSampleSize = 1
            if (options.outHeight > reqHeight || options.outWidth > reqWidth) {
                val halfHeight = options.outHeight / 2
                val halfWidth = options.outWidth / 2
                while (halfHeight / inSampleSize >= reqHeight && halfWidth / inSampleSize >= reqWidth) {
                    inSampleSize *= 2
                }
            }

            val decodeOptions = BitmapFactory.Options().apply {
                this.inSampleSize = inSampleSize
                inPreferredConfig = Bitmap.Config.RGB_565
            }
            BitmapFactory.decodeFile(filePath, decodeOptions)
        } catch (e: Exception) {
            null
        }
    }

    /**
     * Decodes a high-resolution Bitmap for full-screen zoomable view.
     */
    fun loadHighResBitmap(filePath: String, maxDimension: Int = 2048): Bitmap? {
        return loadSampledBitmap(filePath, reqWidth = maxDimension, reqHeight = maxDimension)
    }

    fun deleteFile(filePath: String) {
        try {
            val file = File(filePath)
            if (file.exists()) {
                file.delete()
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}
