package com.example.speertestapp.data

/**
 * User
 * User info
 */
class User(_login: String, _id: String, _avatar_url: String, _html_url: String) {
    private var login: String = _login
    private var id: String = _id
    private var avatar_url: String = _avatar_url
    private var html_url: String = _html_url

    /**
     * init
     */
    init {
    }

    /**
     * get Infos
     */
    fun getLogin(): String {
        return login
    }

    fun getId(): String {
        return id
    }

    fun getAvatar(): String {
        return avatar_url
    }

    fun getURL(): String {
        return html_url
    }
}