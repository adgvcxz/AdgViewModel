package com.adgvcxz.recyclerviewmodel

import android.view.View
import com.adgvcxz.IModel
import com.adgvcxz.WidgetViewModel

/**
 * zhaowei
 * Created by zhaowei on 2017/6/5.
 */
interface IView<V: Views, in M: WidgetViewModel<out IModel>> {

    val layoutId: Int

    @Suppress("UNCHECKED_CAST")
    fun initView(view: View): V {
        return Views() as V
    }

    fun bind(view: V, viewModel: M) {

    }
}