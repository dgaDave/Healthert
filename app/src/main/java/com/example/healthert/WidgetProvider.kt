package com.example.healthert

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.widget.RemoteViews
import android.widget.Toast

class WidgetProvider : AppWidgetProvider() {
    override fun onUpdate(context: Context, appWidgetManager: AppWidgetManager, appWidgetIds: IntArray) {
        for (appWidgetId in appWidgetIds) {
            val serviceIntent = Intent(context, WidgetService::class.java)
            serviceIntent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId)
            serviceIntent.data = Uri.parse(serviceIntent.toUri(Intent.URI_INTENT_SCHEME))

            val clickIntent = Intent(context, MainActivity2::class.java)
            clickIntent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId)
            val clickPendingIntent = PendingIntent.getActivity(context, appWidgetId, clickIntent, PendingIntent.FLAG_UPDATE_CURRENT)

            iniciaViews(context,serviceIntent,appWidgetId,clickPendingIntent,appWidgetManager)

        }
    }

    override fun onAppWidgetOptionsChanged(
        context: Context,
        appWidgetManager: AppWidgetManager,
        appWidgetId: Int,
        newOptions: Bundle) {
        val views = RemoteViews(context.packageName, R.layout.example_widget)
        appWidgetManager.updateAppWidget(appWidgetId, views)
    }

    override fun onDeleted(context: Context, appWidgetIds: IntArray) {
        Toast.makeText(context, "Se eliminó el widget", Toast.LENGTH_SHORT).show()
    }

    override fun onEnabled(context: Context) {
        Toast.makeText(context, "Estableciste un widget", Toast.LENGTH_SHORT).show()
    }

    override fun onDisabled(context: Context) {
        Toast.makeText(context, "onDisabled", Toast.LENGTH_SHORT).show()
    }

    override fun onReceive(context: Context, intent: Intent) {
        if (ACTION_TOAST == intent.action) {
            val clickedPosition = intent.getIntExtra(EXTRA_ITEM_POSITION, 0)
            Toast.makeText(context, "Clicked position: $clickedPosition", Toast.LENGTH_SHORT).show()
        }
        super.onReceive(context, intent)
    }

    companion object {
        const val ACTION_TOAST = "actionToast"
        const val EXTRA_ITEM_POSITION = "extraItemPosition"
    }
    //Función para inicializar el stackview del widget
    private fun iniciaViews(context:Context, serviceIntent:Intent,appWidgetId:Int,clickPendingIntent:PendingIntent,appWidgetManager:AppWidgetManager ){
        val views = RemoteViews(context.packageName, R.layout.example_widget)
        views.setRemoteAdapter(R.id.example_widget_stack_view, serviceIntent)
        views.setEmptyView(R.id.example_widget_stack_view, R.id.example_widget_empty_view)
        views.setPendingIntentTemplate(R.id.example_widget_stack_view, clickPendingIntent)
        appWidgetManager.updateAppWidget(appWidgetId, views)
    }
}
