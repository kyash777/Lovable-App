# System Architecture
<img width="3701" height="2265" alt="ai_design_architecture_f32cec04be" src="https://github.com/user-attachments/assets/7f5bf1fd-ffcf-4c53-88b7-467a20ab0782" />

# High Level Architecture
<img width="3114" height="890" alt="mermaid-diagram" src="https://github.com/user-attachments/assets/ca4d830d-63fd-46d0-82be-93033ac9f039" />

# Complete AI Code Generation Flow - Detailed Explanation

## The Two-Phase Flow

### PHASE 1: Streaming Response (What You Already Know)
```
┌─────────────┐
│   React     │
│  Frontend   │
└──────┬──────┘
       │ 1. User types prompt
       │ "Create a button with red color"
       ↓
┌──────────────────────┐
│  Spring Boot Server  │
│  ChatController      │
└──────┬───────────────┘
       │ 2. Call AiGenerationService.streamResponse()
       ↓
┌──────────────────────────────────────────┐
│  AiGenerationServiceImpl                  │
│  - Add system prompt (React 18 config)   │
│  - Send system + user prompt to LLM      │
│  - Stream response back in real-time     │
└──────┬───────────────────────────────────┘
       │ 3. LLM Response (STREAMED CHUNKS):
       │
       │ <message>I'll update your app...</message>
       │ <file path="src/components/Button.jsx">
       │   import React from 'react';
       │   export const Button = () => (
       │     <button className="btn-primary">
       │       Click me
       │     </button>
       │   );
       │ </file>
       │ <message>Done!</message>
       │
       ↓
┌──────────────────────┐
│   React Frontend     │
│  (User sees chunks   │
│   streaming in chat) │
└──────────────────────┘
```

**Phase 1 Purpose**: 
- Show user the LLM thinking process in real-time
- Deliver the code they asked for
- Better UX (feels responsive, not stuck)

---

## PHASE 2: File Extraction & Storage (THE MISSING PIECE!)
```
WHILE/AFTER streaming, in the background:

┌────────────────────────────────────────────────────────────────┐
│  AiGenerationServiceImpl.parseAndSaveFiles()                    │
│  (Runs ASYNCHRONOUSLY via Schedulers.boundedElastic())         │
│                                                                │
│  WHY ASYNC? → Don't block the streaming response to user!      │
└────────────┬─────────────────────────────────────────────────┘
             │
             ├─→ STEP 1: Parse LLM response with REGEX
             │   Pattern: <file path="([^\"]+)\">(.*?)</file>
             │   Extract:
             │   - File path: "src/components/Button.jsx"
             │   - File content: "import React from 'react'..."
             │
             ├─→ STEP 2: Save to MinIO (Object Storage)
             │   Object Key: "{projectId}/src/components/Button.jsx"
             │   Example: "123/src/components/Button.jsx"
             │   
             │   ┌─────────────────────────┐
             │   │   MinIO Server          │
             │   │   (S3-compatible)       │
             │   │                         │
             │   │   project-bucket/       │
             │   │   └── 123/              │
             │   │       ├── src/          │
             │   │       │   ├── App.jsx   │◄──── File CONTENT stored here
             │   │       │   └── ...       │
             │   │       └── ...           │
             │   └─────────────────────────┘
             │
             └─→ STEP 3: Save Metadata to Database
                 Table: project_files
                 ┌────────────────────────────────┐
                 │ id  │ project_id │ path        │
                 ├─────┼────────────┼─────────────┤
                 │ 1   │ 123        │ src/App.jsx │◄─ METADATA stored here
                 │ 2   │ 123        │ src/Button  │
                 └────────────────────────────────┘
                 Plus: minio_object_key, created_at, updated_at
```

---

## PHASE 3: Frontend Retrieves Files & Shows Live Preview
```
┌──────────────────────────────────────────────────────┐
│ Frontend (React App in Browser)                      │
│                                                      │
│ 1. Polls/listens for file updates                    │
│    (Can use WebSocket or periodic API calls)         │
│                                                      │
│ 2. Calls: GET /api/projects/123/files                │
│    Response:                                         │
│    [                                                 │
│      { id: 1, path: "src/App.jsx", ... },            │
│      { id: 2, path: "src/components/Button.jsx" }    │
│    ]                                                 │
│                                                      │
│ 3. For each file, calls:                             │
│    GET /api/projects/123/files/src/components/Button.jsx
│    Returns: { content: "import React...", ... }      │
│                                                      │
│ 4. Vite HMR (Hot Module Replacement) engine:         │
│    - Detects file changes                            │
│    - Injects new code into running app               │
│    - Re-renders WITHOUT full page reload             │
│    - User sees INSTANT updates in preview iframe     │
│                                                      │
│ ┌─────────────────────────────────────────┐          │
│ │     Live Preview Iframe                 │          │
│ │                                         │          │
│ │  [Red Button with text "Click me"]       │          │
│ │  ✓ Updates instantly as files change    │          │
│ │  ✓ No refresh needed                    │          │
│ └─────────────────────────────────────────┘          │
└──────────────────────────────────────────────────────┘
```

