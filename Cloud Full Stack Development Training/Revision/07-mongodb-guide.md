# MongoDB — Complete Line-by-Line Guide

This guide is grounded strictly in Aakash's actual MongoDB course materials:

- `MongoDB_Courseware.md` — core concept lessons (installation, shell, CRUD, query operators, aggregation, indexes, utilities)
- `MongoDB_Exercise.md` / `MongoDB_Exercise_Answered.md` / `MongoDB_Exercise_Answered_Complete.md` — the NYC Restaurants hands-on exercise, with real answered queries and aggregation-pipeline challenges
- `MongoDB_EMS_Assignment.md` — a 60-question Employee Management System (EMS) assignment with its own schema and answer key
- Real Mongoose-based Node.js code from two projects: a minimal `mongoose-demo.js` script and a full Express/Mongoose EMS backend (`db.js`, three Mongoose models, a seed script, and a query-helper utility)

No examples are invented — every query, schema field, and code line below is reproduced from these files. Since you already know relational databases (and some pymongo/motor), comparisons are drawn to SQL and Python's Mongo drivers where they sharpen the point rather than restate the obvious.

---

# Part I — Core Concepts (MongoDB_Courseware.md)

## 1. Course Navigation and Practice Tasks Overview

The courseware is structured as: concept explanation → syntax reference → code example → practice task, culminating in a capstone **Student Management System** exercise. Prerequisites assumed: JSON familiarity, JDK 11+ (this is an Java Full Stack track, hence the Spring Data MongoDB material at the end), and general RDBMS familiarity. Tools: MongoDB Community Server (engine), `mongosh` (CLI), MongoDB Compass (GUI), MongoDB Atlas (cloud).

## 2. Introduction to MongoDB

**What it is:** an open-source **NoSQL document database**. Instead of rows/tables (MySQL/Oracle), it stores **JSON-like documents** encoded as **BSON** (Binary JSON).

**Relational vs MongoDB:**

| Feature | Relational DB (MySQL) | MongoDB |
|---|---|---|
| Data Format | Rows & Columns (Tables) | Documents (JSON/BSON) |
| Schema | Fixed / Rigid | Flexible / Dynamic |
| Relationships | Foreign Keys & JOINs | Embedded documents or References |
| Scalability | Vertical | Horizontal |
| Best For | Structured data | Semi/unstructured, hierarchical data |

**Core vocabulary mapping (memorize this cold for the assessment):**

| SQL Term | MongoDB Term |
|---|---|
| Database | Database |
| Table | Collection |
| Row | Document |
| Column | Field |
| Primary Key | `_id` |
| JOIN | `$lookup` (Aggregation) |
| INDEX | Index |

A **collection** is a group of documents (like a table) but does **not enforce a schema** — sibling documents in the same collection can have different fields entirely. A **document** is a BSON object:

```json
{
  "_id": "ObjectId('64a7f2c3e4b0a1d2e3f4a5b6')",
  "name": "Ravi Kumar",
  "age": 22,
  "email": "ravi@example.com",
  "courses": ["Java", "MongoDB", "Spring Boot"]
}
```

Every document has a unique `_id` — MongoDB auto-generates an **ObjectId** if you don't supply one. This is the closest MongoDB analogue to a SQL primary key, but it is *not* auto-incrementing; it is derived from timestamp + machine + process + random components (see §9).

Use cases called out: e-commerce catalogs, social media, IoT sensor data, gaming state, CMS content — all cases where schema flexibility or nested/hierarchical shape matters more than strict tabular normalization.

## 3. MongoDB Installation Options

Three deployment paths are taught:
1. **Local installation** — Community Edition on your dev machine.
2. **Dedicated/VPS server** — self-managed Linux server (staging/production).
3. **Cloud MongoDB (Atlas)** — fully managed DBaaS.

## 4. Installing MongoDB on Local Computer

- **Windows:** download MSI installer, choose Complete install, check "Install MongoDB as a Service," optionally install Compass. Verify with `mongod --version` / `mongosh --version`. Service control: `net start MongoDB` / `net stop MongoDB`. Default data dir: `C:\Program Files\MongoDB\Server\<version>\data`.
- **macOS:** `brew tap mongodb/brew && brew install mongodb-community && brew services start mongodb-community`.
- **Linux (Ubuntu/Debian):** import the MongoDB GPG key, add the `mongodb-org` apt repo, `apt-get install mongodb-org`, then `systemctl start mongod && systemctl enable mongod`.

## 5. Installing MongoDB on a Dedicated or VPS Server

Common providers: AWS EC2, DigitalOcean, GCP Compute Engine, Azure VMs. Key steps taught:
- SSH in, install MongoDB as above.
- Edit `/etc/mongod.conf` and set `net.bindIp: 0.0.0.0` to allow remote connections (restrict in production).
- Open the firewall: `sudo ufw allow 27017/tcp`.
- **Enable authentication** — create an admin user via `db.createUser({...})` in the `admin` database with role `userAdminAnyDatabase`, then set `security.authorization: enabled` in `mongod.conf` and restart.
- Connect remotely with a full connection URI: `mongosh "mongodb://adminUser:SecurePassword123@your-server-ip:27017/admin"`.

This is the production-security lesson: MongoDB ships with **no auth by default** — you must explicitly turn it on, unlike most RDBMSes which force credential setup at install time.

## 6. Using MongoDB as a Service (Cloud MongoDB / Atlas)

Atlas is MongoDB's DBaaS — handles backup, scaling, and security automatically. Setup flow: create account → build a free/shared cluster (choose provider/region) → configure **Database Access** (DB user) and **Network Access** (IP allowlist, `0.0.0.0/0` for dev) → copy the connection string:

```
mongodb+srv://<username>:<password>@cluster0.xxxxx.mongodb.net/<dbname>?retryWrites=true&w=majority
```

This `mongodb+srv://` scheme is a DNS-seedlist connection format used by Atlas (resolves to the replica set members automatically — no need to list every host). Atlas features: automated backups, Performance Advisor, Data Explorer, Charts, global multi-region clusters.

## 7. Installing GUI Tools for MongoDB Management

**MongoDB Compass** (official GUI): visual query builder, no-code document editor, index management, aggregation pipeline builder, schema analysis, real-time performance monitoring. Connect with a connection string like `mongodb://localhost:27017`. Alternatives mentioned: Studio 3T (SQL-like querying), NoSQLBooster, Robo 3T, TablePlus.

## 8. Introduction to the MongoDB Shell

