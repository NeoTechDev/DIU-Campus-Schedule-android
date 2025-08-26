const { initializeApp, applicationDefault } = require('firebase-admin/app');
const { getAuth } = require('firebase-admin/auth');
const { getFirestore } = require('firebase-admin/firestore');

// Initialize Firebase Admin with Application Default Credentials
const app = initializeApp({
  credential: applicationDefault(),
  projectId: 'diu-campus-schedule-cc857'
});

const auth = getAuth(app);
const db = getFirestore(app);

async function createAdminUser() {
  try {
    console.log('🔄 Creating admin user...');
    
    const email = 'admin@diucampusschedule.app';
    const password = 'ovi@maruf.25';
    
    // Try to create the user
    let userRecord;
    try {
      userRecord = await auth.createUser({
        email: email,
        password: password,
        emailVerified: true,
        disabled: false
      });
      console.log('✅ User created with UID:', userRecord.uid);
    } catch (error) {
      if (error.code === 'auth/email-already-exists') {
        console.log('⚠️ User already exists, getting user record...');
        userRecord = await auth.getUserByEmail(email);
        console.log('📋 Found existing user with UID:', userRecord.uid);
      } else {
        throw error;
      }
    }

    // Set custom claims for admin access
    await auth.setCustomUserClaims(userRecord.uid, {
      admin: true,
      role: 'admin'
    });
    console.log('🔑 Admin claims set successfully');

    // Add to Firestore admin collection
    await db.collection('admins').doc(userRecord.uid).set({
      role: 'admin',
      email: email,
      createdAt: new Date(),
      permissions: ['upload', 'delete', 'manage', 'view_logs'],
      createdBy: 'admin-setup-script'
    });
    console.log('📄 Admin document created in Firestore');

    // Log the action
    await db.collection('admin_logs').add({
      action: 'admin_created_via_script',
      targetEmail: email,
      targetUid: userRecord.uid,
      timestamp: new Date(),
      createdBy: 'setup-script'
    });

    console.log('🎉 SUCCESS! Admin user is ready:');
    console.log('📧 Email: admin@diucampusschedule.app');
    console.log('🔑 Password: ovi@maruf.25');
    console.log('🆔 UID:', userRecord.uid);
    console.log('🔗 Login at: https://diu-campus-schedule-cc857.web.app/login.html');
    
  } catch (error) {
    console.error('❌ Error:', error.message);
    console.error('Full error:', error);
  }
}

createAdminUser()
  .then(() => process.exit(0))
  .catch((error) => {
    console.error('Script failed:', error);
    process.exit(1);
  });
