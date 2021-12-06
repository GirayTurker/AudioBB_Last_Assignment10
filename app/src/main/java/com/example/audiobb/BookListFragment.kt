package com.example.audiobb

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import edu.temple.audlibplayer.PlayerService
import java.io.File


private const val BOOK_LIST= "booklist"


/**
 * A simple [Fragment] subclass as the default destination in the navigation.
 */
class BookListFragment : Fragment() {
    private var bookList: BookList? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        var mainActivity = (activity as BookListFragment.BookSelectedInterface)

        bookList = mainActivity.getBookList()
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_first, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val bookViewModel = ViewModelProvider(requireActivity()).get(CustomViewModel::class.java)

        val onClick : (Book) -> Unit = {
            // Update the ViewModel
            book: Book -> bookViewModel.setBook(book)
            // Inform the activity of the selection so as to not have the event replayed
            // when the activity is restarted
            (activity as BookSelectedInterface).bookSelected()
        }
        with (view as RecyclerView) {
            layoutManager = LinearLayoutManager(requireActivity())
            adapter = BookAdapter (bookList!!, onClick)
        }
    }

    interface BookSelectedInterface {
        fun bookSelected()

        fun getBinder(): PlayerService.MediaControlBinder?

        fun downloadFile(bookId: Int)

        fun getDownloadedFile(bookId: Int): File?

        fun getBookList(): BookList

        fun saveBookList(bookList: BookList)

        fun setBookList()
    }
}