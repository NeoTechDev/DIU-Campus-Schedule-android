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

// Helper function to transform exam routine from schedule format to days format
function transformExamRoutineForFirebase(rawData) {
  try {
    console.log('Transforming exam routine format from schedule to days');
    
    if (!rawData || !rawData.schedule || !Array.isArray(rawData.schedule)) {
      throw new Error('Invalid rawData: schedule array is required');
    }
    
    console.log(`Processing ${rawData.schedule.length} days`);
    
    const days = rawData.schedule.map((daySchedule, dayIndex) => {
      try {
        if (!daySchedule.courses || !Array.isArray(daySchedule.courses)) {
          console.warn(`Day ${dayIndex} has no courses array, skipping`);
          return {
            date: daySchedule.date || 'Unknown',
            dayName: daySchedule.weekday || 'Unknown',
            courses: []
          };
        }
        
        console.log(`Processing ${daySchedule.courses.length} courses for day ${dayIndex}`);
        
        return {
          date: daySchedule.date,
          dayName: daySchedule.weekday,
          courses: daySchedule.courses.map((course, courseIndex) => {
            try {
              // Get actual time from slots if available
              let time = course.slot || 'TBA';
              if (rawData.slots && rawData.slots[course.slot]) {
                time = rawData.slots[course.slot];
              }
              
              return {
                courseCode: course.code || 'UNKNOWN',
                courseTitle: course.name || 'Unknown Course',
                time: time,
                room: course.room || 'TBA',
                teacher: course.teacher || 'TBA',
                batches: course.batch ? [course.batch.toString()] : []
              };
            } catch (courseError) {
              console.error(`Error processing course ${courseIndex} on day ${dayIndex}:`, courseError);
              throw courseError;
            }
          })
        };
      } catch (dayError) {
        console.error(`Error processing day ${dayIndex}:`, dayError);
        throw dayError;
      }
    });
    
    console.log(`Transformation completed: ${days.length} days processed`);
  } catch (transformError) {
    console.error('Error in transformExamRoutineForFirebase:', transformError);
    throw transformError;
  }
  
  return {
    days: days,
    semester: rawData.semester || 'Unknown',
    examType: rawData.exam_type || 'Exam',
    startDate: rawData.start_date,
    endDate: rawData.end_date
  };
}

// HTTP function to upload exam routine (admin only)
exports.uploadExamRoutine = functions.https.onCall(async (data, context) => {
  console.log('Upload exam routine function started - SIMPLE VERSION');
  
  try {
    // Extract exam routine data - get the actual JSON data
    let examRoutineData;
    
    if (data.examRoutineData) {
      examRoutineData = data.examRoutineData;
    } else if (data.data && data.data.examRoutineData) {
      examRoutineData = data.data.examRoutineData;
    } else if (data.data) {
      examRoutineData = data.data;
    } else {
      examRoutineData = data;
    }
    
    if (!examRoutineData) {
      throw new functions.https.HttpsError('invalid-argument', 'No exam routine data provided');
    }
    
    console.log('BEFORE STORAGE - Raw data keys:', Object.keys(examRoutineData));
    console.log('BEFORE STORAGE - Has schedule:', !!examRoutineData.schedule);
    console.log('BEFORE STORAGE - Has days:', !!examRoutineData.days);
    
    // Create Firestore document
    const examRoutineRef = db.collection('exam_routines').doc();
    
    // Store EXACTLY the same data as received - NO CHANGES AT ALL
    await examRoutineRef.set(examRoutineData);
    
    console.log('STORED SUCCESSFULLY - Document ID:', examRoutineRef.id);
    
    return { 
      success: true, 
      message: 'Exam routine uploaded successfully',
      documentId: examRoutineRef.id
    };
    
  } catch (error) {
    console.error('Error uploading exam routine:', error);
    throw new functions.https.HttpsError('internal', `Failed to upload exam routine: ${error.message}`);
  }
});

// HTTP function to get exam routines (admin only)
exports.getExamRoutines = functions.https.onCall(async (data, context) => {
  console.log('Get exam routines function started - SIMPLE VERSION');
  
  try {
    const examRoutinesSnapshot = await db.collection('exam_routines').get();
    
    if (examRoutinesSnapshot.empty) {
      console.log('No exam routines found');
      return { success: true, examRoutines: [] };
    }
    
    const examRoutines = [];
    examRoutinesSnapshot.forEach(doc => {
      const data = doc.data();
      
      // Return EXACTLY what's stored - NO CHANGES
      examRoutines.push({
        id: doc.id,
        ...data
      });
    });
    
    console.log(`Found ${examRoutines.length} exam routines`);
    
    return { 
      success: true, 
      examRoutines: examRoutines
    };
    
  } catch (error) {
    console.error('Error getting exam routines:', error);
    throw new functions.https.HttpsError('internal', `Failed to get exam routines: ${error.message}`);
  }
});

