# Node.js & Express — Complete Line-by-Line Guide

This guide is grounded strictly in Aakash's actual course materials: the 5-part `nodejs-courseware` series (Modules 1–18), the `Express_Reference.md` (Modules 01–06), and supplementary gotchas from `nodejs_discussion_qa.md`. It then walks through two real projects from the course code: **acme-node-demo** (raw Node.js fundamentals — no framework) and **Express** (a production-style Express + Mongoose Employee Management System with JWT auth, file upload, email, and Socket.io). Everything below reproduces the actual course text/code with line-by-line explanation — nothing is invented. Comparisons are drawn to Python web frameworks (Flask/FastAPI/Django) and tooling (pip, asyncio) where it shortens the explanation; generic backend concepts you already know are not over-explained.

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

*(Note: the actual Employee/Department/Project Mongoose models used in the Express project below are covered in a separate MongoDB-focused guide.)*

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
{
  "name": "acme-node-demo",
  "type": "module",
  "dependencies": { "mongoose": "9.7.2", "nodemailer": "9.0.1", "socket.io": "4.8.3" },
  "devDependencies": { "jest": "30.4.2" }
}
```
`"type": "module"` switches the whole package to ESM: every `.js` file is parsed as an ES module (`import`/`export`), not CommonJS. The `test` script runs Jest with `--experimental-vm-modules`, because native ESM support in Jest is still experimental and needs this flag to work with `import`/`export` test files.

## 29. `calc.js` — Module Export Pattern Exploration (Commented Scratch File)

This file is entirely commented out — it's a teaching scratchpad showing the **four ways to export from an ES module**, left in place for reference/reversion during a live lesson:

```javascript
// pattern 1: single `export default` function
// pattern 2: named consts + one `export { addNums, subNums }` statement at the end
// pattern 3: inline `export const` on each declaration (most common modern style)
// pattern 4: bundle everything into one exported object — export const calc = { addNums, subNums }
```
Pattern 4 is useful when you want namespacing (`calc.addNums(...)`) rather than flat named imports.

## 30. `node-topics.js` — Async/Streams/EventEmitter Scratchpad

Also entirely commented out — a running lesson notebook covering, in order: the classic callback pattern (`readFile` error-first callback demonstrating output ordering 1→2→3, i.e. sync code runs before the async callback fires), `Object.keys()` on an employee object, `Array.filter()` for even numbers, a deliberately buggy `getUserAge(user)` function (accessing `user.profile.age` when `profile` is `undefined` — the exact stack-trace debugging example from Module 5), streams (`readFileSync` vs `createReadStream`/`pipe`), and finally a full `EventEmitter`-based `OrderSystem` class identical to the Module 6 example — a live-coding companion to the Module 5/6/7 lecture content.

## 31. `http-demo.js` — Raw HTTP Server (No Framework)

```javascript
import http from 'http';
const PORT = 3000;

const server = http.createServer((request, response) => {
    console.log(`${request.method} ${request.url}`);
    if (request.url == '/') response.end('Welcome');
    else if (request.url == '/about') response.end('About page');
    else response.end('404! Page not found!');
});

server.listen(PORT, () => console.log(`Server running at http://localhost:${PORT}`));
```
`http.createServer(callback)` fires on every incoming request with raw `request`/`response` objects — no Express convenience methods like `res.json()` exist here; this is the bare-metal API Express wraps. Routing is manual `if`/`else if`/`else` on `request.url`.

**Gotcha**: the "404" branch never sets a status code — it still returns HTTP 200 with a "404!" text body, since `response.statusCode` was never explicitly set. Raw `http` defaults to status 200 unless you set it yourself (unlike Express's `res.status(404)` helper). This is the direct hands-on companion to Module 7's "Bare HTTP Server (Raw Node)" example.

## 32. `server.js` — Raw HTTP Server + Static File Serving + Socket.io

```javascript
import http from 'http';
import fs from 'fs';
import path from 'path';
import { Server } from 'socket.io';

const PORT = 3000;

