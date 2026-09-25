# FinMate

Мобильное приложение для учёта личных финансов на **Kotlin + Jetpack Compose** (Material Design 3).

## Запуск

1. Открыть папку проекта в Android Studio
2. Дождаться Gradle Sync
3. Запустить на эмуляторе или телефоне (Run ▶)

Нужны: Android Studio, JDK 17, Android SDK.

## Что умеет

- дашборд за месяц
- доходы и расходы
- бюджеты по категориям
- отчёты (по категориям / дням / типу)
- предупреждение при превышении лимита

## Ветки

Ветка `main` — рабочая. От неё отделялись фичи:

| Ветка | Содержание |
| --- | --- |
| `feature/models-factory` | модели, Factory |
| `feature/patterns` | Strategy, Observer |
| `feature/data-layer` | Singleton DB, Repository |
| `feature/ui` | экраны Compose |
| `docs/readme` | правки README |

## Архитектура

`UI` → `ViewModel` → `FinanceService` → `Repository` → `LocalDatabase`

| Пакет | Назначение |
| --- | --- |
| `ui/` | Compose-экраны, ViewModel |
| `domain/service/` | Facade (FinanceService) |
| `domain/factory/` | TransactionFactory |
| `domain/strategy/` | отчёты |
| `domain/observer/` | EventBus, BudgetObserver |
| `data/repository/` | FinanceRepository |
| `data/local/` | JSON-хранилище |
| `data/model/` | модели |

## Паттерны

| Паттерн | Где |
| --- | --- |
| MVVM | `ui/FinanceViewModel.kt`, `ui/Screens.kt` |
| Singleton | `data/local/LocalDatabase.kt` |
| Repository | `data/repository/FinanceRepository.kt` |
| Factory Method | `domain/factory/TransactionFactory.kt` |
| Strategy | `domain/strategy/ReportStrategies.kt` |
| Observer | `domain/observer/EventBus.kt`, `BudgetObserver.kt` |
| Facade | `domain/service/FinanceService.kt` |
