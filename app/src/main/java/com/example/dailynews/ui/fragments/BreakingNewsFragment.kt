package com.example.dailynews.ui.fragments

import android.content.res.ColorStateList
import android.os.Bundle
import android.view.View
import android.widget.Button
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
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
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        viewModel = (activity as NewsActivity).viewModel
        binding = FragmentBreakingNewsBinding.bind(view)
//        adding swipe refresh
        val swipeRefreshLayout = binding.swipeRefreshLayout
        swipeRefreshLayout.setOnRefreshListener {
            viewModel.breakingNewsPage = 1
            viewModel.breakingNewsResponses = null
            viewModel.getBreakingNews("us", viewModel.category.value!!)
            swipeRefreshLayout.isRefreshing = false // Hide the refresh icon after refreshing
        }
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
                        if (isLastPage) {
                            binding.rvBreakingNews.setPadding(0, 0, 0, 0)
                        }
                    }
                }

                is Resource.Error -> {
                    hideProgressBar()
                    response.message?.let {
                        val snackbar =
                            Snackbar.make(view, "An error occurred: $it", Snackbar.LENGTH_LONG)
                        snackbar.anchorView =
                            (activity as NewsActivity).findViewById(R.id.bottomNavigationView)
                        snackbar.show()
                    }
                }

                is Resource.Loading -> {
                    showProgressBar()
                }
            }
        })

        //обрабатываю снопки из scroll view
        setCategoryClickListener(binding.businessButton, "business")
        setCategoryClickListener(binding.entertainmentButton, "entertainment")
        setCategoryClickListener(binding.generalButton, "general")
        setCategoryClickListener(binding.healthButton, "health")
        setCategoryClickListener(binding.scienceButton, "science")
        setCategoryClickListener(binding.sportsButton, "sports")
        setCategoryClickListener(binding.technologyButton, "technology")

        viewModel.activeButtonId.observe(viewLifecycleOwner, Observer {
            updateButtonColors(it)
        })
    }

    private fun updateButtonColors(activeButtonId:Int?) {
        val allButtons = listOf(
            binding.businessButton,
            binding.entertainmentButton,
            binding.generalButton,
            binding.healthButton,
            binding.scienceButton,
            binding.sportsButton,
            binding.technologyButton
        )

        allButtons.forEach { button ->
            if (button.id == activeButtonId) {
                button.backgroundTintList = ColorStateList.valueOf(
                    ContextCompat.getColor(requireContext(), R.color.my_light_active)
                )
            } else {
                button.backgroundTintList = ColorStateList.valueOf(
                    ContextCompat.getColor(requireContext(), R.color.blue)
                )
            }
        }
    }


    private fun setCategoryClickListener(button: Button, category: String) {
        button.setOnClickListener {
            if (viewModel.category.value != category) {
                viewModel.category.value = category
                viewModel.activeButtonId.value = button.id

                viewModel.breakingNewsPage = 1
                viewModel.breakingNewsResponses = null
            } else {
                viewModel.breakingNewsPage = 1
                viewModel.breakingNewsResponses = null

                viewModel.activeButtonId.value = null
                viewModel.category.value = ""
            }
            myAdapter.differ.submitList(emptyList())
            viewModel.getBreakingNews("us", viewModel.category.value!!)
        }
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

    val myOnScrollListener = object : RecyclerView.OnScrollListener() {
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
                viewModel.getBreakingNews("us", viewModel.category.value.toString())

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