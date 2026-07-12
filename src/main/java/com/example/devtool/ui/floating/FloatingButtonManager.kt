package com.example.devtool.ui.floating

import android.annotation.SuppressLint
import android.app.Activity
import android.app.Application
import android.os.Bundle
import android.view.Gravity
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import androidx.appcompat.view.ContextThemeWrapper
import com.example.devtool.ui.dashboard.DashboardActivity
import com.google.android.material.floatingactionbutton.FloatingActionButton

object FloatingButtonManager {
    private var isInitialized = false

    fun init(application: Application) {
        if (isInitialized) return
        isInitialized = true

        application.registerActivityLifecycleCallbacks(object : Application.ActivityLifecycleCallbacks {
            override fun onActivityCreated(activity: Activity, savedInstanceState: Bundle?) {}
            override fun onActivityStarted(activity: Activity) {
                if (activity !is DashboardActivity) {
                    showFloatingButton(activity)
                }
            }
            override fun onActivityResumed(activity: Activity) {}
            override fun onActivityPaused(activity: Activity) {}
            override fun onActivityStopped(activity: Activity) {
                removeFloatingButton(activity)
            }
            override fun onActivitySaveInstanceState(activity: Activity, outState: Bundle) {}
            override fun onActivityDestroyed(activity: Activity) {}
        })
    }

    @SuppressLint("ClickableViewAccessibility")
    private fun showFloatingButton(activity: Activity) {
        val rootView = activity.window.decorView.findViewById<ViewGroup>(android.R.id.content)
        
        // Use a ContextThemeWrapper to ensure FloatingActionButton has a MaterialComponents theme
        val contextWrapper = ContextThemeWrapper(activity, com.google.android.material.R.style.Theme_MaterialComponents_DayNight)
        
        val fab = FloatingActionButton(contextWrapper).apply {
            setImageResource(android.R.drawable.ic_dialog_info)
            setOnClickListener {
                activity.startActivity(DashboardActivity.newIntent(activity))
            }
        }

        val params = FrameLayout.LayoutParams(
            FrameLayout.LayoutParams.WRAP_CONTENT,
            FrameLayout.LayoutParams.WRAP_CONTENT
        ).apply {
            gravity = Gravity.BOTTOM or Gravity.END
            setMargins(0, 0, 32, 200)
        }

        var dX = 0f
        var dY = 0f

        fab.setOnTouchListener { view, event ->
            when (event.action) {
                MotionEvent.ACTION_DOWN -> {
                    dX = view.x - event.rawX
                    dY = view.y - event.rawY
                }
                MotionEvent.ACTION_MOVE -> {
                    view.animate()
                        .x(event.rawX + dX)
                        .y(event.rawY + dY)
                        .setDuration(0)
                        .start()
                }
                MotionEvent.ACTION_UP -> {
                    // Simple snap to edge could be added here
                }
            }
            false
        }

        fab.tag = "DEV_TOOL_FAB"
        rootView.addView(fab, params)
    }

    private fun removeFloatingButton(activity: Activity) {
        val rootView = activity.window.decorView.findViewById<ViewGroup>(android.R.id.content)
        val fab = rootView.findViewWithTag<View>("DEV_TOOL_FAB")
        if (fab != null) {
            rootView.removeView(fab)
        }
    }
}
