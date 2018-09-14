package org.researchstack.backbone.ui

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.support.v7.app.AlertDialog
import android.support.v7.widget.Toolbar
import android.view.MenuItem
import android.view.inputmethod.InputMethodManager
import android.widget.Toast
import org.researchstack.backbone.R
import org.researchstack.backbone.interfaces.BackwardsCompatibleStepLayoutProvider
import org.researchstack.backbone.interfaces.ITaskPresenter
import org.researchstack.backbone.interfaces.ITaskPresenterDelegate
import org.researchstack.backbone.result.StepResult
import org.researchstack.backbone.result.TaskResult
import org.researchstack.backbone.step.Step
import org.researchstack.backbone.task.Task
import org.researchstack.backbone.ui.callbacks.StepCallbacks
import org.researchstack.backbone.ui.step.layout.StepLayout
import org.researchstack.backbone.ui.views.StepSwitcher
import java.util.*

class NewViewTaskActivity: PinCodeActivity(), StepCallbacks, ITaskPresenter {

    companion object {

        val EXTRA_TASK = "NewViewTaskActivity.ExtraTask"
        val EXTRA_TASK_RESULT = "NewViewTaskActivity.ExtraTaskResult"
        val EXTRA_STEP = "NewViewTaskActivity.ExtraStep"

        fun newIntent(context: Context, task: Task): Intent {
            val intent = Intent(context, NewViewTaskActivity::class.java);
            intent.putExtra(EXTRA_TASK, task);
            return intent;
        }
    }

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

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        super.setResult(Activity.RESULT_CANCELED)
        super.setContentView(R.layout.rsb_activity_task_viewer)

        val toolbar = findViewById(R.id.toolbar) as Toolbar?
        setSupportActionBar(toolbar)
        getSupportActionBar()!!.setDisplayHomeAsUpEnabled(true)

        this._root = findViewById(R.id.container) as StepSwitcher?

        if (savedInstanceState == null) {
            this._task = getIntent().getSerializableExtra(EXTRA_TASK) as Task
            this._taskResult = TaskResult(this.task.identifier)
            this.taskResult.setStartDate(Date())
        } else {
            this._task = savedInstanceState.getSerializable(EXTRA_TASK) as Task
            this._taskResult = savedInstanceState.getSerializable(EXTRA_TASK_RESULT) as TaskResult
            this._currentStep = savedInstanceState.getSerializable(EXTRA_STEP) as Step
        }

        this.task.validateParameters()

        this.task.onViewChange(Task.ViewChangeType.ActivityCreate, this, this.currentStep)
    }

    protected fun showNextStep() {
        val nextStep = task.getStepAfterStep(currentStep, taskResult)
        if (nextStep == null) {
            saveAndFinish()
        } else {
            showStep(nextStep)
        }
    }

    protected fun showPreviousStep() {
        val previousStep = task.getStepBeforeStep(currentStep, taskResult)
        if (previousStep == null) {
            finish()
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
        val title = task.getTitleForStep(this, step)
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

            val stepLayoutProvider = BackwardsCompatibleStepLayoutProvider()
            val stepLayout = stepLayoutProvider.stepLayout(step)?.let {
                val constructor = it.getConstructor(Context::class.java)
                constructor.newInstance(this)
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

    private fun saveAndFinish() {
        taskResult.setEndDate(Date())
        val resultIntent = Intent()
        resultIntent.putExtra(EXTRA_TASK_RESULT, taskResult)
        setResult(Activity.RESULT_OK, resultIntent)
        finish()
    }

    protected override fun onPause() {
        hideKeyboard()
        super.onPause()

        this.task.onViewChange(Task.ViewChangeType.ActivityPause, this, this.currentStep)
    }

    protected override fun onResume() {
        super.onResume()
        this.task.onViewChange(Task.ViewChangeType.ActivityResume, this, this.currentStep)
    }

    protected override fun onStop() {
        super.onStop()
        this.task.onViewChange(Task.ViewChangeType.ActivityStop, this, this.currentStep)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == android.R.id.home) {
            notifyStepOfBackPress()
            return true
        }

        return super.onOptionsItemSelected(item)
    }

    override fun onBackPressed() {
        notifyStepOfBackPress()
    }

    protected override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putSerializable(EXTRA_TASK, task)
        outState.putSerializable(EXTRA_TASK_RESULT, taskResult)
        outState.putSerializable(EXTRA_STEP, currentStep)
    }

    private fun notifyStepOfBackPress() {
        val currentStepLayout = findViewById(R.id.rsb_current_step) as StepLayout?
        currentStepLayout!!.isBackEventConsumed
    }

    override fun onDataReady() {
        super.onDataReady()
        this.startPresenting()
    }



    override fun startPresenting() {
        if (this.currentStep == null) {
            this._currentStep = task.getStepAfterStep(null, taskResult)
        }

        showStep(this.currentStep!!)
    }

    override fun setDelegate(delegate: ITaskPresenterDelegate) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun onDataFailed() {
        super.onDataFailed()
        Toast.makeText(this, R.string.rsb_error_data_failed, Toast.LENGTH_LONG).show()
        finish()
    }

    override fun onSaveStep(action: Int, step: Step, result: StepResult<*>?) {
        result?.let { onSaveStepResult(step.identifier, it) }
        onExecuteStepAction(action)
    }

    protected fun onSaveStepResult(id: String, result: StepResult<*>) {
        taskResult.setStepResultForStepIdentifier(id, result)
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

    private fun hideKeyboard() {
        val imm = getSystemService(Activity.INPUT_METHOD_SERVICE) as InputMethodManager?
        if (imm!!.isActive && imm.isAcceptingText) {
            imm.toggleSoftInput(InputMethodManager.HIDE_IMPLICIT_ONLY, 0)
        }
    }

    private fun showConfirmExitDialog() {
        val alertDialog = AlertDialog.Builder(this).setTitle(
                "Are you sure you want to exit?")
                .setMessage(R.string.lorem_medium)
                .setPositiveButton("End Task") { dialog, which -> finish() }
                .setNegativeButton("Cancel", null)
                .create()
        alertDialog.show()
    }

    override fun onCancelStep() {
        setResult(Activity.RESULT_CANCELED)
        finish()
    }

    fun setActionBarTitle(title: String) {
        val actionBar = getSupportActionBar()
        if (actionBar != null) {
            actionBar.setTitle(title)
        }
    }

}