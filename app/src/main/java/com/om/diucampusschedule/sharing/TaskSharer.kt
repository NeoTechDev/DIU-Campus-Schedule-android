package com.om.diucampusschedule.sharing

import android.content.Context
import android.net.nsd.NsdManager
import android.net.nsd.NsdServiceInfo
import android.os.Build
import android.util.Log
import com.om.diucampusschedule.domain.model.Task
import dagger.hilt.android.qualifiers.ApplicationContext // Added this import
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import java.io.BufferedReader
import java.io.InputStreamReader
import java.io.OutputStreamWriter
import java.net.Inet4Address
import java.net.NetworkInterface
import java.net.ServerSocket
import java.net.Socket
import javax.inject.Inject
import javax.inject.Singleton

@Serializable
data class TaskWithSender(
    val task: Task,
    val senderName: String
)

@Singleton
class TaskSharer @Inject constructor(
    @ApplicationContext private val context: Context // Added @ApplicationContext
) {

    private val nsdManager by lazy { context.getSystemService(Context.NSD_SERVICE) as NsdManager }
    private val coroutineScope = CoroutineScope(Dispatchers.IO)

    private val SERVICE_TYPE = "_diucs._tcp"
    private var servicePort: Int = 0

    private val _discoveredServices = MutableStateFlow<List<NsdServiceInfo>>(emptyList())
    val discoveredServices: StateFlow<List<NsdServiceInfo>> = _discoveredServices

    // Sharing status for UI feedback
    private val _sharingStatus = MutableStateFlow<SharingStatus>(SharingStatus.Idle)
    val sharingStatus: StateFlow<SharingStatus> = _sharingStatus

    private var registrationListener: NsdManager.RegistrationListener? = null
    private var discoveryListener: NsdManager.DiscoveryListener? = null

    // Callback for received tasks
    var onTaskReceived: ((Task, String) -> Unit)? = null

    fun registerService() {
        coroutineScope.launch {
            try {
                val serverSocket = ServerSocket(0)
                servicePort = serverSocket.localPort

                // Get user name from SharedPreferences
                val sharedPreferences = context.getSharedPreferences("profile_prefs", Context.MODE_PRIVATE)
                val userName = sharedPreferences.getString("name", "Unknown User") ?: "Unknown User"

                val serviceInfo = NsdServiceInfo().apply {
                    serviceName = "$userName|${Build.MODEL}"  // Using | as separator
                    serviceType = SERVICE_TYPE
                    port = servicePort
                }

                registrationListener = object : NsdManager.RegistrationListener {
                    override fun onServiceRegistered(NsdServiceInfo: NsdServiceInfo) {
                        Log.d("TaskSharer", "Service registered: $NsdServiceInfo")
                    }

                    override fun onRegistrationFailed(serviceInfo: NsdServiceInfo, errorCode: Int) {
                        Log.e("TaskSharer", "Service registration failed: $errorCode")
                    }

                    override fun onServiceUnregistered(arg0: NsdServiceInfo) {
                        Log.d("TaskSharer", "Service unregistered: $arg0")
                    }

                    override fun onUnregistrationFailed(serviceInfo: NsdServiceInfo, errorCode: Int) {
                        Log.e("TaskSharer", "Service unregistration failed: $errorCode")
                    }
                }

                nsdManager.registerService(serviceInfo, NsdManager.PROTOCOL_DNS_SD, registrationListener)

                while (true) {
                    val socket = serverSocket.accept()
                    handleIncomingSocket(socket)
                }

            } catch (e: Exception) {
                Log.e("TaskSharer", "Error in server", e)
            }
        }
    }

    fun discoverServices() {
        discoveryListener = object : NsdManager.DiscoveryListener {
            override fun onDiscoveryStarted(regType: String) {
                Log.d("TaskSharer", "Service discovery started")
            }

            override fun onServiceFound(service: NsdServiceInfo) {
                Log.d("TaskSharer", "Service found: $service")
                nsdManager.resolveService(service, object : NsdManager.ResolveListener {
                    override fun onResolveFailed(serviceInfo: NsdServiceInfo, errorCode: Int) {
                        Log.e("TaskSharer", "Resolve failed: $errorCode")
                    }

                    override fun onServiceResolved(serviceInfo: NsdServiceInfo) {
                        Log.d("TaskSharer", "Service resolved: $serviceInfo")
                        val myIp = getLocalIpAddress()
                        serviceInfo.host?.hostAddress?.let { hostAddress ->
                            if (hostAddress != myIp) {
                                _discoveredServices.update { currentList ->
                                    val newList = currentList.filterNot { it.host?.hostAddress == hostAddress }
                                    newList + serviceInfo
                                }
                            }
                        }
                    }
                })
            }

            override fun onServiceLost(service: NsdServiceInfo) {
                Log.d("TaskSharer", "Service lost: $service")
                _discoveredServices.update { currentList ->
                    currentList.filterNot { it.serviceName == service.serviceName }
                }
            }

            override fun onDiscoveryStopped(serviceType: String) {
                Log.i("TaskSharer", "Discovery stopped: $serviceType")
            }

            override fun onStartDiscoveryFailed(serviceType: String, errorCode: Int) {
                Log.e("TaskSharer", "Discovery failed: Error code:$errorCode")
                // Don\'t try to stop discovery if it failed to start - the listener isn\'t registered yet
            }

            override fun onStopDiscoveryFailed(serviceType: String, errorCode: Int) {
                Log.e("TaskSharer", "Stop Discovery failed: Error code:$errorCode")
                nsdManager.stopServiceDiscovery(this)
            }
        }

        nsdManager.discoverServices(SERVICE_TYPE, NsdManager.PROTOCOL_DNS_SD, discoveryListener)
    }

    fun sendTask(peer: NsdServiceInfo, task: Task) {
        coroutineScope.launch {
            try {
                _sharingStatus.value = SharingStatus.Sending(peer.serviceName ?: "Unknown Device")
                val socket = Socket(peer.host, peer.port)
                socket.use {
                    val writer = OutputStreamWriter(it.outputStream)
                    val taskWithSender = TaskWithSender(
                        task = task.copy(id = 0), // Send as new task
                        senderName = Build.MODEL // Include sender device name
                    )
                    val json = Json.encodeToString(taskWithSender)
                    writer.write(json)
                    writer.flush()
                }
                Log.d("TaskSharer", "Task sent to ${peer.host}")
                _sharingStatus.value = SharingStatus.Success(peer.serviceName ?: "Unknown Device")
                // Reset status after delay
                delay(3000)
                _sharingStatus.value = SharingStatus.Idle
            } catch (e: Exception) {
                Log.e("TaskSharer", "Error sending task", e)
                _sharingStatus.value = SharingStatus.Error("Failed to send task: ${e.localizedMessage}")
                // Reset status after delay
                delay(3000)
                _sharingStatus.value = SharingStatus.Idle
            }
        }
    }

    fun sendMultipleTasks(peer: NsdServiceInfo, tasks: List<Task>) {
        coroutineScope.launch {
            try {
                _sharingStatus.value = SharingStatus.Sending(peer.serviceName ?: "Unknown Device")
                val socket = Socket(peer.host, peer.port)
                socket.use {
                    val writer = OutputStreamWriter(it.outputStream)
                    tasks.forEach { task ->
                        val taskWithSender = TaskWithSender(
                            task = task.copy(id = 0), // Send as new task
                            senderName = Build.MODEL // Include sender device name
                        )
                        val json = Json.encodeToString(taskWithSender)
                        writer.write(json + "\n") // Add newline separator for multiple tasks
                        writer.flush()
                    }
                }
                Log.d("TaskSharer", "Tasks sent to ${peer.host}")
                _sharingStatus.value = SharingStatus.Success(peer.serviceName ?: "Unknown Device")
                // Reset status after delay
                delay(3000)
                _sharingStatus.value = SharingStatus.Idle
            } catch (e: Exception) {
                Log.e("TaskSharer", "Error sending tasks", e)
                _sharingStatus.value = SharingStatus.Error("Failed to send tasks: ${e.localizedMessage}")
                // Reset status after delay
                delay(3000)
                _sharingStatus.value = SharingStatus.Idle
            }
        }
    }

    private fun handleIncomingSocket(socket: Socket) {
        coroutineScope.launch {
            try {
                socket.use {
                    val reader = BufferedReader(InputStreamReader(it.inputStream))
                    val json = reader.readText()
                    
                    // Handle multiple tasks separated by newlines
                    json.split("\n").filter { it.isNotBlank() }.forEach { taskJson ->
                        try {
                            val taskWithSender = Json.decodeFromString<TaskWithSender>(taskJson)
                            onTaskReceived?.invoke(taskWithSender.task, taskWithSender.senderName)
                        } catch (e: Exception) {
                            // Fallback for backward compatibility with older app versions
                            try {
                                val task = Json.decodeFromString<Task>(taskJson)
                                onTaskReceived?.invoke(task, "Unknown Device")
                            } catch (e2: Exception) {
                                Log.e("TaskSharer", "Error parsing received task", e2)
                            }
                        }
                    }
                }
            } catch (e: Exception) {
                Log.e("TaskSharer", "Error receiving task", e)
            }
        }
    }
    
    fun unregisterService() {
        registrationListener?.let {
            try {
                nsdManager.unregisterService(it)
            } catch (e: IllegalArgumentException) {
                Log.w("TaskSharer", "Listener already unregistered or never registered.", e)
            }
            registrationListener = null
        }
    }

    fun stopDiscovery() {
        discoveryListener?.let { listener ->
            try {
                nsdManager.stopServiceDiscovery(listener)
                Log.d("TaskSharer", "Service discovery stopped successfully.")
            } catch (e: IllegalArgumentException) {
                // This exception can occur if the listener was already unregistered
                // or if discovery was never successfully started.
                Log.w("TaskSharer", "Error stopping service discovery: listener not registered.", e)
            } finally {
                discoveryListener = null // Ensure listener is nulled out even if stop fails or was already stopped
            }
        }
        _discoveredServices.value = emptyList()
    }

    private fun getLocalIpAddress(): String? {
        try {
            val en = NetworkInterface.getNetworkInterfaces()
            while (en.hasMoreElements()) {
                val intf = en.nextElement()
                val enumIpAddr = intf.inetAddresses
                while (enumIpAddr.hasMoreElements()) {
                    val inetAddress = enumIpAddr.nextElement()
                    if (!inetAddress.isLoopbackAddress && inetAddress is Inet4Address) {
                        return inetAddress.hostAddress
                    }
                }
            }
        } catch (ex: Exception) {
            Log.e("TaskSharer", "Could not get local IP address", ex)
        }
        return null
    }
}

sealed class SharingStatus {
    object Idle : SharingStatus()
    data class Sending(val deviceName: String) : SharingStatus()
    data class Success(val deviceName: String) : SharingStatus()
    data class Error(val message: String) : SharingStatus()
}
