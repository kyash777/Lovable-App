# AI Generation Flow - Visual Quick Reference

## SIMPLE ANALOGY: Restaurant Order

```
PHASE 1: Taking Order (Streaming)
┌─────────────┐
│   Customer  │  "I want a red button"
│             │
└──────┬──────┘
       │
       ↓
┌──────────────────┐
│   Chef (LLM)     │  Starts cooking, tells progress:
│                  │  "Making sauce..." → Tell customer NOW
│                  │  "Adding code..." → Tell customer NOW
└────────┬─────────┘
         │ (Stream updates to customer)
         │
         ↓
    Customer hears updates in real-time ✓

PHASE 2: Storing Ingredients (Async File Saving)
While chef is cooking:
  ├─ Store sauce recipe in cookbook (Database)
  ├─ Store actual sauce in fridge (MinIO)
  └─ Customer doesn't wait for this! ✓

PHASE 3: Serving (Frontend Preview)
Chef done → Waiter gets dish → Serves to customer ✓
(Files ready → Frontend fetches → Shows in preview)

KEY: Customer never waits for storage! They get food updates while chef stores ingredients.
```

---

## TECHNICAL FLOW: What Happens When User Asks "Create Button"

```
┌─────────────────────────────────────────────────────────────────┐
│ TIMELINE: T = 0 to 2 seconds                                    │
└─────────────────────────────────────────────────────────────────┘

T=0ms    USER INPUT
         ┌─────────────────────────┐
         │ "Make a red button"     │
         └────────────┬────────────┘
                      │
                      ↓
         POST /api/chat/stream

────────────────────────────────────────────────────────────────────

T=10ms   SERVER RECEIVES REQUEST
         ChatController.streamChat()
         │
         └─→ Call AiGenerationService.streamResponse()
             │
             │ Add system prompt:
             │ "You are React 18 expert, use Tailwind, daisyUI..."
             │
             └─→ Send to LLM via OpenRouter

────────────────────────────────────────────────────────────────────

T=50ms   LLM STARTS RESPONDING (STREAMED)
         
         Response comes in CHUNKS:

         CHUNK 1 (T=60ms):
         "<message>I'll create a red button for you</message>"
         
         ↓ Send to browser IMMEDIATELY
         
         ┌────────────────────────────────┐
         │ Browser Chat                   │
         │ "I'll create a red button..."  │
         │                                │
         └────────────────────────────────┘

         CHUNK 2 (T=100ms):
         "<file path=\"src/components/Button.jsx\">"
         
         CHUNK 3 (T=150ms):
         "import React from 'react';"
         "export const Button = () => ("
         "  <button className=\"btn-error\">"
         "    Click me"
         "  </button>"
         ");"
         "</file>"
         
         ↓ Send to browser IMMEDIATELY
         
         ┌────────────────────────────────┐
         │ Browser Chat                   │
         │ "I'll create a red button...   │
         │ <file path="src/..."           │
         │ import React..."               │
         │ ...                            │
         │                                │
         └────────────────────────────────┘

         CHUNK 4 (T=200ms):
         "<message>Done! Your red button is ready</message>"
         
         ↓ Send to browser
         
         ┌────────────────────────────────┐
         │ Browser Chat                   │
         │ "I'll create a red button...   │
         │ <file path="src/..."           │
         │ import React..."               │
         │ Done! Your red button ready    │
         │                                │
         └────────────────────────────────┘

────────────────────────────────────────────────────────────────────

T=210ms  STREAMING COMPLETE
         .doOnComplete() triggered
         │
         └─→ Schedule async task:
             Schedulers.boundedElastic().schedule(() -> {
               parseAndSaveFiles(fullResponse);
             });
             
         ⚠️  KEY POINT: User already got response!
         File saving happens in background

────────────────────────────────────────────────────────────────────

T=210-250ms  FILE EXTRACTION (Background)
             
             parseAndSaveFiles() extracts:
             ├─ File path: "src/components/Button.jsx"
             └─ File content: "import React from 'react'..."

────────────────────────────────────────────────────────────────────

T=250-300ms  SAVE TO MINIO
             
             Generate object key: "projectId/src/components/Button.jsx"
             Save file content to MinIO server
             
             MinIO:
             ┌──────────────────────────┐
             │ project-bucket/123/      │
             │ └─ src/                  │
             │    └─ components/        │
             │       └─ Button.jsx  ←── [Binary data stored]
             └──────────────────────────┘

────────────────────────────────────────────────────────────────────

T=300-320ms  SAVE METADATA TO DATABASE
             
             INSERT INTO project_files:
             ┌─────────────────────────────────────────┐
             │ id │ project_id │ path                  │
             ├────┼────────────┼───────────────────────┤
             │ 1  │ 123        │ src/components/       │
             │    │            │ Button.jsx            │
             ├────┼────────────┼───────────────────────┤
             │ minio_object_key: "123/src/components/  │
             │                    Button.jsx"          │
             ├────┼────────────┼───────────────────────┤
             │ created_at: 2026-06-11T14:30:20Z        │
             └─────────────────────────────────────────┘

────────────────────────────────────────────────────────────────────

T=320ms  FILE SAVING COMPLETE ✓

────────────────────────────────────────────────────────────────────

T=330ms  FRONTEND DECIDES TO FETCH FILES
         
         Browser JavaScript:
         fetch('/api/projects/123/files')
         
         Response (from database):
         [
           {
             id: 1,
             projectId: 123,
             path: "src/components/Button.jsx",
             minioObjectKey: "123/src/components/Button.jsx"
           }
         ]

────────────────────────────────────────────────────────────────────

T=340ms  FRONTEND FETCHES FILE CONTENT
         
         Browser JavaScript:
         fetch('/api/projects/123/files/src/components/Button.jsx')
         
         Response (from MinIO via server):
         {
           content: "import React from 'react';\nexport const Button..."
         }

────────────────────────────────────────────────────────────────────

T=350ms  VITE HMR INJECTS CODE
         
         Browser Vite development server:
         ├─ Receives new file content
         ├─ Injects into running React app
         ├─ Hot Module Replacement (HMR)
         └─ Re-renders Button component
             WITHOUT page reload ✓

────────────────────────────────────────────────────────────────────

T=360ms  USER SEES RESULT 🎉
         
         ┌──────────────────────────────┐
         │ Chat on Left   │ Preview     │
         ├────────────────┼─────────────┤
         │ "I'll create   │   ┌──────┐  │
         │  a red button" │   │ Click│  │ ← RED BUTTON appears!
         │ ...            │   │ me   │  │   (Styled with daisyUI)
         │ Done!          │   └──────┘  │
         │                │             │
         └────────────────┴─────────────┘
```

