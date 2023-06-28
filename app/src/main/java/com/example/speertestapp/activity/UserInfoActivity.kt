package com.example.speertestapp.activity

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.ProgressBar
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import com.example.speertestapp.MyApplication
import com.example.speertestapp.R
import com.example.speertestapp.data.AESUtils
import com.example.speertestapp.data.UserInfo
import com.squareup.picasso.Picasso
import okhttp3.Call
import okhttp3.Callback
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import org.json.JSONObject
import org.json.JSONTokener
import java.io.IOException

class UserInfoActivity: AppCompatActivity() {
    private var client = OkHttpClient()
    private lateinit var userInfo: UserInfo

    //status loading
    private var onLoading = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_user_profile)

        val toolbar: Toolbar = findViewById<View>(R.id.toolbar) as Toolbar
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        toolbar.setNavigationOnClickListener {
            onBackPressed() // Implemented by activity
        }

        val follower_layout: LinearLayout = findViewById<View>(R.id.follower_layout) as LinearLayout
        val following_layout: LinearLayout = findViewById<View>(R.id.following_layout) as LinearLayout
        follower_layout.setOnClickListener {
            goToFollow(0)
        }

        following_layout.setOnClickListener {
            goToFollow(1)
        }

        val login = intent.getStringExtra("login")
        if (login != null) {
            title = "$login"
            getUserInfo(login)
        }
    }

    private fun goToFollow(type: Int) {
        val intent = Intent(this@UserInfoActivity, FollowListActivity::class.java)
        if (type == 0)
        {
            //follower
            intent.putExtra("type", "Followers")
            intent.putExtra("num_follow", userInfo.getFollowers())
        }
        else
        {
            //following
            intent.putExtra("type", "Following")
            intent.putExtra("num_follow", userInfo.getFollowing())
        }
        intent.putExtra("name", userInfo.getName())
        intent.putExtra("login", userInfo.getLogin())
        startActivity(intent)
    }

    /**
     * call github api for user info
     */
    private fun getUserInfo(login: String) {
        //get github key
        val encrypted = getString(R.string.github_key)
        var github_key = ""
        try {
            github_key = AESUtils.decrypt(encrypted)
        } catch (e: Exception) {
            e.printStackTrace()
        }

        //check if having in cache
        val userInfoCache = MyApplication.cacheList.getCache(login)
        if (userInfoCache != null)
        {
            updateUserInfo(userInfoCache)
        }
        else
        {
            startLoading()

            val url:String = getString(R.string.github_url) + "users/" + login

            val request = Request.Builder()
                .url(url)
                .addHeader("Accept", "application/vnd.github+json")
                .addHeader("Authorization", "Bearer $github_key")
                .addHeader("X-GitHub-Api-Version", getString(R.string.github_api_version))
                .build()

            client.newCall(request).enqueue(object : Callback {
                override fun onFailure(call: Call, e: IOException) {
                    stopLoading()
                    displayAlert("Error",getString(R.string.error_api_message))
                }
                override fun onResponse(call: Call, response: Response) {
                    stopLoading()
                    val jsonString = response.body?.string()

                    val jsonObject = JSONTokener(jsonString).nextValue() as JSONObject
                    val name = jsonObject.getString("name")
                    val html_url = jsonObject.getString("html_url")
                    val avatar_url = jsonObject.getString("avatar_url")
                    val followers = jsonObject.getString("followers")
                    val following = jsonObject.getString("following")
                    val company = jsonObject.getString("company")
                    val location = jsonObject.getString("location")
                    val bio = jsonObject.getString("bio")
                    val public_repos = jsonObject.getString("public_repos")
                    val created_at = jsonObject.getString("created_at")
                    val updated_at = jsonObject.getString("updated_at")
                    userInfo = UserInfo(login,name,avatar_url,html_url,followers,following,company,location,bio,public_repos,created_at,updated_at)
                    MyApplication.cacheList.addCache(login,userInfo)
                    updateUserInfo(userInfo)
                }
            })
        }
    }

    /**
     * update User Info
     */
    private fun updateUserInfo(_userInfo: UserInfo){
        runOnUiThread(Runnable {
            val name: TextView = findViewById<View>(R.id.name) as TextView
            if (_userInfo.getName()!= "null")
            {
                name.text = _userInfo.getName()
            }

            val login: TextView = findViewById<View>(R.id.login) as TextView
            login.text = _userInfo.getURL()
            val follower: TextView = findViewById<View>(R.id.follower) as TextView
            follower.text = _userInfo.getFollowers()
            val following: TextView = findViewById<View>(R.id.following) as TextView
            following.text = _userInfo.getFollowing()
            val company: TextView = findViewById<View>(R.id.company) as TextView
            if (_userInfo.getCompany()!= "null")
            {
                company.text = _userInfo.getCompany()
            }

            val location: TextView = findViewById<View>(R.id.location) as TextView
            if (_userInfo.getLocation()!= "null")
            {
                location.text = _userInfo.getLocation()
            }

            val bio: TextView = findViewById<View>(R.id.bio) as TextView
            if (_userInfo.getBio()!= "null")
            {
                bio.text = _userInfo.getBio()
            }

            val public_repos: TextView = findViewById<View>(R.id.public_repos) as TextView
            public_repos.text = _userInfo.getPublicRepos()
            val day_created: TextView = findViewById<View>(R.id.day_created) as TextView
            day_created.text = _userInfo.getCreatedAt()
            val day_updated: TextView = findViewById<View>(R.id.day_updated) as TextView
            day_updated.text = _userInfo.getUpdatedAt()

            val image: ImageView = findViewById<View>(R.id.image) as ImageView
            Picasso.get().load(_userInfo.getAvatar()).placeholder(R.mipmap.ic_launcher).into(image)
        })
    }

    /**
     * start loading status
     */
    private fun startLoading(){
        onLoading = 1
        runOnUiThread(Runnable {
            val progressBar: ProgressBar = findViewById<View>(R.id.progressBar) as ProgressBar
            progressBar.visibility = View.VISIBLE
        })
    }

    /**
     * stop loading status
     */
    private fun stopLoading(){
        onLoading = 0
        runOnUiThread(Runnable {
            val progressBar: ProgressBar = findViewById<View>(R.id.progressBar) as ProgressBar
            progressBar.visibility = View.GONE
        })
    }

    /**
     * display alert
     */
    private fun displayAlert(title: String, message: String) {
        runOnUiThread(Runnable {
            val builder = AlertDialog.Builder(this)
            builder.setTitle(title)
            builder.setMessage(message)
            builder.show()
        })
    }
}