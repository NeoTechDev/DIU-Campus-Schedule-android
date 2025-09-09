package com.om.diucampusschedule.ui.utils

import android.content.Context
import com.om.diucampusschedule.core.network.NetworkConnectivityManager
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class NetworkUtils @Inject constructor(
    private val networkConnectivityManager: NetworkConnectivityManager
) {
    
    /**
     * Check if device has internet connectivity
     */
    fun isInternetAvailable(): Boolean {
        return networkConnectivityManager.isConnected()
    }

    /**
     * Get network error message for user display
     */
    fun getNetworkErrorMessage(): String {
        return networkConnectivityManager.getNetworkErrorMessage()
    }
}

/**
 * Extension function to check internet availability from any context
 * This is a simplified version that directly checks connectivity
 */
fun isInternetAvailable(context: Context): Boolean {
    return try {
        val networkManager = NetworkConnectivityManager(context)
        networkManager.isConnected()
    } catch (e: Exception) {
        false
    }
}
