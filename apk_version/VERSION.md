# APK Version Release Log

## Version 1.5 (Version Code: 6)
- **File**: `IncomeControl-v1.5.apk` (and `app-latest.apk`)
- **Date**: October 1, 2026
- **Package**: `com.aistudio.incomecontrol.tkrzq`

### Features & Updates in v1.5 (+0.1):
1. **Added More Expense Options in Home Screen**:
   - Expanded quick item suggestion chips in the "+ Add expense" dialog to include:
     - ☕ **Breakfast**
     - 🥪 **Lunch**
     - 🍲 **Dinner**
     - 🥤 **Drinks**
     - 🛒 **Supermarket**
     - 💧 **Water bottle**
     - ☕ **Coffee**
     - 🍔 **Fast food**
     - 🧺 **Groceries**
     - 🍿 **Snacks**
2. **Text Wrapping for Supermarket & Chips**:
   - Replaced single row with flexible, multi-line `FlowRow` layout with responsive wrapping.
   - "Supermarket", "Breakfast", "Dinner", and all other chips wrap cleanly across lines without truncation or edge clipping.
3. **Fixed Error Codes & Parsing**:
   - Added friendly input sanitization (handles decimal comma `,` or dot `.` seamlessly without crashing or throwing parsing errors).
   - Clear validation feedback ("Please enter or pick what you bought", "Please enter a valid amount").
   - Fixed all compiler warnings, receiver type mismatches, and deprecations across the build.
4. **Clean Decoupling**:
   - Items added from Home Screen remain strictly separate purchases and never get assigned to Alimentation.

---

## Previous Versions:
- **v1.4 (Version Code: 5)**: `IncomeControl-v1.4.apk`
- **v1.3 (Version Code: 4)**: `IncomeControl-v1.3.apk`
- **v1.2 (Version Code: 3)**: `IncomeControl-v1.2.apk`
- **v1.1 (Version Code: 2)**: `IncomeControl-v1.1.apk`
- **v1.0 (Version Code: 1)**: Initial release
