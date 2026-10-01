# APK Version Release Log

## Version 1.4 (Version Code: 5)
- **File**: `IncomeControl-v1.4.apk` (and `app-latest.apk`)
- **Date**: October 1, 2026
- **Package**: `com.aistudio.incomecontrol.tkrzq`

### Features & Updates in v1.4 (+0.1):
1. **Delete Category Support with Confirmation**:
   - Each category card in the Wallet / Categories view now has a dedicated Delete button (`Icons.Rounded.DeleteOutline`).
   - Tapping it opens a confirmation dialog: *"Delete Category? Are you sure you want to delete '[Name]'? Its planned budget and any associated expenses will be removed."*
   - Safely removes mistakenly added or unwanted categories.
2. **Delete Expense Support**:
   - Home Screen separate purchases now have a delete icon button with a confirmation dialog: *"Delete '[Title]' (-[Amount] MAD)?"*
   - Category expenses can also be deleted from the category History dialog.
   - Incomes can be deleted directly from the monthly income list.
3. **Decoupled Home Screen from Alimentation**:
   - Items added via the `+` FAB on the Home Screen (water bottles, fast food, snacks, groceries) are now saved as **Separate Purchases** (`categoryId = 0`), **never** altering or tagging as "Alimentation".
   - Automatically cleaned up any existing shopping items previously assigned to Alimentation.
4. **Quick Suggestion Chips in Add Expense**:
   - Added instant one-tap chips for "Water bottle", "Fast food", "Coffee", "Supermarket" in the Home Screen add expense dialog.
5. **Enhanced Visual Polish**:
   - Sleek dark emerald vertical gradient cards (`#142720` ➔ `#0F1E19`) with subtle illuminated borders.
   - Refined typography, smooth pill buttons, and crisp Material 3 touch targets.

---

## Previous Versions:
- **v1.3 (Version Code: 4)**: `IncomeControl-v1.3.apk`
- **v1.2 (Version Code: 3)**: `IncomeControl-v1.2.apk`
- **v1.1 (Version Code: 2)**: `IncomeControl-v1.1.apk`
- **v1.0 (Version Code: 1)**: Initial release