const server = http.createServer((req, res) => {
    if (req.url === '/') {
        const filePath = path.join(process.cwd(), 'src', 'public', 'index.html');
        fs.readFile(filePath, 'utf8', (err, data) => {
            if (err) { res.writeHead(500, { 'Content-Type': 'text/plain' }); res.end('Error loading page'); return; }
            res.writeHead(200, { 'Content-Type': 'text/html' });
            res.end(data);
        });
        return;
    }
    res.writeHead(404, { 'Content-Type': 'text/plain' });
    res.end('Page Not Found');
});

const io = new Server(server);
io.on('connection', (socket) => {
    socket.on('chat-message', (msg) => io.emit('chat-message', msg));
    socket.on('disconnect', () => console.log(`Client disconnected: ${socket.id}`));
});

server.listen(PORT, () => console.log(`Server running at http://localhost:${PORT}`));
```
On file-read failure this manually writes a 500 with `res.writeHead`/`res.end`; on success it streams the file back the same way — the manual, verbose equivalent of Express's `res.sendFile()`.

**Gotcha**: `path.join(process.cwd(), ...)` uses `process.cwd()` (the directory Node was *launched from*), not `__dirname`/`import.meta.url` — path resolution depends on where `node src/server.js` is *run from*, not where the file itself lives.

Module 17's Socket.io setup: `new Server(server)` attaches Socket.io to the *same* HTTP server instance, not `app.listen()` directly. `socket.on('chat-message', msg => io.emit('chat-message', msg))` implements the simplest possible broadcast chat — **note it's `io.emit`, not `socket.broadcast.emit`**, so the sender also receives their own message echoed back. No room/user-tracking here — a minimal single-room broadcast demo.

## 33. `emp-stuff.js` — Module-with-Tests Pair (Fundamentals)

```javascript
const employees = [
    { id: 1, name: 'Sonu', salary: 50000 },
    { id: 2, name: 'Monu', salary: 60000 },
    { id: 3, name: 'Tonu', salary: 70000 }
];

export const calculateBonus = salary => salary * 0.10;
export const getEmployees = () => employees.map(emp => emp.name);
export const findEmployee = id => employees.find(emp => emp.id === id);
export const addNums = (a, b) => a + b;
```
`employees` is a module-scoped constant array — module caching (Section 3) means every `import` of this module shares the exact same array instance (a de facto singleton in-memory "database"). `findEmployee` uses `Array.prototype.find`, which returns the **first** matching element or `undefined` if none match — this `undefined` return-on-miss is directly exercised in the test file below. (A commented-out duplicate using block-bodied arrow functions is kept as a comparison/rollback reference.)

## 34. `emp-stuff.test.js` — Jest Unit Tests

```javascript
import { calculateBonus, getEmployees, findEmployee, addNums } from './emp-stuff.js';

beforeAll(() => console.log('Setup - runs once before all tests'));
afterAll(() => console.log('Teardown - runs once after all tests'));
beforeEach(() => console.log('Setup - runs before each test'));
afterEach(() => console.log('Teardown - runs after each test'));

describe('ems tests suite', () => {
    describe('find employee by id tests', () => {
        it('given id 1, name shoule be Sonu', () => {
            expect(findEmployee(1).name).toBe('Sonu');
        });
        it('given id 100, should return undefined', () => {
            expect(findEmployee(100)).toBeUndefined();
        });
    });

    describe('demo tests', () => {
        it('test addNums', () => {
            expect(addNums(10, 20)).toBe(30);
        });
        it('test addNums negative', () => {
            expect(addNums(10, 20)).not.toBe(35);
        });
    });
});
```
Imports use ESM syntax with an explicit `.js` extension — required in Node ESM (unlike CommonJS `require`, ESM does not auto-resolve extensions). All four Jest lifecycle hooks appear in one file purely to demonstrate execution order: `beforeAll`/`afterAll` run once total (bracketing the whole file), `beforeEach`/`afterEach` run around **every** individual `it`/`test` — none do real setup here, just `console.log`, so students can *watch* the hook order in test output.

`expect(findEmployee(100)).toBeUndefined()` directly exercises the "not found" branch of `Array.prototype.find` — confirms the function returns `undefined` rather than throwing or returning `null`.

**Gotcha**: `'test addNums negative'` is a **poorly named** test — it doesn't actually test negative numbers, it just re-asserts `10+20 ≠ 35` with `.not.toBe()`. A useful example of a misleading test name: names should describe the *behavior under test*, not just be a variant label.

## 35. `send-email.js` — Nodemailer with Gmail SMTP

```javascript
import nodemailer from 'nodemailer';
import fs from 'fs';
const passwordFile = 'D:/Projects/delete/shridhar-gmail-app-password.txt';
const password = fs.readFileSync(passwordFile, 'utf8').trim();
const transporter = nodemailer.createTransport({
    service: 'gmail', auth: { user: senderEmail, pass: password } });

