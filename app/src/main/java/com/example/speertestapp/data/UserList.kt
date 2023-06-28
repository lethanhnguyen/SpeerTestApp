package com.example.speertestapp.data

import org.json.JSONArray
import org.json.JSONObject
import org.json.JSONTokener

/**
 * UserList
 * Contains list of users when search
 */
class UserList {
    private var userList: ArrayList<User> = ArrayList<User>()
    private var max_user: Int = 0

    /**
     * clean user List
     */
    fun cleanUserList()
    {
        userList.clear()
    }

    /**
     * replace current User with new User
     */
    fun updateUser(position:Int, newUser: User)
    {
        userList[position] = newUser
    }

    /**
     * convert JSON string to User list (for follow)
     */
    fun addFromFollowUserListApi(jsonString: String, max: Int)
    {
        val jsonArray = JSONTokener(jsonString).nextValue() as JSONArray
        if (jsonArray.length() > 0)
        {
            for (i in 0 until jsonArray.length()) {
                val login = jsonArray.getJSONObject(i).getString("login")
                val id = jsonArray.getJSONObject(i).getString("id")
                val avatar_url = jsonArray.getJSONObject(i).getString("avatar_url")
                val html_url = jsonArray.getJSONObject(i).getString("html_url")

                val user = User(login,id,avatar_url,html_url)
                userList.add(user)
            }
        }

        max_user = max
    }

    /**
     * convert JSON string to User list
     */
    fun addFromUserListApi(jsonString: String)
    {
        val jsonObject = JSONTokener(jsonString).nextValue() as JSONObject
        val records = jsonObject.getString("items")
        val jsonArray = JSONTokener(records).nextValue() as JSONArray
        if (jsonArray.length() > 0)
        {
            for (i in 0 until jsonArray.length()) {
                val login = jsonArray.getJSONObject(i).getString("login")
                val id = jsonArray.getJSONObject(i).getString("id")
                val avatar_url = jsonArray.getJSONObject(i).getString("avatar_url")
                val html_url = jsonArray.getJSONObject(i).getString("html_url")

                val user = User(login,id,avatar_url,html_url)
                userList.add(user)
            }
        }

        max_user = jsonObject.getInt("total_count")
    }

    /**
     * get max number of User
     */
    fun getMaxUserCount():Int {
        return max_user
    }

    /**
     * get number of User
     */
    fun getUserCount():Int {
        return userList.size
    }

    /**
     * get User in position
     */
    fun getUser(index: Int): User {
        return userList.get(index)
    }

    /**
     * add another User List to current list
     */
    fun addUserList(extraList: UserList) {
        for (i in 1..extraList.getUserCount()) {
            userList.add(extraList.getUser(i-1))
        }

        max_user = extraList.max_user
    }
}