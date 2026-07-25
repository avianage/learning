# MongoDB — Complete Line-by-Line Guide

This guide is grounded strictly in Aakash's actual MongoDB course materials — no examples are invented; every query, schema field, and code line below is reproduced from these files:

- `MongoDB_Courseware.md` — core concept lessons (installation, shell, CRUD, query operators, aggregation, indexes, utilities)
- `MongoDB_Exercise.md` / `MongoDB_Exercise_Answered.md` / `MongoDB_Exercise_Answered_Complete.md` — the NYC Restaurants hands-on exercise, with real answered queries and aggregation-pipeline challenges
- `MongoDB_EMS_Assignment.md` — a 60-question Employee Management System (EMS) assignment with its own schema and answer key
- Real Mongoose-based Node.js code from two projects: a minimal `mongoose-demo.js` script and a full Express/Mongoose EMS backend (`db.js`, three Mongoose models, a seed script, and a query-helper utility)

Since you already know relational databases (and some pymongo/motor), comparisons are drawn to SQL and Python's Mongo drivers where they sharpen the point rather than restate the obvious.

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

**ObjectId anatomy** (distinguishing it from a UUID or auto-increment PK):

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

**Ordered vs unordered inserts:** `insertMany()` defaults to `{ ordered: true }`, meaning it stops at the first document that errors (e.g. duplicate key), leaving later documents un-inserted. `{ ordered: false }` continues past errors, inserting every valid document and reporting failures afterward — useful for bulk-loading noisy data.

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

**Deleting documents vs dropping a collection** is an important distinction: `deleteMany({})` empties the collection but preserves it and its indexes; `db.students.drop()` removes the collection, its documents, *and* its indexes/metadata entirely; `db.dropDatabase()` removes the whole database. There is **no recycle bin** — deletions are permanent without a backup (`mongodump`). Recommended safe-delete pattern: always run the equivalent `find()` first to preview what a `deleteMany` would remove, before actually running the delete.

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

