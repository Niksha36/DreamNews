package com.example.dailynews.ui.fragments

import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.core.widget.addTextChangedListener
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.dailynews.R
import com.example.dailynews.adapters.NewsAdapter
import com.example.dailynews.databinding.FragmentSearchNewsBinding
import com.example.dailynews.ui.NewsActivity
import com.example.dailynews.ui.NewsViewModel
import com.example.dailynews.util.Constants.QUERY_PAGE_SIZE
import com.example.dailynews.util.Resource
import kotlinx.coroutines.Job
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.launch
import com.example.dailynews.util.Constants.SEARCH_NEWS_TIME_DELAY
import com.google.android.material.snackbar.Snackbar
import kotlinx.coroutines.delay

class SearchNewsFragment : Fragment(R.layout.fragment_search_news) {
    lateinit var viewModel: NewsViewModel
    lateinit var binding: FragmentSearchNewsBinding
    lateinit var myAdapter: NewsAdapter
    val TAG = "SearchNewsFragment"
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        viewModel = (activity as NewsActivity).viewModel
        binding = FragmentSearchNewsBinding.bind(view)
        setUpRecyclerView()

        binding.clearButton.setOnClickListener {
            binding.etSearch.text.clear()
            binding.clearButton.visibility = View.GONE
            viewModel.getSearchingNews(" ")
        }

        myAdapter.setOnItemClickListener {
            val bundle = Bundle().apply {
                putSerializable("article", it)
            }
            findNavController().navigate(
                R.id.action_searchNewsFragment_to_articleFragment,
                bundle
            )
        }
        var job: Job?= null
        binding.etSearch.addTextChangedListener { editable ->
            job?.cancel()
            job = MainScope().launch {
                delay(SEARCH_NEWS_TIME_DELAY)
                editable?.let {
                    if (it.toString().isNotEmpty()) {
                        binding.clearButton.visibility = View.VISIBLE
                        myAdapter.differ.submitList(emptyList())
                        viewModel.getSearchingNews(it.toString())
                    } else {
                        binding.clearButton.visibility = View.GONE
                        myAdapter.differ.submitList(emptyList())
                        viewModel.getSearchingNews(" ")

                    }
                }
            }
        }

        viewModel.searchNews.observe(viewLifecycleOwner, Observer { response ->
            when (response) {
                is Resource.Success -> {
                    hideProgressBar()
                    response.data?.let { NewsResponce ->
                        myAdapter.differ.submitList(NewsResponce.articles)
                        val totalPages = NewsResponce.totalResults / QUERY_PAGE_SIZE + 2
                        if(totalPages == viewModel.searchNewsPage){
                            binding.rvSearchNews.setPadding(0,0,0,0)
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

    val myOnScrollListener = object: RecyclerView.OnScrollListener() {
        override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {
            super.onScrollStateChanged(recyclerView, newState)
            if (newState == RecyclerView.SCROLL_STATE_DRAGGING) {
                isScrolling = true
            }
        }

        override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
            super.onScrolled(recyclerView, dx, dy)
            val layoutManager = recyclerView.layoutManager as LinearLayoutManager
            if (shouldPaginate(layoutManager)) {
                viewModel.getSearchingNews(binding.etSearch.text.toString())
            }
        }
        private fun shouldPaginate(layoutManager: LinearLayoutManager):Boolean {
            val firstVisibleItemPosition = layoutManager.findFirstVisibleItemPosition()
            val visibleItemCount = layoutManager.childCount
            val totalItemCount = layoutManager.itemCount

            val isAtLastItem = firstVisibleItemPosition + visibleItemCount >= totalItemCount
            val isNotAtBeginning = firstVisibleItemPosition >= 0
            val isTotalMoreThenVisible = totalItemCount >= QUERY_PAGE_SIZE
            return !isLoading && !isLastPage && isAtLastItem && isNotAtBeginning
                    && isTotalMoreThenVisible && isScrolling
        }
    }



    private fun setUpRecyclerView() {
        myAdapter = NewsAdapter()
        binding.rvSearchNews.apply {
            adapter = myAdapter
            layoutManager = LinearLayoutManager(activity)
            addOnScrollListener(myOnScrollListener)
        }
    }
}