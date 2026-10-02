# APK Version Release Log

## Version 1.6 (Version Code: 7)
- **File**: `IncomeControl-v1.6.apk` (and `app-latest.apk`)
- **Date**: October 2, 2026
- **Package**: `com.aistudio.incomecontrol.tkrzq`

### Features & Updates in v1.6 (+0.1):
1. **10-Minute Purchase Edit Window**:
   - Every purchase added can be edited (name, amount, note) for 10 minutes after its creation time.
   - Shows an edit action button while active with remaining time countdown.
   - After 10 minutes, the edit action is locked and shows a small lock icon.
   - Uses stored creation timestamps (`dateTimestamp`) in the database so locks persist across app restarts.
2. **Swipeable Summary Card (3 Paging Views)**:
   - Replaced static card with a horizontal swipeable pager with 3 periods:
     - a) **Total spent this month** (vs last month)
     - b) **Total spent this week** (vs last week)
     - c) **Total spent today** (vs yesterday)
   - Scope: Shopping-list purchases only.
   - Period comparison with dynamic arrow and color:
     - `▲ [amount] MAD more` in red
     - `▼ [amount] MAD less` in emerald green
   - Small animated page indicator dots.
3. **Planned Amount per Category (Tap to Edit)**:
   - Each category displays `spent / planned MAD`.
   - Tapping this text opens a dialog to set the planned budget for the category.
   - Persists in database and updates progress bars, "Planned remaining", and "Actual remaining" totals.
4. **Reorder Categories**:
   - Tapping the up/down arrows icon (`SwapVert`) on a category lets you move that category up or down.
   - Persisted in the database via `displayOrder`.
5. **Rent Price 24-Hour Edit Window**:
   - Tapping Rent allows setting/changing its price.
   - Only permitted for 24 hours after creation/last set.
   - After 24 hours, the price is locked (editing disabled with a small lock icon).
   - Stored in database so locks survive restarts.
6. **Removed Circular-Arrows (Sync) Icon**:
   - Removed the unused circular-arrows icon next to the category name.
7. **Visual & UI Refinements**:
   - Added a 1-second emerald animated loading/splash screen on startup.
   - Reduced empty padding/space in the home screen quick-select chips (Breakfast, Lunch, Dinner, etc.).
   - Centralized all constants in `AppConstants` (`EDIT_WINDOW_MINUTES = 10`, `RENT_EDIT_WINDOW_HOURS = 24`).

---

## Previous Versions:
- **v1.5 (Version Code: 6)**: `IncomeControl-v1.5.apk`
- **v1.4 (Version Code: 5)**: `IncomeControl-v1.4.apk`
- **v1.3 (Version Code: 4)**: `IncomeControl-v1.3.apk`
- **v1.2 (Version Code: 3)**: `IncomeControl-v1.2.apk`
- **v1.1 (Version Code: 2)**: `IncomeControl-v1.1.apk`
- **v1.0 (Version Code: 1)**: Initial release