---

## WHY THIS ARCHITECTURE? (3 Key Reasons)

### 1️⃣ PERFORMANCE: Don't Block User
```
BAD (Synchronous):
  User sends prompt
       ↓ (waiting...)
  LLM generates code
       ↓ (waiting...)
  Save to MinIO
       ↓ (waiting...)
  Save to Database
       ↓ (waiting...)
  Response sent to user
  Total: ~500ms to 1s (SLOW!)

GOOD (Async Streaming):
  User sends prompt
       ↓ (takes ~50ms)
  LLM generates code + Stream chunks
       ↓ (user sees in ~100ms) ← FAST!
  Response received by user (T=200ms)
       ↓ 
  (user already reading chat, not waiting)
       ↓
  In background: File saving happens (T=250-320ms)
  Total perceived latency: ~100ms (3-5x FASTER!)
```

### 2️⃣ SCALABILITY: Separation of Concerns
```
Database (PostgreSQL):
├─ Good at: Fast metadata queries
├─ Fast at: "Get all files in project 123"
├─ Bad at: Storing 100MB+ of file content
└─ Cost: Expensive storage

MinIO (Object Storage):
├─ Good at: Large file storage
├─ Fast at: Streaming file downloads
├─ Cheap at: Terabytes of storage
└─ Cost: Much cheaper than DB

Result: Use RIGHT tool for RIGHT job ✓
```

