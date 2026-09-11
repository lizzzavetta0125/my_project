const fs = require("fs");
const path = require("path");

/**
 * Singleton — один экземпляр хранилища на всё приложение.
 * Точка входа: Database.getInstance()
 */
class Database {
  constructor() {
    if (Database.instance) {
      return Database.instance;
    }

    this.filePath = path.join(__dirname, "../../data/db.json");
    this.data = this.#load();
    Database.instance = this;
  }

  static getInstance() {
    if (!Database.instance) {
      Database.instance = new Database();
    }
    return Database.instance;
  }

  getCollection(name) {
    if (!this.data[name]) {
      this.data[name] = [];
    }
    return this.data[name];
  }

  setCollection(name, items) {
    this.data[name] = items;
    this.save();
  }

  save() {
    fs.mkdirSync(path.dirname(this.filePath), { recursive: true });
    fs.writeFileSync(this.filePath, JSON.stringify(this.data, null, 2), "utf8");
  }

  #load() {
    if (!fs.existsSync(this.filePath)) {
      return { categories: [], budgets: [], transactions: [], alerts: [] };
    }
    return JSON.parse(fs.readFileSync(this.filePath, "utf8"));
  }
}

module.exports = Database;
