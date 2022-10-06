package com.example.kbbqreview.util

import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.net.Uri
import android.provider.MediaStore
import androidx.core.content.FileProvider
import androidx.core.net.toUri
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.FileOutputStream
import java.io.OutputStream


class ShareUtils {

    companion object {
        fun saveBitmapAndGetUri(context: Context, bitmap: Bitmap): Uri {
            val path: String = context.externalCacheDir.toString() + "/filename.jpg"
            var out: OutputStream? = null
            val file = File(path)
            try {
                out = FileOutputStream(file)
                bitmap.compress(Bitmap.CompressFormat.JPEG, 100, out)
                out.flush()
                out.close()
            } catch (e: Exception) {
                e.printStackTrace()
            }
            return FileProvider.getUriForFile(
                context, "com.example.kbbqreview.util.provider", file
            )
        }

        fun shareImageToOthers(
            context: Context,
            text: String?,
            address: String,
            bitmap: Bitmap?,
        ): Intent {
            println("Starting the function...")
            val imageUri: Uri? = bitmap?.let { saveBitmapAndGetUri(context, it) }
            println("1")
            val chooserIntent2 = Intent.createChooser(Intent().apply {
                action = Intent.ACTION_SEND
                putExtra(Intent.EXTRA_SUBJECT, text)
                putExtra(Intent.EXTRA_TEXT, address)
                putExtra(Intent.EXTRA_STREAM, imageUri)
                flags = Intent.FLAG_GRANT_READ_URI_PERMISSION
            }, null)

            return chooserIntent2


            /*try {
                println("Attempting to open share sheet...")
                context.startActivity(chooserIntent)

            } catch (ex: Exception) {
                println("An exception has occurred, ${ex.localizedMessage}")
            }*/
        }

    }
}