**Best practices:**
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
27:  db.restaurants.find();                                                          // {} matches every document
40:  db.restaurants.find({}, { restaurant_id: 1, name: 1, borough: 1, cuisine: 1, _id: 0 });
46:  db.restaurants.find({}, { restaurant_id: 1, name: 1, borough: 1, "address.zipcode": 1, _id: 0 });
```

`_id: 0` alongside an inclusion projection is the one case where inclusion (`1`) and exclusion (`0`) may coexist — it explicitly suppresses the otherwise-default `_id`. Projecting a nested field via dot-notation (`"address.zipcode": 1`, line 46) returns it nested back inside an `address` sub-object, not flattened to a top-level key.

### Location & borough filtering (Q5–Q7, Q10)

```javascript
66:  db.restaurants.find({ borough: "Bronx" }).skip(5).limit(5);   // pagination: skip page 1, take next 5
72:  db.restaurants.find({ "address.coord": { $lt: -95.754168 } });
```

Line 66 chains `.skip(5).limit(5)` for pagination (Q6/Q7 build up to this from a plain `.limit(5)`). Line 72's `$lt` targets `"address.coord"`, an **array** field — comparing an array against a scalar operator matches if **any element** satisfies the condition, so this matches on either longitude or latitude in the 2-element `coord` array being `< -95.754168`. That's why the question is *phrased* as "latitude" but the query targets the whole array rather than `coord.1` — looser and technically imprecise, but exercise-accepted (compare the precise version in Q24 below).

### Score-based filtering (Q8, Q9, Q20, Q30)

```javascript
86:  db.restaurants.find({ grades: { $elemMatch: { score: { $gt: 80, $lt: 100 } } } });
92:  db.restaurants.find({ "grades.score": { $lte: 10 } }, { restaurant_id: 1, name: 1, borough: 1, cuisine: 1, _id: 0 });
101: db.restaurants.find({ "grades.score": { $mod: [7, 0] } }, { restaurant_id: 1, name: 1, grades: 1, _id: 0 });
```

Line 86's `$elemMatch` with **two conditions in the same clause** requires both bounds be satisfied by the *same* grade element, not by two different grades in the array — that's exactly why `$elemMatch` exists over plain dot notation. Line 92 shows the contrast: dot notation without `$elemMatch` (`"grades.score": { $lte: 10 }`) matches if *any* element's score qualifies — fine with a single condition, but `$elemMatch` becomes mandatory once multiple conditions must hold on the same element. Line 101's `$mod: [7, 0]` is the "divisible by" operator — `[divisor, remainder]` — matching restaurants where at least one grade's score divided by 7 has remainder 0.

### Geo + cuisine filtering (Q11, Q12, Q17)

```javascript
127: db.restaurants.find({ cuisine: { $ne: "American " }, "grades.score": { $gt: 70 }, "address.coord": { $lt: -65.754168 } });
137: db.restaurants.find({ borough: "Bronx", $or: [{ cuisine: "American " }, { cuisine: "Chinese" }] });
```

Note `"American "` carries a **trailing space** — a real quirk of the underlying dataset, so the literal string must match exactly. Q11 also shows this same filter wrapped in an explicit `$and: [...]`; Q12 (line 127) proves it's redundant here, since multiple keys in one filter document are implicitly ANDed — explicit `$and` is only required when the *same field* needs multiple, independently-evaluated condition clauses. Line 137 mixes an implicit top-level AND (`borough`) with an explicit nested `$or` for the cuisine alternative, showing `$or`/`$and` can nest inside an otherwise-plain filter document.

### Borough filtering (Q18, Q19)

```javascript
148: db.restaurants.find({ borough: { $in: ["Staten Island", "Queens", "Bronx", "Brooklyn"] } }, { restaurant_id: 1, name: 1, borough: 1, cuisine: 1, _id: 0 });
```

`$in`/`$nin` (Q19 is the same query with `$nin`) against a list are the idiomatic MongoDB equivalent of SQL's `IN (...)` / `NOT IN (...)`, avoiding a chain of `$or`/`$and`-`$ne` clauses.

### Regex name matching (Q14–Q16, Q31, Q32)

```javascript
168: db.restaurants.find({ name: /^Wil/ }, { restaurant_id: 1, name: 1, borough: 1, cuisine: 1, _id: 0 });   // starts with
177: db.restaurants.find({ name: /ces$/ }, ...);   // ends with
195: db.restaurants.find({ name: /mon/i }, { name: 1, borough: 1, cuisine: 1, "address.coord": 1, _id: 0 }); // contains, case-insensitive
```

These use **native JavaScript regex literals** (`/pattern/flags`) directly as the filter value, rather than the `{ $regex: ... }` operator form — both are equivalent in `mongosh`, but the literal form is more concise. `^` anchors "starts with," `$` anchors "ends with," a bare substring anchors "contains," and the `i` flag makes it case-insensitive.

### Complex combined conditions (Q13, Q21)

```javascript
215: db.restaurants.find({ cuisine: { $ne: "American " }, "grades.grade": "A", borough: { $ne: "Brooklyn" } }).sort({ cuisine: -1 });
225: db.restaurants.find({ $or: [ { cuisine: { $nin: ["American ", "Chinese"] } }, { name: /^Wil/ } ] }, { restaurant_id: 1, name: 1, borough: 1, cuisine: 1, _id: 0 });
```

Line 225's top-level `$or` mixes two very different clause shapes — an `$nin` array condition and a regex condition — showing `$or` branches don't need matching operator types.

### Array & date queries (Q22–Q24)

```javascript
258: db.restaurants.find({ "grades.1.grade": "A", "grades.1.score": 9, "grades.1.date": ISODate("2014-08-11T00:00:00Z") }, { restaurant_id: 1, name: 1, grades: 1, _id: 0 });
271: db.restaurants.find({ "address.coord.1": { $gt: 42, $lte: 52 } }, { restaurant_id: 1, name: 1, address: 1, _id: 0 });
```

Q22 (not shown) uses `$elemMatch` with three simultaneous conditions (`grade`, `score`, `date`) to require one array element satisfy all three at once. Line 258 contrasts that with dot notation using a **numeric array index** — `"grades.1"` means "the element at index 1" (zero-based), fundamentally different from `"grades.score"` which matches *any* element; `date: ISODate(...)` compares a BSON Date value directly. Line 271's `"address.coord.1"` targets index 1 of the `coord` array (the latitude, since `coord = [longitude, latitude]`) with a range condition in one clause — the *precise* geo query that Q10/Q11 approximated loosely by comparing the whole array.

### Sorting (Q25–Q27)

```javascript
294:  db.restaurants.find().sort({ cuisine: 1, borough: -1 });
```

A **compound sort**: primary key `cuisine` ascending, and within ties, `borough` descending — same semantics as SQL's `ORDER BY cuisine ASC, borough DESC`. (Q25/Q26 are the single-field ascending/descending forms of the same `.sort()` call.)

### Miscellaneous (Q28, Q29)

```javascript
302:  db.restaurants.find({ "address.street": { $exists: false } });
308:  db.restaurants.find({ "address.coord": { $type: 1 } });
```

Q28 asks to *verify* every address has a `street` field; the correct technique is to query for the **absence** case (`$exists: false`) and confirm the result set is empty, rather than trying to positively assert presence across the whole collection. Line 308's `$type: 1` is the BSON type code for **Double** (type-code checking, as introduced in §9/§11 — `1` = double, `2` = string, `19` = decimal128, etc.).

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

**A4** — canonical Top-N: `$group` then `$sort` descending by the metric then `$limit: 3`. This match→group→sort→limit shape recurs throughout the rest of the pipeline exercises (A8, A10) with different metrics.

```javascript
416-431: db.restaurants.aggregate([
           { $unwind: "$grades" },
           { $group: { _id: "$borough", avgScore: { $avg: "$grades.score" } } },
           { $project: { _id: 1, avgScore: { $round: ["$avgScore", 2] } } },
           { $sort: { avgScore: -1 } }
         ]);
