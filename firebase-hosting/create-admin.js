const admin = require("firebase-admin");
const serviceAccount = require("./serviceAccountKey.json");

admin.initializeApp({
  credential: admin.credential.cert(serviceAccount)
});

const auth = admin.auth();
const db = admin.firestore();

async function createAdminUser() {
  try {
    console.log("Creating admin user...");
    
    // Create user
    const userRecord = await auth.createUser({
      email: "admin@diucampusschedule.app",
      password: "ovi@maruf.25",
      emailVerified: true,
      disabled: false
    });

    console.log("User created successfully:", userRecord.uid);

    // Set custom claims
    await auth.setCustomUserClaims(userRecord.uid, { admin: true, role: "admin" });
    console.log("Custom claims set successfully");

    // Add to Firestore admin collection
    await db.collection("admins").doc(userRecord.uid).set({
      role: "admin",
      email: "admin@diucampusschedule.app",
      createdAt: admin.firestore.FieldValue.serverTimestamp(),
      permissions: ["upload", "delete", "manage", "view_logs"],
      createdBy: "admin-setup-script"
    });

    console.log("Admin document created in Firestore");

    // Log the creation
    await db.collection("admin_logs").add({
      action: "admin_created_via_script",
      targetEmail: "admin@diucampusschedule.app",
      targetUid: userRecord.uid,
      timestamp: admin.firestore.FieldValue.serverTimestamp(),
      createdBy: "admin-setup-script"
    });

    console.log("✅ Admin user created successfully!");
    console.log("📧 Email: admin@diucampusschedule.app");
    console.log("🔑 Password: ovi@maruf.25");
    console.log("🆔 UID:", userRecord.uid);
    console.log("�� You can now login at: https://diu-campus-schedule-cc857.web.app/login.html");
    
  } catch (error) {
    console.error("Error creating admin user:", error.message);
    
    // If user already exists, just update permissions
    if (error.code === "auth/email-already-exists") {
      console.log("User already exists, updating permissions...");
      try {
        const userRecord = await auth.getUserByEmail("admin@diucampusschedule.app");
        await auth.setCustomUserClaims(userRecord.uid, { admin: true, role: "admin" });
        
        await db.collection("admins").doc(userRecord.uid).set({
          role: "admin",
          email: "admin@diucampusschedule.app",
          updatedAt: admin.firestore.FieldValue.serverTimestamp(),
          permissions: ["upload", "delete", "manage", "view_logs"],
          updatedBy: "admin-setup-script"
        }, { merge: true });

        console.log("✅ Admin permissions updated successfully!");
        console.log("📧 Email: admin@diucampusschedule.app");
        console.log("🔑 Password: ovi@maruf.25");
        console.log("🆔 UID:", userRecord.uid);
      } catch (updateError) {
        console.error("Error updating admin permissions:", updateError.message);
      }
    }
  }
}

createAdminUser().then(() => {
  console.log("Script completed");
  process.exit(0);
}).catch((error) => {
  console.error("Script failed:", error);
  process.exit(1);
});
