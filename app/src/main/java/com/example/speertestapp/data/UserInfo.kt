package com.example.speertestapp.data

/**
 * User
 * User info
 */
class UserInfo(_login: String, _name: String,_avatar_url: String, _html_url: String,_followers: String,_following: String,_company: String,_location: String,_bio: String,_public_repos: String,_created_at: String,_updated_at: String) {
    private var login: String = _login
    private var name: String = _name
    private var html_url: String = _html_url
    private var avatar_url: String = _avatar_url
    private var followers: String = _followers
    private var following: String = _following
    private var company: String = _company
    private var location: String = _location
    private var bio: String = _bio
    private var public_repos: String = _public_repos
    private var created_at: String = _created_at.split("T")[0]
    private var updated_at: String = _updated_at.split("T")[0]

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

    fun getName(): String {
        return name
    }

    fun getURL(): String {
        return html_url
    }

    fun getAvatar(): String {
        return avatar_url
    }

    fun getFollowers(): String {
        return followers
    }

    fun getFollowing(): String {
        return following
    }

    fun getCompany(): String {
        return company
    }

    fun getLocation(): String {
        return location
    }

    fun getUpdatedAt(): String {
        return updated_at
    }

    fun getPublicRepos(): String {
        return public_repos
    }

    fun getBio(): String {
        return bio
    }

    fun getCreatedAt(): String {
        return created_at
    }
}