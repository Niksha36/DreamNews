package com.example.dailynews.ui.fragments

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import android.util.Log
import android.widget.AbsListView
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.RecyclerView.OnScrollListener
import com.example.dailynews.R
import com.example.dailynews.adapters.NewsAdapter
import com.example.dailynews.databinding.FragmentBreakingNewsBinding
import com.example.dailynews.ui.NewsActivity
import com.example.dailynews.ui.NewsViewModel
import com.example.dailynews.util.Constants.QUERY_PAGE_SIZE
import com.example.dailynews.util.Resource
import com.google.android.material.snackbar.Snackbar

class BreakingNewsFragment : Fragment(R.layout.fragment_breaking_news) {
    private lateinit var binding: FragmentBreakingNewsBinding
    lateinit var viewModel: NewsViewModel
    lateinit var myAdapter: NewsAdapter
    val TAG = "BreakingNewsFragment"
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        viewModel = (activity as NewsActivity).viewModel
        binding = FragmentBreakingNewsBinding.bind(view)
        setUpRecyclerView()

        myAdapter.setOnItemClickListener {
            val bundle = Bundle().apply {
                putSerializable("article", it)
            }
            findNavController().navigate(
                R.id.action_breakingNewsFragment_to_articleFragment,
                bundle
            )
        }

        viewModel.breakingNews.observe(viewLifecycleOwner, Observer { response ->
            when (response) {
                is Resource.Success -> {
                    hideProgressBar()
                    response.data?.let { NewsResponce ->
                        myAdapter.differ.submitList(NewsResponce.articles)
                        val totalPages = NewsResponce.totalResults / QUERY_PAGE_SIZE + 2
                        isLastPage = viewModel.breakingNewsPage == totalPages
                        if(isLastPage){
                            binding.rvBreakingNews.setPadding(0,0,0,0)
                        }
                    }
                }

                is Resource.Error -> {
                    hideProgressBar()
                    response.message?.let {
                        val snackbar = Snackbar.make(view, "An error occurred: $it", Snackbar.LENGTH_LONG)
                        snackbar.anchorView = (activity as NewsActivity).findViewById(R.id.bottomNavigationView)
                        snackbar.show()
                    }
                }

                is Resource.Loading -> {
                    showProgressBar()
                }
            }
        })
    }

    private fun hideProgressBar() {
        binding.paginationProgressBar.visibility = View.GONE
        isLoading = false
    }

    private fun showProgressBar() {
        binding.paginationProgressBar.visibility = View.VISIBLE
        isLoading = true
    }

    //Определяем когда пользователь пролистал текущую страницу и необходимо подгрузить новые
    var isLoading = false
    var isScrolling = false
    var isLastPage = false

    val myOnScrollListener = object: RecyclerView.OnScrollListener(){
        override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
            super.onScrolled(recyclerView, dx, dy)
            val layoutManager = recyclerView.layoutManager as LinearLayoutManager
            val firstVisibleItemPosition = layoutManager.findFirstVisibleItemPosition()
            val visibleItemCount = layoutManager.childCount
            val totalItemCount = layoutManager.itemCount
            val isNotLoadingAndNotLastPage = !isLoading && !isLastPage
            val isAtLastItem = firstVisibleItemPosition + visibleItemCount >= totalItemCount
            val isNotAtBeginning = firstVisibleItemPosition >= 0
            val isTotalMoreThenVisible = totalItemCount >= QUERY_PAGE_SIZE
            val shouldPaginate = isNotLoadingAndNotLastPage && isAtLastItem && isNotAtBeginning
                    && isTotalMoreThenVisible && isScrolling
            if (shouldPaginate) {
                viewModel.getBreakingNews("us")
            }
        }

        override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {
            super.onScrollStateChanged(recyclerView, newState)
            if (newState == RecyclerView.SCROLL_STATE_DRAGGING) {
                isScrolling = true
            }
        }
    }

    private fun setUpRecyclerView() {
        myAdapter = NewsAdapter()
        binding.rvBreakingNews.apply {
            adapter = myAdapter
            layoutManager = LinearLayoutManager(activity)
            addOnScrollListener(myOnScrollListener)
        }
    }

}