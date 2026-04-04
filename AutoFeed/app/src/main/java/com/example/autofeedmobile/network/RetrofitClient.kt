package com.example.autofeedmobile.network

import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.security.SecureRandom
import java.security.cert.X509Certificate
import javax.net.ssl.SSLContext
import javax.net.ssl.TrustManager
import javax.net.ssl.X509TrustManager

object RetrofitClient {
    /**
     * For Emulator: Use "http://10.0.2.2:5123/"
     * For Physical Device (with 'adb reverse tcp:5123 tcp:5123'): Use "http://localhost:5123/"
     * For Physical Device (Wi-Fi): Use your machine's IP, e.g., "http://192.168.1.235:5123/"
     */
    const val BASE_URL = "https://10.0.2.2:7132/"

    fun getFullUrl(url: String?): String? {
        if (url.isNullOrEmpty()) return null
        val result = if (url.startsWith("http")) {
            url
        } else {
            val cleanUrl = if (url.startsWith("/")) url.substring(1) else url
            "$BASE_URL$cleanUrl"
        }
        android.util.Log.d("RetrofitClient", "Normalizing URL: $url -> $result")
        return result
    }

    val instance: ApiService by lazy {
        val retrofit = Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(getUnsafeOkHttpClient())
            .addConverterFactory(GsonConverterFactory.create())
            .build()

        retrofit.create(ApiService::class.java)
    }

    fun getUnsafeOkHttpClient(): OkHttpClient {
        try {
            val trustAllCerts = arrayOf<TrustManager>(object : X509TrustManager {
                override fun checkClientTrusted(chain: Array<out X509Certificate>?, authType: String?) {}
                override fun checkServerTrusted(chain: Array<out X509Certificate>?, authType: String?) {}
                override fun getAcceptedIssuers(): Array<X509Certificate> = arrayOf()
            })

            val sslContext = SSLContext.getInstance("SSL")
            sslContext.init(null, trustAllCerts, SecureRandom())
            val sslSocketFactory = sslContext.socketFactory

            val builder = OkHttpClient.Builder()
            builder.sslSocketFactory(sslSocketFactory, trustAllCerts[0] as X509TrustManager)
            builder.hostnameVerifier { _, _ -> true }

            return builder.build()
        } catch (e: Exception) {
            throw RuntimeException(e)
        }
    }
}
