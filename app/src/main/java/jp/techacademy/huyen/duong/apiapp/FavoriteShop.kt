package jp.techacademy.huyen.duong.apiapp

import android.util.Log
import io.realm.kotlin.Realm
import io.realm.kotlin.RealmConfiguration
import io.realm.kotlin.ext.query
import io.realm.kotlin.types.RealmObject
import io.realm.kotlin.types.annotations.PrimaryKey

open class FavoriteShop(id: String, imageUrl: String, name: String, url: String,address: String,favorite: Int) : RealmObject {
    @PrimaryKey
    var id: String = ""
    var imageUrl: String = ""
    var name: String = ""
    var url: String = ""
    var address: String = ""
    var favorite: Int = 0

    // 初期化処理
    init {
        this.id = id
        this.imageUrl = imageUrl
        this.name = name
        this.url = url
        this.address = address
        this.favorite = favorite
    }

    // realm内部呼び出し用にコンストラクタを用意
    constructor() : this("", "", "", "","", 0)

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
            val result = realm.query<FavoriteShop>().find()
                .map { FavoriteShop(it.id, it.imageUrl, it.name, it.url, it.address,it.favorite) }

            // Realmデータベースとの接続を閉じる
            realm.close()

            return result
        }
        /**
         * お気に入りのShopを全件取得
         */
        fun findAllLike(): List<FavoriteShop> {
            // Realmデータベースとの接続を開く
            val config = RealmConfiguration.create(schema = setOf(FavoriteShop::class))
            val realm = Realm.open(config)

            // Realmデータベースからお気に入り情報を取得
            // mapでディープコピーしてresultに代入する
            val result = realm.query<FavoriteShop>("favorite == 1").find()
                .map { FavoriteShop(it.id, it.imageUrl, it.name, it.url,it.address, it.favorite) }

            // Realmデータベースとの接続を閉じる
            realm.close()

            return result
        }
        /**
         * お気に入りされているShopをidで検索して返す
         * お気に入りに登録されていなければnullで返す
         */
        fun findBy(id: String): Int{
            // Realmデータベースとの接続を開く
            val config = RealmConfiguration.create(schema = setOf(FavoriteShop::class))
            val realm = Realm.open(config)
            var fa = 0
            val result = realm.query<FavoriteShop>("id=='$id'").first().find()
            Log.d("HELLLOOO",""+result?.favorite)
            fa = result?.favorite!!
            // Realmデータベースとの接続を閉じる
            realm.close()

            return fa
        }

        /**
         * お気に入り追加
         */
        suspend fun insert(id: String) {
            // Realmデータベースとの接続を開く
            val config = RealmConfiguration.create(schema = setOf(FavoriteShop::class))
            val realm = Realm.open(config)

            val fa = realm.query<FavoriteShop>("id == '${id}'").first().find()
            // 登録処理
            if (fa != null) {
                if (fa.favorite == 0) {
                    realm.write {
                        findLatest(fa)?.apply {
                            this.favorite = 1
                        }
                    }
                }
            }

            // Realmデータベースとの接続を閉じる
            realm.close()
        }
        fun insertAll(favoriteShop: List<FavoriteShop>) {
            // Realmデータベースとの接続を開く
            val config = RealmConfiguration.create(schema = setOf(FavoriteShop::class))
            val realm = Realm.open(config)

            var data = mutableListOf<FavoriteShop>()
            for ( f in favoriteShop) {
                val shop = realm.query<FavoriteShop>("id == '${f.id}'").first().find()
                if (shop == null) {
                    data.add(f)
                }
            }
            // 登録処理
            if (data.size > 0) {
                realm.writeBlocking {
                    data.map { copyToRealm(it) }
                }
            }
            val count = realm.query<FavoriteShop>().find()
            Log.d("Insert","" + count.size)
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
            val fa = realm.query<FavoriteShop>("id == '${id}'").first().find()
            // 登録処理
            if (fa != null ) {
                if (fa.favorite == 1) {
                    realm.write {
                        findLatest(fa)?.apply {
                            this.favorite = 0
                        }
                    }
                }
            }


            // Realmデータベースとの接続を閉じる
            realm.close()
        }
        suspend fun deleteAll() {
            // Realmデータベースとの接続を開く
            val config = RealmConfiguration.create(schema = setOf(FavoriteShop::class))
            val realm = Realm.open(config)
            realm.write {
                deleteAll()
            }
            realm.close()
        }
    }
}