package com.deepdots.sdk.models

/**
 * A session of the analytics channel as the Deepdots API sees it. Every session becomes ONE
 * feedback, and [sessionId] is what the API stores as `sdkSessionId` on it: the value to look
 * that feedback up with (`GET /feedbacks?filter={"where":{"sdkSessionId":"<sessionId>"}}`).
 * A session completed twice yields two feedbacks with the same id: take the latest `createdAt`.
 *
 * Espejo del tipo `FeedbackSession` del SDK Web (`status: 'open' | 'closed'`).
 */
data class FeedbackSession(
    val sessionId: String,
    val status: FeedbackSessionStatus,
)

enum class FeedbackSessionStatus {
    /** The API acknowledged the session's first batch; its events keep accumulating. */
    Open,

    /**
     * The API acknowledged the session's closing batch (`completed: true`); the feedback is
     * created from it within seconds.
     */
    Closed,
}