```
**A5** — `$unwind: "$grades"` first flattens each restaurant's `grades` array into one document per grade (so a restaurant with 5 grades becomes 5 separate pipeline documents, each still carrying `borough`), which is a prerequisite for averaging *individual* grade scores per borough rather than per restaurant. `$round: ["$avgScore", 2]` rounds the computed average to 2 decimal places inside `$project`.

**A6 (first approach, not shown)** — groups by cuisine first, `$push`-ing every restaurant's whole `grades` array into a `restaurants` field (producing an **array of arrays**), filters to cuisines with `count >= 5` restaurants, then `$unwind`s **twice** — once to flatten the outer array-of-arrays, once to flatten the inner grades array — before averaging. The file itself calls out a cleaner alternative:

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

**A8 (not shown)** — same shape as A5 but capped to top 5 with `$limit`.

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

**A10 (not shown)** — combines the A7 per-restaurant-average pattern with Top-N (adds `cuisine` to the grouped fields, `$sort` + `$limit: 10`).

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
- **`employees`** — the richest document: `employeeId` (business key, distinct from `_id`), name fields, `salary` typed as **`NumberDecimal`** (money always uses Decimal128 — ordinary BSON doubles lose precision for currency), `hireDate` (Date), `status` as a **string enum** (`ACTIVE | INACTIVE | ON_LEAVE | TERMINATED`, enforced only by convention/app code — MongoDB itself doesn't validate this unless you add JSON Schema validation), an **embedded** `department` sub-document (denormalized copy of `code`+`name`, not a reference), an embedded `role` sub-document, a `skills` array, a nested `address` object, and a `metadata` audit sub-document (`createdAt`/`updatedAt`/`createdBy`).
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

`mongoose.connect(uri)` (line 8) opens and internally pools a connection to the server described by the `MONGODB_URI` env var (standard 12-factor config, rather than hardcoding), returning a Promise that resolves to the `mongoose` instance itself. If the connection fails, the error is logged and `process.exit(1)` **terminates the whole Node process** (lines 10–12) — a deliberate fail-fast choice, since an Express app with no DB connection shouldn't keep serving requests that will all fail anyway. `connectDB` is exported as a plain async function, intended to be awaited once at app startup before `app.listen(...)`.

## `src/models/Department.js` — Mongoose schema, validators, virtuals

```javascript
const departmentSchema = new mongoose.Schema(
  {
    name: { type: String, required: [true, 'Department name is required'], unique: true, trim: true, maxlength: [100, 'Name cannot exceed 100 characters'] },
    code: { type: String, required: true, unique: true, uppercase: true, trim: true, match: [/^[A-Z]{2,6}$/, 'Code must be 2-6 uppercase letters'] },
    description: { type: String, trim: true, maxlength: 500 },
    location: { type: String, trim: true },
    budget: { type: Number, min: [0, 'Budget cannot be negative'], default: 0 },
  },
  { timestamps: true }
);

