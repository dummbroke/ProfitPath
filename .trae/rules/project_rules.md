### **General Guidelines**
1. **Language and Documentation**:
   - All code and documentation must be written in English.
   - Provide concise, meaningful inline comments for trade entry logic, data validation, and storage handling.
   - Ensure all code follows Kotlin idiomatic practices.

2. **Type Declaration**:
   - Explicitly declare types for variables, function parameters, and return values.
   - Avoid using `Any`. Instead, use well-defined models like `TradeEntry`, `JournalEntry`.

3. **Coding Style**:
   - Follow **PascalCase** for class names (e.g., `TradeHistoryScreen`).
   - Use **camelCase** for variables, methods, and functions (e.g., `fetchTradeData()`).
   - Name files and directories using **snake_case** for clarity (`trade_history_screen.kt`).
   - Define constants in **UPPERCASE** (e.g., `MAX_SCREENSHOTS = 50`).

---

### **Trade Entry and Data Storage**
1. **Hybrid Storage Approach**:
   - **Text Data** must be stored in **Firebase Firestore** for cloud access.
   - **Screenshots** must be saved **locally on the user's device**, with only the file path stored in Firestore.
   - Trade records must include a **timestamp**, **win/loss status**, and **strategy category**.

2. **Data Handling**:
   - Use Kotlin **data classes** for structured models (`TradeEntry`, `JournalRecord`).
   - Implement **immutability** using `val` instead of `var` where possible.
   - Avoid primitive types for trade metadata; encapsulate data into composite classes.

3. **Validation**:
   - Ensure trade entries **require mandatory fields** before saving.
   - Validate **date formatting** to keep historical records consistent.

---

### **Architecture Patterns**
1. **MVVM for State Management**:
   - **ViewModel Layer** should manage trade data retrieval from Firestore.
   - **Repository Layer** must abstract Firebase and local storage handling.
   - Use **StateFlow** or **LiveData** for UI state updates.

2. **Navigation & UI Structure**:
   - **Bottom Navigation Bar** must include quick access to **Home, Trade Entry, History, Performance**.
   - **Navigation Drawer (Burger Menu)** must provide secondary options like **Settings, Backup, Filters**.

3. **State Management**:
   - UI states should be handled with a **TradeUiState class** consolidating loading, success, and error states.

---

### **UI Guidelines**
1. **Jetpack Compose Principles**:
   - Use **Composable functions** to create reusable trade entry forms, filters, and analytics views.
   - Prioritize **lazy loading** for trade history lists (`LazyColumn`).
   - Implement **Material Design 3** for modern UI styling.

2. **Dark Mode Compatibility**:
   - Default theme should **mimic TradingView’s dark UI**.
   - Provide an option to toggle **light mode** inside settings.

3. **Performance Analytics Page**:
   - Must include **charts, win/loss tracking, and monthly trading trends**.
   - Use **Jetpack Compose's Canvas API** for trade performance graphs.

---

### **Firebase & Async Operations**
1. **Firestore Storage**:
   - Save **trade descriptions and metadata** in Firestore.
   - Store **only image paths** instead of raw image data.

2. **Local Storage Handling**:
   - Save trade screenshots in `/storage/emulated/0/TradingJournal/`.
   - Ensure **efficient retrieval** by storing filenames inside Firestore.

3. **Kotlin Coroutines for Async Tasks**:
   - Trade data fetching should be **non-blocking using Coroutines**.
   - UI must **observe data flow from Firestore asynchronously**.

---

### **Testing Standards**
1. **Unit Tests**:
   - Test **Firestore queries** ensuring trade data retrieval is correct.
   - Validate **image saving and retrieval logic** from local storage.

2. **Integration Tests**:
   - Test **Firestore syncing** and **search/filter functions**.

---

### **Best Practices for Scalability**
1. **Modularize UI Components** into packages (`ui.trade_entry`, `ui.analytics`).  
2. **Minimize API Calls** to Firebase by caching recent trades locally.  
3. **Follow SOLID Principles** to ensure clean architecture.  

