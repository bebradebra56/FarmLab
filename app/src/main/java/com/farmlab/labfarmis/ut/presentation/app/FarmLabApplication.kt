package com.farmlab.labfarmis.ut.presentation.app

import android.app.Application
import android.util.Log
import android.view.WindowManager
import com.appsflyer.AppsFlyerConversionListener
import com.appsflyer.AppsFlyerLib
import com.appsflyer.attribution.AppsFlyerRequestListener
import com.appsflyer.deeplink.DeepLink
import com.appsflyer.deeplink.DeepLinkListener
import com.appsflyer.deeplink.DeepLinkResult
import com.farmlab.labfarmis.data.FarmRepository
import com.farmlab.labfarmis.data.db.FarmDatabase
import com.farmlab.labfarmis.ut.presentation.di.farmLabModule
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
import okhttp3.OkHttpClient
import org.koin.android.ext.koin.androidContext
import org.koin.android.ext.koin.androidLogger
import org.koin.core.context.startKoin
import org.koin.core.logger.Level
import retrofit2.Call
import retrofit2.Retrofit
import retrofit2.awaitResponse
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.create
import retrofit2.http.GET
import retrofit2.http.Headers
import retrofit2.http.Query


sealed interface FarmLabAppsFlyerState {
    data object FarmLabDefault : FarmLabAppsFlyerState
    data class FarmLabSuccess(val farmLabData: MutableMap<String, Any>?) :
        FarmLabAppsFlyerState

    data object FarmLabError : FarmLabAppsFlyerState
}

interface FarmLabAppsApi {
    @Headers("Content-Type: application/json")
    @GET(FARM_LAB_LIN)
    fun farmLabGetClient(
        @Query("devkey") devkey: String,
        @Query("device_id") deviceId: String,
    ): Call<MutableMap<String, Any>?>
}

private const val FARM_LAB_APP_DEV = "sJezbNnX5ir4J5UTNH8S8Y"
private const val FARM_LAB_LIN = "com.farmlab.labfarmis"

class FarmLabApplication : Application() {

    val database: FarmDatabase by lazy { FarmDatabase.getInstance(this) }
    val repository: FarmRepository by lazy { FarmRepository(database) }

    private var farmLabIsResumed = false
    ///////
    private var farmLabConversionTimeoutJob: Job? = null
    private var farmLabDeepLinkData: MutableMap<String, Any>? = null

    override fun onCreate() {
        super.onCreate()

        val appsflyer = AppsFlyerLib.getInstance()
        farmLabSetDebufLogger(appsflyer)
        farmLabMinTimeBetween(appsflyer)

        AppsFlyerLib.getInstance().subscribeForDeepLink(object : DeepLinkListener {
            override fun onDeepLinking(p0: DeepLinkResult) {
                when (p0.status) {
                    DeepLinkResult.Status.FOUND -> {
                        farmLabExtractDeepMap(p0.deepLink)
                        Log.d(FARM_LAB_MAIN_TAG, "onDeepLinking found: ${p0.deepLink}")

                    }

                    DeepLinkResult.Status.NOT_FOUND -> {
                        Log.d(FARM_LAB_MAIN_TAG, "onDeepLinking not found: ${p0.deepLink}")
                    }

                    DeepLinkResult.Status.ERROR -> {
                        Log.d(FARM_LAB_MAIN_TAG, "onDeepLinking error: ${p0.error}")
                    }
                }
            }

        })


        appsflyer.init(
            FARM_LAB_APP_DEV,
            object : AppsFlyerConversionListener {
                override fun onConversionDataSuccess(p0: MutableMap<String, Any>?) {
                    //////////
                    farmLabConversionTimeoutJob?.cancel()
                    Log.d(FARM_LAB_MAIN_TAG, "onConversionDataSuccess: $p0")

                    val afStatus = p0?.get("af_status")?.toString() ?: "null"
                    if (afStatus == "Organic") {
                        CoroutineScope(Dispatchers.IO).launch {
                            try {
                                delay(5000)
                                val api = farmLabGetApi(
                                    "https://gcdsdk.appsflyer.com/install_data/v4.0/",
                                    null
                                )
                                val response = api.farmLabGetClient(
                                    devkey = FARM_LAB_APP_DEV,
                                    deviceId = farmLabGetAppsflyerId()
                                ).awaitResponse()

                                val resp = response.body()
                                Log.d(FARM_LAB_MAIN_TAG, "After 5s: $resp")
                                if (resp?.get("af_status") == "Organic" || resp?.get("af_status") == null) {
                                    farmLabResume(
                                        FarmLabAppsFlyerState.FarmLabError
                                    )
                                } else {
                                    farmLabResume(
                                        FarmLabAppsFlyerState.FarmLabSuccess(
                                            resp
                                        )
                                    )
                                }
                            } catch (d: Exception) {
                                Log.d(FARM_LAB_MAIN_TAG, "Error: ${d.message}")
                                farmLabResume(FarmLabAppsFlyerState.FarmLabError)
                            }
                        }
                    } else {
                        farmLabResume(
                            FarmLabAppsFlyerState.FarmLabSuccess(
                                p0
                            )
                        )
                    }
                }

                override fun onConversionDataFail(p0: String?) {
                    /////////
                    farmLabConversionTimeoutJob?.cancel()
                    Log.d(FARM_LAB_MAIN_TAG, "onConversionDataFail: $p0")
                    farmLabResume(FarmLabAppsFlyerState.FarmLabError)
                }

                override fun onAppOpenAttribution(p0: MutableMap<String, String>?) {
                    Log.d(FARM_LAB_MAIN_TAG, "onAppOpenAttribution")
                }

                override fun onAttributionFailure(p0: String?) {
                    Log.d(FARM_LAB_MAIN_TAG, "onAttributionFailure: $p0")
                }
            },
            this
        )

        appsflyer.start(this, FARM_LAB_APP_DEV, object :
            AppsFlyerRequestListener {
            override fun onSuccess() {
                Log.d(FARM_LAB_MAIN_TAG, "AppsFlyer started")
            }

            override fun onError(p0: Int, p1: String) {
                Log.d(FARM_LAB_MAIN_TAG, "AppsFlyer start error: $p0 - $p1")
            }
        })
        ///////////
        farmLabStartConversionTimeout()
        startKoin {
            androidLogger(Level.DEBUG)
            androidContext(this@FarmLabApplication)
            modules(
                listOf(
                    farmLabModule
                )
            )
        }
    }

