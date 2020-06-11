package de.rki.coronawarnapp.exception.reporting

enum class ErrorCodes(val code: Int) {
    // TECHNICAL
    WRONG_RECEIVER_PROBLEM(100),
    TRANSACTION_PROBLEM(500),
    ROLLBACK_PROBLEM(510),
    APPLICATION_CONFIGURATION_CORRUPT(1000),
    APPLICATION_CONFIGURATION_INVALID(1001),
    CWA_SECURITY_PROBLEM(2000),
    CWA_WEB_SECURITY_PROBLEM(2001),
    DIAGNOSIS_KEY_SERVICE_PROBLEM(3000),
    RISK_LEVEL_CALCULATION_PROBLEM(3500),
    CWA_WEB_REQUEST_PROBLEM(4000),
    EN_PERMISSION_PROBLEM(7000),
    FORMATTER_PROBLEM(8000),
    REPORTED_EXCEPTION_PROBLEM(9001),
    REPORTED_IO_EXCEPTION_PROBLEM(9101),
    REPORTED_EXCEPTION_UNKNOWN_PROBLEM(9002),

    // NONTECHNICAL
    NO_NETWORK_CONNECTIVITY(1),
    EXTERNAL_NAVIGATION(10),
}
