package com.adgvcxz

//import android.arch.lifecycle.Lifecycle
//import android.arch.lifecycle.OnLifecycleEvent
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.subjects.PublishSubject
import io.reactivex.subjects.Subject

/**
 * zhaowei
 * Created by zhaowei on 2017/6/3.
 */
abstract class WidgetViewModel<M : IModel> : IViewModel<M> {

    var action: Subject<IEvent> = PublishSubject.create<IEvent>().toSerialized()

    //initialization initModel maybe have some parameters, so there is no abstract method initModel()
    abstract val initModel: M

    private var _currentModel: M? = null

    val model: Observable<M> by lazy {
        this.action
                .flatMap { this.mutate(it) }
                .compose { transform(it) }
                .scan(initModel) { model, mutation -> scan(model, mutation) }
                .doOnError { it.printStackTrace() }
                .retry()
                .observeOn(AndroidSchedulers.mainThread())
                .doOnNext { _currentModel = it }
                .replay(1)
                .refCount()
    }

    fun currentModel(): M {
        return _currentModel ?: initModel
    }

//    @OnLifecycleEvent(Lifecycle.Event.ON_CREATE)
//    fun onCreate() {
//        this.action.onNext(WidgetLifeCircleEvent.Attach)
//    }
//
//    @OnLifecycleEvent(Lifecycle.Event.ON_DESTROY)
//    fun onDestroy() {
//        this.action.onNext(WidgetLifeCircleEvent.Detach)
//    }
}
