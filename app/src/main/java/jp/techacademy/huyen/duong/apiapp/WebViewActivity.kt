package jp.techacademy.huyen.duong.apiapp


import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import jp.techacademy.huyen.duong.apiapp.databinding.ActivityWebViewBinding

class WebViewActivity : AppCompatActivity() {
    private lateinit var binding: ActivityWebViewBinding
    private var statusStar: String = ""
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityWebViewBinding.inflate(layoutInflater)
        setContentView(binding.root)
        val shop = intent.getStringArrayListExtra(KEY_SHOP)
        shop?.let {
            binding.webView.loadUrl(shop[3])
            binding.favoriteImageView.apply {
                // お気に入り状態を取得
                if (shop?.size == 4) {
                    val isFavorite = FavoriteShop.findBy(shop[0]) != null
                    setImageResource(if (isFavorite) R.drawable.ic_star else R.drawable.ic_star_border)
                    setOnClickListener {
                        if (isFavorite) {
                            showConfirmDeleteFavoriteDialog(shop[0])
                            statusStar = DELETE
                        } else {
                            //onClickDeleteFavorite?.invoke(shop)
                            FavoriteShop.insert(FavoriteShop().apply {
                                id = shop[0]
                                name = shop[1]
                                imageUrl = shop[2]
                                url = shop[3]
                            })
                            setImageResource(R.drawable.ic_star)
                            statusStar = ADD
                        }
                        if ((isFavorite && statusStar == ADD)||(!isFavorite && statusStar == DELETE)) {
                            statusStar = ""
                        }
                    }
                }
            }
        }
       // Log.d("STATUSStar", statusStar)
    }

    override fun onStop() {
        super.onStop()
        Log.d("WEBVIEW","onStop")
    }

    override fun onDestroy() {
        super.onDestroy()

    }

    override fun onBackPressed() {
        super.onBackPressed()
        Log.d("WEBVIEWSTAR","BackPress")
        Log.d("STATUSStart", statusStar)
        val intentSub = Intent()
        intentSub.putExtra(KEY_RESULT, statusStar)
        setResult(RESULT_OK, intentSub)
        finish()
    }

    private fun showConfirmDeleteFavoriteDialog(id: String) {
        AlertDialog.Builder(this)
            .setTitle(R.string.delete_favorite_dialog_title)
            .setMessage(R.string.delete_favorite_dialog_message)
            .setPositiveButton(android.R.string.ok) { _, _ ->
                deleteFavorite(id)
            }
            .setNegativeButton(android.R.string.cancel) { _, _ -> }
            .create()
            .show()
    }
    private fun deleteFavorite(id: String) {
        FavoriteShop.delete(id)
        binding.favoriteImageView.setImageResource(R.drawable.ic_star_border)
    }
//    companion object {
//        //private const val KEY_SHOP = "key_shop"
//        fun start(activity: Activity, shop: ArrayList<String>) {
//            val intent = Intent(activity, WebViewActivity::class.java)
//            activity.startActivity(
//                intent.putExtra(
//                    KEY_SHOP,
//                    shop
//                )
//            )
//
//        }
//    }
}
const val KEY_RESULT = "key_result"
const val DELETE ="delete"
const val ADD = "add"
const val KEY_SHOP = "key_shop"