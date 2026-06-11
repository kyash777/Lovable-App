# AI Generation Flow - Visual Diagrams

## Diagram 1: The Complete 4-Phase Flow

```
┌───────────────────────────────────────────────────────────────────────┐
│                         AI CODE GENERATION FLOW                       │
└───────────────────────────────────────────────────────────────────────┘

                              PHASE 1: STREAMING
                           (T=0 to T=200ms)
┌──────────────────────────────────────────────────────────────────────┐
│                                                                      │
│  User                ChatController              AiGenerationService│
│   │                        │                             │          │
│   ├─ "Create button" ─────→│  POST /api/chat/stream     │          │
│   │                        ├───────────────────────────→│          │
│   │                        │    streamResponse()        │          │
│   │                        │                            ├→ LLM    │
│   │                        │                            │          │
│   │  (Chunks arrive)       │  (Send immediately)        │          │
│   │  "I'll create"         │←──────────────────────────┤          │
│   │←───────────────────────┤                            │          │
│   │  "<file path..."       │←──────────────────────────┤          │
│   │←───────────────────────┤                            │          │
│   │  "...</file>"          │←──────────────────────────┤          │
│   │←───────────────────────┤                            │          │
│   │  "Done!"               │←──────────────────────────┤          │
│   │←───────────────────────┤                            │          │
│   │                        │   .doOnComplete()          │          │
│   │                        │   triggered ───────────────→          │
│   │                        │                            │          │
│   ✓ User sees chat         │                            │          │
│     updating real-time     │                            │          │
│                                                                      │
└──────────────────────────────────────────────────────────────────────┘

                    PHASE 2: FILE EXTRACTION (ASYNC)
                       (T=200 to T=320ms)
                  (Doesn't block streaming)
┌──────────────────────────────────────────────────────────────────────┐
│                                                                      │
│  AiGenerationServiceImpl                                              │
│     │                                                                │
│     ├─ Schedulers.boundedElastic()                                  │
│     │  (Run in background thread)                                   │
│     │                                                                │
│     ├─ parseAndSaveFiles(fullResponse)                              │
│     │                                                                │
│     ├─ Regex extraction:                                            │
│     │  Pattern: <file path="([^\"]+)\">(.*?)</file>                │
│     │  Extract: filePath, fileContent                               │
│     │                                                                │
│     └─→ projectFileService.saveFile()                               │
│                                                                      │
│                            ↓                                         │
│            ┌───────────────┴──────────────┐                          │
│            ↓                              ↓                          │
│      ┌──────────────┐            ┌─────────────────┐                │
│      │   MinIO      │            │   Database      │                │
│      ├──────────────┤            ├─────────────────┤                │
│      │ project-     │            │ project_files   │                │
│      │ bucket/123/  │            ├─────────────────┤                │
│      │ src/Button.  │            │ id: 1           │                │
│      │ jsx          │            │ proj_id: 123    │                │
│      │              │            │ path: src/...   │                │
│      │ [CONTENT]    │            │ minio_key: ...  │                │
│      │ import React │            │ created: now    │                │
│      │ ...          │            │                 │                │
│      └──────────────┘            └─────────────────┘                │
│      ✓ Scalable,                 ✓ Quick queries  │                │
│        Fast large               for file tree    │                │
│        files                                      │                │
│                                                                      │
└──────────────────────────────────────────────────────────────────────┘

                      PHASE 3: FRONTEND RETRIEVAL
                       (T=320 to T=380ms)
┌──────────────────────────────────────────────────────────────────────┐
│                                                                      │
│  Frontend (React)          FileController                            │
│    │                           │                                     │
│    ├─ Get file tree:           │                                     │
│    │  GET /api/projects/123/   ├─ Query database                     │
│    │  files                    │ SELECT * FROM project_files        │
│    │◄──────────────────────────┤ WHERE projectId = 123             │
│    │                           │                                     │
│    │ Response:                 │                                     │
│    │ [                         │                                     │
│    │   {                       │                                     │
│    │     id: 1,                │                                     │
│    │     path: "src/Button.jsx"│                                     │
│    │     minioKey: "123/src/..." │                                   │
│    │   }                       │                                     │
│    │ ]                         │                                     │
│    │                           │                                     │
│    ├─ Get file content:        │                                     │
│    │  GET /api/projects/123/   ├─ Fetch from MinIO                  │
│    │  files/src/Button.jsx     │ GET bucket/123/src/Button.jsx     │
│    │◄──────────────────────────┤                                     │
│    │                           │                                     │
│    │ Response:                 │                                     │
│    │ {                         │                                     │
│    │   content:                │                                     │
│    │   "import React from..."  │                                     │
│    │ }                         │                                     │
│    │                           │                                     │
│    ✓ Frontend has files ready  │                                     │
│                                                                      │
└──────────────────────────────────────────────────────────────────────┘

                      PHASE 4: LIVE PREVIEW (HMR)
                       (T=380 to T=410ms)
┌──────────────────────────────────────────────────────────────────────┐
│                                                                      │
│  Frontend (Vite Dev Server)                                          │
│    │                                                                 │
│    ├─ Receive file content                                           │
│    │  "import React from 'react';"                                  │
│    │  "export const Button = () => ..."                             │
│    │                                                                 │
│    ├─ Hot Module Replacement (HMR)                                   │
│    │  Detects file change                                           │
│    │  Injects new code                                              │
│    │  Updates running module                                         │
│    │                                                                 │
│    ├─ React Reconciliation                                           │
│    │  Re-renders only affected components                            │
│    │  Updates DOM                                                    │
│    │  NO PAGE RELOAD NEEDED                                         │
│    │                                                                 │
│    └─→ Live Preview Iframe                                          │
│        ┌─────────────────────────┐                                  │
│        │    [Red Button ✓]       │  ← Updated!                      │
│        │    Click me             │                                  │
│        └─────────────────────────┘                                  │
│                                                                      │
│    ✓ User sees result instantly                                      │
│    ✓ No refresh needed                                               │
│                                                                      │
└──────────────────────────────────────────────────────────────────────┘
```

