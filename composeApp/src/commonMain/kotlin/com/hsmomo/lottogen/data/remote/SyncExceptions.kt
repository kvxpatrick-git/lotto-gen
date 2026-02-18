package com.hsmomo.lottogen.data.remote

open class LottoSyncException(
    message: String,
    cause: Throwable? = null
) : Exception(message, cause)

class NetworkSyncException(
    message: String = "Network request failed",
    cause: Throwable? = null
) : LottoSyncException(message, cause)

class ApiResponseSyncException(
    message: String = "Invalid API response",
    cause: Throwable? = null
) : LottoSyncException(message, cause)

class LatestDrawResolutionException(
    message: String = "Unable to resolve latest draw",
    cause: Throwable? = null
) : LottoSyncException(message, cause)
