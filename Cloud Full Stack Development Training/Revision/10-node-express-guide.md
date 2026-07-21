# Node.js & Express — Complete Line-by-Line Guide

This guide is grounded strictly in Aakash's actual course materials: the 5-part `nodejs-courseware` series (Modules 1–18), the `Express_Reference.md` (Modules 01–06), and supplementary gotchas from `nodejs_discussion_qa.md`. It then walks through two real projects from the course code: **acme-node-demo** (raw Node.js fundamentals — no framework) and **Express** (a production-style Express + Mongoose Employee Management System with JWT auth, file upload, email, and Socket.io). Everything below reproduces the actual course text/code with line-by-line explanation — nothing is invented.

Audience note: comparisons are drawn to Python web frameworks (Flask/FastAPI/Django) and Python tooling (pip, asyncio) where it shortens the explanation — generic backend concepts you already know are not over-explained.

---

# PART 1 — Node.js Courseware (Modules 1–18)

## 1. What Node.js Is and Why It Exists

Node.js runs JavaScript **outside the browser** using Google's **V8 engine** (the same JIT-compiling engine that powers Chrome). Where browser JS talks to the DOM (`window`, `document`), Node talks to the OS (`process`, `fs`, `http`). It was created by Ryan Dahl (2009) specifically to avoid the one-thread-per-request model of Apache/Tomcat — Node uses **one JS thread + an event loop** to handle thousands of concurrent connections without blocking. This is the direct Node analog of Python's `asyncio` single-threaded event loop, except async is the *default* posture in Node (callbacks/Promises everywhere) rather than an opt-in `async def` style.

Key global objects available in every Node file with no import:
- `process` — runtime info: `process.version`, `process.platform`, `process.cwd()`, `process.argv`, `process.env`
- `__filename` / `__dirname` — absolute path to the current file / its directory (CJS only; ESM uses `import.meta.url` instead)
- `console`, `setTimeout`, `Math` — shared with browser JS

`node file.js` runs a script; `node` alone opens the **REPL** (Read-Eval-Print Loop), Node's equivalent of the Python interactive shell.

## 2. npm and package.json

npm = Node Package Manager, bundled with Node — the direct analog of `pip` + `requirements.txt`/`pyproject.toml` combined.

```bash
npm init -y            # creates package.json
npm install chalk      # installs a package into node_modules/
npm install --save-dev nodemon   # devDependency — dev-only tool
```

- `package.json` — the manifest: name, version, `main` entry point, `scripts`, `dependencies`, `devDependencies`. Analogous to `pyproject.toml`.
- `dependencies` are needed to run in production; `devDependencies` (nodemon, Jest, ESLint) are dev-only. `npm install --only=production` skips devDependencies (used in Docker builds).
- `node_modules/` and `package-lock.json` are generated; **never commit `node_modules/`** — add to `.gitignore`.
- `npm ci` (clean install) — installs exactly what's in `package-lock.json`, deletes `node_modules` first, fails if the lockfile is out of sync. Always used in CI/CD (deterministic, unlike `npm install` which may update the lockfile).

## 3. The Node.js Module System (CommonJS vs ESM)

Node supports two systems:
- **CommonJS (CJS)** — the original Node system: `require()` / `module.exports`. Synchronous and dynamic — can be called anywhere, even inside an `if`.
- **ES Modules (ESM)** — the modern standard: `import`/`export`. Static — resolved at parse time (enables tree-shaking). Requires `"type": "module"` in `package.json`, or a `.mjs` extension.

```javascript
// math.js  (the module)
const add = (a, b) => a + b
module.exports = { add, subtract, multiply }
```
```javascript
// app.js  (the consumer)
const math = require('./math')
console.log(math.add(5, 3))
```

Path resolution rules for `require`:
```javascript
require('./math')        // same folder
require('../utils/math') // parent folder
require('express')       // node_modules/ (installed package)
require('fs')             // built-in module — no install needed
```

**Module caching**: after a module is first `require()`d, Node caches it — every subsequent `require()` of the same file returns the same cached export object (singleton behavior). This is unlike Python where re-importing is also cached via `sys.modules`, so the behavior is actually quite similar.