---

## Diagram 2: Storage Strategy (Why MinIO + Database?)

```
╔════════════════════════════════════════════════════════════════════╗
║               DUAL STORAGE STRATEGY                                ║
╚════════════════════════════════════════════════════════════════════╝

ONE FILE = TWO STORAGE LOCATIONS

┌─────────────────────┐                    ┌────────────────────────┐
│  DATABASE           │                    │  MINIO                 │
│  (PostgreSQL)       │                    │  (Object Storage)      │
├─────────────────────┤                    ├────────────────────────┤
│ project_files       │                    │ project-bucket/        │
│ table               │                    │                        │
│                     │                    │ 123/                   │
│ ┌─────────────────┐ │                    │ ├─ src/                │
│ │ id        │ 1   │ │                    │ │  ├─ App.jsx          │
│ ├─────────────────┤ │                    │ │  ├─ Button.jsx       │
│ │ project_id│ 123 │ │                    │ │  └─ ...              │
│ ├─────────────────┤ │                    │ │                      │
│ │ path      │ src/│ │                    │ 456/                   │
│ │           │ App │ │                    │ ├─ src/                │
│ │           │ .jsx│ │                    │ │  ├─ ...              │
│ ├─────────────────┤ │                    │ │                      │
│ │ minio_    │ 123/│ │  ◄──Reference──►  │ └─ App.jsx             │
│ │ object_   │ src/│ │                    │    [ACTUAL CODE]       │
│ │ key       │ App │ │                    │    import React...     │
│ │           │ .jsx│ │                    │    export const App... │
│ ├─────────────────┤ │                    │    ...                 │
│ │ created_  │ now │ │                    └────────────────────────┘
│ │ at        │     │ │
│ ├─────────────────┤ │                   Why separate?
│ │ updated_  │ now │ │                   
│ │ at        │     │ │                   Database:
│ └─────────────────┘ │                   ✓ Fast queries ("Get all files")
│                     │                   ✗ Slow for large content
│ METADATA STORAGE    │                   ✗ Expensive storage
│ ✓ Indexed for       │                   ✓ Good for: Index/catalog
│   queries           │
│ ✓ Fast lookups      │                   MinIO:
│ ✓ Relationships     │                   ✓ Fast for large files
│ ✓ Transactions      │                   ✓ Cheap storage
│                     │                   ✓ Scalable (terabytes)
│                     │                   ✓ Good for: Content storage
└─────────────────────┘                   
```

