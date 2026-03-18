package com.farmlab.labfarmis.ut.presentation.ui.load

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.farmlab.labfarmis.ut.data.shar.FarmLabSharedPreference
import com.farmlab.labfarmis.ut.data.utils.FarmLabSystemService
import com.farmlab.labfarmis.ut.domain.usecases.FarmLabGetAllUseCase
import com.farmlab.labfarmis.ut.presentation.app.FarmLabAppsFlyerState
import com.farmlab.labfarmis.ut.presentation.app.FarmLabApplication
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class FarmLabLoadViewModel(
    private val farmLabGetAllUseCase: FarmLabGetAllUseCase,
    private val farmLabSharedPreference: FarmLabSharedPreference,
    private val farmLabSystemService: FarmLabSystemService
) : ViewModel() {

    private val _farmLabHomeScreenState: MutableStateFlow<FarmLabHomeScreenState> =
        MutableStateFlow(FarmLabHomeScreenState.FarmLabLoading)
    val farmLabHomeScreenState = _farmLabHomeScreenState.asStateFlow()

    private var farmLabGetApps = false


    init {
        viewModelScope.launch {
            when (farmLabSharedPreference.farmLabAppState) {
                0 -> {
                    if (farmLabSystemService.farmLabIsOnline()) {
                        FarmLabApplication.farmLabConversionFlow.collect {
                            when(it) {
                                FarmLabAppsFlyerState.FarmLabDefault -> {}
                                FarmLabAppsFlyerState.FarmLabError -> {
                                    farmLabSharedPreference.farmLabAppState = 2
                                    _farmLabHomeScreenState.value =
                                        FarmLabHomeScreenState.FarmLabError
                                    farmLabGetApps = true
                                }
                                is FarmLabAppsFlyerState.FarmLabSuccess -> {
                                    if (!farmLabGetApps) {
                                        farmLabGetData(it.farmLabData)
                                        farmLabGetApps = true
                                    }
                                }
                            }
                        }
                    } else {
                        _farmLabHomeScreenState.value =
                            FarmLabHomeScreenState.FarmLabNotInternet
                    }
                }
                1 -> {
                    if (farmLabSystemService.farmLabIsOnline()) {
                        if (FarmLabApplication.FARM_LAB_FB_LI != null) {
                            _farmLabHomeScreenState.value =
                                FarmLabHomeScreenState.FarmLabSuccess(
                                    FarmLabApplication.FARM_LAB_FB_LI.toString()
                                )
                        } else if (System.currentTimeMillis() / 1000 > farmLabSharedPreference.farmLabExpired) {
                            Log.d(FarmLabApplication.FARM_LAB_MAIN_TAG, "Current time more then expired, repeat request")
                            FarmLabApplication.farmLabConversionFlow.collect {
                                when(it) {
                                    FarmLabAppsFlyerState.FarmLabDefault -> {}
                                    FarmLabAppsFlyerState.FarmLabError -> {
                                        _farmLabHomeScreenState.value =
                                            FarmLabHomeScreenState.FarmLabSuccess(
                                                farmLabSharedPreference.farmLabSavedUrl
                                            )
                                        farmLabGetApps = true
                                    }
                                    is FarmLabAppsFlyerState.FarmLabSuccess -> {
                                        if (!farmLabGetApps) {
                                            farmLabGetData(it.farmLabData)
                                            farmLabGetApps = true
                                        }
                                    }
                                }
                            }
                        } else {
                            Log.d(FarmLabApplication.FARM_LAB_MAIN_TAG, "Current time less then expired, use saved url")
                            _farmLabHomeScreenState.value =
                                FarmLabHomeScreenState.FarmLabSuccess(
                                    farmLabSharedPreference.farmLabSavedUrl
                                )
                        }
                    } else {
                        _farmLabHomeScreenState.value =
                            FarmLabHomeScreenState.FarmLabNotInternet
                    }
                }
                2 -> {
                    _farmLabHomeScreenState.value =
                        FarmLabHomeScreenState.FarmLabError
                }
            }
        }
    }


    private suspend fun farmLabGetData(conversation: MutableMap<String, Any>?) {
        val farmLabData = farmLabGetAllUseCase.invoke(conversation)
        if (farmLabSharedPreference.farmLabAppState == 0) {
            if (farmLabData == null) {
                farmLabSharedPreference.farmLabAppState = 2
                _farmLabHomeScreenState.value =
                    FarmLabHomeScreenState.FarmLabError
            } else {
                farmLabSharedPreference.farmLabAppState = 1
                farmLabSharedPreference.apply {
                    farmLabExpired = farmLabData.farmLabExpires
                    farmLabSavedUrl = farmLabData.farmLabUrl
                }
                _farmLabHomeScreenState.value =
                    FarmLabHomeScreenState.FarmLabSuccess(farmLabData.farmLabUrl)
            }
        } else  {
            if (farmLabData == null) {
                _farmLabHomeScreenState.value =
                    FarmLabHomeScreenState.FarmLabSuccess(
                        farmLabSharedPreference.farmLabSavedUrl
                    )
            } else {
                farmLabSharedPreference.apply {
                    farmLabExpired = farmLabData.farmLabExpires
                    farmLabSavedUrl = farmLabData.farmLabUrl
                }
                _farmLabHomeScreenState.value =
                    FarmLabHomeScreenState.FarmLabSuccess(farmLabData.farmLabUrl)
            }
        }
    }


    sealed class FarmLabHomeScreenState {
        data object FarmLabLoading : FarmLabHomeScreenState()
        data object FarmLabError : FarmLabHomeScreenState()
        data class FarmLabSuccess(val data: String) : FarmLabHomeScreenState()
        data object FarmLabNotInternet: FarmLabHomeScreenState()
    }
}