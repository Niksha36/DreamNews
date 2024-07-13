package com.example.dailynews.ui.fragments

import android.os.Bundle
import android.view.View
import android.webkit.WebViewClient
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.navArgs
import com.example.dailynews.R
import com.example.dailynews.databinding.FragmentArticleBinding
import com.example.dailynews.ui.NewsActivity
import com.example.dailynews.ui.NewsViewModel


class ArticleFragment: Fragment(R.layout.fragment_article) {
    lateinit var viewModel: NewsViewModel
    lateinit var binding: FragmentArticleBinding
    val args:ArticleFragmentArgs by navArgs>()
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        viewModel = (activity as NewsActivity).viewModel
        binding = FragmentArticleBinding.bind(view)
        val article = args.article
        binding.webView.apply{
            webViewClient = WebViewClient()
            loadUrl(article.url)
        }
    }
}