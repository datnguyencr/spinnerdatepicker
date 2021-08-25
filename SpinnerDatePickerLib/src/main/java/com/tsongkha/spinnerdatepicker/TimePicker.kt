package com.tsongkha.spinnerdatepicker

import android.content.Context
import android.os.Parcel
import android.os.Parcelable
import android.os.Parcelable.Creator
import android.view.LayoutInflater
import android.view.ViewGroup
import android.view.accessibility.AccessibilityEvent
import android.widget.FrameLayout
import android.widget.LinearLayout
import com.shawnlin.numberpicker.NumberPicker
import java.lang.reflect.InvocationTargetException
import java.util.*

class TimePicker(root: ViewGroup, mainColor: Int, blackColor: Int) : FrameLayout(root.context) {
    private val mPickerContainer: LinearLayout
    private lateinit var mMinuteSpinner: NumberPicker
    private lateinit var mHourSpinner: NumberPicker
    private val mContext: Context
    private var mOnDateChangedListener: OnTimeChangedListener? = null
    var mCurrentHour: Int = 0
    var mCurrentMinute: Int = 0
    private var mIsEnabled = DEFAULT_ENABLED_STATE
    private var mIsDayShown = true
    var minHour = 0
    var maxHour = 23
    var minMinute = 0
    var maxMinute = 59
    fun fixUpdateNumberPicker(picker: NumberPicker) {
        try {
            val method = picker.javaClass.getDeclaredMethod(
                "changeValueByOne",
                Boolean::class.javaPrimitiveType
            )
            method.isAccessible = true
            method.invoke(picker, true)
        } catch (e: NoSuchMethodException) {
            e.printStackTrace()
        } catch (e: IllegalArgumentException) {
            e.printStackTrace()
        } catch (e: InvocationTargetException) {
            e.printStackTrace()
        } catch (e: IllegalAccessException) {
            e.printStackTrace()
        }
    }

    fun init(
        hour: Int,
        minute: Int,
        isDayShown: Boolean,
        onDateChangedListener: OnTimeChangedListener?
    ) {
        mIsDayShown = isDayShown
        setTime(hour, minute)
        updateSpinners()
        mOnDateChangedListener = onDateChangedListener
        notifyTimeChanged()
    }

    override fun setEnabled(enabled: Boolean) {
        mMinuteSpinner.isEnabled = enabled
        mHourSpinner.isEnabled = enabled
        mIsEnabled = enabled
    }

    override fun isEnabled(): Boolean {
        return mIsEnabled
    }

    override fun dispatchPopulateAccessibilityEvent(event: AccessibilityEvent): Boolean {
        onPopulateAccessibilityEvent(event)
        return true
    }

    private fun getCalendarForLocale(oldCalendar: Calendar?, locale: Locale): Calendar {
        return if (oldCalendar == null) {
            Calendar.getInstance(locale)
        } else {
            val currentTimeMillis = oldCalendar.timeInMillis
            val newCalendar = Calendar.getInstance(locale)
            newCalendar.timeInMillis = currentTimeMillis
            newCalendar
        }
    }

    private fun reorderSpinners() {
        mPickerContainer.removeAllViews()
        mPickerContainer.addView(mHourSpinner)
        mPickerContainer.addView(mMinuteSpinner)
    }

    private fun setTime(hour: Int, minute: Int) {
        mCurrentHour = hour
        mCurrentMinute = minute
    }

    private fun updateSpinners() {

        mMinuteSpinner.displayedValues = (minMinute..maxMinute).map { it.toString() }.toTypedArray()
        mMinuteSpinner.minValue = minMinute
        mMinuteSpinner.maxValue = maxMinute
        mMinuteSpinner.wrapSelectorWheel = false

        mHourSpinner.displayedValues = (minHour..maxHour).map { it.toString() }.toTypedArray()
        mHourSpinner.minValue = minHour
        mHourSpinner.maxValue = maxHour
        mHourSpinner.wrapSelectorWheel = false

        // set the spinner values
        mHourSpinner.value = mCurrentHour
        mMinuteSpinner.value = mCurrentMinute
    }

