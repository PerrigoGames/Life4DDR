package com.perrigogames.life4trials.util

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.Build
import android.os.Environment
import android.widget.ImageView
import com.perrigogames.life4trials.R
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.*
import kotlin.math.max
import kotlin.math.min


object DataUtil {

    val picturesDir: File
        get() = File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES), "LIFE4").also {
            it.mkdirs()
        }

    fun timestamp(locale: Locale, date: Date = Date()): String = SimpleDateFormat("yyyyMMdd_HHmmss", locale).format(date)

    fun humanTimestamp(locale: Locale, date: Date = Date()): String = SimpleDateFormat("yyyy-MM-dd   hh:mm aa", locale).format(date)

    fun humanNewlineTimestamp(locale: Locale, date: Date = Date()): String = SimpleDateFormat("yyyy-MM-dd\nhh:mm aa", locale).format(date)

    @Throws(IOException::class)
    fun createTempFile(locale: Locale): File = createTempFile("JPEG_${timestamp(locale)}_")

    @Throws(IOException::class)
    fun createTempFile(filename: String): File = File.createTempFile(filename, ".jpg", picturesDir)

    fun deleteExternalStoragePublicPicture(name: String) {
        File(picturesDir, name).delete()
    }

    fun hasExternalStoragePublicPicture(name: String): Boolean = File(picturesDir, name).exists()

    fun createScaledBitmap(path: String, targetW: Int, targetH: Int): Bitmap? {
        val bmOptions = BitmapFactory.Options().apply {
            // Get the dimensions of the bitmap
            inJustDecodeBounds = true
            BitmapFactory.decodeFile(path, this)

            // Determine how much to scale down the image
            val scaleFactor: Int = min(outWidth / targetW, outHeight / targetH)

            // Decode the image file into a Bitmap sized to fill the View
            inJustDecodeBounds = false
            inSampleSize = scaleFactor
            @Suppress("DEPRECATION")
            inPurgeable = true
        }
        return BitmapFactory.decodeFile(path, bmOptions)
    }

    fun resizeImage(locale: Locale, width: Int, height: Int, bitmap: Bitmap): String? =
        resizeImage(createTempFile(locale), width, height, bitmap)

    fun resizeImage(outputFile: File, width: Int, height: Int, bitmap: Bitmap): String? {
        return try {
            val out = FileOutputStream(outputFile)
            scaleBitmap(bitmap, width, height).compress(Bitmap.CompressFormat.JPEG, 85, out)
            out.flush()
            out.close()
            outputFile.absolutePath
        } catch (e: IOException) {
            e.printStackTrace()
            null
        }
    }

    fun scaleBitmap(bitmap: Bitmap, width: Int, height: Int): Bitmap {
        val scaleFactor = max(1, min(bitmap.width / width, bitmap.height / height)).toFloat()
        return Bitmap.createScaledBitmap(bitmap,
            (bitmap.width / scaleFactor).toInt(),
            (bitmap.height / scaleFactor).toInt(),
            true)
    }
}

fun ImageView.setScaledBitmapFromFile(path: String,
                                      targetW: Int = this.width,
                                      targetH: Int = this.height) {

    DataUtil.createScaledBitmap(path, targetW, targetH)?.also { bitmap ->
        setImageBitmap(bitmap)
    }
}

@Suppress("DEPRECATION")
val Context.locale: Locale get() = when {
    Build.VERSION.SDK_INT >= Build.VERSION_CODES.N -> resources.configuration.locales[0]
    else -> resources.configuration.locale
}


fun List<String>.toListString(c: Context, lastModifierFormatRes: Int = R.string.or_s): String = StringBuilder().apply {
    this@toListString.forEachIndexed { index, d ->
        append(when {
            this@toListString.size == 1 -> d
            index == this@toListString.lastIndex -> c.getString(lastModifierFormatRes, d)
            index == this@toListString.lastIndex - 1 -> "$d "
            else -> "$d, "
        })
    }
}.toString()
