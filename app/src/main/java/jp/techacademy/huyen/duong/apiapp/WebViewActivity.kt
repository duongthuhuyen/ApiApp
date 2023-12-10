package jp.techacademy.huyen.duong.apiapp


import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.activity.addCallback
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import jp.techacademy.huyen.duong.apiapp.databinding.ActivityWebViewBinding
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import okhttp3.internal.notify
import okhttp3.internal.notifyAll

class WebViewActivity : AppCompatActivity() {
    private lateinit var binding: ActivityWebViewBinding
    private var statusStar: String = ""
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityWebViewBinding.inflate(layoutInflater)
        setContentView(binding.root)
        val shop = intent.getStringArrayListExtra(KEY_SHOP)
        onBackPressedDispatcher.addCallback {
            mainActivityBack()
        }
        shop?.let {
            binding.webView.loadUrl(shop[3])
            binding.favoriteImageView.apply {
                // お気に入り状態を取得
                if (shop?.size == 4) {
                    //var favorite: FavoriteShop? = FavoriteShop.findBy(shop[0])
                    //if(favorite!= null) {
                    //Log.d("Helllo", "" + favorite.id)
                    var isFavorite = FavoriteShop.findBy(shop[0])
                    Log.d("HiHello",""+isFavorite)
                    setImageResource(if (isFavorite == 1) R.drawable.ic_star else R.drawable.ic_star_border)
                    setOnClickListener {
                        if (isFavorite == 1) {
                            showConfirmDeleteFavoriteDialog(shop[0])
                            statusStar = DELETE
                            isFavorite = 0
                        } else if(isFavorite == 0){
                            CoroutineScope(Dispatchers.Default).launch {
                                FavoriteShop.insert(shop[0])
                            }
                            setImageResource(R.drawable.ic_star)
                            statusStar = ADD
                            isFavorite = 1
                        }
                        //}
                    }
                }
            }
        }
        binding.buton.setOnClickListener(
        ) {
            mainActivityBack()
        }
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
        CoroutineScope(Dispatchers.Default).launch {
            FavoriteShop.delete(id)
        }
        binding.favoriteImageView.setImageResource(R.drawable.ic_star_border)
    }

    fun mainActivityBack() {
        Log.d("STATUSStart", statusStar)
        val intentSub = Intent()
        intentSub.putExtra(KEY_RESULT, statusStar)
        setResult(RESULT_OK, intentSub)
        finish()
    }
}

const val KEY_RESULT = "key_result"
const val DELETE = "delete"
const val ADD = "add"
const val KEY_SHOP = "key_shop"