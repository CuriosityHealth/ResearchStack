package org.researchstack.backbone.ui

import android.support.v4.app.Fragment
import org.researchstack.backbone.task.Task
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import org.researchstack.backbone.interfaces.ITaskPresenter
import org.researchstack.backbone.interfaces.ITaskProvider

class ViewTaskFragment(): Fragment() {

    companion object {
        val EXTRA_TASK_IDENTIFIER = "ViewTaskFragment.ExtraTaskIdentifier"
//        val EXTRA_TASK_RESULT = "ViewTaskFragment.ExtraTaskResult"
//        val EXTRA_STEP = "ViewTaskFragment.ExtraStep"

        fun newInstance(taskIdentifier: String): ViewTaskFragment {
            val fragment = ViewTaskFragment()
            val args = Bundle()
            args.putString(EXTRA_TASK_IDENTIFIER, taskIdentifier)
            fragment.setArguments(args)
            return fragment
        }
    }

    var taskProvider: ITaskProvider? = null

    override fun onCreateView(inflater: LayoutInflater?, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return super.onCreateView(inflater, container, savedInstanceState)
    }
}