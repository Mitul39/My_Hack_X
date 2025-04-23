# Project Migration Guide for My_HackX

This guide will help you fix the experimental API warnings and unresolved references across your project after the model updates.

## Common Errors and Fixes

### 1. Experimental API Warnings

Add the `@OptIn` annotation to any composable using experimental Material3 APIs:

```kotlin
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun YourScreen() {
    // ...
}
```

**Components that need this annotation:**
- Any screen using `TopAppBar`
- Any screen using `BottomAppBar`
- Any screen using experimental Material3 components

### 2. Field/Property Name Changes

The data models have been updated with new field names. Here's a mapping of old names to new names:

#### HackathonEvent Model
| Old Property | New Property |
|-------------|--------------|
| title | name |
| technologies | tags |
| requirements | rules |
| registeredTeams | teams (List of IDs, not Team objects) |
| maxParticipants | maxTeamSize |
| imageUrl | *removed* - handle separately |
| eventType | *removed* - handle separately |

#### Prize Model
| Old Property | New Property |
|-------------|--------------|
| rank | position |
| title | description |
| amount | value |

### 3. Type Changes

- `startDate`, `endDate` changed from `Long` to `Date` objects
- Team references changed from being embedded to using IDs

## How to Fix the Issues

### For Each Screen

1. **Fix TopAppBar Experimental API Warnings**
   - Add `@OptIn(ExperimentalMaterial3Api::class)` to the composable function

2. **Fix HackathonEvent References**
   - Replace `event.title` with `event.name`
   - Replace `event.technologies` with `event.tags`
   - Replace `event.requirements` with `event.rules`
   - Replace `event.registeredTeams` with a separate teams list in your ViewModel
   - Replace `event.maxParticipants` with `event.maxTeamSize`

3. **Fix Date Handling**
   - Update date formatting functions to accept `Date` objects instead of `Long`

### For ViewModels

1. **Update Event Filtering and Sorting**
   - Update any logic that references old field names
   - Adjust any sorting or filtering that used removed fields

2. **Team Management**
   - Update team handling to work with team IDs rather than embedded team objects
   - Fetch team details separately when needed

## Example Updates

### TopAppBar Usage
```kotlin
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun YourScreen() {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Your Title") },
                // other attributes
            )
        }
    ) { padding ->
        // Content
    }
}
```

### Date Formatting
```kotlin
// Old
private fun formatDate(timestamp: Long): String {
    val sdf = SimpleDateFormat("MMM dd, yyyy", Locale.getDefault())
    return sdf.format(Date(timestamp))
}

// New
private fun formatDate(date: Date): String {
    val formatter = SimpleDateFormat("MMM dd, yyyy", Locale.getDefault())
    return formatter.format(date)
}
```

### Team References
```kotlin
// Old
event.registeredTeams.forEach { team ->
    // Use team directly
}

// New
// In ViewModel
val teamsState = _teamsState.value as TeamState.Success
teamsState.teams.forEach { team ->
    // Use fetched teams
}
```

## Files to Update

Based on the search results, these files need attention:

1. **UI Screens**
   - `EventDetailsScreen.kt`
   - `EventsScreen.kt`
   - `HomeScreen.kt`
   - `AdminScreen.kt`
   - `ProfileScreen.kt`
   - `NotificationsScreen.kt`

2. **ViewModels**
   - `EventDetailsViewModel.kt`
   - `EventsViewModel.kt`
   - `AdminViewModel.kt`

3. **Components**
   - `EventDialog.kt`
   - `FilterDialog.kt`
   - `RegistrationDialog.kt`

## Next Steps

1. Fix the experimental API warnings with `@OptIn`
2. Update UI components with the new property names
3. Update ViewModels to work with the new data structure
4. Test each screen to ensure proper rendering and functionality 