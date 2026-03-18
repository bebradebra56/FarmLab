package com.farmlab.labfarmis.ut

import android.app.Activity
import android.graphics.Rect
import android.view.View
import android.widget.FrameLayout
import com.farmlab.labfarmis.ut.presentation.app.FarmLabApplication

class FarmLabGlobalLayoutUtil {

    private var farmLabMChildOfContent: View? = null
    private var farmLabUsableHeightPrevious = 0

    fun farmLabAssistActivity(activity: Activity) {
        val content = activity.findViewById<FrameLayout>(android.R.id.content)
        farmLabMChildOfContent = content.getChildAt(0)

        farmLabMChildOfContent?.viewTreeObserver?.addOnGlobalLayoutListener {
            possiblyResizeChildOfContent(activity)
        }
    }

    private fun possiblyResizeChildOfContent(activity: Activity) {
        val farmLabUsableHeightNow = farmLabComputeUsableHeight()
        if (farmLabUsableHeightNow != farmLabUsableHeightPrevious) {
            val farmLabUsableHeightSansKeyboard = farmLabMChildOfContent?.rootView?.height ?: 0
            val farmLabHeightDifference = farmLabUsableHeightSansKeyboard - farmLabUsableHeightNow

            if (farmLabHeightDifference > (farmLabUsableHeightSansKeyboard / 4)) {
                activity.window.setSoftInputMode(FarmLabApplication.farmLabInputMode)
            } else {
                activity.window.setSoftInputMode(FarmLabApplication.farmLabInputMode)
            }
//            mChildOfContent?.requestLayout()
            farmLabUsableHeightPrevious = farmLabUsableHeightNow
        }
    }

    private fun farmLabComputeUsableHeight(): Int {
        val r = Rect()
        farmLabMChildOfContent?.getWindowVisibleDisplayFrame(r)
        return r.bottom - r.top  // Visible height без status bar
    }
}