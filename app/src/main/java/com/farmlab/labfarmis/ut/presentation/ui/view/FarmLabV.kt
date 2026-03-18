package com.farmlab.labfarmis.ut.presentation.ui.view

import android.content.DialogInterface
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.webkit.CookieManager
import android.webkit.ValueCallback
import android.widget.FrameLayout
import androidx.activity.OnBackPressedCallback
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import com.farmlab.labfarmis.ut.presentation.app.FarmLabApplication
import com.farmlab.labfarmis.ut.presentation.ui.load.FarmLabLoadFragment
import org.koin.android.ext.android.inject

class FarmLabV : Fragment(){

    private lateinit var farmLabPhoto: Uri
    private var farmLabFilePathFromChrome: ValueCallback<Array<Uri>>? = null

    private val farmLabTakeFile: ActivityResultLauncher<PickVisualMediaRequest> = registerForActivityResult(ActivityResultContracts.PickVisualMedia()) {
        farmLabFilePathFromChrome?.onReceiveValue(arrayOf(it ?: Uri.EMPTY))
        farmLabFilePathFromChrome = null
    }

    private val farmLabTakePhoto: ActivityResultLauncher<Uri> = registerForActivityResult(ActivityResultContracts.TakePicture()) {
        if (it) {
            farmLabFilePathFromChrome?.onReceiveValue(arrayOf(farmLabPhoto))
            farmLabFilePathFromChrome = null
        } else {
            farmLabFilePathFromChrome?.onReceiveValue(null)
            farmLabFilePathFromChrome = null
        }
    }

    private val farmLabDataStore by activityViewModels<FarmLabDataStore>()


    private val farmLabViFun by inject<FarmLabViFun>()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Log.d(FarmLabApplication.FARM_LAB_MAIN_TAG, "Fragment onCreate")
        CookieManager.getInstance().setAcceptCookie(true)
        requireActivity().onBackPressedDispatcher.addCallback(this,
            object : OnBackPressedCallback(true) {
                override fun handleOnBackPressed() {
                    if (farmLabDataStore.farmLabView.canGoBack()) {
                        farmLabDataStore.farmLabView.goBack()
                        Log.d(FarmLabApplication.FARM_LAB_MAIN_TAG, "WebView can go back")
                    } else if (farmLabDataStore.farmLabViList.size > 1) {
                        Log.d(FarmLabApplication.FARM_LAB_MAIN_TAG, "WebView can`t go back")
                        farmLabDataStore.farmLabViList.removeAt(farmLabDataStore.farmLabViList.lastIndex)
                        Log.d(FarmLabApplication.FARM_LAB_MAIN_TAG, "WebView list size ${farmLabDataStore.farmLabViList.size}")
                        farmLabDataStore.farmLabView.destroy()
                        val previousWebView = farmLabDataStore.farmLabViList.last()
                        farmLabAttachWebViewToContainer(previousWebView)
                        farmLabDataStore.farmLabView = previousWebView
                    }
                }

            })
    }


    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        if (farmLabDataStore.farmLabIsFirstCreate) {
            farmLabDataStore.farmLabIsFirstCreate = false
            farmLabDataStore.farmLabContainerView = FrameLayout(requireContext()).apply {
                layoutParams = ViewGroup.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.MATCH_PARENT
                )
                id = View.generateViewId()
            }
            return farmLabDataStore.farmLabContainerView
        } else {
            return farmLabDataStore.farmLabContainerView
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        Log.d(FarmLabApplication.FARM_LAB_MAIN_TAG, "onViewCreated")
        if (farmLabDataStore.farmLabViList.isEmpty()) {
            farmLabDataStore.farmLabView = FarmLabVi(requireContext(), object :
                FarmLabCallBack {
                override fun farmLabHandleCreateWebWindowRequest(farmLabVi: FarmLabVi) {
                    farmLabDataStore.farmLabViList.add(farmLabVi)
                    Log.d(FarmLabApplication.FARM_LAB_MAIN_TAG, "WebView list size = ${farmLabDataStore.farmLabViList.size}")
                    Log.d(FarmLabApplication.FARM_LAB_MAIN_TAG, "CreateWebWindowRequest")
                    farmLabDataStore.farmLabView = farmLabVi
                    farmLabVi.farmLabSetFileChooserHandler { callback ->
                        farmLabHandleFileChooser(callback)
                    }
                    farmLabAttachWebViewToContainer(farmLabVi)
                }

            }, farmLabWindow = requireActivity().window).apply {
                farmLabSetFileChooserHandler { callback ->
                    farmLabHandleFileChooser(callback)
                }
            }
            farmLabDataStore.farmLabView.farmLabFLoad(arguments?.getString(
                FarmLabLoadFragment.FARM_LAB_D) ?: "")
//            ejvview.fLoad("www.google.com")
            farmLabDataStore.farmLabViList.add(farmLabDataStore.farmLabView)
            farmLabAttachWebViewToContainer(farmLabDataStore.farmLabView)
        } else {
            farmLabDataStore.farmLabViList.forEach { webView ->
                webView.farmLabSetFileChooserHandler { callback ->
                    farmLabHandleFileChooser(callback)
                }
            }
            farmLabDataStore.farmLabView = farmLabDataStore.farmLabViList.last()

            farmLabAttachWebViewToContainer(farmLabDataStore.farmLabView)
        }
        Log.d(FarmLabApplication.FARM_LAB_MAIN_TAG, "WebView list size = ${farmLabDataStore.farmLabViList.size}")
    }

    private fun farmLabHandleFileChooser(callback: ValueCallback<Array<Uri>>?) {
        Log.d(FarmLabApplication.FARM_LAB_MAIN_TAG, "handleFileChooser called, callback: ${callback != null}")

        farmLabFilePathFromChrome = callback

        val listItems: Array<out String> = arrayOf("Select from file", "To make a photo")
        val listener = DialogInterface.OnClickListener { _, which ->
            when (which) {
                0 -> {
                    Log.d(FarmLabApplication.FARM_LAB_MAIN_TAG, "Launching file picker")
                    farmLabTakeFile.launch(
                        PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                    )
                }
                1 -> {
                    Log.d(FarmLabApplication.FARM_LAB_MAIN_TAG, "Launching camera")
                    farmLabPhoto = farmLabViFun.farmLabSavePhoto()
                    farmLabTakePhoto.launch(farmLabPhoto)
                }
            }
        }

        AlertDialog.Builder(requireContext())
            .setTitle("Choose a method")
            .setItems(listItems, listener)
            .setCancelable(true)
            .setOnCancelListener {
                Log.d(FarmLabApplication.FARM_LAB_MAIN_TAG, "File chooser canceled")
                callback?.onReceiveValue(null)
                farmLabFilePathFromChrome = null
            }
            .create()
            .show()
    }

    private fun farmLabAttachWebViewToContainer(w: FarmLabVi) {
        farmLabDataStore.farmLabContainerView.post {
            (w.parent as? ViewGroup)?.removeView(w)
            farmLabDataStore.farmLabContainerView.removeAllViews()
            farmLabDataStore.farmLabContainerView.addView(w)
        }
    }


}