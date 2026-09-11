/**
 * MVC: Controller принимает HTTP-запрос и отдаёт ответ.
 * Сам расчёт и хранение — в сервисе и репозиториях.
 */
class FinanceController {
  constructor(financeService) {
    this.financeService = financeService;
  }

  dashboard = (req, res) => {
    res.json(this.financeService.getDashboard());
  };

  listTransactions = (req, res) => {
    res.json(this.financeService.listTransactions());
  };

  createTransaction = (req, res) => {
    const created = this.financeService.createTransaction(req.body);
    res.status(201).json(created);
  };

  deleteTransaction = (req, res) => {
    res.json(this.financeService.deleteTransaction(req.params.id));
  };

  listCategories = (req, res) => {
    res.json(this.financeService.listCategories());
  };

  createCategory = (req, res) => {
    const created = this.financeService.createCategory(req.body);
    res.status(201).json(created);
  };

  listBudgets = (req, res) => {
    res.json(this.financeService.getBudgetStatus());
  };

  saveBudget = (req, res) => {
    const saved = this.financeService.saveBudget(req.body.categoryId, req.body.limit);
    res.json(saved);
  };

  report = (req, res) => {
    const strategy = req.query.strategy || "byCategory";
    res.json({
      strategy,
      rows: this.financeService.getReport(strategy),
    });
  };
}

module.exports = FinanceController;
