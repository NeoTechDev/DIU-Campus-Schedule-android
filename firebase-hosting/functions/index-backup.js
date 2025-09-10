const { onDocumentUpdated } = require('firebase-functions/v2/firestore');
const { onCall } = require('firebase-functions/v2/https');
const admin = require('firebase-admin');

admin.initializeApp();

const db = admin.firestore();
const messaging = admin.messaging();

// Trigger when a routine document is updated
exports.onRoutineUpdate = onDocumentUpdated('routines/{routineId}', async (event) => {
  const routineId = event.params.routineId;
  const newData = event.data.after.data();
  const oldData = event.data.before.data();
  
  // Check if version changed (indicating a routine update)
  if (newData.version !== oldData.version) {
    console.log(`Routine updated for department: ${newData.department}, new version: ${newData.version}`);
    
    try {
      // Send push notifications to all users in this department
      await sendRoutineUpdateNotifications(newData.department, {
        title: 'Schedule Updated',
        body: `Your ${newData.department} class schedule has been updated`,
        department: newData.department,
        version: newData.version
      });
      
      // Log the update
      await logSystemEvent('routine_update', {
        routineId,
        department: newData.department,
        oldVersion: oldData.version,
        newVersion: newData.version,
        timestamp: admin.firestore.FieldValue.serverTimestamp()
      });
      
      return { success: true };
    } catch (error) {
      console.error('Error processing routine update:', error);
      return { success: false, error: error.message };
    }
  }
  
  return { success: true, message: 'No version change detected' };
});

// HTTP function to manually trigger routine update notifications
exports.triggerRoutineUpdate = onCall(async (request) => {
  // Check if user is admin or developer
  if (!request.auth || (!request.auth.token.admin && !request.auth.token.developer)) {
    throw new Error('Permission denied: Only admins can trigger routine updates');
  }
  
  const { department, title, message } = request.data;
  
  if (!department) {
    throw new Error('Invalid argument: Department is required');
  }
  
  try {
    await sendRoutineUpdateNotifications(department, {
      title: title || 'Schedule Updated',
      body: message || `Your ${department} class schedule has been updated`,
      department
    });
    
    return { success: true, message: 'Notifications sent successfully' };
  } catch (error) {
    console.error('Error sending notifications:', error);
    throw new Error('Failed to send notifications');
  }
});

// Trigger when metadata/routine_version document is updated
exports.onMetadataUpdate = onDocumentUpdated('metadata/routine_version', async (event) => {
  const newData = event.data.after.data();
  const oldData = event.data.before.data();
    
    // Check if version changed (indicating a routine update)
    if (newData.version !== oldData.version) {
      console.log(`Metadata version updated: ${oldData.version} -> ${newData.version}`);
      console.log(`Update type: ${newData.updateType}`);
      
      try {
        // Determine notification content based on update type
        let title = 'Schedule Updated';
        let body = 'Your class schedule has been updated';
        
        if (newData.updateType === 'routine_deleted') {
          title = 'Schedule Update';
          body = newData.maintenanceMessage || 'Your schedule is being updated. New schedule will be available soon.';
        } else if (newData.updateType === 'all_routines_deleted') {
          title = 'System Maintenance';
          body = newData.maintenanceMessage || 'System maintenance in progress. New routines will be uploaded soon.';
        } else if (newData.updateType === 'maintenance_enabled') {
          title = 'System Maintenance';
          body = newData.maintenanceMessage || 'System is under maintenance. Please check back later.';
        } else if (newData.updateType === 'semester_break') {
          title = 'Semester Break';
          body = newData.maintenanceMessage || 'Semester break is in progress. New semester routine will be available soon.';
        } else if (newData.updateType === 'routine_uploaded') {
          title = 'New Schedule Available';
          body = 'Your class schedule has been updated with new information.';
        } else if (newData.updateType === 'manual_trigger') {
          title = 'Schedule Refresh';
          body = 'Please refresh your app to see the latest schedule updates.';
        }
        
        // Send notifications to all departments (since metadata affects all)
        const routinesSnapshot = await db.collection('routines').get();
        const departments = new Set();
        
        routinesSnapshot.docs.forEach(doc => {
          const data = doc.data();
          if (data.department) {
            departments.add(data.department);
          }
        });
        
        // If no routines found, send to common departments
        if (departments.size === 0) {
          departments.add('Software Engineering');
          departments.add('Computer Science');
          departments.add('Electrical Engineering');
        }
        
        // Send notifications to all departments
        const promises = Array.from(departments).map(department => 
          sendRoutineUpdateNotifications(department, {
            title,
            body,
            department,
            version: newData.version,
            updateType: newData.updateType
          })
        );
        
        await Promise.all(promises);
        
        // Log the update
        await logSystemEvent('metadata_update_notification', {
          updateType: newData.updateType,
          oldVersion: oldData.version,
          newVersion: newData.version,
          departmentsNotified: Array.from(departments),
          timestamp: admin.firestore.FieldValue.serverTimestamp()
        });
        
        console.log(`Notifications sent to ${departments.size} departments for metadata update`);
        return { success: true };
      } catch (error) {
        console.error('Error processing metadata update:', error);
        throw new Error('Failed to process metadata update');
      }
    }
    
    return { success: true, message: 'No version change detected' };
  });

