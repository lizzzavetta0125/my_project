const TransactionFactory = require("../factories/TransactionFactory");
const { ReportContext, createReportStrategy } = require("../strategies/ReportStrategies");

/**
 * Facade — единственная «дверь» в бизнес-логику.
 * Контроллер не собирает отчёты, бюджеты и фабрику сам.
 */
class FinanceService {
  constructor({ transactionRepo, categoryRepo, budgetRepo, alertRepo, eventBus }) {
    this.transactionRepo = transactionRepo;
    this.categoryRepo = categoryRepo;
    this.budgetRepo = budgetRepo;
    this.alertRepo = alertRepo;
    this.eventBus = eventBus;
  }

  getDashboard() {
    const now = new Date();
    const monthTx = this.transactionRepo.findByMonth(now.getFullYear(), now.getMonth());
    const income = sumByType(monthTx, "income");
    const expense = sumByType(monthTx, "expense");
    const categories = this.categoryRepo.findAll();

    return {
      balance: income - expense,
      income,
      expense,
      transactionCount: monthTx.length,
      recent: [...this.transactionRepo.findAll()]
        .sort((a, b) => b.date.localeCompare(a.date))
        .slice(0, 6)
        .map((tx) => this.#withCategory(tx, categories)),
      budgets: this.getBudgetStatus(),
      alerts: this.alertRepo.latest(5),
      chart: this.getReport("byCategory").filter((row) => row.type === "expense"),
    };
  }

  listTransactions() {
    const categories = this.categoryRepo.findAll();
    return [...this.transactionRepo.findAll()]
      .sort((a, b) => b.date.localeCompare(a.date))
      .map((tx) => this.#withCategory(tx, categories));
  }

  createTransaction(payload) {
    const category = this.categoryRepo.findById(payload.categoryId);
    if (!category) {
      throw Object.assign(new Error("Категория не найдена"), { status: 400 });
    }
    if (category.type !== payload.type) {
      throw Object.assign(new Error("Тип операции не совпадает с категорией"), { status: 400 });
    }
    if (!payload.amount || Number(payload.amount) <= 0) {
      throw Object.assign(new Error("Сумма должна быть больше нуля"), { status: 400 });
    }

    const transaction = TransactionFactory.create(payload);
    this.transactionRepo.add(transaction.toJSON());
    this.eventBus.emit("transaction:created", transaction.toJSON());
    return this.#withCategory(transaction.toJSON(), this.categoryRepo.findAll());
  }

  deleteTransaction(id) {
    const removed = this.transactionRepo.remove(id);
    if (!removed) {
      throw Object.assign(new Error("Операция не найдена"), { status: 404 });
    }
    return { ok: true };
  }

  listCategories() {
    return this.categoryRepo.findAll();
  }

  createCategory(payload) {
    if (!payload.name || !payload.type) {
      throw Object.assign(new Error("Нужны название и тип категории"), { status: 400 });
    }

    const category = {
      id: crypto.randomUUID(),
      name: payload.name.trim(),
      type: payload.type,
      icon: payload.icon || "label",
    };
    return this.categoryRepo.add(category);
  }

  getBudgetStatus() {
    const now = new Date();
    const monthTx = this.transactionRepo.findByMonth(now.getFullYear(), now.getMonth());
    const categories = this.categoryRepo.findAll();

    return this.budgetRepo.findAll().map((budget) => {
      const spent = monthTx
        .filter((tx) => tx.type === "expense" && tx.categoryId === budget.categoryId)
        .reduce((sum, tx) => sum + tx.amount, 0);
      const category = categories.find((item) => item.id === budget.categoryId);
      const percent = budget.limit === 0 ? 0 : Math.round((spent / budget.limit) * 100);

      return {
        categoryId: budget.categoryId,
        categoryName: category ? category.name : "Категория",
        icon: category ? category.icon : "label",
        limit: budget.limit,
        spent,
        left: budget.limit - spent,
        percent,
        over: spent > budget.limit,
      };
    });
  }

  saveBudget(categoryId, limit) {
    const category = this.categoryRepo.findById(categoryId);
    if (!category || category.type !== "expense") {
      throw Object.assign(new Error("Бюджет задаётся только для расходов"), { status: 400 });
    }
    return this.budgetRepo.upsert(categoryId, limit);
  }

  getReport(strategyName) {
    const context = new ReportContext(createReportStrategy(strategyName));
    return context.generate(this.transactionRepo.findAll(), this.categoryRepo.findAll());
  }

  #withCategory(tx, categories) {
    const category = categories.find((item) => item.id === tx.categoryId);
    return {
      ...tx,
      categoryName: category ? category.name : "Без категории",
      categoryIcon: category ? category.icon : "label",
    };
  }
}

function sumByType(items, type) {
  return items.filter((item) => item.type === type).reduce((sum, item) => sum + item.amount, 0);
}

module.exports = FinanceService;
