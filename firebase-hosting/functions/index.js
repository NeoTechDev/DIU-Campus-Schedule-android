const functions = require('firebase-functions');
const admin = require('firebase-admin');

admin.initializeApp();

const db = admin.firestore();
const messaging = admin.messaging();

// HTTP function to enable maintenance mode (admin only)
exports.enableMaintenanceMode = functions.https.onCall(async (data, context) => {
  console.log('Enable maintenance mode called with data:', data);
  
  const { message } = data;
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
    throw new functions.https.HttpsError('internal', `Failed to enable maintenance mode: ${error.message}`);
  }
});

// HTTP function to trigger manual update notifications (admin only)
exports.triggerManualUpdate = functions.https.onCall(async (data, context) => {
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
    throw new functions.https.HttpsError('internal', `Failed to trigger manual update: ${error.message}`);
  }
});

// HTTP function to get system statistics
exports.getSystemStats = functions.https.onCall(async (data, context) => {
  try {
    console.log('Getting system stats...');
    
    const [
      usersSnapshot,
      routinesSnapshot,
      fcmTokensSnapshot,
      metadataSnapshot,
      analyticsSnapshot,
      feedbackSnapshot
    ] = await Promise.all([
      db.collection('users').get().catch(e => { console.log('Users collection error:', e.message); return null; }),
      db.collection('routines').get().catch(e => { console.log('Routines collection error:', e.message); return null; }),
      db.collection('fcm_tokens').get().catch(e => { console.log('FCM tokens collection error:', e.message); return null; }),
      db.collection('metadata').get().catch(e => { console.log('Metadata collection error:', e.message); return null; }),
      db.collection('analytics').get().catch(e => { console.log('Analytics collection error:', e.message); return null; }),
      db.collection('feedback').get().catch(e => { console.log('Feedback collection error:', e.message); return null; })
    ]);
    
    console.log('Collections fetched:', {
      users: usersSnapshot ? usersSnapshot.size : 'null',
      routines: routinesSnapshot ? routinesSnapshot.size : 'null',
      fcmTokens: fcmTokensSnapshot ? fcmTokensSnapshot.size : 'null',
      metadata: metadataSnapshot ? metadataSnapshot.size : 'null',
      analytics: analyticsSnapshot ? analyticsSnapshot.size : 'null',
      feedback: feedbackSnapshot ? feedbackSnapshot.size : 'null'
    });
    
    // Get FCM token statistics by department
    const fcmStats = {};
    let totalValidTokens = 0;
    
    if (fcmTokensSnapshot && fcmTokensSnapshot.docs) {
      fcmTokensSnapshot.docs.forEach(doc => {
        const data = doc.data();
        const dept = data.department || 'Unknown';
        if (!fcmStats[dept]) {
          fcmStats[dept] = 0;
        }
        fcmStats[dept]++;
        totalValidTokens++;
      });
    }
    
    return {
      users: {
        total: usersSnapshot ? usersSnapshot.size : 0,
        breakdown: {} // Could add department breakdown
      },
      routines: {
        total: routinesSnapshot ? routinesSnapshot.size : 0
      },
      metadata: {
        total: metadataSnapshot ? metadataSnapshot.size : 0
      },
      analytics: {
        total: analyticsSnapshot ? analyticsSnapshot.size : 0
      },
      feedback: {
        total: feedbackSnapshot ? feedbackSnapshot.size : 0
      },
      fcmTokens: {
        total: fcmTokensSnapshot ? fcmTokensSnapshot.size : 0,
        validTokens: totalValidTokens,
        breakdown: fcmStats
      },
      generatedAt: admin.firestore.FieldValue.serverTimestamp()
    };
  } catch (error) {
    console.error('Error getting system stats:', error);
    throw new functions.https.HttpsError('internal', 'Failed to get system statistics');
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
      },
      android: {
        notification: {
          channelId: 'routine_updates',
          priority: 'high',
          defaultSound: true,
          defaultVibrateTimings: true
        }
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
      },
      android: {
        notification: {
          channelId: 'routine_updates',
          priority: 'high',
          defaultSound: true,
          defaultVibrateTimings: true
        }
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

// HTTP function to disable maintenance mode
exports.disableMaintenanceMode = functions.https.onCall(async (data, context) => {
  console.log('Disable maintenance mode called');
  
  try {
    // Update metadata to disable maintenance mode
    await db.collection('metadata').doc('routine_version').set({
      version: Date.now(),
      lastUpdated: admin.firestore.FieldValue.serverTimestamp(),
      updatedBy: 'admin',
      updateType: 'maintenance_disabled',
      maintenanceMode: false,
      maintenanceMessage: null
    }, { merge: true });
    
    // Send push notifications to all users
    await sendMaintenanceNotifications({
      title: 'System Back Online',
      body: 'System maintenance has been completed. You can now use the app normally.',
      type: 'maintenance_disabled'
    });
    
    return { 
      success: true, 
      message: 'Maintenance mode disabled successfully'
    };
  } catch (error) {
    console.error('Error disabling maintenance mode:', error);
    throw new functions.https.HttpsError('internal', `Failed to disable maintenance mode: ${error.message}`);
  }
});

// HTTP function to clear all maintenance data
exports.clearMaintenanceData = functions.https.onCall(async (data, context) => {
  console.log('Clear maintenance data called');
  
  try {
    // Reset metadata document
    await db.collection('metadata').doc('routine_version').set({
      version: Date.now(),
      lastUpdated: admin.firestore.FieldValue.serverTimestamp(),
      updatedBy: 'admin',
      updateType: 'data_cleared',
      maintenanceMode: false,
      maintenanceMessage: null
    }, { merge: true });
    
    return { 
      success: true, 
      message: 'Maintenance data cleared successfully'
    };
  } catch (error) {
    console.error('Error clearing maintenance data:', error);
    throw new functions.https.HttpsError('internal', `Failed to clear maintenance data: ${error.message}`);
  }
});

// HTTP function to set semester break
exports.setSemesterBreak = functions.https.onCall(async (data, context) => {
  console.log('Set semester break called with data:', data);
  
  const { message } = data;
  const breakMessage = message || 'Semester break has started. Classes will resume soon.';
  
  try {
    // Update metadata
    await db.collection('metadata').doc('routine_version').set({
      version: Date.now(),
      lastUpdated: admin.firestore.FieldValue.serverTimestamp(),
      updatedBy: 'admin',
      updateType: 'semester_break',
      semesterBreak: true,
      breakMessage: breakMessage
    }, { merge: true });
    
    // Send push notifications to all users
    await sendMaintenanceNotifications({
      title: 'Semester Break',
      body: breakMessage,
      type: 'semester_break'
    });
    
    return { 
      success: true, 
      message: 'Semester break notification sent successfully',
      breakMessage: breakMessage
    };
  } catch (error) {
    console.error('Error setting semester break:', error);
    throw new functions.https.HttpsError('internal', `Failed to set semester break: ${error.message}`);
  }
});
