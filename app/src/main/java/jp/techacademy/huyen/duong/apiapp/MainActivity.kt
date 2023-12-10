package jp.techacademy.huyen.duong.apiapp

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.addCallback
import androidx.activity.result.ActivityResult
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.viewpager2.widget.ViewPager2
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayoutMediator
import com.squareup.moshi.Moshi
import jp.techacademy.huyen.duong.apiapp.databinding.ActivityMainBinding
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import okhttp3.*
import okhttp3.logging.HttpLoggingInterceptor
import java.io.IOException


class MainActivity : AppCompatActivity(), FragmentCallback {
    private lateinit var binding: ActivityMainBinding

    private val viewPagerAdapter by lazy { ViewPagerAdapter(this) }
    //異なるActivity間で通信
    var resultLauncher: ActivityResultLauncher<Intent> = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result: ActivityResult ->
        if (result.resultCode == RESULT_OK) {
            val intent = result.data
            if (intent != null) {
                val res = intent.getStringExtra(KEY_RESULT).toString()
                Log.d("STATUSStartMain", res.toString())
                if (res == DELETE || res == ADD) {
                    (viewPagerAdapter.fragments[VIEW_PAGER_POSITION_API] as ApiFragment).updateView()
                    (viewPagerAdapter.fragments[VIEW_PAGER_POSITION_FAVORITE] as FavoriteFragment).updateData()
                }
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {

//        val url = StringBuilder()
//            .append(getString(R.string.base_url)) // https://webservice.recruit.co.jp/hotpepper/gourmet/v1/
//            .append("?key=").append(getString(R.string.api_key)) // Apiを使うためのApiKey
//            .append("&start=").append(1) // 何件目からのデータを取得するか
//            .append("&count=").append(199) // 1回で20件取得する
//            .append("&keyword=")
//            .append(getString(R.string.api_keyword)) // お店の検索ワード。ここでは例として「ランチ」を検索
//            .append("&format=json") // ここで利用しているAPIは戻りの形をxmlかjsonが選択することができる。Androidで扱う場合はxmlよりもjsonの方が扱いやすいので、jsonを選択
//            .toString()
//        val client = OkHttpClient.Builder()
//            .addInterceptor(HttpLoggingInterceptor().apply {
//                level = HttpLoggingInterceptor.Level.BODY
//            })
//            .build()
//        val request = Request.Builder()
//            .url(url)
//            .build()
//        client.newCall(request).enqueue(object : Callback {
//            override fun onFailure(call: Call, e: IOException) { // Error時の処理
//                e.printStackTrace()
//
//            }
//
//            override fun onResponse(call: Call, response: Response) { // 成功時の処理
//                // Jsonを変換するためのAdapterを用意
//                val moshi = Moshi.Builder().build()
//                val jsonAdapter = moshi.adapter(ApiResponse::class.java)
//
//                response.body?.string()?.also {
//                    val apiResponse = jsonAdapter.fromJson(it)
//                    if (apiResponse != null) {
//                        Log.d("DATAAPI",""+apiResponse.results.shop.size)
//                        var data = mutableListOf<FavoriteShop>()
//                        for (s in apiResponse.results.shop) {
//                            var favoriteShop = FavoriteShop(
//                                s.id,
//                                s.logoImage,
//                                s.name,
//                                s.couponUrls.pc.ifEmpty { s.couponUrls.sp },
//                                0
//                            )
//                            data.add(favoriteShop)
//                        }
//
//                        if (data.size > 0) {
//                            FavoriteShop.insertAll(data)
//                        }
//                       // dataList+= apiResponse.results.shop
//                    }
//                }
//            }
//        })
        //Log.d("DataList",""+ dataList.size)
        //---------------------------------------------------
        Log.d("MainActivity", "Oncreate")
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        // ViewPager2の初期化
        binding.viewPager2.apply {
            adapter = viewPagerAdapter
            // スワイプの向き横（ORIENTATION_VERTICAL を指定すれば縦スワイプで実装可能です）
            orientation =
                ViewPager2.ORIENTATION_HORIZONTAL
            // ViewPager2で保持する画面数
            offscreenPageLimit = viewPagerAdapter.itemCount
        }

        // TabLayoutの初期化
        // TabLayoutとViewPager2を紐づける
        // TabLayoutのTextを指定する
        TabLayoutMediator(binding.tabLayout, binding.viewPager2) { tab, position ->
            tab.setText(viewPagerAdapter.titleIds[position])
        }.attach()

        binding.tabLayout.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {
            // タブが選択された際に呼ばれる
            override fun onTabSelected(tab: TabLayout.Tab) {
                showFavoriteTabInfo(tab)
            }

            override fun onTabUnselected(tab: TabLayout.Tab) {}
            override fun onTabReselected(tab: TabLayout.Tab) {}
        })
    }

    /**
     * お気に入りタブにトーストを表示
     */
    private fun showFavoriteTabInfo(tab: TabLayout.Tab) {
        if (tab.position == VIEW_PAGER_POSITION_FAVORITE && FavoriteShop.findAll()
                .isEmpty()
        ) {
            Toast.makeText(this@MainActivity, R.string.empty_favorite, Toast.LENGTH_SHORT)
                .show()
        }
    }

    override fun onClickItem(shop: ArrayList<String>) {
        // WebViewActivity.start(this, shop)
        val intent = Intent(this, WebViewActivity::class.java)
        intent.putExtra(
            KEY_SHOP,
            shop
        )
        resultLauncher.launch(intent)
    }

    /**
     * Favoriteに追加するときのメソッド(Fragment -> Activity へ通知する)
     */
    override fun onAddFavorite(shop: FavoriteShop) {
        CoroutineScope(Dispatchers.Default).launch {
            FavoriteShop.insert(shop.id)
           // finish()
        }
        (viewPagerAdapter.fragments[VIEW_PAGER_POSITION_FAVORITE] as FavoriteFragment).updateData()
    }

    /**
     * Favoriteから削除するときのメソッド(Fragment -> Activity へ通知する)
     */
    override fun onDeleteFavorite(id: String) {
        showConfirmDeleteFavoriteDialog(id)
    }

    private fun showConfirmDeleteFavoriteDialog(id: String) {
        AlertDialog.Builder(this)
            .setTitle(R.string.delete_favorite_dialog_title)
            .setMessage(R.string.delete_favorite_dialog_message)
            .setPositiveButton(android.R.string.ok) { _, _ ->
                deleteFavorite(id)
                if (binding.tabLayout.selectedTabPosition == VIEW_PAGER_POSITION_FAVORITE) {
                    showFavoriteTabInfo(binding.tabLayout.getTabAt(binding.tabLayout.selectedTabPosition)!!)
                }
            }
            .setNegativeButton(android.R.string.cancel) { _, _ -> }
            .create()
            .show()
    }

    private fun deleteFavorite(id: String) {
        CoroutineScope(Dispatchers.Default).launch {
            FavoriteShop.delete(id)
            //finish()
        }
        (viewPagerAdapter.fragments[VIEW_PAGER_POSITION_API] as ApiFragment).updateView()
        (viewPagerAdapter.fragments[VIEW_PAGER_POSITION_FAVORITE] as FavoriteFragment).updateData()
    }

    companion object {
        private const val VIEW_PAGER_POSITION_API = 0
        private const val VIEW_PAGER_POSITION_FAVORITE = 1
        //var dataList = listOf<Shop>()
    }
}