    private fun notifyTimeChanged() {
        sendAccessibilityEvent(AccessibilityEvent.TYPE_VIEW_SELECTED)
        if (mOnDateChangedListener != null) {
            mOnDateChangedListener?.onTimeChanged(
                this, mCurrentHour, mCurrentMinute
            )
        }
    }

    override fun onSaveInstanceState(): Parcelable? {
        val superState = super.onSaveInstanceState()
        return SavedState(superState, mCurrentHour, mCurrentMinute, mIsDayShown)
    }

    override fun onRestoreInstanceState(state: Parcelable) {
        val ss = state as SavedState
        super.onRestoreInstanceState(ss.superState)
        mCurrentHour = ss.hour
        mCurrentMinute = ss.min
        updateSpinners()
    }

    private class SavedState : BaseSavedState {
        val hour: Int
        val min: Int
        val isDaySpinnerShown: Boolean

        internal constructor(
            superState: Parcelable?,
            hour: Int,
            minute: Int,
            isDaySpinnerShown: Boolean
        ) : super(superState) {
            this.hour = hour
            this.min = minute
            this.isDaySpinnerShown = isDaySpinnerShown
        }

        private constructor(`in`: Parcel) : super(`in`) {
            hour = `in`.readInt()
            min = `in`.readInt()
            isDaySpinnerShown = `in`.readByte().toInt() != 0
        }

        override fun writeToParcel(dest: Parcel, flags: Int) {
            super.writeToParcel(dest, flags)
            dest.writeInt(hour)
            dest.writeInt(min)
            dest.writeByte(if (isDaySpinnerShown) 1.toByte() else 0.toByte())
        }

        companion object {
            @JvmField
            val CREATOR: Creator<SavedState> = object : Creator<SavedState> {
                override fun createFromParcel(`in`: Parcel): SavedState {
                    return SavedState(`in`)
                }

                override fun newArray(size: Int): Array<SavedState?> {
                    return arrayOfNulls(size)
                }
            }
        }
    }

    companion object {
        private const val DEFAULT_ENABLED_STATE = true
    }

    init {
        mContext = root.context
        val inflater = mContext.getSystemService(
            Context.LAYOUT_INFLATER_SERVICE
        ) as LayoutInflater
        inflater.inflate(R.layout.time_picker_container, this, true)
        mPickerContainer = findViewById(R.id.parent)
        val onChangeListener =
            NumberPicker.OnValueChangeListener { picker: NumberPicker, oldVal: Int, newVal: Int ->
                // take care of wrapping of days and months to update greater fields
                var hour = mCurrentHour
                var minute = mCurrentMinute
                if (picker === mHourSpinner) {
                    hour = newVal
                } else if (picker === mMinuteSpinner) {
                    minute = newVal
                } else {
                    throw IllegalArgumentException()
                }
                // now set the date to the adjusted one
                setTime(
                    hour = hour,
                    minute = minute
                )
                updateSpinners()
                notifyTimeChanged()
            }

        mMinuteSpinner = inflater.inflate(
            R.layout.number_picker_time,
            mPickerContainer, false
        ) as NumberPicker
        mMinuteSpinner.dividerColor = mainColor
        mMinuteSpinner.id = R.id.minute
        mMinuteSpinner.minValue = minMinute
        mMinuteSpinner.maxValue = maxMinute

        mMinuteSpinner.setOnLongPressUpdateInterval(200)
        mMinuteSpinner.setOnValueChangedListener(onChangeListener)
        fixUpdateNumberPicker(mMinuteSpinner)

        mHourSpinner = inflater.inflate(
            R.layout.number_picker_time,
            mPickerContainer, false
        ) as NumberPicker
        mHourSpinner.dividerColor = mainColor
        mHourSpinner.id = R.id.hour
        mMinuteSpinner.minValue = minHour
        mMinuteSpinner.maxValue = maxHour
        mHourSpinner.setOnLongPressUpdateInterval(100)
        mHourSpinner.setOnValueChangedListener(onChangeListener)
        fixUpdateNumberPicker(mHourSpinner)

        reorderSpinners()

        // If not explicitly specified this view is important for accessibility.
        if (importantForAccessibility == IMPORTANT_FOR_ACCESSIBILITY_AUTO) {
            importantForAccessibility = IMPORTANT_FOR_ACCESSIBILITY_YES
        }
        root.addView(this)
    }
}