const {
  initializeTestEnvironment,
  assertFails,
  assertSucceeds,
} = require("@firebase/rules-unit-testing");
const { test, before, after, beforeEach } = require("node:test");
const fs = require("node:fs");

let testEnv;
const PROJECT_ID = process.env.GCP_PROJECT || "demo-no-project";
const ALICE_UID = "alice_123";
const BOB_UID = "bob_456";

const [emulatorHost, emulatorPortStr] = (process.env.FIRESTORE_EMULATOR_HOST || "127.0.0.1:8085").split(":");
const emulatorPort = parseInt(emulatorPortStr, 10);

before(async () => {
  const rules = fs.readFileSync("./firestore.rules", "utf8");
  testEnv = await initializeTestEnvironment({
    projectId: PROJECT_ID,
    firestore: {
      rules,
      host: emulatorHost,
      port: emulatorPort,
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

test("Unauthenticated user: cannot read orders", async () => {
  const unauthDb = testEnv.unauthenticatedContext().firestore();
  await assertFails(unauthDb.collection("orders").get());
});

test("Authenticated user: cannot read another user's order", async () => {
  await testEnv.withSecurityRulesDisabled(async (context) => {
    await context.firestore().collection("orders").doc("order_bob").set({
      orderId: "order_bob",
      userId: BOB_UID,
      customerName: "Bob",
      customerEmail: "bob@example.com",
      serviceName: "Passport Photo",
      serviceCategory: "Photo Services",
      status: "pending",
      price: 50,
      createdAt: new Date(),
      updatedAt: new Date()
    });
  });

  const aliceDb = testEnv.authenticatedContext(ALICE_UID).firestore();
  await assertFails(aliceDb.collection("orders").doc("order_bob").get());
});

test("Authenticated user: can create an order with valid data", async () => {
  const aliceDb = testEnv.authenticatedContext(ALICE_UID).firestore();
  await assertSucceeds(
    aliceDb.collection("orders").doc("order_alice_1").set({
      orderId: "order_alice_1",
      userId: ALICE_UID,
      customerName: "Alice",
      customerEmail: "alice@example.com",
      serviceName: "Passport Photo",
      serviceCategory: "Photo Services",
      status: "pending",
      price: 49,
      notes: "Need white background",
      createdAt: new Date(),
      updatedAt: new Date()
    })
  );
});

test("Authenticated user: can query their own orders with filter", async () => {
  await testEnv.withSecurityRulesDisabled(async (context) => {
    await context.firestore().collection("orders").doc("order_alice_2").set({
      orderId: "order_alice_2",
      userId: ALICE_UID,
      customerName: "Alice",
      customerEmail: "alice@example.com",
      serviceName: "Compress PDF",
      serviceCategory: "PDF Tools",
      status: "pending",
      price: 0,
      createdAt: new Date(),
      updatedAt: new Date()
    });
  });

  const aliceDb = testEnv.authenticatedContext(ALICE_UID).firestore();
  await assertSucceeds(
    aliceDb.collection("orders").where("userId", "==", ALICE_UID).get()
  );
});
