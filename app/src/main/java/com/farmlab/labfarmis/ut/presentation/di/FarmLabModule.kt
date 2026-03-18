package com.farmlab.labfarmis.ut.presentation.di

import com.farmlab.labfarmis.ut.data.repo.FarmLabRepository
import com.farmlab.labfarmis.ut.data.shar.FarmLabSharedPreference
import com.farmlab.labfarmis.ut.data.utils.FarmLabPushToken
import com.farmlab.labfarmis.ut.data.utils.FarmLabSystemService
import com.farmlab.labfarmis.ut.domain.usecases.FarmLabGetAllUseCase
import com.farmlab.labfarmis.ut.presentation.pushhandler.FarmLabPushHandler
import com.farmlab.labfarmis.ut.presentation.ui.load.FarmLabLoadViewModel
import com.farmlab.labfarmis.ut.presentation.ui.view.FarmLabViFun
import org.koin.core.module.dsl.viewModel
import org.koin.dsl.module

val farmLabModule = module {
    factory {
        FarmLabPushHandler()
    }
    single {
        FarmLabRepository()
    }
    single {
        FarmLabSharedPreference(get())
    }
    factory {
        FarmLabPushToken()
    }
    factory {
        FarmLabSystemService(get())
    }
    factory {
        FarmLabGetAllUseCase(
            get(), get(), get()
        )
    }
    factory {
        FarmLabViFun(get())
    }
    viewModel {
        FarmLabLoadViewModel(get(), get(), get())
    }
}