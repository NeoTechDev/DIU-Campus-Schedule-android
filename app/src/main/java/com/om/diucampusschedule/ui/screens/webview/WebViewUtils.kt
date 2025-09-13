package com.om.diucampusschedule.ui.screens.webview

import android.annotation.SuppressLint
import android.app.DownloadManager
import android.content.Context
import android.net.Uri
import android.os.Environment
import android.util.Log
import android.webkit.ConsoleMessage
import android.webkit.CookieManager
import android.webkit.GeolocationPermissions
import android.webkit.PermissionRequest
import android.webkit.URLUtil
import android.webkit.ValueCallback
import android.webkit.WebChromeClient
import android.webkit.WebResourceError
import android.webkit.WebResourceRequest
import android.webkit.WebResourceResponse
import android.webkit.WebSettings
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.Toast

/**
 * Professional WebView configuration utility using Android best practices
 * 
 * This implementation follows Google's recommended WebView practices:
 * - Secure default settings
 * - Proper touch and gesture handling
 * - Professional error handling
 * - Minimal JavaScript injection
 */

@SuppressLint("SetJavaScriptEnabled")
fun setupWebView(
    webView: WebView,
    context: Context,
    isDesktopMode: Boolean,
    onLoadingProgressChanged: (Int) -> Unit,
    onNavigationStateChanged: (canGoBack: Boolean, canGoForward: Boolean) -> Unit,
    onUrlChanged: (String) -> Unit,
    onTitleChanged: (String?) -> Unit,
    onError: (String) -> Unit,
    onFileUpload: (ValueCallback<Array<Uri>>) -> Unit,
    onDownload: (String, String) -> Unit
) {
    webView.apply {
        // Enable hardware acceleration for better performance
        setLayerType(android.view.View.LAYER_TYPE_HARDWARE, null)
        
        // Enable scrollbar fading and nested scrolling
        isScrollbarFadingEnabled = true
        isNestedScrollingEnabled = true
        
        // Configure WebView settings with modern best practices
        settings.apply {
            // JavaScript settings
            javaScriptEnabled = true
            javaScriptCanOpenWindowsAutomatically = true
            
            // Storage settings
            domStorageEnabled = true
            
            // File access settings (secure by default)
            allowFileAccess = false
            allowContentAccess = true
            // Note: File access from URLs is disabled for security
            
            // Cache settings
            cacheMode = WebSettings.LOAD_DEFAULT
            
            // Touch and zoom settings
            setSupportZoom(true)
            builtInZoomControls = true
            displayZoomControls = false
            setSupportMultipleWindows(false)
            
            // Layout settings
            useWideViewPort = true
            loadWithOverviewMode = true
            layoutAlgorithm = WebSettings.LayoutAlgorithm.TEXT_AUTOSIZING
            
            // Media settings
            mediaPlaybackRequiresUserGesture = false
            
            // Security settings
            mixedContentMode = WebSettings.MIXED_CONTENT_COMPATIBILITY_MODE
            
            // Font settings
            textZoom = 100
            minimumFontSize = 8
            
            // User agent configuration
            userAgentString = if (isDesktopMode) {
                "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/120.0.0.0 Safari/537.36"
            } else {
                WebSettings.getDefaultUserAgent(context)
            }
        }
        
        // Enable cookies for authentication
        CookieManager.getInstance().apply {
            setAcceptCookie(true)
            setAcceptThirdPartyCookies(webView, true)
        }
        
        // Professional WebViewClient implementation
        webViewClient = object : WebViewClient() {
            override fun shouldOverrideUrlLoading(view: WebView?, request: WebResourceRequest?): Boolean {
                val url = request?.url?.toString() ?: return false
                
                return when {
                    // Allow DIU portal domains
                    url.contains("diu.edu.bd") || 
                    url.contains("daffodilvarsity.edu.bd") ||
                    url.contains("elearn.daffodilvarsity.edu.bd") -> false
                    
                    // Allow document downloads
                    url.matches(Regex(".*\\.(pdf|doc|docx|xls|xlsx|ppt|pptx).*", RegexOption.IGNORE_CASE)) -> false
                    
                    // Handle other URLs as needed
                    else -> false
                }
            }
            
            override fun onPageStarted(view: WebView?, url: String?, favicon: android.graphics.Bitmap?) {
                super.onPageStarted(view, url, favicon)
                url?.let { onUrlChanged(it) }
                updateNavigationState(view)
            }
            
            override fun onPageFinished(view: WebView?, url: String?) {
                super.onPageFinished(view, url)
                url?.let { onUrlChanged(it) }
                updateNavigationState(view)
                
                // Minimal viewport fix only if needed
                view?.evaluateJavascript("""
                    javascript:(function() {
                        var existingMeta = document.querySelector('meta[name="viewport"]');
                        if (!existingMeta) {
                            var meta = document.createElement('meta');
                            meta.name = 'viewport';
                            meta.content = 'width=device-width, initial-scale=1.0, user-scalable=yes';
                            document.head.appendChild(meta);
                        }
                    })()
                """.trimIndent(), null)
            }
            
            override fun onReceivedError(
                view: WebView?,
                request: WebResourceRequest?,
                error: WebResourceError?
            ) {
                super.onReceivedError(view, request, error)
                if (request?.isForMainFrame == true) {
                    onError("Failed to load page: ${error?.description}")
                }
            }
            
            override fun onReceivedHttpError(
                view: WebView?,
                request: WebResourceRequest?,
                errorResponse: WebResourceResponse?
            ) {
                super.onReceivedHttpError(view, request, errorResponse)
                if (request?.isForMainFrame == true) {
                    onError("HTTP Error: ${errorResponse?.statusCode}")
                }
            }
            
            private fun updateNavigationState(view: WebView?) {
                onNavigationStateChanged(view?.canGoBack() ?: false, view?.canGoForward() ?: false)
            }
        }
        
        // Professional WebChromeClient implementation
        webChromeClient = object : WebChromeClient() {
            override fun onProgressChanged(view: WebView?, newProgress: Int) {
                super.onProgressChanged(view, newProgress)
                onLoadingProgressChanged(newProgress)
            }
            
            override fun onReceivedTitle(view: WebView?, title: String?) {
                super.onReceivedTitle(view, title)
                onTitleChanged(title)
            }
            
            override fun onConsoleMessage(consoleMessage: ConsoleMessage?): Boolean {
                consoleMessage?.let { msg ->
                    when (msg.messageLevel()) {
                        ConsoleMessage.MessageLevel.ERROR -> {
                            Log.e("WebView", "JS Error: ${msg.message()} at ${msg.sourceId()}:${msg.lineNumber()}")
                        }
                        ConsoleMessage.MessageLevel.WARNING -> {
                            Log.w("WebView", "JS Warning: ${msg.message()}")
                        }
                        else -> {
                            // Only log debug messages for important info
                            Log.d("WebView", "JS: ${msg.message()}")
                        }
                    }
                }
                return true
            }
            
            override fun onShowFileChooser(
                webView: WebView?,
                filePathCallback: ValueCallback<Array<Uri>>?,
                fileChooserParams: FileChooserParams?
            ): Boolean {
                filePathCallback?.let { callback ->
                    onFileUpload(callback)
                    return true
                }
                return super.onShowFileChooser(webView, filePathCallback, fileChooserParams)
            }
            
            override fun onGeolocationPermissionsShowPrompt(
                origin: String?,
                callback: GeolocationPermissions.Callback?
            ) {
                if (origin?.contains("diu.edu.bd") == true || 
                    origin?.contains("daffodilvarsity.edu.bd") == true) {
                    callback?.invoke(origin, true, false)
                } else {
                    super.onGeolocationPermissionsShowPrompt(origin, callback)
                }
            }
            
            override fun onPermissionRequest(request: PermissionRequest?) {
                request?.let { req ->
                    val resources = req.resources
                    if (resources.any { it == PermissionRequest.RESOURCE_VIDEO_CAPTURE || 
                                       it == PermissionRequest.RESOURCE_AUDIO_CAPTURE }) {
                        req.grant(resources)
                    } else {
                        super.onPermissionRequest(request)
                    }
                }
            }
        }
        
        // Set up download listener
        setDownloadListener { url, _, contentDisposition, mimeType, _ ->
            try {
                val filename = URLUtil.guessFileName(url, contentDisposition, mimeType)
                onDownload(url, filename)
            } catch (e: Exception) {
                Log.e("WebView", "Download error", e)
                Toast.makeText(context, "Download failed: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }
}

/**
 * Downloads a file using Android's DownloadManager
 */
fun downloadFile(context: Context, url: String, filename: String) {
    try {
        val request = DownloadManager.Request(Uri.parse(url)).apply {
            setDestinationInExternalPublicDir(Environment.DIRECTORY_DOWNLOADS, filename)
            setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED)
            setTitle(filename)
            setDescription("Downloading from DIU Portal")
            addRequestHeader("User-Agent", WebSettings.getDefaultUserAgent(context))
            
            val cookies = CookieManager.getInstance().getCookie(url)
            if (!cookies.isNullOrEmpty()) {
                addRequestHeader("Cookie", cookies)
            }
        }
        
        val downloadManager = context.getSystemService(Context.DOWNLOAD_SERVICE) as DownloadManager
        downloadManager.enqueue(request)
        
        Toast.makeText(context, "Download started: $filename", Toast.LENGTH_SHORT).show()
    } catch (e: Exception) {
        Log.e("WebView", "Download failed", e)
        Toast.makeText(context, "Download failed: ${e.message}", Toast.LENGTH_SHORT).show()
    }
}