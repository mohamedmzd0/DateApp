package com.date.fourth.name.ui

import android.annotation.SuppressLint
import android.app.Activity
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.net.Uri
import android.os.*
import android.provider.MediaStore
import android.util.Log
import android.view.View
import android.webkit.ValueCallback
import android.webkit.WebChromeClient
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.ImageView
import android.widget.ProgressBar
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.isVisible
import com.date.fourth.name.Constants
import com.date.fourth.name.R
import com.date.fourth.name.helper.MessageHelper
import com.example.cacso.helper.SharedHelper
import com.google.android.material.button.MaterialButton
import com.sasco.user.helper.NetworkConnectionHelper
import java.io.File
import java.io.IOException
import java.lang.Exception
import java.text.SimpleDateFormat
import java.util.*


class MainActivity : AppCompatActivity() {
    private val FCR = 1
    private var mCM: String? = null
    private var mUM: ValueCallback<Uri>? = null
    private var mUMA: ValueCallback<Array<Uri>>? = null
    lateinit var webView: WebView
    lateinit var noConnection: ImageView
    lateinit var buttonRetry: MaterialButton
    lateinit var broadcastReceiver: InternetMonitorBroadcast
    lateinit var progress: ProgressBar
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        initialComponents()
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            webView.settings.safeBrowsingEnabled = true
        }
        webView.settings.mediaPlaybackRequiresUserGesture = false

        webView.settings.javaScriptCanOpenWindowsAutomatically = false
        webView.settings.allowFileAccess = true
        webView.settings.allowFileAccessFromFileURLs = true
        webView.settings.allowUniversalAccessFromFileURLs = true

        webView.settings.allowContentAccess = true
        webView.webViewClient = WebViewClient()

        webView.webViewClient = object : WebViewClient() {
            override fun shouldOverrideUrlLoading(view: WebView?, url: String?): Boolean {
                if (url != null) {
                    view?.loadUrl(url)
                }
                return true
            }
        }

        webView.loadUrl(SharedHelper.getString(this, SharedHelper.LAST_LINK).toString())


        if (Build.VERSION.SDK_INT >= 21) {
            webView.settings.mixedContentMode = 0
            webView.setLayerType(View.LAYER_TYPE_HARDWARE, null)
        } else
            webView.setLayerType(View.LAYER_TYPE_HARDWARE, null)
        webView.webViewClient = Callback()

        noConnection.isVisible = !NetworkConnectionHelper.isNetworkConnected(this@MainActivity)
        buttonRetry.isVisible = !NetworkConnectionHelper.isNetworkConnected(this@MainActivity)

        webView.webChromeClient = object : WebChromeClient() {
            override fun onProgressChanged(view: WebView?, newProgress: Int) {
                super.onProgressChanged(view, newProgress)
                progress.progress = newProgress
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


//                val intentArray: Array<Intent>
//                intentArray = takePictureIntent?.let { arrayOf(it) } ?: arrayOfNulls<Intent>(0)
                val chooserIntent = Intent(Intent.ACTION_CHOOSER)
                chooserIntent.putExtra(Intent.EXTRA_INTENT, contentSelectionIntent)
                chooserIntent.putExtra(Intent.EXTRA_TITLE, "Choose an Action")
//                chooserIntent.putExtra(Intent.EXTRA_INITIAL_INTENTS, intentArray)
                startActivityForResult(chooserIntent, FCR)
                return true
            }
        }

    }

    private fun initialComponents() {
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

    @Throws(IOException::class)
    private fun createImageFile(): File? {
        @SuppressLint("SimpleDateFormat") val timeStamp: String =
            SimpleDateFormat("yyyyMMdd_HHmmss").format(Date())
        val imageFileName = "img_" + timeStamp + "_"
        val storageDir: File =
            Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES)
        return File.createTempFile(imageFileName, ".jpg", storageDir)
    }


    class Callback : WebViewClient() {
        override fun onReceivedError(
            view: WebView,
            errorCode: Int,
            description: String,
            failingUrl: String
        ) {

//            Toast.makeText(contxt, "Failed loading app!", Toast.LENGTH_SHORT)
//                .show()
        }
    }


    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (Build.VERSION.SDK_INT >= 21) {
            var results: Array<*>? = null
            //Check if response is positive
            if (resultCode == Activity.RESULT_OK) {
                if (requestCode == FCR) {
                    if (null == mUMA) {
                        return
                    }
                    if (intent == null) {
                        //Capture Photo if no image available
                        if (mCM != null) {
                            results = arrayOf(Uri.parse(mCM))
                        }
                    } else {
                        val dataString = intent.dataString
                        if (dataString != null) {
                            results = arrayOf(Uri.parse(dataString))
                        }
                    }
                }
            }
            mUMA?.onReceiveValue(results as Array<Uri>?)
            mUMA = null
        } else {
            if (requestCode == FCR) {
                if (null == mUM) return
                val result =
                    if (intent == null || resultCode != Activity.RESULT_OK) null else intent.data
                mUM!!.onReceiveValue(result)
                mUM = null
            }
        }
    }

    var doubleBackToExitPressedOnce = false

    override fun onBackPressed() {
        if (webView.canGoBack())
            webView.goBack()
        else {
            if (doubleBackToExitPressedOnce) {
                finish()
                return
            }

            doubleBackToExitPressedOnce = true
            MessageHelper.toast(this, resources.getString(R.string.double_tab_exit))
            Handler(Looper.myLooper()!!).postDelayed(
                { doubleBackToExitPressedOnce = false },
                2000
            )
        }
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
            webView.reload()

        }

    }
}