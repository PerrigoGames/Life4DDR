package com.perrigogames.life4.util

sealed class ViewState<out V, out E> {
    object Loading : ViewState<Nothing, Nothing>()
    data class Error<E>(val error: E) : ViewState<Nothing, E>()
    data class Success<V>(val data: V) : ViewState<V, Nothing>()
}