const sendDemoMail = async () => {
    try {
        const info = await transporter.sendMail({ from: senderEmail, to: receiverEmail, subject: mailSubject, text: mailBody });
        console.log('Message ID:', info.messageId);
    } catch (err) { console.error('Failed to send email:', err.message); }
}
sendDemoMail();
```
The Gmail **app password** (not the real account password — Gmail requires a separate 16-char app password for SMTP when 2FA is on) is stored in a plain local text file **outside the repo**, at an absolute Windows path — the exact "read credentials from a local file instead of `.env`" pattern also seen in the Express project's `utils/email.js` (Section 39): a deliberate alternative to environment variables for local dev convenience, though clearly not portable across machines. `fs.readFileSync` here is fine since this is a one-shot script, not a server handling concurrent requests. `service: 'gmail'` lets Nodemailer resolve Gmail's SMTP host/port internally. The script calls itself at the top level — a standalone script meant to be run directly (`node src/send-email.js`), not a reusable module (contrast with `utils/email.js`, which exports a function for other files to call — Section 39).

---

# PART 4 — Project B: Express EMS Backend (Main Project)

Location: `Code/Express/`. This is a full production-style Employee Management System REST API — Express + Mongoose + MongoDB, JWT auth, role-based authorization, Multer file uploads, Nodemailer email, Socket.io real-time notifications, and a Jest + Supertest test suite. It uses **CommonJS** (`require`/`module.exports`) throughout, unlike Project A's ESM style. The `db.js` connection setup and the `Employee`/`Department`/`Project` Mongoose models are covered in a separate MongoDB-focused guide — referenced here by name only, not re-explained schema-field-by-field.

```json
{
  "name": "acme-ems-api",
  "main": "src/app.js",
  "scripts": { "start": "node src/app.js", "dev": "nodemon src/app.js", "test": "jest --runInBand" },
  "dependencies": {
    "bcryptjs": "^2.4.3", "dotenv": "^16.3.1", "express": "^4.18.2", "jsonwebtoken": "^9.0.2",
    "mongoose": "^8.0.3", "multer": "^1.4.5-lts.1", "nodemailer": "^6.9.8", "socket.io": "^4.6.2",
    "validator": "^13.11.0", "cors": "2.8.6"
  },
  "devDependencies": { "jest": "^29.7.0", "nodemon": "^3.0.2", "supertest": "^6.3.4" },
  "jest": { "testEnvironment": "node" }
}
```
`jest --runInBand` forces tests to run **serially** in a single process rather than parallel workers — necessary here because all tests share one MongoDB connection and one Express `app` instance; parallel workers would race on shared DB state (e.g., two test files both trying to register the same admin email). Jest config is embedded directly in `package.json` (alternative to a separate `jest.config.js`); `testEnvironment: 'node'` (as opposed to `'jsdom'`) is correct for a backend-only project with no DOM to simulate.

## 36. `src/app.js` — Full Middleware Stack and Registration Order

```javascript
require('dotenv').config();               // must be first — later requires read process.env at load time
const cors = require('cors');
const express = require('express');
const http = require('http');
const { Server } = require('socket.io');
const path = require('path');

