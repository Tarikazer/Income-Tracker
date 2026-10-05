# APK Version Release Log

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