// Virtual: employee count (populated separately via aggregation)
departmentSchema.virtual('employeeCount', { ref: 'Employee', localField: '_id', foreignField: 'department', count: true });

module.exports = mongoose.model('Department', departmentSchema);
```

`new mongoose.Schema({...}, options)` defines the document's shape and constraints. Unlike raw MongoDB (schemaless), Mongoose enforces this shape at the application layer — casting field values to the declared `type` and rejecting writes that fail validators **before** anything is sent to the server. Field-level options seen here: `required`/`maxlength`/`min`/`match` all accept a **`[value, 'custom message']` tuple** (Mongoose's validator shorthand); `unique: true` actually builds a unique **index** in MongoDB, not just an app-level check — equivalent to `db.departments.createIndex({ name: 1 }, { unique: true })` from courseware §15; `trim: true` strips whitespace automatically on save/cast; `uppercase: true` on `code` is a **setter** (transforms the value before validation/save, not just a check), so the `match` regex validator runs *after* the uppercase transform; `default: 0` on `budget` fills in a value when omitted, comparable to a SQL `DEFAULT` constraint.

`{ timestamps: true }` auto-adds and maintains `createdAt`/`updatedAt` Date fields on every save — no manual bookkeeping needed (contrast with the EMS shell assignment, where `metadata.createdAt`/`updatedAt` had to be set by hand).

`departmentSchema.virtual('employeeCount', {...})` defines a **virtual populate** field: it doesn't exist in the stored document, but when explicitly `.populate('employeeCount')`'d, Mongoose runs a count query against the `Employee` model where `Employee.department` (`foreignField`) equals this department's `_id` (`localField`), because `count: true`. This is Mongoose's way of expressing a "reverse reference" / one-to-many relationship without physically storing an array of employee IDs on the department — the inverse of how `projects.assignedEmployees` stores references directly (see below).

`mongoose.model('Department', departmentSchema)` **compiles** the schema into a Model class bound to the `departments` collection (Mongoose auto-lowercases + pluralizes `'Department'` → `departments`) — analogous to declaring an ORM entity class in SQLAlchemy/Spring Data.

## `src/models/Employee.js` — auth, hashing, instance/static methods, virtuals

```javascript
const employeeSchema = new mongoose.Schema(
  {
    firstName: { type: String, required: true, trim: true },
    lastName:  { type: String, required: true, trim: true },
    email: {
      type: String, required: true, unique: true, lowercase: true, trim: true,
      validate: { validator: validator.isEmail, message: 'Please provide a valid email' },
    },
    phone: { type: String, trim: true },
    avatar: { type: String, default: null },   // stores filename after upload

    password: { type: String, required: true, minlength: 6, select: false },  // never returned by default
    role: { type: String, enum: ['employee', 'manager', 'admin'], default: 'employee' },
    tokens: [ { token: { type: String, required: true } } ],

    designation: { type: String, trim: true },
    salary: { type: Number, min: 0 },
    joinDate: { type: Date, default: Date.now },
    isActive: { type: Boolean, default: true },

    department: { type: mongoose.Schema.Types.ObjectId, ref: 'Department' },
  },
  { timestamps: true }
);

