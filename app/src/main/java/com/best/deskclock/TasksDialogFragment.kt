package com.best.deskclock

import android.app.DialogFragment
import android.content.Context
import android.os.Bundle
import android.text.Editable
import android.text.SpannableStringBuilder
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.CheckBox
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import java.util.*

class TasksDialogFragment : DialogFragment() {
    companion object {
        const val BUNDLE_ARG_ALARM_UUID = "${BuildConfig.APPLICATION_ID}.task_uuid"
        const val delimiter = "~~~~~~~~~~"
        fun newInstance(alarmUUID: Long): TasksDialogFragment {
            val args = Bundle()
            args.putString(BUNDLE_ARG_ALARM_UUID, alarmUUID.toString())
            val fragment = TasksDialogFragment()
            fragment.arguments = args
            return fragment
        }
    }

    data class Task(
        val uuid: String,
        val text: String,
        var isComplete: Boolean
    )

    val alarmID by lazy { arguments?.getString(BUNDLE_ARG_ALARM_UUID).also { println("Alarm id: $it") } }
    val prefs by lazy { context.getSharedPreferences("tasks", Context.MODE_PRIVATE) }
    val tasks by lazy {
        prefs
            .getString(
                alarmID, null
            )
            ?.also { println("Got string from file: $it") }
            ?.split("\n")
            ?.mapTo(mutableListOf()) {
                it
                    .split(delimiter)
                    .iterator()
                    .let {
                        Task(
                            it.next(),
                            it.next(),
                            it.next().toBooleanStrict()
                        )
                    }
            } ?: mutableListOf()
    }
    val adapter =
        object : ListAdapter<Task, TaskViewHolder>(object : DiffUtil.ItemCallback<Task>() {
            override fun areItemsTheSame(oldItem: Task, newItem: Task): Boolean {
                return oldItem.uuid == newItem.uuid
            }

            override fun areContentsTheSame(oldItem: Task, newItem: Task): Boolean {
                return oldItem.text == newItem.text &&
                        oldItem.isComplete == newItem.isComplete
            }

        }) {
            override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TaskViewHolder {
                return TaskViewHolder(
                    LayoutInflater.from(parent.context)
                        .inflate(R.layout.item_task, parent, false)
                )
            }

            override fun onBindViewHolder(holder: TaskViewHolder, position: Int) {
                val task = getItem(position)
                holder.checkbox.setOnCheckedChangeListener { buttonView, isChecked ->
                    task.isComplete = isChecked
                    setTaskComplete(holder, isChecked)
                    submitList(tasks.toList())
                }
                setTaskComplete(holder, task.isComplete)
                holder.taskTest.text = task.text
            }

            private fun setTaskComplete(
                holder: TaskViewHolder,
                complete: Boolean
            ) {
                if(complete) {
                    holder.checkbox.isChecked = true
                    holder.taskTest.isEnabled = false
                } else {
                    holder.checkbox.isChecked = false
                    holder.taskTest.isEnabled = true
                }
            }
        }

    override fun onCreateView(
        inflater: LayoutInflater?,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        super.onCreateView(inflater, container, savedInstanceState)

        val view = inflater?.inflate(R.layout.tasks_dialog, container)
        val recycler = view?.findViewById<RecyclerView>(R.id.tasks)
        val add = view?.findViewById<Button>(R.id.add_task_button)
        val ok = view?.findViewById<Button>(R.id.ok_button)
        ok?.setOnClickListener {
            saveList()
            dismiss()
        }
        add?.setOnClickListener {
            CreateNewTaskDialogFragment().show(childFragmentManager, "create_new_task_dialog")
        }
        recycler?.layoutManager = LinearLayoutManager(context!!)
        recycler?.adapter = adapter
        adapter.submitList(tasks)
        return view
    }

    override fun onDestroy() {
        saveList()
        super.onDestroy()
    }

    private fun saveList() {
        val data =
            tasks.joinToString("\n") { "${it.uuid}$delimiter${it.text}$delimiter${it.isComplete}" }
        println("Putting data into file $alarmID: $data")
        prefs.edit()
            .putString(
                alarmID,
                data
            )
            .apply()
    }

    fun createNewTask(randomUUID: UUID, text: Editable?) {
        text?.toString()?.let {
            tasks.add(Task(randomUUID.toString(), it, false))
            adapter.submitList(tasks.toList())
        }
    }

    class TaskViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val checkbox = view.findViewById<CheckBox>(R.id.task_checkbox)
        val taskTest = view.findViewById<TextView>(R.id.task_text).apply {
            setOnClickListener { checkbox.performClick() }
        }
    }
}