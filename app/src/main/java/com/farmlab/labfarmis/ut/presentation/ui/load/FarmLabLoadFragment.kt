package com.farmlab.labfarmis.ut.presentation.ui.load

import android.Manifest
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.view.View
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import com.farmlab.labfarmis.MainActivity
import com.farmlab.labfarmis.R
import com.farmlab.labfarmis.databinding.FragmentLoadFarmLabBinding
import com.farmlab.labfarmis.ut.data.shar.FarmLabSharedPreference
import kotlinx.coroutines.launch
import org.koin.android.ext.android.inject
import org.koin.androidx.viewmodel.ext.android.viewModel


class FarmLabLoadFragment : Fragment(R.layout.fragment_load_farm_lab) {
    private lateinit var farmLabLoadBinding: FragmentLoadFarmLabBinding

    private val farmLabLoadViewModel by viewModel<FarmLabLoadViewModel>()

    private val farmLabSharedPreference by inject<FarmLabSharedPreference>()

    private var farmLabUrl = ""

    private val farmLabRequestNotificationPermission = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        farmLabSharedPreference.farmLabNotificationState = 2
        farmLabNavigateToSuccess(farmLabUrl)
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        farmLabLoadBinding = FragmentLoadFarmLabBinding.bind(view)

        farmLabLoadBinding.farmLabGrandButton.setOnClickListener {
            val farmLabPermission = Manifest.permission.POST_NOTIFICATIONS
            farmLabRequestNotificationPermission.launch(farmLabPermission)
        }

        farmLabLoadBinding.farmLabSkipButton.setOnClickListener {
            farmLabSharedPreference.farmLabNotificationState = 1
            farmLabSharedPreference.farmLabNotificationRequest =
                (System.currentTimeMillis() / 1000) + 259200
            farmLabNavigateToSuccess(farmLabUrl)
        }

        lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                farmLabLoadViewModel.farmLabHomeScreenState.collect {
                    when (it) {
                        is FarmLabLoadViewModel.FarmLabHomeScreenState.FarmLabLoading -> {

                        }

                        is FarmLabLoadViewModel.FarmLabHomeScreenState.FarmLabError -> {
                            requireActivity().startActivity(
                                Intent(
                                    requireContext(),
                                    MainActivity::class.java
                                )
                            )
                            requireActivity().finish()
                        }

                        is FarmLabLoadViewModel.FarmLabHomeScreenState.FarmLabSuccess -> {
                            if (Build.VERSION.SDK_INT > Build.VERSION_CODES.S_V2) {
                                val farmLabNotificationState = farmLabSharedPreference.farmLabNotificationState
                                when (farmLabNotificationState) {
                                    0 -> {
                                        farmLabLoadBinding.farmLabNotiGroup.visibility = View.VISIBLE
                                        farmLabLoadBinding.farmLabLoadingGroup.visibility = View.GONE
                                        farmLabUrl = it.data
                                    }
                                    1 -> {
                                        if (System.currentTimeMillis() / 1000 > farmLabSharedPreference.farmLabNotificationRequest) {
                                            farmLabLoadBinding.farmLabNotiGroup.visibility = View.VISIBLE
                                            farmLabLoadBinding.farmLabLoadingGroup.visibility = View.GONE
                                            farmLabUrl = it.data
                                        } else {
                                            farmLabNavigateToSuccess(it.data)
                                        }
                                    }
                                    2 -> {
                                        farmLabNavigateToSuccess(it.data)
                                    }
                                }
                            } else {
                                farmLabNavigateToSuccess(it.data)
                            }
                        }

                        FarmLabLoadViewModel.FarmLabHomeScreenState.FarmLabNotInternet -> {
                            farmLabLoadBinding.farmLabStateGroup.visibility = View.VISIBLE
                            farmLabLoadBinding.farmLabLoadingGroup.visibility = View.GONE
                        }
                    }
                }
            }
        }
    }


    private fun farmLabNavigateToSuccess(data: String) {
        findNavController().navigate(
            R.id.action_farmLabLoadFragment_to_farmLabV,
            bundleOf(FARM_LAB_D to data)
        )
    }

    companion object {
        const val FARM_LAB_D = "farmLabData"
    }
}