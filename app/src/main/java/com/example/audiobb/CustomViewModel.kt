package com.example.audiobb

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class CustomViewModel : ViewModel() {
    private val book: MutableLiveData<Book> by lazy {
        MutableLiveData()
    }

    fun getBook(): LiveData<Book> {
        return book
    }

    fun setBook(_book: Book) {
        this.book.value = _book
    }
}