    private fun farmLabExtractDeepMap(dl: DeepLink) {
        val map = mutableMapOf<String, Any>()
        dl.deepLinkValue?.let { map["deep_link_value"] = it }
        dl.mediaSource?.let { map["media_source"] = it }
        dl.campaign?.let { map["campaign"] = it }
        dl.campaignId?.let { map["campaign_id"] = it }
        dl.afSub1?.let { map["af_sub1"] = it }
        dl.afSub2?.let { map["af_sub2"] = it }
        dl.afSub3?.let { map["af_sub3"] = it }
        dl.afSub4?.let { map["af_sub4"] = it }
        dl.afSub5?.let { map["af_sub5"] = it }
        dl.matchType?.let { map["match_type"] = it }
        dl.clickHttpReferrer?.let { map["click_http_referrer"] = it }
        dl.getStringValue("timestamp")?.let { map["timestamp"] = it }
        dl.isDeferred?.let { map["is_deferred"] = it }
        for (i in 1..10) {
            val key = "deep_link_sub$i"
            dl.getStringValue(key)?.let {
                if (!map.containsKey(key)) {
                    map[key] = it
                }
            }
        }
        Log.d(FARM_LAB_MAIN_TAG, "Extracted DeepLink data: $map")
        farmLabDeepLinkData = map
    }
    /////////////////

    private fun farmLabStartConversionTimeout() {
        farmLabConversionTimeoutJob = CoroutineScope(Dispatchers.Main).launch {
            delay(30000)
            if (!farmLabIsResumed) {
                Log.d(FARM_LAB_MAIN_TAG, "TIMEOUT: No conversion data received in 30s")
                farmLabResume(FarmLabAppsFlyerState.FarmLabError)
            }
        }
    }

    private fun farmLabResume(state: FarmLabAppsFlyerState) {
        ////////////
        farmLabConversionTimeoutJob?.cancel()
        if (state is FarmLabAppsFlyerState.FarmLabSuccess) {
            val convData = state.farmLabData ?: mutableMapOf()
            val deepData = farmLabDeepLinkData ?: mutableMapOf()
            val merged = mutableMapOf<String, Any>().apply {
                putAll(convData)
                for ((key, value) in deepData) {
                    if (!containsKey(key)) {
                        put(key, value)
                    }
                }
            }
            if (!farmLabIsResumed) {
                farmLabIsResumed = true
                farmLabConversionFlow.value =
                    FarmLabAppsFlyerState.FarmLabSuccess(merged)
            }
        } else {
            if (!farmLabIsResumed) {
                farmLabIsResumed = true
                farmLabConversionFlow.value = state
            }
        }
    }

    private fun farmLabGetAppsflyerId(): String {
        val appsflyrid = AppsFlyerLib.getInstance().getAppsFlyerUID(this) ?: ""
        Log.d(FARM_LAB_MAIN_TAG, "AppsFlyer: AppsFlyer Id = $appsflyrid")
        return appsflyrid
    }

    private fun farmLabSetDebufLogger(appsflyer: AppsFlyerLib) {
        appsflyer.setDebugLog(true)
    }

    private fun farmLabMinTimeBetween(appsflyer: AppsFlyerLib) {
        appsflyer.setMinTimeBetweenSessions(0)
    }

    private fun farmLabGetApi(url: String, client: OkHttpClient?): FarmLabAppsApi {
        val retrofit = Retrofit.Builder()
            .baseUrl(url)
            .client(client ?: OkHttpClient.Builder().build())
            .addConverterFactory(GsonConverterFactory.create())
            .build()
        return retrofit.create()
    }

    companion object {
        var farmLabInputMode = WindowManager.LayoutParams.SOFT_INPUT_ADJUST_PAN
        val farmLabConversionFlow: MutableStateFlow<FarmLabAppsFlyerState> = MutableStateFlow(
            FarmLabAppsFlyerState.FarmLabDefault
        )
        var FARM_LAB_FB_LI: String? = null
        const val FARM_LAB_MAIN_TAG = "FarmLabMainTag"
    }
}