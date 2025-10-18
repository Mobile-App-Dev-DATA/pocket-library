package com.example.pocket_library

import android.Manifest
import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import androidx.annotation.RequiresPermission
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.net.HttpURLConnection
import java.net.URL
import android.util.Log

object APIImpl : API {
    private lateinit var appContext: Context

    fun initialize(context: Context) {
        appContext = context.applicationContext
    }

    // internet check
    @RequiresPermission(Manifest.permission.ACCESS_NETWORK_STATE)
    override fun isConnectedToInternet(): Boolean {
        val cm = appContext.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val network = cm.activeNetwork ?: return false
        val capabilities = cm.getNetworkCapabilities(network) ?: return false
        return capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
    }

    // book search with open library api
    override suspend fun getBooksMatchingSearch(
        title: String,
        author: String,
        year: Year_type
    ): List<Book> = withContext(Dispatchers.IO) {
        if (!isConnectedToInternet()) throw NoInternetException("No internet connection", null)

        // Build query (combine title, author, year)
        val query = listOf(title, author, year)
            .filter { it.isNotBlank() }
            .joinToString("+") { it.trim().replace(" ", "+") }

        // Open Library search endpoint
        val urlString =
            "https://openlibrary.org/search.json?q=$query&fields=key,title,author_name,first_publish_year,cover_i&limit=20"
        Log.d("APIImpl", "Starting search for query: $query")

        val url = URL(urlString)
        val connection = url.openConnection() as HttpURLConnection
        connection.connectTimeout = 5000
        connection.readTimeout = 5000
        connection.requestMethod = "GET"

        return@withContext try {
            connection.connect()

            if (connection.responseCode != HttpURLConnection.HTTP_OK)
                throw Exception("HTTP error: ${connection.responseCode}")

            val response = connection.inputStream.bufferedReader().use { it.readText() }
            val json = JSONObject(response)
            val docs = json.getJSONArray("docs")

            val books = mutableListOf<Book>()
            for (i in 0 until docs.length()) {
                val obj = docs.getJSONObject(i)

                val titleStr = obj.optString("title", "Unknown Title")
                val authorStr = obj.optJSONArray("author_name")?.optString(0) ?: "Unknown Author"
                val yearStr = obj.optInt("first_publish_year", 0).toString()
                val coverId = obj.optInt("cover_i", -1)

                val coverUrl = if (coverId != -1)
                    "https://covers.openlibrary.org/b/id/${coverId}-M.jpg"
                else null

                books.add(
                    Book(
                        id = obj.optString("key", ""),
                        title = titleStr,
                        author = authorStr,
                        year = yearStr,
                        coverUrl = coverUrl,
                        myPicture = null
                    )
                )
            }

            Log.d("APIImpl", "Found ${books.size} books")
            books

        } catch (e: Exception) {
            Log.e("APIImpl", "Error fetching books: ${e.message}", e)
            emptyList<Book>()
        } finally {
            connection.disconnect()
        }
    }

    // TODO
    override fun setFavouritesLocal(newFav: List<Book>) {}
    override fun getFavouritesLocal(): List<Book> = emptyList()
    override fun setFavouritesInternet(newFav: List<Book>) { throw NoInternetException("Not yet implemented", null) }
    override fun getFavouritesInternet(): List<Book> { throw NoInternetException("Not yet implemented", null) }
    override fun takePhoto(): Img_type { throw NotImplementedError("Camera not implemented yet") }
    override fun shareBook(book: Book, contact: Contact) { throw NotImplementedError("Share not implemented yet") }
}