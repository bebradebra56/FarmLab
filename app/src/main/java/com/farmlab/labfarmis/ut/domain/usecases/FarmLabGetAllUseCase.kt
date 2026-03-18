package com.farmlab.labfarmis.ut.domain.usecases

import android.util.Log
import com.farmlab.labfarmis.ut.data.repo.FarmLabRepository
import com.farmlab.labfarmis.ut.data.utils.FarmLabPushToken
import com.farmlab.labfarmis.ut.data.utils.FarmLabSystemService
import com.farmlab.labfarmis.ut.domain.model.FarmLabEntity
import com.farmlab.labfarmis.ut.domain.model.FarmLabParam
import com.farmlab.labfarmis.ut.presentation.app.FarmLabApplication

class FarmLabGetAllUseCase(
    private val farmLabRepository: FarmLabRepository,
    private val farmLabSystemService: FarmLabSystemService,
    private val farmLabPushToken: FarmLabPushToken,
) {
    suspend operator fun invoke(conversion: MutableMap<String, Any>?) : FarmLabEntity?{
        val params = FarmLabParam(
            farmLabLocale = farmLabSystemService.farmLabGetLocale(),
            farmLabPushToken = farmLabPushToken.farmLabGetToken(),
            farmLabAfId = farmLabSystemService.farmLabGetAppsflyerId()
        )
        Log.d(FarmLabApplication.FARM_LAB_MAIN_TAG, "Params for request: $params")
        return farmLabRepository.farmLabGetClient(params, conversion)
    }



}