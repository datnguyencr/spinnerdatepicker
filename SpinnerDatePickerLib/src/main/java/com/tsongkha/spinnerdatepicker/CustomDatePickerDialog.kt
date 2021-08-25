package com.tsongkha.spinnerdatepicker

import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.widget.AppCompatTextView
import androidx.fragment.app.DialogFragment
import java.text.DateFormat
import java.util.*

class CustomDatePickerDialog constructor(
    var mainColor: Int,
    var blackColor: Int,
    private val mCallBack: OnDateSetListener?,
    private val mOnCancel: OnDateCancelListener?,
    var defaultDate: Calendar,
    var minDate: Calendar,
    var maxDate: Calendar,
    var isDayShown: Boolean,
    var isTitleShown: Boolean,
    var customTitle: String?
) : DialogFragment(), OnDateChangedListener {
    private var mDatePicker: DatePicker? = null
    private var mTitleDateFormat: DateFormat? = null
    private var mIsDayShown = true
    private var mIsTitleShown = true
    private var mCustomTitle: String? = ""
    private var vTitle: AppCompatTextView? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.date_picker_dialog_container, null, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        dialog?.setCanceledOnTouchOutside(true)
        dialog?.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        mTitleDateFormat = DateFormat.getDateInstance(DateFormat.LONG, Locale.JAPAN)
        mIsDayShown = isDayShown
        mIsTitleShown = isTitleShown
        mCustomTitle = customTitle
        updateTitle(defaultDate)

        val container = view.findViewById<ViewGroup>(R.id.datePickerContainer)

        vTitle = view.findViewById(R.id.vTitle)
        vTitle?.setTextColor(blackColor)

        val btnCancel = view.findViewById(R.id.vCancel) as AppCompatTextView
        btnCancel.setTextColor(blackColor)
        btnCancel.setOnClickListener {
            if (mOnCancel != null) {
                mDatePicker?.clearFocus()
                mOnCancel.onCancelled(mDatePicker)
            }
            dismiss()
        }

        val btnOk = view.findViewById(R.id.vOk) as AppCompatTextView
        btnOk.setTextColor(mainColor)
        btnOk.setOnClickListener {
            if (mCallBack != null) {
                mDatePicker?.clearFocus()
                mCallBack.onDateSet(
                    mDatePicker, mDatePicker?.year ?: 0,
                    mDatePicker?.month ?: 0, mDatePicker?.dayOfMonth ?: 0
                )
            }
            dismiss()
        }
        mDatePicker = DatePicker(container, mainColor, blackColor)
        mDatePicker?.setMinDate(minDate.timeInMillis)
        mDatePicker?.setMaxDate(maxDate.timeInMillis)
        mDatePicker?.init(
            defaultDate[Calendar.YEAR],
            defaultDate[Calendar.MONTH],
            defaultDate[Calendar.DAY_OF_MONTH],
            isDayShown,
            this
        )

    }

    interface OnDateSetListener {
        fun onDateSet(view: DatePicker?, year: Int, monthOfYear: Int, dayOfMonth: Int)
    }

    interface OnDateCancelListener {
        fun onCancelled(view: DatePicker?)
    }

    private fun setTitle(title: String?) {
        if (vTitle != null) {
            vTitle?.text = title
        }
    }

    override fun onDateChanged(view: DatePicker?, year: Int, monthOfYear: Int, dayOfMonth: Int) {
        val updatedDate = DateUtils.getCalendar()
        updatedDate[Calendar.YEAR] = year
        updatedDate[Calendar.MONTH] = monthOfYear
        updatedDate[Calendar.DAY_OF_MONTH] = dayOfMonth
        updateTitle(updatedDate)
    }

    private fun updateTitle(updatedDate: Calendar) {
        if (mIsTitleShown && mCustomTitle != null && !mCustomTitle!!.isEmpty()) {
            setTitle(mCustomTitle)
        } else if (mIsTitleShown) {
            val dateFormat = mTitleDateFormat
            setTitle(dateFormat?.format(updatedDate.time))
        } else {
            setTitle(" ")
        }
    }
}