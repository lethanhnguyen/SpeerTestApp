package com.example.speertestapp.activity

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.ProgressBar
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.core.widget.NestedScrollView
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.speertestapp.R
import com.example.speertestapp.adapter.FollowListAdapter
import com.example.speertestapp.data.AESUtils
import com.example.speertestapp.data.UserList
import okhttp3.Call
import okhttp3.Callback
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import java.io.IOException

interface FollowCellClickListener {
    fun onCellClickListener(position: Int)
}

class FollowListActivity: AppCompatActivity(),FollowCellClickListener {
    private lateinit var adapter: FollowListAdapter
    private var client = OkHttpClient()

    //list of users
    private var userList = UserList()
    //tmp list of users, using for continuous loading
    private var userListTmp = UserList()
    //status loading
    private var onLoading = 0

    //current visible page
    private var current_page = 1
    //num of user per page
    private var per_page = 30

    private lateinit var type:String
    private lateinit var name:String
    private lateinit var login:String
    private lateinit var num_follow:String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_follow_list)

        type = intent.getStringExtra("type").toString()
        name = intent.getStringExtra("name").toString()
        login = intent.getStringExtra("login").toString()
        num_follow = intent.getStringExtra("num_follow").toString()

        val toolbar: Toolbar = findViewById<View>(R.id.toolbar) as Toolbar
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        toolbar.setNavigationOnClickListener {
            onBackPressed() // Implemented by activity
        }
        title = "$login - $type"

        //setting recycler to vertical scroll
        val userListRecycler: RecyclerView = findViewById<View>(R.id.user_list_recycler) as RecyclerView
        userListRecycler.layoutManager = LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false)
        //setting adapter to recycler
        adapter = FollowListAdapter(this, userList, this)
        userListRecycler.adapter = adapter

        //do not display no user text or list in the beginning
        turnTextEmpty(0)

        //init listeners
        initScrollListener()

        getUserList()
    }

    /**
     * call github list api for get user list
     */
    private fun getUserList() {
        //get github key
        val encrypted = getString(R.string.github_key)
        var github_key = ""
        try {
            github_key = AESUtils.decrypt(encrypted)
        } catch (e: Exception) {
            e.printStackTrace()
        }

        startLoading()

        var url:String = getString(R.string.github_url) + "users/" + login + "/followers?page=" + current_page.toString() + "&per_page=30"
        if (type == "Following")
        {
            url = getString(R.string.github_url) + "users/" + login + "/following?page=" + current_page.toString() + "&per_page=30"
        }

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
                val jsonString = response.body?.string()
                println(jsonString)
                if (jsonString != null) {
                    userListTmp.addFromFollowUserListApi(jsonString,num_follow.toInt())
                    if (userListTmp.getUserCount()>0)
                    {
                        per_page = userListTmp.getUserCount()
                        userList.addUserList(userListTmp)
                        userListTmp.cleanUserList()
                        turnTextEmpty(0)
                        generateUserListData()
                    }
                    else
                    {
                        stopLoading()
                        if (userList.getUserCount() == 0)
                        {
                            turnTextEmpty(1)
                        }

                    }
                }
                else
                {
                    displayAlert("Error",getString(R.string.error_api_message))
                }
            }
        })
    }

    /**
     * display user list
     */
    private fun generateUserListData() {
        runOnUiThread(Runnable {
            adapter.notifyItemRangeChanged((current_page-1)*per_page,per_page)
            stopLoading()
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

    /**
     * turn text empty on and off
     */
    private fun turnTextEmpty(on: Int) {
        runOnUiThread(Runnable {
            if (on == 1)
            {
                val textEmpty: TextView = findViewById<View>(R.id.text_empty) as TextView
                textEmpty.visibility = View.VISIBLE
            }
            else
            {
                val textEmpty: TextView = findViewById<View>(R.id.text_empty) as TextView
                textEmpty.visibility = View.GONE
            }
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
     * listener to scroll
     */
    private fun initScrollListener() {
        val nestedContent: NestedScrollView = findViewById<View>(R.id.nested_content) as NestedScrollView
        nestedContent.setOnScrollChangeListener(NestedScrollView.OnScrollChangeListener { v, scrollX, scrollY, oldScrollX, oldScrollY ->
            if (scrollY == v.getChildAt(0).measuredHeight - v.measuredHeight) {
                if (onLoading == 0)
                {
                    if (current_page*per_page < userList.getMaxUserCount())
                    {
                        current_page += 1
                        getUserList()
                    }
                }
            }
        })
    }

    /**
     * listener on store click
     */
    override fun onCellClickListener(position: Int) {
        if (onLoading == 0)
        {
            val intent = Intent(this@FollowListActivity, UserInfoActivity::class.java)
            intent.putExtra("login", userList.getUser(position).getLogin())
            startActivity(intent)
        }
    }
}