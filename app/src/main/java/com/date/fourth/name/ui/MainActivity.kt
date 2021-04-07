package com.date.fourth.name.ui

import android.Manifest
import android.annotation.TargetApi
import android.app.Activity
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageManager
import android.net.Uri
import android.os.*
import android.provider.MediaStore
import android.util.Log
import android.view.View
import android.view.WindowManager
import android.webkit.*
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.ProgressBar
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import com.appsflyer.AppsFlyerConversionListener
import com.appsflyer.AppsFlyerLib
import com.date.fourth.name.Constants
import com.date.fourth.name.R
import com.example.cacso.helper.SharedHelper
import com.google.android.material.button.MaterialButton
import com.sasco.user.helper.NetworkConnectionHelper
import java.io.File
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.*

private const val TAG = "MainActivity"

class MainActivity : AppCompatActivity() {
    private val FCR = 1
    private var mCM: String? = null
    private var mUM: ValueCallback<Uri>? = null
    private var mUMA: ValueCallback<Array<Uri>>? = null
    lateinit var webView: WebView
    lateinit var noConnection: ImageView
    lateinit var spLogo: ImageView
    lateinit var spBk: FrameLayout
    lateinit var buttonRetry: MaterialButton
    lateinit var broadcastReceiver: InternetMonitorBroadcast
    lateinit var progress: ProgressBar
    lateinit var handler: Handler
    lateinit var runnable: Runnable
    private val SPLASH_TIME: Long = 2000
    private var UID: String? = null
    val INPUT_FILE_REQUEST_CODE = 1
    private var mFilePathCallback: ValueCallback<Array<Uri?>?>? = null
    private var mCameraPhotoPath: String? = null


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        animateUI()

