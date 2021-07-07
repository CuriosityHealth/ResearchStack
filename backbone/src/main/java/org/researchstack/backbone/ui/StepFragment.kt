package org.researchstack.backbone.ui

import android.support.v4.app.Fragment
import org.researchstack.backbone.interfaces.IResult
import org.researchstack.backbone.interfaces.IStep
import org.researchstack.backbone.ui.callbacks.StepCallbacks

interface StepFragment {

    fun initialize(step: IStep, result: IResult?)
    fun setCallbacks(callbacks: StepCallbacks)
    fun onBackPressed()
    val fragment: Fragment

}