---

## Diagram 3: Async Processing (Why Not Block?)

```
╔════════════════════════════════════════════════════════════════════╗
║         SYNCHRONOUS (BAD) vs ASYNCHRONOUS (GOOD)                   ║
╚════════════════════════════════════════════════════════════════════╝

SYNCHRONOUS APPROACH (Blocking):
┌────────────────────────────────────────────────────┐
│ User sends prompt (T=0)                            │
│ │                                                  │
│ ├─ LLM generates (T=50-200ms)                      │
│ │  User waiting... ⏳                              │
│ │                                                  │
│ ├─ Send response (T=200ms)                         │
│ │  User FINALLY sees something                    │
│ │                                                  │
│ ├─ Extract files (T=200-250ms)                     │
│ │  User waiting AGAIN... ⏳⏳                        │
│ │                                                  │
│ ├─ Save to MinIO (T=250-300ms)                     │
│ │  User waiting... ⏳⏳⏳                            │
│ │                                                  │
│ ├─ Save to DB (T=300-320ms)                        │
│ │  User waiting... ⏳⏳⏳⏳                           │
│ │                                                  │
│ └─ Send response (T=320ms) ← FINALLY!              │
│                                                    │
│ Perceived latency: ~320ms                          │
│ User experience: SLOW ❌                           │
└────────────────────────────────────────────────────┘

ASYNCHRONOUS APPROACH (Async Scheduling):
┌────────────────────────────────────────────────────┐
│ User sends prompt (T=0)                            │
│ │                                                  │
│ ├─ LLM generates (T=50-200ms)                      │
│ │  Accumulate in buffer                           │
│ │                                                  │
│ ├─ Send response (T=100-200ms)                     │
│ │  User IMMEDIATELY sees chat ✓                   │
│ │  "I'll create a button..."                      │
│ │                                                  │
│ └─ doOnComplete() triggered                        │
│    │                                               │
│    └─ Schedule async task:                         │
│       Schedulers.boundedElastic()                  │
│       (Run in background thread)                   │
│       │                                            │
│       ├─ Extract files (background)                │
│       │  User NOT waiting ✓                        │
│       │  (Already reading chat)                    │
│       │                                            │
│       ├─ Save to MinIO (background)                │
│       │  User NOT waiting ✓                        │
│       │                                            │
│       └─ Save to DB (background)                   │
│          User NOT waiting ✓                        │
│                                                    │
│ Perceived latency: ~150-200ms                      │
│ User experience: FAST ✓                            │
└────────────────────────────────────────────────────┘

WHY? User doesn't need to wait for file storage.
     They're reading chat, so background work is invisible!
```

---

## Diagram 4: RAG Context in Next Iteration

