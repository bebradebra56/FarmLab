package com.farmlab.labfarmis

import android.os.Bundle
import android.util.Log
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.updateLayoutParams
import com.farmlab.labfarmis.ut.FarmLabGlobalLayoutUtil
import com.farmlab.labfarmis.ut.farmLabSetupSystemBars
import com.farmlab.labfarmis.ut.presentation.app.FarmLabApplication
import com.farmlab.labfarmis.ut.presentation.pushhandler.FarmLabPushHandler
import org.koin.android.ext.android.inject

class FarmLabActivity : AppCompatActivity() {

    private val farmLabPushHandler by inject<FarmLabPushHandler>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        farmLabSetupSystemBars()
        WindowCompat.setDecorFitsSystemWindows(window, false)

        setContentView(R.layout.activity_farm_lab)

        val farmLabRootView = findViewById<View>(android.R.id.content)
        FarmLabGlobalLayoutUtil().farmLabAssistActivity(this)
        ViewCompat.setOnApplyWindowInsetsListener(farmLabRootView) { farmLabView, farmLabInsets ->
            val farmLabSystemBars = farmLabInsets.getInsets(WindowInsetsCompat.Type.systemBars())
            val farmLabDisplayCutout = farmLabInsets.getInsets(WindowInsetsCompat.Type.displayCutout())
            val farmLabIme = farmLabInsets.getInsets(WindowInsetsCompat.Type.ime())


            val farmLabTopPadding = maxOf(farmLabSystemBars.top, farmLabDisplayCutout.top)
            val farmLabLeftPadding = maxOf(farmLabSystemBars.left, farmLabDisplayCutout.left)
            val farmLabRightPadding = maxOf(farmLabSystemBars.right, farmLabDisplayCutout.right)
            window.setSoftInputMode(FarmLabApplication.farmLabInputMode)

            if (window.attributes.softInputMode == WindowManager.LayoutParams.SOFT_INPUT_ADJUST_PAN) {
                Log.d(FarmLabApplication.FARM_LAB_MAIN_TAG, "ADJUST PUN")
                val farmLabBottomInset = maxOf(farmLabSystemBars.bottom, farmLabDisplayCutout.bottom)

                farmLabView.setPadding(farmLabLeftPadding, farmLabTopPadding, farmLabRightPadding, 0)

                farmLabView.updateLayoutParams<ViewGroup.MarginLayoutParams> {
                    bottomMargin = farmLabBottomInset
                }
            } else {
                Log.d(FarmLabApplication.FARM_LAB_MAIN_TAG, "ADJUST RESIZE")

                val farmLabBottomInset = maxOf(farmLabSystemBars.bottom, farmLabDisplayCutout.bottom, farmLabIme.bottom)

                farmLabView.setPadding(farmLabLeftPadding, farmLabTopPadding, farmLabRightPadding, 0)

                farmLabView.updateLayoutParams<ViewGroup.MarginLayoutParams> {
                    bottomMargin = farmLabBottomInset
                }
            }



            WindowInsetsCompat.CONSUMED
        }
        Log.d(FarmLabApplication.FARM_LAB_MAIN_TAG, "Activity onCreate()")
        farmLabPushHandler.farmLabHandlePush(intent.extras)
    }

    override fun onWindowFocusChanged(hasFocus: Boolean) {
        super.onWindowFocusChanged(hasFocus)
        if (hasFocus) {
            farmLabSetupSystemBars()
        }
    }

    override fun onResume() {
        super.onResume()
        farmLabSetupSystemBars()
    }
}