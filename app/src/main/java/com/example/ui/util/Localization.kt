package com.example.ui.util

import androidx.compose.runtime.staticCompositionLocalOf

enum class AppLanguage(val code: String, val displayName: String) {
    ENGLISH("en", "English"),
    FRENCH("fr", "Français")
}

enum class ThemeMode {
    DARK,
    LIGHT,
    SYSTEM
}

class AppStrings(val language: AppLanguage) {
    val isFrench: Boolean = language == AppLanguage.FRENCH

    // App & Navigation
    val appName = if (isFrench) "Contrôle des Revenus" else "Income Control"
    val home = if (isFrench) "Accueil" else "Home"
    val budget = if (isFrench) "Budget & Revenu" else "Budget"
    val statistics = if (isFrench) "Statistiques" else "Reports"
    val settings = if (isFrench) "Paramètres" else "Settings"
    val backupRestore = if (isFrench) "Sauvegarde & Restauration" else "Backup & Restore"

    // Spending Summary Card
    val totalSpentThisMonth = if (isFrench) "Dépenses ce mois-ci" else "Total spent this month"
    val totalSpentThisWeek = if (isFrench) "Dépenses cette semaine" else "Total spent this week"
    val totalSpentToday = if (isFrench) "Dépenses aujourd'hui" else "Total spent today"
    val shoppingListPurchasesOnly = if (isFrench) "Achats quotidiens uniquement" else "Shopping-list purchases only"
    val thanLastMonth = if (isFrench) "que le mois dernier" else "than last month"
    val thanLastWeek = if (isFrench) "que la semaine dernière" else "than last week"
    val thanYesterday = if (isFrench) "qu'hier" else "than yesterday"
    val more = if (isFrench) "de plus" else "more"
    val less = if (isFrench) "de moins" else "less"
    val equalToPreviousPeriod = if (isFrench) "• Égal à la période précédente" else "• Equal to previous period"

    // Home Screen Sections
    val separatePurchases = if (isFrench) "Achats Séparés" else "Separate Purchases"
    val items = if (isFrench) "articles" else "items"
    val searchPlaceholder = if (isFrench) "Rechercher eau, café, supermarché..." else "Search water bottle, fast food, supermarket..."
    val noPurchasesYet = if (isFrench) "Aucun achat séparé ce mois-ci" else "No separate purchases yet"
    val noPurchasesSubtext = if (isFrench) "Appuyez sur le bouton + pour ajouter un achat avec date et heure." else "Tap the + button to add a purchase with custom date and time."
    val today = if (isFrench) "Aujourd'hui" else "Today"
    val yesterday = if (isFrench) "Hier" else "Yesterday"

    // Budget Screen
    val totalIncome = if (isFrench) "Revenu total" else "Total income"
    val totalSpent = if (isFrench) "Total dépensé" else "Total spent"
    val remainingBalance = if (isFrench) "Solde restant" else "Remaining balance"
    val monthlyIncomeSection = if (isFrench) "Revenu mensuel" else "Monthly income"
    val addIncome = if (isFrench) "Ajouter un revenu" else "Add income"
    val monthlyIncome = if (isFrench) "Revenu Mensuel" else "Monthly Income"
    val otherIncomes = if (isFrench) "Autres Revenus" else "Other Incomes"
    val recurringMonthly = if (isFrench) "Récurrent mensuel" else "Recurring Monthly"
    val categories = if (isFrench) "Catégories" else "Categories"
    val newCategory = if (isFrench) "Nouvelle Catégorie" else "New Category"
    val addExpense = if (isFrench) "Ajouter une dépense" else "Add expense"
    val edit = if (isFrench) "Modifier" else "Edit"
    val delete = if (isFrench) "Supprimer" else "Delete"
    val cancel = if (isFrench) "Annuler" else "Cancel"
    val save = if (isFrench) "Enregistrer" else "Save"
    val done = if (isFrench) "Terminé" else "Done"
    val close = if (isFrench) "Fermer" else "Close"
    val locked = if (isFrench) "Verrouillé" else "Locked"
    val update = if (isFrench) "Mettre à jour" else "Update"

    // Add Purchase Dialog
    val addPurchaseTitle = if (isFrench) "Ajouter un achat" else "Add purchase"
    val editPurchaseTitle = if (isFrench) "Modifier l'achat" else "Edit purchase"
    val purchaseNameLabel = if (isFrench) "Nom de l'article" else "Item name"
    val noteOptionalLabel = if (isFrench) "Note (optionnelle)" else "Note (optional)"
    val dateLabel = if (isFrench) "Date" else "Date"
    val timeLabel = if (isFrench) "Heure" else "Time"
    val dateTimeLabel = if (isFrench) "Date et heure" else "Date & Time"
    val dateTimeHelper = if (isFrench) "Choisissez la date et l'heure de cet achat" else "Select the date and time of this purchase"
    val lockedHelper = if (isFrench) "Date et heure verrouillées après création (règle des 24h)" else "Date & time locked after creation (24h edit window)"

