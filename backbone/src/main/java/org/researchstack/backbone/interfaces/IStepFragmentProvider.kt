package org.researchstack.backbone.interfaces

import android.content.Context
import org.researchstack.backbone.ui.StepFragment

interface IStepFragmentProvider {
    fun stepFragment(context: Context, step: IStep): StepFragment?
}