const connectDB = require('./config/db');
const errorHandler = require('./middleware/errorHandler');
const setupSocketIO = require('./sockets/notifications');
const authRoutes = require('./routes/authRoutes');
const employeeRoutes = require('./routes/employeeRoutes');
const departmentRoutes = require('./routes/departmentRoutes');
const projectRoutes = require('./routes/projectRoutes');

const app = express();
const server = http.createServer(app);         // raw server, not app.listen() — Socket.io needs it directly

const io = new Server(server, { cors: { origin: '*' } });
setupSocketIO(io);

connectDB();                                    // not awaited — Mongoose queues ops until ready

app.use(cors({ origin: '*' }));
app.use(express.json());
app.use(express.urlencoded({ extended: true }));
app.use('/uploads', express.static(path.join(__dirname, '..', 'uploads')));  // serves Multer's output (Section 38)

app.use('/api/auth', authRoutes);
app.use('/api/employees', employeeRoutes);
app.use('/api/departments', departmentRoutes);
app.use('/api/projects', projectRoutes);

app.get('/health', (req, res) => res.json({ status: 'OK', timestamp: new Date().toISOString(), uptime: process.uptime(), env: process.env.NODE_ENV }));

app.use((req, res) => res.status(404).json({ error: `Route not found: ${req.method} ${req.path}` }));
app.use(errorHandler);   // must be last

const PORT = process.env.PORT || 3000;
if (process.env.NODE_ENV !== 'test') {
  server.listen(PORT, () => console.log(`EMS API running at http://localhost:${PORT}`));
}
module.exports = { app, server };
```
`const app = express(); const server = http.createServer(app);` — `app.listen()` is shorthand for `http.createServer(app).listen()`, but the raw server is created explicitly here **because** Socket.io needs direct access to it; you cannot attach Socket.io to whatever internal server `app.listen()` would have created implicitly. Socket.io gets its own permissive CORS config (`origin: '*'`), separate from the Express-level `cors()` middleware — its WebSocket handshake isn't covered by Express HTTP middleware.

The **middleware order** mirrors Section 26's pattern: CORS first, then `express.json()`, then `express.urlencoded({ extended: true })` — any route registered before these two would see `req.body` as `undefined`.

**No auth middleware is applied globally** — each router decides internally which routes need `authenticate`/`authorize`, and most routes in this codebase have that check **commented out** (see Sections 40–42).

**Testability pattern from Section 17**: `server.listen()` only runs when `NODE_ENV !== 'test'`, so `tests/employee.test.js` (Section 41) can `require('../src/app')` and pass `app` straight into `supertest(app)` without a real network listener. Both `app` and `server` are exported — `server` too, since Socket.io is attached to it.

## 37. `src/middleware/errorHandler.js` — Express 4-Argument Error Middleware

```javascript
const errorHandler = (err, req, res, next) => {
  console.error('Error:', err.message);

  if (err.name === 'ValidationError') {                          // Mongoose validation error
    const messages = Object.values(err.errors).map((e) => e.message);
    return res.status(400).json({ error: 'Validation failed', details: messages });
  }
  if (err.code === 11000) {                                       // Mongoose duplicate key
    const field = Object.keys(err.keyValue)[0];
    return res.status(409).json({ error: `Duplicate value for field: ${field}` });
  }
  if (err.name === 'JsonWebTokenError') return res.status(401).json({ error: 'Invalid token' });
  if (err.name === 'TokenExpiredError') return res.status(401).json({ error: 'Token expired' });
  if (err.name === 'CastError') return res.status(400).json({ error: `Invalid ${err.path}: ${err.value}` });  // bad ObjectId

  res.status(err.statusCode || 500).json({ error: err.message || 'Internal Server Error' });
};
module.exports = errorHandler;
```
The signature `(err, req, res, next)` has **exactly four parameters**. Express distinguishes error-handling middleware from regular middleware purely by **counting the declared parameters** (function arity) — invoked only when something upstream calls `next(err)`. The unused `next` parameter must remain in the signature or Express would misidentify this as ordinary middleware.

Five error branches classify the failure: `ValidationError` (Mongoose schema validation → 400 with flattened messages), `err.code === 11000` (MongoDB's raw duplicate-key code, `err.keyValue` gives the offending field → 409), `JsonWebTokenError`/`TokenExpiredError` (malformed/tampered token vs. expired `exp` claim, both 401 but distinguished in the message), `CastError` (Mongoose failing to cast a malformed value into a schema type — most commonly a non-ObjectId string as an `:id` param). The fallback responds with `err.statusCode` if the thrower set one (a hand-rolled "operational error" convention), else defaults to 500. This is a near-verbatim implementation of the Express reference's `errorHandler` in Section 27, with a `CastError` branch added on top.

## 38. `src/middleware/upload.js` — Multer Disk Storage Configuration

```javascript
const multer = require('multer');
const path = require('path');
const fs = require('fs');

