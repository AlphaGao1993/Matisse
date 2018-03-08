package com.zhihu.matisse.internal.utils

import android.annotation.SuppressLint
import android.annotation.TargetApi
import android.app.Activity
import android.content.Context
import android.content.res.Resources
import android.os.Build
import android.support.annotation.FloatRange
import android.support.annotation.RequiresApi
import android.util.Log
import android.util.TypedValue
import android.view.View
import android.view.ViewGroup
import android.view.Window
import android.view.WindowManager

/**
 * Created by blazer on 2018/1/10.
 * 状态栏相关工具
 */
class StatusBarUtil {

    companion object {
        private var DEFAULT_COLOR = 0
        private var DEFAULT_ALPHA = 0f//Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP ? 0.2f : 0.3f;
        private const val MIN_API = 19
        private const val SYSTEM_UI_FLAG_OP_STATUS_BAR_TINT = 0x00000010

        @JvmOverloads
        fun immersive(activity: Activity, color: Int = DEFAULT_COLOR, @FloatRange(from = 0.0, to = 1.0) alpha: Float = DEFAULT_ALPHA) {
            immersive(activity.window, color, alpha)
        }

        fun immersive(window: Window) {
            immersive(window, DEFAULT_COLOR, DEFAULT_ALPHA)
        }

        private fun immersive(window: Window, color: Int, @FloatRange(from = 0.0, to = 1.0) alpha: Float = 1f) {
            if (Build.VERSION.SDK_INT >= 19) {
                window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS)
                if (Build.VERSION.SDK_INT >= 21) {
                    window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS)
                    window.statusBarColor = mixtureColor(color, alpha)
                }
            }