```
╔════════════════════════════════════════════════════════════════════╗
║    RAG (RETRIEVAL-AUGMENTED GENERATION) IN ACTION                 ║
╚════════════════════════════════════════════════════════════════════╝

ITERATION 1: User asks to create form
─────────────────────────────────────

User: "Create a login form with blue button"
  ↓
LLM generates: <file path="src/LoginForm.jsx">
  import React from 'react';
  export const LoginForm = () => (
    <form>
      <input type="email" placeholder="Email" />
      <input type="password" placeholder="Password" />
      <button className="btn-primary">Login</button>  ← BLUE
    </form>
  );
</file>
  ↓
FILES SAVED to MinIO + Database
  ✓ Content in MinIO
  ✓ Reference in Database


ITERATION 2: User asks to modify form
─────────────────────────────────────

User: "Add email validation"
  ↓
Backend RETRIEVES current file from MinIO:
  (Get LoginForm.jsx from storage)
  ↓
  import React from 'react';
  export const LoginForm = () => (
    <form>
      <input type="email" placeholder="Email" />
      <input type="password" placeholder="Password" />
      <button className="btn-primary">Login</button>  ← KEEPS BLUE!
    </form>
  );
  ↓
Send FULL prompt to LLM with context:
  
  "System: React 18 expert...
   
   Current file content:
   ─────────────────────
   import React from 'react';
   export const LoginForm = () => (
     <form>
       <input type="email" placeholder="Email" />
       <input type="password" placeholder="Password" />
       <button className="btn-primary">Login</button>
     </form>
   );
   
   User request: Add email validation
   ─────────────────────────────────
   
   Please update ONLY the necessary parts,
   keeping the blue button and structure intact."
  ↓
LLM SEES previous code and makes smart edits:
  <file path="src/LoginForm.jsx">
    import React, { useState } from 'react';
    export const LoginForm = () => {
      const [email, setEmail] = useState('');
      const [error, setError] = useState('');
      
      const handleSubmit = (e) => {
        e.preventDefault();
        if (!email.includes('@')) {
          setError('Invalid email');
          return;
        }
      };
      
      return (
        <form onSubmit={handleSubmit}>
          <input 
            type="email" 
            value={email}
            onChange={(e) => setEmail(e.target.value)}
            placeholder="Email" 
          />
          <input type="password" placeholder="Password" />
          {error && <span className="text-error">{error}</span>}
          <button className="btn-primary">Login</button>  ← STILL BLUE!
        </form>
      );
    };
  </file>
  ↓
PERFECT! ✓
✓ Kept blue button
✓ Kept form structure
✓ Added email validation
✓ Smart incremental edit


ITERATION 3: User asks for more
───────────────────────────────

User: "Make password show/hide toggle"
  ↓
Backend retrieves LATEST file (v2 with validation)
  ↓
LLM sees form WITH validation + BLUE BUTTON
  ↓
LLM adds password toggle, keeps validation + blue button
  ↓
Final form: Blue button + Validation + Password toggle
  ✓ Built incrementally
  ✓ Smart context-aware edits
  ✓ Nothing lost


WITHOUT FILE PERSISTENCE:

Iteration 1: Generic form
Iteration 2: User asks to modify
            LLM doesn't see v1
            Generates GENERIC form again ❌
            Lost blue button, forgot structure ❌

Iteration 3: User asks to modify again
            LLM doesn't see v1 or v2
            Generates GENERIC form AGAIN ❌
            Lost EVERYTHING ❌

Result: Frustrating, low quality code! ❌
```

---

## Diagram 5: Error Handling & Circuit Breaker

```
┌──────────────────────────────────────────────────────────┐
│         WHAT IF SOMETHING FAILS?                         │
└──────────────────────────────────────────────────────────┘

SCENARIO 1: MinIO Down
──────────────────────

Streaming Response:        File Saving:
✓ Still works!            ✗ Fails silently (async)
✓ User sees chat          ✓ User NOT blocked
✓ Responsive UX

User sees: Chat OK, Preview doesn't update
(Acceptable - can retry or continue)


SCENARIO 2: Database Down
──────────────────────────

Streaming Response:        File Saving:
✓ Still works!            ✗ Fails (async task)
✓ User sees chat          ✓ User NOT blocked

User sees: Chat OK, File tree doesn't load
(Acceptable - can still stream)


SCENARIO 3: LLM Timeout
────────────────────────

Streaming Response:        
✗ Stream stops
✓ User sees partial response (better than nothing)
✓ Not user's fault


CIRCUIT BREAKER PATTERN:
(Prevents cascading failures)

If save fails:
  ├─ Log error
  ├─ Alert user (Optional)
  ├─ Don't crash entire system
  ├─ User can still chat
  └─ Retry mechanism (in real app)

Result: Graceful degradation ✓
```

---

## Diagram 6: Code Flow Timeline

```
┌────────────────────────────────────────────────────────────────┐
│                    TIMELINE VISUALIZATION                      │
└────────────────────────────────────────────────────────────────┘

Time   ChatController    AiGenerationService    MinIO    Database
│       │                    │                   │           │
0ms     │ POST /chat/stream  │                   │           │
│       ├───────────────────→│                   │           │
│       │                    │                   │           │
50ms    │                    ├─→ LLM             │           │
│       │                    │                   │           │
100ms   │                    ├─→ LLM             │           │
│       │        Chunk 1 ←───┤                   │           │
│       ├──────────────────←──                   │           │
│       │                    │                   │           │
150ms   │                    ├─→ LLM             │           │
│       │        Chunk 2 ←───┤                   │           │
│       ├──────────────────←──                   │           │
│       │                    │                   │           │
200ms   │                    ├─→ LLM             │           │
│       │    Final Chunk ←───┤                   │           │
│       ├──────────────────←──                   │           │
│       │                    │                   │           │
210ms   │              doOnComplete() triggered  │           │
│       │                    │                   │           │
220ms   │              Schedulers.boundedElastic │           │
│       │              (background thread)       │           │
│       │                    │                   │           │
250ms   │              parseAndSaveFiles()       │           │
│       │                    │                   │           │
280ms   │                    ├─────────────────→│           │
│       │                    │  putObject()      │           │
│       │                    │                   ├─ Saved   │
│       │                    │                   │           │
320ms   │                    ├────────────────────────────→│
│       │                    │ INSERT project_files │       │
│       │                    │                   │   ├─ Saved
│       │                    │                   │   │
350ms   │   (User reading chat, not waiting)    │   │
│       │                    │                   │   │
```

