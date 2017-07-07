package com.adgvcxz.recyclerviewmodel

import android.support.v7.util.DiffUtil
import android.support.v7.util.ListUpdateCallback
import android.support.v7.widget.RecyclerView
import android.support.v7.widget.RecyclerView.NO_POSITION
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.adgvcxz.IModel
import com.adgvcxz.WidgetLifeCircleEvent
import com.adgvcxz.addTo
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.functions.Consumer
import kotlin.reflect.KClass

/**
 * zhaowei
 * Created by zhaowei on 2017/6/5.
 */
class RecyclerAdapter(val viewModel: RecyclerViewModel,
                      private val configureItem: ((RecyclerItemViewModel<out IModel>) -> IView<*, *>)) :
        RecyclerView.Adapter<ItemViewHolder>(),
        Consumer<DiffUtil.DiffResult> {

    private var inflater: LayoutInflater? = null
    private var viewMap: HashMap<Int, IView<*, *>?> = HashMap()
    private val layoutMap: HashMap<KClass<RecyclerItemViewModel<out IModel>>, Int> = HashMap()
    var itemClickListener: View.OnClickListener? = null
    var notify: Boolean = false
    var loading: Boolean = false
    val disposables: CompositeDisposable by lazy { CompositeDisposable() }

    init {
        viewModel.model.map { it.items }
                .bindTo(this)
                .addTo(disposables)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ItemViewHolder {
        if (inflater == null) {
            inflater = LayoutInflater.from(parent.context)
        }
        val view = inflater!!.inflate(viewType, parent, false)
        ifNotNull(view, itemClickListener) { view, listener -> view.setOnClickListener(listener) }
        val views = viewMap[viewType]?.initView(view)
        views?.itemView = view
        return ItemViewHolder(views)
    }

    @Suppress("UNCHECKED_CAST")
    override fun onBindViewHolder(holder: ItemViewHolder, position: Int) {
        val iView = viewMap[getItemViewType(position)]
        ifNotNull(iView, holder.baseViewHolder) { iView, views ->
            (iView as IView<BaseViewHolder, RecyclerItemViewModel<out IModel>>).bind(views, viewModel.currentModel().items[position])
        }
        checkLoadMore(position)
    }

    override fun getItemCount(): Int {
        return viewModel.count
    }

    @Suppress("UNCHECKED_CAST")
    override fun getItemViewType(position: Int): Int {
        val model = viewModel.currentModel().items[position]
        var id = layoutMap[model::class]
        if (id == null) {
            val type = model::class as KClass<RecyclerItemViewModel<out IModel>>
            val view = configureItem.invoke(model)
            layoutMap.put(type, view.layoutId)
            id = view.layoutId
            viewMap.put(id, view)
        }
        return id
    }

    override fun onViewAttachedToWindow(holder: ItemViewHolder) {
        super.onViewAttachedToWindow(holder)
        if (holder.layoutPosition != NO_POSITION) {
            viewModel.currentModel().items[holder.layoutPosition].action.onNext(WidgetLifeCircleEvent.Attach)
        }
    }

    override fun onViewDetachedFromWindow(holder: ItemViewHolder) {
        super.onViewDetachedFromWindow(holder)
        if (holder.layoutPosition != NO_POSITION) {
//            viewModel.currentModel().items[holder.layoutPosition].action.onNext(WidgetLifeCircleEvent.Detach)
            holder.baseViewHolder?.disposables?.clear()
        }
    }


    fun checkLoadMore(position: Int) {
        val loadingModel = viewModel.currentModel().loadingViewModel
        loadingModel?.let {
            if ((position == itemCount - 1) && !viewModel.currentModel().isLoading && !loading) {
                viewModel.action.onNext(RecyclerViewModel.Event.loadMore)
                loading = true
            } else {
                loading = false
            }
        }
    }


    override fun onAttachedToRecyclerView(recyclerView: RecyclerView?) {
        super.onAttachedToRecyclerView(recyclerView)
        recyclerView?.addOnAttachStateChangeListener(object : View.OnAttachStateChangeListener {
            override fun onViewDetachedFromWindow(v: View?) {
                viewModel.currentModel().items.forEach { it.dispose() }
                disposables.dispose()
            }

            override fun onViewAttachedToWindow(v: View?) {
            }
        })
    }

    override fun accept(result: DiffUtil.DiffResult) {
        if (viewModel.currentModel().isAnim && !notify) {
            result.dispatchUpdatesTo(this)
        } else {
            result.dispatchUpdatesTo(object : ListUpdateCallback {
                override fun onChanged(position: Int, count: Int, payload: Any?) {
                    notifyDataSetChanged()
                }

                override fun onMoved(fromPosition: Int, toPosition: Int) {
                    notifyDataSetChanged()
                }

                override fun onInserted(position: Int, count: Int) {
                    notifyDataSetChanged()
                }

                override fun onRemoved(position: Int, count: Int) {
                    notifyDataSetChanged()
                }
            })
        }
        notify = viewModel.currentModel().isRefresh
    }
}