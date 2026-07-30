package com.deepdots.sdk.tracking

import com.deepdots.sdk.storage.InMemoryStorage
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue

/**
 * Paridad con el TrackingManager Web (src/tracking/tracking-manager.test.ts): identidad
 * persistente + sesión propiedad del backend.
 */
class TrackingManagerParityTest {

    private var now: Long = 1_700_000_000_000
    private var uuidCounter = 0
    private fun uuid(): String = "uuid-${++uuidCounter}"

    private fun mgr(
        storage: InMemoryStorage = InMemoryStorage(),
        clientUserId: String? = null,
        enabled: Boolean = true,
    ) = TrackingManager(
        storage = storage,
        clientUserId = clientUserId,
        now = { now },
        uuid = { uuid() },
        enabled = enabled,
    )

    // ───────── user_id ─────────

    @Test
    fun generates_and_persists_a_user_id_when_the_client_provides_none() {
        val storage = InMemoryStorage()
        val id = mgr(storage).getUserId()

        assertNotNull(id)
        assertEquals(id, storage.getString(StorageKeys.USER_ID))
        assertNotNull(storage.getString(StorageKeys.FIRST_SEEN))
    }

    @Test
    fun reuses_the_persisted_user_id_on_later_sessions() {
        val storage = InMemoryStorage()
        val first = mgr(storage).getUserId()
        val second = mgr(storage).getUserId()

        assertEquals(first, second)
    }

    @Test
    fun is_new_user_only_on_the_session_that_created_the_id() {
        val storage = InMemoryStorage()
        val a = mgr(storage)
        a.getUserId()
        assertTrue(a.isNewUser())

        val b = mgr(storage)
        b.getUserId()
        assertTrue(!b.isNewUser())
    }

    @Test
    fun client_user_id_wins_and_is_not_persisted() {
        val storage = InMemoryStorage()
        val id = mgr(storage, clientUserId = "host-42").getUserId()

        assertEquals("host-42", id)
        assertNull(storage.getString(StorageKeys.USER_ID))
    }

    @Test
    fun blank_client_user_id_falls_back_to_a_generated_one() {
        val storage = InMemoryStorage()
        val id = mgr(storage, clientUserId = "").getUserId()

        assertNotNull(id)
        assertEquals(id, storage.getString(StorageKeys.USER_ID))
    }

    @Test
    fun the_generated_user_id_is_stable_within_the_same_manager() {
        val m = mgr()
        assertEquals(m.getUserId(), m.getUserId())
    }

    // ───────── session_id (lo provee el backend) ─────────

    @Test
    fun session_id_is_null_until_the_backend_sends_one() {
        assertNull(mgr().getSessionId())
    }

    @Test
    fun caches_the_session_id_from_the_backend() {
        val m = mgr()
        m.setSessionId("srv-9")
        assertEquals("srv-9", m.getSessionId())
    }

    @Test
    fun session_id_can_be_cleared_when_the_session_closes() {
        val m = mgr()
        m.setSessionId("srv-9")
        m.setSessionId(null)
        assertNull(m.getSessionId())
    }

    // ───────── kill-switch ─────────

    @Test
    fun disabled_tracking_returns_no_identity_and_touches_no_storage() {
        val storage = InMemoryStorage()
        val m = mgr(storage, enabled = false)

        assertNull(m.getUserId())
        assertNull(m.getSessionId())
        m.setSessionId("srv-9")
        assertNull(m.getSessionId())
        assertNull(storage.getString(StorageKeys.USER_ID))
    }

    @Test
    fun re_enabling_tracking_restores_the_identity() {
        val storage = InMemoryStorage()
        val m = mgr(storage, enabled = false)
        assertNull(m.getUserId())

        m.setTrackingEnabled(true)
        assertNotNull(m.getUserId())
    }

    // ───────── identidad inyectada en el survey (§5) ─────────

    @Test
    fun builds_survey_identity_with_profile_and_metadata() {
        val identity = buildSurveyIdentity("u-1", "s-1")

        assertEquals(listOf(IdentityAnswer("external-user-id", listOf("u-1"))), identity.profile)
        val md = identity.metadata.associate { it.key to it.value[0] }
        assertEquals("s-1", md["session_id"])
        assertEquals("u-1", md["user_id"])
    }

    @Test
    fun omits_missing_pieces_of_the_survey_identity() {
        val identity = buildSurveyIdentity(null, null)
        assertEquals(emptyList(), identity.profile)
        assertEquals(emptyList(), identity.metadata)

        val onlyUser = buildSurveyIdentity("u-1", null)
        assertEquals(listOf("user_id"), onlyUser.metadata.map { it.key })
    }

    @Test
    fun adds_mini_service_to_the_survey_metadata_when_active() {
        val identity = buildSurveyIdentity("u-1", "s-1", miniService = "checkout")
        val md = identity.metadata.associate { it.key to it.value[0] }
        assertEquals("checkout", md["mini_service"])
    }
}
