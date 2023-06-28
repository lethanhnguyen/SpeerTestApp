package com.example.speertestapp.data

/**
 * UserInfoCacheList
 * Where to Save Cache
 */
class UserInfoCacheList {
    //cache all users
    private var cache: HashMap<String, UserInfoCache> = HashMap<String, UserInfoCache>()
    //list of cache user, using to remove first added user when cache fulled
    private var cacheList: ArrayList<String> = ArrayList<String>()
    //max number of users in cache
    private var maxCache: Int = 10000

    init {
    }

    /**
     * add user to cache
     */
    fun addCache(id: String, user: UserInfo)
    {
        val user_cache = UserInfoCache(user)
        if (cacheList.size > maxCache)
        {
            //remove the first user in list and cache
            val name = cacheList[0]
            cacheList.removeAt(0)
            cache.remove(name)
        }

        //add mew user to list and cache
        cacheList.add(id)
        cache.put(id, user_cache)
    }

    /**
     * get user from cache
     */
    fun getCache(id: String): UserInfo?
    {
        if (cache.containsKey(id))
        {
            if (cache.get(id) != null)
            {
                //if more than 30 minutes, remove it
                if (System.currentTimeMillis()- cache.get(id)!!.getTime() > 1800000)
                {
                    val idx = cacheList.indexOf(id)
                    cacheList.removeAt(idx)
                    cache.remove(id)
                    return null
                }
            }
            return cache.get(id)?.getUserInfo()
        }
        return null
    }
}