const express = require("express");
const { withLogging } = require("../middleware/loggerDecorator");

function createApiRouter(controller) {
  const router = express.Router();

  router.get("/dashboard", withLogging(controller.dashboard, "dashboard"));
  router.get("/transactions", withLogging(controller.listTransactions, "listTransactions"));
  router.post("/transactions", withLogging(controller.createTransaction, "createTransaction"));
  router.delete("/transactions/:id", withLogging(controller.deleteTransaction, "deleteTransaction"));
  router.get("/categories", withLogging(controller.listCategories, "listCategories"));
  router.post("/categories", withLogging(controller.createCategory, "createCategory"));
  router.get("/budgets", withLogging(controller.listBudgets, "listBudgets"));
  router.put("/budgets", withLogging(controller.saveBudget, "saveBudget"));
  router.get("/reports", withLogging(controller.report, "report"));

  return router;
}

module.exports = createApiRouter;
