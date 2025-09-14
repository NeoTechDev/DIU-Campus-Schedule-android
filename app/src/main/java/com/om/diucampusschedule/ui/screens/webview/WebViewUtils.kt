package com.om.diucampusschedule.ui.screens.webview

import android.annotation.SuppressLint
import android.app.DownloadManager
import android.content.Context
import android.net.Uri
import android.net.http.SslError
import android.os.Build
import android.os.Environment
import android.util.Log
import android.webkit.CookieManager
import android.webkit.GeolocationPermissions
import android.webkit.PermissionRequest
import android.webkit.SslErrorHandler
import android.webkit.URLUtil
import android.webkit.ValueCallback
import android.webkit.WebChromeClient
import android.webkit.WebResourceError
import android.webkit.WebResourceRequest
import android.webkit.WebSettings
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.Toast

/**
 * Configures a WebView with settings optimized for compatibility,
 * including a switchable desktop/mobile mode to handle sites like Cloudflare.
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
        layoutParams = android.view.ViewGroup.LayoutParams(
            android.view.ViewGroup.LayoutParams.MATCH_PARENT,
            android.view.ViewGroup.LayoutParams.MATCH_PARENT
        )

        settings.apply {
            // Core essentials for modern web compatibility
            javaScriptEnabled = true
            domStorageEnabled = true
            databaseEnabled = true
            cacheMode = WebSettings.LOAD_DEFAULT

            // Content and file access
            allowFileAccess = true
            allowContentAccess = true
            javaScriptCanOpenWindowsAutomatically = true
            mediaPlaybackRequiresUserGesture = false
            setSupportMultipleWindows(true)

            // Zoom is useful in both modes
            setSupportZoom(true)
            builtInZoomControls = true
            displayZoomControls = false

            // Conditional Desktop/Mobile Mode Logic
            if (isDesktopMode) {
                // --- DESKTOP MODE SETTINGS ---
                userAgentString = "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/125.0.0.0 Safari/537.36"
                useWideViewPort = true
                loadWithOverviewMode = true
            } else {
                // --- MOBILE MODE SETTINGS ---
                userAgentString = null // Use WebView's default mobile user agent
                useWideViewPort = false
                loadWithOverviewMode = true
            }

            // Security and mixed content
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                mixedContentMode = WebSettings.MIXED_CONTENT_ALWAYS_ALLOW
            }

            // Enable remote debugging in debug builds
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
                WebView.setWebContentsDebuggingEnabled(true)
            }
        }

        // --- Cookie Management ---
        // Ensure cookies are enabled and persisted to stay logged in and pass challenges.
        CookieManager.getInstance().setAcceptCookie(true)
        CookieManager.getInstance().setAcceptThirdPartyCookies(this, true)

        // --- WebViewClient ---
        // Handles navigation, errors, and page events.
        webViewClient = object : WebViewClient() {
            override fun onPageFinished(view: WebView?, url: String?) {
                super.onPageFinished(view, url)
                url?.let { onUrlChanged(it) }
                CookieManager.getInstance().flush() // Persist cookies
                onNavigationStateChanged(view?.canGoBack() ?: false, view?.canGoForward() ?: false)
            }

            override fun onReceivedSslError(view: WebView?, handler: SslErrorHandler?, error: SslError?) {
                // This is a security risk, but can be necessary for sites with certificate issues.
                // Only proceed for trusted domains.
                val url = error?.url
                if (url?.contains("diu.edu.bd") == true || url?.contains("daffodilvarsity.edu.bd") == true) {
                    handler?.proceed()
                } else {
                    super.onReceivedSslError(view, handler, error)
                }
            }

            override fun onReceivedError(view: WebView?, request: WebResourceRequest?, error: WebResourceError?) {
                super.onReceivedError(view, request, error)
                if (request?.isForMainFrame == true && Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    Log.e("WebViewError", "Error: ${error?.description}")
                    onError(error?.description?.toString() ?: "An unknown error occurred")
                }
            }
        }

        // --- WebChromeClient ---
        // Handles UI-related events like progress, alerts, and file uploads.
        webChromeClient = object : WebChromeClient() {
            override fun onProgressChanged(view: WebView?, newProgress: Int) {
                super.onProgressChanged(view, newProgress)
                onLoadingProgressChanged(newProgress)
            }

            override fun onReceivedTitle(view: WebView?, title: String?) {
                super.onReceivedTitle(view, title)
                onTitleChanged(title)
            }

            override fun onShowFileChooser(
                webView: WebView?,
                filePathCallback: ValueCallback<Array<Uri>>?,
                fileChooserParams: FileChooserParams?
            ): Boolean {
                filePathCallback?.let { onFileUpload(it) }
                return true
            }

            override fun onGeolocationPermissionsShowPrompt(origin: String?, callback: GeolocationPermissions.Callback?) {
                callback?.invoke(origin, true, false)
            }

            override fun onPermissionRequest(request: PermissionRequest?) {
                request?.grant(request.resources)
            }
        }

        // --- Download Listener ---
        setDownloadListener { url, _, contentDisposition, mimeType, _ ->
            try {
                val filename = URLUtil.guessFileName(url, contentDisposition, mimeType)
                onDownload(url, filename)
            } catch (e: Exception) {
                Log.e("WebViewDownload", "Download error", e)
                Toast.makeText(context, "Download failed: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }
}

/**
 * Initiates a file download using Android's DownloadManager.
 */
fun downloadFile(context: Context, url: String, filename: String) {
    try {
        val request = DownloadManager.Request(Uri.parse(url)).apply {
            setDestinationInExternalPublicDir(Environment.DIRECTORY_DOWNLOADS, filename)
            setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED)
            setTitle(filename)
            setDescription("Downloading File")
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
        Log.e("WebViewDownload", "Download failed", e)
        Toast.makeText(context, "Download failed: ${e.message}", Toast.LENGTH_SHORT).show()
    }
}