const uploadDir = process.env.UPLOAD_DIR || 'uploads';
if (!fs.existsSync(uploadDir)) fs.mkdirSync(uploadDir, { recursive: true });

const storage = multer.diskStorage({
  destination: (req, file, cb) => cb(null, uploadDir),
  filename: (req, file, cb) => {
    const ext = path.extname(file.originalname);
    cb(null, `employee_${req.employee._id}_${Date.now()}${ext}`);   // unique, no collision
  },
});

const fileFilter = (req, file, cb) => {
  const allowed = ['image/jpeg', 'image/png', 'image/webp'];
  if (allowed.includes(file.mimetype)) cb(null, true);
  else cb(new Error('Only JPEG, PNG, and WebP images are allowed'), false);
};

const upload = multer({
  storage, fileFilter,
  limits: { fileSize: parseInt(process.env.MAX_FILE_SIZE) || 5 * 1024 * 1024 },
});
module.exports = upload;
```
`fs.existsSync`/`fs.mkdirSync({ recursive: true })` run once **at module-load time**, guaranteeing the upload directory exists before any request tries to write to it.

**Gotcha**: the generated filename reads `req.employee._id`, meaning **this middleware assumes `req.employee` has already been set by an earlier auth middleware**. If `upload.single(...)` were used on a route without `authenticate` running first, this line would throw — and `employeeRoutes.js` (Section 40) does exactly that: the avatar upload route does *not* run `authenticate` before `upload.single('avatar')`.

`fileFilter` allow-lists exactly three MIME types, rejecting via `cb(new Error(...), false)`. This project has no dedicated Multer-error-catching middleware, so Multer errors (e.g. `LIMIT_FILE_SIZE`) propagate to the generic error handler, which has no `MulterError` branch.

## 39. `src/utils/email.js` — Nodemailer with File-Based Credential Fallback

```javascript
const nodemailer = require('nodemailer');
const fs = require('fs');

const passwordFile = 'D:/Projects/delete/shridhar-gmail-app-password.txt';
let EMAIL_PASS = process.env.EMAIL_PASS;   // fallback to .env
try {
  EMAIL_PASS = fs.readFileSync(passwordFile, 'utf-8').trim();
} catch (err) {
  console.warn(`Could not read password file (${err.code}). Falling back to EMAIL_PASS from .env`);
}

const transporter = nodemailer.createTransport({
  host: process.env.EMAIL_HOST || 'smtp.gmail.com',
  port: parseInt(process.env.EMAIL_PORT) || 587,
  secure: false,
  auth: { user: process.env.EMAIL_USER, pass: EMAIL_PASS },
});

