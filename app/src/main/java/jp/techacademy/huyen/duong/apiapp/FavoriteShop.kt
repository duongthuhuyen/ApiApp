package jp.techacademy.huyen.duong.apiapp

import io.realm.kotlin.Realm
import io.realm.kotlin.RealmConfiguration
import io.realm.kotlin.ext.query
import io.realm.kotlin.types.RealmObject
import io.realm.kotlin.types.annotations.PrimaryKey

open class FavoriteShop(
    id: String,
    imageUrl: String,
    name: String,
    url: String,
    favorite: Int
) : RealmObject, java.io.Serializable {
    @PrimaryKey
    var id: String = ""
    var imageUrl: String = ""
    var name: String = ""
    var url: String = ""
    var favorite: Int = 0

    // 初期化処理
    init {
        this.id = id
        this.imageUrl = imageUrl
        this.name = name
        this.url = url
        this.favorite = favorite
    }

    // realm内部呼び出し用にコンストラクタを用意
    constructor() : this("", "", "", "", 0)

    companion object {
        /**
         * お気に入りのShopを全件取得
         */
        fun findAll(): List<FavoriteShop> {
            // Realmデータベースとの接続を開く
            val config = RealmConfiguration.create(schema = setOf(FavoriteShop::class))
            val realm = Realm.open(config)

            // Realmデータベースからお気に入り情報を取得
            // mapでディープコピーしてresultに代入する
            val result = realm.query<FavoriteShop>("favorite == 1").find()
                .map { FavoriteShop(it.id, it.imageUrl, it.name, it.url, it.favorite) }

            // Realmデータベースとの接続を閉じる
            realm.close()

            return result
        }

        /**
         * お気に入りされているShopをidで検索して返す
         * お気に入りに登録されていなければnullで返す
         */
        fun findBy(id: String): FavoriteShop? {
            // Realmデータベースとの接続を開く
            val config = RealmConfiguration.create(schema = setOf(FavoriteShop::class))
            val realm = Realm.open(config)

            val result = realm.query<FavoriteShop>("id=='$id'").first().find()

            // Realmデータベースとの接続を閉じる
            realm.close()

            return result
        }

        /**
         * データリスト追加
         */
        fun insert(favoriteShop: List<FavoriteShop>) {
            // Realmデータベースとの接続を開く
            val config = RealmConfiguration.create(schema = setOf(FavoriteShop::class))
            val realm = Realm.open(config)

            var data = mutableListOf<FavoriteShop>()
            for ( f in favoriteShop) {
                val shop = realm.query<FavoriteShop>("id == '${f.id}'").find()
                if (shop == null) {
                    data.add(shop)
                }
            }
            // 登録処理
            if (data.size > 0) {
                realm.writeBlocking {
                    data.map { copyToRealm(it) }
                }
            }

            // Realmデータベースとの接続を閉じる
            realm.close()
        }

        /**
         * 気に入り追加
         */
        suspend fun update(id: String) {
            // Realmデータベースとの接続を開く
            val config = RealmConfiguration.create(schema = setOf(FavoriteShop::class))
            val realm = Realm.open(config)

            val fa = realm.query<FavoriteShop>("id == ${id}").find()
            // 登録処理
            if (fa != null && fa.first().favorite == 0) {
                realm.write {
                    findLatest(fa.first())?.let { shop ->
                        shop.favorite = 1
                    }
                }
            }

            // Realmデータベースとの接続を閉じる
            realm.close()
        }

        /**
         * idでお気に入りから削除する
         */
        suspend fun delete(id: String) {
            // Realmデータベースとの接続を開く
            val config = RealmConfiguration.create(schema = setOf(FavoriteShop::class))
            val realm = Realm.open(config)

            // 削除処理
            val fa = realm.query<FavoriteShop>("id == ${id}").find()
            // 登録処理
            if (fa != null && fa.first().favorite==1 ) {
                realm.write {
                    findLatest(fa.first())?.let { shop ->
                        shop.favorite= 0
                    }
                }
            }

            // Realmデータベースとの接続を閉じる
            realm.close()
        }
    }
}