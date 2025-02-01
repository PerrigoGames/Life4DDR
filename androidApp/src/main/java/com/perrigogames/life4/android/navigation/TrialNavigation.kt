package com.perrigogames.life4.android.navigation

import androidx.compose.material3.Text
import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import com.perrigogames.life4.android.feature.trial.TrialSession
import com.perrigogames.life4.feature.trials.TrialDestination

fun NavGraphBuilder.trialNavigation(navController: NavController) {
    composable(
        route = TrialDestination.TrialDetails.BASE_ROUTE
    ) { entry ->
        val trialId = entry.arguments?.getString("trialId") ?: "empty"
        TrialSession(trialId = trialId)
    }

    composable(TrialDestination.TrialRecords.route) {
        Text("Trial Records TODO")
    }
}