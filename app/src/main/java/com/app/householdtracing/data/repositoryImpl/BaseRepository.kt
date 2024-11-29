package com.app.householdtracing.data.repositoryImpl

import android.system.ErrnoException
import com.app.householdtracing.App
import com.app.householdtracing.R
import com.app.householdtracing.network.converter.Resource
import com.app.householdtracing.util.AppUtil.showLogError
import okhttp3.ResponseBody
import org.json.JSONObject
import retrofit2.HttpException
import java.net.ConnectException
import java.net.SocketException

abstract class BaseRepository {

    suspend fun <T> doApiCall(apiCall: suspend () -> T): Resource<T> {
        return try {
            Resource.success(apiCall.invoke())
        } catch (e: Exception) {
            Resource.error(getErrorMessage(e), throwable = e)
        }
    }

}

fun getErrorMessage(it: Throwable): String {
    showLogError("API Exception", it.message ?: "N/A")
    when (it) {
        is ConnectException, is java.net.UnknownHostException, is ErrnoException -> {
            return App.getInstance().resources.getString(R.string.internet_missing_error)
        }

        is SocketException -> {
            return App.getInstance().resources.getString(R.string.connection_error)
        }

        else -> {
            if (it is HttpException) {
                val response = (it).response()
                if (response?.code() == 401 || response?.code() == 406 || response?.code() == 500) {
                    val obj = JSONObject((response.errorBody() as ResponseBody).string())
                    return when {
                        obj.has("message") -> {
                            obj.getString("message")
                        }

                        obj.has("error") -> {
                            obj.getString("error")
                        }

                        else -> {
                            ""
                        }
                    }
                } else {
                    return App.getInstance().resources.getString(R.string.please_try_later)
                }
            } else {
                return App.getInstance().resources.getString(R.string.please_try_later)
            }
        }
    }
}