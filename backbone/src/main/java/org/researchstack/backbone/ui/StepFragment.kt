package org.researchstack.backbone.ui

import android.support.v4.app.Fragment
import org.researchstack.backbone.interfaces.IStep
import org.researchstack.backbone.result.StepResult
import org.researchstack.backbone.ui.callbacks.StepCallbacks

interface StepFragment {

    fun initialize(step: IStep, result: StepResult<*>?)
    fun setCallbacks(callbacks: StepCallbacks)
    fun onBackPressed()
    val fragment: Fragment

}