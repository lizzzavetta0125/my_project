const express = require("express");
const path = require("path");

const Database = require("./db/Database");
const EventBus = require("./observers/EventBus");
const BudgetObserver = require("./observers/BudgetObserver");
const FinanceService = require("./services/FinanceService");
const FinanceController = require("./controllers/FinanceController");
const createApiRouter = require("./routes/api");
const {
  TransactionRepository,
  CategoryRepository,
  BudgetRepository,
  AlertRepository,
} = require("./repositories/Repositories");

Database.getInstance();

const eventBus = new EventBus();
const transactionRepo = new TransactionRepository();
const categoryRepo = new CategoryRepository();
const budgetRepo = new BudgetRepository();
const alertRepo = new AlertRepository();

new BudgetObserver({ eventBus, transactionRepo, budgetRepo, alertRepo });

const financeService = new FinanceService({
  transactionRepo,
  categoryRepo,
  budgetRepo,
  alertRepo,
  eventBus,
});
const controller = new FinanceController(financeService);

const app = express();
const PORT = process.env.PORT || 3000;

app.use(express.json());
app.use(express.static(path.join(__dirname, "../public")));
app.use("/api", createApiRouter(controller));

app.use((error, _req, res, _next) => {
  const status = error.status || 500;
  res.status(status).json({ error: error.message || "Ошибка сервера" });
});

app.listen(PORT, () => {
  console.log(`FinMate запущен: http://localhost:${PORT}`);
});
