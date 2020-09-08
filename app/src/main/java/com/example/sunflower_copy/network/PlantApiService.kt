package com.example.sunflower_copy.network

import com.example.sunflower_copy.domain.PlantInformation
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import retrofit2.Call
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import retrofit2.converter.scalars.ScalarsConverterFactory
import retrofit2.http.GET
import retrofit2.http.Query


enum class PlantApiFilter(val value: String) {
    SHOW_RENT("rent"),
    SHOW_BUY("buy"),
    SHOW_ALL("all") }

private const val BASE_URL = "https://android-kotlin-fun-mars-server.appspot.com/"

/**
 * Build the Moshi object that Retrofit will be using, making sure to add the Kotlin adapter for
 * full Kotlin compatibility.
 */
private val moshi = Moshi.Builder()
    .add(KotlinJsonAdapterFactory())
    .build()

/**
 * Use the Retrofit builder to build a retrofit object using a Moshi converter with our Moshi
 * object.
 */
private val retrofit = Retrofit.Builder()
    .addConverterFactory(MoshiConverterFactory.create(moshi))
    .baseUrl(BASE_URL)
    .build()

private val retrofitString = Retrofit.Builder()
    .addConverterFactory(ScalarsConverterFactory.create())
    .baseUrl(BASE_URL)
    .build()

/**
 * A public interface that exposes the [getProperties] method
 */
interface PlantApiService {
    /**
     * Returns a Coroutine [List] of [MarsProperty] which can be fetched in a Coroutine scope.
     * The @GET annotation indicates that the "realestate" endpoint will be requested with the GET
     * HTTP method
     */
    @GET("realestate")
    suspend fun getPlants(@Query("filter") type: String): List<PlantInformation>

    @GET("realestate")
    suspend fun getProperties(): String
}

/**
 * A public Api object that exposes the lazy-initialized Retrofit service
 */
object PlantApi {
    val retrofitService : PlantApiService by lazy {
        retrofit.create(PlantApiService::class.java) }

    val retrofitStringService : PlantApiService by lazy {
        retrofitString.create(PlantApiService::class.java) }
}
