package com.deepdots.sdk

/**
 * Simple runtime holder for SDK-wide config needed by UI/HTML builders.
 */
object SdkRuntime {
    var publicKey: String? = null
    var env: String = "prod" // magicfeedback env: 'dev' | 'prod'
    var userId: String? = null // optional user identifier from init metadata
    var metadata: Map<String, Any>? = null // raw metadata from InitOptions
}
