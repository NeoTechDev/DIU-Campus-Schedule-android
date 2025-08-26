const functions = require('firebase-functions');
const admin = require('firebase-admin');

admin.initializeApp();

const db = admin.firestore();
const messaging = admin.messaging();

// Trigger when a routine document is updated
exports.onRoutineUpdate = functions.firestore
  .document('routines/{routineId}')
  .onUpdate(async (change, context) => {
    const routineId = context.params.routineId;
    const newData = change.after.data();
    const oldData = change.before.data();
    
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
        throw new functions.https.HttpsError('internal', 'Failed to process routine update');
      }
    }
    
    return { success: true, message: 'No version change detected' };
  });

// HTTP function to manually trigger routine update notifications
exports.triggerRoutineUpdate = functions.https.onCall(async (data, context) => {
  // Check if user is admin or developer
  if (!context.auth || (!context.auth.token.admin && !context.auth.token.developer)) {
    throw new functions.https.HttpsError('permission-denied', 'Only admins can trigger routine updates');
  }
  
  const { department, title, message } = data;
  
  if (!department) {
    throw new functions.https.HttpsError('invalid-argument', 'Department is required');
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
    throw new functions.https.HttpsError('internal', 'Failed to send notifications');
  }
});

// HTTP function to upload routine data (developer only)
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

// HTTP function to get system statistics (admin only)
exports.getSystemStats = functions.https.onCall(async (data, context) => {
  if (!context.auth || !context.auth.token.admin) {
    throw new functions.https.HttpsError('permission-denied', 'Only admins can access system stats');
  }
  
  try {
    const [
      usersSnapshot,
      routinesSnapshot,
      analyticsSnapshot,
      feedbackSnapshot
    ] = await Promise.all([
      db.collection('users').get(),
      db.collection('routines').get(),
      db.collection('analytics').get(),
      db.collection('feedback').get()
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
      generatedAt: admin.firestore.FieldValue.serverTimestamp()
    };
  } catch (error) {
    console.error('Error getting system stats:', error);
    throw new functions.https.HttpsError('internal', 'Failed to get system statistics');
  }
});

// Helper function to send routine update notifications
async function sendRoutineUpdateNotifications(department, notificationData) {
  try {
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
      },
      apns: {
        payload: {
          aps: {
            alert: {
              title: notificationData.title,
              body: notificationData.body
            },
            sound: 'default'
          }
        }
      }
    };
    
    // Send to all tokens (batch of 500 max)
    const batchSize = 500;
    const promises = [];
    
    for (let i = 0; i < tokens.length; i += batchSize) {
      const batch = tokens.slice(i, i + batchSize);
      const multicastMessage = {
        ...message,
        tokens: batch
      };
      
      promises.push(messaging.sendMulticast(multicastMessage));
    }
    
    const results = await Promise.all(promises);
    
    let totalSuccess = 0;
    let totalFailure = 0;
    
    results.forEach(result => {
      totalSuccess += result.successCount;
      totalFailure += result.failureCount;
      
      // Handle failed tokens (remove invalid ones)
      if (result.failureCount > 0) {
        const failedTokens = [];
        result.responses.forEach((resp, idx) => {
          if (!resp.success && 
              (resp.error.code === 'messaging/invalid-registration-token' ||
               resp.error.code === 'messaging/registration-token-not-registered')) {
            failedTokens.push(tokens[idx]);
          }
        });
        
        // Remove invalid tokens from database
        if (failedTokens.length > 0) {
          removeInvalidTokens(failedTokens);
        }
      }
    });
    
    console.log(`Notifications sent: ${totalSuccess} success, ${totalFailure} failed`);
    
    return { success: totalSuccess, failed: totalFailure };
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
