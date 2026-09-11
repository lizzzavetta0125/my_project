const BaseRepository = require("./BaseRepository");

class TransactionRepository extends BaseRepository {
  constructor() {
    super("transactions");
  }

  findByMonth(year, month) {
    return this.findAll().filter((item) => {
      const date = new Date(item.date);
      return date.getFullYear() === year && date.getMonth() === month;
    });
  }
}

class CategoryRepository extends BaseRepository {
  constructor() {
    super("categories");
  }
}

class BudgetRepository extends BaseRepository {
  constructor() {
    super("budgets");
  }

  findByCategoryId(categoryId) {
    return this.findAll().find((item) => item.categoryId === categoryId) || null;
  }

  upsert(categoryId, limit) {
    const items = this.findAll();
    const index = items.findIndex((item) => item.categoryId === categoryId);
    const record = { categoryId, limit: Number(limit) };

    if (index === -1) {
      items.push(record);
    } else {
      items[index] = record;
    }

    this.db.setCollection(this.collectionName, items);
    return record;
  }
}

class AlertRepository extends BaseRepository {
  constructor() {
    super("alerts");
  }

  addUnique(alert) {
    const exists = this.findAll().some(
      (item) => item.categoryId === alert.categoryId && item.monthKey === alert.monthKey
    );
    if (exists) return null;
    return this.add(alert);
  }

  latest(limit = 10) {
    return [...this.findAll()].reverse().slice(0, limit);
  }
}

module.exports = {
  TransactionRepository,
  CategoryRepository,
  BudgetRepository,
  AlertRepository,
};
