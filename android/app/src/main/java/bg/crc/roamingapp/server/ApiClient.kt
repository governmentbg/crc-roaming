package bg.crc.roamingapp.server

import android.content.Context
import bg.crc.roamingapp.R
import bg.crc.roamingapp.constant.Constants
import bg.crc.roamingapp.debug.MyDebug
import com.google.gson.GsonBuilder
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

/**
 * This object class sets Api client and provide basic structure of api request .
 * There also can set when to show log and how to handle the response.
 * you can also set connection, read and write connection timeout.
 *
 * */

object ApiClient {
    private const val REQUEST_TIMEOUT = Constants.SERVER_REQUEST_TIMEOUT_IN_SECOND

    private var mRetrofit: Retrofit? = null
    private var mOkHttpClient: OkHttpClient? = null

    /**
     * This function provides basic skeleton of the api request, and how to handle it.
     * very first it's check the HttpClient is initialized or not if not then initialize it.[mOkHttpClient]
     * <p>
     * In retrofit object we follow rxjava + gson to handle quick response. so there are two converter factory
     *  sets [RxJava2CallAdapterFactory] and [GsonConverterFactory].
     *  </p>
     *
     * For the retrofit object we set base-url from [Constants.BASE_URL]
     * * */
    fun getClient(context: Context): Retrofit {
        if (mOkHttpClient == null)
            initOkHttp(context)

        val mGsonBuilder = GsonBuilder()
            .setLenient()
            .create()


        if (mRetrofit == null) {
            mRetrofit = Retrofit.Builder()
                .baseUrl(Constants.BASE_URL)
                .client(mOkHttpClient!!)
                .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
                .addConverterFactory(GsonConverterFactory.create(mGsonBuilder))
                .build()
        }
        return mRetrofit!!
    }

    /**
     * function set OkHttpClient into the [mOkHttpClient].
     *
     * This function build HttpClientBuilder with  connection timeout, read timeout and write timeout.
     *
     * There will be set the log interceptor with build. it's removed if [MyDebug.IS_DEBUG] or
     * [MyDebug.IS_SERVER_LOG_ENABLE] not enable.
     *
     * After that it's try to set chaining process for api request. and filly Build that.
     *
     * */
    private fun initOkHttp(context: Context) {
        val httpClient = OkHttpClient().newBuilder()
            .connectTimeout(REQUEST_TIMEOUT.toLong(), TimeUnit.SECONDS)
            .readTimeout(REQUEST_TIMEOUT.toLong(), TimeUnit.SECONDS)
            .writeTimeout(REQUEST_TIMEOUT.toLong(), TimeUnit.SECONDS)

        if (MyDebug.IS_DEBUG && MyDebug.IS_SERVER_LOG_ENABLE) {
            val interceptor = HttpLoggingInterceptor()
            interceptor.level = HttpLoggingInterceptor.Level.BODY
            httpClient.addInterceptor(interceptor)
        }

        try {

            httpClient.addInterceptor { chain ->
                val original = chain.request()
                val requestBuilder = original.newBuilder()
                val request = requestBuilder.build()

                return@addInterceptor chain.proceed(request)

            }
        } catch (e: Exception) {
            MyDebug.showServerLog(
                ApiClient::class.java.simpleName, e.message
                    ?: context.getString(R.string.unknown_error_in_process_chain), MyDebug.LogType.E
            )
        }

        mOkHttpClient = httpClient.build()
    }


}