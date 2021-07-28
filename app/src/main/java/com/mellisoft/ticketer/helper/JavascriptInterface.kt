package com.mellisoft.ticketer.helper

import android.content.Context
import android.os.Build
import android.text.Html
import android.util.Log
import android.webkit.JavascriptInterface
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.mellisoft.ticketer.R
import org.json.JSONObject
import java.util.*

private const val TAG = "JSInterface"


class JavascriptInterface(private val context: Context) {
    @JavascriptInterface
    fun printTask(task: String) {
        val taskObj = DataTask(JSONObject(task))
        val text = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            Html.fromHtml(String.format(context.resources.getString(R.string.print_task_body), taskObj.title), Html.FROM_HTML_MODE_LEGACY)
        } else {
            Html.fromHtml(String.format(context.resources.getString(R.string.print_task_body), taskObj.title))
        }
        MaterialAlertDialogBuilder(context)
            .setTitle(context.resources.getString(R.string.print_task_title))
            .setMessage(text)
            .setCancelable(false)
            .setNegativeButton(context.resources.getString(R.string.cancel)
            ) { dialog, _ -> dialog?.dismiss() }
            .setPositiveButton(context.resources.getString(R.string.confirm)
            ) { dialog, _ ->
                dialog?.dismiss()
                internalPrintTask(taskObj)
            }
            .show()
    }

    private fun internalPrintTask(task: DataTask) {
        Log.d(TAG, "Task is: $task")
    }
}

private class DataTask(json: JSONObject) {

    val title: String = json.getString("title")
    val description: String = json.getString("description")
    val id: String? = json.optString("id", "")
    val clientName: String = json.getString("clientName")
    val total: Double = json.getDouble("total")
    val date: Date = Date(json.getLong("date"))
    val type: String = json.getString("type")


    override fun toString(): String {
        return "DataTask(\n" +
                "clientName: $clientName\n" +
                "title: $title \n" +
                "description: $description\n" +
                "id: $id\n" +
                "total: $total\n" +
                "date: $date\n" +
                "type: $type\n" +
                ")"
    }
}