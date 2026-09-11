/**
 * Observer: следит за новыми расходами и пишет предупреждение,
 * если категория вышла за месячный бюджет.
 */
class BudgetObserver {
  constructor({ eventBus, transactionRepo, budgetRepo, alertRepo }) {
    this.transactionRepo = transactionRepo;
    this.budgetRepo = budgetRepo;
    this.alertRepo = alertRepo;
    eventBus.subscribe("transaction:created", (tx) => this.onTransaction(tx));
  }

  onTransaction(tx) {
    if (tx.type !== "expense") return;

    const budget = this.budgetRepo.findByCategoryId(tx.categoryId);
    if (!budget) return;

    const date = new Date(tx.date);
    const monthKey = `${date.getFullYear()}-${date.getMonth() + 1}`;
    const spent = this.transactionRepo
      .findByMonth(date.getFullYear(), date.getMonth())
      .filter((item) => item.type === "expense" && item.categoryId === tx.categoryId)
      .reduce((sum, item) => sum + item.amount, 0);

    if (spent <= budget.limit) return;

    this.alertRepo.addUnique({
      id: crypto.randomUUID(),
      categoryId: tx.categoryId,
      monthKey,
      spent,
      limit: budget.limit,
      message: `Бюджет превышен: потрачено ${spent} ₽ из ${budget.limit} ₽`,
      createdAt: new Date().toISOString(),
    });
  }
}

module.exports = BudgetObserver;