        initialComponents()
        if (SharedHelper.getString(this@MainActivity, SharedHelper.LAST_LINK).isNullOrEmpty())
            initialAppsFlyer()
        else
            initialWebView(SharedHelper.getString(this, SharedHelper.LAST_LINK).toString())

    }

    private fun animateUI() {

        spLogo = findViewById<ImageView>(R.id.spLogo)
        spBk = findViewById<FrameLayout>(R.id.spBk)

        spLogo.animate().alpha(1f).scaleX(1f).scaleY(1f).setDuration(1000)
            .setStartDelay(1000).start()
        handler = Handler(Looper.myLooper()!!)
        runnable = Runnable {
            if (UID != null || !SharedHelper.getString(this@MainActivity, SharedHelper.LAST_LINK)
                    .isNullOrEmpty()
            ) {
                spLogo.visibility = View.GONE
                spBk.visibility = View.GONE
            }
        }
        handler.postDelayed(runnable, SPLASH_TIME)

    }

    private fun initialWebView(url: String) {
        SharedHelper.saveString(this@MainActivity, SharedHelper.LAST_LINK, url)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            webView.settings.safeBrowsingEnabled = true
        }


        webView.settings.setJavaScriptEnabled(true)
        webView.settings.setUseWideViewPort(true)
        webView.settings.setDomStorageEnabled(true)
        webView.settings.setMediaPlaybackRequiresUserGesture(false)
        setUpWebViewDefaults(webView)
        webView.setWebChromeClient(MyWebChromeClient())
        webView.setWebViewClient(object : WebViewClient() {
            override fun shouldOverrideUrlLoading(
                webview: WebView,
                url: String?
            ): Boolean {
                webview.loadUrl(url!!)
                return true
            }

            override fun onPageFinished(view: WebView?, url: String?) {
                super.onPageFinished(view, url)
            }
        })
        // Load the local index.html file

        // Load the local index.html file
        webView.loadUrl(url)

    }

    private fun initialAppsFlyer() {
        AppsFlyerLib.getInstance()
            .init(Constants.APPS_FLYER_ID, object : AppsFlyerConversionListener {
                override fun onConversionDataSuccess(data: MutableMap<String, Any>?) {

                    UID = AppsFlyerLib.getInstance().getAppsFlyerUID(this@MainActivity)
                    Log.d(
                        TAG,
                        "onConversionDataSuccess: " + data.toString()
                    )

                    data?.let {

                        val af_status = it.get("af_status") as String
                        Log.d(TAG, "onConversionDataSuccess: af_staus $af_status")
                        runOnUiThread {
                            if (spLogo.visibility != View.GONE) {
                                spLogo.visibility = View.GONE
                                spBk.visibility = View.GONE
                            }
                            if (af_status.trim().toLowerCase().contentEquals("organic")) {
                                Log.d(TAG, "onConversionDataSuccess: equal ")
                                initialWebView(
                                    Constants.BASE_URL + AppsFlyerLib.getInstance()
                                        .getAppsFlyerUID(this@MainActivity) +
                                            "&source=organic&campaign=organic&adset=organic"
                                )
                            } else {
                                Log.d(TAG, "onConversionDataSuccess: not equal")

                                initialWebView(
                                    Constants.BASE_URL + AppsFlyerLib.getInstance()
                                        .getAppsFlyerUID(this@MainActivity) +
                                            "&source=${it.get("source")}&campaign=${it.get("campaign")}&adset=${it.get(
                                                "adset"
                                            )}"
                                )
                            }
                        }
                    }
                    AppsFlyerLib.getInstance().stop(true, this@MainActivity)

                }

                override fun onConversionDataFail(error: String?) {
                    Log.e(TAG, "error onAttributionFailure :  $error")
                }

                override fun onAppOpenAttribution(data: MutableMap<String, String>?) {
                    Log.d(TAG, "onAppOpenAttribution: ")
                    Log.d(TAG, "onAppOpenAttribution: " + data?.toString())
                    data?.map {
                        Log.d(TAG, "onAppOpen_attribute: ${it.key} = ${it.value}")
                    }
                }

                override fun onAttributionFailure(error: String?) {
                    Log.e(TAG, "error onAttributionFailure :  $error")
                }
            }, this)
        AppsFlyerLib.getInstance().start(this)

    }

    private fun initialComponents() {
        Log.d(TAG, "initialComponents: ")
        broadcastReceiver = InternetMonitorBroadcast()
        registerReceiver(broadcastReceiver, IntentFilter("android.net.conn.CONNECTIVITY_CHANGE"))
        webView = findViewById<WebView>(R.id.webView)
        noConnection = findViewById<ImageView>(R.id.noConnection)
        buttonRetry = findViewById<MaterialButton>(R.id.buttonRetry)
        progress = findViewById(R.id.progress_circular)
        webView.settings.javaScriptEnabled = true
        webView.settings.databaseEnabled = true
        webView.settings.domStorageEnabled = true
        buttonRetry.setOnClickListener { webView.reload() }

    }


    override fun onBackPressed() {
        if (webView.canGoBack())
            webView.goBack()
    }

    override fun onDestroy() {
        try {
            unregisterReceiver(broadcastReceiver)
        } catch (ex: Exception) {
            //nothing
        }
        super.onDestroy()
    }

    inner class InternetMonitorBroadcast : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            if (UID == null && SharedHelper.getString(this@MainActivity, SharedHelper.LAST_LINK)
                    .isNullOrEmpty()
            )
                initialAppsFlyer()
            else
                webView.reload()

        }

    }


    fun createImageFile(): File? {
        val timeStamp = SimpleDateFormat("yyyyMMdd_HHmmss").format(Date())
        val imageFileName = "JPEG_" + timeStamp + "_"
        val storageDir = Environment.getExternalStoragePublicDirectory(
            Environment.DIRECTORY_PICTURES
        )
        return File.createTempFile(
            imageFileName,
            ".jpg",
            storageDir
        )
    }


    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    private fun setUpWebViewDefaults(webView: WebView) {
        val settings: WebSettings = webView.settings

        // Enable Javascript
        settings.setJavaScriptEnabled(true)

        // Use WideViewport and Zoom out if there is no viewport defined
        settings.setUseWideViewPort(true)
        settings.setLoadWithOverviewMode(true)

        settings.setBuiltInZoomControls(true)
        if (Build.VERSION.SDK_INT > Build.VERSION_CODES.HONEYCOMB) {
            settings.setDisplayZoomControls(false)
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            WebView.setWebContentsDebuggingEnabled(true)
        }

        webView.setWebViewClient(WebViewClient())
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (requestCode != INPUT_FILE_REQUEST_CODE || mFilePathCallback == null) {
            super.onActivityResult(requestCode, resultCode, data)
            return
        }
        var results: Array<Uri?>? = null

        // Check that the response is a good one
        if (resultCode == Activity.RESULT_OK) {
            if (data == null) {
                // If there is not data, then we may have taken a photo
                if (mCameraPhotoPath != null) {
                    results = arrayOf(Uri.parse(mCameraPhotoPath))
                }
            } else {
                val dataString = data.dataString
                if (dataString != null) {
                    results = arrayOf(Uri.parse(dataString))
                }
            }
        }
        mFilePathCallback?.onReceiveValue(results)
        mFilePathCallback = null
        return
    }


    inner class MyWebChromeClient : WebChromeClient() {
        @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
        override fun onPermissionRequest(request: PermissionRequest) {
            val requestedResources: Array<String> = request.getResources()
            for (r in requestedResources) {
                if (r == PermissionRequest.RESOURCE_VIDEO_CAPTURE) {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                        if (ContextCompat.checkSelfPermission(
                                this@MainActivity, Manifest.permission.CAMERA
                            ) ==
                            PackageManager.PERMISSION_GRANTED
                            &&
                            ContextCompat.checkSelfPermission(
                                this@MainActivity, Manifest.permission.RECORD_AUDIO
                            ) ==
                            PackageManager.PERMISSION_GRANTED
                        ) {

                            request.grant(
                                arrayOf<String>(
                                    PermissionRequest.RESOURCE_VIDEO_CAPTURE,
                                    PermissionRequest.RESOURCE_AUDIO_CAPTURE
                                )
                            )
                        } else

                            requestPermissions(
                                arrayOf<String>(
                                    Manifest.permission.CAMERA,
                                    Manifest.permission.RECORD_AUDIO
                                ), 456
                            )
                    }
                    break
                }
            }
        }

        override fun onProgressChanged(view: WebView?, newProgress: Int) {

            if (newProgress >= 90)
                progress.visibility = View.GONE
            else progress.visibility = View.VISIBLE

            SharedHelper.saveString(
                this@MainActivity,
                SharedHelper.LAST_LINK,
                webView.url.toString()
            )
            if (newProgress > 95) {
                buttonRetry.isVisible =
                    !NetworkConnectionHelper.isNetworkConnected(this@MainActivity)
                noConnection.isVisible =
                    !NetworkConnectionHelper.isNetworkConnected(this@MainActivity)
            }
        }

        override fun onShowFileChooser(
            webView: WebView, filePathCallback: ValueCallback<Array<Uri>>,
            fileChooserParams: FileChooserParams
        ): Boolean {
            if (mUMA != null) {
                mUMA!!.onReceiveValue(null)
            }
            mUMA = filePathCallback
            var takePictureIntent: Intent? = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
            if (getPackageManager()
                    ?.let { takePictureIntent!!.resolveActivity(it) } != null
            ) {
                var photoFile: File? = null
                try {
                    photoFile = createImageFile()
                    takePictureIntent?.putExtra("PhotoPath", mCM)
                } catch (ex: IOException) {
                    Log.e("TAG", "Image file creation failed", ex)
                }
                if (photoFile != null) {
                    mCM = "file:" + photoFile.getAbsolutePath()
                    takePictureIntent?.putExtra(
                        MediaStore.EXTRA_OUTPUT,
                        Uri.fromFile(photoFile)
                    )
                } else {
                    takePictureIntent = null
                }
            }
            val contentSelectionIntent = Intent(Intent.ACTION_GET_CONTENT)
            contentSelectionIntent.addCategory(Intent.CATEGORY_OPENABLE)
            contentSelectionIntent.type = "*/*"

            val chooserIntent = Intent(Intent.ACTION_CHOOSER)
            chooserIntent.putExtra(Intent.EXTRA_INTENT, contentSelectionIntent)
            chooserIntent.putExtra(Intent.EXTRA_TITLE, "Choose an Action")
            startActivityForResult(chooserIntent, FCR)
            return true
        }

    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {

        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == 456 && grantResults.isNotEmpty()) {
            webView.setWebChromeClient(MyWebChromeClient())
            webView.reload()

        }
    }
}