const { onDocumentUpdated } = require('firebase-functions/v2/firestore');
const { onCall } = require('firebase-functions/v2/https');
const admin = require('firebase-admin');

admin.initializeApp();

const db = admin.firestore();
const messaging = admin.messaging();

// HTTP function to enable maintenance mode (admin only)
exports.enableMaintenanceMode = onCall(async (request) => {
  console.log('Enable maintenance mode called with data:', request.data);
  
  const { message } = request.data;
  const maintenanceMessage = message || 'System is under maintenance. Please check back later.';
  
  try {
    // Update metadata
    await db.collection('metadata').doc('routine_version').set({
      version: Date.now(),
      lastUpdated: admin.firestore.FieldValue.serverTimestamp(),
      updatedBy: 'admin',
      updateType: 'maintenance_enabled',
      maintenanceMode: true,
      maintenanceMessage: maintenanceMessage
    }, { merge: true });
    
    // Send push notifications to all users
    await sendMaintenanceNotifications({
      title: 'System Maintenance',
      body: maintenanceMessage,
      type: 'maintenance_enabled'
    });
    
    return { 
      success: true, 
      message: 'Maintenance mode enabled successfully',
      maintenanceMessage: maintenanceMessage
    };
  } catch (error) {
    console.error('Error enabling maintenance mode:', error);
    throw new Error(`Failed to enable maintenance mode: ${error.message}`);
  }
});

// HTTP function to trigger manual update notifications (admin only)
exports.triggerManualUpdate = onCall(async (request) => {
  console.log('Manual update triggered');
  
  try {
    // Update metadata
    await db.collection('metadata').doc('routine_version').set({
      version: Date.now(),
      lastUpdated: admin.firestore.FieldValue.serverTimestamp(),
      updatedBy: 'admin',
      updateType: 'manual_trigger'
    }, { merge: true });
    
    // Send push notifications to all users
    await sendManualUpdateNotifications({
      title: 'Schedule Updated',
      body: 'Your class schedule has been updated. Please check the latest version.',
      type: 'manual_update'
    });
    
    return { success: true, message: 'Manual update notifications sent successfully' };
  } catch (error) {
    console.error('Error triggering manual update:', error);
    throw new Error(`Failed to trigger manual update: ${error.message}`);
  }
});

// HTTP function to get system statistics
exports.getSystemStats = onCall(async (request) => {
  try {
    const [
      usersSnapshot,
      routinesSnapshot,
      fcmTokensSnapshot,
      metadataSnapshot
    ] = await Promise.all([
      db.collection('users').get(),
      db.collection('routines').get(),
      db.collection('fcm_tokens').get(),
      db.collection('metadata').get()
    ]);
    
    // Get FCM token statistics by department
    const fcmStats = {};
    let totalValidTokens = 0;
    
    fcmTokensSnapshot.docs.forEach(doc => {
      const data = doc.data();
      const dept = data.department || 'Unknown';
      if (!fcmStats[dept]) {
        fcmStats[dept] = 0;
      }
      fcmStats[dept]++;
      totalValidTokens++;
    });
    
    return {
      users: {
        total: usersSnapshot.size,
        breakdown: {} // Could add department breakdown
      },
      routines: {
        total: routinesSnapshot.size
      },
      metadata: {
        total: metadataSnapshot.size
      },
      fcmTokens: {
        total: fcmTokensSnapshot.size,
        validTokens: totalValidTokens,
        breakdown: fcmStats
      },
      generatedAt: admin.firestore.FieldValue.serverTimestamp()
    };
  } catch (error) {
    console.error('Error getting system stats:', error);
    throw new Error('Failed to get system statistics');
  }
});

