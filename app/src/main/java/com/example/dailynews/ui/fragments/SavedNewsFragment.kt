package com.example.dailynews.ui.fragments
import android.content.Context
import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.dailynews.R
import com.example.dailynews.adapters.SavedNewsAdapter
import com.example.dailynews.databinding.FragmentSavedNewsBinding
import com.example.dailynews.ui.NewsActivity
import com.example.dailynews.ui.NewsViewModel
import com.example.dailynews.util.AuthStates
import com.google.android.material.snackbar.Snackbar

class SavedNewsFragment: Fragment(R.layout.fragment_saved_news) {
    lateinit var viewModel: NewsViewModel
    lateinit var myAdapter:SavedNewsAdapter
    lateinit var binding:FragmentSavedNewsBinding
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        viewModel = (activity as NewsActivity).viewModel
        binding = FragmentSavedNewsBinding.bind(view)
        setUpRecyclerView()
        //Оттображаем сохраненные статьи из фрагментов
        viewModel.getSavedArticles().observe(viewLifecycleOwner, Observer {
            myAdapter.differ.submitList(it)
        })
        //Добавим возможность удалять новости свайпом влево или вправо
        val sharedPreferences = requireActivity().getSharedPreferences("app_prefs", Context.MODE_PRIVATE)
        val authState = sharedPreferences.getString("auth_state", null)

        val itemTouchHelperCallBack = object: ItemTouchHelper.SimpleCallback(
            ItemTouchHelper.UP or ItemTouchHelper.DOWN,
            ItemTouchHelper.LEFT or ItemTouchHelper.RIGHT
        ){
            override fun onMove(
                recyclerView: RecyclerView,
                viewHolder: RecyclerView.ViewHolder,
                target: RecyclerView.ViewHolder
            ): Boolean {
                return true
            }

            override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
                val position = viewHolder.adapterPosition
                val articleToDelete = myAdapter.differ.currentList[position]
                viewModel.deleteArticle(articleToDelete)
                if (authState == AuthStates.AUTHENTICATED.name) {viewModel.deleteFromFireStore(articleToDelete)}
                val snackbar = Snackbar.make(view, "Article was successfully deleted", Snackbar.LENGTH_LONG).apply {
                    setAction("Undo") {
                        viewModel.saveArticle(articleToDelete)
                        if (authState == AuthStates.AUTHENTICATED.name) {viewModel.saveToFireStore(articleToDelete)}
                    }
                }
                snackbar.anchorView = (activity as NewsActivity).findViewById(R.id.bottomNavigationView)
                snackbar.show()
            }
        }
        ItemTouchHelper(itemTouchHelperCallBack).attachToRecyclerView(binding.rvSavedNews)

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
        myAdapter = SavedNewsAdapter(viewModel.email)
        binding.rvSavedNews.apply {
            adapter = myAdapter
            layoutManager = LinearLayoutManager(activity)
        }
    }
}