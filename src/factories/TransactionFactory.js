/**
 * Factory Method — создаёт нужный класс операции по типу,
 * контроллер не знает про Income/Expense напрямую.
 */
const { IncomeTransaction, ExpenseTransaction } = require("../models/Transaction");

class TransactionFactory {
  static create(payload) {
    const type = payload.type;

    if (type === "income") {
      return new IncomeTransaction(payload);
    }

    if (type === "expense") {
      return new ExpenseTransaction(payload);
    }

    throw new Error(`Неизвестный тип операции: ${type}`);
  }

  static fromJSON(raw) {
    return TransactionFactory.create(raw);
  }
}

module.exports = TransactionFactory;
