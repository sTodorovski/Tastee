package com.example.tastee.network

import android.annotation.SuppressLint
import android.content.Context
import com.example.tastee.api.AuthApi
import com.example.tastee.api.DishApi
import com.example.tastee.api.OrderApi
import com.example.tastee.api.RestaurantApi
import com.example.tastee.api.UserApi
import okhttp3.Cookie
import okhttp3.CookieJar
import okhttp3.HttpUrl
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

@SuppressLint("StaticFieldLeak")
object RetrofitClient {
    private const val BASE_URL = "http://10.0.2.2:8080/"
    private const val PREF_NAME = "cookie_prefs"
    private var appContext: Context? = null

    fun init(context: Context) {
        appContext = context.applicationContext
    }

    private val cookieJar = object : CookieJar {
        private val prefs by lazy {
            val context = appContext ?: throw IllegalStateException("RetrofitClient.init(context) must be called in your Application or starting Activity onCreate()")
            context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
        }

        override fun saveFromResponse(url: HttpUrl, cookies: List<Cookie>) {
            try {
                val host = url.host
                val editor = prefs.edit()
                val currentTime = System.currentTimeMillis()
                for (cookie in cookies) {
                    if (cookie.expiresAt > currentTime) {
                        val key = "${host}_${cookie.name}"
                        editor.putString(key, cookie.toString())
                    }
                }
                editor.apply()
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }

        override fun loadForRequest(url: HttpUrl): List<Cookie> {
            return try {
                val host = url.host
                val cookies = mutableListOf<Cookie>()
                val currentTime = System.currentTimeMillis()
                val allEntries = prefs.all
                for ((key, value) in allEntries) {
                    if (key.startsWith("${host}_") && value is String) {
                        val cookie = Cookie.parse(url, value)
                        if (cookie != null && cookie.expiresAt > currentTime) {
                            cookies.add(cookie)
                        }
                    }
                }
                cookies
            } catch (e: Exception) {
                emptyList()
            }
        }
    }

    private val client by lazy {
        OkHttpClient.Builder()
            .cookieJar(cookieJar)
            .build()
    }

    private val retrofit: Retrofit by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(client)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }

    val authApi: AuthApi by lazy { retrofit.create(AuthApi::class.java) }
    val restaurantApi: RestaurantApi by lazy { retrofit.create(RestaurantApi::class.java) }
    val orderApi: OrderApi by lazy { retrofit.create(OrderApi::class.java) }
    val userApi: UserApi by lazy { retrofit.create(UserApi::class.java) }
    val dishApi: DishApi by lazy { retrofit.create(DishApi::class.java) }
}