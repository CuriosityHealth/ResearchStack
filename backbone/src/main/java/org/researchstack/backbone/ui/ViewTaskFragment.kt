package org.researchstack.backbone.ui

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.support.v4.app.Fragment
import org.researchstack.backbone.task.Task
import android.os.Bundle
import android.support.v7.app.AlertDialog
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.Toolbar
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import org.researchstack.backbone.R
import org.researchstack.backbone.interfaces.*
import org.researchstack.backbone.result.StepResult
import org.researchstack.backbone.result.TaskResult
import org.researchstack.backbone.step.Step
import org.researchstack.backbone.ui.callbacks.StepCallbacks
import org.researchstack.backbone.ui.step.layout.StepLayout
import org.researchstack.backbone.ui.views.StepSwitcher
import java.util.*

class ViewTaskFragment(): Fragment(), StepCallbacks, ITaskPresenter {

    companion object {
        val EXTRA_TASK_IDENTIFIER = "ViewTaskFragment.ExtraTaskIdentifier"
        val EXTRA_TASK_RESULT = "ViewTaskFragment.ExtraTaskResult"
        val EXTRA_STEP = "ViewTaskFragment.ExtraStep"

        fun newInstance(taskIdentifier: String): ViewTaskFragment {
            val fragment = ViewTaskFragment()
            val args = Bundle()
            args.putString(EXTRA_TASK_IDENTIFIER, taskIdentifier)
            fragment.setArguments(args)
            return fragment
        }
    }

    public var taskProvider: ITaskProvider? = null
    public var stepLayoutProvider: INewStepLayoutProvider? = null

    var _root: StepSwitcher? = null
    val root: StepSwitcher
        get() = this._root!!

    var _currentStep: Step? = null

    val currentStep: Step?
        get() = this._currentStep


    var _task: Task? = null
    val task: Task
        get() = this._task!!

    var _taskResult: TaskResult? = null
    val taskResult: TaskResult
        get() = this._taskResult!!

    var taskPresentaterDelegate: ITaskPresenterDelegate? = null

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {

        val view = inflater.inflate(R.layout.rsb_fragment_task_viewer, container, false);

        val toolbar = view.findViewById(R.id.toolbar) as Toolbar?

        val appCompatActivity: AppCompatActivity = this.activity as AppCompatActivity
        appCompatActivity.setSupportActionBar(toolbar)
        appCompatActivity.supportActionBar!!.setDisplayHomeAsUpEnabled(true)

        this._root = view.findViewById(R.id.container) as StepSwitcher?

        if (savedInstanceState == null) {
            val taskIdentifier = arguments.getString(ViewTaskFragment.EXTRA_TASK_IDENTIFIER)
            this._task = this.taskProvider!!.task(taskIdentifier) as Task
            this._taskResult = TaskResult(this.task.identifier)
            this.taskResult.setStartDate(Date())
        } else {
            val taskIdentifier = savedInstanceState.getString(ViewTaskFragment.EXTRA_TASK_IDENTIFIER)
            this._task = this.taskProvider!!.task(taskIdentifier) as Task
            this._taskResult = savedInstanceState.getSerializable(NewViewTaskActivity.EXTRA_TASK_RESULT) as TaskResult
            this._currentStep = savedInstanceState.getSerializable(NewViewTaskActivity.EXTRA_STEP) as Step
        }

        this.task.validateParameters()

        return view
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        this.task.onViewChange(Task.ViewChangeType.ActivityCreate, this, this.currentStep)
        super.onActivityCreated(savedInstanceState)
    }

    override fun startPresenting() {
        if (this.currentStep == null) {
            this._currentStep = task.getStepAfterStep(null, taskResult)
        }

        showStep(this.currentStep!!)

        this.taskPresentaterDelegate?.didStartPresenting(this.task)
    }

    override fun setDelegate(delegate: ITaskPresenterDelegate) {
        this.taskPresentaterDelegate = delegate
    }