employeeSchema.virtual('fullName').get(function () {
  return `${this.firstName} ${this.lastName}`;
});

// Pre-save hook: hash password
employeeSchema.pre('save', async function (next) {
  if (!this.isModified('password')) return next();
  this.password = await bcrypt.hash(this.password, 10);
  next();
});

// Instance methods
employeeSchema.methods.generateAuthToken = async function () {
  const token = jwt.sign({ _id: this._id.toString(), role: this.role }, process.env.JWT_SECRET, { expiresIn: process.env.JWT_EXPIRES_IN });
  this.tokens = this.tokens.concat({ token });
  await this.save();
  return token;
};
employeeSchema.methods.comparePassword = async function (candidatePassword) {
  return bcrypt.compare(candidatePassword, this.password);
};

// Static method
employeeSchema.statics.findByCredentials = async function (email, password) {
  const employee = await this.findOne({ email }).select('+password');
  if (!employee || !(await employee.comparePassword(password))) throw new Error('Invalid email or password');
  return employee;
};

// toJSON: strip sensitive fields
employeeSchema.methods.toJSON = function () {
  const obj = this.toObject({ virtuals: true });
  delete obj.password;
  delete obj.tokens;
  return obj;
};

module.exports = mongoose.model('Employee', employeeSchema);
```

`email` combines several Mongoose mechanics at once: `unique: true` builds a unique **index** (equivalent to `db.employees.createIndex({ email: 1 }, { unique: true })` from EMS Q50); `lowercase: true` is a **setter** normalizing case before storage, so `"Foo@X.com"` and `"foo@x.com"` collide correctly against the unique index; and `validate: { validator: validator.isEmail, message }` plugs the third-party `validator` package's `isEmail` in as a **custom validator** — Mongoose's extensible validation system beyond the built-in `required`/`match`/`min`/`max`.

`password` combines `minlength` with `select: false` — this field is **excluded from every query result by default** (`employee.password` is `undefined` even right after fetching), a security default preventing accidental password-hash leakage in API responses, and must be explicitly re-requested with `.select('+password')`.

`role` is a `String` **enum** — Mongoose validates the value is one of the listed strings *before the write is even sent*, which is stricter than the EMS shell assignment's `status`/`priority` "enums" (those were just conventions, never enforced by MongoDB itself). `tokens` is an **array of embedded sub-documents** — the same array-of-embedded-documents shape as the EMS assignment's `projects.team`, here used to hold multiple active JWTs per user (e.g. one per logged-in device).

`department: { type: ObjectId, ref: 'Department' }` declares a **reference** (foreign key) by storing only the `_id` — the opposite embedding choice from the EMS shell assignment's `employees.department`, which embedded a `{code, name}` copy. Mongoose can later `.populate('department')` to replace the ObjectId with the full fetched document — the ODM equivalent of a SQL `JOIN`, implemented as a separate query under the hood rather than a native join.

`virtual('fullName').get(fn)` defines a **computed, non-persisted** property — never stored, but exposed as `.fullName` on any document instance. The getter must be a `function` (not an arrow function) so `this` binds to the document instance.

`pre('save', async function(next) {...})` is **middleware (a hook)** that runs before every `.save()`. `this.isModified('password')` guards against **re-hashing an already-hashed password** whenever an unrelated field is updated and `.save()` runs again; only a genuine password change triggers `bcrypt.hash(..., 10)` (salted hash, cost factor 10).

`generateAuthToken` (an **instance method**, `employee.generateAuthToken()`) signs a JWT embedding `_id`/`role`, appends it to `tokens`, persists, and returns the raw token. `comparePassword` wraps `bcrypt.compare`, which re-hashes the candidate with the stored hash's embedded salt and does a constant-time comparison — never compare password hashes with `===`.

`findByCredentials` is a **static method** on the Model itself (`Employee.findByCredentials(email, password)`, not an instance). It overrides `select: false` with `.select('+password')` for this one lookup, then throws a **generic** "Invalid email or password" error whether the email wasn't found or the password was wrong — deliberately avoiding leaking which one failed (prevents user-enumeration attacks).

Overriding `toJSON` controls what `JSON.stringify`/Express's `res.json()` actually serializes: `.toObject({ virtuals: true })` includes computed virtuals (excluded by default), then `password`/`tokens` are stripped manually — defense-in-depth, since `select: false` only affects *queries*, not documents already loaded into memory (e.g. right after `.save()`).

## `src/models/Project.js` — arrays of references, enums

```javascript
const projectSchema = new mongoose.Schema(
  {
    name: { type: String, required: true, trim: true, maxlength: 150 },
    description: { type: String, trim: true },
    status: { type: String, enum: ['planning', 'active', 'on-hold', 'completed'], default: 'planning' },
    startDate: { type: Date },
    endDate: { type: Date },
    budget: { type: Number, min: 0, default: 0 },
    department: { type: mongoose.Schema.Types.ObjectId, ref: 'Department', required: true },
    // Array of employee references – demonstrates arrays in Mongoose
    assignedEmployees: [ { type: mongoose.Schema.Types.ObjectId, ref: 'Employee' } ],
    tags: [{ type: String, trim: true }],
  },
  { timestamps: true }
);

