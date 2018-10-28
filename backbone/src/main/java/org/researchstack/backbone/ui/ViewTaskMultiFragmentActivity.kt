
package org.researchstack.backbone.ui

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.MenuItem
import android.widget.Toast
import org.researchstack.backbone.R
import org.researchstack.backbone.interfaces.*
import org.researchstack.backbone.result.TaskResult
import org.researchstack.backbone.step.Step
import org.researchstack.backbone.task.Task
import java.util.*

class ViewTaskMultiFragmentActivity: PinCodeActivity(), ITaskProvider, ITaskPresenterDelegate {

    companion object {

        val EXTRA_TASK = "ViewTaskMultiFragmentActivity.ExtraTask"
        val EXTRA_TASK_RESULT = "ViewTaskMultiFragmentActivity.ExtraTaskResult"

        fun newIntent(context: Context, task: Task): Intent {
            val intent = Intent(context, ViewTaskMultiFragmentActivity::class.java);
            intent.putExtra(EXTRA_TASK, task);
            return intent;
        }
    }

    var _task: Task? = null
    val task: Task
        get() = this._task!!

    var viewTaskFragment: ViewTaskMultiFragment? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        super.setResult(Activity.RESULT_CANCELED)
        this.setContentView(R.layout.rsb_activity_multi_fragment_task_viewer)

        if (savedInstanceState == null) {
            this._task = getIntent().getSerializableExtra(ViewTaskMultiFragmentActivity.EXTRA_TASK) as Task
        } else {
            this._task = savedInstanceState.getSerializable(ViewTaskMultiFragmentActivity.EXTRA_TASK) as Task
        }

        if (savedInstanceState == null) {
            val transaction = supportFragmentManager.beginTransaction()
                    .replace(R.id.container, ViewTaskMultiFragment.newInstance(this.task.identifier), this.task.identifier)
                    .commit()

            supportFragmentManager.executePendingTransactions()

            val fragment: ViewTaskMultiFragment = supportFragmentManager.findFragmentByTag(this.task.identifier) as ViewTaskMultiFragment
            fragment.taskProvider = this
            val stepLayoutProvider = BackwardsCompatibleStepLayoutProvider()
            fragment.stepFragmentProvider = BackwardsCompatibleStepFragmentProvider(stepLayoutProvider)
            fragment.setTaskPresenterDelegate(this)
            this.viewTaskFragment = fragment
        }

    }

    override fun onDataReady() {
        super.onDataReady()

        val fragment: ViewTaskMultiFragment = supportFragmentManager.findFragmentByTag(this.task.identifier) as ViewTaskMultiFragment
        fragment.startPresenting()
    }

    override fun onDataFailed() {
        super.onDataFailed()
        Toast.makeText(this, R.string.rsb_error_data_failed, Toast.LENGTH_LONG).show()
        finish()
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == android.R.id.home) {
            this.viewTaskFragment?.onBackPressed()
            return true
        }

        return super.onOptionsItemSelected(item)
    }

    override fun task(identifier: String): ITask? {

        if (this.task.identifier == identifier) {
            return this.task
        }
        else {
            return null
        }

    }

    override fun didStartPresenting(task: ITask) {

    }

    override fun didFinishPresenting(task: ITask, result: IResult?) {

        val taskResult: TaskResult? = result?.let { it as? TaskResult }

        if (taskResult != null) {
            val resultIntent = Intent()
            resultIntent.putExtra(ViewTaskMultiFragmentActivity.EXTRA_TASK_RESULT, taskResult)
            setResult(Activity.RESULT_OK, resultIntent)
        }

        finish()
    }
}