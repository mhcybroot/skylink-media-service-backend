# Admin Dashboard - Advanced Search Quick Reference

## Overview
The Admin Dashboard now includes powerful advanced search capabilities to help you find projects quickly and precisely.

---

## How to Use

### Basic Search (Quick)
1. Type in the search box: WO#, client code, location, or description
2. Click **Search**
3. Results appear instantly

### Advanced Search (Precise)
1. Click the **Filters** button
2. Select your filter criteria:
   - **Project Status**: Filter by workflow stage
   - **Payment Status**: Filter by payment state
   - **Due Date Range**: Find projects due within specific dates
   - **Invoice Price Range**: Filter by project value
   - **Assigned Contractor**: See projects assigned to specific contractors
3. Click **Search**
4. View active filters summary below the filter panel

### Reset Filters
Click the **Reset** button to clear all filters and show all projects.

---

## Filter Options

### Project Status
- **Unassigned**: Projects not yet assigned to contractors
- **Assigned**: Projects assigned but not started
- **Unread**: Projects assigned but not opened by contractor
- **In Field**: Contractor is actively working on the project
- **Ready to Office**: Work completed, awaiting admin review
- **Closed**: Project completed and closed

### Payment Status
- **Unpaid**: No payment received
- **Partial**: Partial payment received
- **Paid**: Fully paid

### Date Filters
- **Due Date From**: Projects due on or after this date
- **Due Date To**: Projects due on or before this date
- Use both for a specific date range

### Price Filters
- **Invoice Price From**: Projects valued at or above this amount
- **Invoice Price To**: Projects valued at or below this amount
- Use both for a specific price range

### Contractor Filter
- Select a contractor to see only their assigned projects
- Useful for workload monitoring

---

## Example Use Cases

### Find Overdue Projects
1. Click **Filters**
2. Set **Due Date To**: Today's date
3. Set **Status**: Not "Closed"
4. Click **Search**

### Find High-Value Unpaid Projects
1. Click **Filters**
2. Set **Payment Status**: Unpaid
3. Set **Invoice Price From**: $5,000
4. Click **Search**

### Find Projects Ready for Review
1. Click **Filters**
2. Set **Status**: Ready to Office
3. Click **Search**

### Monitor Specific Contractor
1. Click **Filters**
2. Select **Assigned Contractor**: [Contractor Name]
3. Click **Search**

### Find Projects Due This Week
1. Click **Filters**
2. Set **Due Date From**: Monday of this week
3. Set **Due Date To**: Sunday of this week
4. Click **Search**

---

## Tips

- **Combine Filters**: Use multiple filters together for precise results
- **Active Filters**: Color-coded tags show which filters are active
- **Persistent Search**: Your search persists when switching between Projects and Contractors tabs
- **Quick Reset**: One click clears all filters
- **Collapsible Panel**: Hide filters when not needed to keep the interface clean

---

## Keyboard Shortcuts

- **Enter** in search box: Submit search
- **Escape** when filter panel is open: Close panel (if implemented)

---

## Performance Notes

- Searches are optimized for speed
- Results appear in under 300ms for most queries
- Database indexes ensure fast filtering even with 1000+ projects

---

## Troubleshooting

**No results found?**
- Check if filters are too restrictive
- Click **Reset** and try again
- Verify date formats are correct

**Filter panel won't open?**
- Refresh the page
- Check browser console for errors

**Slow search?**
- Contact system administrator to run database optimization
- File: `advanced-search-indexes.sql`

---

## Support

For technical issues or feature requests, contact your system administrator.

**Version**: 1.0  
**Last Updated**: March 17, 2026
