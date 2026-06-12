package com.example.Consultaitems.utils.cls
import android.graphics.Bitmap
import androidx.collection.LruCache
import android.graphics.BitmapFactory
import android.os.AsyncTask
import android.util.Log
import android.widget.ImageView
import java.io.InputStream
import java.net.HttpURLConnection
import java.net.URL

class ImageCache {
    private val maxMemory = (Runtime.getRuntime().maxMemory() / 1024).toInt()
    private val cacheSize = maxMemory / 8
    private val imageCache = object : LruCache<String, Bitmap>(cacheSize) {
        override fun sizeOf(key: String, bitmap: Bitmap): Int {
            return bitmap.byteCount / 1024
        }
    }

    fun getBitmap(key: String): Bitmap? {
        return imageCache.get(key)
    }

    fun putBitmap(key: String, bitmap: Bitmap) {
        if (getBitmap(key) == null) {
            imageCache.put(key, bitmap)
        }
    }
}




open class DownloadImageTask(
    private val imageView: ImageView,
    private val imageCache: ImageCache,
    private val errorImageResId: Int  // Recurso de imagen de error
) : AsyncTask<String, Void, Bitmap?>() {

    private var url: String? = null

    override fun doInBackground(vararg urls: String): Bitmap? {
        url = urls[0]
        var bitmap: Bitmap? = imageCache.getBitmap(url!!)
        if (bitmap == null) {
            try {
                val connection: HttpURLConnection = URL(url).openConnection() as HttpURLConnection
                connection.doInput = true
                connection.connect()
                val input: InputStream = connection.inputStream
                bitmap = BitmapFactory.decodeStream(input)
                input.close()
                imageCache.putBitmap(url!!, bitmap)
            } catch (e: Exception) {
                Log.e("DownloadImageTask", "Error downloading image: ${e.message}")
                return null
            }
        }
        return bitmap
    }

    override fun onPostExecute(result: Bitmap?) {
        if (result != null) {
            imageView.setImageBitmap(result)
        } else {
            imageView.setImageResource(errorImageResId)  // Usar imagen de error
        }
    }
}