module.exports = mongoose.model('Project', projectSchema);
```

`status` reuses the `enum` + `default` pattern from `Employee.role`, but note this project's enum values (`'planning' | 'active' | 'on-hold' | 'completed'`, lowercase-hyphenated) are a **different vocabulary** than the EMS shell assignment's `projects.status` (`PLANNED | ACTIVE | ON_HOLD | COMPLETED | CANCELLED`, uppercase) — the same conceptual field, modeled independently in the two source materials. `department` is a **required** single reference — every project must belong to exactly one department (`required: true`), unlike `Employee.department` in the previous file which was optional.

`assignedEmployees: [ { type: ObjectId, ref: 'Employee' } ]` is an **array of references** — the many-to-many analogue of a SQL join table (`project_employees(project_id, employee_id)`), modeled as an embedded array of foreign keys directly on the `projects` document rather than a separate collection. This is a *different* relationship style again from the EMS shell assignment's `projects.team`, which embedded full employee **snapshots** (`{employeeId, name, projectRole, assignedDate}`) rather than just ID references — the trade-off being: references stay small and always reflect the current employee record (via `.populate()`), while snapshots avoid extra lookups but can go stale if the employee's real name changes. `tags: [{ type: String, trim: true }]` is the simplest array form — plain (non-document) values, each individually trimmed as added.

## `scripts/seed.js` — bulk insert pattern

```javascript
require('dotenv').config();
const MONGO_URI = process.env.MONGODB_URI || 'mongodb://localhost:27017/ems';

const departments = [
  { name: 'Engineering', code: 'ENG', location: 'Bengaluru', budget: 5000000 },
  // ... (3 more: HR, Finance, Marketing)
];

// Factory functions – need deptMap/empIds, which don't exist until earlier inserts assign ObjectIds
const getEmployees = (deptMap) => [
  { firstName: 'Aditya', lastName: 'Kulkarni', email: 'aditya.k@ems.local', password: 'pass1234',
    role: 'admin', designation: 'CTO', salary: 200000, department: deptMap['ENG'] },
  // ... (4 more employees)
];
const getProjects = (deptMap, empIds) => [
  { name: 'EMS Cloud Migration', status: 'active', budget: 2000000,
    department: deptMap['ENG'], assignedEmployees: empIds.slice(0, 3), tags: ['cloud', 'aws', 'migration'] },
  // ... (1 more project)
];

