package com.finmate.app.domain.observer

/** Шина событий для Observer. */
class EventBus {
    private val listeners = mutableMapOf<String, MutableList<(Any) -> Unit>>()

    fun subscribe(eventName: String, handler: (Any) -> Unit) {
        listeners.getOrPut(eventName) { mutableListOf() }.add(handler)
    }

    fun emit(eventName: String, payload: Any) {
        listeners[eventName].orEmpty().forEach { it(payload) }
    }
}