### 3️⃣ RAG (Context for Future Prompts)
```
Why keep files?

Iteration 1:
  User: "Create a button"
  LLM: [generates Button.jsx]
  Saved ✓

Iteration 2:
  User: "Make button blue"
  Backend fetches current Button.jsx from MinIO
  Includes in next prompt:
  "Current code: [actual Button.jsx content here]"
  LLM sees current code → Makes incremental edits ✓
  
Result: Smarter, context-aware code generation!
```

---

## Quick Checklist: What Happens

```
✓ User sends prompt
  └─→ Input goes to LLM

✓ LLM responds with chunks
  └─→ Each chunk sent to browser IMMEDIATELY
      (User sees code in chat)

✓ Streaming completes
  └─→ doOnComplete() triggered

✓ Background thread extracts files
  └─→ Regex: <file path="...">content</file>

✓ Files saved to MinIO
  └─→ Actual code stored there

✓ Metadata saved to DB
  └─→ Index: project_id, path, minio_object_key

✓ Frontend fetches file tree from DB
  └─→ Gets list of files

✓ Frontend fetches file content from MinIO
  └─→ Gets actual code

✓ Vite HMR injects code
  └─→ Live preview updates
      WITHOUT page reload

✓ User sees result ✓
```

---

## The Architecture Pattern (Important!)

```
┌─────────────────────────────────────────────────────┐
│ RESPONSE LAYER (Fast, Streaming)                   │
├─────────────────────────────────────────────────────┤
│  ChatController                                     │
│  ↓                                                  │
│  AiGenerationServiceImpl.streamResponse()            │
│  ↓                                                  │
│  Return Flux<ServerSentEvent<String>>              │
│  (Stream to browser immediately)                    │
└──────────────────┬──────────────────────────────────┘
                   │
      ┌────────────┴────────────┐
      │  doOnComplete()         │
      │  (After streaming)      │
      ↓                         ↓
┌──────────────────────────────────────────────────────┐
│ BACKGROUND LAYER (Async, No user waiting)           │
├──────────────────────────────────────────────────────┤
│  Schedulers.boundedElastic()                        │
│  ↓                                                  │
│  parseAndSaveFiles()                                │
│  ↓                                                  │
│  ├─ Extract files (regex)                           │
│  ├─ Save to MinIO (file storage)                    │
│  └─ Save to Database (metadata)                     │
└──────────────────────────────────────────────────────┘
                   │
      ┌────────────┴────────────┐
      │ Files Ready             │
      ↓                         ↓
┌──────────────────────────────────────────────────────┐
│ RETRIEVAL LAYER (When frontend asks)                │
├──────────────────────────────────────────────────────┤
│  FileController                                     │
│  ├─ GET /files → Query Database (metadata)          │
│  └─ GET /files/{path} → Fetch from MinIO            │
│  ↓                                                  │
│  Return to Frontend                                 │
└──────────────────────────────────────────────────────┘
                   │
                   ↓
┌──────────────────────────────────────────────────────┐
│ PREVIEW LAYER (Frontend, Browser)                   │
├──────────────────────────────────────────────────────┤
│  Frontend React App                                 │
│  ├─ Parse files from API response                   │
│  ├─ Feed to Vite HMR engine                         │
│  ├─ Update running React components                 │
│  └─ Re-render live preview WITHOUT reload           │
└──────────────────────────────────────────────────────┘
```

---

## Code Example: Where Each Piece Happens

