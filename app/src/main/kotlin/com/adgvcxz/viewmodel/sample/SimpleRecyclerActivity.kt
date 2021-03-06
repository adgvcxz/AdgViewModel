package com.adgvcxz.viewmodel.sample

import android.content.Intent
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.LinearLayoutManager
import com.adgvcxz.*
import com.adgvcxz.recyclerviewmodel.RecyclerAdapter
import com.adgvcxz.recyclerviewmodel.Refresh
import com.adgvcxz.recyclerviewmodel.ReplaceData
import com.adgvcxz.recyclerviewmodel.itemClicks
import com.jakewharton.rxbinding2.support.v4.widget.refreshes
import io.reactivex.disposables.CompositeDisposable
import kotlinx.android.synthetic.main.activity_simple_recycler.*


class SimpleRecyclerActivity : AppCompatActivity() {

    private val disposables: CompositeDisposable by lazy { CompositeDisposable() }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_simple_recycler)
        initViewModel()
    }

    private fun initViewModel() {
        val viewModel = SimpleRecyclerViewModel()
        val adapter = RecyclerAdapter(viewModel) {
            when (it) {
                is TextItemViewModel -> TextItemView()
                else -> LoadingItemView()
            }
        }
        recyclerView.layoutManager = LinearLayoutManager(this)
        recyclerView.adapter = adapter
//        recyclerView.setHasFixedSize(true)

        viewModel.toEventBind(disposables) {
            add({ refreshes() }, refreshLayout, { Refresh })
        }

        adapter.itemClicks()
                .filter { viewModel.currentModel().items[it].currentModel() is TextItemViewModel.Model }
                .map { viewModel.currentModel().items[it].currentModel() as TextItemViewModel.Model }
                .subscribe {
                    if (it.id == 0) {
                        val intent = Intent(this, TestActivity::class.java)
                        intent.putExtra("id", it.id)
                        intent.putExtra("value", it.content)
                        startActivity(intent)
                    } else {
                        viewModel.action.onNext(ReplaceData(arrayListOf(it.id), arrayListOf(TextItemViewModel())))
                    }
                }
                .addTo(disposables)

//        adapter.itemClicks()
//                .filter { it != adapter.itemCount - 1 }
//                .map { RecyclerViewModel.RemoveDataEvent(arrayListOf(it)) }
//                .bindTo(viewModel.action)
//                .addTo(disposables)

//        adapter.itemClicks()
//                .filter { it != adapter.itemCount - 1 }
//                .subscribe { adapter.notifyDataSetChanged() }
//                .addTo(disposables)

//        adapter.itemClicks()
//                .filter { it != adapter.itemCount - 1 }
//                .map { RecyclerViewModel.ReplaceDataEvent(arrayListOf(it), arrayListOf(TextItemViewModel())) }
//                .bindTo(viewModel.action)
//                .addTo(disposables)

        viewModel.toBind(disposables) {
            add({ isRefresh }, { refreshLayout.isRefreshing = this })
        }


        viewModel.action.onNext(Refresh)
    }

    override fun onDestroy() {
        super.onDestroy()
        disposables.dispose()
    }
}
