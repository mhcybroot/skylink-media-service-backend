# QUICK START TESTING GUIDE
**For:** QA Team / Product Owner  
**Purpose:** Verify payment status filter fix  
**Time Required:** 10 minutes

---

## WHAT WAS FIXED

**Problem:** When you selected "Paid" in the payment status filter and clicked Search, it showed ALL projects instead of just paid ones.

**Fix:** Added the missing "Partial" payment status and fixed the filter logic.

---

## HOW TO TEST

### Step 1: Access the Application
1. Open browser
2. Go to: http://localhost:8085
3. Login with: `admin` / `admin123`
4. You should see the Admin Dashboard

### Step 2: Test Payment Status Filter

#### Test A: Filter by UNPAID
1. Click the **"Filters"** button (next to search bar)
2. In the dropdown that appears, find **"Payment Status"**
3. Select **"Unpaid"**
4. Click **"Search"** button
5. ✅ **VERIFY:** Only projects with red "UNPAID" badge are shown
6. ✅ **VERIFY:** Active filter badge shows "Payment: Unpaid" in red

#### Test B: Filter by PARTIAL
1. Click **"Filters"** button
2. Select **"Partial"** from Payment Status
3. Click **"Search"**
4. ✅ **VERIFY:** Only projects with amber "PARTIAL" badge are shown
5. ✅ **VERIFY:** Active filter badge shows "Payment: Partial" in amber/yellow

#### Test C: Filter by PAID
1. Click **"Filters"** button
2. Select **"Paid"** from Payment Status
3. Click **"Search"**
4. ✅ **VERIFY:** Only projects with green "PAID" badge are shown
5. ✅ **VERIFY:** Active filter badge shows "Payment: Paid" in green
6. ✅ **VERIFY:** This is the bug that was broken - it should NOT show all projects

#### Test D: Reset Filters
1. After applying any filter, click **"Reset"** button
2. ✅ **VERIFY:** All projects are shown again
3. ✅ **VERIFY:** No active filter badges appear
4. ✅ **VERIFY:** Filter dropdown is cleared

### Step 3: Test Payment Status Change

#### Test E: Change a Project's Payment Status
1. Find any project card
2. Click the **"SET PAYMENT"** dropdown button
3. You should see three options: UNPAID, PARTIAL, PAID
4. Select **"PARTIAL"**
5. ✅ **VERIFY:** Page reloads with success message
6. ✅ **VERIFY:** Project badge now shows "PARTIAL" in amber color
7. Try changing it to **"PAID"**
8. ✅ **VERIFY:** Badge changes to green "PAID"

### Step 4: Test UI Consistency

#### Test F: Visual Alignment
1. Look at the search bar at the top
2. ✅ **VERIFY:** Search input, Filters button, Search button, and Reset button are all the same height
3. ✅ **VERIFY:** Search icon inside the input is vertically centered
4. Click **"Filters"** to open the advanced filter panel
5. ✅ **VERIFY:** All filter dropdowns and inputs have the same height
6. ✅ **VERIFY:** Everything looks aligned and professional

### Step 5: Test Combined Filters

#### Test G: Multiple Filters at Once
1. Click **"Filters"**
2. Select **Status: "ASSIGNED"**
3. Select **Payment Status: "UNPAID"**
4. Click **"Search"**
5. ✅ **VERIFY:** Only projects that are BOTH assigned AND unpaid are shown
6. ✅ **VERIFY:** Both filter badges appear in the active filters section

---

## EXPECTED RESULTS SUMMARY

| Test | What to Check | Expected Result |
|------|---------------|-----------------|
| A | Filter by UNPAID | Only red UNPAID badges shown |
| B | Filter by PARTIAL | Only amber PARTIAL badges shown |
| C | Filter by PAID | Only green PAID badges shown (THIS WAS BROKEN) |
| D | Reset filters | All projects shown, filters cleared |
| E | Change payment status | Badge updates to correct color |
| F | UI alignment | All elements same height, properly aligned |
| G | Combined filters | Only projects matching ALL criteria shown |

---

## WHAT IF SOMETHING FAILS?

### If filter shows wrong projects:
1. Check the browser console for errors (F12 → Console tab)
2. Check server logs: `tail -f /tmp/skylink-boot.log`
3. Look for lines starting with `DEBUG AdminController` or `ERROR`

### If UI looks misaligned:
1. Try refreshing the page (Ctrl+F5 / Cmd+Shift+R)
2. Try a different browser
3. Check browser zoom is at 100%

### If application won't start:
1. Check if port 8085 is already in use: `netstat -tlnp | grep 8085`
2. Check logs: `tail -f /tmp/skylink-boot.log`
3. Verify database is running: `psql -U postgres -d skylink_media_service -c "SELECT 1"`

---

## PASS/FAIL CRITERIA

**PASS if:**
- ✅ All 7 tests (A-G) pass
- ✅ No console errors
- ✅ UI looks consistent and professional
- ✅ Filters work correctly individually and combined

**FAIL if:**
- ❌ Filter by PAID still shows all projects (original bug)
- ❌ Any filter shows wrong results
- ❌ UI elements are misaligned
- ❌ Console shows JavaScript errors
- ❌ Server logs show exceptions

---

## QUICK TROUBLESHOOTING

### "I don't see any projects"
- This might be correct if no projects match your filters
- Click "Reset" to see all projects
- Check if you have any projects in the database

### "PARTIAL option doesn't appear"
- Refresh the page (Ctrl+F5)
- Clear browser cache
- Verify you're on the latest version

### "Filter doesn't seem to work"
- Make sure you clicked "Search" button after selecting filter
- Check if active filter badge appears below the filter panel
- Try clicking "Reset" and filtering again

---

## TESTING CHECKLIST

Print this and check off as you test:

```
[ ] Test A: Filter by UNPAID - PASS / FAIL
[ ] Test B: Filter by PARTIAL - PASS / FAIL
[ ] Test C: Filter by PAID - PASS / FAIL (CRITICAL - this was the bug)
[ ] Test D: Reset filters - PASS / FAIL
[ ] Test E: Change payment status - PASS / FAIL
[ ] Test F: UI consistency - PASS / FAIL
[ ] Test G: Combined filters - PASS / FAIL

Overall Result: PASS / FAIL

Tested by: ___________________
Date: ___________________
Browser: ___________________
Notes: ___________________
```

---

## CONTACT

If you find any issues:
1. Note which test failed
2. Take a screenshot
3. Check browser console for errors
4. Check server logs
5. Report to development team with all above information

---

**Document Version:** 1.0  
**Last Updated:** 2026-03-17  
**Estimated Testing Time:** 10 minutes  
**Difficulty:** Easy