---

## Why This Architecture? (KEY INSIGHTS)

### 1. **Separation of Concerns**
```
Database (PostgreSQL)
├─ Fast for metadata queries
├─ Structured data (project_id, path, timestamps)
├─ Query: "Get all files in this project"
└─ NOT suitable for large binary/text content

MinIO (Object Storage)
├─ Fast for file content retrieval
├─ Handles large files efficiently
├─ Scalable (can add more storage)
└─ Use case: Store actual React code files
```

### 2. **Async Processing (Why Schedulers.boundedElastic()?)**
```
WITHOUT Async:
  User sends prompt
    ↓
  Stream response to user (BLOCKED while saving files)
    ↓
  User waits longer ❌

WITH Async (Current):
  User sends prompt
    ↓
  Stream response IMMEDIATELY ✓
  (File saving happens in background thread)
    ↓
  User sees response in chat while files save
  Frontend fetches files when ready ✓
```

### 3. **Template + Circuit Breaker Pattern**
```
From diagram:
  
  template-bucket/
  ├─ package.json (with all deps installed)
  ├─ vite.config.js
  ├─ tailwind.config.js
  ├─ src/index.jsx
  └─ ...other boilerplate

circuit-breaker:
  ├─ Catches errors if file save fails
  ├─ Prevents cascading failures
  ├─ Retries or alerts user
```

---

## Complete Data Flow Timeline

```
T=0s   User: "Create a button"
       ↓
T=0.1s Frontend → ChatController POST /api/chat/stream
       ↓
T=0.2s AiGenerationServiceImpl.streamResponse()
       │
       ├─ Chunk 1: "<message>Creating button...</message>"
       │   → Sent to user immediately ✓
       │
       ├─ Chunk 2: "<file path=\"src/Button.jsx\">..."
       │   → Sent to user immediately ✓
       │
       ├─ Chunk 3: "<message>Done!</message>"
       │   → Sent to user immediately ✓
       │
       └─ doOnComplete() triggered
          │
          └─ Schedulers.boundedElastic().schedule(() -> {
               parseAndSaveFiles(response);  ← NOW file saving starts
             });

T=0.5s While streaming still happening:
       parseAndSaveFiles() runs SEPARATELY:
       ├─ Extract: path="src/Button.jsx", content="..."
       ├─ Save to MinIO
       ├─ Save to ProjectFile table
       └─ Done ✓

T=0.6s Frontend receives full response
       ├─ Streaming complete ✓
       └─ Files still being saved (okay, user can wait a bit)

T=0.7s Frontend calls GET /api/projects/{id}/files
       └─ Gets file tree (from database metadata)

T=0.8s Frontend calls GET /api/projects/{id}/files/src/Button.jsx
       └─ Gets file content (from MinIO)
       └─ Vite HMR injects code
       └─ Live preview updates ✓

T=1.0s User sees: Chat + Updated Button in Preview 🎉
```

---

## The Real Value: RAG (Retrieval-Augmented Generation)

```
Why keep files in the system?

When user asks: "Change button color to blue"

Next iteration:

  AiGenerationServiceImpl:
  ├─ Get file tree from database
  ├─ Read actual file content from MinIO
  ├─ Include in next prompt to LLM:
  │  "Current button.jsx: [actual code here]"
  ├─ LLM can now see what was generated before
  ├─ LLM generates incremental changes
  └─ More accurate code generation!

Example Prompt to LLM:
  "System: React 18 stack...
   File tree: [list of files]
   File contents:
   - src/Button.jsx:
     import React from 'react';
     export const Button = () => (
       <button className=\"btn-primary\">
         Click me
       </button>
     );
   
   User: Change the button color to blue
   
   Your task: Update the file..."

This is why files MUST be persisted!
```

---

## Summary: Why Each Component?

| Component | Purpose | Why Async |
|-----------|---------|-----------|
| **Streaming Response** | Send LLM output to user in real-time | Must be fast, user waiting |
| **File Parsing** | Extract `<file>` tags from response | Parse happens while streaming |
| **MinIO Storage** | Store actual code content | Can take time, doesn't block user |
| **Database Metadata** | Index files for quick queries | Metadata is small, fast to save |
| **Frontend Fetch** | Retrieve files for live preview | Done after streaming completes |
| **Vite HMR** | Inject code changes without reload | Happens client-side, instant |

---






  
