package com.farmlab.labfarmis.ut.domain.model

import com.google.gson.annotations.SerializedName


data class FarmLabEntity (
    @SerializedName("ok")
    val farmLabOk: String,
    @SerializedName("url")
    val farmLabUrl: String,
    @SerializedName("expires")
    val farmLabExpires: Long,
)