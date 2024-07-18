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
import com.google.android.material.snackbar.Snackbar


class ArticleFragment: Fragment(R.layout.fragment_article) {
    lateinit var viewModel: NewsViewModel
    lateinit var binding: FragmentArticleBinding
    //ArticleFragmentArgs will be provided by navArgs
    val args:ArticleFragmentArgs by navArgs()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        viewModel = (activity as NewsActivity).viewModel
        binding = FragmentArticleBinding.bind(view)

        val article = args.article
        binding.webView.apply{
            webViewClient = WebViewClient()
            article.url?.let { loadUrl(it) }
        }


        binding.fab.setOnClickListener {
            val counter = ++viewModel.counter
            if (counter % 2 != 0) {
                viewModel.saveArticle(article)
                val snackbar =
                    Snackbar.make(view, "Article was successfully saved", Snackbar.LENGTH_SHORT)
                snackbar.anchorView =
                    (activity as NewsActivity).findViewById(R.id.bottomNavigationView)
                snackbar.show()
            } else{
                viewModel.deleteArticle(article)
                val snackbar =
                    Snackbar.make(view, "Article was deleted", Snackbar.LENGTH_SHORT)
                snackbar.anchorView =
                    (activity as NewsActivity).findViewById(R.id.bottomNavigationView)
                snackbar.show()
            }
        }
    }
}