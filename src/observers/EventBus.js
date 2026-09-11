/**
 * Observer — подписчики реагируют на события, не зная друг о друге.
 * EventBus = издатель, BudgetObserver = подписчик.
 */
class EventBus {
  constructor() {
    this.listeners = new Map();
  }

  subscribe(eventName, handler) {
    const list = this.listeners.get(eventName) || [];
    list.push(handler);
    this.listeners.set(eventName, list);
  }

  emit(eventName, payload) {
    const list = this.listeners.get(eventName) || [];
    for (const handler of list) {
      handler(payload);
    }
  }
}

module.exports = EventBus;
