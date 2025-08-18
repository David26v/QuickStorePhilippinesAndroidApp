package com.example.quickstorephilippinesandroidapp.ui.admin.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.quickstorephilippinesandroidapp.databinding.ItemUserBinding
import com.example.quickstorephilippinesandroidapp.ui.admin.model.User

class UserListAdapter(
    private val onUserClick: (User) -> Unit
) : ListAdapter<User, UserListAdapter.UserViewHolder>(UserDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): UserViewHolder {
        val binding = ItemUserBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return UserViewHolder(binding)
    }

    override fun onBindViewHolder(holder: UserViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    inner class UserViewHolder(
        private val binding: ItemUserBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(user: User) {
            binding.apply {
                textUserName.text = user.name
                textUserEmail.text = user.email
                textUserRole.text = user.role
                textCreatedAt.text = "Created: ${user.createdAt}"
                textLastLogin.text = if (user.lastLogin != null) {
                    "Last login: ${user.lastLogin}"
                } else {
                    "Never logged in"
                }

                // Status indicator
                viewStatusIndicator.setBackgroundColor(
                    if (user.isActive) {
                        itemView.context.getColor(android.R.color.holo_green_light)
                    } else {
                        itemView.context.getColor(android.R.color.holo_red_light)
                    }
                )

                textUserStatus.text = if (user.isActive) "Active" else "Inactive"

                // Auth methods
                if (user.authMethods.isNotEmpty()) {
                    textAuthMethods.text = "Auth: ${user.authMethods.joinToString(", ")}"
                    textAuthMethods.visibility = View.VISIBLE
                } else {
                    textAuthMethods.visibility = View.GONE
                }

                root.setOnClickListener {
                    onUserClick(user)
                }
            }
        }
    }

    class UserDiffCallback : DiffUtil.ItemCallback<User>() {
        override fun areItemsTheSame(oldItem: User, newItem: User): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: User, newItem: User): Boolean {
            return oldItem == newItem
        }
    }
}