package com.example

import com.example.base.FirestoreEmulatorTestBase
import com.example.data.firebase.FirebaseDistributor
import com.example.data.firebase.FirestoreSyncRepository
import com.example.data.model.Distributor
import com.google.firebase.firestore.FirebaseFirestoreException
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Assert.fail
import org.junit.Test

class FirestoreSyncRepositoryRuleTest : FirestoreEmulatorTestBase() {

  @Test
  fun testSyncDistributorAndObserveAsOwner() = runBlocking {
    val uid = signInTestUser("owner@example.com")
    val repository = FirestoreSyncRepository(firestore)

    val distributor = Distributor(
      id = 101L,
      name = "موزع الأمل",
      phone = "777123456",
      currentBalance = 1500.0,
      creditLimit = 30000.0
    )

    repository.syncDistributor(uid, distributor)

    val list = repository.observeDistributors(uid).first()
    assertTrue("Distributor list should contain synced record", list.isNotEmpty())
    val synced = list.find { it.name == "موزع الأمل" }
    assertNotNull(synced)
    assertEquals(1500.0, synced?.currentBalance ?: 0.0, 0.001)
  }

  @Test
  fun testUnauthenticatedUserCannotWrite() = runBlocking {
    auth.signOut()
    val repository = FirestoreSyncRepository(firestore)

    val distributor = Distributor(
      id = 102L,
      name = "موزع تجربة",
      phone = "770000000"
    )

    try {
      repository.syncDistributor("alice_fake", distributor)
      fail("Expected FirebaseFirestoreException for unauthenticated write")
    } catch (e: FirebaseFirestoreException) {
      assertEquals(FirebaseFirestoreException.Code.PERMISSION_DENIED, e.code)
    }
  }

  @Test
  fun testUserCannotAccessOtherUserData() = runBlocking {
    val aliceUid = signInTestUser("alice@example.com")
    val repository = FirestoreSyncRepository(firestore)

    val distAlice = Distributor(
      id = 201L,
      name = "موزع أليس",
      phone = "771111111"
    )
    repository.syncDistributor(aliceUid, distAlice)

    // Sign in as Bob
    val bobUid = signInTestUser("bob@example.com")
    try {
      // Bob tries to read Alice's subcollection
      val aliceDocs = repository.observeDistributors(aliceUid).first()
      fail("Expected PERMISSION_DENIED when Bob attempts to read Alice's documents")
    } catch (e: Exception) {
      val firestoreEx = (e as? FirebaseFirestoreException)
        ?: (e.cause as? FirebaseFirestoreException)
        ?: (e.cause?.cause as? FirebaseFirestoreException)
      assertNotNull("Expected FirebaseFirestoreException in cause chain", firestoreEx)
      assertEquals(FirebaseFirestoreException.Code.PERMISSION_DENIED, firestoreEx?.code)
    }
  }
}
