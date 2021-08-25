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
import java.text.DecimalFormat
import java.text.NumberFormat
import java.util.*

class CustomTimePickerDialog constructor(
    var mainColor: Int,
    var blackColor: Int,
    private val mCallBack: OnTimeSetListener?,
    private val mOnCancel: OnTimeCancelListener?,
    var isDayShown: Boolean,
    var isTitleShown: Boolean,
    var customTitle: String?,
    var defaultHour: Int = 0,
    var defaultMinute: Int = 0
) : DialogFragment(), OnTimeChangedListener {
    private var mPicker: TimePicker? = null
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
        updateTitle(defaultHour, defaultMinute)

        val container = view.findViewById<ViewGroup>(R.id.datePickerContainer)

        vTitle = view.findViewById(R.id.vTitle)
        vTitle?.setTextColor(blackColor)

        val btnCancel = view.findViewById(R.id.vCancel) as AppCompatTextView
        btnCancel.setTextColor(blackColor)
        btnCancel.setOnClickListener {
            if (mOnCancel != null) {
                mPicker?.clearFocus()
                mOnCancel.onCancelled(mPicker)
            }
            dismiss()
        }

        val btnOk = view.findViewById(R.id.vOk) as AppCompatTextView
        btnOk.setTextColor(mainColor)
        btnOk.setOnClickListener {
            if (mCallBack != null) {
                mPicker?.clearFocus()
                mCallBack.onTimeSet(
                    mPicker, mPicker?.mCurrentHour ?: 0,
                    mPicker?.mCurrentMinute ?: 0
                )
            }
            dismiss()
        }
        mPicker = TimePicker(container, mainColor, blackColor)
        mPicker?.init(
            hour = defaultHour,
            minute = defaultMinute,
            isDayShown,
            this
        )

    }

    interface OnTimeSetListener {
        fun onTimeSet(view: TimePicker?, hour: Int, minute: Int)
    }

    interface OnTimeCancelListener {
        fun onCancelled(view: TimePicker?)
    }

    private fun setTitle(title: String?) {
        if (vTitle != null) {
            vTitle?.text = title
        }
    }

    override fun onTimeChanged(view: TimePicker?, hour: Int, minute: Int) {
        updateTitle(hour, minute)
    }

    private fun updateTitle(hour: Int, minute: Int) {
        if (mIsTitleShown && mCustomTitle != null && !mCustomTitle!!.isEmpty()) {
            setTitle(mCustomTitle)
        } else if (mIsTitleShown) {
            val formatter: NumberFormat = DecimalFormat("00")
            val value = "${formatter.format(hour)}:${formatter.format(minute)}"
            setTitle(value)
        } else {
            setTitle(" ")
        }
    }
}