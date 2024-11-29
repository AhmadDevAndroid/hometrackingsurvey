package com.app.householdtracing.network.converter

data class Resource<out T>(
    val status: Status,
    val data: T?,
    val message: String?,
    val throwable: Throwable?
) {

    companion object {
        fun <T> success(data: T?): Resource<T> {
            return Resource(Status.SUCCESS, data, null, null)
        }

        fun <T> error(msg: String, data: T? = null, throwable: Throwable? = null): Resource<T> {
            return Resource(Status.ERROR, data, msg, throwable)
        }

        fun <T> loading(data: T?): Resource<T> {
            return Resource(Status.LOADING, data, null, null)
        }

        fun <T> empty(data: T? = null): Resource<T> {
            return Resource(Status.EMPTY, data, null, null)
        }

    }

}

enum class Status {
    SUCCESS,
    ERROR,
    LOADING,
    EMPTY
}