---

## Diagram 7: Storage Organization

```
┌─────────────────────────────────────────────────────────────┐
│         HOW FILES ARE ORGANIZED                             │
└─────────────────────────────────────────────────────────────┘

MinIO (Object Storage):
┌────────────────────────────────────────┐
│ project-bucket/                        │
│                                        │
│ 123/  (projectId 123)                  │
│ ├─ src/                                │
│ │  ├─ App.jsx          ← File 1        │
│ │  ├─ Button.jsx       ← File 2        │
│ │  └─ utils.js         ← File 3        │
│ ├─ public/                             │
│ │  └─ index.html       ← File 4        │
│ └─ package.json        ← File 5        │
│                                        │
│ 456/  (projectId 456)                  │
│ ├─ src/                                │
│ │  └─ ...                              │
│                                        │
└────────────────────────────────────────┘

Database (PostgreSQL):
┌────────────────────────────────────────┐
│ project_files table                    │
│                                        │
│ ┌─ ID ┬─ ProjectId ┬─ Path       ┐   │
│ ├──────┼───────────┼─────────────┤   │
│ │ 1   │ 123       │ src/App.jsx │   │
│ │ 2   │ 123       │ src/Button..│   │
│ │ 3   │ 123       │ src/utils.js│   │
│ │ 4   │ 123       │ public/...  │   │
│ │ 5   │ 123       │ package.json│   │
│ │ 6   │ 456       │ src/...     │   │
│ └─────┴───────────┴─────────────┘   │
│                                        │
│ Link (minio_object_key):               │
│ "123/src/App.jsx"  → MinIO location   │
│                                        │
└────────────────────────────────────────┘

Frontend Request:
┌────────────────────────────────────────┐
│ GET /api/projects/123/files            │
│ ↓                                      │
│ Query DB: WHERE projectId = 123        │
│ ↓                                      │
│ Return paths: [                        │
│   "src/App.jsx",                       │
│   "src/Button.jsx",                    │
│   "src/utils.js",                      │
│   "public/index.html",                 │
│   "package.json"                       │
│ ]                                      │
│ ✓ Fast because metadata is indexed     │
│                                        │
│ GET /api/projects/123/files/src/App    │
│ ↓                                      │
│ Get minio_object_key from DB: "123/..  │
│ ↓                                      │
│ Fetch from MinIO using key             │
│ ↓                                      │
│ Return content: "import React..."      │
│ ✓ Fast because content is in MinIO     │
│                                        │
└────────────────────────────────────────┘
```

---

## Diagram 8: Component Interaction

```
┌─────────────────────────────────────────────────────────────┐
│              COMPONENT INTERACTION DIAGRAM                  │
└─────────────────────────────────────────────────────────────┘

                      Frontend
                    (Browser)
                        ▲
                        │
                ┌───────┴────────┐
                │                │
           Streaming        File Retrieval
                │                │
                ├────────────────┘
                │
                ↓
         ChatController
              (Stream Response)
                │
                ↓
      AiGenerationServiceImpl
       (Streaming + File Extract)
                │
        ┌───────┴────────┐
        │                │
        ↓                ↓
    ChatClient       ProjectFileService
     (LLM)          (File Operations)
                        │
                ┌───────┴──────────┐
                │                  │
                ↓                  ↓
           MinioClient      ProjectFileRepository
             (Content)          (Metadata)
                │                  │
                ↓                  ↓
            MinIO            PostgreSQL
           Storage           Database
```

These diagrams should help visualize the complete flow! 🎉