/*
// Commented out functions that need v2 migration
exports.uploadRoutineData = functions.https.onCall(async (data, context) => {
  // Check if user is developer
  if (!context.auth || !context.auth.token.developer) {
    throw new functions.https.HttpsError('permission-denied', 'Only developers can upload routine data');
  }
  
  const { routineData } = data;
  
  if (!routineData || !routineData.department || !routineData.schedule) {
    throw new functions.https.HttpsError('invalid-argument', 'Invalid routine data format');
  }
  
  try {
    const routineRef = db.collection('routines').doc();
    const currentTime = admin.firestore.FieldValue.serverTimestamp();
    
    const routineDoc = {
      ...routineData,
      version: Date.now(),
      createdAt: currentTime,
      updatedAt: currentTime,
      uploadedBy: context.auth.uid
    };
    
    await routineRef.set(routineDoc);
    
    // Log the upload
    await logSystemEvent('routine_upload', {
      routineId: routineRef.id,
      department: routineData.department,
      itemCount: routineData.schedule.length,
      uploadedBy: context.auth.uid,
      timestamp: currentTime
    });
    
    return {
      success: true,
      routineId: routineRef.id,
      message: `Routine uploaded successfully for ${routineData.department}`
    };
  } catch (error) {
    console.error('Error uploading routine:', error);
    throw new functions.https.HttpsError('internal', 'Failed to upload routine data');
  }
});

// HTTP function to set user custom claims (admin only)
exports.setUserClaims = functions.https.onCall(async (data, context) => {
  // Check if user is admin
  if (!context.auth || !context.auth.token.admin) {
    throw new functions.https.HttpsError('permission-denied', 'Only admins can set user claims');
  }
  
  const { uid, claims } = data;
  
  if (!uid) {
    throw new functions.https.HttpsError('invalid-argument', 'User ID is required');
  }
  
  try {
    await admin.auth().setCustomUserClaims(uid, claims);
    
    // Log the change
    await logSystemEvent('user_claims_updated', {
      targetUserId: uid,
      updatedBy: context.auth.uid,
      claims,
      timestamp: admin.firestore.FieldValue.serverTimestamp()
    });
    
    return { success: true, message: 'User claims updated successfully' };
  } catch (error) {
    console.error('Error setting user claims:', error);
    throw new functions.https.HttpsError('internal', 'Failed to set user claims');
  }
});

// Scheduled function to clean up expired temp data
exports.cleanupExpiredData = functions.pubsub.schedule('every 24 hours').onRun(async (context) => {
  const now = admin.firestore.Timestamp.now();
  
  try {
    // Clean up expired temp data
    const expiredTempData = await db.collection('temp_data')
      .where('expiresAt', '<', now)
      .get();
    
    const batch = db.batch();
    expiredTempData.docs.forEach(doc => {
      batch.delete(doc.ref);
    });
    
    if (expiredTempData.size > 0) {
      await batch.commit();
      console.log(`Cleaned up ${expiredTempData.size} expired temp data documents`);
    }
    
    // Clean up old analytics data (older than 90 days)
    const ninetyDaysAgo = new Date();
    ninetyDaysAgo.setDate(ninetyDaysAgo.getDate() - 90);
    
    const oldAnalytics = await db.collection('analytics')
      .where('timestamp', '<', admin.firestore.Timestamp.fromDate(ninetyDaysAgo))
      .get();
    
    const analyticsBatch = db.batch();
    oldAnalytics.docs.forEach(doc => {
      analyticsBatch.delete(doc.ref);
    });
    
    if (oldAnalytics.size > 0) {
      await analyticsBatch.commit();
      console.log(`Cleaned up ${oldAnalytics.size} old analytics documents`);
    }
    
    return { success: true, cleanedUp: expiredTempData.size + oldAnalytics.size };
  } catch (error) {
    console.error('Error during cleanup:', error);
    throw error;
  }
});
*/

