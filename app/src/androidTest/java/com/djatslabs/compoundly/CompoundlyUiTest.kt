package com.djatslabs.compoundly

import android.content.Context
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertTextContains
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo
import androidx.compose.ui.test.performTextInput
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import org.junit.Rule
import org.junit.Test
import org.junit.rules.ExternalResource
import org.junit.rules.RuleChain
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class CompoundlyUiTest {

    private val clearPrefsRule = object : ExternalResource() {
        override fun before() {
            val context = InstrumentationRegistry.getInstrumentation().targetContext
            context.getSharedPreferences("compoundly_prefs", Context.MODE_PRIVATE)
                .edit()
                .clear()
                .commit()
        }
    }

    private val composeRule = createAndroidComposeRule<MainActivity>()

    @get:Rule
    val ruleChain: RuleChain = RuleChain
        .outerRule(clearPrefsRule)
        .around(composeRule)

    @Test
    fun onboardingNextFlow_reachesMainCalculator() {
        composeRule.onNodeWithTag("onboarding_cta").assertIsDisplayed().performClick()
        composeRule.onNodeWithTag("onboarding_cta").performClick()
        composeRule.onNodeWithTag("onboarding_cta").performClick()

        composeRule.onNodeWithTag("compound_hero_value").assertIsDisplayed()
        composeRule.onNodeWithTag("nav_compound").assertIsDisplayed()
        composeRule.onNodeWithTag("nav_savings").assertIsDisplayed()
    }

    @Test
    fun compoundInterest_realTimeCalculation_currencyToggle_breakdownAndReset() {
        composeRule.onNodeWithTag("onboarding_skip").performClick()

        composeRule.onNodeWithTag("input_initial").performScrollTo().performTextInput("10000000")
        composeRule.onNodeWithTag("input_rate").performScrollTo().performTextInput("8")
        composeRule.onNodeWithTag("input_years").performScrollTo().performTextInput("1")

        composeRule.onNodeWithTag("compound_hero_value").assertTextContains("10.829.995", substring = true)

        composeRule.onNodeWithTag("currency_USD").performClick()
        composeRule.onNodeWithTag("compound_hero_value").assertTextContains("$", substring = true)

        composeRule.onNodeWithTag("currency_IDR").performClick()
        composeRule.onNodeWithTag("breakdown_toggle").performScrollTo().performClick()

        composeRule.onNodeWithTag("reset_button").performClick()
        composeRule.onNodeWithTag("compound_hero_value").assertTextContains("0", substring = true)
    }

    @Test
    fun savingsGoalTab_calculatesAndResetsWithoutClearingCompoundTab() {
        composeRule.onNodeWithTag("onboarding_skip").performClick()

        composeRule.onNodeWithTag("input_initial").performScrollTo().performTextInput("5000000")
        composeRule.onNodeWithTag("nav_savings").performClick()

        composeRule.onNodeWithTag("input_target").performScrollTo().performTextInput("12000000")
        composeRule.onNodeWithTag("input_timeframe").performScrollTo().performTextInput("12")
        composeRule.onNodeWithTag("unit_months").performScrollTo().performClick()

        composeRule.onNodeWithTag("savings_hero_value").assertTextContains("1.000.000", substring = true)

        composeRule.onNodeWithTag("reset_button").performClick()
        composeRule.onNodeWithTag("savings_hero_value").assertTextContains("0", substring = true)

        composeRule.onNodeWithTag("nav_compound").performClick()
        composeRule.onNodeWithTag("compound_hero_value").assertTextContains("5.000.000", substring = true)
    }
}
