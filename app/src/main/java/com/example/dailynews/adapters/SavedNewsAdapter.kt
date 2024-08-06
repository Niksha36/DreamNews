package com.example.dailynews.adapters

import android.content.Intent
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.AsyncListDiffer
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.dailynews.R
import com.example.dailynews.databinding.ItemArticlePreviewBinding
import com.example.dailynews.databinding.ProfileCardViewBinding
import com.example.dailynews.models.Article
import com.example.dailynews.ui.auth.AuthActivity

class SavedNewsAdapter(private val userEmail: String?): RecyclerView.Adapter<RecyclerView.ViewHolder>()  {
    companion object {
        private const val VIEW_TYPE_PROFILE = 0
        private const val VIEW_TYPE_ARTICLE = 1
    }

    inner class ProfileViewHolder(val binding: ProfileCardViewBinding) :
        RecyclerView.ViewHolder(binding.root)

    inner class NewsViewHolder(val binding: ItemArticlePreviewBinding) :
        RecyclerView.ViewHolder(binding.root)

    private val differCallback = object : DiffUtil.ItemCallback<Article>() {
        override fun areItemsTheSame(oldItem: Article, newItem: Article): Boolean {
            return oldItem.url == newItem.url
        }

        override fun areContentsTheSame(oldItem: Article, newItem: Article): Boolean {
            return oldItem == newItem
        }
    }
    val differ = AsyncListDiffer(this, differCallback)

    override fun getItemViewType(position: Int): Int {
        return if (position == 0) VIEW_TYPE_PROFILE else VIEW_TYPE_ARTICLE
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return when (viewType) {
            VIEW_TYPE_PROFILE -> {
                val itemView = ProfileCardViewBinding.inflate(LayoutInflater.from(parent.context), parent, false)
                ProfileViewHolder(itemView)
            }
            VIEW_TYPE_ARTICLE -> {
                val itemView = ItemArticlePreviewBinding.inflate(LayoutInflater.from(parent.context), parent, false)
                NewsViewHolder(itemView)
            }
            else -> throw IllegalArgumentException("Invalid view type")
        }
    }

    override fun getItemCount(): Int {
        return differ.currentList.size + 1 // +1 for the profile card
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        if (holder.itemViewType == VIEW_TYPE_PROFILE) {
            (holder as ProfileViewHolder).apply {
                binding.imageView2.setImageResource(R.drawable.ic_profile)
                binding.root.setOnClickListener {
                    val context = holder.itemView.context
                    val intent = Intent(context, AuthActivity::class.java)
                    context.startActivity(intent)
                }
                binding.textView.text = userEmail ?: "Sign in or Sign up"
            }

        } else {
            val oneArticle = differ.currentList[position - 1] // Adjust for profile card
            (holder as NewsViewHolder).binding.apply {
                Glide.with(this.root).load(oneArticle.urlToImage).into(ivArticleImage)
                tvSource.text = oneArticle.source?.name
                tvTitle.text = oneArticle?.title
                tvDescription.text = oneArticle?.description
                tvPublishedAt.text = oneArticle?.publishedAt

                root.setOnClickListener { onItemClickListener?.let { it(oneArticle) } }
            }
        }
    }
    private var onItemClickListener: ((Article) -> Unit)? = null

    fun setOnItemClickListener(transfer: (Article) -> Unit) {
        onItemClickListener = transfer
    }
}