const sendWelcomeEmail = async ({ to, firstName }) => {
  const mailOptions = {
    from: `"EMS System" <${process.env.EMAIL_USER}>`, to,
    subject: 'Welcome to the Team!',
    html: `<h2>Hello, ${firstName}!</h2><p>Your EMS account has been created successfully.</p>`,
  };
  if (process.env.NODE_ENV === 'test' || !process.env.EMAIL_USER) {
    console.log(`[EMAIL SKIPPED] Would send welcome mail to ${to}`);
    return;
  }
  await transporter.sendMail(mailOptions);
};
module.exports = { sendWelcomeEmail };
```
`EMAIL_PASS` is initialized from `process.env.EMAIL_PASS` **first**, then a `try/catch` around a **synchronous** `fs.readFileSync` (module-load time) attempts to overwrite it from an absolute local file path (same technique as Project A's `send-email.js`, Section 35). If the file doesn't exist, `catch` quietly falls back to the `.env` value — a graceful-degradation pattern that runs fine on a teammate's machine or in CI, but on the original author's machine silently prefers the file. The transporter is built **once at module scope** and reused across every `sendWelcomeEmail` call.

**Test/dev safety guard**: if running under Jest (`NODE_ENV === 'test'`) or `EMAIL_USER` was never configured, the function logs and returns early **without** calling `transporter.sendMail` — this is why the test suite (Section 41) can register employees repeatedly without hitting a real SMTP server or needing live credentials in CI.

(A fully commented-out earlier version of the file, reading `EMAIL_PASS` purely from `.env` with no file-fallback, is kept as a rollback/comparison reference — same pattern as Project A's `emp-stuff.js`, Section 33.)

## 40. Route Files — `authRoutes.js`, `employeeRoutes.js`, `departmentRoutes.js`, `projectRoutes.js`

### `src/routes/authRoutes.js`

```javascript
const express = require('express');
const router = express.Router();
const Employee = require('../models/Employee');
const { authenticate } = require('../middleware/auth');
const { sendWelcomeEmail } = require('../utils/email');

// Public: create first account (or admin creates employees)
router.post('/register', async (req, res, next) => {
  try {
    const { firstName, lastName, email, password, role } = req.body;
    const employee = new Employee({ firstName, lastName, email, password, role });
    await employee.save();
    const token = await employee.generateAuthToken();
    sendWelcomeEmail({ to: email, firstName }).catch(console.error);   // fire-and-forget
    res.status(201).json({ employee, token });
  } catch (err) { next(err); }
});

router.post('/login', async (req, res, next) => {
  try {
    const { email, password } = req.body;
    if (!email || !password) return res.status(400).json({ error: 'Email and password are required' });
    const employee = await Employee.findByCredentials(email, password);
    const token = await employee.generateAuthToken();
    res.json({ employee, token });
  } catch (err) { res.status(401).json({ error: err.message }); }
});

router.post('/logout', authenticate, async (req, res, next) => {
  try {
    req.employee.tokens = req.employee.tokens.filter((t) => t.token !== req.token);
    await req.employee.save();
    res.json({ message: 'Logged out successfully' });
  } catch (err) { next(err); }
});

// Invalidate ALL tokens (useful when password compromised)
router.post('/logout-all', authenticate, async (req, res, next) => {
  try {
    req.employee.tokens = [];
    await req.employee.save();
    res.json({ message: 'Logged out from all devices' });
  } catch (err) { next(err); }
});

