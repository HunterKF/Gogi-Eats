package com.example.kbbqreview.util

import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.net.Uri
import android.provider.MediaStore
import androidx.core.content.FileProvider
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.FileOutputStream
import java.io.OutputStream
import java.net.URI


class ShareUtils {

    companion object {
        fun saveBitmapAndGetUri(context: Context, bitmap: Bitmap): URI {
            println("Starting save bitmap")
            val path: String = context.externalCacheDir.toString() + "/1234.jpg"
            File.createTempFile("", context.cacheDir.toString())
            var out: OutputStream? = null
            val file = File(path)
            try {
                out = FileOutputStream(file)
                bitmap.compress(Bitmap.CompressFormat.JPEG, 100, out)
                out.flush()
                out.close()
                println("It's now over here.")
            } catch (e: Exception) {
                println("It has died here.... ${e.localizedMessage}")
                e.printStackTrace()
            }
            val file2 = try {
                val outputFile = File(context.externalCacheDir.toString() + "/1234.jpg", "Title.png")
                val outPutStream = FileOutputStream(outputFile)
                bitmap.compress(Bitmap.CompressFormat.PNG, 100, outPutStream)
                outPutStream.flush()
                outPutStream.close()
                outputFile
            } catch (e: Throwable) {

            }
            val uri = file2
            val shareIntent = Intent().apply {
                action = Intent.ACTION_SEND
                type = "image/png"
                putExtra(Intent.EXTRA_STREAM, uri)
            }
//            startActivity(Intent.createChooser(shareIntent, title))
            println("Returning")
            return uri
        }

        fun shareImageToOthers(context: Context, text: String?, address: String, bitmap: Bitmap?): Intent {
            println("Starting the function...")
//            val imageUri: Uri? = bitmap?.let { saveBitmapAndGetUri(context, it) }
            val imageUri2: Uri? = bitmap?.let { getImageUri(context, it) }
            println("1")
            val chooserIntent = Intent.createChooser(Intent().apply {
                action = Intent.ACTION_SEND
                putExtra(Intent.EXTRA_SUBJECT, text)
                putExtra(Intent.EXTRA_TEXT, address)
                putExtra(Intent.EXTRA_TITLE, "Hello")
                type = "text/plain"
//                data = imageUri2
                flags = Intent.FLAG_GRANT_READ_URI_PERMISSION
            }, null)
            println("2")

//            chooserIntent.type = "image/*"
//            println("3")
//            chooserIntent.putExtra(Intent.EXTRA_TEXT, text)
//            println("4")
//            chooserIntent.putExtra(Intent.EXTRA_STREAM, imageUri2)
//            println("5")
//            chooserIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            return chooserIntent
            /*try {
                println("Attempting to open share sheet...")
                context.startActivity(chooserIntent)

            } catch (ex: Exception) {
                println("An exception has occurred, ${ex.localizedMessage}")
            }*/
        }

        fun getImageUri(inContext: Context, inImage: Bitmap): Uri? {
            val bytes = ByteArrayOutputStream()
            inImage.compress(Bitmap.CompressFormat.JPEG, 100, bytes)
            val path: String =
                MediaStore.Images.Media.insertImage(inContext.contentResolver,
                    inImage,
                    "Title",
                    null)
            return Uri.parse(path)
        }
    }
}