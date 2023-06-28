package com.example.speertestapp.data

/**
 * UserInfoCache
 * Cache Item contains User info and added time
 */
class UserInfoCache(_user: UserInfo) {
    private var user: UserInfo = _user
    private var time = System.currentTimeMillis()

    /**
     * get User Info
     */
    fun getUserInfo(): UserInfo
    {
        return user
    }

    /**
     * get time of User Info
     */
    fun getTime():Long
    {
        return time
    }
}