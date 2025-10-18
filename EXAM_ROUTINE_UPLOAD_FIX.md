## Exam Routine Upload Fix

### Issue
The exam routine JSON file uses a different structure than expected:
- **JSON file has:** `"schedule"` array with `"code"`, `"name"`, `"batch"`, `"slot"`
- **Code expected:** `"days"` array with `"courseCode"`, `"courseTitle"`, `"batches"`, `"time"`

### Solution Applied

1. **Updated Dashboard Validation (`dashboard.html`):**
   - Added `transformExamRoutineFormat()` function
   - Accepts both `schedule` and `days` formats
   - Transforms `schedule` format to expected `days` format
   - Maps time slots using the `slots` object

2. **Updated Firebase Function (`index.js`):**
   - Added `transformExamRoutineForFirebase()` function 
   - Server-side validation for both formats
   - Consistent data transformation

3. **Fixed Android Collection Name:**
   - Changed from `"examRoutines"` to `"exam_routines"` to match Firebase function

### Data Transformation
```
Input (examRoutine.json):
{
  "schedule": [
    {
      "date": "29/10/2025",
      "weekday": "Wednesday", 
      "courses": [
        {
          "code": "ENG101",
          "name": "English-1",
          "batch": "46",
          "slot": "A"
        }
      ]
    }
  ],
  "slots": {
    "A": "9:00 am - 10:30 am"
  }
}

Output (Firebase storage):
{
  "days": [
    {
      "date": "29/10/2025",
      "dayName": "Wednesday",
      "courses": [
        {
          "courseCode": "ENG101", 
          "courseTitle": "English-1",
          "batches": ["46"],
          "time": "9:00 am - 10:30 am",
          "room": "TBA",
          "teacher": "TBA"
        }
      ]
    }
  ]
}
```

### Next Steps
1. Try uploading the exam routine JSON file again via the admin dashboard
2. The system should now accept and transform the file correctly
3. If successful, you should see exam routines listed in the admin panel
4. You can then enable exam mode to test the complete workflow

### Files Updated
- `firebase-hosting/public/dashboard.html` - Added transformation logic
- `firebase-hosting/functions/index.js` - Added server-side transformation
- `app/src/main/java/.../ExamRoutineRemoteDataSource.kt` - Fixed collection name

Both Firebase functions and hosting have been deployed with these changes.