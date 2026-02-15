package com.example.helloandroid.ui.theme

import androidx.compose.runtime.Immutable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color

// ── Legacy Material defaults ──────────────────────────────
val Purple80 = Color(0xFFD0BCFF)
val PurpleGrey80 = Color(0xFFCCC2DC)
val Pink80 = Color(0xFFEFB8C8)

val Purple40 = Color(0xFF6650a4)
val PurpleGrey40 = Color(0xFF625b71)
val Pink40 = Color(0xFF7D5260)

// ══════════════════════════════════════════════════════════
// FINANCE TRACKER DESIGN TOKENS
// ══════════════════════════════════════════════════════════

/**
 * Holds every custom color token used across the app.
 * Two instances exist — [LightFinanceColors] and [DarkFinanceColors].
 * Screens access the current set via [FinanceTheme.colors].
 */
@Immutable
data class FinanceColors(
    // ── Backgrounds ─────────────────────────────────────
    val screenBg: Color,
    val cardBg: Color,

    // ── Primary text & accent ───────────────────────────
    val darkNavy: Color,
    val titleLight: Color,

    // ── Tab colors ──────────────────────────────────────
    val tabActive: Color,
    val tabInactive: Color,

    // ── Date navigation ─────────────────────────────────
    val dateLabelText: Color,
    val dateSublabel: Color,

    // ── Summary card ────────────────────────────────────
    val summaryLabel: Color,
    val summaryValue: Color,

    // ── Transaction colors ──────────────────────────────
    val incomeGreen: Color,
    val expenseRed: Color,
    val incomeIconBg: Color,
    val expenseIconBg: Color,

    // ── Neutral / utility ───────────────────────────────
    val subtitleGray: Color,
    val sectionLabelColor: Color,
    val dividerColor: Color,

    // ── Empty state ─────────────────────────────────────
    val emptyStateIcon: Color,
    val emptyStateText: Color,

    // ── Bottom dock ─────────────────────────────────────
    val dockBgSelected: Color,
    val dockTextSelected: Color,
    val dockTextInactive: Color,

    // ── Button content ──────────────────────────────────
    val onPrimaryButton: Color,
)

// ── Light palette (current design) ─────────────────────────
val LightFinanceColors = FinanceColors(
    screenBg         = Color(0xFFF5F6FA),
    cardBg           = Color(0xFFFFFFFF),
    darkNavy         = Color(0xFF1C2536),
    titleLight       = Color(0xFF90A4AE),
    tabActive        = Color(0xFF1A1A2E),
    tabInactive      = Color(0xFF90A4AE),
    dateLabelText    = Color(0xFF37474F),
    dateSublabel     = Color(0xFF78909C),
    summaryLabel     = Color(0xFF90A4AE),
    summaryValue     = Color(0xFF263238),
    incomeGreen      = Color(0xFF2E7D32),
    expenseRed       = Color(0xFFE53935),
    incomeIconBg     = Color(0xFFE8F5E9),
    expenseIconBg    = Color(0xFFFFEBEE),
    subtitleGray     = Color(0xFF78909C),
    sectionLabelColor = Color(0xFF90A4AE),
    dividerColor     = Color(0xFFEEEEEE),
    emptyStateIcon   = Color(0xFFC5CAD0),
    emptyStateText   = Color(0xFF78909C),
    dockBgSelected   = Color(0xFFE8EAF6),
    dockTextSelected = Color(0xFF3F51B5),
    dockTextInactive = Color(0xFF90A4AE),
    onPrimaryButton  = Color(0xFFFFFFFF),
)

// ── Dark palette (premium deep blue-gray) ──────────────────
val DarkFinanceColors = FinanceColors(
    screenBg         = Color(0xFF121218),
    cardBg           = Color(0xFF1E1E2A),
    darkNavy         = Color(0xFFE8EAED),
    titleLight       = Color(0xFF6B7280),
    tabActive        = Color(0xFFE8EAED),
    tabInactive      = Color(0xFF6B7280),
    dateLabelText    = Color(0xFFD1D5DB),
    dateSublabel     = Color(0xFF9CA3AF),
    summaryLabel     = Color(0xFF6B7280),
    summaryValue     = Color(0xFFF3F4F6),
    incomeGreen      = Color(0xFF4CAF50),
    expenseRed       = Color(0xFFEF5350),
    incomeIconBg     = Color(0xFF1B3A1E),
    expenseIconBg    = Color(0xFF3A1B1E),
    subtitleGray     = Color(0xFF9CA3AF),
    sectionLabelColor = Color(0xFF6B7280),
    dividerColor     = Color(0xFF2D2D3A),
    emptyStateIcon   = Color(0xFF4B5563),
    emptyStateText   = Color(0xFF9CA3AF),
    dockBgSelected   = Color(0xFF2A2A3D),
    dockTextSelected = Color(0xFF818CF8),
    dockTextInactive = Color(0xFF6B7280),
    onPrimaryButton  = Color(0xFF121218),
)

/**
 * CompositionLocal that provides [FinanceColors] to the Compose tree.
 * Falls back to [LightFinanceColors] if no provider is supplied.
 */
val LocalFinanceColors = staticCompositionLocalOf { LightFinanceColors }

// ── Backward-compat top-level vals (used by legacy code) ──
// These still resolve to Light values for any code not yet
// migrated to FinanceTheme.colors.*
val ScreenBg          = LightFinanceColors.screenBg
val CardBg            = LightFinanceColors.cardBg
val DarkNavy          = LightFinanceColors.darkNavy
val TitleLight        = LightFinanceColors.titleLight
val TabActive         = LightFinanceColors.tabActive
val TabInactive       = LightFinanceColors.tabInactive
val DateLabelText     = LightFinanceColors.dateLabelText
val DateSublabel      = LightFinanceColors.dateSublabel
val SummaryLabel      = LightFinanceColors.summaryLabel
val SummaryValue      = LightFinanceColors.summaryValue
val IncomeGreen       = LightFinanceColors.incomeGreen
val ExpenseRed        = LightFinanceColors.expenseRed
val IncomeIconBg      = LightFinanceColors.incomeIconBg
val ExpenseIconBg     = LightFinanceColors.expenseIconBg
val SubtitleGray      = LightFinanceColors.subtitleGray
val SectionLabelColor = LightFinanceColors.sectionLabelColor
val DividerColor      = LightFinanceColors.dividerColor
val EmptyStateIcon    = LightFinanceColors.emptyStateIcon
val EmptyStateText    = LightFinanceColors.emptyStateText
val DockBgSelected    = LightFinanceColors.dockBgSelected
val DockTextSelected  = LightFinanceColors.dockTextSelected
val DockTextInactive  = LightFinanceColors.dockTextInactive