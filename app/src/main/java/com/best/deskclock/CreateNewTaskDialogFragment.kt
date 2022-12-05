package com.best.deskclock

import android.app.DialogFragment
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import java.util.UUID

class CreateNewTaskDialogFragment: DialogFragment() {
    override fun onCreateView(
        inflater: LayoutInflater?,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        super.onCreateView(inflater, container, savedInstanceState)
        val view = inflater?.inflate(R.layout.tasks_create_new_dialog, container)
        val ok = view?.findViewById<Button>(R.id.task_create_button)
        val et = view?.findViewById<EditText>(R.id.task_edit_text)
        ok?.setOnClickListener {
            if(parentFragment is TasksDialogFragment) (parentFragment as? TasksDialogFragment)?.createNewTask(UUID.randomUUID(), et?.text)
            dismiss()
        }
        return view
    }
}