// HTTP function to delete exam routine (admin only) - UPDATED VERSION
exports.deleteExamRoutine = functions.https.onCall(async (data, context) => {
  console.log('Delete exam routine called - UPDATED VERSION');
  console.log('Received data:', JSON.stringify(data));
  
  const { documentId } = data;
  
  if (!documentId) {
    console.error('No documentId provided in data:', data);
    throw new functions.https.HttpsError('invalid-argument', 'Document ID is required for deletion');
  }
  
  try {
    console.log(`Attempting to delete exam routine with document ID: ${documentId}`);
    await db.collection('exam_routines').doc(documentId).delete();
    
    console.log(`SUCCESS: Exam routine deleted with document ID: ${documentId}`);
    
    return { 
      success: true, 
      message: `Exam routine deleted successfully`
    };
  } catch (error) {
    console.error('ERROR deleting exam routine:', error);
    throw new functions.https.HttpsError('internal', `Failed to delete exam routine: ${error.message}`);
  }
});

// HTTP function to set exam mode (admin only)
exports.setExamMode = functions.https.onCall(async (data, context) => {
  console.log('Set exam mode called');
  console.log('Data received:', data);
  console.log('Data type:', typeof data);
  console.log('Data keys:', data ? Object.keys(data) : 'data is null/undefined');
  
  // Handle different possible data structures
  let examModeEnabled;
  
  if (data && typeof data === 'object') {
    if ('examModeEnabled' in data) {
      examModeEnabled = data.examModeEnabled;
      console.log('Found examModeEnabled directly in data');
    } else if (data.data && 'examModeEnabled' in data.data) {
      examModeEnabled = data.data.examModeEnabled;
      console.log('Found examModeEnabled in data.data');
    } else {
      console.log('examModeEnabled not found in expected locations');
      console.log('Available properties:', Object.keys(data));
    }
  }
  
  console.log('Final examModeEnabled:', examModeEnabled);
  console.log('examModeEnabled type:', typeof examModeEnabled);
  
  if (typeof examModeEnabled !== 'boolean') {
    console.error('Invalid examModeEnabled type:', typeof examModeEnabled, 'Value:', examModeEnabled);
    throw new functions.https.HttpsError('invalid-argument', `examModeEnabled must be a boolean, received ${typeof examModeEnabled}: ${examModeEnabled}`);
  }
  
  try {
    // Update metadata to include exam mode
    await db.collection('metadata').doc('routine_version').set({
      examMode: examModeEnabled,
      examModeUpdatedAt: admin.firestore.FieldValue.serverTimestamp(),
      examModeUpdatedBy: 'admin'
    }, { merge: true });
    
    console.log(`Exam mode ${examModeEnabled ? 'enabled' : 'disabled'} successfully`);
    
    // Send push notifications to all users about exam mode change
    if (examModeEnabled) {
      await sendMaintenanceNotifications({
        title: 'Exam Mode Activated',
        body: 'The app is now showing exam schedules. Check your exam routine!',
        type: 'exam_mode_enabled'
      });
    } else {
      await sendMaintenanceNotifications({
        title: 'Back to Normal Schedule',
        body: 'The app is now showing regular class schedules.',
        type: 'exam_mode_disabled'
      });
    }
    
    return { 
      success: true, 
      message: `Exam mode ${examModeEnabled ? 'enabled' : 'disabled'} successfully`,
      examModeEnabled: examModeEnabled
    };
  } catch (error) {
    console.error('Error setting exam mode:', error);
    throw new functions.https.HttpsError('internal', `Failed to set exam mode: ${error.message}`);
  }
});

// HTTP function to get exam mode status
exports.getExamMode = functions.https.onCall(async (data, context) => {
  console.log('Get exam mode called');
  
  try {
    const metadataDoc = await db.collection('metadata').doc('routine_version').get();
    
    let examModeEnabled = false;
    if (metadataDoc.exists) {
      const metadataData = metadataDoc.data();
      examModeEnabled = metadataData.examMode || false;
    }
    
    console.log(`Current exam mode status: ${examModeEnabled}`);
    
    return { 
      success: true, 
      examModeEnabled: examModeEnabled
    };
  } catch (error) {
    console.error('Error getting exam mode:', error);
    throw new functions.https.HttpsError('internal', `Failed to get exam mode: ${error.message}`);
  }
});
