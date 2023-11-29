package jp.techacademy.huyen.duong.apiapp

interface FragmentCallback {
    // Itemを押したときの処理
    fun onClickItem(shop: ArrayList<String>)

    // お気に入り追加時の処理
    fun onAddFavorite(shop: FavoriteShop)

    // お気に入り削除時の処理
    fun onDeleteFavorite(id: String)
}