// Helper function to send maintenance notifications
async function sendMaintenanceNotifications(notificationData) {
  try {
    console.log('Starting to send maintenance notifications...');
    
    // Get all FCM tokens
    const tokensSnapshot = await db.collection('fcm_tokens').get();
    
    if (tokensSnapshot.empty) {
      console.log('No FCM tokens found');
      return { success: 0, failed: 0 };
    }
    
    const tokens = tokensSnapshot.docs.map(doc => doc.data().token);
    console.log(`Found ${tokens.length} FCM tokens`);
    
    // Send notifications individually
    let successCount = 0;
    let failureCount = 0;
    const invalidTokens = [];
    
    const message = {
      notification: {
        title: notificationData.title,
        body: notificationData.body
      },
      data: {
        type: notificationData.type,
        timestamp: Date.now().toString()
      }
    };
    
    for (const token of tokens) {
      try {
        await messaging.send({
          ...message,
          token: token
        });
        successCount++;
        console.log(`✓ Sent notification to token: ${token.substring(0, 20)}...`);
      } catch (error) {
        failureCount++;
        console.error(`✗ Failed to send notification to token ${token.substring(0, 20)}...:`, error.message);
        
        // Track invalid tokens for removal
        if (error.code === 'messaging/invalid-registration-token' ||
            error.code === 'messaging/registration-token-not-registered') {
          invalidTokens.push(token);
        }
      }
    }
    
    // Remove invalid tokens
    if (invalidTokens.length > 0) {
      console.log(`Removing ${invalidTokens.length} invalid tokens`);
      await removeInvalidTokens(invalidTokens);
    }
    
    console.log(`Notification results: ${successCount} success, ${failureCount} failed`);
    return { success: successCount, failed: failureCount };
    
  } catch (error) {
    console.error('Error in sendMaintenanceNotifications:', error);
    throw error;
  }
}

// Helper function to send manual update notifications  
async function sendManualUpdateNotifications(notificationData) {
  try {
    console.log('Starting to send manual update notifications...');
    
    // Get all FCM tokens
    const tokensSnapshot = await db.collection('fcm_tokens').get();
    
    if (tokensSnapshot.empty) {
      console.log('No FCM tokens found');
      return { success: 0, failed: 0 };
    }
    
    const tokens = tokensSnapshot.docs.map(doc => doc.data().token);
    console.log(`Found ${tokens.length} FCM tokens`);
    
    // Send notifications individually
    let successCount = 0;
    let failureCount = 0;
    const invalidTokens = [];
    
    const message = {
      notification: {
        title: notificationData.title,
        body: notificationData.body
      },
      data: {
        type: notificationData.type,
        timestamp: Date.now().toString()
      }
    };
    
    for (const token of tokens) {
      try {
        await messaging.send({
          ...message,
          token: token
        });
        successCount++;
        console.log(`✓ Sent notification to token: ${token.substring(0, 20)}...`);
      } catch (error) {
        failureCount++;
        console.error(`✗ Failed to send notification to token ${token.substring(0, 20)}...:`, error.message);
        
        // Track invalid tokens for removal
        if (error.code === 'messaging/invalid-registration-token' ||
            error.code === 'messaging/registration-token-not-registered') {
          invalidTokens.push(token);
        }
      }
    }
    
    // Remove invalid tokens
    if (invalidTokens.length > 0) {
      console.log(`Removing ${invalidTokens.length} invalid tokens`);
      await removeInvalidTokens(invalidTokens);
    }
    
    console.log(`Notification results: ${successCount} success, ${failureCount} failed`);
    return { success: successCount, failed: failureCount };
    
  } catch (error) {
    console.error('Error in sendManualUpdateNotifications:', error);
    throw error;
  }
}

// Helper function to remove invalid FCM tokens
async function removeInvalidTokens(tokens) {
  try {
    const batch = db.batch();
    
    for (const token of tokens) {
      const tokenQuery = await db.collection('fcm_tokens')
        .where('token', '==', token)
        .get();
      
      tokenQuery.docs.forEach(doc => {
        batch.delete(doc.ref);
      });
    }
    
    await batch.commit();
    console.log(`Removed ${tokens.length} invalid FCM tokens`);
  } catch (error) {
    console.error('Error removing invalid tokens:', error);
  }
}
