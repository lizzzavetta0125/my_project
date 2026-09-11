/**
 * Strategy — один и тот же отчёт считается разными алгоритмами.
 * Контекст (ReportContext) не знает, какой именно алгоритм вызван.
 */

class ReportStrategy {
  execute(_transactions, _categories) {
    throw new Error("Стратегия должна реализовать execute()");
  }
}

class ByCategoryStrategy extends ReportStrategy {
  execute(transactions, categories) {
    const map = new Map();

    for (const tx of transactions) {
      const current = map.get(tx.categoryId) || 0;
      map.set(tx.categoryId, current + tx.amount);
    }

    return [...map.entries()].map(([categoryId, total]) => {
      const category = categories.find((item) => item.id === categoryId);
      return {
        key: categoryId,
        label: category ? category.name : "Без категории",
        type: category ? category.type : "expense",
        total,
      };
    });
  }
}

class ByDayStrategy extends ReportStrategy {
  execute(transactions) {
    const map = new Map();

    for (const tx of transactions) {
      const day = tx.date.slice(0, 10);
      const current = map.get(day) || { income: 0, expense: 0 };
      current[tx.type] += tx.amount;
      map.set(day, current);
    }

    return [...map.entries()]
      .sort((a, b) => a[0].localeCompare(b[0]))
      .map(([day, totals]) => ({
        key: day,
        label: formatDay(day),
        income: totals.income,
        expense: totals.expense,
        total: totals.income - totals.expense,
      }));
  }
}

class ByTypeStrategy extends ReportStrategy {
  execute(transactions) {
    const income = transactions
      .filter((tx) => tx.type === "income")
      .reduce((sum, tx) => sum + tx.amount, 0);
    const expense = transactions
      .filter((tx) => tx.type === "expense")
      .reduce((sum, tx) => sum + tx.amount, 0);

    return [
      { key: "income", label: "Доходы", total: income },
      { key: "expense", label: "Расходы", total: expense },
    ];
  }
}

class ReportContext {
  constructor(strategy) {
    this.strategy = strategy;
  }

  setStrategy(strategy) {
    this.strategy = strategy;
  }

  generate(transactions, categories) {
    return this.strategy.execute(transactions, categories);
  }
}

function createReportStrategy(name) {
  switch (name) {
    case "byDay":
      return new ByDayStrategy();
    case "byType":
      return new ByTypeStrategy();
    case "byCategory":
    default:
      return new ByCategoryStrategy();
  }
}

function formatDay(isoDate) {
  const [year, month, day] = isoDate.split("-");
  return `${day}.${month}.${year}`;
}

module.exports = {
  ReportContext,
  createReportStrategy,
  ByCategoryStrategy,
  ByDayStrategy,
  ByTypeStrategy,
};
