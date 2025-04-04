package com.sougata.supplysync.util


object Status {

    private var counter = 69

    val STARTED = this.counter++
    val SUCCESS = this.counter++
    val FAILED = this.counter++
    val NO_CHANGE = this.counter++

}