package com.example.gogieats.util

import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.net.Uri
import android.provider.MediaStore
import androidx.core.app.ShareCompat
import androidx.core.content.FileProvider
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.FileOutputStream
import java.io.OutputStream


class ShareUtils {

    companion object {
        fun saveBitmapAndGetUri(context: Context, bitmap: Bitmap): Uri? {

            val path: String = context.externalCacheDir.toString() + "/123.jpg"
            println(path)
            var out: OutputStream? = null
            val file = File(path)
            try {
                out = FileOutputStream(file)
                bitmap.compress(Bitmap.CompressFormat.JPEG, 50, out)
                out.flush()
                out.close()
                println("this has fired")
            } catch (e: Exception) {
                e.printStackTrace()
                println(e.localizedMessage)
            }
            val fileOut =
                FileProvider.getUriForFile(context, context.packageName + ".util.provider", file)
            return FileProvider.getUriForFile(
                context, context.packageName + ".util.provider", file
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

            val intent = ShareCompat.IntentBuilder(context)
            intent.setText("$text - $address")
            intent.setType("*/*")
            val share = Intent.createChooser(Intent().apply {
                action = Intent.ACTION_SEND
                putExtra(Intent.EXTRA_TEXT, "$text - $address")
            }, null)
            return share


            /*try {
                println("Attempting to open share sheet...")
                context.startActivity(chooserIntent)

            } catch (ex: Exception) {
                println("An exception has occurred, ${ex.localizedMessage}")
            }*/
        }

        fun genericShare(
            context: Context,
            text: String?,
            address: String,
            photoUri: String,
        ): Intent {
            val chooserIntent = Intent.createChooser(Intent().apply {
                action = Intent.ACTION_SEND
                putExtra(Intent.EXTRA_SUBJECT, "$text - $address")
                putExtra(Intent.EXTRA_TEXT, photoUri)
                type = "text/*"
            }, null)
            return chooserIntent
        }

        private fun getImageUri(inContext: Context, inImage: Bitmap): Uri? {
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