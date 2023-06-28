package com.example.speertestapp.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.speertestapp.R
import com.example.speertestapp.data.UserList
import com.example.speertestapp.activity.FollowCellClickListener
import com.squareup.picasso.Picasso

/**
 * FollowListAdapter
 * Adapter of Follow List
 */

class FollowListAdapter(private val context: Context, private val list_store: UserList, private val cellClickListener: FollowCellClickListener) :
    RecyclerView.Adapter<FollowListAdapter.MyView>() {
    class MyView(view: View) : RecyclerView.ViewHolder(view) {
        var imageView: ImageView
        var name: TextView
        var item_content: LinearLayout
        init {
            imageView = view
                .findViewById<ImageView>(R.id.image)
            name = view
                .findViewById<TextView>(R.id.name)
            item_content = view
                .findViewById<LinearLayout>(R.id.item_content)
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyView {
        val itemView: View = LayoutInflater
            .from(parent.context)
            .inflate(
                R.layout.user_recycler_item,
                parent,
                false
            )
        return MyView(itemView)
    }

    override fun onBindViewHolder(holder: MyView, position: Int) {
        val user = list_store.getUser(position)
        //Loading Image into view
        Picasso.get().load(user.getAvatar()).placeholder(R.mipmap.ic_launcher).into(holder.imageView)
        holder.name.text = user.getLogin()

        holder.item_content.setOnClickListener {
            val data=position
            cellClickListener.onCellClickListener(position)
        }
    }

    override fun getItemCount(): Int {
        return list_store.getUserCount()
    }

}