    protected fun showNextStep() {
        val nextStep = task.getStepAfterStep(currentStep, taskResult)
        if (nextStep == null) {
            this.saveAndFinish(false)
        } else {
            showStep(nextStep)
        }
    }

    protected fun showPreviousStep() {
        val previousStep = task.getStepBeforeStep(currentStep, taskResult)
        if (previousStep == null) {
            saveAndFinish(true)
        } else {
            showStep(previousStep)
        }
    }

    private fun showStep(step: Step) {
        val currentStepPosition = task.getProgressOfCurrentStep(currentStep, taskResult)
                .current
        val newStepPosition = task.getProgressOfCurrentStep(step, taskResult).current

        val stepLayout = getLayoutForStep(step)
        stepLayout.layout.setTag(R.id.rsb_step_layout_id, step.identifier)
        root.show(stepLayout,
                if (newStepPosition >= currentStepPosition)
                    StepSwitcher.SHIFT_LEFT
                else
                    StepSwitcher.SHIFT_RIGHT)
        this._currentStep = step
    }

    protected fun getLayoutForStep(step: Step): StepLayout {
        // Change the title on the activity
        val title = task.getTitleForStep(this.activity, step)
        setActionBarTitle(title)

        // Get result from the TaskResult, can be null
        val result = taskResult.getStepResult(step.identifier)

        // Return the Class & constructor
        val stepLayout = createLayoutFromStep(step)
        stepLayout.initialize(step, result)
        stepLayout.setCallbacks(this)

        return stepLayout
    }

    private fun createLayoutFromStep(step: Step): StepLayout {
        try {

            val stepLayout = this.stepLayoutProvider?.stepLayout(step)?.let {
                val constructor = it.getConstructor(Context::class.java)
                constructor.newInstance(this.activity)
            } as? StepLayout

            if (stepLayout != null) {
                return stepLayout
            }
            else {
                throw RuntimeException("Could not instantiate Step Layout")
            }

        } catch (e: Exception) {
            throw RuntimeException(e)
        }

    }

    private fun saveAndFinish(clearResult: Boolean) {
        if (clearResult) {
            this.taskPresentaterDelegate?.didFinishPresenting(this.task, null)
        }
        else {
            this.taskResult.setEndDate(Date())
            this.taskPresentaterDelegate?.didFinishPresenting(this.task, this.taskResult)
        }
    }

    fun setActionBarTitle(title: String) {

        val appCompatActivity: AppCompatActivity = this.activity as AppCompatActivity

        val actionBar = appCompatActivity.supportActionBar
        if (actionBar != null) {
            actionBar.setTitle(title)
        }
    }

    protected fun onExecuteStepAction(action: Int) {
        if (action == StepCallbacks.ACTION_NEXT) {
            showNextStep()
        } else if (action == StepCallbacks.ACTION_PREV) {
            showPreviousStep()
        } else if (action == StepCallbacks.ACTION_END) {
            showConfirmExitDialog()
        } else if (action == StepCallbacks.ACTION_NONE) {
            // Used when onSaveInstanceState is called of a view. No action is taken.
        } else {
            throw IllegalArgumentException("Action with value " + action + " is invalid. " +
                    "See StepCallbacks for allowable arguments")
        }
    }

    private fun showConfirmExitDialog() {
        val alertDialog = AlertDialog.Builder(this.activity).setTitle(
                "Are you sure you want to exit?")
                .setMessage(R.string.lorem_medium)
                .setPositiveButton("End Task") { dialog, which -> saveAndFinish(true) }
                .setNegativeButton("Cancel", null)
                .create()
        alertDialog.show()
    }

    protected fun onSaveStepResult(id: String, result: StepResult<*>) {
        taskResult.setStepResultForStepIdentifier(id, result)
    }

    override fun onSaveStep(action: Int, step: Step, result: StepResult<*>?) {
        result?.let { onSaveStepResult(step.identifier, it) }
        onExecuteStepAction(action)
    }

    override fun onCancelStep() {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }
}