You **cannot freely mix** `require` and `import` in the same file. Most backend Node projects (including the course's Express project) still default to CommonJS; ESM is more common in frontend/modern tooling.

## 4. Built-in (Core) Modules

Node ships a standard library with no install needed:

| Module | Purpose |
|---|---|
| `fs` | file system — read/write/delete files |
| `path` | cross-platform path handling |
| `os` | OS info — CPUs, memory, platform |
| `http` / `https` | create servers, make requests |
| `events` | `EventEmitter` pattern |
| `util` | utility functions (`util.promisify`, `util.inspect`) |
| `crypto` | hashing, HMAC, random bytes |

```javascript
const path = require('path')
console.log(path.join(__dirname, 'data', 'file.txt'))
```

## 5. File System Module (`fs`) and CLI Args

```javascript
// Sync (blocks the event loop — fine for CLI tools, bad for servers)
const data = fs.readFileSync('./notes.txt', 'utf8')

// Async (non-blocking — preferred for servers)
fs.readFile('./notes.txt', 'utf8', (err, data) => {
  if (err) { console.error(err.message); return }
  console.log(data)
})
```

Other core `fs` operations covered: `writeFileSync`/`appendFileSync`, `existsSync`, `mkdirSync({ recursive: true })`, `readdirSync`, `unlinkSync`, `renameSync`.

`path` module: `path.join()` (safe cross-platform concatenation — Windows `\` vs POSIX `/`), `path.basename()`, `path.dirname()`, `path.extname()`, `path.parse()`.

**CLI tools** are built on `process.argv` — `['node', 'scriptPath', ...userArgs]`, so `const [,, command, ...args] = process.argv` extracts real args. The course also covers `yargs` for structured CLI parsing (`yargs(hideBin(process.argv)).command(...)`).

## 6. Debugging Node.js

- Stack traces read **bottom-up**: the deepest frame (closest to where the throw happened) is at the top of the printed trace, the entry point is at the bottom.
- `node inspect app.js` — built-in CLI debugger (`cont`/`c`, `next`/`n`, `step`/`s`, `out`/`o`, `repl`).
- VS Code: `node --inspect app.js` (auto-attach) or a `.vscode/launch.json` config + F5.
- `node --inspect-brk app.js` + Chrome's `chrome://inspect` gives the full Chrome DevTools debugger for Node code.
- Always check the `err` argument first in callbacks — silently ignoring it is the #1 debugging trap.
- The `debug` npm package gives toggleable namespaced logging (`DEBUG=app:* node app.js`) instead of `console.log` spam — this is how Express itself logs internally.

## 7. Asynchronous Node.js — The Event Loop

This is the conceptual core of the course. The flow:

```
Your Code → Call Stack → Node APIs (delegated to OS/libuv) → Callback Queue → Event Loop → Call Stack
```

When `fs.readFile()` is called: Node registers the callback, hands the actual I/O off to the OS/**libuv** thread pool, keeps executing other code, and when the I/O completes, queues the callback for the event loop to pick up once the call stack is empty. This is *why* Node can serve thousands of concurrent connections on one thread — nothing sits idle waiting.

Per the discussion Q&A supplement: **libuv** is the C library that actually owns the event loop and the thread pool (default 4 threads) — the event loop is libuv's, not V8's. The loop runs in phases: **timers → I/O callbacks → idle/prepare → poll → check (`setImmediate`) → close callbacks**, with microtasks (Promises, `process.nextTick`) drained after each phase.

Gotcha pairs frequently tested:
- `process.nextTick()` vs `setImmediate()`: `nextTick` runs before the event loop advances to the next phase at all (fires soonest, but can starve I/O if used recursively); `setImmediate` runs in the check phase, after I/O callbacks.
- `setTimeout(fn, 0)` vs `setImmediate(fn)`: both mean "ASAP," but from inside an I/O callback, `setImmediate` always fires before `setTimeout(fn, 0)`.

Async pattern evolution:
1. **Callbacks** (error-first convention: `(err, result) => {}`) → prone to **callback hell** (the pyramid of doom) with deep nesting.
2. **Promises** — `fs.readFile` promisified via `require('fs').promises`, `.then()/.catch()` chaining.
3. **async/await** — syntactic sugar over Promises; `await` only pauses the enclosing async function, not the whole event loop, so other requests still get served (this is Node's version of Python's `await` inside `async def`).

```javascript
async function combineFiles() {
  try {
    const [file1, file2, file3] = await Promise.all([
      fs.readFile('file1.txt', 'utf8'),
      fs.readFile('file2.txt', 'utf8'),
      fs.readFile('file3.txt', 'utf8'),
    ])
    await fs.writeFile('combined.txt', file1 + file2 + file3)
  } catch (err) { console.error(err.message) }
}
```

Promise utilities: `Promise.all` (parallel, fails fast if any reject), `Promise.allSettled` (parallel, returns per-item success/failure), `Promise.race` (first to settle, win or lose), `Promise.any` (first success, ignores rejections).

`await` inside a `for` loop is **sequential** (each iteration waits for the previous); to parallelize, use `Promise.all(items.map(asyncFn))`.

**EventEmitter** — the foundation of Node's event-driven architecture (streams, HTTP servers extend it):
```javascript
class OrderSystem extends EventEmitter {
  placeOrder(item) { this.emit('order:placed', { item, time: new Date() }) }
}
orders.on('order:placed', data => console.log(`Email sent for: ${data.item}`))
```

**Streams** — process data chunk-by-chunk rather than loading everything into memory (critical for large files):
```javascript
const readStream = fs.createReadStream('huge-file.txt', 'utf8')
readStream.on('data', chunk => console.log('Got chunk of', chunk.length))
readStream.on('end', () => console.log('Done!'))
// Or piping directly:
fs.createReadStream('input.txt').pipe(fs.createWriteStream('output.txt'))
```
Four stream types (per the Q&A supplement): Readable, Writable, Duplex (both), Transform (modifies data in transit).

**Buffer** — Node's class for raw binary data (images, network packets); unlike JS strings (UTF-16), Buffers hold raw bytes.

## 8. Web Servers — Raw HTTP and Express

Raw `http` module (verbose — motivates Express):
```javascript
const server = http.createServer((req, res) => {
  res.setHeader('Content-Type', 'text/plain')
  if (req.url === '/') res.end('Welcome')
  else { res.statusCode = 404; res.end('Not found') }
})
server.listen(3000)
```

Express wraps this:
```javascript
const app = express()
app.use(express.json())            // parse JSON bodies into req.body
app.get('/users/:id', (req, res) => res.json({ userId: req.params.id }))
app.post('/users', (req, res) => res.status(201).json({ message: 'created' }))
app.use((req, res) => res.status(404).json({ error: 'Route not found' }))  // catch-all, must be last
app.listen(process.env.PORT || 3000)
```

**Middleware** — functions run between request and response, signature `(req, res, next)`. Calling `next()` passes control forward; not calling it (without sending a response) hangs the request. This is Node/Express's version of Flask's `before_request`/`after_request` hooks or WSGI middleware, except in Express middleware is explicit and chained per-route rather than implicitly wrapping the WSGI app.

```javascript
const logger = (req, res, next) => {
  console.log(`→ ${req.method} ${req.url}`)
  res.on('finish', () => console.log(`← ${res.statusCode}`))
  next()  // CRITICAL
}
const requireAuth = (req, res, next) => {
  if (!req.headers.authorization) return res.status(401).json({ error: 'Auth required' })
  req.user = { id: 1 }
  next()
}
```

Static file serving: `app.use(express.static(path.join(__dirname, 'public')))`.

**Router** for organizing routes by resource — `express.Router()` creates a mini sub-app, mounted with `app.use('/api/users', usersRouter)`.

**Error-handling middleware** takes **4 params** — `(err, req, res, next)`. Express recognizes it by arity and routes `next(err)` calls to it. Must be registered **last**:
```javascript
app.use((err, req, res, next) => {
  console.error(err.stack)
  res.status(err.statusCode || 500).json({ error: err.message || 'Internal Server Error' })
})
// trigger from a route:
app.get('/risky', (req, res, next) => {
  try { throw new Error('broke') } catch (err) { next(err) }
})
```

`dotenv` loads `.env` into `process.env` — `require('dotenv').config()` at the very top of the entry file. Never commit `.env`.

## 9. Accessing the API from a Browser — CORS

**CORS** (Cross-Origin Resource Sharing) is the browser's mechanism for blocking cross-origin requests (different protocol/domain/port = different origin) unless the server opts in.

```javascript
app.use(cors())  // dev: allow all
app.use(cors({ origin: ['http://localhost:5500'], methods: ['GET','POST'], allowedHeaders: ['Content-Type','Authorization'] }))
```

Query params (`req.query`) power filtering from the frontend: `req.query.done`, `req.query.search`. `fetch` requires manually checking `res.ok`; `axios` throws automatically on non-2xx.

## 10. Application Deployment

- Always read the port from `process.env.PORT` — hosting platforms assign it dynamically.
- `package.json` needs a `"start": "node server.js"` script (and `"dev": "nodemon server.js"` for local hot-reload).
- `.gitignore` must include `node_modules/` and `.env`.
- Deploy flow to Render: connect GitHub repo → Build Command `npm install` → Start Command `npm start` → set env vars in the dashboard.
- Production hardening: `helmet()` (security headers), `express-rate-limit` (abuse prevention), `compression()` (smaller responses), **PM2** as a process manager (`pm2 start server.js --name app`, auto-restarts on crash, `pm2 monit`, cluster mode via `--instances max`).
- **Graceful shutdown** (from Q&A): listen for `SIGTERM` and finish in-flight requests before exiting — `process.on('SIGTERM', () => server.close(() => { db.disconnect(); process.exit(0) }))`.

## 11. MongoDB and Promises (Native Driver)

MongoDB = NoSQL document database. Mapping: Database↔Database, Table↔Collection, Row↔Document, Column↔Field, JOIN↔`$lookup`/embedding. Both MongoDB and Node speak JSON natively — no ORM translation layer needed (contrast with e.g. Django ORM mapping Python objects to SQL rows).

```javascript
const { MongoClient } = require('mongodb')
const client = new MongoClient(process.env.MONGO_URI)
await client.connect()
const db = client.db('myapp')
```

CRUD with the native driver: `insertOne`/`insertMany`, `find({}).toArray()`, `findOne({ _id: new ObjectId(id) })`, `updateOne(filter, { $set: {...} })`, `deleteOne(filter)`. `ObjectId` is Mongo's auto-generated unique ID — must wrap string IDs with `new ObjectId(id)` when querying.

Query operators: `$gt`/`$gte`/`$lt`/`$in`/`$ne` (comparison), `$and`/`$or` (logical), `$all`/`$elemMatch` (arrays), `$text`/`$search` (text index search).

Keep DB logic in a **service layer**, not directly in route handlers.

## 12. REST APIs and Mongoose

Mongoose is an **ODM** (Object Document Mapper) — wraps the native driver and adds Schemas (shape of documents), Models (collection interface classes), Validation, Middleware/hooks, and Virtuals (computed properties).

```javascript
const userSchema = new mongoose.Schema({
  name: { type: String, required: [true, 'Name is required'], trim: true, minlength: 2 },
  email: { type: String, required: true, unique: true, lowercase: true, match: [/^\S+@\S+\.\S+$/, 'invalid'] },
  role: { type: String, enum: ['user', 'admin', 'moderator'], default: 'user' },
  tags: [String],
  address: { city: String, state: String }
}, { timestamps: true })   // auto createdAt/updatedAt
```

CRUD: `new Model(data).save()` or `Model.create(data)` (both trigger `pre('save')` hooks); `Model.find(filter)`, `Model.findById(id)`, `Model.findByIdAndUpdate(id, updates, { new: true, runValidators: true })` (**always pass `new: true`** to get the post-update doc back), `Model.findByIdAndDelete(id)`, `Model.countDocuments(filter)`.

**Mongoose middleware/hooks** — `pre`/`post` on operations like `save`, `findOneAndDelete`:
```javascript
userSchema.pre('save', async function(next) {
  if (this.isModified('password')) this.password = await bcrypt.hash(this.password, 8)
  next()
})
```

**`populate()`** joins referenced documents: `Task.findById(id).populate('owner', 'name email')` replaces an ObjectId ref with the actual referenced document (projected fields only).

**Custom validators**: `validate: { validator: fn, message: '...' }`.

*(Note: the actual Employee/Department/Project Mongoose models and `db.js` connection setup used in the Express project below are covered in a separate MongoDB-focused guide — this guide references their usage in routes but does not re-explain schema definitions line by line.)*

## 13. API Authentication and Security (JWT)

Authentication = "who are you"; Authorization = "what can you do" — auth always precedes authz.

**JWT flow**: user logs in with email+password → server verifies → server signs a JWT (`header.payload.signature`, each Base64URL-encoded) → client stores it and sends it as `Authorization: Bearer <token>` on every request → server verifies the **signature** — no DB lookup needed for validity (though the course's implementation *does* also check a DB-stored token list, to support logout/invalidation). The payload is encoded, not encrypted — never put secrets in it.

```javascript
const hashed = await bcrypt.hash(plainPassword, 8)      // 8 = salt rounds
const isMatch = await bcrypt.compare('MySecret123', hashed)
```

User model auth methods pattern:
```javascript
userSchema.methods.generateAuthToken = async function() {
  const token = jwt.sign({ _id: this._id.toString() }, process.env.JWT_SECRET, { expiresIn: '7d' })
  this.tokens = this.tokens.concat({ token })
  await this.save()
  return token
}
userSchema.statics.findByCredentials = async (email, password) => {
  const user = await User.findOne({ email })
  if (!user) throw new Error('Invalid email or password')
  const isMatch = await bcrypt.compare(password, user.password)
  if (!isMatch) throw new Error('Invalid email or password')
  return user
}
```

Auth middleware pattern (verify Bearer token + confirm it's still in the user's `tokens[]`, which is what makes logout actually invalidate a token instead of just deleting client-side state):
```javascript
const auth = async (req, res, next) => {
  const token = req.headers.authorization.replace('Bearer ', '')
  const decoded = jwt.verify(token, process.env.JWT_SECRET)
  const user = await User.findOne({ _id: decoded._id, 'tokens.token': token })
  if (!user) return res.status(401).json({ error: 'Token invalid or expired' })
  req.user = user; req.token = token
  next()
}
```

Role-based authorization is a middleware factory: `authorize('admin')` returns a middleware checking `roles.includes(req.user.role)`.

Security checklist package stack: `helmet()` (headers), `express-rate-limit` (esp. tighter limits on `/api/auth`), `express-mongo-sanitize` (blocks NoSQL injection via `$where`/`$gt` in body), `xss-clean` (sanitizes user input).

## 14. Sorting, Pagination, and Filtering

All driven by **query parameters**:

```javascript
// Filtering
if (req.query.done !== undefined) match.done = req.query.done === 'true'

// Sorting — ?sortBy=createdAt:desc
const [field, order] = req.query.sortBy.split(':')
sort[field] = order === 'desc' ? -1 : 1

// Pagination — ?page=2&limit=10
const page = parseInt(req.query.page) || 1
const limit = parseInt(req.query.limit) || 10
const skip = (page - 1) * limit
const [tasks, total] = await Promise.all([
  Task.find(match).sort(sort).skip(skip).limit(limit),
  Task.countDocuments(match)
])
res.json({ data: tasks, pagination: { total, page, limit, totalPages: Math.ceil(total/limit) } })
```

Always **cap `limit`** (e.g. `Math.min(100, ...)`) to prevent abuse via `?limit=99999`. Run the `find` and `countDocuments` in `Promise.all` to execute in parallel rather than sequentially. Field selection/projection via `?fields=title,done` → `.select(fields)`. The course also shows building a reusable `buildQuery(reqQuery, allowedFilters, allowedSortFields)` helper — this is exactly the shape of `queryHelper.js` used in the Express project below.

## 15. File Uploads

`multer` handles `multipart/form-data`. Storage can be disk, memory (for pre-processing), or cloud (Cloudinary/S3 in production).

```javascript
const storage = multer.diskStorage({
  destination: (req, file, cb) => cb(null, 'uploads/'),
  filename: (req, file, cb) => cb(null, `${Date.now()}-${Math.round(Math.random()*1e9)}${path.extname(file.originalname)}`)
})
const fileFilter = (req, file, cb) => {
  const ok = /jpeg|jpg|png|gif|webp/.test(path.extname(file.originalname).toLowerCase()) && /jpeg|jpg|png|gif|webp/.test(file.mimetype)
  cb(ok ? null : new Error('Only image files are allowed!'), ok)
}
const upload = multer({ storage, limits: { fileSize: 5 * 1024 * 1024 }, fileFilter })
```

`upload.single('avatar')` for one file (`req.file`), `upload.array('photos', 10)` for many (`req.files`). Multer-specific errors are instances of `multer.MulterError` (e.g. `LIMIT_FILE_SIZE`) and should be checked in a dedicated error handler. `sharp` is the standard for in-process image resizing (`.resize(200,200,{fit:'cover'}).png({quality:80}).toFile(...)`). Frontend: use `FormData`, and **do not** manually set `Content-Type` — the browser sets it with the correct multipart boundary.

## 16. Sending Emails

`nodemailer` wraps SMTP; `@sendgrid/mail` wraps the SendGrid HTTP API directly. Pattern: build a transporter once, reuse across calls.

```javascript
const transporter = nodemailer.createTransport({
  host: process.env.SMTP_HOST, port: process.env.SMTP_PORT,
  auth: { user: process.env.SMTP_USER, pass: process.env.SMTP_PASS }
})
const sendEmail = async ({ to, subject, html, text }) => {
  const info = await transporter.sendMail({ from: `"MyApp" <${process.env.FROM_EMAIL}>`, to, subject, html, text })
  return info
}
```

**Critical pattern**: don't `await` email sends in the request/response path — fire-and-forget with a `.catch()` so a slow/failed email provider never blocks or breaks the API response:
```javascript
sendWelcomeEmail(user.email, user.name).catch(err => console.error('Failed to send welcome email:', err.message))
res.status(201).json({ user, token })
```

Mailtrap is recommended for dev (fake inbox, avoids spamming real addresses during testing). `node-cron` handles scheduled jobs (`cron.schedule('0 9 * * *', async () => {...})` = daily at 9 AM).

## 17. Testing Node.js

Three levels: unit (single function), integration (multiple units together), E2E (full app as a real user would use it). The course uses **Jest** + **Supertest**.

```bash
npm install --save-dev jest supertest
```

Unit test structure — `describe` groups, `test`/`it` cases, `expect` assertions:
```javascript
describe('Math utils', () => {
  describe('add()', () => {
    test('adds two positive numbers', () => { expect(add(2, 3)).toBe(5) })
  })
  describe('divide()', () => {
    test('throws on division by zero', () => { expect(() => divide(5, 0)).toThrow('Division by zero') })
  })
})
```

Key matchers: `.toBe` (strict `===`), `.toEqual` (deep equality), `.toBeTruthy`/`.toBeFalsy`, `.toBeGreaterThan`, `.toMatch`/`.toContain`, `.toHaveLength`, `.toHaveProperty`, `.toThrow`, and async variants `await expect(fn()).resolves.toBe(...)` / `.rejects.toThrow(...)`.

**Mocking**: replace real dependencies (DB, HTTP, email) with fakes via `jest.mock('../utils/email', () => ({ sendWelcomeEmail: jest.fn().mockResolvedValue(true) }))` — keeps tests fast and deterministic.

**Integration testing with Supertest** — makes HTTP requests directly to the Express `app` object without actually binding a port:
```javascript
const request = require('supertest')
const app = require('../app')
const res = await request(app).post('/api/tasks').set('Authorization', `Bearer ${token}`).send({ title: 'Write tests' }).expect(201)
```

`beforeAll`/`afterAll` for one-time setup/teardown (DB connect/disconnect); `beforeEach`/`afterEach` for per-test setup (clean DB, register a fresh test user). Use a **separate test database** (`MONGO_URI_TEST`).

**Critical architectural pattern for testability**: separate `app.js` (exports the Express `app` without calling `.listen()`) from `server.js` (imports `app`, connects to Mongo, then calls `.listen()`). This lets Supertest import `app` directly with zero network binding — exactly the pattern the Express project below follows.

## 18. Real-Time Web Applications with Socket.io

HTTP is request-response; **WebSockets** provide a persistent bidirectional connection so the server can push data anytime. Socket.io wraps WebSockets with fallbacks/conveniences.

```javascript
const server = http.createServer(app)  // NOT app.listen() directly
const io = new Server(server, { cors: { origin: '*' } })
io.on('connection', (socket) => {
  console.log(`Client connected: ${socket.id}`)
  socket.on('disconnect', () => console.log(`Client disconnected: ${socket.id}`))
})
server.listen(3000)
```

Rooms group sockets: `socket.join(room)`. Emit targets:
- `socket.emit(...)` — to the sender only
- `socket.broadcast.emit(...)` — everyone except sender
- `socket.to(room).emit(...)` — room, except sender
- `io.to(room).emit(...)` — everyone in room, including sender
- `io.emit(...)` — everyone connected

Store minimal ephemeral state on `socket.data`; persist anything durable (chat history) to MongoDB.

## 19. Course Summary Reference

The Node.js course's own recap table (Module 18) maps modules 1–17 to topics: Node basics → modules → fs/CLI → debugging → event loop/async → HTTP/Express → CORS → deployment → MongoDB → Mongoose → JWT/security → pagination → uploads → email → testing → Socket.io. Tools reference includes: `nodemon`, `dotenv`, `morgan` (productivity); `helmet`, `bcryptjs`, `jsonwebtoken`, `express-rate-limit`, `express-mongo-sanitize`, `xss-clean` (security); `mongoose`, `redis` (database); `joi`, `express-validator`, `zod` (validation); `multer`, `sharp`, `cloudinary` (files/media); `nodemailer`, `@sendgrid/mail` (email); `jest`, `supertest` (testing); `socket.io` (real-time); `node-cron`, `bull` (scheduling).

---

# PART 2 — Express Reference (Modules 01–06)

## 20. Express Introduction and Philosophy

Express is a minimal, unopinionated web framework — 20M+ weekly downloads, and the base that NestJS (enterprise-opinionated) and Fastify (performance-focused) build on. Express has exactly **three core concepts**: routes, middleware, request/response objects. Everything else (auth, DB, validation) is composed by you via middleware — much like Flask's minimalism versus Django's batteries-included approach; Express is the Flask of the Node ecosystem, not the Django.

## 21. The Request Object (`req`)

```javascript
app.get('/api/users/:id', (req, res) => {
  req.params.id              // ':id' from the URL path
  req.query.role              // ?role=admin — ALWAYS a string, parseInt() numeric query params
  req.body.name                // from POST/PUT/PATCH — requires express.json() middleware first
  req.headers.authorization
  req.get('Authorization')    // case-insensitive header helper
  req.method, req.path, req.url, req.hostname, req.ip, req.protocol, req.secure
  req.cookies                 // requires cookie-parser middleware
  req.user                    // conventionally set by custom auth middleware
})
```

## 22. The Response Object (`res`)

```javascript
res.status(201).json({ id: 1, name: 'Alice' })
res.status(400).json({ error: 'Bad request' })
res.sendStatus(204)             // 204 No Content, no body
res.send('<h1>Hello</h1>')       // raw HTML
res.download('./report.pdf', 'Q3-Report.pdf')
res.redirect('/login')
res.redirect(301, '/new-url')
res.set('X-Custom-Header', 'value')
res.cookie('token', jwtToken, { httpOnly: true, secure: process.env.NODE_ENV === 'production', maxAge: 7*24*60*60*1000, sameSite: 'strict' })
res.clearCookie('token')
res.render('dashboard', { user: req.user })  // template engine (EJS/Handlebars)
```

## 23. Express Router

```javascript
const router = Router()
router.route('/tasks')
  .get(requireAuth, getAllTasks)
  .post(requireAuth, createTask)
router.route('/tasks/:id')
  .get(requireAuth, getTask).patch(requireAuth, updateTask).delete(requireAuth, deleteTask)
router.use(requireAuth)   // router-level middleware — applies to all routes registered below it in this router
export default router
```

`express.Router()` creates a self-contained mini-app with its own middleware stack, mounted into the main app via `app.use('/prefix', router)`.

## 24. Express Generator

`npx express-generator --no-view myapp` scaffolds a starter project with the conventional folder layout.

## 25. Module 03 — Movie Fan App (Server-Side Rendered Example)

Shows Express + EJS templates + `axios` calling an external API (TMDB) inside a route handler, then `res.render('movies/index', { movies: data.results })`. This is the course's one example of classic server-rendered (non-API) Express usage — relevant conceptually but not used in the Express project below, which is a pure JSON API.

## 26. Module 04 — Production-Ready `app.js` Pattern

This is the canonical middleware stack order the reference teaches, and it directly parallels what the Express project's `src/app.js` does:

```javascript
const app = express();
app.use(helmet());                                    // 1. security headers
app.use(cors({ origin: process.env.ALLOWED_ORIGINS?.split(',') ?? '*' }));  // 2. CORS
app.use(mongoSanitize());                              // 3. NoSQL injection protection
app.use(rateLimit({ windowMs: 15*60*1000, max: 100 })); // 4. rate limit
app.use(express.json({ limit: '10kb' }));               // 5. body parsing (JSON)
app.use(express.urlencoded({ extended: false }));       // 6. body parsing (forms)
app.use(compression());                                 // 7. response compression
app.use((req, res, next) => { console.log(...); next() }); // 8. request logging
app.use('/api/v1', routes);                              // 9. routes
app.use('/health', (req, res) => res.json({ status: 'ok', uptime: process.uptime() }));
app.use(notFound);       // 10. 404 handler
app.use(errorHandler);   // 11. error handler — MUST be last
```

The ordering matters because middleware executes strictly in registration order and each layer assumes the previous ones already ran (e.g., `express.json()` must run before any route reads `req.body`; the error handler must be registered after every route so `next(err)` calls from anywhere upstream reach it).

## 27. Centralized Error Middleware Pattern

```javascript
export const notFound = (req, res) => res.status(404).json({ error: `Route ${req.originalUrl} not found` });
export const errorHandler = (err, req, res, next) => {
  console.error(err.stack);
  if (err.name === 'ValidationError') { /* Mongoose validation → 400 */ }
  if (err.code === 11000) { /* Mongoose duplicate key → 409 */ }
  if (err.name === 'JsonWebTokenError') return res.status(401).json({ error: 'Invalid token' });
  if (err.name === 'TokenExpiredError') return res.status(401).json({ error: 'Token expired' });
  if (err.isOperational) return res.status(err.statusCode ?? 400).json({ error: err.message });
  res.status(500).json({ error: 'Internal server error' });
};
```
This exact error-classification pattern (Mongoose ValidationError, duplicate-key 11000, JWT errors, generic fallback) reappears verbatim in the Express project's `errorHandler.js` (see Section 32).

## 28. Passport.js — Alternative Auth Strategy

Shown as an alternative to hand-rolled JWT middleware: `passport-local` (email+password strategy) and `passport-jwt` (Bearer token strategy), wired via `passport.authenticate('local'/'jwt', { session: false })`. The Express project below does **not** use Passport — it hand-rolls JWT verification in `middleware/auth.js` — but this is worth knowing as the "framework" alternative to the manual pattern.

---

# PART 3 — Project A: `acme-node-demo` (Pure Node.js Fundamentals)

Location: `Code/UI (HTML, CSS, JS, Ts, Node)/node-projects/acme-node-demo/`. This project uses **ESM syntax** (`"type": "module"` in `package.json`, `import`/`export`) rather than CommonJS — note the contrast with the Express project (Part 4), which uses CommonJS `require`/`module.exports` throughout. Both styles appear in the courseware; this project demonstrates the ESM path in practice.

```json
// package.json
1:  {
2:    "name": "acme-node-demo",
...
13:   "type": "module",
14:   "dependencies": {
15:     "mongoose": "9.7.2",
16:     "nodemailer": "9.0.1",
17:     "socket.io": "4.8.3"
18:   },
19:   "devDependencies": {
20:     "jest": "30.4.2"
21:   }
22: }
```
- **Line 13** — `"type": "module"` switches the whole package to ESM: every `.js` file is parsed as an ES module (`import`/`export`), not CommonJS.
- **Line 8** — the `test` script runs Jest with `--experimental-vm-modules`, because native ESM support in Jest is still experimental and needs this flag to work with `import`/`export` test files.

## 29. `calc.js` — Module Export Pattern Exploration (Commented Scratch File)

This file is entirely commented out — it's a teaching scratchpad showing the **four ways to export from an ES module**, left in place for reference/reversion during a live lesson:

```javascript
1:  // // calc.js
...
33: // // 4. one export object
34: // export const calc = {
35: //     addNums: (a, b) => {
36: //         return a + b;
37: //     },
38: //
39: //     subNums: (a, b) => {
40: //         return a + b;
41: //     }
42: // }
```
- **Lines 3–8** — pattern 1: a single `export default` function.
- **Lines 10–20** — pattern 2: named consts plus a single `export { addNums, subNums }` statement at the end.
- **Lines 22–30** — pattern 3: inline `export const` on each declaration (the most common modern style).
- **Lines 33–43** — pattern 4: bundle everything into one exported object (`calc.addNums(...)`) — useful when you want namespacing rather than flat named imports.

This directly illustrates ESM's flexibility (`export default` vs multiple named `export`) versus CommonJS's single `module.exports` object convention taught in Module 3 of the courseware.

## 30. `node-topics.js` — Async/Streams/EventEmitter Scratchpad

Also entirely commented out — a running lesson notebook covering, in order: the classic callback pattern (`readFile` error-first callback demonstrating output ordering 1→2→3, i.e. sync code runs before the async callback fires), `Object.keys()` on an employee object, `Array.filter()` for even numbers, a deliberately buggy `getUserAge(user)` function (accessing `user.profile.age` when `profile` is `undefined` — the exact stack-trace debugging example from Module 5 of the courseware), streams (`readFileSync` vs `createReadStream`/`pipe`), and finally a full `EventEmitter`-based `OrderSystem` class identical to the courseware's Module 6 example. This file's purpose is as a live-coding companion to the Module 5/6/7 lecture content — it mirrors the courseware almost verbatim, confirming that Module 6's EventEmitter pattern was taught hands-on with this exact `OrderSystem`/`orders.on('order:placed', ...)` example.

## 31. `http-demo.js` — Raw HTTP Server (No Framework)

```javascript
1:  import http from 'http';
2:
3:  const PORT = 3000;
4:
...
9:  const server = http.createServer((request, response) => {
10:     console.log(`${request.method} ${request.url}`);
11:     // request.
12:     if (request.url == '/')
13:         response.end('Welcome');
14:     else if (request.url == '/about')
15:         response.end('About page');
16:     else
17:         response.end('404! Page not found!');
18: });
19:
20: server.listen(PORT, () => {
21:     console.log(`Server running at http://localhost:${PORT}`);
22: });
```
- **Line 1** — `http` is a Node core module, imported via ESM `import` syntax (works because `"type": "module"` is set).
- **Line 9** — `http.createServer(callback)`: the callback fires on every incoming HTTP request; it receives raw `request`/`response` objects (no Express convenience methods like `res.json()` exist here — this is the bare-metal API that Express wraps).
- **Line 10** — logs method + URL for every request, useful debugging pattern before any routing logic runs.
- **Lines 12–17** — manual URL-based routing via `if`/`else if`/`else` on `request.url`. Note there's no status code set on the "404" branch (line 17) — it still returns HTTP 200 with a "404!" text body, since `response.statusCode` was never explicitly set to `404`. This is a realistic gotcha: raw `http` defaults to status 200 unless you set it yourself (unlike Express's `res.status(404)` helper).
- **Line 20** — `server.listen(PORT, callback)` binds and starts listening; the callback fires once the socket is bound.

This is the direct hands-on companion to Module 7's "Bare HTTP Server (Raw Node)" example — it is essentially that exact snippet, run for real.

## 32. `server.js` — Raw HTTP Server + Static File Serving + Socket.io

```javascript
1:  import http from 'http';
2:  import fs from 'fs';
3:  import path from 'path';
4:  import { Server } from 'socket.io';
5:
6:  const PORT = 3000;
7:
8:  const server = http.createServer((req, res) => {
9:      console.log('createServer started.');
10:     if (req.url === '/') {
11:         console.log('/ requested.');
12:         const filePath = path.join(
13:             process.cwd(),
14:             'src',
15:             'public',
16:             'index.html'
17:         );
18:
19:         fs.readFile(filePath, 'utf8', (err, data) => {
20:             console.log('html accessed.');
21:             if (err) {
22:                 res.writeHead(500, {
23:                     'Content-Type': 'text/plain'
24:                 });
25:                 res.end('Error loading page');
26:                 console.log('html not found.');
27:                 return;
28:             }
29:
30:             res.writeHead(200, {
31:                 'Content-Type': 'text/html'
32:             });
33:             console.log('html started.');
34:
35:             res.end(data);
36:         });
37:
38:         return;
39:     }
40:
41:     res.writeHead(404, {
42:         'Content-Type': 'text/plain'
43:     });
44:
45:     res.end('Page Not Found');
46: });
47:
48: const io = new Server(server);
49:
50: io.on('connection', (socket) => {
51:     console.log(`Client connected: ${socket.id}`);
52:
53:     socket.on('chat-message', (msg) => {
54:         console.log(`Received: ${msg}`);
55:         io.emit('chat-message', msg);
56:     });
57:
58:     socket.on('disconnect', () => {
59:         console.log(`Client disconnected: ${socket.id}`);
60:     });
61: });
62:
63: server.listen(PORT, () => {
64:     console.log(`Server running at http://localhost:${PORT}`);
65: });
```
- **Line 12–17** — `path.join(process.cwd(), 'src', 'public', 'index.html')` builds an absolute, cross-platform path to a static HTML file. Note it uses `process.cwd()` (the directory Node was *launched from*) rather than `__dirname`/`import.meta.url` — this makes the path resolution dependent on where `node src/server.js` is *run from*, not where the file itself lives. This is a subtle but real distinction the courseware flags: `process.cwd()` vs `__dirname` are not interchangeable.
- **Line 19** — `fs.readFile` (async, non-blocking) reads the HTML file; the callback is error-first, exactly the pattern from Module 5/7 ("always check `err` first").
- **Lines 21–28** — on file-read failure, manually writes a 500 status with `res.writeHead(500, {...})` then `res.end(...)`; note the explicit `return` on line 27 to avoid falling through to line 30's success path.
- **Line 30–35** — on success, `res.writeHead(200, { 'Content-Type': 'text/html' })` then `res.end(data)` streams the HTML back. This is the manual, verbose equivalent of Express's `res.sendFile()`.
- **Line 38** — `return` after handling `/` prevents execution from continuing to the catch-all 404 block below (lines 41–45) — this is the raw-`http` equivalent of Express's implicit "first matching route wins, then stop."
- **Lines 48–61** — this is exactly Module 17's Socket.io setup: `new Server(server)` attaches Socket.io to the *same* HTTP server instance (not `app.listen()` directly — confirming the "must use `http.createServer`, not Express's shortcut" rule from the courseware). `io.on('connection', ...)` fires per client; `socket.on('chat-message', msg => io.emit('chat-message', msg))` implements the simplest possible broadcast chat — every message from any client is re-emitted to **all** connected clients (`io.emit`, not `socket.broadcast.emit`, so the sender also receives their own message echoed back).
- **Line 58–60** — `disconnect` handler logs cleanup; note there's no room/user-tracking here (unlike the fuller courseware chat example in Module 17) — this is a minimal single-room broadcast demo.

## 33. `emp-stuff.js` — Module-with-Tests Pair (Fundamentals)

```javascript
1:  // emp-stuff.js 
2:
3:  const employees = [
4:      { id: 1, name: 'Sonu', salary: 50000 },
5:      { id: 2, name: 'Monu', salary: 60000 },
6:      { id: 3, name: 'Tonu', salary: 70000 }
7:  ];
8:
9:  export const calculateBonus = salary => salary * 0.10;
10:
11: export const getEmployees = () => employees.map(emp => emp.name);
12:
13: export const findEmployee = id => employees.find(emp => emp.id === id);
14:
15: export const addNums = (a, b) => a + b;
```
- **Lines 3–7** — `employees` is a module-scoped constant array — module caching (Section 3) means every `import` of this module shares the exact same array instance (a de facto singleton in-memory "database," same idea as the in-memory array pattern from Module 7/9 of the courseware).
- **Line 9** — `calculateBonus`: single-expression arrow function, no explicit `return` needed (implicit return of `salary * 0.10`).
- **Line 11** — `getEmployees` uses `Array.prototype.map` to project just the `name` field out of each employee object — a pure function with no side effects, ideal for unit testing.
- **Line 13** — `findEmployee` uses `Array.prototype.find`, which returns the **first** matching element or `undefined` if none match — this `undefined` return-on-miss is directly exercised in the test file below.
- **Line 15** — `addNums`, a trivial two-arg adder — used purely as the simplest possible unit-test subject.
- **Lines 18–40 (commented)** — a duplicate, more verbose version of the same four functions using block-bodied arrow functions (`(salary) => { return salary * 0.10; }`) kept as a comparison/rollback reference — showing the same logic in both single-expression and block-bodied arrow function styles.

## 34. `emp-stuff.test.js` — Jest Unit Tests

```javascript
1:  // emp-tests.js
2:  // documentation of matchers - 
3:  // https://jestjs.io/docs/using-matchers
4:
5:  import { calculateBonus, getEmployees, findEmployee, addNums } from './emp-stuff.js';
6:
7:  beforeAll(() => {
8:      console.log('Setup - runs once before all tests');
9:  });
10:
11: afterAll(() => {
12:     console.log('Teardown - runs once after all tests');
13: });
14:
15: beforeEach(() => {
16:     console.log('Setup - runs before each test');
17: });
18:
19: afterEach(() => {
20:     console.log('Teardown - runs after each test');
21: });
22:
23: describe('ems tests suite', () => {
24:
25:     describe('find employee by id tests', () => {
26:
27:         it('given id 1, name shoule be Sonu', () => {
28:             expect(findEmployee(1).name).toBe('Sonu');
29:         });
30:
31:         it('given id 1, name shoule NOT be Monu', () => {
32:             expect(findEmployee(1).name).not.toBe('Monu');
33:         });
34:         it('given id 100, should return undefined', () => {
35:             expect(findEmployee(100)).toBeUndefined();
36:         });
37:
38:     });
39:
40:
41:
42:     describe('demo tests', () => {
43:
44:         it('test addNums', () => {
45:             const sum = addNums(10, 20);
46:             expect(sum).toBe(30);
47:         });
48:
49:         it('test addNums negative', () => {
50:             const sum = addNums(10, 20);
51:             expect(sum).not.toBe(35);
52:         });
53:     });
54:
55: });
```
- **Line 5** — imports the module under test using ESM `import` syntax with an explicit `.js` extension — required in Node ESM (unlike CommonJS `require`, ESM does not auto-resolve extensions).
- **Lines 7–21** — the four Jest lifecycle hooks, all present in one file purely as a teaching demonstration of execution order: `beforeAll`/`afterAll` run once total (bracketing the whole file), `beforeEach`/`afterEach` run around **every** individual `it`/`test`. None of them do real setup here (just `console.log`) — this file exists to let students *watch* the hook execution order in test output, not to demonstrate real fixture management.
- **Line 23** — outer `describe('ems tests suite', ...)` — purely organizational grouping, does not affect execution, just test-report nesting.
- **Lines 25–38** — nested `describe('find employee by id tests', ...)` — demonstrates `describe` blocks can nest arbitrarily.
- **Line 28** — `expect(findEmployee(1).name).toBe('Sonu')` — `.toBe()` is strict equality (`===`); safe here since `.name` is a primitive string.
- **Line 32** — `.not.toBe('Monu')` — demonstrates matcher negation via `.not`.
- **Line 35** — `expect(findEmployee(100)).toBeUndefined()` — directly exercises the "not found" branch of `Array.prototype.find` (Section 33, line 13) — confirms the function returns `undefined` rather than throwing or returning `null`.
- **Lines 44–47, 49–52** — trivial arithmetic assertions on `addNums`, including one **poorly named** test (`'test addNums negative'` at line 49) that doesn't actually test negative numbers — it just re-asserts `10+20 ≠ 35` with `.not.toBe()`. Worth flagging as an example of a misleading test name versus what it actually verifies — a good assessment gotcha to be aware of (test names should describe the *behavior under test*, not just be a variant label).
- **Lines 59–71 (commented)** — a progressive scratchpad showing how `describe`/`it`/`test` calls are typically built up incrementally during a live lesson (empty calls → single test → full body), left in as a teaching artifact.

## 35. `send-email.js` — Nodemailer with Gmail SMTP

```javascript
1:  import nodemailer from 'nodemailer';
2:  import fs from 'fs';
3:  const passwordFile = 'D:/Projects/delete/shridhar-gmail-app-password.txt';
4:  const senderEmail = 'shridhar.javafsd@gmail.com';
5:  const receiverEmail = 'dyesmuk@gmail.com';
6:  const mailSubject = 'Sample Mail';
7:  const mailBody = 'This is sample mail.';
8:  const password = fs.readFileSync(passwordFile, 'utf8').trim();
9:  const transporter = nodemailer.createTransport({
10:     service: 'gmail', auth: { user: senderEmail, pass: password } });
11: const sendDemoMail = async () => {
12:     try {
13:         const info = await transporter.sendMail({
14:             from: senderEmail, to: receiverEmail, subject: mailSubject, text: mailBody
15:         });
16:         console.log('Email sent successfully');
17:         console.log('Message ID:', info.messageId);
18:     } catch (err) { console.error('Failed to send email:', err.message); }
19: }
20: sendDemoMail();
```
- **Line 3** — the Gmail **app password** (not the real account password — Gmail requires a separate 16-char app password for SMTP when 2FA is on) is stored in a plain local text file **outside the repo**, at an absolute Windows path. This is the exact "read credentials from a local file instead of `.env`" pattern also seen in the Express project's `utils/email.js` (Section 39) — a deliberate alternative to environment variables for local dev convenience, though clearly not portable across machines.
- **Line 8** — `fs.readFileSync(passwordFile, 'utf8').trim()` — synchronous read (acceptable here since this is a one-shot script, not a server handling concurrent requests — Module 4's sync-vs-async guidance: sync is fine for CLI/one-off scripts).
- **Line 9–10** — `nodemailer.createTransport({ service: 'gmail', auth: {...} })` — using the `service: 'gmail'` shorthand (Nodemailer knows Gmail's SMTP host/port internally) rather than manually specifying `host`/`port` as the courseware's more generic SMTP example does.
- **Line 11** — `sendDemoMail` is an `async` named function (not actually immediately-invoked in the strict sense, but called once at the bottom on line 20) — wraps the `await transporter.sendMail(...)` call in `try/catch`, following the error-handling convention taught in Module 15/16.
- **Line 20** — the script calls itself at the top level — this is a standalone script meant to be run directly (`node src/send-email.js`), not a reusable module (contrast with the Express project's `utils/email.js`, which exports a function for other files to call — Section 39).

---

# PART 4 — Project B: Express EMS Backend (Main Project)

Location: `Code/Express/`. This is a full production-style Employee Management System REST API — Express + Mongoose + MongoDB, JWT auth, role-based authorization, Multer file uploads, Nodemailer email, Socket.io real-time notifications, and a Jest + Supertest test suite. It uses **CommonJS** (`require`/`module.exports`) throughout, unlike Project A's ESM style. The `db.js` connection setup and the `Employee`/`Department`/`Project` Mongoose models are covered in a separate MongoDB-focused guide — referenced here by name only, not re-explained schema-field-by-field.

```json
// package.json
1:  {
2:    "name": "acme-ems-api",
...
5:    "main": "src/app.js",
6:    "scripts": {
7:      "start": "node src/app.js",
8:      "dev": "nodemon src/app.js",
9:      "test": "jest --runInBand"
10:   },
11:   "dependencies": {
12:     "bcryptjs": "^2.4.3",
13:     "dotenv": "^16.3.1",
14:     "express": "^4.18.2",
15:     "jsonwebtoken": "^9.0.2",
16:     "mongoose": "^8.0.3",
17:     "multer": "^1.4.5-lts.1",
18:     "nodemailer": "^6.9.8",
19:     "socket.io": "^4.6.2",
20:     "validator": "^13.11.0",
21:     "cors": "2.8.6"
22:   },
23:   "devDependencies": {
24:     "jest": "^29.7.0",
25:     "nodemon": "^3.0.2",
26:     "supertest": "^6.3.4"
27:   },
28:   "jest": {
29:     "testEnvironment": "node"
30:   }
31: }
```
- **Line 9** — `jest --runInBand` forces tests to run **serially** in a single process rather than parallel workers — necessary here because all tests share one MongoDB connection and one Express `app` instance; parallel workers would race on shared DB state (e.g., two test files both trying to register the same admin email).
- **Lines 28–30** — Jest config embedded directly in `package.json` (alternative to a separate `jest.config.js`); `testEnvironment: 'node'` (as opposed to `'jsdom'`) is correct for a backend-only project with no DOM to simulate.

## 36. `src/app.js` — Full Middleware Stack and Registration Order

```javascript
1:  // src/app.js
2:  // Demonstrates: Express setup, middleware chain, routing, Web Servers topic,
3:  //               environment config, Node.js module system
4:
5:  require('dotenv').config();
6:  const cors = require('cors');
7:
8:  const express = require('express');
9:  const http = require('http');
10: const { Server } = require('socket.io');
11: const path = require('path');
12:
13: const connectDB = require('./config/db');
14: const errorHandler = require('./middleware/errorHandler');
15: const setupSocketIO = require('./sockets/notifications');
16:
17: // ── Route modules ─────────────────────────────────────────────
18: const authRoutes = require('./routes/authRoutes');
19: const employeeRoutes = require('./routes/employeeRoutes');
20: const departmentRoutes = require('./routes/departmentRoutes');
21: const projectRoutes = require('./routes/projectRoutes');
22:
23: // ── App & HTTP server ─────────────────────────────────────────
24: const app = express();
25: const server = http.createServer(app);
26:
27: // ── Socket.io ─────────────────────────────────────────────────
28: const io = new Server(server, {
29:   cors: { origin: '*' },
30: });
31: setupSocketIO(io);
32:
33: // ── Connect to MongoDB ────────────────────────────────────────
34: connectDB();
35:
36: // ── Global Middleware ─────────────────────────────────────────
37: app.use(cors({ origin: '*' }));
38: app.use(express.json());                        // parse JSON bodies
39: app.use(express.urlencoded({ extended: true })); // parse form data
40:
41: // Serve uploaded files statically (File Uploads demo)
42: app.use('/uploads', express.static(path.join(__dirname, '..', 'uploads')));
43:
44: // ── Routes ────────────────────────────────────────────────────
45: app.use('/api/auth', authRoutes);
46: app.use('/api/employees', employeeRoutes);
47: app.use('/api/departments', departmentRoutes);
48: app.use('/api/projects', projectRoutes);
49:
50: // ── Health check (Accessing API from Browser topic) ───────────
51: app.get('/health', (req, res) => {
52:   res.json({
53:     status: 'OK',
54:     timestamp: new Date().toISOString(),
55:     uptime: process.uptime(),
56:     env: process.env.NODE_ENV,
57:   });
58: });
59:
60: // ── 404 handler ───────────────────────────────────────────────
61: app.use((req, res) => {
62:   res.status(404).json({ error: `Route not found: ${req.method} ${req.path}` });
63: });
64:
65: // ── Global error handler (must be last) ──────────────────────
66: app.use(errorHandler);
67:
68: // ── Start server ──────────────────────────────────────────────
69: const PORT = process.env.PORT || 3000;
70:
71: // Export app for testing (Testing Node.js topic)
72: if (process.env.NODE_ENV !== 'test') {
73:   server.listen(PORT, () => {
74:     console.log(`
75: 🚀 EMS API running at http://localhost:${PORT}
76: 📋 Health check: http://localhost:${PORT}/health
77: 🌍 Environment: ${process.env.NODE_ENV || 'development'}
78:     `);
79:   });
80: }
81:
82: module.exports = { app, server };
```
- **Line 5** — `require('dotenv').config()` is the **very first** line executed (after the comment header) — this is deliberate: every subsequent `require` (route files, `config/db`, middleware) may read `process.env.*` at module-load time, so `.env` must be loaded before anything else imports.
- **Lines 8–15** — all dependency imports use CommonJS `require` (contrast with Project A's ESM `import`) — this project targets Node's default module system with no `"type": "module"` flag.
- **Line 24–25** — `const app = express(); const server = http.createServer(app);` — this is precisely the pattern flagged in the courseware Q&A (#69): `app.listen()` is shorthand for `http.createServer(app).listen()`, but here the raw server instance is created explicitly **because** Socket.io needs direct access to it (line 28's `new Server(server, ...)`) — you cannot attach Socket.io to whatever internal server `app.listen()` would have created implicitly.
- **Lines 28–31** — Socket.io is instantiated with its own permissive CORS config (`origin: '*'`) — note this is a **separate** CORS configuration from the Express-level `cors()` middleware on line 37; Socket.io's WebSocket handshake is not covered by Express HTTP middleware, so it needs its own CORS setting. `setupSocketIO(io)` delegates all the room/event logic to `sockets/notifications.js` (mentioned in the README topic map — not required reading for this guide, but worth knowing it exists for socket event wiring).
- **Line 34** — `connectDB()` is called but **not awaited** at the top level — this means the server *starts listening* (line 73) without guaranteeing the DB connection has completed first. In production this is usually acceptable because Mongoose queues operations until the connection is ready, but it's a notable ordering choice worth understanding versus `await connectDB()` before `listen()`.
- **Lines 37–39** — the **middleware order** here matters and directly mirrors Section 26's Express reference pattern: CORS first (so preflight OPTIONS requests are handled before anything else), then `express.json()` (populates `req.body` for JSON payloads), then `express.urlencoded({ extended: true })` (populates `req.body` for form-encoded payloads). Any route registered *before* these two body-parsing lines would see `req.body` as `undefined` — this is the exact gotcha called out in the Q&A supplement (#63).
- **Line 42** — `app.use('/uploads', express.static(...))` mounts a static file server scoped to the `/uploads` URL prefix, serving whatever Multer (Section 38) writes into the `uploads/` directory — so an avatar saved as `uploads/employee_123_1700000000000.png` becomes reachable at `GET /uploads/employee_123_1700000000000.png`.
- **Lines 45–48** — each resource gets its own Router mounted under a versioned-by-resource prefix (`/api/auth`, `/api/employees`, etc.) — this is the Section 23 Router pattern applied at scale. Note that **no auth middleware is applied globally here** — each individual router decides internally which routes need `authenticate`/`authorize` (see Sections 40–42 — and notably, most routes in this codebase have that check **commented out**, a real gap worth flagging below).
- **Lines 61–63** — the catch-all 404 handler is registered **after** all real routes but **before** the error handler — any request that doesn't match a mounted route or the `/health` endpoint falls through to here.
- **Line 66** — `app.use(errorHandler)` — registered dead last, so it only catches errors explicitly passed via `next(err)` from any route/middleware upstream (see Section 37 for what `errorHandler` does with them).
- **Lines 71–80** — the **testability pattern from Section 17 (Module 16)** applied directly: `server.listen()` only runs when `NODE_ENV !== 'test'`. When Jest sets `NODE_ENV=test` (implicitly, or via test config), the app module loads fully (routes mounted, middleware wired) but never binds a port — so `tests/employee.test.js` (Section 41) can `require('../src/app')` and pass the `app` object straight into `supertest(app)` without a real network listener.
- **Line 82** — exports **both** `app` and `server` (not just `app`) — `server` is exported too because Socket.io is attached to it; tests or other consumers that need the raw HTTP server (e.g. to close it cleanly) have access to it.

## 37. `src/middleware/errorHandler.js` — Express 4-Argument Error Middleware

```javascript
1:  // src/middleware/errorHandler.js
2:  // Demonstrates: Express error-handling middleware (4-argument signature),
3:  //               centralised error responses
4:
5:  const errorHandler = (err, req, res, next) => {
6:    console.error('💥 Error:', err.message);
7:
8:    // Mongoose validation error
9:    if (err.name === 'ValidationError') {
10:     const messages = Object.values(err.errors).map((e) => e.message);
11:     return res.status(400).json({ error: 'Validation failed', details: messages });
12:   }
13:
14:   // Mongoose duplicate key
15:   if (err.code === 11000) {
16:     const field = Object.keys(err.keyValue)[0];
17:     return res.status(409).json({ error: `Duplicate value for field: ${field}` });
18:   }
19:
20:   // JWT errors
21:   if (err.name === 'JsonWebTokenError') {
22:     return res.status(401).json({ error: 'Invalid token' });
23:   }
24:   if (err.name === 'TokenExpiredError') {
25:     return res.status(401).json({ error: 'Token expired' });
26:   }
27:
28:   // Cast error (invalid ObjectId)
29:   if (err.name === 'CastError') {
30:     return res.status(400).json({ error: `Invalid ${err.path}: ${err.value}` });
31:   }
32:
33:   // Generic fallback
34:   res.status(err.statusCode || 500).json({
35:     error: err.message || 'Internal Server Error',
36:   });
37: };
38:
39: module.exports = errorHandler;
```
- **Line 5** — the function signature `(err, req, res, next)` has **exactly four parameters**. Express distinguishes error-handling middleware from regular middleware purely by **counting the declared parameters** (function arity) — a regular middleware's 3-param `(req, res, next)` is never invoked with an error, and this 4-param function is only invoked when something upstream calls `next(err)` (never on the normal request flow). The `next` parameter here is never actually called (there's no case that falls through) — but it must remain in the signature or Express would misidentify this as ordinary middleware.
- **Lines 9–12** — `ValidationError` is Mongoose's error name when a document fails schema validation (`required`, `min`, `enum`, etc. — Section 12/Module 11). `err.errors` is an object keyed by field name; `Object.values(...).map(e => e.message)` flattens it into a plain array of human-readable messages, returned as `400 Bad Request`.
- **Lines 15–18** — `err.code === 11000` is MongoDB's raw duplicate-key error code (fired when a `unique: true` index is violated, e.g. registering with an email that already exists). `err.keyValue` is an object like `{ email: 'x@y.com' }`; `Object.keys(...)[0]` extracts just the field name for the message. Returned as `409 Conflict` — the correct HTTP status for "this resource state already exists," distinct from `400` (which the ValidationError branch uses for malformed input).
- **Lines 21–26** — two separate JWT error types from the `jsonwebtoken` library: `JsonWebTokenError` (malformed/tampered token, wrong secret) and `TokenExpiredError` (valid signature but `exp` claim has passed) — both correctly mapped to `401 Unauthorized`, distinguished in the message for debuggability even though the HTTP status is the same for both.
- **Lines 29–31** — `CastError` is Mongoose's error when a query tries to cast a malformed value into a schema type — most commonly, passing a non-ObjectId string as an `:id` route param (e.g. `GET /api/employees/not-a-real-id`). `err.path` is the field name being cast, `err.value` is the offending input — both surfaced directly in the response.
- **Lines 34–36** — the fallback branch: if none of the named error types matched, respond with `err.statusCode` if the thrower set one (a hand-rolled "operational error" convention), else default to `500`. This exact five-branch classification (`ValidationError` → `duplicate key 11000` → `JsonWebTokenError`/`TokenExpiredError` → `CastError` → generic) is a direct, near-verbatim implementation of the Express reference's `errorHandler` in Section 27 — the project simply adds the `CastError` branch on top of what the reference showed.

## 38. `src/middleware/upload.js` — Multer Disk Storage Configuration

```javascript
1:  // src/middleware/upload.js
2:  // Demonstrates: File Uploads (Task App topic), Multer, Node.js fs module
3:
4:  const multer = require('multer');
5:  const path = require('path');
6:  const fs = require('fs');
7:
8:  // Ensure upload directory exists (Node.js fs module demo)
9:  const uploadDir = process.env.UPLOAD_DIR || 'uploads';
10: if (!fs.existsSync(uploadDir)) {
11:   fs.mkdirSync(uploadDir, { recursive: true });
12: }
13:
14: // Storage configuration
15: const storage = multer.diskStorage({
16:   destination: (req, file, cb) => {
17:     cb(null, uploadDir);
18:   },
19:   filename: (req, file, cb) => {
20:     // employee_<id>_<timestamp>.ext  — unique, no collision
21:     const ext = path.extname(file.originalname);
22:     const name = `employee_${req.employee._id}_${Date.now()}${ext}`;
23:     cb(null, name);
24:   },
25: });
26:
27: // File type filter
28: const fileFilter = (req, file, cb) => {
29:   const allowed = ['image/jpeg', 'image/png', 'image/webp'];
30:   if (allowed.includes(file.mimetype)) {
31:     cb(null, true);
32:   } else {
33:     cb(new Error('Only JPEG, PNG, and WebP images are allowed'), false);
34:   }
35: };
36:
37: const upload = multer({
38:   storage,
39:   fileFilter,
40:   limits: {
41:     fileSize: parseInt(process.env.MAX_FILE_SIZE) || 5 * 1024 * 1024, // 5 MB
42:   },
43: });
44:
45: module.exports = upload;
```
- **Lines 9–12** — `fs.existsSync`/`fs.mkdirSync({ recursive: true })` run once **at module-load time** (not inside a request handler) — this guarantees the upload directory exists before any request tries to write to it, and `recursive: true` means it won't throw if the directory (or any parent) already exists. This is the exact `fs` idiom from Module 4 of the courseware.
- **Lines 15–25** — `multer.diskStorage` takes two callback-style configuration functions, both following Node's `(err, result)` callback convention via the `cb` parameter (`cb(null, value)` on success, `cb(error)` on failure) — this is the same error-first callback pattern taught in Module 5/6, just applied to configuration callbacks rather than I/O callbacks.
- **Line 16–18** — `destination` always returns the same `uploadDir` — no per-request branching, so all uploads land in one flat directory.
- **Line 22** — the generated filename is `employee_<req.employee._id>_<Date.now()><ext>` — critically, this reads `req.employee._id`, meaning **this middleware assumes `req.employee` has already been set by an earlier auth middleware** in the chain. If `upload.single(...)` were used on a route without `authenticate` running first, this line would throw (`Cannot read properties of undefined`). This is a real coupling to check when reading `employeeRoutes.js` (Section 40) — the avatar upload route does *not* actually run `authenticate` before `upload.single('avatar')`, which is a genuine risk/gotcha worth flagging (see Section 40 analysis).
- **Lines 28–35** — `fileFilter` allow-lists exactly three MIME types (JPEG/PNG/WebP); rejecting via `cb(new Error(...), false)` — Multer will surface this as an error to the route's `(err, req, res, next)` handling if wired, or as an unhandled error otherwise (note: this project has no dedicated Multer-error-catching middleware like the courseware's Module 14 example — Multer errors propagate to the generic error handler instead, which doesn't have a specific `MulterError`/`LIMIT_FILE_SIZE` branch — another realistic gap versus the courseware's more complete example).
- **Lines 37–43** — the final `upload` object is configured with `storage`, `fileFilter`, and a `limits.fileSize` cap read from `process.env.MAX_FILE_SIZE` (falling back to 5MB) — exported directly as Express middleware, consumed via `.single('avatar')` in the route file.

## 39. `src/utils/email.js` — Nodemailer with File-Based Credential Fallback

```javascript
1:  // src/utils/email.js
2:  // Demonstrates: Sending Emails (Task App topic), Nodemailer, async/await,
3:  //               Node.js File System (fs.readFileSync)
4:
5:  const nodemailer = require('nodemailer');
6:  const fs = require('fs');
7:
8:  // ── Read EMAIL_PASS from a local file ─────────────────────────
9:  // Useful in dev when you don't want to put credentials in .env
10: // The file should contain just the app-password text, nothing else.
11: const passwordFile = 'D:/Projects/delete/shridhar-gmail-app-password.txt';
12:
13: let EMAIL_PASS = process.env.EMAIL_PASS; // fallback to .env
14:
15: try {
16:   EMAIL_PASS = fs.readFileSync(passwordFile, 'utf-8').trim();
17:   console.log('🔑 EMAIL_PASS loaded from file');
18: } catch (err) {
19:   // File not found or unreadable — fall back to .env value
20:   console.warn(`⚠️  Could not read password file (${err.code}). Falling back to EMAIL_PASS from .env`);
21: }
22:
23: // ── Create transporter (reused across calls) ──────────────────
24: const transporter = nodemailer.createTransport({
25:   host: process.env.EMAIL_HOST || 'smtp.gmail.com',
26:   port: parseInt(process.env.EMAIL_PORT) || 587,
27:   secure: false, // TLS (STARTTLS on port 587)
28:   auth: {
29:     user: process.env.EMAIL_USER,
30:     pass: EMAIL_PASS,           // ← from file (or .env fallback)
31:   },
32: });
33:
34: /**
35:  * Send a welcome email to a newly created employee.
36:  * Skipped automatically in test/dev when EMAIL_USER is not set.
37:  */
38: const sendWelcomeEmail = async ({ to, firstName }) => {
39:   const mailOptions = {
40:     from: `"EMS System" <${process.env.EMAIL_USER}>`,
41:     to,
42:     subject: 'Welcome to the Team! 🎉',
43:     html: `
44:       <h2>Hello, ${firstName}!</h2>
45:       <p>Your EMS account has been created successfully.</p>
46:       <p>Log in at <a href="http://localhost:${process.env.PORT || 3000}">EMS Portal</a>.</p>
47:       <p>— HR Team</p>
48:     `,
49:   };
50:
51:   if (process.env.NODE_ENV === 'test' || !process.env.EMAIL_USER) {
52:     console.log(`📧 [EMAIL SKIPPED] Would send welcome mail to ${to}`);
53:     return;
54:   }
55:
56:   await transporter.sendMail(mailOptions);
57:   console.log(`📧 Welcome email sent to ${to}`);
58: };
59:
60: module.exports = { sendWelcomeEmail };
```
- **Line 13, 15–21** — `EMAIL_PASS` is initialized from `process.env.EMAIL_PASS` **first**, then a `try/catch` around a **synchronous** `fs.readFileSync` attempts to overwrite it by reading from an absolute local file path (same technique as Project A's `send-email.js`, Section 35). If the file doesn't exist on the current machine, the `catch` block quietly falls back to the already-set `.env` value rather than crashing — this graceful-degradation pattern (env var as the portable default, local file as a dev-machine-specific override) is worth understanding: it means this code runs fine on a teammate's machine or in CI (no such file → falls back to `.env`), but on the original author's machine it silently prefers the file. `err.code` (e.g. `'ENOENT'`) is included in the warning log for debuggability.
- **Line 16** — this is a **synchronous, blocking** file read executed at **module load time** (outside any function) — meaning it runs once when `require('./utils/email')` first executes, blocking the event loop briefly during server startup only (acceptable per Module 4's sync-is-fine-for-startup guidance, since it's not inside a per-request handler).
- **Lines 24–32** — the transporter is built **once at module scope** and reused across every `sendWelcomeEmail` call — this matches the courseware's explicit guidance ("create transporter once, reuse across calls") and avoids the overhead of establishing a new SMTP connection per email.
- **Line 38** — `sendWelcomeEmail` takes a single destructured object parameter `{ to, firstName }` rather than positional args — a common Node convention for functions with multiple string parameters, avoiding call-site ambiguity (`sendWelcomeEmail('x@y.com', 'Raj')` is less self-documenting than `sendWelcomeEmail({ to: 'x@y.com', firstName: 'Raj' })`).
- **Lines 51–54** — a **test/dev safety guard**: if running under Jest (`NODE_ENV === 'test'`) or if `EMAIL_USER` was never configured, the function logs and returns early **without** calling `transporter.sendMail`. This is exactly why the test suite (Section 41) can register employees repeatedly without ever hitting a real SMTP server or requiring live credentials in CI.
- **Line 60** — exports a single named function `{ sendWelcomeEmail }`, consumed in `authRoutes.js` (Section 40).
- **Lines 62–106** — a fully commented-out earlier version of the same file (reading `EMAIL_PASS` purely from `.env`, no file-fallback) — left in as a rollback/comparison reference, exactly like the pattern seen in Project A's `emp-stuff.js` (Section 33).

## 40. Route Files — `authRoutes.js`, `employeeRoutes.js`, `departmentRoutes.js`, `projectRoutes.js`

### `src/routes/authRoutes.js`

```javascript
1:  // src/routes/authRoutes.js
2:  // Demonstrates: Express Router, REST API design, JWT auth flow
3:
4:  const express = require('express');
5:  const router = express.Router();
6:
7:  const Employee = require('../models/Employee');
8:  const { authenticate } = require('../middleware/auth');
9:  const { sendWelcomeEmail } = require('../utils/email');
10:
11: // ── POST /api/auth/register ───────────────────────────────────
12: // Public: create first account (or admin creates employees)
13: router.post('/register', async (req, res, next) => {
14:   try {
15:     // Destructure only what we need (JS Objects demo)
16:     const { firstName, lastName, email, password, role } = req.body;
17:
18:     const employee = new Employee({ firstName, lastName, email, password, role });
19:     await employee.save();
20:
21:     const token = await employee.generateAuthToken();
22:
23:     // Fire-and-forget email (Promises demo)
24:     sendWelcomeEmail({ to: email, firstName }).catch(console.error);
25:
26:     res.status(201).json({ employee, token });
27:   } catch (err) {
28:     next(err);
29:   }
30: });
31:
32: // ── POST /api/auth/login ──────────────────────────────────────
33: router.post('/login', async (req, res, next) => {
34:   try {
35:     const { email, password } = req.body;
36:
37:     if (!email || !password) {
38:       return res.status(400).json({ error: 'Email and password are required' });
39:     }
40:
41:     const employee = await Employee.findByCredentials(email, password);
42:     const token = await employee.generateAuthToken();
43:
44:     res.json({ employee, token });
45:   } catch (err) {
46:     res.status(401).json({ error: err.message });
47:   }
48: });
49:
50: // ── POST /api/auth/logout ─────────────────────────────────────
51: // Invalidate current token (remove from tokens array)
52: router.post('/logout', authenticate, async (req, res, next) => {
53:   try {
54:     req.employee.tokens = req.employee.tokens.filter(
55:       (t) => t.token !== req.token
56:     );
57:     await req.employee.save();
58:     res.json({ message: 'Logged out successfully' });
59:   } catch (err) {
60:     next(err);
61:   }
62: });
63:
64: // ── POST /api/auth/logout-all ─────────────────────────────────
65: // Invalidate ALL tokens (useful when password compromised)
66: router.post('/logout-all', authenticate, async (req, res, next) => {
67:   try {
68:     req.employee.tokens = [];
69:     await req.employee.save();
70:     res.json({ message: 'Logged out from all devices' });
71:   } catch (err) {
72:     next(err);
73:   }
74: });
75:
76: // ── GET /api/auth/me ──────────────────────────────────────────
77: router.get('/me', authenticate, (req, res) => {
78:   res.json(req.employee);
79: });
80:
81: module.exports = router;
```
- **Line 5** — `express.Router()` instantiates a mini-app scoped to this file; mounted at `/api/auth` in `app.js` (Section 36, line 45).
- **Line 13** — `router.post('/register', async (req, res, next) => {...})` — registers a handler for `POST /api/auth/register`. It is deliberately **not** protected by `authenticate` — registration must be public (you can't require a token to get your first token).
- **Line 16** — destructures exactly the fields needed from `req.body`; notably `role` is accepted directly from client input here with **no server-side default or restriction** — meaning a malicious client could self-register as `role: 'admin'` unless the `Employee` schema restricts this via an enum default or the field is stripped elsewhere. This is a genuine security consideration worth flagging in an assessment context (contrast with `employeeRoutes.js`'s POST handler, which explicitly whitelists allowed fields — Section 40 below).
- **Lines 18–19** — `new Employee({...})` then `.save()` — this two-step form (rather than `Employee.create()`) is used because Mongoose's `pre('save')` hook (password hashing, defined on the model, referenced but not re-explained here per the MongoDB-guide note) needs to fire, and both `.save()` and `.create()` trigger it equally — the choice here is stylistic, matching the courseware's coverage of both forms in Module 11.
- **Line 21** — `employee.generateAuthToken()` is a Mongoose instance method (defined on the `Employee` model) that signs a JWT and appends it to the employee's `tokens[]` array — same pattern as the courseware's Module 12 `generateAuthToken` method (Section 13).
- **Line 24** — `sendWelcomeEmail({...}).catch(console.error)` — the email Promise is **not** `await`ed; a bare `.catch()` swallows any rejection into a console log rather than propagating it. This is precisely the "don't block the response on email sending" pattern the courseware calls out explicitly in Module 15 — if this line instead read `await sendWelcomeEmail(...)`, a slow or failing SMTP server would delay or break the registration response entirely.
- **Line 26** — responds `201 Created` with both the (Mongoose-`toJSON`-sanitized, per the model's presumed `toJSON` override — see the MongoDB guide) employee document and the freshly minted token — the standard "register returns a usable session immediately" pattern, avoiding a separate login round-trip.
- **Line 27–29** — the `catch` block calls `next(err)`, routing any thrown error (e.g. a Mongoose `ValidationError` from a missing required field, or an `11000` duplicate email) into the centralized `errorHandler` (Section 37), which already has dedicated branches for exactly those two cases.
- **Lines 33–48 (`/login`)** — note this handler's `catch` block (line 46) responds directly with `res.status(401).json(...)` rather than calling `next(err)` — a deliberate choice to always surface login failures as `401 Unauthorized` regardless of the underlying error, rather than letting the generic error handler potentially return a `500` for what should always look like "bad credentials" to the client (also avoids leaking whether the failure was "email not found" vs "wrong password" — both paths in `findByCredentials` throw the same generic message, per the courseware's login pattern in Module 12).
- **Line 41** — `Employee.findByCredentials(email, password)` — a Mongoose **static** method (defined on the model, not an instance) that looks up by email then `bcrypt.compare`s the password — same pattern as Section 13's `userSchema.statics.findByCredentials`.
- **Lines 52–62 (`/logout`)** — protected by the `authenticate` middleware (line 52, third argument to `router.post`) — Express allows an arbitrary number of middleware functions before the final handler in a route registration; `authenticate` runs first, and only calls `next()` (implicitly, inside its own body) to reach this handler if the token is valid. Line 54–56 filters the current token **out** of `req.employee.tokens` — this is what makes logout actually invalidate that specific token server-side (a stateless-JWT system with no server tracking couldn't do this — the DB-backed `tokens[]` array is what enables it, at the cost of a DB lookup on every authenticated request).
- **Lines 66–74 (`/logout-all`)** — same idea but clears the **entire** `tokens` array — invalidates every session across every device, the standard "I think my password was compromised" flow.
- **Line 77** — `GET /me` simply returns `req.employee`, which was already attached by the `authenticate` middleware (Section 42) — no additional DB query needed since `authenticate` already fetched the full employee document to verify the token.

### `src/routes/departmentRoutes.js` — CRUD + Nested Resource + Business-Rule Delete Guard

Key structural patterns (full code reproduced in the read above; here explaining the distinctive lines):
- **Line 12** — `// router.use(authenticate);` is **commented out**. Per the README, all endpoints except `/health` are supposed to require a Bearer token — but in this file (and identically in `employeeRoutes.js` line 16 and `projectRoutes.js` line 11), the router-wide `authenticate` gate is disabled. Individual write routes still call `authorize('admin')` (e.g. line 67's `router.post('/', authorize('admin'), ...)`), but **`authorize` reads `req.employee.role`**, and `req.employee` is only ever set by `authenticate` (Section 42) — so with `authenticate` commented out, `authorize('admin')` would actually **throw** (`Cannot read properties of undefined (reading 'role')`) on any unauthenticated request, rather than cleanly rejecting with 401/403. This is a real, checkable bug/gap in the codebase worth understanding for assessment purposes: the GET routes (lines 15, 31, 47) are effectively **fully public** with no auth applied at all, while POST/PATCH/DELETE routes are broken in a way that crashes instead of denying.
- **Lines 17–24** — `parseQuery(req.query, [])` (from `utils/queryHelper.js`, the reusable filter/sort/paginate helper matching the courseware's Module 13 `buildQuery` pattern — Section 14) builds `{ filter, sort, skip, limit, page }` from query string params; `Promise.all([Department.find(...), Department.countDocuments(...)])` runs the page query and the total count concurrently — the exact "run both in parallel" idiom from Module 13.
- **Lines 36–37** — `Employee.countDocuments({ department: dept._id })` is a cross-model count used to enrich the single-department response with a computed `employeeCount` field not stored on the Department document itself.
- **Lines 99–105 (delete guard)** — `Employee.exists({ department: req.params.id })` checks for any employee still assigned to this department **before** allowing deletion; if any exist, responds `409 Conflict` instead of deleting — a referential-integrity business rule enforced at the application layer (MongoDB itself has no foreign-key constraints, so this check is Mongoose/Express's responsibility, unlike a SQL DB with `ON DELETE RESTRICT`).

### `src/routes/employeeRoutes.js` — Sorting/Pagination/Filtering + Field Whitelisting + Role-Conditional Updates + Avatar Upload

- **Lines 22–25** — `parseQuery(req.query, ['isActive', 'department', 'role', 'designation'])` — the second argument is an **explicit allow-list of filterable fields**, preventing a client from injecting an arbitrary Mongo filter via unexpected query keys — this is the practical, production-grade version of the courseware's Module 13 `allowedFilters` parameter.
- **Line 30** — `.populate('department', 'name code')` — joins in just the `name` and `code` fields of the referenced Department document (Section 12's projection-on-populate pattern), avoiding pulling the entire department document into every employee list response.
- **Lines 62–72 (POST, field whitelisting via `reduce`)** —
  ```
  const allowed = ['firstName', 'lastName', 'email', 'password', 'phone', 'designation', 'salary', 'department', 'joinDate', 'role'];
  const body = allowed.reduce((acc, key) => { if (req.body[key] !== undefined) acc[key] = req.body[key]; return acc; }, {});
  ```
  This builds a new object containing **only** the whitelisted keys present in `req.body`, using `Array.prototype.reduce` as an object-builder — a direct security best practice (comment on line 61 explicitly says so) preventing mass-assignment of unexpected fields (e.g. a client couldn't sneak in `isAdmin: true` or `tokens: [...]` this way, since those keys aren't in `allowed`). Contrast this with `authRoutes.js`'s `/register` handler (Section 40 above), which destructures `role` directly from `req.body` with no such whitelist — an inconsistency between the two files worth noting.
- **Lines 85–118 (PATCH — role-conditional field permissions)** —
  ```
  const allowedForAll = ['firstName', 'lastName', 'phone', 'designation'];
  const allowedForAdmin = [...allowedForAll, 'salary', 'role', 'department', 'isActive'];
  const allowed = req.employee.role === 'admin' ? allowedForAdmin : allowedForAll;
  ```
  This is a fine-grained authorization pattern beyond simple route-level `authorize()`: **which fields** a request may update depends on the caller's role, computed inline rather than via a separate middleware. Non-admins are restricted to cosmetic self-profile fields; only admins can touch `salary`, `role`, `department`, `isActive`. Line 91 (`req.employee.role === 'admin'`) again depends on `req.employee` being set — meaning this route, too, silently assumes `authenticate` ran, even though (per the note above) it's commented out at the router level.
- **Lines 99–103 (ownership check)** — `if (req.employee.role !== 'admin' && req.employee._id.toString() !== targetId) return res.status(403)...` — non-admins may only edit **their own** record; `.toString()` is required because `req.employee._id` is a Mongoose `ObjectId` object, not a string, and must be explicitly stringified before comparing to `req.params.id` (a raw string from the URL) — a very common Mongoose gotcha (`ObjectId !== string` even for the "same" id).
- **Lines 133–153 (`POST /:id/avatar`)** — this route uses `upload.single('avatar')` (Section 38) as route-level middleware **without** `authenticate` preceding it (line 133: `router.post('/:id/avatar', upload.single('avatar'), async (req, res, next) => {...})`) — directly confirming the coupling flagged in Section 38: Multer's `filename` callback reads `req.employee._id`, but on this route `req.employee` is never set (no `authenticate` in the chain), so **this upload route would throw at the Multer filename-generation step for every request** — a genuine bug traceable end-to-end from `upload.js` through this route registration. Lines 141–144 additionally delete the employee's previous avatar file (`fs.unlinkSync`) before saving the new filename — cleanup pattern preventing orphaned files accumulating in `uploads/`.

### `src/routes/projectRoutes.js` — Multi-Ref Populate + Array Manipulation

- **Lines 22–24** — chains **two** `.populate()` calls on the same query (`department` and `assignedEmployees`), each projecting different fields — Mongoose supports populating multiple ref paths independently on one query.
- **Lines 92–114 (`POST /:id/assign`)** — assigns an array of employee IDs to a project while avoiding duplicates:
  ```
  const existing = project.assignedEmployees.map((id) => id.toString());
  const toAdd = employeeIds.filter((id) => !existing.includes(id));
  project.assignedEmployees.push(...toAdd);
  ```
  This manually implements set-like uniqueness using `Array.prototype.filter` + `.includes()` (an O(n·m) approach, acceptable for typically small arrays) rather than a `Set`, and again requires `.toString()` on the existing ObjectId array before comparing against the incoming plain-string `employeeIds` — same ObjectId-vs-string gotcha as Section 40's employee update route.
- **Lines 118–132 (`DELETE /:id/assign/:employeeId`)** — removes a single employee from the array via `.filter(empId => empId.toString() !== req.params.employeeId)` — the standard "remove by id" idiom for Mongoose subdocument/ref arrays (no native `.remove()` on plain arrays of ObjectIds, so `filter`-and-reassign is required).

## 41. `tests/employee.test.js` — Jest + Supertest Integration Suite

```javascript
1:  // tests/employee.test.js
2:  // Demonstrates: Testing Node.js (Task App topic), Jest, Supertest
3:
4:  const request = require('supertest');
5:  const mongoose = require('mongoose');
6:  const { app } = require('../src/app');
7:  const Employee = require('../src/models/Employee');
8:  const Department = require('../src/models/Department');
...
30: beforeAll(async () => {
31:   const mongoUri = process.env.MONGODB_URI || 'mongodb://localhost:27017/ems_test';
32:   await mongoose.connect(mongoUri);
33: });
34:
35: afterAll(async () => {
36:   await Employee.deleteMany({ email: /@ems-test\.com$/ });
37:   await Department.deleteMany({ code: 'TST' });
38:   await mongoose.connection.close();
39: });
...
43: describe('Auth Routes', () => {
44:   test('POST /api/auth/register – should create admin', async () => {
45:     const res = await request(app)
46:       .post('/api/auth/register')
47:       .send(adminData)
48:       .expect(201);
49:
50:     expect(res.body).toHaveProperty('token');
51:     expect(res.body.employee.email).toBe(adminData.email);
52:     expect(res.body.employee).not.toHaveProperty('password');
53:
54:     adminToken = res.body.token;
55:   });
...
85:   test('GET /api/auth/me – no token returns 401', async () => {
86:     await request(app).get('/api/auth/me').expect(401);
87:   });
88: });
```
- **Line 6** — `const { app } = require('../src/app')` — destructures **only** `app` out of the `{ app, server }` export (Section 36, line 82); the test suite never needs `server` since Supertest doesn't require an actual bound port — it drives the Express app's request pipeline directly in-process.
- **Lines 30–33** — `beforeAll` connects Mongoose **once** for the whole file (not per test) — hitting `MONGODB_URI` from the environment, or falling back to a local `ems_test` database — following the courseware's explicit guidance (Section 17/Module 16) to use a **separate test database**, never the production/dev one.
- **Lines 35–38 (`afterAll` cleanup)** — rather than dropping the whole test database, this cleans up **surgically**: deletes only employees whose email matches `/@ems-test\.com$/` and departments with `code: 'TST'` — a regex-scoped teardown that lets the test DB be shared/reused across runs without needing a full wipe, and avoids accidentally deleting unrelated seed data if the test DB is shared with manual dev testing.
- **Line 44–55** — the very first test **registers a real admin account** and captures the returned `token` into the outer-scope `adminToken` variable (declared at line 27, `let adminToken`) — every subsequent test in the file **depends on this one running first and succeeding**, since Jest (without explicit isolation) runs tests within a `describe` block in file order by default. This is a deliberate integration-testing tradeoff: tests are **not independent** (unlike the pure-unit tests in Project A's `emp-stuff.test.js`), they form a **sequential story** (register → login → CRUD → cleanup) mirroring how a real client session would actually use the API.
- **Line 50–52** — three assertions on the register response: `toHaveProperty('token')` (session issued), `.email` equality (correct data echoed back), and critically `.not.toHaveProperty('password')` — this last assertion is what actually **verifies** that the Employee model's `toJSON` override (referenced conceptually in Section 40, defined in the model file which is out of scope here per the MongoDB-guide note) is correctly stripping the password hash before serialization — a security-relevant test, not just a data-shape test.
- **Lines 85–87 (unauthenticated 401 test)** — worth cross-referencing against Section 40's finding that `employeeRoutes.js`/`departmentRoutes.js`/`projectRoutes.js` have `authenticate` commented out at the router level: this specific 401 test only passes because `authRoutes.js`'s `/me` route (Section 40, line 77) **does** explicitly pass `authenticate` as route-level middleware (`router.get('/me', authenticate, ...)`), independent of any router-wide `.use()`. The test suite (Section 41's Department/Employee tests) never actually exercises "call employee/department endpoints with no token and expect 401" — which is consistent with those routes genuinely not enforcing auth in this codebase, a gap the test suite doesn't happen to catch.
- **Lines 94–103, 119–174** — the Department and Employee CRUD test blocks follow the identical Supertest chain shape throughout: `request(app).<method>(<path>).set('Authorization', ...).send(<body>).expect(<status>)`, then assert on `res.body`. Notably **every** write request in these blocks **does** set the `Authorization` header with `adminToken` even though (per the router-level finding above) most of these routes don't actually require it — meaning the test suite is testing the "happy path with a valid token" but not exercising the actual authorization gap.
- **Lines 176–182 (Health Check)** — a simple smoke test on `GET /health` (Section 36, lines 51–58) confirming `res.body.status === 'OK'` — the simplest possible test in the file, included as the baseline "is the app even wired up" sanity check.

---

# PART 5 — Quick Cross-Reference: Where Each Courseware Topic Shows Up in the Real Code

| Courseware Topic (Part 1/2) | Project A file | Project B file |
|---|---|---|
| Module system (require/export vs import/export) | `calc.js`, all files (ESM) | `app.js`, all files (CommonJS) |
| Event loop / async / EventEmitter | `node-topics.js` | `app.js` (fire-and-forget email), `authRoutes.js` |
| Raw `http` module | `http-demo.js`, `server.js` | `app.js` (`http.createServer(app)` for Socket.io) |
| `fs` module | `server.js`, `send-email.js` | `upload.js`, `email.js` |
| Debugging (stack traces, buggy access) | `node-topics.js` (commented example) | — |
| Express middleware chain & order | — | `app.js` |
| Express Router | — | all `routes/*.js` |
| Error-handling middleware (4-arg) | — | `errorHandler.js` |
| JWT auth + bcrypt | — | `middleware/auth.js`, `authRoutes.js` |
| Role-based authorization | — | `middleware/auth.js` (`authorize`), route files |
| Sorting/Pagination/Filtering | — | `employeeRoutes.js`, `departmentRoutes.js`, `projectRoutes.js` (via `queryHelper.js`) |
| File uploads (Multer) | — | `middleware/upload.js`, `employeeRoutes.js` |
| Sending email (Nodemailer) | `send-email.js` | `utils/email.js`, `authRoutes.js` |
| Socket.io real-time | `server.js` | `app.js`, `sockets/notifications.js` |
| Jest unit testing | `emp-stuff.test.js` | — |
| Jest + Supertest integration testing | — | `tests/employee.test.js` |
| Mongoose CRUD, populate, hooks | — | all `routes/*.js` (models covered in MongoDB guide) |

---

# PART 6 — Notable Gotchas Worth Knowing for Assessment (from `nodejs_discussion_qa.md`)

- **`process.nextTick` vs `setImmediate`**: nextTick fires before the event loop advances phases at all; setImmediate fires in the check phase after I/O. Inside an I/O callback, `setImmediate` always beats `setTimeout(fn, 0)`.
- **Module caching**: `require()` caches by resolved file path — exported objects are singletons across the whole process; clearing `require.cache[filename]` forces a reload (used in some test setups).
- **`req.body` is `undefined`**: almost always means `express.json()` wasn't registered before the route, or wasn't registered at all.
- **`app.use()` vs `app.get()`**: `use` matches all HTTP methods on a path prefix (middleware); `get/post/put/delete` match one specific method (route handlers).
- **Operational vs programmer errors**: operational (network timeout, bad input) should be handled gracefully with a proper HTTP status; programmer errors (bugs) should generally crash the process after logging — don't try to "handle" a bug into a 200 response.
- **Unhandled promise rejections**: crash the process by default in Node 15+ — always attach `.catch()` or wrap in `try/catch`.
- **`npm install` vs `npm ci`**: `npm ci` is deterministic (installs exactly what's locked, wipes `node_modules` first) — always the right choice in CI/CD.
- **ObjectId vs string comparison**: a recurring bug source in this exact codebase (Sections 40) — Mongoose ObjectIds must be `.toString()`'d before comparing to route params or array contents, since `ObjectId !== 'sameLookingString'` by reference/type.
