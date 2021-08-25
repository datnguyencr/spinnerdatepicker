package com.tsongkha.spinnerdatepicker

import android.content.Context
import android.graphics.Color
import com.tsongkha.spinnerdatepicker.CustomDatePickerDialog.OnDateCancelListener
import java.util.*

class SpinnerDatePickerDialogBuilder {
    private var context: Context? = null
    private var callBack: CustomDatePickerDialog.OnDateSetListener? = null
    private var onCancel: OnDateCancelListener? = null
    private var isDayShown = true
    private var isTitleShown = true
    private var customTitle = ""
    private var mainColor = Color.parseColor("#FF8F2B")
    private var blackColor = Color.parseColor("#C1C1C1")
    private var defaultDate: Calendar = GregorianCalendar(1980, 0, 1)
    private var minDate: Calendar = GregorianCalendar(1900, 0, 1)
    private var maxDate: Calendar = GregorianCalendar(2100, 0, 1)

    fun context(context: Context?): SpinnerDatePickerDialogBuilder {
        this.context = context
        return this
    }

    fun callback(callBack: CustomDatePickerDialog.OnDateSetListener?): SpinnerDatePickerDialogBuilder {
        this.callBack = callBack
        return this
    }

    fun onCancel(onCancel: OnDateCancelListener?): SpinnerDatePickerDialogBuilder {
        this.onCancel = onCancel
        return this
    }

    fun mainColor(mainColor: Int): SpinnerDatePickerDialogBuilder {
        this.mainColor = mainColor
        return this
    }

    fun blackColor(blackColor: Int): SpinnerDatePickerDialogBuilder {
        this.blackColor = blackColor
        return this
    }

    fun defaultDate(
        year: Int,
        monthIndexedFromZero: Int,
        day: Int
    ): SpinnerDatePickerDialogBuilder {
        defaultDate = GregorianCalendar(year, monthIndexedFromZero, day)
        return this
    }

    fun minDate(year: Int, monthIndexedFromZero: Int, day: Int): SpinnerDatePickerDialogBuilder {
        minDate = GregorianCalendar(year, monthIndexedFromZero, day)
        return this
    }

    fun maxDate(year: Int, monthIndexedFromZero: Int, day: Int): SpinnerDatePickerDialogBuilder {
        maxDate = GregorianCalendar(year, monthIndexedFromZero, day)
        return this
    }

    fun showDaySpinner(showDaySpinner: Boolean): SpinnerDatePickerDialogBuilder {
        isDayShown = showDaySpinner
        return this
    }

    fun showTitle(showTitle: Boolean): SpinnerDatePickerDialogBuilder {
        isTitleShown = showTitle
        return this
    }

    fun customTitle(title: String): SpinnerDatePickerDialogBuilder {
        customTitle = title
        return this
    }

    fun build(): CustomDatePickerDialog {
        requireNotNull(context) { "Context must not be null" }
        require(maxDate.time.time > minDate.time.time) { "Max date is not after Min date" }
        return CustomDatePickerDialog(
            mainColor,
            blackColor,
            callBack,
            onCancel,
            defaultDate,
            minDate,
            maxDate,
            isDayShown,
            isTitleShown,
            customTitle
        )
    }
}