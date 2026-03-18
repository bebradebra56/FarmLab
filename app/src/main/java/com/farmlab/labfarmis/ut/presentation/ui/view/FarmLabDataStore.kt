package com.farmlab.labfarmis.ut.presentation.ui.view

import android.annotation.SuppressLint
import android.widget.FrameLayout
import androidx.lifecycle.ViewModel

class FarmLabDataStore : ViewModel(){
    val farmLabViList: MutableList<FarmLabVi> = mutableListOf()
    var farmLabIsFirstCreate = true
    @SuppressLint("StaticFieldLeak")
    lateinit var farmLabContainerView: FrameLayout
    @SuppressLint("StaticFieldLeak")
    lateinit var farmLabView: FarmLabVi

}