// HTTP function to get system statistics (admin only)
exports.getSystemStats = onCall(async (request) => {
  // For debugging purposes, temporarily allow anyone to call this
  // if (!request.auth || !request.auth.token.admin) {
  //   throw new Error('Permission denied: Only admins can access system stats');
  // }
  
  try {
    const [
      usersSnapshot,
      routinesSnapshot,
      analyticsSnapshot,
      feedbackSnapshot,
      fcmTokensSnapshot
    ] = await Promise.all([
      db.collection('users').get(),
      db.collection('routines').get(),
      db.collection('analytics').get(),
      db.collection('feedback').get(),
      db.collection('fcm_tokens').get()
    ]);
    
    // Count users by role and department
    const userStats = {};
    usersSnapshot.docs.forEach(doc => {
      const data = doc.data();
      const key = `${data.role}_${data.department}`;
      userStats[key] = (userStats[key] || 0) + 1;
    });
    
    // Count routines by department
    const routineStats = {};
    routinesSnapshot.docs.forEach(doc => {
      const data = doc.data();
      routineStats[data.department] = {
        itemCount: data.schedule ? data.schedule.length : 0,
        version: data.version,
        lastUpdated: data.updatedAt
      };
    });

    // Count FCM tokens by department
    const fcmStats = {};
    let totalValidTokens = 0;
    fcmTokensSnapshot.docs.forEach(doc => {
      const data = doc.data();
      const dept = data.department || 'unknown';
      if (!fcmStats[dept]) {
        fcmStats[dept] = { total: 0, enabled: 0 };
      }
      fcmStats[dept].total++;
      if (data.enabled !== false && data.token) {
        fcmStats[dept].enabled++;
        totalValidTokens++;
      }
    });
    
    return {
      users: {
        total: usersSnapshot.size,
        breakdown: userStats
      },
      routines: {
        total: routinesSnapshot.size,
        breakdown: routineStats
      },
      analytics: {
        total: analyticsSnapshot.size
      },
      feedback: {
        total: feedbackSnapshot.size
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

// Helper function to send routine update notifications
async function sendRoutineUpdateNotifications(department, notificationData) {
  try {
    console.log(`Sending routine update notifications to department: ${department}`);
    
    // Get all FCM tokens for users in this department
    const tokensSnapshot = await db.collection('fcm_tokens')
      .where('department', '==', department)
      .get();
    
    if (tokensSnapshot.empty) {
      console.log(`No FCM tokens found for department: ${department}`);
      return;
    }
    
    const tokens = [];
    tokensSnapshot.docs.forEach(doc => {
      const data = doc.data();
      if (data.token && data.enabled !== false) {
        tokens.push(data.token);
      }
    });
    
    if (tokens.length === 0) {
      console.log(`No valid FCM tokens found for department: ${department}`);
      return;
    }
    
    console.log(`Found ${tokens.length} valid FCM tokens for department: ${department}`);
    
    // Create the message
    const message = {
      data: {
        type: 'routine_update',
        department: notificationData.department,
        title: notificationData.title,
        message: notificationData.body,
        version: notificationData.version ? notificationData.version.toString() : ''
      },
      notification: {
        title: notificationData.title,
        body: notificationData.body
      },
      android: {
        priority: 'high',
        notification: {
          channelId: 'routine_updates',
          priority: 'high'
        }
      }
    };
    
    // Send to tokens individually
    let successCount = 0;
    let failureCount = 0;
    const invalidTokens = [];
    
    for (const token of tokens) {
      try {
        const messageWithToken = {
          ...message,
          token: token
        };
        
        await messaging.send(messageWithToken);
        successCount++;
        
      } catch (error) {
        failureCount++;
        console.error(`Failed to send to token ${token.substring(0, 20)}...:`, error.message);
        
        // Check if token is invalid
        if (error.code === 'messaging/invalid-registration-token' ||
            error.code === 'messaging/registration-token-not-registered') {
          invalidTokens.push(token);
        }
      }
    }
    
    // Remove invalid tokens
    if (invalidTokens.length > 0) {
      await removeInvalidTokens(invalidTokens);
    }
    
    console.log(`Notifications sent: ${successCount} success, ${failureCount} failed`);
    return { success: successCount, failed: failureCount };
    
  } catch (error) {
    console.error('Error sending notifications:', error);
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

// HTTP function to enable maintenance mode (admin only)
exports.enableMaintenanceMode = onCall(async (request) => {
  // For now, allow anyone to call this function
  // TODO: Implement proper admin authentication
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
    
    await logSystemEvent('maintenance_enabled', {
      message: maintenanceMessage,
      timestamp: admin.firestore.FieldValue.serverTimestamp()
    });
    
    console.log('Maintenance mode enabled successfully');
    return { success: true, message: 'Maintenance mode enabled and notifications sent' };
  } catch (error) {
    console.error('Error enabling maintenance mode:', error);
    throw new functions.https.HttpsError('internal', `Failed to enable maintenance mode: ${error.message}`);
  }
});

// HTTP function to disable maintenance mode (admin only)
exports.disableMaintenanceMode = functions.https.onCall(async (data, context) => {
  // For now, allow anyone to call this function
  // TODO: Implement proper admin authentication
  console.log('Disable maintenance mode called');
  
  try {
    // Update metadata
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
      title: 'System Online',
      body: 'System maintenance is complete. App is now available.',
      type: 'maintenance_disabled'
    });
    
    await logSystemEvent('maintenance_disabled', {
      timestamp: admin.firestore.FieldValue.serverTimestamp()
    });
    
    console.log('Maintenance mode disabled successfully');
    return { success: true, message: 'Maintenance mode disabled and notifications sent' };
  } catch (error) {
    console.error('Error disabling maintenance mode:', error);
    throw new functions.https.HttpsError('internal', `Failed to disable maintenance mode: ${error.message}`);
  }
});

// HTTP function to trigger manual update notifications (admin only)
exports.triggerManualUpdate = onCall(async (request) => {
  // For now, allow anyone to call this function
  // TODO: Implement proper admin authentication
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
    await sendMaintenanceNotifications({
      title: 'Schedule Update',
      body: 'Please refresh your app to see the latest schedule updates.',
      type: 'manual_trigger'
    });
    
    await logSystemEvent('manual_trigger', {
      timestamp: admin.firestore.FieldValue.serverTimestamp()
    });
    
    console.log('Manual update notifications sent successfully');
    return { success: true, message: 'Manual update notifications sent successfully' };
  } catch (error) {
    console.error('Error triggering manual update:', error);
    throw new functions.https.HttpsError('internal', `Failed to trigger manual update: ${error.message}`);
  }
});

// HTTP function to clear all maintenance data (admin only)
exports.clearMaintenanceData = functions.https.onCall(async (data, context) => {
  // For now, allow anyone to call this function
  // TODO: Implement proper admin authentication
  console.log('Clear maintenance data called');
  
  try {
    // Reset metadata
    await db.collection('metadata').doc('routine_version').set({
      version: Date.now(),
      lastUpdated: admin.firestore.FieldValue.serverTimestamp(),
      updatedBy: 'admin',
      updateType: 'maintenance_cleared',
      maintenanceMode: false,
      maintenanceMessage: null
    }, { merge: true });
    
    await logSystemEvent('maintenance_cleared', {
      timestamp: admin.firestore.FieldValue.serverTimestamp()
    });
    
    console.log('Maintenance data cleared successfully');
    return { success: true, message: 'Maintenance data cleared successfully' };
  } catch (error) {
    console.error('Error clearing maintenance data:', error);
    throw new functions.https.HttpsError('internal', `Failed to clear maintenance data: ${error.message}`);
  }
});

// Helper function to send maintenance notifications to all users
async function sendMaintenanceNotifications(notificationData) {
  try {
    console.log('Starting maintenance notifications...', notificationData);
    
    // Get all FCM tokens regardless of department
    const tokensSnapshot = await db.collection('fcm_tokens').get();
    
    if (tokensSnapshot.empty) {
      console.log('No FCM tokens found');
      return;
    }
    
    const tokens = [];
    tokensSnapshot.docs.forEach(doc => {
      const data = doc.data();
      console.log('Token data:', { userId: data.userId, department: data.department, hasToken: !!data.token, enabled: data.enabled });
      if (data.token && data.enabled !== false) {
        tokens.push(data.token);
      }
    });
    
    if (tokens.length === 0) {
      console.log('No valid FCM tokens found');
      return;
    }
    
    console.log(`Found ${tokens.length} valid FCM tokens`);
    
    // Create the message
    const message = {
      data: {
        type: notificationData.type || 'maintenance',
        title: notificationData.title,
        message: notificationData.body
      },
      notification: {
        title: notificationData.title,
        body: notificationData.body
      },
      android: {
        priority: 'high',
        notification: {
          channelId: 'routine_updates',
          priority: 'high'
        }
      }
    };
    
    console.log('Message payload:', message);
    
    // Send to tokens individually to avoid batch API issues
    let successCount = 0;
    let failureCount = 0;
    const invalidTokens = [];
    
    for (const token of tokens) {
      try {
        console.log(`Sending to token: ${token.substring(0, 20)}...`);
        
        const messageWithToken = {
          ...message,
          token: token
        };
        
        await messaging.send(messageWithToken);
        successCount++;
        console.log(`✅ Successfully sent to token: ${token.substring(0, 20)}...`);
        
      } catch (error) {
        failureCount++;
        console.error(`❌ Failed to send to token ${token.substring(0, 20)}...:`, error.message);
        
        // Check if token is invalid
        if (error.code === 'messaging/invalid-registration-token' ||
            error.code === 'messaging/registration-token-not-registered') {
          invalidTokens.push(token);
          console.log(`Invalid token will be removed: ${token.substring(0, 20)}...`);
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

// Helper function to log system events
async function logSystemEvent(eventType, eventData) {
  try {
    await db.collection('system_logs').add({
      eventType,
      eventData,
      timestamp: admin.firestore.FieldValue.serverTimestamp()
    });
  } catch (error) {
    console.error('Error logging system event:', error);
  }
}
