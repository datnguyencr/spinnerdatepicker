package com.tsongkha.spinnerdatepicker

import android.content.Context
import android.content.res.Configuration
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

class DatePicker(root: ViewGroup, mainColor: Int, blackColor: Int) : FrameLayout(root.context) {
    private val mPickerContainer: LinearLayout
    private lateinit var mDaySpinner: NumberPicker
    private lateinit var mMonthSpinner: NumberPicker
    private lateinit var mYearSpinner: NumberPicker
    private val mContext: Context
    private var mOnDateChangedListener: OnDateChangedListener? = null
    private var mShortMonths: Array<String?> = arrayOf()
    private var mNumberOfMonths = 0
    private var mTempDate: Calendar = Calendar.getInstance()
    private var mMinDate: Calendar = Calendar.getInstance()
    private var mMaxDate: Calendar = Calendar.getInstance()
    private var mCurrentDate: Calendar = Calendar.getInstance()
    private var mIsEnabled = DEFAULT_ENABLED_STATE
    private var mIsDayShown = true

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
        year: Int, monthOfYear: Int, dayOfMonth: Int,
        isDayShown: Boolean, onDateChangedListener: OnDateChangedListener?
    ) {
        mIsDayShown = isDayShown
        setDate(year, monthOfYear, dayOfMonth)
        updateSpinners()
        mOnDateChangedListener = onDateChangedListener
        notifyDateChanged()
    }

    val year: Int
        get() = mCurrentDate[Calendar.YEAR]
    val month: Int
        get() = mCurrentDate[Calendar.MONTH]
    val dayOfMonth: Int
        get() = mCurrentDate[Calendar.DAY_OF_MONTH]

    fun setMinDate(minDate: Long) {
        mTempDate.timeInMillis = minDate
        if (mTempDate[Calendar.YEAR] == mMinDate[Calendar.YEAR]
            && mTempDate[Calendar.DAY_OF_YEAR] == mMinDate[Calendar.DAY_OF_YEAR]
        ) {
            // Same day, no-op.
            return
        }
        mMinDate.timeInMillis = minDate
        if (mCurrentDate.before(mMinDate)) {
            mCurrentDate.timeInMillis = mMinDate.timeInMillis
        }
        updateSpinners()
    }

    fun setMaxDate(maxDate: Long) {
        mTempDate.timeInMillis = maxDate
        if (mTempDate[Calendar.YEAR] == mMaxDate[Calendar.YEAR]
            && mTempDate[Calendar.DAY_OF_YEAR] == mMaxDate[Calendar.DAY_OF_YEAR]
        ) {
            // Same day, no-op.
            return
        }
        mMaxDate.timeInMillis = maxDate
        if (mCurrentDate.after(mMaxDate)) {
            mCurrentDate.timeInMillis = mMaxDate.timeInMillis
        }
        updateSpinners()
    }

    override fun setEnabled(enabled: Boolean) {
        mDaySpinner.isEnabled = enabled
        mMonthSpinner.isEnabled = enabled
        mYearSpinner.isEnabled = enabled
        mIsEnabled = enabled
    }

    override fun isEnabled(): Boolean {
        return mIsEnabled
    }

    public override fun onConfigurationChanged(newConfig: Configuration) {
        setCurrentLocale(newConfig.locale)
    }

    override fun dispatchPopulateAccessibilityEvent(event: AccessibilityEvent): Boolean {
        onPopulateAccessibilityEvent(event)
        return true
    }

    protected fun setCurrentLocale(locale: Locale) {
        mTempDate = getCalendarForLocale(mTempDate, locale)
        mMinDate = getCalendarForLocale(mMinDate, locale)
        mMaxDate = getCalendarForLocale(mMaxDate, locale)
        mCurrentDate = getCalendarForLocale(mCurrentDate, locale)
        mNumberOfMonths = mTempDate.getActualMaximum(Calendar.MONTH) + 1
        mShortMonths = arrayOf(
            "1", "2", "3", "4", "5", "6",
            "7", "8", "9", "10", "11", "12"
        )
        for (i in 0 until mNumberOfMonths) {
            mShortMonths[i] = String.format("%s月", i + 1)
        }
        if (usingNumericMonths()) {
            // We're in a locale where a date should either be all-numeric, or all-text.
            // All-text would require custom NumberPicker formatters for day and year.
            mShortMonths = arrayOfNulls(mNumberOfMonths)
            for (i in 0 until mNumberOfMonths) {
                mShortMonths[i] = String.format("%d月", i + 1)
            }
        }
    }

    private fun usingNumericMonths(): Boolean {
        return Character.isDigit(mShortMonths[Calendar.JANUARY]?.get(0)!!)
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
        mPickerContainer.addView(mYearSpinner)
        mPickerContainer.addView(mMonthSpinner)
        mPickerContainer.addView(mDaySpinner)
    }

    private fun setDate(year: Int, month: Int, dayOfMonth: Int) {
        mCurrentDate[year, month] = dayOfMonth
        if (mCurrentDate.before(mMinDate)) {
            mCurrentDate.timeInMillis = mMinDate.timeInMillis
        } else if (mCurrentDate.after(mMaxDate)) {
            mCurrentDate.timeInMillis = mMaxDate.timeInMillis
        }
    }

    private fun updateSpinners() {
        // set the spinner ranges respecting the min and max dates
        mDaySpinner.visibility = if (mIsDayShown) VISIBLE else GONE
        if (mCurrentDate == mMinDate) {
            mDaySpinner.minValue = mCurrentDate[Calendar.DAY_OF_MONTH]
            mDaySpinner.maxValue = mCurrentDate.getActualMaximum(Calendar.DAY_OF_MONTH)
            mDaySpinner.wrapSelectorWheel = false
            mMonthSpinner.displayedValues = null
            mMonthSpinner.minValue = mCurrentDate[Calendar.MONTH]
            mMonthSpinner.maxValue = mCurrentDate.getActualMaximum(Calendar.MONTH)
            mMonthSpinner.wrapSelectorWheel = false
        } else if (mCurrentDate == mMaxDate) {
            mDaySpinner.minValue = mCurrentDate.getActualMinimum(Calendar.DAY_OF_MONTH)
            mDaySpinner.maxValue = mCurrentDate[Calendar.DAY_OF_MONTH]
            mDaySpinner.wrapSelectorWheel = false
            mMonthSpinner.displayedValues = null
            mMonthSpinner.minValue = mCurrentDate.getActualMinimum(Calendar.MONTH)
            mMonthSpinner.maxValue = mCurrentDate[Calendar.MONTH]
            mMonthSpinner.wrapSelectorWheel = false
        } else {
            mDaySpinner.minValue = 1
            mDaySpinner.maxValue = mCurrentDate.getActualMaximum(Calendar.DAY_OF_MONTH)
            mDaySpinner.wrapSelectorWheel = true
            mMonthSpinner.displayedValues = null
            mMonthSpinner.minValue = 0
            mMonthSpinner.maxValue = 11
            mMonthSpinner.wrapSelectorWheel = true
        }

        // make sure the month names are a zero based array
        // with the months in the month spinner
        val displayedValues =
            mShortMonths.copyOfRange(mMonthSpinner.minValue, mMonthSpinner.maxValue + 1)
        mMonthSpinner.displayedValues = displayedValues

        // year spinner range does not change based on the current date
        mYearSpinner.minValue = mMinDate[Calendar.YEAR]
        mYearSpinner.maxValue = mMaxDate[Calendar.YEAR]
        mYearSpinner.wrapSelectorWheel = false

        // set the spinner values
        mYearSpinner.value = mCurrentDate[Calendar.YEAR]
        mMonthSpinner.value = mCurrentDate[Calendar.MONTH]
        mDaySpinner.value = mCurrentDate[Calendar.DAY_OF_MONTH]
    }

    private fun notifyDateChanged() {
        sendAccessibilityEvent(AccessibilityEvent.TYPE_VIEW_SELECTED)
        if (mOnDateChangedListener != null) {
            mOnDateChangedListener?.onDateChanged(
                this, year, month,
                dayOfMonth
            )
        }
    }

    override fun onSaveInstanceState(): Parcelable {
        val superState = super.onSaveInstanceState()
        return SavedState(superState, mCurrentDate, mMinDate, mMaxDate, mIsDayShown)
    }

    override fun onRestoreInstanceState(state: Parcelable) {
        val ss = state as SavedState
        super.onRestoreInstanceState(ss.superState)
        mCurrentDate = Calendar.getInstance()
        mCurrentDate.timeInMillis = ss.currentDate
        mMinDate = Calendar.getInstance()
        mMinDate.timeInMillis = ss.minDate
        mMaxDate = Calendar.getInstance()
        mMaxDate.timeInMillis = ss.maxDate
        updateSpinners()
    }

    private class SavedState : BaseSavedState {
        val currentDate: Long
        val minDate: Long
        val maxDate: Long
        val isDaySpinnerShown: Boolean

        internal constructor(
            superState: Parcelable?,
            currentDate: Calendar,
            minDate: Calendar,
            maxDate: Calendar,
            isDaySpinnerShown: Boolean
        ) : super(superState) {
            this.currentDate = currentDate.timeInMillis
            this.minDate = minDate.timeInMillis
            this.maxDate = maxDate.timeInMillis
            this.isDaySpinnerShown = isDaySpinnerShown
        }

        private constructor(`in`: Parcel) : super(`in`) {
            currentDate = `in`.readLong()
            minDate = `in`.readLong()
            maxDate = `in`.readLong()
            isDaySpinnerShown = `in`.readByte().toInt() != 0
        }

        override fun writeToParcel(dest: Parcel, flags: Int) {
            super.writeToParcel(dest, flags)
            dest.writeLong(currentDate)
            dest.writeLong(minDate)
            dest.writeLong(maxDate)
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

        // initialization based on locale
        setCurrentLocale(Locale.getDefault())
        val inflater = mContext.getSystemService(
            Context.LAYOUT_INFLATER_SERVICE
        ) as LayoutInflater
        inflater.inflate(R.layout.date_picker_container, this, true)
        mPickerContainer = findViewById(R.id.parent)
        val onChangeListener =
            NumberPicker.OnValueChangeListener { picker: NumberPicker, oldVal: Int, newVal: Int ->
                mTempDate.timeInMillis = mCurrentDate.timeInMillis
                // take care of wrapping of days and months to update greater fields
                if (picker === mDaySpinner) {
                    val maxDayOfMonth = mTempDate.getActualMaximum(Calendar.DAY_OF_MONTH)
                    if (oldVal == maxDayOfMonth && newVal == 1) {
                        mTempDate.add(Calendar.DAY_OF_MONTH, 1)
                    } else if (oldVal == 1 && newVal == maxDayOfMonth) {
                        mTempDate.add(Calendar.DAY_OF_MONTH, -1)
                    } else {
                        mTempDate.add(Calendar.DAY_OF_MONTH, newVal - oldVal)
                    }
                } else if (picker === mMonthSpinner) {
                    if (oldVal == 11 && newVal == 0) {
                        mTempDate.add(Calendar.MONTH, 1)
                    } else if (oldVal == 0 && newVal == 11) {
                        mTempDate.add(Calendar.MONTH, -1)
                    } else {
                        mTempDate.add(Calendar.MONTH, newVal - oldVal)
                    }
                } else if (picker === mYearSpinner) {
                    mTempDate[Calendar.YEAR] = newVal
                } else {
                    throw IllegalArgumentException()
                }
                // now set the date to the adjusted one
                setDate(
                    mTempDate[Calendar.YEAR], mTempDate[Calendar.MONTH],
                    mTempDate[Calendar.DAY_OF_MONTH]
                )
                updateSpinners()
                notifyDateChanged()
            }

        // day
        mDaySpinner = inflater.inflate(
            R.layout.number_picker_day,
            mPickerContainer, false
        ) as NumberPicker
        mDaySpinner.dividerColor = mainColor
        //        mDaySpinner.setTextColor(blackColor);
        mDaySpinner.id = R.id.day
        mDaySpinner.setOnLongPressUpdateInterval(100)
        mDaySpinner.setOnValueChangedListener(onChangeListener)
        //        mDaySpinnerInput = NumberPickers.findEditText(mDaySpinner);
        fixUpdateNumberPicker(mDaySpinner)

        // month
        mMonthSpinner = inflater.inflate(
            R.layout.number_picker_day_month,
            mPickerContainer, false
        ) as NumberPicker
        mMonthSpinner.dividerColor = mainColor
        //        mMonthSpinner.setTextColor(blackColor);
        mMonthSpinner.id = R.id.month
        mMonthSpinner.minValue = 0
        mMonthSpinner.maxValue = mNumberOfMonths - 1
        mMonthSpinner.displayedValues = mShortMonths
        mMonthSpinner.setOnLongPressUpdateInterval(200)
        mMonthSpinner.setOnValueChangedListener(onChangeListener)
        //        mMonthSpinnerInput = NumberPickers.findEditText(mMonthSpinner);
        fixUpdateNumberPicker(mMonthSpinner)

        // year
        mYearSpinner = inflater.inflate(
            R.layout.number_picker_year,
            mPickerContainer, false
        ) as NumberPicker
        mYearSpinner.dividerColor = mainColor
        //        mYearSpinner.setTextColor(blackColor);
        mYearSpinner.id = R.id.year
        mYearSpinner.setOnLongPressUpdateInterval(100)
        mYearSpinner.setOnValueChangedListener(onChangeListener)
        //        mYearSpinnerInput = NumberPickers.findEditText(mYearSpinner);
        fixUpdateNumberPicker(mYearSpinner)

        // initialize to current date
        mCurrentDate.timeInMillis = DateUtils.timeInMillis()

        // re-order the number spinners to match the current date format
        reorderSpinners()

        // If not explicitly specified this view is important for accessibility.
        if (importantForAccessibility == IMPORTANT_FOR_ACCESSIBILITY_AUTO) {
            importantForAccessibility = IMPORTANT_FOR_ACCESSIBILITY_YES
        }
        root.addView(this)
    }
}