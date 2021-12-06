package com.example.audiobb

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class BookAdapter(_bookList:BookList, _clickListener : (Book) -> Unit) : RecyclerView.Adapter<BookAdapter.BookViewHolder>() {
    val bookList = _bookList
    val clickListener = _clickListener

    class BookViewHolder(itemView: View, clickListener : (Book) -> Unit) :  RecyclerView.ViewHolder(itemView){
        val bookTitleView: TextView
        val bookAuthorView: TextView
        lateinit var book: Book

        init {
            bookTitleView = itemView.findViewById(R.id.title)
            bookAuthorView = itemView.findViewById(R.id.author)
            itemView.setOnClickListener {
                clickListener(book)
            }
        }
    }
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BookViewHolder {
        return BookViewHolder(LayoutInflater.from(parent.context).inflate(R.layout.book_item, parent, false), clickListener)
    }

    override fun onBindViewHolder(holder: BookViewHolder, position: Int) {
        holder.bookTitleView.text = bookList[position].title
        holder.bookAuthorView.text = bookList[position].author
        holder.book = bookList[position]
    }

    override fun getItemCount(): Int {
        return bookList.size()
    }
}