const seed = async () => {
  try {
    await mongoose.connect(MONGO_URI);
    await Promise.all([Department.deleteMany({}), Employee.deleteMany({}), Project.deleteMany({})]);

    const createdDepts = await Department.insertMany(departments);
    const deptMap = {};
    createdDepts.forEach((d) => (deptMap[d.code] = d._id));

    const createdEmployees = [];
    for (const data of getEmployees(deptMap)) {
      const emp = new Employee(data);
      await emp.save();
      createdEmployees.push(emp);
    }

    const empIds = createdEmployees.map((e) => e._id);
    await Project.insertMany(getProjects(deptMap, empIds));

    await mongoose.connection.close();
    process.exit(0);
  } catch (error) {
    console.error('❌ Seed failed:', error);
    process.exit(1);
  }
};
seed();
```

`getEmployees`/`getProjects` are **factory functions**, not static arrays, because they need `deptMap` (department codes → generated `_id`s) and, for projects, `empIds` — values that don't exist until *after* the earlier inserts run and MongoDB assigns ObjectIds. This is the standard "seed script" dependency-ordering problem: departments must be created before employees can reference them, and employees before projects can reference them.

`Promise.all([...deleteMany calls])` clears all three collections **concurrently** — safe since the calls don't depend on each other. `Department.insertMany(departments)` is a single bulk-insert; `insertMany` skips Mongoose's per-document `pre('save')` middleware by default (a lower-level bulk op), which is fine for `Department` (no such hook) but is exactly why employees are handled differently: they're inserted **one at a time** with `new Employee(data)` + `await emp.save()` inside a loop, explicitly **not** `insertMany`, because `.save()` is what triggers the password-hashing hook — `insertMany` bypassing it would store **plaintext passwords**. Whenever a schema has model-level hooks that must run, seed/import scripts must use `.save()` in a loop, not `insertMany`.
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
const parseQuery = (query = {}, allowedFilters = []) => {
  const page  = Math.max(parseInt(query.page)  || 1, 1);
  const limit = Math.min(parseInt(query.limit) || 10, 100);   // cap at 100
  const skip  = (page - 1) * limit;

  let sort = { createdAt: -1 };   // default: newest first
  if (query.sortBy) {
    const [field, order] = query.sortBy.split(':');
    sort = { [field]: order === 'desc' ? -1 : 1 };   // computed property key
  }

  const filter = {};
  allowedFilters.forEach((key) => {
    if (query[key] !== undefined) {
      if (query[key] === 'true') filter[key] = true;
      else if (query[key] === 'false') filter[key] = false;
      else filter[key] = query[key];
    }
  });

  if (query.search) {
    filter.$or = [
      { firstName: { $regex: query.search, $options: 'i' } },
      { lastName:  { $regex: query.search, $options: 'i' } },
      { email:     { $regex: query.search, $options: 'i' } },
    ];
  }

  let projection = null;
  if (query.fields) projection = query.fields.split(',').join(' ');

  return { filter, sort, skip, limit, page, projection };
};

const paginatedResponse = (data, total, page, limit) => ({
  data,
  pagination: { total, page, limit, totalPages: Math.ceil(total / limit), hasNextPage: page * limit < total, hasPrevPage: page > 1 },
});

module.exports = { parseQuery, paginatedResponse };
```

`page`/`limit` are parsed from Express's `req.query` (always strings), defaulted and **clamped** — `Math.max(..., 1)` prevents a non-positive/invalid page, `Math.min(..., 100)` caps `limit` to stop unbounded result sets; `skip` is derived arithmetically, mapping directly onto the `.skip(n).limit(n)` cursor methods from courseware §10. `sort` defaults to `{ createdAt: -1 }` (relying on each schema's `{ timestamps: true }`); an optional `sortBy=field:order` param is split and converted into the Mongo sort shape `{ [field]: 1|-1 }` via a computed property key.

