package org.researchstack.backbone.interfaces

public interface ITaskPresenter {
    fun startPresenting()
    fun setDelegate(delegate: ITaskPresenterDelegate)
}

public interface ITaskPresenterDelegate {
    fun didStartPresenting(task: ITask)
    fun didFinishPresenting(task: ITask, result: IResult?)
}
