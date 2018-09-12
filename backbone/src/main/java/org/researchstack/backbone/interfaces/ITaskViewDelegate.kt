package org.researchstack.backbone.interfaces

import org.researchstack.backbone.step.Step
import org.researchstack.backbone.task.Task

interface ITaskViewDelegate {

    fun onViewChange(type: Task.ViewChangeType, presenter: ITaskPresenter, currentStep: Step?) {

    }

}