```java
// ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
// PHASE 1: STREAMING (FAST, to user immediately)
// ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━

@PostMapping("/api/chat/stream")  // ← User hits this
public Flux<ServerSentEvent<String>> streamChat(@RequestBody ChatRequest request) {
    // Call service to stream response
    return aiGenerationService.streamResponse(request.message(), request.projectId())
        .map(data -> ServerSentEvent.<String>builder()
            .data(data)  // ← Each chunk sent to browser NOW
            .build());
}

public Flux<String> streamResponse(String userMessage, Long projectId) {
    StringBuilder fullResponseBuffer = new StringBuilder();
    
    return chatClient.prompt()
        .system(PromptUtils.CODE_GENERATION_SYSTEM_PROMPT)
        .user(userMessage)
        .stream()
        .chatResponse()
        
        // As response chunks arrive:
        .doOnNext(response -> {
            String content = response.getResult().getOutput().getText();
            fullResponseBuffer.append(content);  // Accumulate full response
        })
        
        // When streaming ENDS:
        .doOnComplete(() -> {
            // ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
            // PHASE 2: FILE SAVING (ASYNC, user doesn't wait)
            // ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
            
            Schedulers.boundedElastic().schedule(() -> {
                parseAndSaveFiles(fullResponseBuffer.toString(), projectId);
                //                         ↑ Now we have full response
                //                         Parse and save in background
            });
        })
        
        // Stream each chunk:
        .map(response -> response.getResult().getOutput().getText());
}

private void parseAndSaveFiles(String fullResponse, Long projectId) {
    // Extract: <file path="...">content</file>
    Matcher matcher = FILE_TAG_PATTERN.matcher(fullResponse);
    
    while (matcher.find()) {
        String filePath = matcher.group(1);      // "src/Button.jsx"
        String fileContent = matcher.group(2);   // "import React..."
        
        projectFileService.saveFile(projectId, filePath, fileContent);
        //                                    ↓
        //        ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
        //        Saves to MinIO + Database
        //        ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
    }
}

// ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
// PHASE 3: RETRIEVAL (When frontend asks)
// ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━

@GetMapping("/api/projects/{projectId}/files")  // Get file tree
public ResponseEntity<List<FileNode>> getFileTree(@PathVariable Long projectId) {
    // Query database (fast metadata lookup)
    List<ProjectFile> files = projectFileRepository.findByProjectId(projectId);
    return ResponseEntity.ok(projectFileMapper.toListOfFileNode(files));
}

@GetMapping("/api/projects/{projectId}/files/{*path}")  // Get file content
public ResponseEntity<FileContentResponse> getFile(@PathVariable Long projectId, @PathVariable String path) {
    // Fetch from MinIO
    return ResponseEntity.ok(projectFileService.getFileContent(projectId, path, userId));
}

// ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
// PHASE 4: PREVIEW (Frontend, Browser)
// ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
// Happens in browser:
// 1. Fetch file tree: GET /api/projects/123/files
// 2. For each file: GET /api/projects/123/files/src/Button.jsx
// 3. Vite HMR injects code
// 4. React re-renders → Live preview updates
```

---

## Summary Table

| Component | What | Where | When | Why |
|-----------|------|-------|------|-----|
| **Streaming** | Chat chunks | ChatController | T=0-200ms | User sees progress |
| **Extraction** | Parse regex | AiGenerationServiceImpl | T=200-250ms | Background async |
| **MinIO Save** | File content | ProjectFileServiceImpl | T=250-300ms | Scalable storage |
| **DB Save** | Metadata | ProjectFileServiceImpl | T=300-320ms | Quick queries |
| **File Tree** | List files | FileController | T=330ms | Frontend requests |
| **File Content** | Code data | FileController | T=340ms | Frontend requests |
| **HMR** | Inject code | Browser (Vite) | T=350ms | Live preview |
| **Result** | Updated UI | Browser | T=360ms | User sees button |

---

## KEY TAKEAWAY

```
The genius of this architecture:

1. Streaming Response → Fast user feedback (feels snappy)
2. Async File Saving → Doesn't block streaming (responsive)
3. MinIO + Database → Right tool for right job (scalable)
4. File Retrieval → Context for next iteration (smart)
5. Live Preview → Instant visual feedback (wow!)

Result: Fast + Responsive + Scalable + Smart system!
```