    // Settings Screen
    val settingsTitle = if (isFrench) "Paramètres" else "Settings"
    val appearanceSection = if (isFrench) "Apparence & Thème" else "Appearance & Theme"
    val themeModeLabel = if (isFrench) "Mode d'affichage" else "Display Mode"
    val themeDark = if (isFrench) "Mode Sombre" else "Dark Mode"
    val themeLight = if (isFrench) "Mode Clair" else "Light Mode"
    val themeSystem = if (isFrench) "Système" else "System"
    val colorPaletteLabel = if (isFrench) "Palette de couleurs" else "Color Palette"
    val colorPaletteDescription = if (isFrench) "Bleu Clair & Blanc (Actif)" else "Light Blue & White (Active)"
    val languageSection = if (isFrench) "Langue de l'application" else "App Language"
    val languageFrench = "Français"
    val languageEnglish = "English"
    val householdSection = if (isFrench) "Profil & Foyer" else "Profile & Household"
    val householdName = if (isFrench) "Nom du profil" else "Profile Name"
    val currencyLabel = if (isFrench) "Devise principale" else "Main Currency"
    val dataSection = if (isFrench) "Données & Sauvegarde" else "Data & Backup"
    val exportBackup = if (isFrench) "Exporter la sauvegarde locale" else "Export Local Backup"
    val restoreBackup = if (isFrench) "Restaurer une sauvegarde locale" else "Restore Local Backup"
    val backupDescription = if (isFrench) "Sauvegardez vos données hors-ligne sur votre appareil (Storage Access Framework)." else "Safely backup all your data offline to your device."
    val appInfoSection = if (isFrench) "À propos" else "About"
    val versionLabel = "Version 1.10"
    val offlineSecure = if (isFrench) "100% Hors-ligne & Sécurisé (SQLite Local)" else "100% Offline & Private (Local SQLite)"
    val createdBy = if (isFrench) "Créé par Frost Dev" else "Created by Frost Dev"
    val developerLabel = if (isFrench) "Développeur" else "Developer"
    val contactDeveloper = if (isFrench) "Contacter le Développeur" else "Contact Developer"
    val developerPhone = "+212693780909"
    val call = if (isFrench) "Appeler" else "Call"
    val whatsapp = "WhatsApp"
    val copy = if (isFrench) "Copier" else "Copy"
    val numberCopied = if (isFrench) "Numéro copié (+212 693-780909)" else "Phone number copied (+212 693-780909)"

    // Prepaid / Coverage Strings
    val coversLabel = if (isFrench) "Couvre" else "Covers"
    val month1 = if (isFrench) "1 mois" else "1 mo"
    val months2 = if (isFrench) "2 mois" else "2 mos"
    val months3 = if (isFrench) "3 mois" else "3 mos"
    val months6 = if (isFrench) "6 mois" else "6 mos"
    val months12 = if (isFrench) "12 mois" else "12 mos"
    fun coversHelper(startMonth: String, endMonth: String): String {
        return if (isFrench) "Couvre $startMonth → $endMonth" else "Covers $startMonth → $endMonth"
    }
    fun alreadyPaidCoveredUntil(endMonth: String): String {
        return if (isFrench) "Déjà payé · couvert jusqu'à $endMonth" else "Already paid · covered until $endMonth"
    }
    fun coveredAddAnywayTitle(categoryName: String, endMonth: String): String {
        return if (isFrench) {
            "$categoryName est déjà payé jusqu'à $endMonth. Ajouter une autre dépense quand même ?"
        } else {
            "$categoryName is already paid until $endMonth. Add another expense anyway?"
        }
    }
    val addAnyway = if (isFrench) "Ajouter quand même" else "Add anyway"
    fun coveredByPaidIn(amountStr: String, currency: String, paidMonth: String): String {
        return if (isFrench) {
            "Couvert par $amountStr $currency payés en $paidMonth"
        } else {
            "Covered by $amountStr $currency paid in $paidMonth"
        }
    }
    fun monthsBadge(count: Int): String {
        return if (isFrench) "$count mois" else "$count months"
    }

    // Category Deletion Confirmation
    val deleteCategoryTitle = if (isFrench) "Supprimer la catégorie ?" else "Delete Category?"
    fun deleteCategoryConfirmation(categoryName: String, count: Int): String {
        return if (isFrench) {
            "Supprimer $categoryName et ses $count achats ? Cette action est irréversible."
        } else {
            "Delete $categoryName and its $count purchases? This cannot be undone."
        }
    }

    // Statistics
    val statsTitle = if (isFrench) "Statistiques & Tendances" else "Reports & Trends"
    val statsSubtitle = if (isFrench) "Analyse financière du mois" else "Monthly financial analysis"
    val categoryBreakdown = if (isFrench) "Répartition par catégorie" else "Category Breakdown"
    val dailyTrend = if (isFrench) "Évolution quotidienne" else "Daily Spending Trend"
    val avgPerDay = if (isFrench) "Moyenne par jour" else "Daily Average"
}

val LocalAppStrings = staticCompositionLocalOf { AppStrings(AppLanguage.ENGLISH) }
