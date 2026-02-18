package com.hsmomo.lottogen.domain.model

data class AppInfo(
    val latestDrawNo: Int?,
    val lastSyncTime: Long?,
    val needsUpdate: Boolean
)
