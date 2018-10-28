package org.researchstack.backbone.ui

import android.content.Context
import android.os.Bundle
import android.support.v4.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import org.researchstack.backbone.R
import org.researchstack.backbone.interfaces.IStep
import org.researchstack.backbone.interfaces.IStepFragmentProvider
import org.researchstack.backbone.interfaces.IStepLayoutProvider
import org.researchstack.backbone.result.StepResult
import org.researchstack.backbone.step.Step
import org.researchstack.backbone.ui.callbacks.StepCallbacks
import org.researchstack.backbone.ui.step.layout.StepLayout

public class BackwardsCompatibleStepFragmentProvider(val stepLayoutProvider: IStepLayoutProvider): IStepFragmentProvider {

    override fun stepFragment(context: Context, step: IStep): StepFragment? {

        try {

            val stepLayout = this.stepLayoutProvider.stepLayout(step)?.let {
                val constructor = it.getConstructor(Context::class.java)
                constructor.newInstance(context)
            } as? StepLayout

            if (stepLayout != null) {
                val fragment = BackwardsCompatibleStepFragment.newInstance(step.identifier, stepLayout)
                return fragment
            }
            else {
                throw RuntimeException("Could not instantiate Step Layout")
            }

        } catch (e: Exception) {
            throw RuntimeException(e)
        }

        return null
    }
}

public class BackwardsCompatibleStepFragment(): Fragment(), StepFragment {

    companion object {
        val EXTRA_TASK_IDENTIFIER = "BackwardsCompatibleStepFragment.ExtraTaskIdentifier"

        fun newInstance(taskIdentifier: String, stepLayout: StepLayout): BackwardsCompatibleStepFragment {
            val fragment = BackwardsCompatibleStepFragment()
            val args = Bundle()
            args.putString(EXTRA_TASK_IDENTIFIER, taskIdentifier)
            fragment.setArguments(args)
            fragment.stepLayout = stepLayout
            return fragment
        }
    }

    //this will implement the traditional step layout
    var stepLayout: StepLayout? = null

    override val fragment: Fragment
        get() = this

    private fun getLayoutParams(stepLayout: StepLayout): FrameLayout.LayoutParams {
        var lp: FrameLayout.LayoutParams? = stepLayout.layout.layoutParams?.let {
            it as? FrameLayout.LayoutParams
        }
        if (lp == null) {
            lp = FrameLayout.LayoutParams(FrameLayout.LayoutParams.MATCH_PARENT, FrameLayout.LayoutParams.MATCH_PARENT)
        }
        return lp
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {

        val layout = this.stepLayout!!
        val view = inflater.inflate(R.layout.rsb_fragment_child, container, false)
        val containerView: FrameLayout = view.findViewById(R.id.rsb_content_child) as FrameLayout

        val lp = getLayoutParams(layout)

//        I think that this is probably the problem here...
        containerView.addView(layout.layout, 0, lp)
//        layout.getLayout().id = R.id.rsb_current_step

        return view
    }

    override fun onBackPressed() {
        this.stepLayout!!.isBackEventConsumed
    }

    override fun initialize(step: IStep, result: StepResult<*>?) {
        this.stepLayout!!.initialize(step as Step, result)
    }

    override fun setCallbacks(callbacks: StepCallbacks) {
        this.stepLayout!!.setCallbacks(callbacks)
    }

}