package com.deepdots.sdk.util

import platform.Foundation.NSUUID

actual fun randomUuid(): String = NSUUID().UUIDString().lowercase()
