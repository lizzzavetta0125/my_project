/**
 * Repository — вся работа с коллекциями данных в одном месте.
 * Сервисы не трогают файл БД напрямую.
 */
class BaseRepository {
  constructor(collectionName) {
    this.collectionName = collectionName;
    this.db = require("../db/Database").getInstance();
  }

  findAll() {
    return this.db.getCollection(this.collectionName);
  }

  findById(id) {
    return this.findAll().find((item) => item.id === id) || null;
  }

  add(item) {
    const items = this.findAll();
    items.push(item);
    this.db.setCollection(this.collectionName, items);
    return item;
  }

  update(id, patch) {
    const items = this.findAll();
    const index = items.findIndex((item) => item.id === id);
    if (index === -1) return null;
    items[index] = { ...items[index], ...patch, id };
    this.db.setCollection(this.collectionName, items);
    return items[index];
  }

  remove(id) {
    const items = this.findAll();
    const next = items.filter((item) => item.id !== id);
    if (next.length === items.length) return false;
    this.db.setCollection(this.collectionName, next);
    return true;
  }
}

module.exports = BaseRepository;
