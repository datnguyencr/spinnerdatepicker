package com.tsongkha.spinnerdatepicker

interface OnDateChangedListener {
    fun onDateChanged(view: DatePicker?, year: Int, monthOfYear: Int, dayOfMonth: Int)
}