package com.example.audiobb

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.graphics.BitmapFactory
import android.os.*
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.SeekBar
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModelProvider
import java.net.URL
import edu.temple.audlibplayer.PlayerService

/**
 * A simple [Fragment] subclass as the second destination in the navigation.
 */
class BookDetailsFragment : Fragment() {
    lateinit var titleView: TextView
    lateinit var authorView: TextView
    lateinit var imageView: ImageView
    lateinit var playButton: Button
    lateinit var pauseButton: Button
    lateinit var stopButton: Button
    lateinit var progressBar: SeekBar
    var bookId: Int = 0
    var bookDuration: Int = 0
    var progressAlreadyHandled: Boolean = false
    private var playerService: PlayerService.MediaControlBinder? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val policy = StrictMode.ThreadPolicy.Builder().permitAll().build()
        StrictMode.setThreadPolicy(policy)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val layout = inflater.inflate(R.layout.fragment_second, container, false)

        titleView = layout.findViewById(R.id.title)
        authorView = layout.findViewById(R.id.author)
        imageView = layout.findViewById(R.id.imageView)
        playButton = layout.findViewById(R.id.playButton)
        pauseButton = layout.findViewById(R.id.pauseButton)
        stopButton = layout.findViewById(R.id.stopButton)
        progressBar = layout.findViewById(R.id.seekBar)

        var mainActivity = (activity as BookListFragment.BookSelectedInterface)

        var bookList = mainActivity.getBookList()

        playerService = mainActivity.getBinder()

        setPlayerService(mainActivity, bookList)

        progressBar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                if (fromUser) {
                    playerService?.seekTo(progress);
                }
            }

            override fun onStartTrackingTouch(seekBar: SeekBar?) {
            }

            override fun onStopTrackingTouch(seekBar: SeekBar?) {
            }
        })

        playButton.setOnClickListener {
            setPlayerService(mainActivity, bookList)
            var file = mainActivity.getDownloadedFile(bookId)

            var book = bookList.getById(bookId)


            if (file != null && file.exists() && book != null && book.isDownloaded) {
                if (book != null) {
                    playerService?.play(file, book.progress)
                }
                playButton.text = "Playing Downloaded"
            } else {
                playerService?.play(bookId)
                mainActivity.downloadFile(bookId);
                playButton.text = "Playing Stream"
            }
        }

        pauseButton.setOnClickListener {
            setPlayerService(mainActivity, bookList)
            playerService?.pause()
            mainActivity.saveBookList(bookList)
        }

        stopButton.setOnClickListener {
            setPlayerService(mainActivity, bookList)
            playerService?.stop()
            var book = bookList.getById(bookId)
            book?.progress = 0;
            mainActivity.saveBookList(bookList)
        }

        return layout;
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val book = ViewModelProvider(requireActivity()).get(CustomViewModel::class.java)
            .getBook();

        book.observe(requireActivity(), {bookDetails(it)})
    }

    private fun bookDetails(_book: Book) {

        _book.run {
            bookId= id
            bookDuration=duration
            titleView.text = title
            authorView.text = author
            progressBar.max = bookDuration
            progressBar.setProgress(progress)

            var image = BitmapFactory.decodeStream(URL(coverUrl).openConnection().getInputStream());
            imageView.setImageBitmap(image);
        }
    }

    private fun setPlayerService(mainActivity:BookListFragment.BookSelectedInterface,bookList:BookList){
        playerService = mainActivity.getBinder()
        if (!progressAlreadyHandled && playerService != null) {
            playerService?.setProgressHandler(object : Handler(Looper.getMainLooper()) {
                override fun handleMessage(msg: Message) {
                    if (msg != null && msg.obj != null) {
                        var progress = msg.obj as PlayerService.BookProgress
                        progressBar.setProgress(progress.progress)
                        var book = bookList.getById(bookId)
                        book?.progress = progress.progress
                    }
                }
            })
            progressAlreadyHandled = true
        }

    }



}