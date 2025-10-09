package com.om.diucampusschedule.ui.screens.webview

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.webkit.ValueCallback
import android.webkit.WebView
import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.DesktopWindows
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Smartphone
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WebViewScreen(
    url: String,
    title: String,
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current

    // WebView and UI state
    var webView: WebView? by remember { mutableStateOf(null) }
    var isLoading by remember { mutableStateOf(true) }
    var loadingProgress by remember { mutableStateOf(0f) }
    var canGoBack by remember { mutableStateOf(false) }
    var canGoForward by remember { mutableStateOf(false) }
    var currentUrl by remember { mutableStateOf(url) }
    var pageTitle by remember { mutableStateOf(title) }
    var isDesktopMode by remember { mutableStateOf(false) }
    var hasError by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf("") }
    var showDropdownMenu by remember { mutableStateOf(false) }
    var fileUploadCallback: ValueCallback<Array<Uri>>? by remember { mutableStateOf(null) }

    // File picker launcher for uploads
    val filePickerLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        val uris = result.data?.let {
            it.data?.let { uri -> arrayOf(uri) } ?: it.clipData?.let { clipData ->
                Array(clipData.itemCount) { i -> clipData.getItemAt(i).uri }
            }
        } ?: emptyArray()
        fileUploadCallback?.onReceiveValue(uris)
        fileUploadCallback = null
    }

    // Permission launcher for downloads
    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        Toast.makeText(context, if (isGranted) "Permission granted" else "Permission denied", Toast.LENGTH_SHORT).show()
    }

    // Progress bar animation
    val animatedProgress by animateFloatAsState(
        targetValue = loadingProgress,
        animationSpec = tween(durationMillis = 300, easing = LinearEasing),
        label = "progress"
    )

    // Handle system back button press to navigate in WebView
    BackHandler(enabled = canGoBack) {
        webView?.goBack()
    }

    // Ensure WebView is destroyed to prevent memory leaks
    DisposableEffect(Unit) {
        onDispose {
            webView?.destroy()
        }
    }

    fun checkAndRequestStoragePermission(): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            true // No specific permission needed on Android 13+
        } else {
            val permission = Manifest.permission.WRITE_EXTERNAL_STORAGE
            if (ContextCompat.checkSelfPermission(context, permission) == PackageManager.PERMISSION_GRANTED) {
                true
            } else {
                permissionLauncher.launch(permission)
                false
            }
        }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .navigationBarsPadding()
            .background(MaterialTheme.colorScheme.background)
    ) {
        TopAppBar(
            title = {
                Column {
                    Text(
                        text = pageTitle,
                        style = MaterialTheme.typography.titleMedium,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                    Text(
                        text = currentUrl,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            },
            navigationIcon = {
                IconButton(onClick = onNavigateBack) {
                    Icon(Icons.Default.Close, contentDescription = "Close")
                }
            },
            actions = {
                IconButton(onClick = { webView?.goBack() }, enabled = canGoBack) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                }
                IconButton(onClick = { webView?.goForward() }, enabled = canGoForward) {
                    Icon(Icons.AutoMirrored.Filled.ArrowForward, contentDescription = "Forward")
                }
                IconButton(onClick = { hasError = false; webView?.reload() }) {
                    if (isLoading) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(20.dp),
                            strokeWidth = 2.dp,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    } else {
                        Icon(
                            imageVector = Icons.Default.Refresh,
                            contentDescription = "Reload",
                            tint = MaterialTheme.colorScheme.onSurface
                        )
                    }
                }
                Box {
                    IconButton(onClick = { showDropdownMenu = true }) {
                        Icon(Icons.Default.MoreVert, contentDescription = "More options")
                    }
                    DropdownMenu(
                        expanded = showDropdownMenu,
                        onDismissRequest = { showDropdownMenu = false }
                    ) {
                        DropdownMenuItem(
                            text = { Text(if (isDesktopMode) "Mobile View" else "Desktop View") },
                            leadingIcon = {
                                Icon(
                                    if (isDesktopMode) Icons.Default.Smartphone else Icons.Default.DesktopWindows,
                                    contentDescription = null
                                )
                            },
                            onClick = {
                                isDesktopMode = !isDesktopMode
                                showDropdownMenu = false
                            }
                        )
                    }
                }
            },
            colors = TopAppBarDefaults.topAppBarColors(
                containerColor = if(isSystemInDarkTheme()) MaterialTheme.colorScheme.surface else MaterialTheme.colorScheme.background
            )
        )
        if(!isSystemInDarkTheme()){
            HorizontalDivider(
                thickness = 1.dp,
                color = MaterialTheme.colorScheme.outline.copy(alpha = 0.12f)
            )
        }

        if (isLoading && animatedProgress < 100f) {
            LinearProgressIndicator(
                progress = { animatedProgress / 100f },
                modifier = Modifier.fillMaxWidth(),
                strokeCap = StrokeCap.Round
            )
        }

        if (hasError) {
            Box(
                modifier = Modifier.fillMaxSize().padding(16.dp),
                contentAlignment = Alignment.Center
            ) {
                Card(
                    shape = RoundedCornerShape(16.dp),
                    elevation = CardDefaults.cardElevation(4.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text("Connection Error", style = MaterialTheme.typography.titleLarge)
                        Spacer(Modifier.height(8.dp))
                        Text(
                            text = errorMessage,
                            textAlign = TextAlign.Center,
                            style = MaterialTheme.typography.bodyMedium
                        )
                        Spacer(Modifier.height(24.dp))
                        Surface(
                            onClick = { hasError = false; webView?.reload() },
                            shape = RoundedCornerShape(8.dp),
                            color = MaterialTheme.colorScheme.primary,
                            contentColor = MaterialTheme.colorScheme.onPrimary
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 24.dp, vertical = 12.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(Icons.Default.Refresh, contentDescription = null)
                                Spacer(Modifier.width(8.dp))
                                Text("Try Again", fontWeight = FontWeight.Medium)
                            }
                        }
                    }
                }
            }
        } else {
            // By using `key`, we force the AndroidView to be recomposed (and the WebView recreated)
            // whenever `isDesktopMode` changes. This is the most reliable way to switch modes.
            key(isDesktopMode) {
                AndroidView(
                    factory = { ctx ->
                        WebView(ctx).apply {
                            webView = this // Update the reference
                            setupWebView(
                                webView = this,
                                context = ctx,
                                isDesktopMode = isDesktopMode,
                                onLoadingProgressChanged = { progress ->
                                    loadingProgress = progress.toFloat()
                                    isLoading = progress < 100
                                },
                                onNavigationStateChanged = { back, forward ->
                                    canGoBack = back
                                    canGoForward = forward
                                },
                                onUrlChanged = { newUrl -> currentUrl = newUrl },
                                onTitleChanged = { newTitle -> pageTitle = newTitle ?: title },
                                onError = { message ->
                                    hasError = true
                                    errorMessage = message
                                    isLoading = false
                                },
                                onFileUpload = { callback ->
                                    fileUploadCallback = callback
                                    val intent = Intent(Intent.ACTION_GET_CONTENT).apply {
                                        type = "*/*"
                                        addCategory(Intent.CATEGORY_OPENABLE)
                                        putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true)
                                    }
                                    filePickerLauncher.launch(intent)
                                },
                                onDownload = { downloadUrl, filename ->
                                    if (checkAndRequestStoragePermission()) {
                                        downloadFile(context, downloadUrl, filename)
                                    }
                                }
                            )
                            loadUrl(currentUrl) // Load current URL in case user navigated
                        }
                    },
                    modifier = Modifier.fillMaxSize()
                )
            }
        }
    }
}