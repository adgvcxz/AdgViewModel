package com.adgvcxz.viewmodel.sample

import android.view.View
import android.widget.TextView
import com.adgvcxz.IModel
import com.adgvcxz.WidgetViewModel
import com.adgvcxz.recyclerviewmodel.*
import com.jakewharton.rxbinding2.view.visibility
import com.jakewharton.rxbinding2.widget.text
import io.reactivex.Observable
import kotlinx.android.synthetic.main.item_loading.view.*
import java.util.concurrent.TimeUnit

/**
 * zhaowei
 * Created by zhaowei on 2017/6/6.
 */

class SimpleRecyclerViewModel : RecyclerViewModel() {

    override val initModel: Model = Model((0 until 2).map { TextItemViewModel() }, true, false)


    override fun request(refresh: Boolean): Observable<ListResult> {
//        if (Random().nextInt() < 16) {
            return Observable.timer(1, TimeUnit.SECONDS)
                    .map { (0 until 10).map { TextItemViewModel() } }
                    .map { ListResult(it) }
//        } else {
//            return Observable.timer(1, TimeUnit.SECONDS)
//                    .map { ListResult(null) }
//        }
    }
}

class TextItemView : IView<TextItemView.TextItemViews, TextItemViewModel> {
    override val layoutId: Int = R.layout.item_text_view

    class TextItemViews: Views() {
        lateinit var content: TextView
    }



    override fun initView(view: View): TextItemViews {
        return TextItemViews().also {
            it.content = view.findViewById(R.id.textView) as TextView
        }
    }

    override fun bind(view: TextItemViews, viewModel: TextItemViewModel) {
        viewModel.model.map { it.content }
                .distinctUntilChanged()
                .subscribe(view.content.text())
    }
}

var a = 1

class TextItemViewModel : WidgetViewModel<TextItemViewModel.Model>() {
    override val initModel: Model = Model()

    class Model : IModel {
        val content: String = "$a"
        init {
            a++
        }
    }
}

class LoadingItemView : IDefaultView<LoadingItemViewModel> {

    override val layoutId: Int = R.layout.item_loading

    override fun bind(view: Views, viewModel: LoadingItemViewModel) {
        viewModel.model.map { it.state }
                .map { it != LoadingItemViewModel.State.failure }
                .subscribe(view.itemView.loading.visibility())
        viewModel.model.map { it.state }
                .map { it == LoadingItemViewModel.State.failure }
                .subscribe(view.itemView.failed.visibility())
    }
}