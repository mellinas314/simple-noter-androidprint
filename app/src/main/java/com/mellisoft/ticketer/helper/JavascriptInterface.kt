package com.mellisoft.ticketer.helper

import android.bluetooth.BluetoothAdapter
import android.content.Context
import android.content.DialogInterface
import android.graphics.fonts.FontStyle
import android.os.Build
import android.text.Html
import android.util.Log
import android.util.Printer
import android.webkit.JavascriptInterface
import android.widget.ArrayAdapter
import androidx.core.graphics.drawable.toBitmap
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.mellisoft.ticketer.MainActivity
import com.mellisoft.ticketer.R
import hk.ucom.printer.connection.ResultReceiver
import org.json.JSONObject
import java.util.*
import kotlin.collections.ArrayList
import hk.ucom.printer.UcomPrinterManager
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import java.text.DecimalFormat
import java.text.SimpleDateFormat


private const val TAG = "JSInterface"

class JavascriptInterface(private val context: MainActivity): ResultReceiver {
    @JavascriptInterface
    fun printTask(task: String) {
        val taskObj = DataTask(JSONObject(task))
        val text = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            Html.fromHtml(String.format(context.resources.getString(R.string.print_task_body), taskObj.title), Html.FROM_HTML_MODE_LEGACY)
        } else {
            Html.fromHtml(String.format(context.resources.getString(R.string.print_task_body), taskObj.title))
        }
        Log.d(TAG, "Print task: " + taskObj)
        val builder = MaterialAlertDialogBuilder(context)
            .setTitle(context.resources.getString(R.string.print_task_title))
            .setMessage(text)
            .setCancelable(false)
            .setNeutralButton(context.resources.getString(R.string.cancel)
            ) { dialog, _ -> dialog?.dismiss() }
            .setPositiveButton(context.resources.getString(R.string.confirm_cierre)
            ) { dialog, _ ->
                dialog?.dismiss()
                internalPrintTask(taskObj)
            }
        if(taskObj.type == "REPARACION") {
            builder.setNegativeButton(context.resources.getString(R.string.confirm_recogida)
            ) { dialog, _ ->
                dialog?.dismiss()
                internalPrintRecogidaTask(taskObj)
            }
        }
        builder.show()
    }

    private var pendingTask : DataTask? = null
    private var pendingRecogidaTask : DataTask? = null
    private fun internalPrintTask(task: DataTask) {
        //Si hay ya conexión, no la creamos
        if(!context.getPrinterManager().isConnected) {
            Log.d(TAG, "NOT connected")
            pendingTask = task
            connectBT()
        }else {
            launchPrint(task)
        }
    }

    private fun internalPrintRecogidaTask(task: DataTask) {
        //Si hay ya conexión, no la creamos
        if(!context.getPrinterManager().isConnected) {
            Log.d(TAG, "NOT connected")
            pendingRecogidaTask = task
            connectBT()
        }else {
            launchPrintRecogida(task)
        }
    }

    private fun connectBT() {
        Log.d(TAG, "ConnectBT. Trying to do bad things")
        val adapter = BluetoothAdapter.getDefaultAdapter()
        val pairedDevices = adapter.bondedDevices
        val deviceList = ArrayList<String>()
        context.getPrinterManager().registerResultReceiver(this)
        if(pairedDevices.size > 0) {
            pairedDevices.forEach {
                val deviceDetail = it.name + "\n" + it.address
                deviceList.add(deviceDetail)
            }
            val btAdapter = ArrayAdapter(context, android.R.layout.simple_list_item_1, deviceList)
            MaterialAlertDialogBuilder(context)
                .setCancelable(false)
                .setTitle(R.string.select_bt_title)
                .setAdapter(btAdapter) { dialog, which ->
                    val device = deviceList[which]
                    val deviceAddress = device.substring(device.length - 17)
                    Log.d(TAG, "Printer selected: $deviceAddress")
                    connectNative(deviceAddress)
                    dialog.dismiss()
                }
                .setNegativeButton(R.string.cancel, null).show()
        }else {
            printMessage("Not paired available devices", "Error")
        }
    }

    private fun connectNative(address: String) {
        context.showLoading()
        context.getPrinterManager().setManualSocketClose(true)
        context.getPrinterManager().setBluetoothConnection(address)
        context.getPrinterManager().executeCommand(false)
    }

    private fun printSeparator() {
        val separator = "--------------------------------"
        val printer = context.getPrinterManager()
        printer.printText(separator, UcomPrinterManager.FontStyle.NORMAL)
        printer.writeln()
    }

    private fun printHeader() {
        val printer = context.getPrinterManager()
        printer.printText("PCSoft Reparaciones", UcomPrinterManager.FontStyle.DOUBLE_HEIGHT or UcomPrinterManager.FontStyle.BOLD)
        printer.writeln()
        printer.printText("Informatica a domicilio", UcomPrinterManager.FontStyle.BOLD)
        printer.writeln(2)
        printer.printText("Web: www.pcsoftreparaciones.com", UcomPrinterManager.FontStyle.NORMAL)
        printer.writeln()
        printer.printText("Mail: sat@pcsoftreparaciones.com", UcomPrinterManager.FontStyle.NORMAL)
        printer.writeln()
        printer.printText("Tel: 658528409", UcomPrinterManager.FontStyle.NORMAL)
        printer.writeln()
    }

    private fun launchPrint(task: DataTask) {
        //Imprimimos el nombre
        val separator = "--------------------------------"
        val printer = context.getPrinterManager()
        val cal = Calendar.getInstance()
        val sdf = SimpleDateFormat(
            "yyyy-MM-dd HH:mm:ss",
            Locale.getDefault()
        )
        val currentTime = sdf.format(cal.time)
        val ticketTime = sdf.format(task.date)
        printHeader()
        printSeparator()

        //Si es reparación, imprimos el bloque de motivo
        if(task.type == "REPARACION") {
            //Bloque cliente
            printer.printText("N. orden: ${task.id}", UcomPrinterManager.FontStyle.NORMAL)
            printer.writeln()
            printer.printText("F. recogida: ", UcomPrinterManager.FontStyle.NORMAL)
            printer.printText(ticketTime, UcomPrinterManager.FontStyle.NORMAL)
            printer.writeln()
            printer.printText("Cliente: ${task.title}", UcomPrinterManager.FontStyle.NORMAL)
            printer.writeln()
            printSeparator()
            //Bloque datos recogida
            printer.printText("PRODUCTOS A REVISAR Y MOTIVO:", UcomPrinterManager.FontStyle.BOLD)
            printer.writeln(2)
            printer.printText(task.description, UcomPrinterManager.FontStyle.NORMAL)
            printer.writeln()
            printSeparator()
            //Bloque entrega y reparación
            printer.printText("F. entrega: ", UcomPrinterManager.FontStyle.BOLD or UcomPrinterManager.FontStyle.NORMAL)
            printer.printText(currentTime, UcomPrinterManager.FontStyle.NORMAL)
            printer.writeln()
            printer.printText("REPARACIONES REALIZADAS:", UcomPrinterManager.FontStyle.BOLD)
            printer.writeln(2)
            printer.printText(task.operation, UcomPrinterManager.FontStyle.NORMAL)
            printer.writeln()
            printSeparator()
        }else {
            //Bloque cliente
            printer.printText("N. venta: ${task.id}", UcomPrinterManager.FontStyle.NORMAL)
            printer.writeln()
            printer.printText("F. venta: $ticketTime", UcomPrinterManager.FontStyle.NORMAL)
            printer.writeln()
            printer.printText("Cliente: ${task.title}", UcomPrinterManager.FontStyle.NORMAL)
            printer.writeln()
            printSeparator()
            //Bloque datos venta
            printer.printText(task.description, UcomPrinterManager.FontStyle.NORMAL)
            printer.writeln()
            printSeparator()
        }
        printer.printText("Total: ${task.total} Euros", UcomPrinterManager.Align.RIGHT,
            UcomPrinterManager.FontStyle.DOUBLE_HEIGHT or UcomPrinterManager.FontStyle.BOLD )
        printer.writeln(5)
        printer.paperCut()
        printer.executeCommand(true)
    }

    private fun launchPrintRecogida(task: DataTask) {
        //Imprimimos el nombre
        val separator = "--------------------------------"
        val printer = context.getPrinterManager()
        val cal = Calendar.getInstance()
        val sdf = SimpleDateFormat(
            "yyyy-MM-dd HH:mm:ss",
            Locale.getDefault()
        )
        val time = sdf.format(cal.time)
        val ticketTime = sdf.format(task.date)

        //Bloque cabecera
        printHeader()
        printSeparator()

        //Bloque cliente
        printer.printText("N. orden: ${task.id}", UcomPrinterManager.FontStyle.NORMAL)
        printer.writeln()
        printer.printText("F. recogida: ", UcomPrinterManager.FontStyle.NORMAL)
        printer.printText(ticketTime, UcomPrinterManager.FontStyle.NORMAL)
        printer.writeln()
        printer.printText("Cliente: ${task.title}", UcomPrinterManager.FontStyle.NORMAL)
        printer.writeln()
        printSeparator()

        //Bloque datos recogida
        printer.printText("PRODUCTOS A REVISAR Y MOTIVO:", UcomPrinterManager.FontStyle.BOLD)
        printer.writeln(2)
        printer.printText(task.description, UcomPrinterManager.FontStyle.NORMAL)
        printer.writeln(6)
        printer.paperCut()
        printer.executeCommand(true)
    }

    private fun printMessage(message: String, title: String = "") {
        MaterialAlertDialogBuilder(context)
            .setTitle(title)
            .setMessage(message)
            .setCancelable(false)
            .setPositiveButton(context.resources.getString(R.string.close)
            ) { dialog, _ ->
                dialog?.dismiss()
            }
            .show()
    }
    //region ConnectionCallback
    override fun onConnectionFinished(status: Int) {
        Log.d(TAG, "onConnectionFinished: $status. Printer status: ${context.getPrinterManager().printerStatus}")
        Log.d(TAG, "Readable status: ${ResultReceiver.Result.getMessage(status)} for ${context.getPrinterManager().printerModel}")
        Log.d(TAG, "Is connected: ${context.getPrinterManager().isConnected}.")
        if(status != ResultReceiver.Result.SUCCESS) {
            printMessage("$status - ${ResultReceiver.Result.getMessage(status)}\n ${context.getPrinterManager().printerStatus} - ${ResultReceiver.Result.getMessage(
                context.getPrinterManager().printerStatus)}")
        }else if(pendingTask != null) {
            GlobalScope.launch {
                launchPrint(pendingTask!!)
                pendingTask = null
            }
        }else if(pendingRecogidaTask != null) {
            GlobalScope.launch {
                launchPrintRecogida(pendingRecogidaTask!!)
                pendingRecogidaTask = null
            }
        }
        context.hideLoading()
    }
    //endregion
}

private class DataTask(json: JSONObject) {

    val title: String = json.getString("title")
    val description: String = json.getString("description")
    val operation: String = json.getString("operation")
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
                "operation: $operation\n" +
                "id: $id\n" +
                "total: $total\n" +
                "date: $date\n" +
                "type: $type\n" +
                ")"
    }
}