`filter` is restricted to an explicit **allow-list** (`allowedFilters`, passed per-route) rather than trusting arbitrary query keys — a security-conscious pattern preventing clients from injecting filters on fields the route didn't intend to expose (e.g. `?password=...`). String `'true'`/`'false'` are explicitly coerced to real booleans, since query-string values always arrive as strings and would otherwise never match a Boolean field. An optional `search` param builds a **`$or` regex filter** across three fields with the `i` option — the same `$regex`/`$options` mechanics from courseware §11, generalized into a "search box" pattern, merged into the same `filter` object as the allow-listed exact filters.

`fields=firstName,email` (comma-separated) is transformed into `"firstName email"` (space-separated) — the string-projection syntax Mongoose's `.select()` expects, a shorthand alternative to the object-form `{ firstName: 1, email: 1 }` projection used in raw `find()` calls.

`paginatedResponse` wraps results in a standard envelope, computing `totalPages`, `hasNextPage` (whether `page * limit` has exceeded `total`), and `hasPrevPage` — a conventional REST pagination metadata shape built from the same `page`/`limit`/`total` values.

## `mongoose-demo.js` — standalone script (raw MongoDB driver, not Mongoose)

Despite the filename, this script does **not** use Mongoose — it uses the low-level official `mongodb` Node.js driver directly (`import { MongoClient } from 'mongodb'`), the same driver Mongoose itself is built on top of. It's a useful contrast case showing what you'd write *without* an ODM's schema/model layer — closer in spirit to using `pymongo` directly in Python rather than an ODM like `mongoengine`.

```javascript
import { MongoClient } from 'mongodb';   // official low-level driver, not mongoose

const uri = 'mongodb://localhost:27017';   // no database segment — chosen separately below

const MongoDbCon = async () => {
  const client = new MongoClient(uri);   // does NOT connect yet — a separate explicit step
  try {
    await client.connect();
    const db = client.db('acme-ems');                       // analogous to `use acme-ems` in mongosh
    const collectionList = await db.collections();
    collectionList.forEach(c => console.log(c.collectionName));
    const employees = db.collection('emps');                // no existence check — collections are lazily created
    const employeeList = await employees.find().toArray();  // cursor requires an explicit terminal call
    employeeList.forEach(e => console.log(e.name));
  } catch (error) {
    console.error(error.message);
  } finally {
    await client.close();   // always releases the connection, success or failure
  }
}

MongoDbCon();   // fire-and-forget top-level invocation; no await needed since it self-handles errors
```

There is no schema layer, no models, no validators here — everything is a plain JS object in, plain JS object out, unlike the Mongoose files above. `new MongoClient(uri)` constructs the client without connecting (unlike Mongoose's `mongoose.connect()`, which does both in one call); `client.db(name)` returns a `Db` handle; `db.collections()` returns full `Collection` handles (not just names). `db.collection('emps')` never fails even if the collection doesn't exist yet, since MongoDB creates collections lazily on first write. `employees.find().toArray()` explicitly materializes the lazy cursor into an array — necessary because, unlike Mongoose queries (thenable/awaitable directly), the raw driver's cursor needs an explicit terminal call (`.toArray()`, `.next()`, or `for await`). The `finally` block's `client.close()` is critical in a one-shot script, contrasted with a long-lived Express server that keeps the client open for the app's lifetime (as `db.js`'s `connectDB` does).

The file also contains a **commented-out alternative design** (lines 29–48 in the source) sketching a `connect()`/`getDb()` module pattern — a singleton-style connection holder (`let db;` at module scope, `getDb()` throws if `connect()` hasn't run yet) intended for use across multiple files in a larger app, contrasted with this file's single self-contained function. It's dead code (fully commented out) but shows the author considering the "connect once, reuse the handle" pattern that `db.js`'s `connectDB` implements properly via Mongoose's internal connection singleton.
