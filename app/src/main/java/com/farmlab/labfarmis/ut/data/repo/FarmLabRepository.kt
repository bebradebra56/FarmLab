package com.farmlab.labfarmis.ut.data.repo

import android.util.Log
import com.farmlab.labfarmis.ut.domain.model.FarmLabEntity
import com.farmlab.labfarmis.ut.domain.model.FarmLabParam
import com.farmlab.labfarmis.ut.presentation.app.FarmLabApplication.Companion.FARM_LAB_MAIN_TAG
import com.google.gson.Gson
import com.google.gson.JsonElement
import com.google.gson.JsonObject
import okhttp3.OkHttpClient
import retrofit2.Call
import retrofit2.Retrofit
import retrofit2.awaitResponse
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.create
import retrofit2.http.Body
import retrofit2.http.Headers
import retrofit2.http.POST

interface FarmLabApi {
    @Headers("Content-Type: application/json")
    @POST("config.php")
    fun farmLabGetClient(
        @Body jsonString: JsonObject,
    ): Call<FarmLabEntity>
}


private const val FARM_LAB_MAIN = "https://farrmlab.com/"
class FarmLabRepository {

    suspend fun farmLabGetClient(
        farmLabParam: FarmLabParam,
        farmLabConversion: MutableMap<String, Any>?
    ): FarmLabEntity? {
        val gson = Gson()
        val api = farmLabGetApi(FARM_LAB_MAIN, null)

        val farmLabJsonObject = gson.toJsonTree(farmLabParam).asJsonObject
        farmLabConversion?.forEach { (key, value) ->
            val element: JsonElement = gson.toJsonTree(value)
            farmLabJsonObject.add(key, element)
        }
        return try {
            val farmLabRequest: Call<FarmLabEntity> = api.farmLabGetClient(
                jsonString = farmLabJsonObject,
            )
            val farmLabResult = farmLabRequest.awaitResponse()
            Log.d(FARM_LAB_MAIN_TAG, "Retrofit: Result code: ${farmLabResult.code()}")
            if (farmLabResult.code() == 200) {
                Log.d(FARM_LAB_MAIN_TAG, "Retrofit: Get request success")
                Log.d(FARM_LAB_MAIN_TAG, "Retrofit: Code = ${farmLabResult.code()}")
                Log.d(FARM_LAB_MAIN_TAG, "Retrofit: ${farmLabResult.body()}")
                farmLabResult.body()
            } else {
                null
            }
        } catch (e: java.lang.Exception) {
            Log.d(FARM_LAB_MAIN_TAG, "Retrofit: Get request failed")
            Log.d(FARM_LAB_MAIN_TAG, "Retrofit: ${e.message}")
            null
        }
    }


    private fun farmLabGetApi(url: String, client: OkHttpClient?) : FarmLabApi {
        val retrofit = Retrofit.Builder()
            .baseUrl(url)
            .client(client ?: OkHttpClient.Builder().build())
            .addConverterFactory(GsonConverterFactory.create())
            .build()
        return retrofit.create()
    }


}
