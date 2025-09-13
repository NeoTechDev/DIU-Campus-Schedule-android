package com.om.diucampusschedule.ui.screens.webview

import android.annotation.SuppressLint
import android.app.DownloadManager
import android.content.Context
import android.net.Uri
import android.net.http.SslError
import android.os.Build
import android.os.Environment
import android.util.Log
import android.webkit.ConsoleMessage
import android.webkit.CookieManager
import android.webkit.GeolocationPermissions
import android.webkit.PermissionRequest
import android.webkit.SslErrorHandler
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
 * WebView configuration based on working previous version
 * This approach uses the exact settings that made menu drawers work properly
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
        // Layout params (from working version)
        layoutParams = android.view.ViewGroup.LayoutParams(
            android.view.ViewGroup.LayoutParams.MATCH_PARENT,
            android.view.ViewGroup.LayoutParams.MATCH_PARENT
        )
        
        // Configure WebView settings for better portal access
        settings.apply {
            // Core settings
            javaScriptEnabled = true
            mixedContentMode = WebSettings.MIXED_CONTENT_ALWAYS_ALLOW
            cacheMode = WebSettings.LOAD_CACHE_ELSE_NETWORK // Try cache first, may help with 403
            domStorageEnabled = true
            loadWithOverviewMode = true
            useWideViewPort = true

            // File access settings (required for proper functionality)
            allowFileAccess = true
            allowContentAccess = true
            javaScriptCanOpenWindowsAutomatically = true
            setSupportMultipleWindows(true)
            
            // Security settings - more permissive for educational sites
            @Suppress("DEPRECATION")
            allowUniversalAccessFromFileURLs = true
            @Suppress("DEPRECATION")
            allowFileAccessFromFileURLs = true
            
            // Plugin and rendering settings
            @Suppress("DEPRECATION")
            pluginState = WebSettings.PluginState.OFF
            @Suppress("DEPRECATION")
            setRenderPriority(WebSettings.RenderPriority.HIGH)
            @Suppress("DEPRECATION")
            setEnableSmoothTransition(true)
            blockNetworkImage = false
            
            // Zoom settings
            setSupportZoom(true)
            builtInZoomControls = true
            displayZoomControls = false

            // Lollipop+ specific settings
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                mixedContentMode = WebSettings.MIXED_CONTENT_ALWAYS_ALLOW
            }

            // Enable debugging
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
                WebView.setWebContentsDebuggingEnabled(true)
            }

            // File URL access (important for menu functionality)
            @Suppress("DEPRECATION")
            allowUniversalAccessFromFileURLs = true
            @Suppress("DEPRECATION")
            allowFileAccessFromFileURLs = true

            // Database and session management
            @Suppress("DEPRECATION")
            databaseEnabled = true
            setGeolocationEnabled(true)
            @Suppress("DEPRECATION")
            setDatabasePath(context.getDir("database", Context.MODE_PRIVATE).path)
            @Suppress("DEPRECATION")
            setGeolocationDatabasePath(context.getDir("geolocation", Context.MODE_PRIVATE).path)
            @Suppress("DEPRECATION")
            setSaveFormData(true)
            @Suppress("DEPRECATION")
            setSavePassword(true)

            // Network and media settings
            loadsImagesAutomatically = true
            mediaPlaybackRequiresUserGesture = false
            setSupportMultipleWindows(true)
            setNeedInitialFocus(true)
            allowContentAccess = true
            allowFileAccess = true
            
            // User agent
            userAgentString = if (isDesktopMode) {
                "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/120.0.0.0 Safari/537.36"
            } else {
                WebSettings.getDefaultUserAgent(context)
            }
        }

        // Clear cache and cookies (from working version)
        clearCache(true)
        clearHistory()
        CookieManager.getInstance().removeAllCookies(null)
        CookieManager.getInstance().flush()
        CookieManager.getInstance().setAcceptCookie(true)
        CookieManager.getInstance().setAcceptThirdPartyCookies(this, true)
        
        // Enable cookies for authentication
        CookieManager.getInstance().apply {
            setAcceptCookie(true)
            setAcceptThirdPartyCookies(webView, true)
        }
        
        // WebViewClient with enhanced error handling
        webViewClient = object : WebViewClient() {
            override fun shouldOverrideUrlLoading(view: WebView?, request: WebResourceRequest?): Boolean {
                val url = request?.url?.toString()
                
                // Allow all DIU related URLs
                if (url?.contains("diu.edu.bd") == true || 
                    url?.contains("daffodilvarsity.edu.bd") == true) {
                    return false
                }
                
                return false // Handle all URLs in WebView
            }
            
            override fun shouldInterceptRequest(view: WebView?, request: WebResourceRequest?): WebResourceResponse? {
                // Add more aggressive headers to bypass 403 restrictions
                request?.let { req ->
                    if (req.url.host?.contains("diu.edu.bd") == true) {
                        try {
                            val url = req.url.toString()
                            val connection = java.net.URL(url).openConnection()
                            
                            // Add comprehensive headers to mimic desktop browser
                            connection.setRequestProperty("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/120.0.0.0 Safari/537.36")
                            connection.setRequestProperty("Accept", "text/html,application/xhtml+xml,application/xml;q=0.9,image/webp,*/*;q=0.8")
                            connection.setRequestProperty("Accept-Language", "en-US,en;q=0.9")
                            connection.setRequestProperty("Accept-Encoding", "gzip, deflate, br")
                            connection.setRequestProperty("DNT", "1")
                            connection.setRequestProperty("Connection", "keep-alive")
                            connection.setRequestProperty("Upgrade-Insecure-Requests", "1")
                            connection.setRequestProperty("Sec-Fetch-Dest", "document")
                            connection.setRequestProperty("Sec-Fetch-Mode", "navigate")
                            connection.setRequestProperty("Sec-Fetch-Site", "none")
                            connection.setRequestProperty("Sec-Fetch-User", "?1")
                            connection.setRequestProperty("Cache-Control", "max-age=0")
                            connection.setRequestProperty("Referer", "https://diu.edu.bd/")
                            connection.setRequestProperty("Origin", "https://diu.edu.bd")
                            
                            val inputStream = connection.getInputStream()
                            val mimeType = connection.contentType?.substringBefore(";") ?: "text/html"
                            val encoding = connection.contentType?.substringAfter("charset=", "UTF-8")?.takeIf { it.isNotEmpty() } ?: "UTF-8"
                            
                            return WebResourceResponse(mimeType, encoding, inputStream)
                        } catch (e: Exception) {
                            // Fallback to default handling if custom request fails
                        }
                    }
                }
                return super.shouldInterceptRequest(view, request)
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
                
                // NO JavaScript injection - let the site work naturally
            }
            
            override fun onReceivedError(
                view: WebView?,
                request: WebResourceRequest?,
                error: WebResourceError?
            ) {
                super.onReceivedError(view, request, error)
                if (request?.isForMainFrame == true) {
                    Log.e("WebView", "Error loading page: ${error?.description}")
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
                    Log.e("WebView", "HTTP Error: ${errorResponse?.statusCode} - ${errorResponse?.reasonPhrase}")
                    when (errorResponse?.statusCode) {
                        403 -> onError("Access Forbidden (403) - Try refreshing or check your internet connection")
                        404 -> onError("Page Not Found (404)")
                        500 -> onError("Server Error (500) - Please try again later")
                        else -> onError("HTTP Error: ${errorResponse?.statusCode}")
                    }
                }
            }
            
            override fun onReceivedSslError(view: WebView?, handler: android.webkit.SslErrorHandler?, error: android.net.http.SslError?) {
                // Accept SSL certificates for DIU domains (they might have certificate issues)
                val url = error?.url
                if (url?.contains("diu.edu.bd") == true || url?.contains("daffodilvarsity.edu.bd") == true) {
                    handler?.proceed()
                } else {
                    super.onReceivedSslError(view, handler, error)
                }
            }
            
            private fun updateNavigationState(view: WebView?) {
                onNavigationStateChanged(view?.canGoBack() ?: false, view?.canGoForward() ?: false)
            }
        }
        
        // WebChromeClient for progress and file uploads
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
        
        // Set up download listener (from working version)
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