            var systemUiVisibility = window.decorView.systemUiVisibility
            systemUiVisibility = systemUiVisibility or View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
            systemUiVisibility = systemUiVisibility or View.SYSTEM_UI_FLAG_LAYOUT_STABLE
            window.decorView.systemUiVisibility = systemUiVisibility
        }

        @TargetApi(Build.VERSION_CODES.M)
        fun darkMode(activity: Activity, dark: Boolean) {
            when {
                RomUtil.isFlyme4Later() -> darkModeForFlyme4(activity.window, dark)
                RomUtil.isMIUI6Later() -> darkModeForMIUI6(activity.window, dark)
                RomUtil.isColorOs() -> darkModeForColorOs(activity.window, true)
                Build.VERSION.SDK_INT >= Build.VERSION_CODES.M -> darkModeForM(activity.window, dark)
            }
        }

        /** 设置状态栏darkMode,字体颜色及icon变黑(目前支持MIUI6以上,Flyme4以上,Android M以上)  */
        fun darkMode(activity: Activity) {
            darkMode(activity.window, DEFAULT_COLOR, DEFAULT_ALPHA)
        }

        fun darkMode(activity: Activity, color: Int, @FloatRange(from = 0.0, to = 1.0) alpha: Float) {
            darkMode(activity.window, color, alpha)
        }

        /** 设置状态栏darkMode,字体颜色及icon变黑(目前支持MIUI6以上,Flyme4以上,Android M以上)  */
        @TargetApi(Build.VERSION_CODES.M)
        private fun darkMode(window: Window, color: Int, @FloatRange(from = 0.0, to = 1.0) alpha: Float) {
            when {
                RomUtil.isFlyme4Later() -> {
                    darkModeForFlyme4(window, true)
                    immersive(window, color, alpha)
                }
                RomUtil.isMIUI6Later() -> {
                    darkModeForMIUI6(window, true)
                    darkModeForM(window, true)
                    immersive(window, color, alpha)
                }
                RomUtil.isColorOs() -> {
                    darkModeForColorOs(window, true)
                    immersive(window, color, alpha)
                }
                Build.VERSION.SDK_INT >= Build.VERSION_CODES.M -> {
                    darkModeForM(window, true)
                    immersive(window, color, alpha)
                }
                Build.VERSION.SDK_INT >= 21 -> {
                    window.addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS)
                    setTranslucentView(window.decorView as ViewGroup, color, alpha)
                }
                else -> immersive(window, color, alpha)
            }
        }


        /** android 6.0设置字体颜色  */
        @RequiresApi(Build.VERSION_CODES.M)
        private fun darkModeForM(window: Window, dark: Boolean) {
            var systemUiVisibility = window.decorView.systemUiVisibility
            systemUiVisibility = if (dark) {
                systemUiVisibility or View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR
            } else {
                systemUiVisibility and View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR.inv()
            }
            window.decorView.systemUiVisibility = systemUiVisibility
        }

        /**
         * 设置Flyme4+的darkMode,darkMode时候字体颜色及icon变黑
         * http://open-wiki.flyme.cn/index.php?title=Flyme%E7%B3%BB%E7%BB%9FAPI
         */
        private fun darkModeForFlyme4(window: Window?, dark: Boolean): Boolean {
            var result = false
            if (window != null) {
                try {
                    val e = window.attributes
                    val darkFlag = WindowManager.LayoutParams::class.java.getDeclaredField("MEIZU_FLAG_DARK_STATUS_BAR_ICON")
                    val meizuFlags = WindowManager.LayoutParams::class.java.getDeclaredField("meizuFlags")
                    darkFlag.isAccessible = true
                    meizuFlags.isAccessible = true
                    val bit = darkFlag.getInt(null)
                    var value = meizuFlags.getInt(e)
                    value = if (dark) {
                        value or bit
                    } else {
                        value and bit.inv()
                    }

                    meizuFlags.setInt(e, value)
                    window.attributes = e
                    result = true
                } catch (var8: Exception) {
                    Log.e("StatusBar", "darkIcon: failed")
                }
            }
            return result
        }

        /**
         * 设置MIUI6+的状态栏是否为darkMode,darkMode时候字体颜色及icon变黑
         * http://dev.xiaomi.com/doc/p=4769/
         */
        @SuppressLint("PrivateApi")
        private fun darkModeForMIUI6(window: Window, darkMode: Boolean): Boolean {
            val clazz = window.javaClass
            return try {
                val darkModeFlag: Int
                val layoutParams = Class.forName("android.view.MiuiWindowManager\$LayoutParams")
                val field = layoutParams.getField("EXTRA_FLAG_STATUS_BAR_DARK_MODE")
                darkModeFlag = field.getInt(layoutParams)
                val extraFlagField = clazz.getMethod("setExtraFlags", Int::class.javaPrimitiveType, Int::class.javaPrimitiveType)
                extraFlagField.invoke(window, if (darkMode) darkModeFlag else 0, darkModeFlag)
                true
            } catch (e: Exception) {
                e.printStackTrace()
                false
            }

        }

        private fun darkModeForColorOs(window: Window, darkMode: Boolean) {
            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS)
            var vis = window.decorView.systemUiVisibility
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                vis = if (darkMode) {
                    vis or View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR
                } else {
                    vis and View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR.inv()
                }
            } else if (Build.VERSION.SDK_INT >= 21) {
                vis = if (darkMode) {
                    vis or SYSTEM_UI_FLAG_OP_STATUS_BAR_TINT
                } else {
                    vis and SYSTEM_UI_FLAG_OP_STATUS_BAR_TINT.inv()
                }
            }
            window.decorView.systemUiVisibility = vis
        }


        /** 增加View的paddingTop,增加的值为状态栏高度  */
        fun setPadding(context: Context, view: View) {
            if (Build.VERSION.SDK_INT >= MIN_API) {
                view.setPadding(view.paddingLeft, view.paddingTop + getStatusBarHeight(context),
                        view.paddingRight, view.paddingBottom)
            }
        }

        /** 增加View的paddingTop,增加的值为状态栏高度 (智能判断，并设置高度) */
        fun setPaddingSmart(context: Context, view: View) {
            if (Build.VERSION.SDK_INT >= MIN_API) {
                val lp = view.layoutParams
                if (lp != null && lp.height > 0) {
                    lp.height += getStatusBarHeight(context)//增高
                }
                view.setPadding(view.paddingLeft, view.paddingTop + getStatusBarHeight(context),
                        view.paddingRight, view.paddingBottom)
            }
        }

        /** 增加View的高度以及paddingTop,增加的值为状态栏高度.一般是在沉浸式全屏给ToolBar用的  */
        fun setHeightAndPadding(context: Context, view: View) {
            if (Build.VERSION.SDK_INT >= MIN_API) {
                val lp = view.layoutParams
                lp.height += getStatusBarHeight(context)//增高
                view.setPadding(view.paddingLeft, view.paddingTop + getStatusBarHeight(context),
                        view.paddingRight, view.paddingBottom)
            }
        }

        /** 增加View上边距（MarginTop）一般是给高度为 WARP_CONTENT 的小控件用的 */
        fun setMargin(context: Context, view: View) {
            if (Build.VERSION.SDK_INT >= MIN_API) {
                val lp = view.layoutParams
                if (lp is ViewGroup.MarginLayoutParams) {
                    lp.topMargin += getStatusBarHeight(context)//增高
                }
                view.layoutParams = lp
            }
        }

        /**
         * 创建假的透明栏
         */
        private fun setTranslucentView(container: ViewGroup, color: Int, @FloatRange(from = 0.0, to = 1.0) alpha: Float) {
            val mixtureColor = mixtureColor(color, alpha)
            var translucentView: View? = container.findViewById(android.R.id.custom)
            if (translucentView == null && mixtureColor != 0) {
                translucentView = View(container.context)
                translucentView.id = android.R.id.custom
                val lp = ViewGroup.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT, getStatusBarHeight(container.context))
                container.addView(translucentView, lp)
            }
            if (translucentView != null) {
                translucentView.setBackgroundColor(mixtureColor)
            }
        }

        private fun mixtureColor(color: Int, @FloatRange(from = 0.0, to = 1.0) alpha: Float): Int {
            val a = if (color and -0x1000000 == 0) 0xff else color.ushr(24)
            return color and 0x00ffffff or ((a * alpha).toInt() shl 24)
        }

        /** 获取状态栏高度  */
        private fun getStatusBarHeight(context: Context): Int {
            var result = 24
            val resId = context.resources.getIdentifier("status_bar_height", "dimen", "android")
            result = if (resId > 0) {
                context.resources.getDimensionPixelSize(resId)
            } else {
                TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP,
                        result.toFloat(), Resources.getSystem().displayMetrics).toInt()
            }
            return result
        }
    }
}