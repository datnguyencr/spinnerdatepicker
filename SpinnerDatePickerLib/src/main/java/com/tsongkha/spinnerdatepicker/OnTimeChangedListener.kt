package com.tsongkha.spinnerdatepicker

interface OnTimeChangedListener {
    fun onTimeChanged(view: TimePicker?, hour: Int, minute: Int)
}