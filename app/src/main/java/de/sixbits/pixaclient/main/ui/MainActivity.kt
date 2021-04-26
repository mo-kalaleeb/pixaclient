package de.sixbits.pixaclient.main.ui

import android.content.Intent
import android.os.Bundle
import android.view.View.*
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.OrientationHelper
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.integration.recyclerview.RecyclerViewPreloader
import com.bumptech.glide.util.ViewPreloadSizeProvider
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import de.sixbits.pixaclient.MyApplication
import de.sixbits.pixaclient.databinding.ActivityMainBinding
import de.sixbits.pixaclient.main.MainComponent
import de.sixbits.pixaclient.main.adapters.SearchResultRecyclerAdapter
import de.sixbits.pixaclient.main.callbacks.OnImageClickListener
import de.sixbits.pixaclient.main.keys.IntentKeys
import de.sixbits.pixaclient.main.utils.NetworkUtils
import de.sixbits.pixaclient.main.view_model.MainViewModel
import de.sixbits.pixaclient.network.model.ImageListItemModel
import javax.inject.Inject

class MainActivity : AppCompatActivity(), OnImageClickListener {
    @Inject
    lateinit var viewModelFactory: ViewModelProvider.Factory

    private lateinit var mainComponent: MainComponent
    private lateinit var mainViewModel: MainViewModel
    private lateinit var binding: ActivityMainBinding

    private lateinit var searchRecyclerAdapter: SearchResultRecyclerAdapter

    private var loading = true
    private lateinit var activeLayoutManager: GridLayoutManager
    private var canLoadMorePages = true


    override fun onCreate(savedInstanceState: Bundle?) {
        mainComponent = (application as MyApplication)
            .appComponent
            .mainComponent()
            .create()
        mainComponent.inject(this)

        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        initViewModel()
        initViews()
        initRecyclerView()
    }

    private fun initViewModel() {
        mainViewModel = ViewModelProvider(
            this,
            viewModelFactory
        ).get(MainViewModel::class.java)

        // Handle data response
        mainViewModel.searchImagesLiveData.observe(this, {
            searchRecyclerAdapter.switchItems(it)
        })
        mainViewModel.pagerLiveData.observe(this, {
            if (it.isNotEmpty()) {
                searchRecyclerAdapter.addItemsToCurrent(it)
            } else {
                canLoadMorePages = false
            }
        })

        // Handle Loading Events
        mainViewModel.loadingLiveData.observe(this, {
            if (it) {
                binding.pbLoadingSearchResult.visibility = VISIBLE
                binding.rvSearchResult.visibility = INVISIBLE
            } else {
                binding.pbLoadingSearchResult.visibility = GONE
                binding.rvSearchResult.visibility = VISIBLE
            }
        })
    }

    private fun initViews() {
        activeLayoutManager =
            if (resources.configuration.orientation == OrientationHelper.HORIZONTAL) {
                GridLayoutManager(this, 2)
            } else {
                GridLayoutManager(this, 1)
            }

        binding.rvSearchResult.layoutManager = activeLayoutManager

        if (NetworkUtils.isInternetAvailable(this)) {
            binding.etSearchBar.setOnSearchClickListener {
                // Request the search
                mainViewModel.searchFor(binding.etSearchBar.query.toString())
            }
            binding.etSearchBar.setQuery("Fruits", true)
            mainViewModel.searchFor(binding.etSearchBar.query.toString())
        } else {
            binding.etSearchBar.isClickable = false
            binding.etSearchBar.setQuery("Offline Mode", true)
            mainViewModel.getCachedImages()
        }
    }

    private fun initRecyclerView() {
        // For Preloading images
        val searchRecyclerRequestBuilder = Glide
            .with(this)
            .asDrawable()
        val preloadSizeProvider = ViewPreloadSizeProvider<ImageListItemModel>()

        // initially we have no items
        searchRecyclerAdapter = SearchResultRecyclerAdapter(
            listOf(),
            searchRecyclerRequestBuilder,
            this
        )

        // For Preloading  images
        val preLoader = RecyclerViewPreloader(
            Glide.with(this),
            searchRecyclerAdapter,
            preloadSizeProvider,
            4
        )

        // Attach the adapter
        binding.rvSearchResult.adapter = searchRecyclerAdapter
        searchRecyclerAdapter.stateRestorationPolicy =
            RecyclerView.Adapter.StateRestorationPolicy.PREVENT_WHEN_EMPTY

        // attaching the preLoader
        binding.rvSearchResult.addOnScrollListener(preLoader)

        // Attach Infinite Scroll
        binding.rvSearchResult.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                if (dy > 0) { //check for scroll down
                    val visibleItemCount = activeLayoutManager.childCount
                    val totalItemCount = activeLayoutManager.itemCount
                    val pastVisiblesItems = activeLayoutManager.findFirstVisibleItemPosition()

                    // To save network requests
                    if (loading && canLoadMorePages &&
                        (visibleItemCount + pastVisiblesItems) >= totalItemCount
                    ) {
                        mainViewModel.requestMoreImage()
                    }
                }
            }
        })
    }

    override fun onClick(id: Int) {
        MaterialAlertDialogBuilder(this)
            .setMessage("Are you sure you want to view this entry?")
            .setPositiveButton("Yes") { _, _ ->
                val intent = Intent(this, DetailsActivity::class.java)
                intent.putExtra(IntentKeys.DETAILS_ID_KEY, id)
                startActivity(intent)
            }
            .setNegativeButton("No") { _, _ ->
                // Respond to positive button press
            }
            .show()
    }
}