router.get('/me', authenticate, (req, res) => res.json(req.employee));
module.exports = router;
```
`/register` is deliberately **not** protected by `authenticate` — registration must be public — and destructures `role` directly from `req.body` with **no server-side default or restriction**: a malicious client could self-register as `role: 'admin'` unless the `Employee` schema restricts it. Contrast with `employeeRoutes.js`'s POST handler, which explicitly whitelists allowed fields (below). `new Employee({...}).save()` (rather than `Employee.create()`) is used so Mongoose's `pre('save')` hook (password hashing) fires — both forms trigger it equally, so the choice is stylistic. `sendWelcomeEmail({...}).catch(console.error)` is **not** `await`ed — the "don't block the response on email sending" pattern from Module 15.

`/login`'s `catch` block responds directly with `res.status(401).json(...)` rather than calling `next(err)` — always surfaces login failures as `401` regardless of the underlying error, and avoids leaking whether the failure was "email not found" vs "wrong password" (both paths in `findByCredentials` throw the same generic message).

`/logout` and `/logout-all` are both protected by `authenticate`. `/logout` filters the current token **out** of `req.employee.tokens`, which is what makes logout actually invalidate that specific token server-side (a stateless-JWT system with no server tracking couldn't do this — the DB-backed `tokens[]` array enables it, at the cost of a DB lookup per authenticated request). `/logout-all` clears the **entire** array — the "I think my password was compromised" flow. `GET /me` simply returns `req.employee`, already attached by the `authenticate` middleware (Section 42) — no additional DB query needed.

### `src/routes/departmentRoutes.js` — CRUD + Nested Resource + Business-Rule Delete Guard

- **Line 12** — `// router.use(authenticate);` is **commented out**. Per the README, all endpoints except `/health` are supposed to require a Bearer token — but in this file (and identically in `employeeRoutes.js` line 16 and `projectRoutes.js` line 11), the router-wide `authenticate` gate is disabled. Individual write routes still call `authorize('admin')` (e.g. line 67), but **`authorize` reads `req.employee.role`**, and `req.employee` is only ever set by `authenticate` (Section 42) — so with it commented out, `authorize('admin')` actually **throws** (`Cannot read properties of undefined (reading 'role')`) rather than cleanly rejecting with 401/403. GET routes (lines 15, 31, 47) are effectively **fully public**, while POST/PATCH/DELETE routes crash instead of denying.
- **Lines 17–24** — `parseQuery(req.query, [])` (from `utils/queryHelper.js`, the reusable filter/sort/paginate helper matching Module 13's `buildQuery` pattern — Section 14) builds `{ filter, sort, skip, limit, page }` from query string params; `Promise.all([Department.find(...), Department.countDocuments(...)])` runs the page query and total count concurrently.
- **Lines 36–37** — `Employee.countDocuments({ department: dept._id })` is a cross-model count used to enrich the single-department response with a computed `employeeCount` field not stored on the Department document itself.
- **Lines 99–105 (delete guard)** — `Employee.exists({ department: req.params.id })` checks for any employee still assigned to this department **before** allowing deletion; if any exist, responds `409 Conflict` instead of deleting — a referential-integrity rule enforced at the application layer, since MongoDB itself has no foreign-key constraints like SQL's `ON DELETE RESTRICT`.

### `src/routes/employeeRoutes.js` — Sorting/Pagination/Filtering + Field Whitelisting + Role-Conditional Updates + Avatar Upload

- `parseQuery(req.query, ['isActive', 'department', 'role', 'designation'])` — the second argument is an **explicit allow-list of filterable fields**, preventing a client from injecting an arbitrary Mongo filter via unexpected query keys. `.populate('department', 'name code')` joins in just those fields of the referenced Department (Section 12's projection-on-populate pattern).
- **POST — field whitelisting via `reduce`**: `allowed.reduce((acc, key) => { if (req.body[key] !== undefined) acc[key] = req.body[key]; return acc; }, {})` builds a new object containing **only** whitelisted keys present in `req.body` — a direct security best practice preventing mass-assignment of unexpected fields (e.g. `isAdmin: true`). Contrast with `authRoutes.js`'s `/register` handler, which destructures `role` directly with no such whitelist.
- **PATCH — role-conditional field permissions**: `allowedForAdmin = [...allowedForAll, 'salary', 'role', 'department', 'isActive']`, then `allowed = req.employee.role === 'admin' ? allowedForAdmin : allowedForAll`. A fine-grained authorization pattern beyond simple route-level `authorize()`: **which fields** a request may update depends on the caller's role. Non-admins are restricted to cosmetic self-profile fields. This again depends on `req.employee` being set — silently assumes `authenticate` ran, even though it's commented out at the router level.
- **Ownership check**: non-admins may only edit **their own** record; `.toString()` is required because `req.employee._id` is a Mongoose `ObjectId` object, not a string, and must be stringified before comparing to `req.params.id` — a very common Mongoose gotcha (`ObjectId !== string` even for the "same" id).
- **`POST /:id/avatar`** — uses `upload.single('avatar')` (Section 38) as route-level middleware **without** `authenticate` preceding it, directly confirming the coupling flagged in Section 38: Multer's `filename` callback reads `req.employee._id`, but on this route `req.employee` is never set, so **this upload route would throw at the Multer filename-generation step for every request**. It also deletes the employee's previous avatar file (`fs.unlinkSync`) before saving the new one — cleanup preventing orphaned files in `uploads/`.

### `src/routes/projectRoutes.js` — Multi-Ref Populate + Array Manipulation

Chains **two** `.populate()` calls on the same query (`department` and `assignedEmployees`), each projecting different fields. `POST /:id/assign` avoids duplicates: `existing = project.assignedEmployees.map(id => id.toString()); toAdd = employeeIds.filter(id => !existing.includes(id))` — manually implements set-like uniqueness using `filter`+`.includes()` rather than a `Set`, again requiring `.toString()` on the existing ObjectId array before comparing against the incoming plain-string `employeeIds` — same ObjectId-vs-string gotcha as the employee update route above. `DELETE /:id/assign/:employeeId` removes a single employee via `.filter(empId => empId.toString() !== req.params.employeeId)` — the standard "remove by id" idiom for Mongoose ref arrays (no native `.remove()` on plain ObjectId arrays).

## 41. `tests/employee.test.js` — Jest + Supertest Integration Suite

```javascript
const request = require('supertest');
const mongoose = require('mongoose');
const { app } = require('../src/app');
const Employee = require('../src/models/Employee');
const Department = require('../src/models/Department');

beforeAll(async () => {
  const mongoUri = process.env.MONGODB_URI || 'mongodb://localhost:27017/ems_test';
  await mongoose.connect(mongoUri);
});

afterAll(async () => {
  await Employee.deleteMany({ email: /@ems-test\.com$/ });
  await Department.deleteMany({ code: 'TST' });
  await mongoose.connection.close();
});

describe('Auth Routes', () => {
  test('POST /api/auth/register – should create admin', async () => {
    const res = await request(app).post('/api/auth/register').send(adminData).expect(201);
    expect(res.body).toHaveProperty('token');
    expect(res.body.employee.email).toBe(adminData.email);
    expect(res.body.employee).not.toHaveProperty('password');
    adminToken = res.body.token;
  });

  test('GET /api/auth/me – no token returns 401', async () => {
    await request(app).get('/api/auth/me').expect(401);
  });
});
```
Destructures **only** `app` out of the `{ app, server }` export (Section 36); Supertest drives the Express app's request pipeline directly in-process, no bound port needed.

`beforeAll` connects Mongoose **once** for the whole file, hitting `MONGODB_URI` from the environment or falling back to a local `ems_test` database — a **separate test database**, never the production/dev one. `afterAll` cleans up **surgically** rather than dropping the whole DB: deletes only employees matching `/@ems-test\.com$/` and departments with `code: 'TST'`, a regex-scoped teardown that lets the test DB be shared/reused across runs without a full wipe.

The very first test **registers a real admin account** and captures the returned `token` into the outer-scope `adminToken` variable — every subsequent test **depends on this one running first and succeeding**, since Jest runs tests within a `describe` block in file order by default. Tests here are **not independent** (unlike the pure-unit tests in Project A's `emp-stuff.test.js`) — they form a **sequential story** (register → login → CRUD → cleanup). Three assertions matter on the register response: `toHaveProperty('token')`, `.email` equality, and critically `.not.toHaveProperty('password')` — verifying the Employee model's `toJSON` override strips the password hash before serialization, a security-relevant test, not just a data-shape one.

**Gotcha**: the unauthenticated 401 test only passes because `authRoutes.js`'s `/me` route **does** explicitly pass `authenticate` as route-level middleware, independent of any router-wide `.use()`. The suite never exercises "call employee/department endpoints with no token and expect 401" — consistent with those routes genuinely not enforcing auth in this codebase.

The Department and Employee CRUD blocks (not shown) follow an identical Supertest chain: `request(app).<method>(<path>).set('Authorization', ...).send(<body>).expect(<status>)`. Notably **every** write request sets `Authorization` with `adminToken` even though most routes don't actually require it — testing the "happy path with a valid token" but not the authorization gap. A simple smoke test on `GET /health` confirms `res.body.status === 'OK'`.

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
