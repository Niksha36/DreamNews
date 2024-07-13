package com.example.dailynews.ui.fragments
import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.dailynews.R
import com.example.dailynews.adapters.NewsAdapter
import com.example.dailynews.databinding.FragmentSavedNewsBinding
import com.example.dailynews.ui.NewsActivity
import com.example.dailynews.ui.NewsViewModel

class SavedNewsFragment: Fragment(R.layout.fragment_saved_news) {
    lateinit var viewModel: NewsViewModel
    lateinit var myAdapter:NewsAdapter
    lateinit var binding:FragmentSavedNewsBinding
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        viewModel = (activity as NewsActivity).viewModel
        binding = FragmentSavedNewsBinding.bind(view)
        setUpRecyclerView()

        myAdapter.setOnItemClickListener {
            val bundle = Bundle().apply {
                putSerializable("article", it)
            }
            findNavController().navigate(
                R.id.action_savedNewsFragment_to_articleFragment,
                bundle
            )
        }
    }
    private fun setUpRecyclerView() {
        myAdapter = NewsAdapter()
        binding.rvSavedNews.apply {
            adapter = myAdapter
            layoutManager = LinearLayoutManager(activity)
        }
    }
}