class Transaction {
  constructor({ id, amount, categoryId, note, date }) {
    this.id = id || crypto.randomUUID();
    this.amount = Number(amount);
    this.categoryId = categoryId;
    this.note = note || "";
    this.date = date || new Date().toISOString();
  }

  signedAmount() {
    throw new Error("Метод должен быть переопределён в наследнике");
  }

  toJSON() {
    return {
      id: this.id,
      type: this.type,
      amount: this.amount,
      categoryId: this.categoryId,
      note: this.note,
      date: this.date,
    };
  }
}

class IncomeTransaction extends Transaction {
  constructor(data) {
    super(data);
    this.type = "income";
  }

  signedAmount() {
    return this.amount;
  }
}

class ExpenseTransaction extends Transaction {
  constructor(data) {
    super(data);
    this.type = "expense";
  }

  signedAmount() {
    return -this.amount;
  }
}

module.exports = { Transaction, IncomeTransaction, ExpenseTransaction };
