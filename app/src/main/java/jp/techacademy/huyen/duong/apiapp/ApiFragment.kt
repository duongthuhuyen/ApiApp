package jp.techacademy.huyen.duong.apiapp

import android.annotation.SuppressLint
import android.content.Context
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.squareup.moshi.Moshi
import jp.techacademy.huyen.duong.apiapp.databinding.FragmentApiBinding
import okhttp3.*
import okhttp3.logging.HttpLoggingInterceptor
import java.io.IOException

class ApiFragment : Fragment() {
    private var _binding: FragmentApiBinding? = null
    private val binding get() = _binding!!

    private val apiAdapter by lazy { ApiAdapter() }
    private val handler = Handler(Looper.getMainLooper())

    // Fragment -> Activity にFavoriteの変更を通知する
    private var fragmentCallback: FragmentCallback? = null

    // -----追加ここから
    // 一覧に表示するShopを保持
    private var list = mutableListOf<FavoriteShop>()

    // 現在のページ
    private var page = 0

    // Apiでデータを読み込み中ですフラグ。追加ページの読み込みの時にこれがないと、連続して読み込んでしまうので、それの制御のため
    private var isLoading = false
    // -----追加ここまで

    override fun onAttach(context: Context) {
        super.onAttach(context)
        if (context is FragmentCallback) {
            fragmentCallback = context
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentApiBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        // ここから初期化処理を行う
        // ApiAdapterのお気に入り追加、削除用のメソッドの追加を行う
        apiAdapter.apply {
            // Adapterの処理をそのままActivityに通知する
            onClickAddFavorite = {
                fragmentCallback?.onAddFavorite(it)
            }
            // Adapterの処理をそのままActivityに通知する
            onClickDeleteFavorite = {
                fragmentCallback?.onDeleteFavorite(it.id)
            }
            // Itemをクリックしたとき
            onClickItem = {
                fragmentCallback?.onClickItem(it)
            }
        }

        // RecyclerViewの初期化
        binding.recyclerView.apply {
            adapter = apiAdapter
            layoutManager = LinearLayoutManager(requireContext()) // 一列ずつ表示

            // -----追加ここから
            // Scrollを検知するListenerを実装する。これによって、RecyclerViewの下端に近づいた時に次のページを読み込んで、下に付け足す
            addOnScrollListener(object : RecyclerView.OnScrollListener() {
                // dx はx軸方向の変化量(横) dy はy軸方向の変化量(縦) ここではRecyclerViewは縦方向なので、dyだけ考慮する
                override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                    super.onScrolled(recyclerView, dx, dy)

                    // 縦方向の変化量(スクロール量)が0の時は動いていないので何も処理はしない
                    if (dy == 0) {
                        return
                    }

                    // RecyclerViewの現在の表示アイテム数
                    val totalCount = apiAdapter.itemCount

                    // RecyclerViewの現在見えている最後のViewHolderのposition
                    val lastVisibleItem =
                        (layoutManager as LinearLayoutManager).findLastVisibleItemPosition()

                    // totalCountとlastVisibleItemから全体のアイテム数のうちどこまでが見えているかがわかる
                    // (例:totalCountが20、lastVisibleItemが15の時は、現在のスクロール位置から下に5件見えていないアイテムがある)
                    // 一番下にスクロールした時に次の20件を表示する等の実装が可能になる。
                    // ユーザビリティを考えると、一番下にスクロールしてから追加した場合、一度スクロールが止まるので、ユーザーは気付きにくい
                    // ここでは、一番下から5番目を表示した時に追加読み込みする様に実装する
                    if (!isLoading && lastVisibleItem >= totalCount - 6) { // 読み込み中でない、かつ、現在のスクロール位置から下に5件見えていないアイテムがある
                        updateData(true)
                    }
                }
            })
            // -----追加ここまで
        }
        binding.swipeRefreshLayout.setOnRefreshListener {
            updateData()
        }
        updateData()
    }

    /**
     * お気に入りが削除されたときの処理（Activityからコールされる）
     */
    fun updateView() {
        // RecyclerViewのAdapterに対して再描画のリクエストをする
        apiAdapter.notifyItemRangeChanged(0, apiAdapter.itemCount)
    }

    // -----変更ここから
    private fun updateData(isAdd: Boolean = false) {
        // 読み込み中なら処理を行わずに終了
        if (isLoading) {
            return
        } else {
            isLoading = true
        }
        // 追加モードならページを加算
        if (isAdd) {
            page++
        } else {
            page = 0
            list.clear()
        }
        // 開始位置を計算
        val start = page * COUNT + 1
        var data = FavoriteShop.findAll()
        Log.d("HIII",""+data.size)
        handler.post() {
            updateRecyclerView(data)
        }
        isLoading = false
    }
    // -----変更ここまで

    private fun updateRecyclerView(list: List<FavoriteShop>) {
        apiAdapter.submitList(list)
        // SwipeRefreshLayoutのくるくるを消す
        binding.swipeRefreshLayout.isRefreshing = false
    }

    companion object {
        // 1回のAPIで取得する件数
        private const val COUNT = 20
    }
}