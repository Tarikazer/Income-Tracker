# APK Version Release Log

## Version 1.11 (Version Code: 12)
- **File**: `IncomeControl-v1.11.apk` (and `app-latest.apk`)
- **Date**: October 10, 2026
- **Package**: `com.aistudio.incomecontrol.tkrzq`

### Features & Updates in v1.11:
1. **Per-Month Categories (Lifecycle & Scoping)**:
   - Added `activeFromMonth: String` (defaults to `'0000-01'`) and `activeUntilMonth: String?` (nullable) to `CategoryEntity` with Room database migration `MIGRATION_5_6` and DB version 6.
   - Creating a new category assigns `activeFromMonth = currentCalendarMonth` (e.g. today's "yyyy-MM") and switches the selected month to that current month, ensuring newly added categories never appear in past months.
   - Deleting a category from a selected month sets `activeUntilMonth = previousMonth` (or completely removes the category if it was created in that month or later) via `repository.deleteCategoryFromMonth(category, selectedMonth)`.
   - Expenses for this category in past months are strictly preserved; only expenses from the selected month onward are deleted.
   - Past months preserve archived categories and their historical expenses. Category statistics, breakdown charts, category history dialogs, and search use `allCategories` so historical names, colors, and icons are never lost.
   - Category reordering (`moveCategoryUp` / `moveCategoryDown`) correctly handles active categories for the selected month.
   - Prepaid coverage (`getCoveredCategoriesForMonth`) ignores months where the category is not active.
2. **Income Note Support**:
   - Added `note: String = ""` to `IncomeEntity` with database migration `MIGRATION_5_6`.
   - In `AddIncomeDialog`, added an optional Note field displayed specifically when adding secondary incomes (labeled "Other Incomes"). The primary "Monthly Income" field remains simple.
   - `IncomeRow` in `BudgetScreen` displays the note beneath the income title when non-blank.
   - Backup export and import preserves and restores `IncomeEntity.note`.
3. **Universal Confirmation Before Every Deletion**:
   - Created reusable composable `ConfirmDeleteDialog(title, message, confirmText, cancelText, onConfirm, onDismiss)` in `ui/components` with theme-aware colors, high-contrast red confirm button, cancel button, and localized strings (EN/FR).
   - Applied `ConfirmDeleteDialog` across:
     * Category deletion (explaining past months are preserved, displaying count of purchases deleted from this month onward).
     * Expense deletion in both `HomeScreen` and `BudgetScreen` (including deletions triggered from `CategoryHistoryDialog`).
     * Income deletion in `BudgetScreen` `IncomeRow`.
     * Restoring a local backup in both `HomeScreen` and `SettingsScreen` (warning that current data will be replaced).
4. **Clean Comparison Rule on Summary Cards**:
   - In `SwipeableHomeSpendingCard`, replaced ad-hoc title/period label string matching with a unified check: hides the comparison line on all pages (Today, Week, Month) whenever `!AppConstants.shouldShowComparison(page.previousAmount)`.
5. **Database Migration & Verification**:
   - Room schema `6.json` exported and verified.
   - Added automated JVM / Robolectric tests for `MIGRATION_5_6`, `deleteCategoryFromMonth`, future month scoping, `shouldShowComparison` edge cases (0.0, -1.0, 0.5), and backup import/export round-trip.

---

## Version 1.10 (Version Code: 11)
- **File**: `IncomeControl-v1.10.apk` (and `app-latest.apk`)
- **Date**: October 8, 2026
- **Package**: `com.aistudio.incomecontrol.tkrzq`

### Features & Updates in v1.10:
1. **AppConstants Month Math & Calendar Safety**:
   - Fixed `AppConstants.addMonths` and `FinanceViewModel.dailySpendingTrend` by initializing `Calendar.DAY_OF_MONTH = 1` prior to setting year and month, avoiding 29–31 day-of-month calendar overflows when calculating short months.
   - Added unit tests for month arithmetic across year boundaries (`2026-11 + 2 = 2027-01`, `2026-12 + 1 = 2027-01`, `2027-02 + 1 = 2027-03`) and with day 31 calendar state.
2. **Prepaid / Advance Multi-Month Payment Coverage (Part A)**:
   - Added `coversMonths: Int = 1` to `ExpenseEntity` with database schema migration `MIGRATION_4_5` (`ALTER TABLE expenses ADD COLUMN coversMonths INTEGER NOT NULL DEFAULT 1`) and Room version bump to 5.
   - `AddCategoryExpenseDialog`: added "Covers" row with selectable chips (1, 2, 3, 6, 12 months) and dynamic date range helper (`Covers Oct 2026 → Dec 2026`), passing `coversMonths` to repository.
   - `EditShoppingExpenseDialog`: displays "Covers" selectable chips when `categoryId != 0` within 24h edit window, preselected with the expense's `coversMonths` value.
   - Wired `categoryCoverageMap` in `BudgetScreen`: passes coverage to `CategoryCard` to display "Already paid · covered until [Month]" check badge with 0.00 spent.
   - Add Anyway Confirmation: tapping "+ Add expense" on a covered category displays an `AlertDialog` confirmation (`[Category] is already paid until [Month]. Add another expense anyway?`) before opening the entry dialog.
   - `ExpenseItemRow`: displays a coverage duration chip (e.g. `3 months` / `3 mois`) next to the title on advance-payment expenses.
   - `CategoryHistoryDialog`: displays a read-only info card (`Covered by [Amount] [Currency] paid in [Month]`) when viewing a covered category with no expenses in the selected month.
   - Backup: `exportBackupJson` persists `coversMonths` and `importBackupJson` parses `optInt("coversMonths", 1)` for backwards compatibility.
3. **Edit Transaction Dialog Layout Bug Fix (Part B)**:
   - Replaced cramped two-column Date and Time row in `EditShoppingExpenseDialog` with a unified, full-width, single-line read-only field labeled "Date & Time" displaying formatted timestamp (`Oct 7, 2026 · 09:23`) with calendar and lock icons.
   - Dialog content made vertically scrollable (`verticalScroll`) for 360dp width and large font scales.
4. **Light Theme Contrast & Theming Polish (Part C)**:
   - Added `accent: Color` theme token to `AppColors` (`LightAppColors.accent = Color(0xFF176FA3)`, `DarkAppColors.accent = Color(0xFFD3E8F8)`), exposed as `AccentOnSurface`.
   - Fixed light-on-light text and icon colors across `ExpenseItemRow`, `CategoryCard`, `StatisticsScreen`, `Charts`, `HomeScreen`, `BudgetScreen`, and `Dialogs`.

---

## Version 1.9 (Version Code: 10)
- **File**: `IncomeControl-v1.9.apk` (and `app-latest.apk`)
- **Date**: October 7, 2026
- **Package**: `com.aistudio.incomecontrol.tkrzq`

### Code Review Hardening & Architectural Fixes in v1.9:
1. **Database Safety & Migration Registry**:
   - Replaced generic destructive migration fallback with `.fallbackToDestructiveMigrationFrom(1, 2, 3)` so version 4+ databases are never wiped.
   - Retained database version 4, enabled `exportSchema = true`, and configured Room schema directory in KSP.
   - Created `data/local/Migrations.kt` with `ALL_MIGRATIONS` registry and migration template.
2. **Category Breakdown Statistics Bug Fix**:
   - Grouped separate purchases (`categoryId = 0`) into a virtual category entry (`CategoryEntity` id = 0, name "Separate Purchases" / FR "Achats séparés", iconKey "shopping", neutral slate color).
   - Included virtual category in the category breakdown list, interactive donut chart, legend, and exported text summary so all percentages sum to 100%.
3. **Month & Date Consistency**:
   - Standardized `addCategoryExpense`, `quickAddExpense`, `addIncome`, and `addIncomeWithSource` to always derive `monthYear` from `dateTimestamp`.
   - Automatically switches selected month to the item's month if different so new entries are visible immediately.
4. **Delete Category Confirmation**:
   - Added `getExpenseCountForCategory` query in `ExpenseDao` and `FinanceRepository`.
   - Added localized confirmation dialog: "Delete <name> and its N purchases? This cannot be undone." (EN + FR).
5. **Application Class Implementation**:
   - Created `IncomeControlApp : Application` managing database and repository lazily with dedicated `applicationScope`.
   - Registered `IncomeControlApp` in `AndroidManifest.xml` and updated `MainActivity` to read the repository from Application.
6. **Build, Size & Signing Optimizations**:
   - Removed custom `debugConfig` signing from debug build type to rely on default Android debug keystore.
   - Release signing checks keystore file existence before configuring.
   - Enabled R8 shrinking and minification (`isMinifyEnabled = true`, `isShrinkResources = true`) with custom keep rules in `proguard-rules.pro`.
   - Removed unused libraries: Retrofit, Moshi, OkHttp, Logging Interceptor, Google Services plugin, and Secrets plugin.
7. **Adaptive Launcher Icon & Monochrome Theming**:
   - Replaced raster foreground with 108x108 VectorDrawable (`ic_launcher_foreground.xml`) featuring light sky blue circle (#D3E8F8, 66dp diameter in safe zone), flat blue wallet (#2B9CE0), and tilted banknotes.
   - Created `ic_launcher_monochrome.xml` silhouette for Android 13+ themed icons.
   - Corrected escaped quotes in `favicon.svg`.
8. **Resource Cleanup**:
   - Removed obsolete legacy images from `res/drawable`.
9. **Unit & Integration Test Suite**:
   - Added JUnit and Robolectric tests for 24h edit window rules, daily expense grouping headers and totals, category percentage sum to 100%, and full JSON backup export/import round trip.
10. **Version Bump**:
   - Bumped to `versionCode = 10` and `versionName = "1.9"`.

---

## Version 1.8 (Version Code: 9)
- **File**: `IncomeControl-v1.8.apk`
- **Date**: October 6, 2026
- **Package**: `com.aistudio.incomecontrol.tkrzq`

### Features & Updates in v1.8:
1. **Local Backup & Restore UI Integration**:
   - Local database export and import via Storage Access Framework (SAF) in Settings and Household profile.
2. **Custom Date & Time Picker**:
   - Backdated purchase support respecting 24-hour universal editing window.
3. **Display & Styling**:
   - Light blue and white default theme with developer contact information in Settings.

---

## Version 1.7 (Version Code: 8)
- **File**: `IncomeControl-v1.7.apk` (and `app-latest.apk`)
- **Date**: October 5, 2026
- **Package**: `com.aistudio.incomecontrol.tkrzq`

### Features & Updates in v1.7:
1. **Local Backup & Restore via Android Storage Access Framework (SAF)**:
   - Full backup and restore functionality without cloud services.
   - Exports the entire Room database (household, categories, incomes, expenses, timestamps) to a clean, portable `.json` file on the user's device (e.g. Downloads, Documents).
   - Allows importing any previous backup file to safely restore all app data.
   - Accessible via the new Backup & Restore button in the top bar and within the Household profile dialog.
2. **Dynamic Comparison Display in HomeScreen**:
   - In the swipeable summary card, if last month's total spending was 0, the difference text compared to last month is automatically hidden.
3. **Updated Income Logic**:
   - The very first income added by the user is automatically categorized and labeled as **Monthly Income**.
   - Any subsequent incomes added after the first are categorized and labeled as **Other Incomes**.
   - Visual badges in the Add Income dialog and Budget screen clearly communicate the income classification.
4. **Custom Date & Time Picker on Add Purchase**:
   - Allows picking date and time for purchases (defaulting to now, preventing future timestamps, supporting backdating, locking transactions backdated >24 hours, and showing as read-only upon edit).

---

## Version 1.6 (Version Code: 7)
- **File**: `IncomeControl-v1.6.apk` (and `app-latest.apk`)
- **Date**: October 3, 2026
- **Package**: `com.aistudio.incomecontrol.tkrzq`

### Features & Updates in v1.6:
1. **Group Purchases by Day with Sticky Headers**:
   - The separate purchases list is grouped by day.
   - Sticky day header before each day's group:
     - Shows "Today", "Yesterday", or "Oct 1, 2026" on the left.
     - Shows that day's total spent on the right (e.g. `120.00 MAD`).
   - Days are sorted newest first, and transactions within each day are sorted newest first.
2. **Universal 24-Hour Edit Window for ALL Items**:
   - Every purchase and category transaction can be edited for 24 hours after creation (`createdAt` timestamp).
   - Within 24 hours: shows an Edit icon and Delete icon.
   - After 24 hours: becomes locked (shows a small lock icon, editing and deletion disabled).
   - Rule automatically applies universally to all categories (Rent, Sport, Bills, Family, Alimentation, and any new custom category created).
   - Deletion is available only during the same 24 hours.
   - Timestamps persist in the database across app restarts.
3. **Complete Removal of Planning Feature**:
   - Completely deleted all planned/expected spending UI, input fields, and data models.
   - The app cleanly displays only actual spent amounts across the budget screen, cards, dialogs, and reports.
   - Categories and summaries focus purely on actual expenses, total income, and remaining balance.

---

## Previous Versions:
- **v1.5 (Version Code: 6)**: `IncomeControl-v1.5.apk`
- **v1.4 (Version Code: 5)**: `IncomeControl-v1.4.apk`
- **v1.3 (Version Code: 4)**: `IncomeControl-v1.3.apk`
- **v1.2 (Version Code: 3)**: `IncomeControl-v1.2.apk`
- **v1.1 (Version Code: 2)**: `IncomeControl-v1.1.apk`
- **v1.0 (Version Code: 1)**: Initial release
