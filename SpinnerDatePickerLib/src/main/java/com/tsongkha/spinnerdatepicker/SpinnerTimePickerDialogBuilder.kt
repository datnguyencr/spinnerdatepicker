package com.tsongkha.spinnerdatepicker

import android.content.Context
import android.graphics.Color

class SpinnerTimePickerDialogBuilder {
    private var context: Context? = null
    private var callBack: CustomTimePickerDialog.OnTimeSetListener? = null
    private var onCancel: CustomTimePickerDialog.OnTimeCancelListener? = null
    private var isDayShown = true
    private var isTitleShown = true
    private var customTitle = ""
    private var mainColor = Color.parseColor("#FF8F2B")
    private var blackColor = Color.parseColor("#C1C1C1")
    private var defaultHour = 0
    private var defaultMinute = 0
    fun context(context: Context?): SpinnerTimePickerDialogBuilder {
        this.context = context
        return this
    }

    fun callback(callBack: CustomTimePickerDialog.OnTimeSetListener?): SpinnerTimePickerDialogBuilder {
        this.callBack = callBack
        return this
    }

    fun onCancel(onCancel: CustomTimePickerDialog.OnTimeCancelListener?): SpinnerTimePickerDialogBuilder {
        this.onCancel = onCancel
        return this
    }

    fun mainColor(mainColor: Int): SpinnerTimePickerDialogBuilder {
        this.mainColor = mainColor
        return this
    }

    fun blackColor(blackColor: Int): SpinnerTimePickerDialogBuilder {
        this.blackColor = blackColor
        return this
    }

    fun defaultTime(
        hour: Int,
        minute: Int,
    ): SpinnerTimePickerDialogBuilder {
        defaultHour = hour
        defaultMinute = minute
        return this
    }

    fun showDaySpinner(showDaySpinner: Boolean): SpinnerTimePickerDialogBuilder {
        isDayShown = showDaySpinner
        return this
    }

    fun showTitle(showTitle: Boolean): SpinnerTimePickerDialogBuilder {
        isTitleShown = showTitle
        return this
    }

    fun customTitle(title: String): SpinnerTimePickerDialogBuilder {
        customTitle = title
        return this
    }

    fun build(): CustomTimePickerDialog {
        requireNotNull(context) { "Context must not be null" }
        return CustomTimePickerDialog(
            mainColor = mainColor,
            blackColor = blackColor,
            mCallBack = callBack,
            mOnCancel = onCancel,
            defaultHour = defaultHour,
            defaultMinute = defaultMinute,
            isDayShown = isDayShown,
            isTitleShown = isTitleShown,
            customTitle = customTitle
        )
    }
}