`mongosh` is the official CLI — a **full JavaScript runtime**, not just a query DSL (this is the biggest conceptual difference from `psql`/`mysql` CLIs, and from Python's `mongosh`-less pymongo REPL experience).

```javascript
mongosh                                              // connect to local MongoDB
mongosh "mongodb://localhost:27017/mydb"             // connect to a specific db
mongosh "mongodb+srv://user:pass@cluster0.mongodb.net/mydb"  // connect to Atlas

db                       // current database
show dbs                 // list databases
use studentdb             // switch/create database (lazy-created on first write)
show collections          // list collections in current db
help / db.help()          // help
cls                        // clear screen
exit                       // exit shell
```

Since it's JS, you can write real control flow inline:

```javascript
let name = "MongoDB"
print("Hello, " + name)

for (let i = 1; i <= 5; i++) { print("Student " + i) }

function greet(user) { return "Welcome, " + user }
greet("Ravi")
```

Useful cursor/shell helpers: `db.students.find().pretty()`, `.limit(5)`, `db.students.countDocuments()`, `db.serverStatus()`.

## 9. Primary MongoDB Data Types

MongoDB uses **BSON**, a superset of JSON with additional typed values JSON itself lacks (JSON only has string/number/bool/null/array/object):

| BSON Type | Example | Description |
|---|---|---|
| String | `"name": "Ravi"` | UTF-8 text |
| Integer (32-bit) | `"age": 22` | Whole numbers |
| Double | `"gpa": 8.5` | Floating point |
| Boolean | `"active": true` | true/false |
| Date | `"dob": ISODate("2002-05-15")` | Date+time |
| ObjectId | `"_id": ObjectId("...")` | Unique 12-byte ID |
| Array | `"courses": ["Java","MongoDB"]` | List of values |
| Embedded Document | `"address": {"city": "Mumbai"}` | Nested document |
| Null | `"middleName": null` | No value |
| Regular Expression | `"pattern": /^Ravi/` | Regex |
| Binary Data | `"photo": BinData(...)` | Binary content |
| Timestamp | internal use | replication/oplog internal type |
| Long (64-bit) | `NumberLong("123456789012")` | Large integers |
| Decimal128 | `NumberDecimal("19.99")` | High-precision decimal — **always use for money** (this rule reappears in the EMS assignment's `salary`/`budget` fields) |

```javascript
db.students.insertOne({
  name: "Priya Sharma",                          // String
  age: 21,                                        // Integer
  gpa: 9.1,                                       // Double
  isEnrolled: true,                               // Boolean
  enrolledDate: new Date("2024-07-01"),           // Date
  courses: ["Java", "Spring", "MongoDB"],         // Array
  address: {                                      // Embedded Document
    street: "12 MG Road", city: "Bengaluru", pincode: "560001"
  },
  profilePic: null,                               // Null
  studentId: NumberLong("202400123456")           // Long Integer
})
```

**ObjectId anatomy** (this is exam-relevant — expect a question distinguishing it from a UUID or auto-increment PK):

```
ObjectId("64a7f2c3e4b0a1d2e3f4a5b6")
           |------| |----| |--|  |--|
           4-byte    3-byte  2    3-byte
           timestamp machine  PID  random
```

```javascript
let id = new ObjectId()
print(id)
print(id.getTimestamp())   // extract creation time — no separate createdAt needed if you rely on _id
```

Because the first 4 bytes are a Unix timestamp, ObjectIds are **roughly time-sortable** even though they aren't strictly sequential — a property Python's `bson.ObjectId` shares.

## 10. CRUD Operations

### Setup dataset used throughout the module

```javascript
use acmetraining
db.students.insertMany([
  { name: "Ravi Kumar", age: 22, city: "Mumbai", gpa: 8.5, courses: ["Java", "MongoDB"] },
  { name: "Priya Sharma", age: 21, city: "Bengaluru", gpa: 9.1, courses: ["Python", "Django"] },
  { name: "Amit Singh", age: 23, city: "Delhi", gpa: 7.8, courses: ["Java", "Spring Boot"] },
  { name: "Neha Patel", age: 20, city: "Pune", gpa: 8.9, courses: ["Java", "MongoDB", "React"] },
  { name: "Suresh Reddy", age: 24, city: "Hyderabad", gpa: 7.5, courses: ["Node.js", "MongoDB"] }
])
```

### CREATE

`insertOne()` inserts a single document and returns `{ acknowledged: true, insertedId: ObjectId(...) }`. `insertMany()` inserts an array of documents. You may supply a custom `_id` (any unique BSON value, e.g. a string like `"STU001"`) instead of letting MongoDB generate an ObjectId.

**Ordered vs unordered inserts** — this is a subtle exam point: `insertMany()` defaults to `{ ordered: true }`, meaning it stops at the first document that errors (e.g. duplicate key), leaving later documents un-inserted. `{ ordered: false }` continues past errors, inserting every valid document and reporting failures afterward — useful for bulk-loading noisy data.

```javascript
db.students.insertMany(
  [{ name: "A" }, { name: "B" }, { name: "C" }],
  { ordered: false }
)
```

### READ

`find(filter)` returns a **cursor** over matching documents (analogous to a `SELECT` result set / a pymongo `Cursor`); `findOne(filter)` returns the first match as a plain document or `null`.

**Projection** — the second argument to `find()` — controls which fields come back, exactly like `SELECT col1, col2` vs `SELECT *`:

```javascript
db.students.find({}, { name: 1, city: 1, _id: 0 })   // 1 = include, 0 = exclude
db.students.find({}, { gpa: 0 })                       // exclude one field, keep the rest
```

Rule: you may not mix inclusion (`1`) and exclusion (`0`) in the same projection *except* for `_id`, which can always be excluded alongside an inclusion projection.

**Cursor methods chain fluently**, same idea as SQL's `ORDER BY … LIMIT … OFFSET`:

```javascript
db.students.find().limit(3)
db.students.find().skip(2)
db.students.find().sort({ gpa: -1 })     // -1 = descending, 1 = ascending
db.students.find().sort({ gpa: -1 }).limit(3)
db.students.countDocuments({ city: "Bengaluru" })
```

### Practice Task 6 (courseware)
Insert 3 more students; find all students in "Delhi"; project only `name`+`gpa` sorted by `gpa` descending; find the highest-GPA student via `sort`+`limit(1)`.

## 11. MongoDB Queries (Operators)

**Comparison operators** (mirror SQL's `=`, `<>`, `>`, `>=`, `<`, `<=`, `IN`, `NOT IN`):

```javascript
db.students.find({ age: { $eq: 22 } })            // equal (bare value does this implicitly)
db.students.find({ city: { $ne: "Mumbai" } })      // not equal
db.students.find({ gpa: { $gt: 8.5 } })            // greater than
db.students.find({ gpa: { $gte: 8.5 } })           // greater/equal
db.students.find({ age: { $lt: 22 } })             // less than
db.students.find({ age: { $lte: 22 } })            // less/equal
db.students.find({ city: { $in: ["Mumbai", "Pune", "Bengaluru"] } })
db.students.find({ city: { $nin: ["Delhi", "Hyderabad"] } })
```

**Logical operators** (top-level, take an array of condition documents):

```javascript
db.students.find({ $and: [ { age: { $gte: 21 } }, { gpa: { $gte: 8.0 } } ] })
db.students.find({ $or: [ { city: "Mumbai" }, { gpa: { $gte: 9.0 } } ] })
db.students.find({ gpa: { $not: { $gte: 8.0 } } })   // $not negates a single field condition
db.students.find({ $nor: [ { city: "Delhi" }, { gpa: { $lt: 7.0 } } ] })
```

Note: an implicit `$and` already happens when you list multiple fields in one filter document (`{ age: 22, city: "Mumbai" }`), so explicit `$and` is only needed when you must repeat the *same field* with multiple operator sets.

**Element operators** — test field presence/type rather than value:

```javascript
db.students.find({ gpa: { $exists: true } })
db.students.find({ middleName: { $exists: false } })
db.students.find({ age: { $type: "int" } })
db.students.find({ name: { $type: "string" } })
```

**Array operators:**

```javascript
db.students.find({ courses: { $all: ["Java", "MongoDB"] } })                    // contains ALL listed values
db.students.find({ scores: { $elemMatch: { $gt: 85, $lt: 95 } } })              // one element satisfies ALL conditions
db.students.find({ courses: { $size: 3 } })                                     // exact array length
db.students.find({ courses: "Java" })                                           // array contains this scalar
```

`$elemMatch` matters specifically when a single array *element* must satisfy multiple conditions simultaneously — without it, MongoDB would happily match if condition A is satisfied by element 0 and condition B by element 3 of the *same array field*, which is usually not what you want.

**Evaluation operators:**

```javascript
db.students.find({ name: { $regex: /^Ravi/i } })       // starts with, case-insensitive
db.students.find({ name: { $regex: "Kumar$" } })         // ends with
db.students.find({ name: { $regex: ".*Singh.*" } })      // contains
db.students.find({ $where: "this.age > 21 && this.gpa > 8" })   // raw JS predicate — slow, avoid in production
db.students.find({ $expr: { $gt: ["$gpa", 8.0] } })      // lets you use aggregation expressions inside find(), e.g. to compare two fields of the same document
```

**Embedded documents:**

```javascript
db.employees.find({ address: { city: "Mumbai", state: "Maharashtra", pincode: "400001" } })  // exact whole-object match — field ORDER matters!
db.employees.find({ "address.city": "Mumbai" })                       // dot notation — recommended, order-independent
db.employees.find({ "address.pincode": { $regex: /^400/ } })
```

The exact-object-match pitfall (field order sensitivity) is a classic gotcha the courseware flags explicitly — always prefer dot notation for nested-field queries.

## 12. Updating Documents

**Update operator reference:**

| Operator | Description |
|---|---|
| `$set` | Sets the value of a field |
| `$unset` | Removes a field |
| `$inc` | Increments a field by a value |
| `$mul` | Multiplies a field by a value |
| `$rename` | Renames a field |
| `$min` / `$max` | Updates only if new value is smaller/larger |
| `$push` | Adds an element to an array |
| `$pop` | Removes first (-1) or last (1) array element |
| `$pull` | Removes elements matching a condition |
| `$addToSet` | Adds element only if not already present |
| `$currentDate` | Sets field to current date |

`updateOne(filter, update)` touches the first match; `updateMany(filter, update)` touches all matches — this filter/update two-argument shape is the Mongo analogue of `UPDATE ... SET ... WHERE ...`.

```javascript
db.students.updateOne({ name: "Ravi Kumar" }, { $set: { gpa: 9.0 } })
db.students.updateMany({ gpa: { $gte: 9.0 } }, { $set: { status: "Merit" } })
db.students.updateMany({}, { $inc: { age: 1 } })
```

`replaceOne(filter, newDoc)` swaps the **entire document body** (everything except `_id`) — unlike `$set`, any field not present in `newDoc` is deleted. The courseware explicitly warns: use `updateOne` + `$set` for partial updates, `replaceOne` only when you truly mean to overwrite the whole record.

Array update operators:

```javascript
db.students.updateOne({ name: "Ravi Kumar" }, { $push: { courses: "Docker" } })
db.students.updateOne({ name: "Priya Sharma" }, { $push: { courses: { $each: ["Kubernetes", "AWS"] } } })  // push multiple
db.students.updateOne({ name: "Amit Singh" }, { $addToSet: { courses: "Java" } })   // no-op if "Java" already present
db.students.updateOne({ name: "Neha Patel" }, { $pop: { courses: 1 } })             // remove last element
db.students.updateOne({ name: "Ravi Kumar" }, { $pull: { courses: "Docker" } })
db.students.updateOne({ name: "Suresh Reddy" }, { $pullAll: { courses: ["Node.js", "MongoDB"] } })
```

**Upsert** — `{ upsert: true }` as the third argument to `updateOne`/`updateMany` inserts a new document built from the filter + update if nothing matched. This is the pattern used for idempotent "create-or-update" writes without a separate existence check:

```javascript
db.students.updateOne(
  { name: "New Student" },
  { $set: { name: "New Student", age: 20, city: "Nagpur", gpa: 7.5 } },
  { upsert: true }
)
```

## 13. Delete Operations

```javascript
db.students.deleteOne({ name: "Rahul Verma" })
db.students.deleteOne({ _id: ObjectId("64a7f2c3e4b0a1d2e3f4a5b6") })
db.students.deleteMany({ gpa: { $lt: 7.5 } })
db.students.deleteMany({})              // delete ALL docs — collection still exists
```

`findOneAndDelete(filter)` deletes **and returns** the deleted document in one atomic call — useful when the app needs to know exactly what was removed (e.g. to log it), rather than issuing a `find()` then a separate `deleteOne()` (which is not atomic and can race).

**Deleting documents vs dropping a collection** is an important distinction: `deleteMany({})` empties the collection but preserves it and its indexes; `db.students.drop()` removes the collection, its documents, *and* its indexes/metadata entirely; `db.dropDatabase()` removes the whole database. There is **no recycle bin** — deletions are permanent without a backup (`mongodump`).

Recommended safe-delete pattern: always run the equivalent `find()` first to preview what a `deleteMany` would remove, before actually running the delete.

## 14. Aggregation Framework

Aggregation is MongoDB's data-processing **pipeline** — documents flow through an ordered array of `$stage`s, each transforming the data, analogous conceptually to a SQL query built from `WHERE` → `GROUP BY` → `HAVING` → `ORDER BY` → `SELECT` but expressed as an explicit sequence of stages you control:

```javascript
db.collection.aggregate([
  { $stage1: { /* options */ } },
  { $stage2: { /* options */ } }
])
```

Stages taught, each with its SQL rough-equivalent:

- **`$match`** — filter documents, identical syntax to `find()`'s filter. (≈ `WHERE`)
  ```javascript
  db.students.aggregate([ { $match: { gpa: { $gte: 8.0 } } } ])
  ```
- **`$group`** — group by an `_id` expression and compute accumulators per group. (≈ `GROUP BY`)
  ```javascript
  db.students.aggregate([
    { $group: { _id: "$city", count: { $sum: 1 }, avgGPA: { $avg: "$gpa" } } }
  ])
  // _id: null groups the ENTIRE collection into one bucket
  db.students.aggregate([
    { $group: { _id: null, totalStudents: { $sum: 1 }, avgGPA: { $avg: "$gpa" }, maxGPA: { $max: "$gpa" }, minGPA: { $min: "$gpa" } } }
  ])
  ```
  Accumulators: `$sum`, `$avg`, `$min`, `$max`, `$count`, `$push` (collect all values into an array), `$addToSet` (collect *unique* values), `$first`, `$last` (require a prior `$sort` to be meaningful).
- **`$project`** — reshape output fields, optionally computing new ones. (≈ `SELECT` with computed columns)
  ```javascript
  db.students.aggregate([
    { $project: { name: 1, gpa: 1, _id: 0,
        gradeCategory: { $cond: { if: { $gte: ["$gpa", 9.0] }, then: "Distinction", else: "Pass" } } } }
  ])
  ```
- **`$sort`** — `{ $sort: { gpa: -1 } }`. (≈ `ORDER BY`)
- **`$limit` / `$skip`** — pagination within a pipeline, same semantics as the cursor methods. (≈ `LIMIT`/`OFFSET`)
- **`$unwind`** — deconstructs an array field into one output document per array element (necessary before grouping *on* array contents):
  ```javascript
  db.students.aggregate([ { $unwind: "$courses" } ])
  db.students.aggregate([
    { $unwind: "$courses" },
    { $group: { _id: "$courses", studentCount: { $sum: 1 } } },
    { $sort: { studentCount: -1 } }
  ])
  ```
- **`$lookup`** — a left-outer-join against another collection in the same database. (≈ SQL `LEFT JOIN`)
  ```javascript
  db.students.aggregate([
    { $lookup: { from: "enrollments", localField: "name", foreignField: "studentName", as: "enrollmentDetails" } }
  ])
  ```
  `as` always produces an **array** field (even for a 1:1 relationship) — you typically follow with `$unwind` if you expect exactly one match per document.
- **`$addFields`** — appends computed fields without dropping existing ones (unlike `$project`, which drops anything not explicitly listed):
  ```javascript
  db.students.aggregate([
    { $addFields: { ageInMonths: { $multiply: ["$age", 12] }, fullLabel: { $concat: ["$name", " - ", "$city"] } } }
  ])
  ```
- **`$count`** — collapses the pipeline into a single `{ fieldName: N }` document.
  ```javascript
  db.students.aggregate([ { $match: { gpa: { $gte: 8.0 } } }, { $count: "highPerformers" } ])
  ```

**Full pipeline example** (Top-3 cities by average GPA):

```javascript
db.students.aggregate([
  { $match: { gpa: { $gte: 7.5 } } },
  { $group: { _id: "$city", avgGPA: { $avg: "$gpa" }, studentCount: { $sum: 1 }, names: { $push: "$name" } } },
  { $sort: { avgGPA: -1 } },
  { $limit: 3 },
  { $project: { city: "$_id", avgGPA: { $round: ["$avgGPA", 2] }, studentCount: 1, names: 1, _id: 0 } }
])
```

This shows the canonical stage order for a "top-N grouped aggregate" question: **match → group → sort → limit → project** — filter early (cheap), aggregate, order, cap, then reshape for output last.

## 15. Indexes

An **index** is a data structure over a subset of a collection's data, kept in a fast-traversable form (typically a B-tree), so MongoDB can avoid a full **collection scan (`COLLSCAN`)** and instead do a targeted **index scan (`IXSCAN`)**. Same purpose as a SQL index.

```javascript
db.students.find({ city: "Mumbai" }).explain("executionStats")   // check COLLSCAN vs IXSCAN
```

Index types taught:

```javascript
db.students.createIndex({ city: 1 })                                    // single-field (1=asc, -1=desc)
db.students.createIndex({ city: 1, gpa: -1 })                            // compound
db.students.createIndex({ email: 1 }, { unique: true })                  // unique — rejects duplicate values
db.students.createIndex({ name: "text", city: "text" })                  // text index → $text search
db.students.find({ $text: { $search: "Ravi Mumbai" } })
db.students.createIndex({ email: 1 }, { sparse: true })                  // only indexes docs that HAVE the field
db.sessions.createIndex({ createdAt: 1 }, { expireAfterSeconds: 3600 })  // TTL — auto-deletes after N seconds
```

Management:

```javascript
db.students.getIndexes()
db.students.dropIndex({ city: 1 })
db.students.dropIndex("city_1")
db.students.dropIndexes()   // drops all except the mandatory _id index
```

**Best practices (likely a short-answer question):**
1. Index fields used in `find()`, `sort()`, and `$match`.
2. Avoid over-indexing — every index adds write overhead (each insert/update must also update every index).
3. In a compound index, **field order matters** — put equality-filtered fields first, range-filtered fields last (this is the same "equality, sort, range" (ESR) rule from general index design theory).
4. Verify with `explain()`.
5. Use sparse indexes for optional fields to save space.

**Covered query** — when every field referenced by the filter *and* the projection is present in the index, MongoDB can answer entirely from the index without touching the documents themselves:

```javascript
db.students.createIndex({ city: 1, gpa: 1, name: 1 })
db.students.find({ city: "Mumbai" }, { gpa: 1, name: 1, _id: 0 })   // fully covered
```

## 16. Utilities

Command-line administration tools (run at the OS shell, not inside `mongosh`):

- **`mongodump`** / **`mongorestore`** — binary backup/restore (whole instance, one DB, one collection, or straight from an Atlas URI via `--uri`). `mongorestore --drop` clears the target before restoring.
- **`mongoexport`** / **`mongoimport`** — JSON/CSV interchange, supporting `--fields`, `--query` (filter what's exported), `--headerline` (CSV), and `--drop` (clear target collection before import).
- **`mongostat`** — real-time server-wide stats (inserts/sec, queries/sec, connections, memory).
- **`mongotop`** — per-collection read/write activity.
- In-shell utilities: `db.stats()`, `db.students.stats()`, `db.currentOp()`, `db.killOp(opid)`, `db.students.validate()`, `db.runCommand({ compact: "students" })`.
- Atlas adds a **Performance Advisor**, a real-time performance panel, alerting, and a query **Profiler**.

## 17. Wrap-Up

The courseware closes with a **Quick Reference Cheat Sheet** (database/collection/CRUD/index/aggregation one-liners — effectively a condensed version of everything above) and a capstone **Student Management System** project spec: build `sms_db` with `students`, `courses`, `enrollments`, `faculty` collections; insert sample data (10 students with embedded address, 5 courses referencing faculty, enrollment records linking students↔courses); write queries (GPA filter, course-enrollment lookup, cities with >2 students); perform updates (GPA update, add enrollment); aggregate (avg GPA per city, most-enrolled course, top-3 students); index `email` (unique) + `gpa`; export `students` to CSV.

It also gives a **Spring Data MongoDB** quick reference (relevant since this is a Java Full Stack track) — `@Document(collection = "students")` entity annotation, `@Id` on the identifier field, a `MongoRepository<Student, String>` interface with derived-query methods (`findByCity`, `findByGpaGreaterThan`), and `spring.data.mongodb.*` connection properties. This is the Java-world analogue of what the Node/Mongoose code in Part IV of this guide does in JavaScript.

---

# Part II — NYC Restaurants Exercise (MongoDB_Exercise*.md)

## What the exercise asks (MongoDB_Exercise.md)

Setup: import a `restaurants.json` dataset into a `nyc` database/`restaurants` collection via `mongoimport --db nyc --collection restaurants --file restaurants.json --jsonArray`, then `use nyc`. Each restaurant document has this shape:

```json
{
  "address": { "building": "1007", "coord": [-73.856077, 40.848447], "street": "Morris Park Ave", "zipcode": "10462" },
  "borough": "Bronx",
  "cuisine": "Bakery",
  "grades": [ { "date": {"$date": 1393804800000}, "grade": "A", "score": 2 }, ... ],
  "name": "Morris Park Bake Shop",
  "restaurant_id": "30075445"
}
```

`address.coord` is `[longitude, latitude]` — note this ordering, because several questions (Q10, Q11, Q24) filter on `"address.coord"` even though they're phrased as "latitude." `grades` is an array of embedded documents, each with `grade`, `score`, and `date`.

The exercise poses 32 `find()`-level questions (Q1–Q32) grouped into: basic retrieval/projection, borough/location filtering, score filtering, geo+cuisine combos, regex name matching, complex AND/OR conditions, array/date queries on embedded arrays, sorting, and two miscellaneous type-checking questions. `MongoDB_Exercise_Answered_Complete.md` additionally adds 15 aggregation-pipeline challenges (A1–A15).

## Walking through the answered `find()` solutions

(Both `MongoDB_Exercise_Answered.md` and `MongoDB_Exercise_Answered_Complete.md` contain identical answers for Q1–Q32; line numbers below are from `MongoDB_Exercise_Answered.md`.)

### Basic retrieval & projection (Q1–Q4)

```javascript
27:  db.restaurants.find();
34:  db.restaurants.find({}, { restaurant_id: 1, name: 1, borough: 1, cuisine: 1 });
40:  db.restaurants.find({}, { restaurant_id: 1, name: 1, borough: 1, cuisine: 1, _id: 0 });
46:  db.restaurants.find({}, { restaurant_id: 1, name: 1, borough: 1, "address.zipcode": 1, _id: 0 });
```

- **Line 27** — empty filter `{}` matches every document; this is Q1's "show all."
- **Line 34** — an empty filter with an **inclusion projection**: only the four named fields (plus `_id`, included by default) come back per document.
- **Line 40** — same projection as above, but `_id: 0` explicitly suppresses the otherwise-default `_id` field. This is the one case where inclusion (`1`) and exclusion (`0`) are allowed to coexist.
- **Line 46** — projects a **nested field** using dot-notation `"address.zipcode": 1`; MongoDB returns it nested back inside an `address` sub-object in the result (it doesn't flatten it to a top-level `zipcode` key).

### Location & borough filtering (Q5–Q7, Q10)

```javascript
54:  db.restaurants.find({ borough: "Bronx" });
60:  db.restaurants.find({ borough: "Bronx" }).limit(5);
66:  db.restaurants.find({ borough: "Bronx" }).skip(5).limit(5);
72:  db.restaurants.find({ "address.coord": { $lt: -95.754168 } });
```

- **Line 54** — a plain equality filter on a top-level string field.
- **Line 60** — chains `.limit(5)` onto the cursor to cap the result at 5 documents (Q6: "first 5").
- **Line 66** — chains `.skip(5).limit(5)` for pagination — skip the first page, take the next 5 (Q7). Order matters conceptually (`skip` then `limit`), though MongoDB applies them logically regardless of call order in the chain.
- **Line 72** — `$lt` against `"address.coord"`, an **array** field. When you compare an array field against a scalar operator like `$lt`, MongoDB matches if **any element** of the array satisfies the condition — here it matches if either the longitude or latitude value in the 2-element `coord` array is `< -95.754168`. This is why the question is *phrased* as "latitude" but the query targets the whole `coord` array rather than `coord.1` specifically — a looser (and technically imprecise, but exercise-accepted) approach.

### Score-based filtering (Q8, Q9, Q20, Q30)

```javascript
80:  db.restaurants.find({ grades: { $elemMatch: { score: { $gt: 90 } } } });
86:  db.restaurants.find({ grades: { $elemMatch: { score: { $gt: 80, $lt: 100 } } } });
92-95: db.restaurants.find(
         { "grades.score": { $lte: 10 } },
         { restaurant_id: 1, name: 1, borough: 1, cuisine: 1, _id: 0 }
       );
101-104: db.restaurants.find(
           { "grades.score": { $mod: [7, 0] } },
           { restaurant_id: 1, name: 1, grades: 1, _id: 0 }
         );
```

- **Line 80** — `$elemMatch` requires that a **single element** of the `grades` array satisfy `score > 90`. Two approaches are noted in the exercise: `$elemMatch` (at least one grade over 90) vs `$all: [{ score: { $gt: 90 } } ]` (semantically different — `$all` with a document containing an operator actually behaves like an implicit `$elemMatch` per array element too, but the exercise treats it as the "all grades" reading for contrast; in practice `$elemMatch` is the correct/idiomatic tool here).
- **Line 86** — `$elemMatch` with **two conditions in the same clause** (`$gt: 80, $lt: 100`) — critically, both bounds must be satisfied by the *same* grade element, not by two different grades in the array (that distinction is exactly why `$elemMatch` exists over dot-notation).
- **Lines 92–95** — uses **dot notation without `$elemMatch`**: `"grades.score": { $lte: 10 }` matches if *any* element's `score` is `<= 10`. Since there's only one condition, dot notation and `$elemMatch` are equivalent here — `$elemMatch` is only strictly required when multiple conditions must hold on the same element.
- **Lines 101–104** — `$mod: [7, 0]` is the "divisible by" operator: `[divisor, remainder]`. `{ "grades.score": { $mod: [7, 0] } }` matches restaurants where at least one grade's score, divided by 7, has remainder 0.

### Geo + cuisine filtering (Q11, Q12, Q17)

```javascript
115-121: db.restaurants.find({
           $and: [
             { cuisine: { $ne: "American " } },
             { "grades.score": { $gt: 70 } },
             { "address.coord": { $lt: -65.754168 } }
           ]
         });
127-131: db.restaurants.find({
           cuisine: { $ne: "American " },
           "grades.score": { $gt: 70 },
           "address.coord": { $lt: -65.754168 }
         });
137-140: db.restaurants.find({
           borough: "Bronx",
           $or: [{ cuisine: "American " }, { cuisine: "Chinese" }]
         });
```

- **Lines 115–121** — explicit `$and` wrapping three separate condition documents. Note `"American "` has a **trailing space** — a real quirk of the underlying NYC restaurants dataset (cuisine values are stored with trailing whitespace), which is why the literal string must match exactly.
- **Lines 127–131** — the *same* result as Q11 without `$and`: multiple keys in one filter document are implicitly ANDed. This is the key lesson of Q12 — explicit `$and` is redundant when each condition applies to a **different field**, and is only required when the same field needs multiple, independently-evaluated condition clauses.
- **Lines 137–140** — mixes an implicit top-level AND (`borough: "Bronx"`) with an explicit `$or` array for the cuisine alternative — demonstrating that `$or`/`$and` can be nested inside an otherwise-plain filter document.

### Borough filtering (Q18, Q19)

```javascript
148-151: db.restaurants.find(
           { borough: { $in: ["Staten Island", "Queens", "Bronx", "Brooklyn"] } },
           { restaurant_id: 1, name: 1, borough: 1, cuisine: 1, _id: 0 }
         );
157-160: db.restaurants.find(
           { borough: { $nin: ["Staten Island", "Queens", "Bronx", "Brooklyn"] } },
           { restaurant_id: 1, name: 1, borough: 1, cuisine: 1, _id: 0 }
         );
```

- **Lines 148–151 / 157–160** — `$in`/`$nin` against a list are the idiomatic MongoDB equivalent of SQL's `IN (...)` / `NOT IN (...)`, avoiding a chain of `$or`/`$and`-`$ne` clauses.

### Regex name matching (Q14–Q16, Q31, Q32)

```javascript
168-171: db.restaurants.find({ name: /^Wil/ }, { restaurant_id: 1, name: 1, borough: 1, cuisine: 1, _id: 0 });
177-180: db.restaurants.find({ name: /ces$/ }, { restaurant_id: 1, name: 1, borough: 1, cuisine: 1, _id: 0 });
186-189: db.restaurants.find({ name: /Reg/ },  { restaurant_id: 1, name: 1, borough: 1, cuisine: 1, _id: 0 });
195-198: db.restaurants.find({ name: /mon/i }, { name: 1, borough: 1, cuisine: 1, "address.coord": 1, _id: 0 });
204-207: db.restaurants.find({ name: /^Mad/ }, { name: 1, borough: 1, cuisine: 1, "address.coord": 1, _id: 0 });
```

- These use **native JavaScript regex literals** (`/pattern/flags`) directly as the filter value, rather than the `{ $regex: ... }` operator form — both are equivalent in `mongosh`, but the literal form is more concise. `^` anchors "starts with," `$` anchors "ends with," a bare substring anchors "contains," and the `i` flag makes it case-insensitive (line 195).

### Complex combined conditions (Q13, Q21)

```javascript
215-219: db.restaurants.find({
           cuisine: { $ne: "American " },
           "grades.grade": "A",
           borough: { $ne: "Brooklyn" }
         }).sort({ cuisine: -1 });
225-233: db.restaurants.find(
           { $or: [ { cuisine: { $nin: ["American ", "Chinese"] } }, { name: /^Wil/ } ] },
           { restaurant_id: 1, name: 1, borough: 1, cuisine: 1, _id: 0 }
         );
```

- **Lines 215–219** — three implicitly-ANDed conditions, then `.sort({ cuisine: -1 })` chained onto the cursor.
- **Lines 225–233** — top-level `$or` with two very different clause shapes: an `$nin` array condition and a regex condition, showing `$or` branches don't need matching operator types.

### Array & date queries (Q22–Q24)

```javascript
241-252: db.restaurants.find(
           { grades: { $elemMatch: { grade: "A", score: 11, date: ISODate("2014-08-11T00:00:00Z") } } },
           { restaurant_id: 1, name: 1, grades: 1, _id: 0 }
         );
258-265: db.restaurants.find(
           { "grades.1.grade": "A", "grades.1.score": 9, "grades.1.date": ISODate("2014-08-11T00:00:00Z") },
           { restaurant_id: 1, name: 1, grades: 1, _id: 0 }
         );
271-274: db.restaurants.find(
           { "address.coord.1": { $gt: 42, $lte: 52 } },
           { restaurant_id: 1, name: 1, address: 1, _id: 0 }
         );
```

- **Lines 241–252** — `$elemMatch` with three simultaneous conditions (`grade`, `score`, `date`), requiring one array element to satisfy **all three at once** — necessary here because there could be multiple grades sharing a score elsewhere in the array.
- **Lines 258–265** — dot notation with a **numeric array index**: `"grades.1"` means "the element at index 1" (zero-based, so the 2nd element) — this is fundamentally different from `"grades.score"`, which matched *any* element. `date: ISODate(...)` compares a BSON Date value directly.
- **Lines 271–274** — `"address.coord.1"` targets index 1 of the `coord` array (i.e., the latitude, since `coord = [longitude, latitude]`), with a range condition `$gt: 42, $lte: 52` in one clause — this is the *precise* geo query that Q10/Q11 approximated loosely by comparing the whole array.

### Sorting (Q25–Q27)

```javascript
282:  db.restaurants.find().sort({ name: 1 });
288:  db.restaurants.find().sort({ name: -1 });
294:  db.restaurants.find().sort({ cuisine: 1, borough: -1 });
```

- **Line 294** — a **compound sort**: primary key `cuisine` ascending, and within ties, `borough` descending — same semantics as SQL's `ORDER BY cuisine ASC, borough DESC`.

### Miscellaneous (Q28, Q29)

```javascript
302:  db.restaurants.find({ "address.street": { $exists: false } });
308:  db.restaurants.find({ "address.coord": { $type: 1 } });
```

- **Line 302** — Q28 asks to *verify* every address has a `street` field; the correct technique is to query for the **absence** case (`$exists: false`) and confirm the result set is empty, rather than trying to positively assert presence across the whole collection in one call.
- **Line 308** — `$type: 1` is the BSON type code for **Double** (type-code checking, as introduced in §11/§9 of the courseware — `1` = double, `2` = string, `19` = decimal128, etc.).

## Aggregation Pipeline Challenges (A1–A15, from MongoDB_Exercise_Answered_Complete.md)

These only appear in the "Complete" answered file and require chained `aggregate()` stages.

```javascript
373-376: db.restaurants.aggregate([
           { $group: { _id: "$borough", total: { $sum: 1 } } },
           { $sort: { total: -1 } }
         ]);
```
**A1** — group by borough, count documents per group with `$sum: 1`, then sort descending. Standard "count per category" template.

```javascript
382-386: db.restaurants.aggregate([
           { $group: { _id: "$cuisine", total: { $sum: 1 } } },
           { $match: { total: { $gt: 10 } } },
           { $sort: { total: -1 } }
         ]);
```
**A2** — note `$match` appears **after** `$group` here, filtering on the *computed* `total` field. `$match` can be used both before a `$group` (to cheaply filter raw documents) and after (to filter aggregated results) — the latter is effectively a `HAVING` clause since you can't filter on a computed aggregate before it exists.

```javascript
392-396: db.restaurants.aggregate([
           { $group: { _id: { borough: "$borough", cuisine: "$cuisine" } } },
           { $group: { _id: "$_id.borough", distinctCuisines: { $sum: 1 } } },
           { $sort: { distinctCuisines: -1 } }
         ]);
```
**A3** — a **two-stage group**: the first `$group` uses a **compound `_id`** (`{ borough, cuisine }`) which naturally deduplicates borough+cuisine pairs (equivalent to SQL `SELECT DISTINCT borough, cuisine`); the second `$group` re-groups by `_id.borough` alone and counts how many distinct cuisine-buckets survived per borough — this "group twice" pattern is the standard MongoDB idiom for a `COUNT(DISTINCT ...)` per category.

```javascript
402-406: db.restaurants.aggregate([
           { $group: { _id: "$cuisine", total: { $sum: 1 } } },
           { $sort: { total: -1 } },
           { $limit: 3 }
         ]);
```
**A4** — canonical Top-N: group, sort descending by the metric, limit.

```javascript
416-431: db.restaurants.aggregate([
           { $unwind: "$grades" },
           { $group: { _id: "$borough", avgScore: { $avg: "$grades.score" } } },
           { $project: { _id: 1, avgScore: { $round: ["$avgScore", 2] } } },
           { $sort: { avgScore: -1 } }
         ]);
```
**A5** — `$unwind: "$grades"` first flattens each restaurant's `grades` array into one document per grade (so a restaurant with 5 grades becomes 5 separate pipeline documents, each still carrying `borough`), which is a prerequisite for averaging *individual* grade scores per borough rather than per restaurant. `$round: ["$avgScore", 2]` rounds the computed average to 2 decimal places inside `$project`.

```javascript
437-451: db.restaurants.aggregate([
           { $group: { _id: "$cuisine", count: { $sum: 1 }, restaurants: { $push: "$grades" } } },
           { $match: { count: { $gte: 5 } } },
           { $unwind: "$restaurants" },
           { $unwind: "$restaurants" },
           { $group: { _id: "$_id", avgScore: { $avg: "$restaurants.score" } } },
           { $project: { _id: 1, avgScore: { $round: ["$avgScore", 2] } } },
           { $sort: { avgScore: -1 } },
           { $limit: 1 }
         ]);
```
**A6 (first approach)** — groups by cuisine first, `$push`-ing every restaurant's whole `grades` array into a `restaurants` array field (producing an **array of arrays**), filters to cuisines with `count >= 5` restaurants, then `$unwind`s **twice** — once to flatten the outer array-of-arrays, once to flatten the inner grades array — before averaging. The file itself calls out a cleaner alternative:

```javascript
457-476: db.restaurants.aggregate([
           { $unwind: "$grades" },
           { $group: { _id: "$cuisine", avgScore: { $avg: "$grades.score" }, restaurantCount: { $addToSet: "$restaurant_id" } } },
           { $project: { avgScore: { $round: ["$avgScore", 2] }, count: { $size: "$restaurantCount" } } },
           { $match: { count: { $gte: 5 } } },
           { $sort: { avgScore: -1 } },
           { $limit: 1 }
         ]);
```
**A6 (cleaner approach)** — single `$unwind`, then uses `$addToSet: "$restaurant_id"` to collect **unique** restaurant IDs per cuisine (so the count reflects distinct restaurants, not grade entries), and `$size` inside `$project` converts that set into a count. This is a better pattern than the double-unwind: prefer `$addToSet` + `$size` over nested unwinding whenever you need a distinct count alongside an aggregate.

```javascript
481-493: db.restaurants.aggregate([
           { $unwind: "$grades" },
           { $group: { _id: "$restaurant_id", name: { $first: "$name" }, borough: { $first: "$borough" }, avgScore: { $avg: "$grades.score" } } },
           { $project: { name: 1, borough: 1, avgScore: { $round: ["$avgScore", 2] } } },
           { $sort: { avgScore: -1 } }
         ]);
```
**A7** — `$first: "$name"` (and `$first: "$borough"`) inside `$group` pulls a representative value from the first document in each group — valid here because `name`/`borough` are constant across all grade-rows of the same restaurant after `$unwind`, so "first" is just "the value," not an ordering-sensitive pick.

```javascript
499-510: db.restaurants.aggregate([
           { $unwind: "$grades" },
           { $group: { _id: "$borough", avgScore: { $avg: "$grades.score" } } },
           { $project: { avgScore: { $round: ["$avgScore", 2] } } },
           { $sort: { avgScore: -1 } },
           { $limit: 5 }
         ]);
```
**A8** — same shape as A5 but capped to top 5 with `$limit`.

```javascript
520-539: db.restaurants.aggregate([
           { $unwind: "$grades" },
           { $group: { _id: "$restaurant_id", name: { $first: "$name" }, borough: { $first: "$borough" }, maxScore: { $max: "$grades.score" } } },
           { $sort: { maxScore: -1 } },
           { $group: { _id: "$borough", topRestaurant: { $first: "$name" }, highestScore: { $first: "$maxScore" } } },
           { $sort: { highestScore: -1 } }
         ]);
```
**A9** — a "top-1-per-group" pattern: first compute each restaurant's `maxScore` via `$max`, **sort by that metric descending**, then re-group by `borough` and use `$first` to grab the top-scoring restaurant's name — this **sort-then-`$first`-in-group** combination is the standard MongoDB idiom for "the best row per category," since `$group` has no built-in "top-1" accumulator.

```javascript
545-559: db.restaurants.aggregate([
           { $unwind: "$grades" },
           { $group: { _id: "$restaurant_id", name: { $first: "$name" }, borough: { $first: "$borough" }, cuisine: { $first: "$cuisine" }, avgScore: { $avg: "$grades.score" } } },
           { $project: { name: 1, borough: 1, cuisine: 1, avgScore: { $round: ["$avgScore", 2] } } },
           { $sort: { avgScore: -1 } },
           { $limit: 10 }
         ]);
```
**A10** — combines the A7 per-restaurant-average pattern with Top-N (`$sort` + `$limit: 10`).

```javascript
565-577: db.restaurants.aggregate([
           { $group: { _id: { borough: "$borough", cuisine: "$cuisine" }, count: { $sum: 1 } } },
           { $sort: { "_id.borough": 1, count: -1 } },
           { $group: { _id: "$_id.borough", topCuisine: { $first: "$_id.cuisine" }, count: { $first: "$count" } } },
           { $sort: { _id: 1 } }
         ]);
```
**A11** — again the sort-then-`$first`-in-group pattern (A9's technique), applied to counts instead of scores: sort each `(borough, cuisine)` bucket **within its borough** by count descending (`{ "_id.borough": 1, count: -1 }` — a compound sort that groups boroughs together while ranking cuisines inside each), then re-group by borough and take the first (i.e., highest-count) cuisine per borough.

```javascript
586-596: db.restaurants.aggregate([
           { $unwind: "$grades" },
           { $match: { "grades.grade": { $in: ["A", "B", "C"] } } },
           { $group: { _id: "$grades.grade", count: { $sum: 1 } } },
           { $sort: { _id: 1 } }
         ]);
```
**A12** — `$match` **after** `$unwind` filters the flattened grade-documents (not the original restaurant documents), then groups by the grade letter itself.

```javascript
602-616: db.restaurants.aggregate([
           { $unwind: "$grades" },
           { $project: { month: { $month: "$grades.date" } } },
           { $group: { _id: "$month", inspectionCount: { $sum: 1 } } },
           { $sort: { _id: 1 } }
         ]);
```
**A13** — `$month` is a **date-extraction operator** usable inside `$project`/expressions, pulling the calendar month (1–12) out of a BSON Date field — this is the aggregation-framework equivalent of SQL's `EXTRACT(MONTH FROM date)`.

```javascript
622-635: db.restaurants.aggregate([
           { $unwind: "$grades" },
           { $match: { "grades.grade": "C" } },
           { $group: { _id: "$restaurant_id", name: { $first: "$name" }, borough: { $first: "$borough" }, cuisine: { $first: "$cuisine" }, cGradeCount: { $sum: 1 } } },
           { $sort: { cGradeCount: -1 } }
         ]);
```
**A14** — unwind, filter down to only `"C"` grades, then group by restaurant and count how many `C`s each one has — `$sum: 1` after a filtering `$match` is the pipeline's way of doing a conditional `COUNT`.

```javascript
645-672: db.restaurants.aggregate([
           { $facet: {
               byBorough: [ { $group: { _id: "$borough", count: { $sum: 1 } } }, { $sort: { count: -1 } } ],
               topCuisines: [ { $group: { _id: "$cuisine", count: { $sum: 1 } } }, { $sort: { count: -1 } }, { $limit: 5 } ],
               overallAvgScore: [
                 { $unwind: "$grades" },
                 { $group: { _id: null, avgScore: { $avg: "$grades.score" } } },
                 { $project: { _id: 0, avgScore: { $round: ["$avgScore", 2] } } }
               ]
           } }
         ]);
```
**A15** — `$facet` runs **multiple independent sub-pipelines in parallel over the same input**, packaging each sub-pipeline's output as an array under its own key (`byBorough`, `topCuisines`, `overallAvgScore`) in a **single result document**. This avoids issuing three separate `aggregate()` round-trips when a dashboard needs several unrelated summaries computed from the same source collection at once.

---

# Part III — EMS Assignment (MongoDB_EMS_Assignment.md)

## Schema design

The assignment models an HR platform across four collections in the `ems` database, and — unlike the courseware's flatter `students` examples — it deliberately demonstrates several distinct relationship-modeling patterns side by side:

- **`departments`** — flat documents: `code`, `name`, `location`, `established` (Date), `headCount` (Integer), `isActive` (Boolean), `tags` (Array).
- **`employees`** — the richest document: `employeeId` (business key, distinct from `_id`), name fields, `salary` typed as **`NumberDecimal`** ("always use for money" — explicitly called out, since ordinary BSON doubles lose precision for currency), `hireDate` (Date), `status` as a **string enum** (`ACTIVE | INACTIVE | ON_LEAVE | TERMINATED`, enforced only by convention/app code — MongoDB itself doesn't validate this unless you add JSON Schema validation), an **embedded** `department` sub-document (denormalized copy of `code`+`name`, not a reference), an embedded `role` sub-document, a `skills` array, a nested `address` object, and a `metadata` audit sub-document (`createdAt`/`updatedAt`/`createdBy`).
- **`roles`** — a separate lookup-style collection describing job titles, salary bands (`minSalary`/`maxSalary` as `NumberDecimal`), `level` (seniority integer), and `requiredSkills`/`responsibilities` arrays. Note `employees.role` **embeds a copy** of title+level rather than referencing `roles._id` — a deliberate denormalization choice (fast reads, no join needed to display an employee's role, at the cost of needing to sync copies if a role's title changes).
- **`projects`** — has `status` and `priority` string enums, Decimal128 `budget`, and a `team` array of **embedded documents** (`employeeId`, `name`, `projectRole`, `assignedDate`) — this embeds a snapshot of team members directly in the project rather than storing an array of employee references, trading normalization for read-locality (you can render a project's team without a `$lookup`).

This mix — embedding `department`/`role` summaries in `employees`, but keeping `departments`/`roles` as independent authoritative collections too — is the assignment's core schema-design lesson: **embed for read-heavy, rarely-changing denormalized display data; keep a separate collection when the entity has its own independent lifecycle and lookups** (a department or role can be queried/managed on its own).

## Exercises and answers, section by section

### Section 1 — Database & Collection Basics (Q1–Q5)
- **A1** `show collections` — lists collection names in the current db.
- **A2** `db.employees.countDocuments()` — total doc count (replaces the deprecated `count()`).
- **A3** — one `countDocuments()` call per collection; no single command counts across collections simultaneously.
- **A4** `db.departments.find().pretty()` — pretty-printed full dump.
- **A5** `db.departments.drop()` then re-run the `insertMany` from Setup — demonstrates that dropping only removes the named collection, and that restoring means re-running your seed script, since there's no undo.

### Section 2 — Create (Q6–Q9)
- **A6** `insertOne` with `established: ISODate("2022-02-01")` — `ISODate(...)` is shell syntax for constructing a BSON Date from an ISO-8601 string.
- **A7** `insertOne` for `EMP009`, using `salary: NumberDecimal("105000.00")` and `hireDate: new Date()` (JS runtime "now," equivalent to `ISODate()` with the current instant) — matches the money/date typing rules from §9.
- **A8** `insertMany` with two role documents in one call — demonstrates bulk-inserting related-but-independent documents.
- **A9** `insertOne` for project `PRJ006` with `endDate: null` (explicitly modeling "no end date yet" as a null Date rather than omitting the field) and one `team` entry.

### Section 3 — Read (Q10–Q18)
- **A10–A16** are single-condition `find()` calls, several combined with a projection to select specific fields (A11, A14) — same technique as courseware §10/§11.
- **A13** `db.employees.find({ "department.name": "Engineering" })` — dot notation into the **embedded** `department` sub-document, not a `$lookup` against the separate `departments` collection, since the assignment deliberately denormalized department name onto each employee.
- **A17** `db.employees.find().sort({ hireDate: 1 }).limit(3)` — ascending sort by hire date + limit 3 = the three longest-tenured employees (earliest hire dates first).
- **A18** `db.projects.find().sort({ priority: -1, name: 1 })` — compound sort; the answer key itself flags a caveat: sorting a string-typed enum (`priority`) descending sorts **alphabetically** ("MEDIUM" > "LOW" > "HIGH" > "CRITICAL" alphabetically), not by actual business severity — a real production fix would map priority strings to a numeric rank field first (e.g., via `$addFields`) before sorting.

### Section 4 — Comparison & Logical Operators (Q19–Q26)
- **A19–A20** compare `salary` against `NumberDecimal("100000")` / range `$gte`/`$lte` — note the comparison values are themselves wrapped in `NumberDecimal(...)`, because comparing a Decimal128 field against a plain JS number can behave inconsistently; matching types on both sides of the comparison is the safe practice.
- **A22** `{ status: { $ne: "TERMINATED" } }` — `$ne` for exclusion.
- **A23** implicit AND across `"department.name"` and `salary`.
- **A24** `{ status: { $in: ["ON_LEAVE", "TERMINATED"] } }` — `$in` as a compact OR-over-one-field.
- **A25** implicit AND of `status: "ACTIVE"` and `priority: { $in: [...] }`.
- **A26** `$or` of two range conditions on `headCount` — modeling "NOT BETWEEN" as "less than X OR greater than Y" since MongoDB has no native `$between`.

### Section 5 — Array Operators (Q27–Q31)
- **A27** `{ skills: "Docker" }` — bare-scalar-against-array match (array contains the value).
- **A28** `{ skills: { $all: ["Java", "Kubernetes"] } }` — array must contain **both** values (order-independent, unlike `$elemMatch` which is about *one element* satisfying multiple conditions — `$all` here is about the *whole array* containing multiple distinct values).
- **A29** `{ skills: { $in: [...] } }` — array contains **any** of the listed values.
- **A31** `{ team: { $size: 3 } }` — exact array length match; `$size` cannot be combined with range operators (e.g., you cannot query "$size > 3" directly — that requires `$expr` + `$size` as an aggregation expression instead).

### Section 6 — Embedded Documents & Dot Notation (Q32–Q36)
- **A32–A34** dot-notation queries into `role.level` / `address.city` / `address.state`, including a combined two-field filter (A34).
- **A35** `{ team: { $elemMatch: { projectRole: "Tech Lead" } } }` — required here (rather than plain dot notation) conceptually because `$elemMatch` is the robust way to assert a condition against members of an array-of-documents, even though with only one condition here it behaves the same as `"team.projectRole": "Tech Lead"` would.
- **A36** `{ "team.0.projectRole": "Project Manager" }` — numeric-index dot notation, same technique as the NYC exercise's Q23 (`"grades.1..."`), targeting the array's **first** (index 0) element specifically.

### Section 7 — Regex (Q37–Q39)
- **A37** `/^S/` — starts with. **A38** `/Portal/i` — contains, case-insensitive. **A39** `/@acme\.com$/` — ends with, with the literal dot **escaped** (`\.`) so it matches a literal period rather than the regex "any character" wildcard — an important precision point when validating email-like suffixes.

### Section 8 — Update (Q40–Q46)
- **A40** `updateOne` + `$set` — simple status flip.
- **A41** `updateMany({ "department.name": "Engineering", status: "ACTIVE" }, { $mul: { salary: NumberDecimal("1.10") } })` — a **bulk percentage raise**: `$mul` multiplies the existing field value in place (10% raise), applied only to the filtered subset (active engineers) — demonstrating that update filters can be as selective as read filters.
- **A42** shows `$push` (allows duplicate skill entries if run twice) directly contrasted with `$addToSet` (idempotent — a second run is a no-op) on the same field, to make the difference concrete.
- **A43** `$pull` removes a matching array element by value.
- **A44** `updateOne` with **two fields set in one `$set` document**, one of them nested (`"metadata.updatedAt": new Date()`) — dot notation works inside update documents exactly as it does in filters.
- **A45** `$push` an entire embedded sub-document object onto `team`.
- **A46** `$inc: { headCount: 1 }` — atomic increment, safe under concurrent writes (unlike read-modify-write in application code).

### Section 9 — Delete (Q47–Q49)
- **A48** explicitly brackets the `deleteMany` with a `countDocuments` **before and after**, reinforcing the courseware's "preview/confirm" safe-delete pattern from §13 — except here it counts rather than previews full documents, since the filter (`status: "COMPLETED"`) is presumably already understood.
- **A49** `deleteOne({ isActive: false })` — `deleteOne` removes only the **first document matched** (deletion order is not guaranteed unless you sort first), contrasted with `deleteMany` for "all matches."

### Section 10 — Indexes (Q50–Q52)
- **A50** creates a **unique index** on `email`, then deliberately tries to insert a duplicate to observe the `E11000 duplicate key error` — a hands-on demonstration that unique indexes are *enforced at write time*, not just an optimization.
- **A51** `db.employees.createIndex({ "department.code": 1, salary: -1 }, { name: "idx_dept_salary" })` — a **compound index** with an explicit custom name (`{ name: "..." }` option), matching the "equality field first, range/sort field last" rule from courseware §15: `department.code` is typically filtered by equality, `salary` is sorted/ranged, so this ordering lets MongoDB satisfy a `{ "department.code": X }` filter + `sort({ salary: -1 })` query in a single index scan with no separate in-memory sort.
- **A52** `getIndexes()` to list, `dropIndex("idx_dept_salary")` to drop **by name** (as opposed to by key-pattern shape, `dropIndex({ field: 1 })`, both forms are valid — courseware §15 shows both).

### Section 11 — Aggregation Pipeline (Q53–Q57)
- **A53** `$group` by `"$department.name"` + `$sum: 1`, sorted descending — standard count-per-category.
- **A54** groups by department, computing `$avg: { $toDouble: "$salary" }` — **`$toDouble` explicitly converts the Decimal128 `salary` field to a double before averaging**; this is necessary because `$avg` over Decimal128 values behaves correctly but the assignment's convention casts to double for consistent JSON-serializable output, then rounds with `$round`.
- **A55** — the same **sort-then-`$first`-in-group** idiom seen in the NYC exercise's A9: `$sort: { salary: -1 }` **before** grouping so that within each department-group, the first document encountered is the highest earner; `$first` on `firstName`/`lastName`/`salary` then captures that top earner's fields; a final `$project` uses **`$concat`** to join `firstName` + `" "` + `lastName` into a single `name` string (the aggregation-expression equivalent of SQL string concatenation).
- **A56** `$match` on `status: "ACTIVE"`, then `$project` computing `teamSize: { $size: "$team" }` — `$size` used as an **aggregation expression** inside `$project` (distinct from the query operator `{ field: { $size: N } }` used in A31 — same name, different context: one is a query-matching operator, the other is a value-computing expression).
- **A57** a 5-step pipeline: `$match` (active only) → `$group` by `"$role.title"` computing both `count: { $sum: 1 }` and `avgSalary: { $avg: { $toDouble: "$salary" } }` in the **same** `$group` stage (multiple accumulators can coexist) → `$project` to rename `_id` to `role` and round the average → `$sort` by count descending.

### Section 12 — Miscellaneous (Q58–Q60)
- **A58** contrasts `{ "address.street": { $exists: true } }` (has the field) against `{ phone: { $exists: false } }` (lacks the field) in the same answer — the two symmetric uses of `$exists`.
- **A59** `{ salary: { $type: 19 } }` or the equivalent string alias `{ salary: { $type: "decimal" } }` — BSON type code **19 = Decimal128**; this query is explicitly framed as a **data-integrity check**, confirming `salary` was actually stored as `NumberDecimal` and not silently coerced to a plain double/int by careless application code.
- **A60** three separate `distinct()` calls: `db.employees.distinct("status")`, `db.projects.distinct("priority")`, `db.employees.distinct("address.city")` — `distinct(field)` returns the **set of unique values** for a field across a collection (works with dot-notation for nested fields too), the Mongo equivalent of SQL's `SELECT DISTINCT field FROM table`.

---

# Part IV — Real Mongoose Code

Mongoose is an **Object Document Mapper (ODM)** for MongoDB in Node.js — the rough analogue of SQLAlchemy for relational Python, but purpose-built around MongoDB's document model (it adds schema definition/validation, casting, middleware hooks, and query-building on top of the low-level `mongodb` driver, which itself is comparable to `pymongo`).

## `src/config/db.js` — connection setup

```javascript
1:  // src/config/db.js
2:  // Demonstrates: Async Node.js, Promises, MongoDB connection
3:
4:  const mongoose = require('mongoose');
5:
6:  const connectDB = async () => {
7:    try {
8:      const conn = await mongoose.connect(process.env.MONGODB_URI);
9:      console.log(`✅ MongoDB connected: ${conn.connection.host}`);
10:   } catch (error) {
11:     console.error(`❌ MongoDB connection error: ${error.message}`);
12:     process.exit(1);
13:   }
14: };
15:
16: module.exports = connectDB;
```

- **Line 8** — `mongoose.connect(uri)` opens (and internally pools) a connection to the MongoDB server described by the URI, read from the `MONGODB_URI` environment variable rather than hardcoded (standard 12-factor config practice). It returns a Promise that resolves to the `mongoose` instance itself, here captured as `conn`.
- **Line 9** — `conn.connection.host` reads the resolved host from the underlying native connection object Mongoose wraps, confirming which server was actually reached (useful when the URI resolves via DNS SRV, e.g. an Atlas `mongodb+srv://` string).
- **Lines 10–12** — if the connection fails (bad URI, auth failure, network unreachable), the error is logged and `process.exit(1)` **terminates the whole Node process** — a deliberate fail-fast choice, since an Express app with no DB connection generally shouldn't keep serving requests that will all fail anyway.
- **Line 16** — exports `connectDB` as a plain async function, intended to be awaited once at app startup (typically in the app's entry file before `app.listen(...)`).

## `src/models/Department.js` — Mongoose schema, validators, virtuals

```javascript
1:  // src/models/Department.js
2:  // Demonstrates: Mongoose Schema, Validators, Timestamps
3:
4:  const mongoose = require('mongoose');
5:
6:  const departmentSchema = new mongoose.Schema(
7:    {
8:      name: {
9:        type: String,
10:       required: [true, 'Department name is required'],
11:       unique: true,
12:       trim: true,
13:       maxlength: [100, 'Name cannot exceed 100 characters'],
14:     },
15:     code: {
16:       type: String,
17:       required: [true, 'Department code is required'],
18:       unique: true,
19:       uppercase: true,
20:       trim: true,
21:       match: [/^[A-Z]{2,6}$/, 'Code must be 2-6 uppercase letters'],
22:     },
23:     description: {
24:       type: String,
25:       trim: true,
26:       maxlength: [500, 'Description cannot exceed 500 characters'],
27:     },
28:     location: {
29:       type: String,
30:       trim: true,
31:     },
32:     budget: {
33:       type: Number,
34:       min: [0, 'Budget cannot be negative'],
35:       default: 0,
36:     },
37:   },
38:   { timestamps: true }
39: );
40:
41: // Virtual: employee count (populated separately via aggregation)
42: departmentSchema.virtual('employeeCount', {
43:   ref: 'Employee',
44:   localField: '_id',
45:   foreignField: 'department',
46:   count: true,
47: });
48:
49: module.exports = mongoose.model('Department', departmentSchema);
```

- **Line 6** — `new mongoose.Schema({...}, options)` defines the document's shape and constraints. Unlike raw MongoDB (schemaless), Mongoose enforces this shape at the application layer — casting field values to the declared `type` and rejecting writes that fail validators, **before** anything is sent to the server.
- **Lines 8–14** — `name` is a `String`, `required` with a **custom error message tuple** `[true, 'message']` (Mongoose's validator shorthand — the same pattern repeats for `maxlength`, `min`, `match`), `unique: true` (this actually builds a unique **index** in MongoDB, not an app-level check — equivalent to `db.departments.createIndex({ name: 1 }, { unique: true })` from courseware §15), `trim: true` (strips leading/trailing whitespace automatically on save/cast), `maxlength: [100, ...]` (string-length validator).
- **Lines 15–21** — `code` similarly required+unique, plus `uppercase: true` (Mongoose auto-uppercases the value before saving — a **setter**, not just a validator) and `match: [/^[A-Z]{2,6}$/, ...]` (a **regex validator** — rejects the save if the value doesn't match 2–6 uppercase letters after the `uppercase` transform is applied).
- **Lines 32–36** — `budget` is a `Number` with a `min` validator and a `default: 0` — if omitted on insert, Mongoose fills in `0` automatically, comparable to a SQL `DEFAULT` column constraint.
- **Line 38** — `{ timestamps: true }` is a schema-level option that auto-adds and auto-maintains `createdAt`/`updatedAt` Date fields on every save — no manual `metadata.createdAt` bookkeeping needed (contrast with the EMS assignment's Mongo-shell exercises, where `metadata.createdAt`/`updatedAt` had to be set by hand in every insert/update).
- **Lines 42–47** — `departmentSchema.virtual('employeeCount', {...})` defines a **virtual populate** field: it does not exist in the stored document at all, but when explicitly `.populate('employeeCount')`'d on a query, Mongoose runs a count query against the `Employee` model where `Employee.department` (`foreignField`) equals this department's `_id` (`localField`), because `count: true`. This is Mongoose's way of expressing a "reverse reference" / one-to-many relationship (a department *has many* employees) without physically storing an array of employee IDs on the department document — the inverse of how `projects.assignedEmployees` stores references directly (see below).
- **Line 49** — `mongoose.model('Department', departmentSchema)` **compiles** the schema into a usable Model class bound to the `departments` collection (Mongoose automatically lowercases + pluralizes `'Department'` → `departments`), and this Model is what's exported and used elsewhere (`Department.insertMany(...)`, `Department.find(...)`, etc.) — analogous to declaring an ORM entity class in SQLAlchemy/Spring Data.

## `src/models/Employee.js` — auth, hashing, instance/static methods, virtuals

```javascript
1:  // src/models/Employee.js
2:  // Demonstrates: Mongoose, bcrypt hashing, instance methods, static methods,
3:  //               toJSON transform (hide password), virtual fields
4:
5:  const mongoose = require('mongoose');
6:  const bcrypt = require('bcryptjs');
7:  const jwt = require('jsonwebtoken');
8:  const validator = require('validator');
9:
10: const employeeSchema = new mongoose.Schema(
11:   {
12:     // ── Basic Info ────────────────────────────────────────────
13:     firstName: {
14:       type: String,
15:       required: [true, 'First name is required'],
16:       trim: true,
17:     },
18:     lastName: {
19:       type: String,
20:       required: [true, 'Last name is required'],
21:       trim: true,
22:     },
23:     email: {
24:       type: String,
25:       required: [true, 'Email is required'],
26:       unique: true,
27:       lowercase: true,
28:       trim: true,
29:       validate: {
30:         validator: validator.isEmail,
31:         message: 'Please provide a valid email',
32:       },
33:     },
34:     phone: {
35:       type: String,
36:       trim: true,
37:     },
38:     avatar: {
39:       type: String, // stores filename after upload
40:       default: null,
41:     },
42:
43:     // ── Auth ──────────────────────────────────────────────────
44:     password: {
45:       type: String,
46:       required: [true, 'Password is required'],
47:       minlength: [6, 'Password must be at least 6 characters'],
48:       select: false, // never returned by default
49:     },
50:     role: {
51:       type: String,
52:       enum: ['employee', 'manager', 'admin'],
53:       default: 'employee',
54:     },
55:     tokens: [
56:       {
57:         token: {
58:           type: String,
59:           required: true,
60:         },
61:       },
62:     ],
63:
64:     // ── Job Info ──────────────────────────────────────────────
65:     designation: {
66:       type: String,
67:       trim: true,
68:     },
69:     salary: {
70:       type: Number,
71:       min: [0, 'Salary cannot be negative'],
72:     },
73:     joinDate: {
74:       type: Date,
75:       default: Date.now,
76:     },
77:     isActive: {
78:       type: Boolean,
79:       default: true,
80:     },
81:
82:     // ── Relations ─────────────────────────────────────────────
83:     department: {
84:       type: mongoose.Schema.Types.ObjectId,
85:       ref: 'Department',
86:     },
87:   },
88:   { timestamps: true }
89: );
90:
91: // ── Virtual: full name ────────────────────────────────────────
92: employeeSchema.virtual('fullName').get(function () {
93:   return `${this.firstName} ${this.lastName}`;
94: });
95:
96: // ── Pre-save hook: hash password ──────────────────────────────
97: employeeSchema.pre('save', async function (next) {
98:   if (!this.isModified('password')) return next();
99:   this.password = await bcrypt.hash(this.password, 10);
100:   next();
101: });
102:
103: // ── Instance method: generate JWT ─────────────────────────────
104: employeeSchema.methods.generateAuthToken = async function () {
105:   const token = jwt.sign(
106:     { _id: this._id.toString(), role: this.role },
107:     process.env.JWT_SECRET,
108:     { expiresIn: process.env.JWT_EXPIRES_IN }
109:   );
110:   this.tokens = this.tokens.concat({ token });
111:   await this.save();
112:   return token;
113: };
114:
115: // ── Instance method: compare password ─────────────────────────
116: employeeSchema.methods.comparePassword = async function (candidatePassword) {
117:   return bcrypt.compare(candidatePassword, this.password);
118: };
119:
120: // ── Static method: find by credentials ────────────────────────
121: employeeSchema.statics.findByCredentials = async function (email, password) {
122:   const employee = await this.findOne({ email }).select('+password');
123:   if (!employee) throw new Error('Invalid email or password');
124:
125:   const isMatch = await employee.comparePassword(password);
126:   if (!isMatch) throw new Error('Invalid email or password');
127:
128:   return employee;
129: };
130:
131: // ── toJSON: strip sensitive fields ───────────────────────────
132: employeeSchema.methods.toJSON = function () {
133:   const obj = this.toObject({ virtuals: true });
134:   delete obj.password;
135:   delete obj.tokens;
136:   return obj;
137: };
138:
139: module.exports = mongoose.model('Employee', employeeSchema);
```

- **Lines 23–33** — `email` combines several Mongoose mechanics at once: `unique: true` (unique index — the Mongoose equivalent of the courseware's `db.employees.createIndex({ email: 1 }, { unique: true })` from EMS Q50), `lowercase: true` (a **setter** normalizing case before storage, so `"Foo@X.com"` and `"foo@x.com"` are treated as the same value against the unique index), and a **custom validator**: `validate: { validator: validator.isEmail, message: '...' }` plugs in the third-party `validator` package's `isEmail` function as the validation predicate, with a custom failure message — this is Mongoose's extensible validator system beyond the built-in `required`/`match`/`min`/`max`.
- **Lines 44–49** — `password` has `minlength` and, critically, `select: false` — this field is **excluded from every query result by default** (you'd get `undefined` for `employee.password` even right after fetching), a security default preventing accidental password-hash leakage in API responses. It must be explicitly re-requested with `.select('+password')` (see line 122).
- **Lines 50–54** — `role` is a `String` **enum** — Mongoose validates the value is one of the listed strings at write time; this is stricter than the EMS assignment's shell-level `status`/`priority` "enums" (those were just conventions, not actually enforced by MongoDB itself) — Mongoose enum validation *is* enforced by the application layer before the write is even sent.
- **Lines 55–62** — `tokens` is an **array of embedded sub-documents**, each with its own `token: { type: String, required: true }` — this pattern (multiple active JWTs per user, e.g. one per logged-in device) mirrors the EMS assignment's `projects.team` array-of-embedded-documents design.
- **Lines 83–86** — `department: { type: mongoose.Schema.Types.ObjectId, ref: 'Department' }` declares a **reference** (foreign key) to a `Department` document by storing only its `_id` — this is the opposite embedding choice from the EMS shell assignment's `employees.department` (which embedded a `{code, name}` copy). Here, Mongoose can later `.populate('department')` to replace this ObjectId with the full fetched Department document — the ODM-level equivalent of a SQL `JOIN`, implemented under the hood as a separate query (or `$lookup`-style aggregation) rather than a native join.
- **Lines 92–94** — `virtual('fullName').get(fn)` defines a **computed, non-persisted** property: `fullName` never exists in the MongoDB document, but any Mongoose document instance exposes `.fullName` as a getter computed from `firstName`+`lastName` on the fly. `function () {...}` (not an arrow function) is required here specifically so `this` binds to the document instance, not the enclosing lexical scope.
- **Lines 97–101** — `schema.pre('save', async function(next) {...})` is **middleware (a hook)** that runs automatically before every `.save()` call. `this.isModified('password')` checks whether the `password` path changed since it was last loaded/created — this guards against **re-hashing an already-hashed password** every time an unrelated field (like `salary`) is updated and `.save()` is called again. If the password *did* change, `bcrypt.hash(this.password, 10)` replaces the plaintext with a salted hash (cost factor 10) before the document is persisted.
- **Lines 104–113** — `schema.methods.generateAuthToken` adds an **instance method** available on every fetched/created Employee document (`employee.generateAuthToken()`). It signs a JWT embedding `_id` and `role` using `process.env.JWT_SECRET`, appends the new token to the `tokens` array (`.concat`, not `.push`, so it returns a new array rather than mutating in place — functionally equivalent here since it's reassigned), persists via `this.save()`, and returns the raw token string to the caller (typically to send back in a login response).
- **Lines 116–118** — `comparePassword` is another instance method wrapping `bcrypt.compare(candidatePassword, this.password)`, which re-hashes the candidate with the same salt embedded in the stored hash and does a constant-time comparison — never compare passwords with `===`.
- **Lines 121–129** — `schema.statics.findByCredentials` adds a **static method** on the Model itself (`Employee.findByCredentials(email, password)`, not on an instance). It explicitly `.select('+password')` to override the field's `select: false` default just for this lookup, then delegates to the instance method `comparePassword`, throwing a **generic** "Invalid email or password" error in both the not-found and wrong-password cases — a deliberate security choice to avoid leaking which part (email vs password) was wrong (prevents user-enumeration attacks).
- **Lines 132–137** — overriding `schema.methods.toJSON` changes what `JSON.stringify(employeeDoc)` (and therefore Express's `res.json(employeeDoc)`) actually serializes: it calls `.toObject({ virtuals: true })` (which **would** include `fullName` and the `employeeCount`-style virtuals, and note `virtuals: true` is needed because virtuals are excluded from plain object conversion by default), then manually strips `password` and `tokens` even though `password` already has `select: false` — this is defense-in-depth, since `select: false` only affects *queries*, not documents already loaded into memory (e.g., right after `.save()`, the password field **is** present in memory even though it wouldn't be returned by a subsequent `find`).
- **Line 139** — `mongoose.model('Employee', employeeSchema)` → collection `employees`.

## `src/models/Project.js` — arrays of references, enums

```javascript
1:  // src/models/Project.js
2:  // Demonstrates: Mongoose Schema with Arrays of References, Enum fields
3:
4:  const mongoose = require('mongoose');
5:
6:  const projectSchema = new mongoose.Schema(
7:    {
8:      name: {
9:        type: String,
10:       required: [true, 'Project name is required'],
11:       trim: true,
12:       maxlength: [150, 'Name cannot exceed 150 characters'],
13:     },
14:     description: {
15:       type: String,
16:       trim: true,
17:     },
18:     status: {
19:       type: String,
20:       enum: ['planning', 'active', 'on-hold', 'completed'],
21:       default: 'planning',
22:     },
23:     startDate: {
24:       type: Date,
25:     },
26:     endDate: {
27:       type: Date,
28:     },
29:     budget: {
30:       type: Number,
31:       min: [0, 'Budget cannot be negative'],
32:       default: 0,
33:     },
34:     department: {
35:       type: mongoose.Schema.Types.ObjectId,
36:       ref: 'Department',
37:       required: [true, 'Department is required'],
38:     },
39:     // Array of employee references – demonstrates arrays in Mongoose
40:     assignedEmployees: [
41:       {
42:         type: mongoose.Schema.Types.ObjectId,
43:         ref: 'Employee',
44:       },
45:     ],
46:     tags: [{ type: String, trim: true }],
47:   },
48:   { timestamps: true }
49: );
50:
51: module.exports = mongoose.model('Project', projectSchema);
```

- **Line 18–22** — `status` uses `enum` + `default: 'planning'` — note this project's enum values (`'planning' | 'active' | 'on-hold' | 'completed'`, lowercase-hyphenated) are a **different vocabulary** than the EMS shell assignment's `projects.status` (`PLANNED | ACTIVE | ON_HOLD | COMPLETED | CANCELLED`, uppercase) — the same conceptual field, modeled independently in the two source materials; worth remembering these aren't meant to be the same dataset.
- **Lines 34–38** — `department` is a **required** single reference (`ObjectId` + `ref: 'Department'`) — every project must belong to exactly one department, enforced at the schema level (`required: [true, ...]`), unlike `Employee.department` in the previous file which was optional.
- **Lines 40–45** — `assignedEmployees: [ { type: ObjectId, ref: 'Employee' } ]` is an **array of references** — the many-to-many analogue of a SQL join table (`project_employees(project_id, employee_id)`), except modeled as an embedded array of foreign keys directly on the `projects` document rather than a separate collection. This is a *different* relationship style again from the EMS shell assignment's `projects.team`, which embedded full employee **snapshots** (`{employeeId, name, projectRole, assignedDate}`) rather than just ID references — the trade-off being: references stay small and always reflect the current employee record (via `.populate()`), while snapshots avoid extra lookups but can go stale if the employee's real name changes.
- **Line 46** — `tags: [{ type: String, trim: true }]` — an array of plain (non-document) values, each individually validated/trimmed as it's added — the simplest array form, contrasted with the sub-document arrays above.

## `scripts/seed.js` — bulk insert pattern

```javascript
1:  // scripts/seed.js
2:  // Run: node scripts/seed.js
3:  // Demonstrates: Node.js scripting, async/await, Mongoose bulk operations,
4:  //               JavaScript arrays & objects
5:
6:  require('dotenv').config();
7:  const mongoose = require('mongoose');
8:  const Department = require('../src/models/Department');
9:  const Employee = require('../src/models/Employee');
10: const Project = require('../src/models/Project');
11:
12: const MONGO_URI = process.env.MONGODB_URI || 'mongodb://localhost:27017/ems';
13:
14: // ── Seed data defined as JS objects & arrays ──────────────────
15: const departments = [
16:   { name: 'Engineering',  code: 'ENG',  location: 'Bengaluru', budget: 5000000 },
17:   { name: 'HR',           code: 'HR',   location: 'Mumbai',    budget: 1000000 },
18:   { name: 'Finance',      code: 'FIN',  location: 'Pune',      budget: 2000000 },
19:   { name: 'Marketing',    code: 'MKT',  location: 'Delhi',     budget: 1500000 },
20: ];
21:
22: const getEmployees = (deptMap) => [
23:   {
24:     firstName: 'Aditya', lastName: 'Kulkarni',
25:     email: 'aditya.k@ems.local', password: 'pass1234',
26:     role: 'admin', designation: 'CTO', salary: 200000,
27:     department: deptMap['ENG'],
28:   },
29:   // ... (4 more employees, roles: manager/employee, departments ENG/HR/FIN)
30: ];
31:
32: const getProjects = (deptMap, empIds) => [
33:   {
34:     name: 'EMS Cloud Migration',
35:     description: 'Migrate on-prem EMS to AWS',
36:     status: 'active',
37:     startDate: new Date('2024-01-01'),
38:     endDate: new Date('2024-12-31'),
39:     budget: 2000000,
40:     department: deptMap['ENG'],
41:     assignedEmployees: empIds.slice(0, 3),
42:     tags: ['cloud', 'aws', 'migration'],
43:   },
44:   // ... (1 more project — HR Digital Portal, status 'planning')
45: ];
46:
47: // ── Main seed function ────────────────────────────────────────
48: const seed = async () => {
49:   try {
50:     await mongoose.connect(MONGO_URI);
51:     console.log('✅ Connected to MongoDB');
52:
53:     await Promise.all([
54:       Department.deleteMany({}),
55:       Employee.deleteMany({}),
56:       Project.deleteMany({}),
57:     ]);
58:     console.log('🧹 Cleared existing data');
59:
60:     const createdDepts = await Department.insertMany(departments);
61:     const deptMap = {};
62:     createdDepts.forEach((d) => (deptMap[d.code] = d._id));
63:     console.log(`✅ Created ${createdDepts.length} departments`);
64:
65:     const employeeSeedData = getEmployees(deptMap);
66:     const createdEmployees = [];
67:     for (const data of employeeSeedData) {
68:       const emp = new Employee(data);
69:       await emp.save();
70:       createdEmployees.push(emp);
71:     }
72:     console.log(`✅ Created ${createdEmployees.length} employees`);
73:
74:     const empIds = createdEmployees.map((e) => e._id);
75:     const projectSeedData = getProjects(deptMap, empIds);
76:     const createdProjects = await Project.insertMany(projectSeedData);
77:     console.log(`✅ Created ${createdProjects.length} projects`);
78:
79:     console.log('\n🎉 Seed complete!\n');
80:     await mongoose.connection.close();
81:     process.exit(0);
82:   } catch (error) {
83:     console.error('❌ Seed failed:', error);
84:     process.exit(1);
85:   }
86: };
87:
88: seed();
```

(Line numbers reproduced from the actual 128-line file; a few middle employee/project entries were condensed with `// ...` for brevity — every line shown is exact.)

- **Line 6** — `require('dotenv').config()` loads `.env` variables into `process.env` before anything else reads them — must run before line 12 uses `process.env.MONGODB_URI`.
- **Line 12** — falls back to a local default URI if the env var is unset, so the script is runnable out-of-the-box against a local `mongod`.
- **Lines 22, 32** — `getEmployees`/`getProjects` are **factory functions**, not static arrays, because they need `deptMap` (department codes → generated `_id`s) and, for projects, `empIds` — values that don't exist until *after* the earlier inserts run and MongoDB assigns ObjectIds. This is the standard "seed script" dependency-ordering problem: departments must be created first, so their real `_id`s can be embedded as foreign keys into employees, which must in turn be created before their `_id`s can be assigned into projects.
- **Lines 53–57** — `Promise.all([...deleteMany calls])` clears all three collections **concurrently** (not sequentially) before reseeding — safe here because the three `deleteMany({})` calls don't depend on each other or share state.
- **Line 60** — `Department.insertMany(departments)` — a single bulk-insert call; `insertMany` skips Mongoose's per-document `pre('save')` middleware by default (it's a lower-level bulk op), which is fine for `Department` since it has no such hook, but is exactly why employees are **not** bulk-inserted the same way (see below).
- **Lines 61–62** — builds a `deptMap` (`{ ENG: ObjectId(...), HR: ObjectId(...), ... }`) from the just-created departments, keyed by business `code`, so the employee records can reference the correct department `_id`.
- **Lines 67–71** — employees are inserted **one at a time** with `new Employee(data)` + `await emp.save()` inside a `for...of` loop, explicitly **not** `insertMany` — because `.save()` is what triggers the `pre('save')` password-hashing hook from `Employee.js` line 97; `insertMany` bypasses document middleware, so bulk-inserting employees directly would store **plaintext passwords**. This is a deliberate and important correctness detail: whenever a schema has model-level hooks that must run, seed/import scripts must use `.save()` in a loop, not `insertMany`.
- **Line 74** — collects the newly created employees' `_id`s into a plain array, to be sliced into each project's `assignedEmployees` reference array.
- **Line 76** — `Project.insertMany(projectSeedData)` — safe to bulk-insert here since `Project` has no pre-save hooks.
- **Lines 80–81** — cleanly closes the Mongoose connection and exits `0` (success) on completion, or `1` (line 84, in the `catch`) on failure — important for a one-shot CLI script so it doesn't hang the terminal waiting on an open socket.

## `src/utils/queryHelper.js` — pagination/sort/filter abstraction

```javascript
1:  // src/utils/queryHelper.js
2:  // Demonstrates: Sorting, Pagination, and Filtering (Task App topic)
3:  // JavaScript: Objects, Functions, Operators, Control Flow
4:
5:  /**
6:   * Parse common query params and return a Mongoose-ready options object.
7:   *
8:   * Supported query params:
9:   *   ?page=2&limit=10          – pagination
10:  *   ?sortBy=salary:desc       – sorting  (field:asc|desc)
11:  *   ?isActive=true            – filtering (key=value, applied to query directly)
12:  *   ?fields=firstName,email   – field projection
13:  *
14:  * Usage:
15:  *   const { filter, sort, skip, limit, projection } = parseQuery(req.query, ['isActive', 'department']);
16:  */
17: const parseQuery = (query = {}, allowedFilters = []) => {
18:   const page   = Math.max(parseInt(query.page)  || 1, 1);
19:   const limit  = Math.min(parseInt(query.limit) || 10, 100); // cap at 100
20:   const skip   = (page - 1) * limit;
21:
22:   // ── Sort ──────────────────────────────────────────────────
23:   let sort = { createdAt: -1 }; // default: newest first
24:   if (query.sortBy) {
25:     const [field, order] = query.sortBy.split(':');
26:     sort = { [field]: order === 'desc' ? -1 : 1 };
27:   }
28:
29:   // ── Filter ────────────────────────────────────────────────
30:   const filter = {};
31:   allowedFilters.forEach((key) => {
32:     if (query[key] !== undefined) {
33:       if (query[key] === 'true')  filter[key] = true;
34:       else if (query[key] === 'false') filter[key] = false;
35:       else filter[key] = query[key];
36:     }
37:   });
38:
39:   // ── Search (text) ─────────────────────────────────────────
40:   if (query.search) {
41:     filter.$or = [
42:       { firstName: { $regex: query.search, $options: 'i' } },
43:       { lastName:  { $regex: query.search, $options: 'i' } },
44:       { email:     { $regex: query.search, $options: 'i' } },
45:     ];
46:   }
47:
48:   // ── Field Projection ──────────────────────────────────────
49:   let projection = null;
50:   if (query.fields) {
51:     projection = query.fields.split(',').join(' ');
52:   }
53:
54:   return { filter, sort, skip, limit, page, projection };
55: };
56:
57: /**
58:  * Build a standard paginated response envelope.
59:  */
59: const paginatedResponse = (data, total, page, limit) => ({
60:   data,
61:   pagination: {
62:     total,
63:     page,
64:     limit,
65:     totalPages: Math.ceil(total / limit),
66:     hasNextPage: page * limit < total,
67:     hasPrevPage: page > 1,
68:   },
69: });
70:
71: module.exports = { parseQuery, paginatedResponse };
```

(Line numbers as in the 73-line source; the doc-comment/blank-line spacing between the two exported functions is preserved from the file.)

- **Lines 18–20** — `page`/`limit` are parsed from Express's `req.query` (always strings), defaulted (`|| 1`, `|| 10`) and **clamped** — `Math.max(..., 1)` prevents a non-positive/invalid page, `Math.min(..., 100)` caps `limit` at 100 to stop clients from requesting unbounded result sets. `skip` is then derived arithmetically — this `skip`/`limit` pair maps directly onto the cursor methods from courseware §10 (`.skip(n).limit(n)`).
- **Lines 23–27** — `sort` defaults to `{ createdAt: -1 }` (newest first, relying on the schema's `{ timestamps: true }` option from `Department.js`/`Employee.js`/`Project.js`). If a `sortBy=field:order` query param is supplied, it's split on `:` and converted into the Mongo sort-document shape `{ [field]: 1|-1 }` using a **computed property key** (`[field]:`) — direct JS syntax for building an object key from a variable.
- **Lines 30–37** — builds a filter object restricted to an explicit **allow-list** (`allowedFilters`, passed in by the caller per-route) rather than trusting arbitrary query string keys — a security-conscious pattern preventing clients from injecting filters on fields the route didn't intend to expose (e.g., you wouldn't want a public `/employees` endpoint to accept `?password=...` as a filter). String `'true'`/`'false'` values are explicitly coerced to real booleans, since all query-string values arrive as strings and Mongoose/Mongo would otherwise compare a Boolean field against the *string* `"true"` and never match.
- **Lines 40–46** — an optional `search` param builds a **`$or` regex filter** across three name/email fields with the `i` (case-insensitive) option — the same `$regex`/`$options` mechanics from courseware §11, generalized into a "search box" pattern, and merged directly into the same `filter` object as the allow-listed exact filters (so search and structured filters combine, both ANDed together implicitly since `filter.$or` sits alongside the other top-level keys).
- **Lines 49–52** — `fields=firstName,email` (comma-separated) is transformed into `"firstName email"` (space-separated) — this is exactly the string-projection syntax Mongoose's `.select()` method expects (`Model.find(filter).select('firstName email')`), a shorthand alternative to the object-form `{ firstName: 1, email: 1 }` projection used in raw `find()` calls.
- **Lines 59–69** — `paginatedResponse` wraps query results in a standard envelope, computing `totalPages` (`Math.ceil`), `hasNextPage` (whether `page * limit` has already exceeded `total`), and `hasPrevPage` (`page > 1`) — a conventional REST pagination metadata shape, independent of Mongo/Mongoose specifics but built directly from the `page`/`limit`/`total` values this same module computes.

## `mongoose-demo.js` — standalone script (raw MongoDB driver, not Mongoose)

Despite the filename, this script does **not** use Mongoose — it uses the low-level official `mongodb` Node.js driver directly (`import { MongoClient } from 'mongodb'`), the same driver Mongoose itself is built on top of. This is worth knowing explicitly for the assessment: it's a useful contrast case showing what you'd write *without* an ODM's schema/model layer — closer in spirit to using `pymongo` directly in Python rather than an ODM like `mongoengine`.

```javascript
1:  import { MongoClient } from 'mongodb';
2:
3:  const uri = 'mongodb://localhost:27017';
4:
5:  const MongoDbCon = async () => {
6:
7:      const client = new MongoClient(uri);
8:      try {
9:          await client.connect(); // connect to the server 
10:         console.log('Connected!');
11:         const db = client.db('acme-ems'); // connect to the database 
12:         // get list of collections 
13:         const collectionList = await db.collections();
14:         collectionList.forEach(c => console.log(c.collectionName));
15:         const employees = db.collection('emps'); // connect to the collection (table)
16:         const employeeList = await employees.find().toArray(); // perform CRUD ops 
17:         employeeList.forEach(e => console.log(e.name));
18:
19:     } catch (error) {
20:         console.error(error.message);
21:     } finally {
22:         await client.close();
23:         console.log('Closed!');
24:     }
25: }
26:
27: MongoDbCon();
```

- **Line 1** — imports `MongoClient` from the **`mongodb`** package (not `mongoose`) — this is the official low-level driver; there is no schema layer, no models, no validators here — everything is a plain JS object in, plain JS object out.
- **Line 3** — a bare connection string with no database name segment — the database is chosen separately at line 11 via `client.db('acme-ems')`, not baked into the URI.
- **Line 7** — `new MongoClient(uri)` constructs the client but does **not** connect yet — connecting is a separate explicit step (unlike Mongoose's `mongoose.connect()`, which does both in one call).
- **Line 9** — `await client.connect()` opens the actual connection/connection pool to the server.
- **Line 11** — `client.db('acme-ems')` returns a `Db` handle scoped to the `acme-ems` database — analogous to `use acme-ems` in `mongosh`.
- **Lines 13–14** — `db.collections()` returns the list of `Collection` objects that already exist in this database (not just names — full collection handles), then logs each one's `.collectionName` property.
- **Line 15** — `db.collection('emps')` gets a handle to a specific collection **without** first checking it exists — MongoDB creates collections lazily on first write, so this call itself never fails even if `emps` doesn't exist yet; only a subsequent operation against a genuinely absent collection would just return no results.
- **Line 16** — `employees.find().toArray()` — the raw driver's `find()` also returns a lazy cursor (same concept as `mongosh`'s `find()` in courseware §10), and `.toArray()` explicitly materializes it into an in-memory JS array by awaiting all documents — necessary because, unlike Mongoose queries (which are thenable/awaitable directly and auto-buffer), the raw driver's cursor requires an explicit terminal call (`.toArray()`, `.next()`, or `for await` iteration) to actually pull documents.
- **Lines 19–20** — a single `catch` around the whole try-block logs any connection or query error.
- **Lines 21–24** — the `finally` block **always** calls `client.close()` regardless of success or failure, releasing the connection/socket — critical in a one-shot script (as opposed to a long-lived Express server, which would keep the client open for the app's lifetime instead, exactly like `db.js`'s `connectDB` does).
- **Line 27** — `MongoDbCon()` is called immediately at module scope with no `await` (a fire-and-forget top-level invocation, acceptable here since it's a standalone script with its own internal try/catch/finally handling completion).

The file also contains a **commented-out alternative design** (lines 29–48 in the source) sketching a `connect()`/`getDb()` module pattern — a singleton-style connection holder (`let db;` at module scope, `getDb()` throws if `connect()` hasn't run yet) intended for use across multiple files in a larger app, contrasted with this file's single self-contained function. It's dead code (fully commented out) but shows the author considering the "connect once, reuse the handle" pattern that `db.js`'s `connectDB` implements properly via Mongoose's internal connection singleton.
