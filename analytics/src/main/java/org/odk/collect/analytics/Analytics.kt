package org.odk.collect.analytics

interface Analytics {
    @Deprecated("")
    fun logEvent(category: String, action: String)

    @Deprecated("")
    fun logEvent(category: String, action: String, label: String)

    fun logEvent(event: String)
    fun logFormEvent(event: String, formIdHash: String)
    fun logServerEvent(event: String, serverHash: String)
    fun logFatal(throwable: Throwable)
    fun logNonFatal(message: String)
    fun setAnalyticsCollectionEnabled(isAnalyticsEnabled: Boolean)
    fun setUserProperty(name: String, value: String)
}
