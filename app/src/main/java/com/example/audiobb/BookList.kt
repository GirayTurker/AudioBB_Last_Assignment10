package com.example.audiobb

import java.io.Serializable

class BookList : Serializable {
    private val bookList : MutableList<Book> by lazy {
        ArrayList()
    }

    fun add(book: Book) {
        bookList.add(book)
    }

    operator fun get(index: Int) = bookList[index]

    fun getById(bookId: Int): Book? {
        return bookList.findLast { e->e.id.equals(bookId) }
    }

    fun getListofBooks(): List<Book> {
        return bookList;
    }

    fun size() = bookList.size
}