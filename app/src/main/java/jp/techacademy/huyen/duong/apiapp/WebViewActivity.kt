package jp.techacademy.huyen.duong.apiapp


import android.app.Activity
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import jp.techacademy.huyen.duong.apiapp.databinding.ActivityWebViewBinding

class WebViewActivity : AppCompatActivity() {
    private lateinit var binding: ActivityWebViewBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityWebViewBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.webView.loadUrl(intent.getStringExtra(KEY_URL).toString())
    }

    companion object {
        private const val KEY_URL = "key_url"
        fun start(activity: Activity, url: String) {
            activity.startActivity(
                Intent(activity, WebViewActivity::class.java).putExtra(
                    KEY_URL,
                    url
                )
            )
        }
    }
}