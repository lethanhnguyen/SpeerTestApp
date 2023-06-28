package com.example.speertestapp.activity

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputMethodManager
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.widget.NestedScrollView
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.speertestapp.R
import com.example.speertestapp.adapter.UserListAdapter
import com.example.speertestapp.data.AESUtils.decrypt
import com.example.speertestapp.data.UserList
import com.example.speertestapp.databinding.ActivityMainBinding
import okhttp3.Call
import okhttp3.Callback
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import java.io.IOException


interface CellClickListener {
    fun onCellClickListener(position: Int)
}

class MainActivity : AppCompatActivity(), CellClickListener {
    private lateinit var binding: ActivityMainBinding
    private lateinit var adapter: UserListAdapter
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
    //search keyword
    private var current_keyword = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        //setting recycler to vertical scroll
        binding.userListRecycler.layoutManager = LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false)
        //setting adapter to recycler
        adapter = UserListAdapter(userList, this)
        binding.userListRecycler.adapter = adapter

        //do not display no user text or list in the beginning
        turnTextEmpty(0)

        //init listeners
        initScrollListener()
        initSearchListener()
    }

    /**
     * turn text empty on and off
     */
    private fun turnTextEmpty(on: Int) {
        runOnUiThread(Runnable {
            if (on == 1)
            {
                binding.textEmpty.visibility = View.VISIBLE
            }
            else
            {
                binding.textEmpty.visibility = View.GONE
            }
        })
    }

    /**
     * start loading status
     */
    private fun startLoading(){
        onLoading = 1
        runOnUiThread(Runnable {
            binding.progressBar.visibility = View.VISIBLE
        })
    }

    /**
     * stop loading status
     */
    private fun stopLoading(){
        onLoading = 0
        runOnUiThread(Runnable {
            binding.progressBar.visibility = View.GONE
        })
    }

    /**
     * call github list api for get user list
     */
    private fun getUserList(keyword: String) {
        //get github key
        val encrypted = getString(R.string.github_key)
        var github_key = ""
        try {
            github_key = decrypt(encrypted)
        } catch (e: Exception) {
            e.printStackTrace()
        }

        startLoading()
        current_keyword = keyword

        val url:String = getString(R.string.github_url) + "search/users?q=" + keyword + "&page=" + current_page.toString() + "&per_page=30"

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
                    userListTmp.addFromUserListApi(jsonString)
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
     * clear all data
     */
    private fun refreshAllData() {
        val numrow = userList.getUserCount()
        userList.cleanUserList()
        binding.userListRecycler.adapter?.notifyItemRangeRemoved(0,numrow)
        current_page = 1
    }

    /**
     * close keyboard
     */
    private fun closeKeyboard() {
        val view: View? = this.currentFocus
        val imm = getSystemService(INPUT_METHOD_SERVICE) as InputMethodManager
        if (view != null) {
            imm.hideSoftInputFromWindow(view.windowToken, 0)
        }
    }

    /**
     * listener to search box
     */
    private fun initSearchListener() {
        binding.etSearch.setOnEditorActionListener { _, actionId, _ ->
            if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                closeKeyboard()
                refreshAllData()
                getUserList(binding.etSearch.text.toString())
            }
            true
        }
    }

    /**
     * listener to scroll
     */
    private fun initScrollListener() {
        binding.nestedContent.setOnScrollChangeListener(NestedScrollView.OnScrollChangeListener { v, scrollX, scrollY, oldScrollX, oldScrollY ->
            if (scrollY == v.getChildAt(0).measuredHeight - v.measuredHeight) {
                if (onLoading == 0)
                {
                    if (current_page*per_page < userList.getMaxUserCount())
                    {
                        current_page += 1
                        getUserList(current_keyword)
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
            val intent = Intent(this@MainActivity, UserInfoActivity::class.java)
            intent.putExtra("login", userList.getUser(position).getLogin())
            startActivity(intent)
        }
    }
}