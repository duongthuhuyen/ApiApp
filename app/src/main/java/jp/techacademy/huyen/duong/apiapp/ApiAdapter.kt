package jp.techacademy.huyen.duong.apiapp

import android.annotation.SuppressLint
import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.squareup.picasso.Picasso
import jp.techacademy.huyen.duong.apiapp.databinding.RecyclerFavoriteBinding

/**
 * RecyclerView用Adapter
 * 第一引数: データを保持するクラス。今回はShop
 * 第二引数: リスト内の1行の内容を保持するViewHolder。今回はApiItemViewHolder
 */
class ApiAdapter : ListAdapter<FavoriteShop, ApiItemViewHolder>(ApiItemCallback()) {

    // 一覧画面から登録するときのコールバック（FavoriteFragmentへ通知するメソッド)
    var onClickAddFavorite: ((FavoriteShop) -> Unit)? = null

    // 一覧画面から削除するときのコールバック（ApiFragmentへ通知するメソッド)
    var onClickDeleteFavorite: ((FavoriteShop) -> Unit)? = null

    // Itemを押したときのメソッド
    var onClickItem: ((ArrayList<String>) -> Unit)? = null

    /**
     * ViewHolderを生成して返す
     */
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ApiItemViewHolder {
        // ViewBindingを引数にApiItemViewHolderを生成
        val view =
            RecyclerFavoriteBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ApiItemViewHolder(view)
    }

    /**
     * 指定された位置（position）のViewにShopの情報をセットする
     */
    override fun onBindViewHolder(holder: ApiItemViewHolder, position: Int) {
        holder.bind(getItem(position), position, this)
    }
}

/**
 * リスト内の1行の内容を保持する
 */
class ApiItemViewHolder(private val binding: RecyclerFavoriteBinding) :
    RecyclerView.ViewHolder(binding.root) {
    fun bind(shop: FavoriteShop, position: Int, adapter: ApiAdapter) {
        binding.rootView.apply {
            // 偶数番目と奇数番目で背景色を変更させる
            setBackgroundColor(
                ContextCompat.getColor(
                    context,
                    if (position % 2 == 0) android.R.color.white else android.R.color.darker_gray
                )
            )
            setOnClickListener {
                val shopData = arrayListOf<String>(
                    shop.id,
                    shop.name,
                    shop.imageUrl,
                    shop.url,
                )
                adapter.onClickItem?.invoke(shopData)
            }
        }
        // nameTextViewのtextプロパティに代入されたオブジェクトのnameプロパティを代入
        binding.nameTextView.text = shop.name + "-" + shop.address

        // Picassoライブラリを使い、imageViewにdata.logoImageのurlの画像を読み込ませる
        Picasso.get().load(shop.imageUrl).into(binding.imageView)
        //val f = FavoriteShop.findBy(shop.id)
        // 星の処理
        binding.favoriteImageView.apply {
            // お気に入り状態を取得
//            if (f != null) {
            var isFavorite = FavoriteShop.findBy(shop.id)

            // 白抜きの星を設定
            setImageResource(if (isFavorite == 1) R.drawable.ic_star else R.drawable.ic_star_border)

            // 星をタップした時の処理
            setOnClickListener {
                if (isFavorite == 1) {
                    adapter.onClickDeleteFavorite?.invoke(shop)
                    //isFavorite = 0
                } else if (isFavorite == 0) {
                    adapter.onClickAddFavorite?.invoke(shop)
                    //isFavorite = 1
                }
                Log.d("CheckError", "Error1")
                adapter.notifyItemChanged(position)
                Log.d("CheckError", "Error2")
            }
            Log.d("CheckError", "Hiii")
        }
    }
}

/**
 * データの差分を確認するクラス
 */
internal class ApiItemCallback : DiffUtil.ItemCallback<FavoriteShop>() {

    override fun areItemsTheSame(oldItem: FavoriteShop, newItem: FavoriteShop): Boolean {
        return oldItem.id == newItem.id
    }

    @SuppressLint("DiffUtilEquals")
    override fun areContentsTheSame(oldItem: FavoriteShop, newItem: FavoriteShop): Boolean {
        return oldItem == newItem
    }
}
