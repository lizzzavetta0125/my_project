/**
 * Decorator — оборачивает обработчик маршрута и добавляет лог,
 * не меняя саму бизнес-логику контроллера.
 */
function withLogging(handler, actionName) {
  return async (req, res, next) => {
    const started = Date.now();
    console.log(`[LOG] ${actionName} → ${req.method} ${req.originalUrl}`);

    try {
      await handler(req, res, next);
      console.log(`[LOG] ${actionName} готово за ${Date.now() - started} мс`);
    } catch (error) {
      console.log(`[LOG] ${actionName} ошибка: ${error.message}`);
      next(error);
    }
  };
}

module.exports = { withLogging };
