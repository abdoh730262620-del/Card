const {
  initializeTestEnvironment,
  assertFails,
  assertSucceeds,
} = require("@firebase/rules-unit-testing");
const { test, before, after, beforeEach } = require("node:test");
const fs = require("node:fs");

const PROJECT_ID = "demo-no-project";
const ALICE_UID = "alice_123";
const BOB_UID = "bob_456";

let testEnv;

before(async () => {
  const host = "127.0.0.1";
  const port = 8085;
  const rules = fs.readFileSync("./firestore.rules", "utf8");

  testEnv = await initializeTestEnvironment({
    projectId: PROJECT_ID,
    firestore: {
      rules,
      host,
      port,
    },
  });
});

after(async () => {
  if (testEnv) {
    await testEnv.cleanup();
  }
});

beforeEach(async () => {
  if (testEnv) {
    await testEnv.clearFirestore();
  }
});

test("Unauthenticated user: cannot read or write any user profile or subcollections", async () => {
  const unauthDb = testEnv.unauthenticatedContext().firestore();
  await assertFails(unauthDb.collection("users").doc(ALICE_UID).get());
  await assertFails(unauthDb.collection("users").doc(ALICE_UID).collection("distributors").get());
  await assertFails(unauthDb.collection("users").doc(ALICE_UID).set({ name: "Alice" }));
});

test("Authenticated user: can read and write their own data", async () => {
  const aliceDb = testEnv.authenticatedContext(ALICE_UID).firestore();
  await assertSucceeds(
    aliceDb.collection("users").doc(ALICE_UID).set({
      userId: ALICE_UID,
      displayName: "Alice",
      createdAt: Date.now()
    })
  );
  await assertSucceeds(
    aliceDb.collection("users").doc(ALICE_UID).collection("distributors").doc("dist_1").set({
      userId: ALICE_UID,
      name: "Distributor 1",
      createdAt: Date.now()
    })
  );
  await assertSucceeds(aliceDb.collection("users").doc(ALICE_UID).collection("distributors").get());
});

test("Authenticated user: cannot access another user's data", async () => {
  await testEnv.withSecurityRulesDisabled(async (context) => {
    await context.firestore().collection("users").doc(ALICE_UID).set({
      userId: ALICE_UID,
      displayName: "Alice",
      createdAt: Date.now()
    });
  });

  const bobDb = testEnv.authenticatedContext(BOB_UID).firestore();
  await assertFails(bobDb.collection("users").doc(ALICE_UID).get());
  await assertFails(
    bobDb.collection("users").doc(ALICE_UID).collection("distributors").get()
  );
  await assertFails(
    bobDb.collection("users").doc(ALICE_UID).set({ displayName: "Hacked" })
  );
});
