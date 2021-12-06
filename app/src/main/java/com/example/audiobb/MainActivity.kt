package com.example.audiobb

import android.app.DownloadManager
import android.content.*
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.os.IBinder
import android.util.JsonReader
import android.util.Log
import android.widget.SearchView
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.FragmentContainerView
import androidx.lifecycle.ViewModelProvider
import com.android.volley.Request
import com.android.volley.RequestQueue
import com.android.volley.toolbox.JsonObjectRequest
import com.android.volley.toolbox.Volley
import com.downloader.Error
import com.downloader.OnDownloadListener
import com.downloader.PRDownloader
import com.downloader.PRDownloaderConfig
import edu.temple.audlibplayer.PlayerService
import edu.temple.audlibplayer.PlayerService.MediaControlBinder
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import org.json.JSONException
import java.io.*
import java.lang.Exception


class MainActivity : AppCompatActivity(), BookListFragment.BookSelectedInterface {


    val isSmall : Boolean by lazy {
        findViewById<FragmentContainerView>(R.id.fragment2) == null
    }

    val customViewModel : CustomViewModel by lazy {
        ViewModelProvider(this).get(CustomViewModel::class.java)
    }

    val volleyQueue : RequestQueue by lazy {
        Volley.newRequestQueue(this)
    }

    var playerService: MediaControlBinder? = null

    private lateinit var connection: ServiceConnection
    private lateinit var bookList: BookList



    override fun onCreate(savedInstanceState: Bundle?) {
        setBookList()
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        setTitle("AudioBB")



        val config = PRDownloaderConfig.newBuilder()
            .setReadTimeout(30000)
            .setConnectTimeout(30000)
            .build()
        PRDownloader.initialize(applicationContext, config)

        connection = object : ServiceConnection {
            override fun onServiceDisconnected(componentName: ComponentName) {
                playerService = null
            }

            override fun onServiceConnected(componentName: ComponentName
                                            , service: IBinder
            ) {
                playerService = (service as MediaControlBinder)
            }
        }

        Intent(this, PlayerService::class.java).also { intent ->
            bindService(intent, connection, Context.BIND_AUTO_CREATE)
        }

        var searchButton = findViewById<SearchView>(R.id.search_button)

        searchButton.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String): Boolean {
                val url = "https://kamorris.com/lab/cis3515/book.php?id=" + query

                volleyQueue.add (
                    JsonObjectRequest(
                        Request.Method.GET
                        , url
                        , null
                        , {
                            Log.d("Response", it.toString())
                            try {
                                var book = Book(it.getString("title"), it.getString("author"),
                                    it.getInt("id"), it.getString("cover_url"), it.getInt("duration"), 0, false)
                                bookList.add(book);

                                saveBookList(bookList)

                                supportFragmentManager.beginTransaction()
                                    .replace(R.id.fragment, BookListFragment())
                                    .commit()
                            } catch (e : JSONException) {
                                e.printStackTrace()
                            }
                        }
                        , {
                            // NOOP
                        })
                )

                return true
            }

            override fun onQueryTextChange(newText: String?): Boolean {
                return true
            }
        })

        if (supportFragmentManager.findFragmentById(R.id.fragment) is BookDetailsFragment) {
            supportFragmentManager.popBackStack()
        }

        if (savedInstanceState == null) {
            supportFragmentManager.beginTransaction()
                .add(R.id.fragment, BookListFragment())
                .commit()
        } else
            if (isSmall && customViewModel.getBook().value != null) {
                supportFragmentManager.beginTransaction()
                    .replace(R.id.fragment, BookDetailsFragment())
                    .setReorderingAllowed(true)
                    .addToBackStack(null)
                    .commit()
            }

        // If we have two containers but no BookDetailsFragment, add one to container2
        if (!isSmall && supportFragmentManager.findFragmentById(R.id.fragment2) !is BookDetailsFragment)
            supportFragmentManager.beginTransaction()
                .add(R.id.fragment2, BookDetailsFragment())
                .commit()
    }

    override fun setBookList() {

        try {
            var bookListFile = File(filesDir, "booklist.txt")

            var file = FileInputStream(bookListFile)
            var out = ObjectInputStream(file)

            bookList = out.readObject() as BookList
        } catch (notFound: Exception) {
            bookList = BookList()
        }
    }

    override fun getBookList(): BookList {
        return bookList
    }

    override fun saveBookList(bookList: BookList) {

        var newFile = File(filesDir,"booklist.txt")

        if (!newFile.exists()) {
            newFile.createNewFile()
        }

        var file = FileOutputStream(newFile)
        var out = ObjectOutputStream(file)

        out.writeObject(bookList)
        out.flush()

        out.close()

        setBookList()
    }

    override fun bookSelected() {
        if (isSmall) {
            supportFragmentManager.beginTransaction().replace(R.id.fragment, BookDetailsFragment()).addToBackStack(null).commit()
        }
    }

    override fun getBinder(): MediaControlBinder? {
        return playerService;
    }

    override fun downloadFile(bookId: Int) {
        PRDownloader.download(
            "https://kamorris.com/lab/audlib/download.php?id="+bookId,
            baseContext.filesDir.absolutePath,
            bookId.toString() + ".mp3")
            .build()
            .start(object : OnDownloadListener {
                override fun onDownloadComplete() {
                    var book = bookList.getById(bookId)
                    if (book != null) {
                        book.isDownloaded = true
                    };
                    saveBookList(bookList)
                    Log.d("Response", "Download Completed")
                }

                override fun onError(error: Error?) {
                    Log.d("Response", error.toString())
                }
            })
    }

    override fun getDownloadedFile(bookId: Int): File? {
        try {
            return File(filesDir, bookId.toString() + ".mp3")
        } catch (exception: Exception) {
            return null;
        }
    }
}