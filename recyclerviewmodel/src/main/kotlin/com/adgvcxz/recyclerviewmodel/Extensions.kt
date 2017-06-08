package com.adgvcxz.recyclerviewmodel

import android.support.v7.util.DiffUtil
import com.adgvcxz.IModel
import com.adgvcxz.WidgetViewModel
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.Disposable
import io.reactivex.schedulers.Schedulers

/**
 * zhaowei
 * Created by zhaowei on 2017/5/12.
 */

fun Observable<List<WidgetViewModel<out IModel>>>.bindTo(adapter: RecyclerAdapter): Disposable {
    return this.observeOn(Schedulers.computation())
            .scan(Pair<List<WidgetViewModel<out IModel>>,
                    DiffUtil.DiffResult?>(adapter.viewModel.currentModel.items, null)) { (first), list ->
                val diff = ItemDiffCallback(first, list)
                val result = DiffUtil.calculateDiff(diff, true)
                Pair(list, result)
            }
            .skip(1)
            .map { it.second }
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe(adapter)
}
