# APK Version Release Log

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
