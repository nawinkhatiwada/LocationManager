package com.androidbolts.locationmanager

inline fun <R> R?.orElse(block: () -> R): R {
    return this ?: block()
}