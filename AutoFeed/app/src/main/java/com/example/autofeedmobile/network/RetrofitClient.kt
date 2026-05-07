package com.example.autofeedmobile.network

import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.security.SecureRandom
import java.security.cert.X509Certificate
import javax.net.ssl.SSLContext
import javax.net.ssl.TrustManager
import javax.net.ssl.X509TrustManager

object RetrofitClient {
    private var authToken: String? = null

    fun setAuthToken(token: String?) {
        authToken = token
    }

    /**
     * For Emulator: Use "http://10.0.2.2:5207/"
     * For Physical Device (USB + 'adb reverse'): Use "http://127.0.0.1:5207/"
     * For Physical Device (Wi-Fi): Use your machine's IP, e.g., "http://192.168.1.xxx:5207/"
     */
//    const val BASE_URL = "http://127.0.0.1:5207/"
//    const val BASE_URL = "http://10.0.2.2:5207/"
//    const val BASE_URL = "http://192.168.230.30:8080/"
//    const val BASE_URL = "http://10.1.160.222:5207/"
    const val BASE_URL = "http://autofeed-enbrgggphncjd4hd.southeastasia-01.azurewebsites.net/"


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
            builder.addInterceptor(Interceptor { chain ->
                val original = chain.request()
                val requestBuilder = original.newBuilder()
                
                authToken?.let {
                    requestBuilder.header("Authorization", "Bearer $it")
                }
                
                val request = requestBuilder.method(original.method, original.body).build()
                chain.proceed(request)
            })
            builder.sslSocketFactory(sslSocketFactory, trustAllCerts[0] as X509TrustManager)
            builder.hostnameVerifier { _, _ -> true }

            return builder.build()
        } catch (e: Exception) {
            throw RuntimeException(e)
        }
    }
}
