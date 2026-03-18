package com.farmlab.labfarmis.ut.domain.model

import com.google.gson.annotations.SerializedName


private const val FARM_LAB_A = "com.farmlab.labfarmis"
private const val FARM_LAB_B = "farmlab-61c5b"
data class FarmLabParam (
    @SerializedName("af_id")
    val farmLabAfId: String,
    @SerializedName("bundle_id")
    val farmLabBundleId: String = FARM_LAB_A,
    @SerializedName("os")
    val farmLabOs: String = "Android",
    @SerializedName("store_id")
    val farmLabStoreId: String = FARM_LAB_A,
    @SerializedName("locale")
    val farmLabLocale: String,
    @SerializedName("push_token")
    val farmLabPushToken: String,
    @SerializedName("firebase_project_id")
    val farmLabFirebaseProjectId: String = FARM_LAB_B,

    )