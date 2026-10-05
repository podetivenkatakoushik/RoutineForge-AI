package com.routineforge.app.security

import com.routineforge.app.workflow.model.RiskLevel

interface SafetyPolicy {
    fun requiresExplicitConfirmation(riskLevel: RiskLevel): Boolean
    fun isActionAllowed(actionName: String, riskLevel: RiskLevel): Boolean
}

class DefaultSafetyPolicy : SafetyPolicy {
    override fun requiresExplicitConfirmation(riskLevel: RiskLevel): Boolean {
        return when (riskLevel) {
            RiskLevel.LOW -> false
            RiskLevel.MEDIUM, RiskLevel.HIGH, RiskLevel.CRITICAL -> true
        }
    }

    override fun isActionAllowed(actionName: String, riskLevel: RiskLevel): Boolean {
        // Critical actions (such as financial transactions) require explicit confirmation
        return true
    }
}
