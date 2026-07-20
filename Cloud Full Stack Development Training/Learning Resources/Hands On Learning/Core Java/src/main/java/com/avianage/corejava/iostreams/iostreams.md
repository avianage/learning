# Module 25 — IO Streams (Condensed)

> Part G: I/O and Collections · Prerequisites: Module 08–13, 17

## Overview
```
java.io       — classic stream-based I/O (Java 1.0+)
java.nio      — buffer-based, non-blocking (Java 1.4+)
java.nio.file — modern file API: Path, Files, Paths (Java 7+, PREFERRED)
```

## Stream Categories
```
Byte Streams      — raw binary (images, PDFs, serialized objects) → InputStream / OutputStream
Character Streams — text, handles encoding → Reader / Writer
```

---

## 1. Byte Streams — Binary Data
```java
try (FileOutputStream fos = new FileOutputStream("data.bin")) {
    fos.write(new byte[]{72, 101, 108, 108, 111});
}
try (FileInputStream fis = new FileInputStream("data.bin")) {
    int b;
    while ((b = fis.read()) != -1) System.out.print((char) b);   // -1 = EOF
}
```
Always `try-with-resources` — streams hold OS file handles.

## 2. Character Streams — Text
```java
try (FileWriter fw = new FileWriter("employees.txt")) { fw.write("101,Sonu,75000\n"); }
try (FileReader fr = new FileReader("employees.txt")) { /* char-by-char, inefficient */ }
```
Reading char-by-char is slow — always wrap with a buffered reader.

---

## 3. Buffered Streams — The Right Way for Text
```java
try (BufferedWriter bw = new BufferedWriter(new FileWriter("employees.txt"))) {
    bw.write("ID,Name,Salary,Department"); bw.newLine();   // platform-independent line sep
    bw.write("101,Sonu,75000,Engineering"); bw.newLine();
}

try (BufferedReader br = new BufferedReader(new FileReader("employees.txt"))) {
    String line;
    while ((line = br.readLine()) != null) {   // null = EOF
        String[] parts = line.split(",");
    }
}
```

## 4. `PrintWriter` — Formatted Output
```java
try (PrintWriter pw = new PrintWriter(new BufferedWriter(new FileWriter("report.txt")))) {
    pw.printf("%-5s %-12s %10s%n", "ID", "Name", "Salary");
    pw.println("-".repeat(30));
}
```
**Append mode:** `new FileWriter("employees.txt", true)` — second arg `true` appends instead of overwriting.

## 5. `DataInputStream` / `DataOutputStream` — Typed Binary
```java
try (DataOutputStream dos = new DataOutputStream(new BufferedOutputStream(new FileOutputStream("salary.dat")))) {
    dos.writeInt(101); dos.writeUTF("Sonu"); dos.writeDouble(75000.0); dos.writeBoolean(true);
}
try (DataInputStream dis = new DataInputStream(new BufferedInputStream(new FileInputStream("salary.dat")))) {
    int id = dis.readInt(); String name = dis.readUTF();   // MUST read in same order written
}
```

---

## 6. Serialization — Object ↔ File
Requires implementing `Serializable` (marker interface) + a `serialVersionUID`.

```java
public class Employee implements Serializable {
    private static final long serialVersionUID = 1L;
    private int id; private String name; private double salary;
    private transient String sessionToken;   // transient — SKIPPED during serialization
}
```
**Write:**
```java
try (ObjectOutputStream oos = new ObjectOutputStream(new BufferedOutputStream(new FileOutputStream("employees.ser")))) {
    oos.writeObject(e1); oos.writeObject(e2);
}
```
**Read:**
```java
try (ObjectInputStream ois = new ObjectInputStream(new BufferedInputStream(new FileInputStream("employees.ser")))) {
    Employee emp1 = (Employee) ois.readObject();   // sessionToken will be null — was transient
}
```
**`transient`:** skip sensitive data, recomputable fields, or non-serializable objects (DB connections, threads).
**`serialVersionUID`:** verifies serialized class matches current definition; mismatch → `InvalidClassException`. Keep same UID for backward-compatible changes; change it to force a break.

---

## 7. Modern File API — `java.nio.file` (Preferred)

**`Path` / `Paths`:**
```java
Path p1 = Paths.get("employees.txt");
Path p2 = Paths.get("/home/user", "data", "employees.txt");
Path p3 = Path.of("employees.txt");          // Java 11+
p2.getFileName(); p2.getParent(); p2.toAbsolutePath();
```

**`Files` — read/write:**
```java
Files.write(path, lines, StandardCharsets.UTF_8);           // write all lines
List<String> lines = Files.readAllLines(path);               // read all — small files
String content = Files.readString(path);                      // Java 11+
Files.writeString(path, content, StandardOpenOption.TRUNCATE_EXISTING);
Files.writeString(path, "\nmore", StandardOpenOption.APPEND);
```

**`Files.lines()` — lazy stream, good for large files:**
```java
try (Stream<String> lines = Files.lines(path, StandardCharsets.UTF_8)) {
    lines.skip(1)
         .map(line -> line.split(","))
         .filter(p -> Double.parseDouble(p[2]) > 70000)
         .forEach(p -> System.out.println(p[1]));
}
```

**File/directory ops:**
```java
Files.exists(file); Files.notExists(file); Files.isDirectory(dir); Files.isRegularFile(file);
Files.createFile(file); Files.createDirectory(dir); Files.createDirectories(Paths.get("a/b/c"));
Files.copy(file, dest, StandardCopyOption.REPLACE_EXISTING);
Files.move(file, dest, StandardCopyOption.REPLACE_EXISTING);
Files.delete(file);              // throws if missing
Files.deleteIfExists(file);      // safe version
long size = Files.size(file);
```

**Directory traversal:**
```java
Files.list(Paths.get(".")).filter(Files::isRegularFile).forEach(System.out::println);   // one level
Files.walk(Paths.get("src")).filter(p -> p.toString().endsWith(".java")).forEach(...);   // recursive
Files.find(Paths.get("."), 3, (p, attr) -> p.toString().endsWith(".txt") && attr.isRegularFile());
```

---

## 8. Legacy `File` Class (seen in older code)
```java
File f = new File("employees.txt");
f.exists(); f.isFile(); f.length(); f.getName(); f.getAbsolutePath();
f.mkdir(); f.mkdirs(); f.listFiles(); f.createNewFile(); f.renameTo(new File("new.txt"));
```
Prefer `java.nio.file` for new code — better errors, atomic ops, richer API.

---

## 9. Stream Decorator Pattern
Java I/O wraps streams to layer functionality — read inside-out:
```java
new PrintWriter(                        // adds print/println/printf
    new BufferedWriter(                 // adds buffering
        new FileWriter("output.txt")))  // actual file target
```
```java
new BufferedReader(new FileReader("file.txt"));                                    // efficient text read
new DataInputStream(new BufferedInputStream(new FileInputStream("file.dat")));      // efficient typed binary
new ObjectOutputStream(new BufferedOutputStream(new FileOutputStream("data.ser"))); // efficient serialization
```

---

## 10. Practical Pattern — CSV Read/Write
```java
public static void writeEmployees(List<Employee> emps) throws IOException {
    List<String> lines = new ArrayList<>();
    lines.add("id,name,salary,department");
    for (Employee e : emps) lines.add(String.format("%d,%s,%.2f,%s", e.getId(), e.getName(), e.getSalary(), e.getDepartment()));
    Files.write(FILE, lines, StandardCharsets.UTF_8);
}

public static List<Employee> readEmployees() throws IOException {
    return Files.lines(FILE, StandardCharsets.UTF_8)
                .skip(1)
                .map(line -> { String[] p = line.split(","); return new Employee(Integer.parseInt(p[0]), p[1], Double.parseDouble(p[2]), p[3]); })
                .collect(Collectors.toList());
}
```

---

## Quick Reference

| Class | Use For |
|---|---|
| `FileInputStream`/`FileOutputStream` | Raw binary file read/write |
| `FileReader`/`FileWriter` | Text file read/write |
| `BufferedReader`/`BufferedWriter` | Efficient text I/O — `readLine()` |
| `PrintWriter` | Formatted text output |
| `DataInputStream`/`DataOutputStream` | Typed primitive binary I/O |
| `ObjectInputStream`/`ObjectOutputStream` | Object serialization |
| `Path`, `Paths`, `Files` | Modern file API — prefer this |
| `Files.readAllLines()` | All lines into a List — small files |
| `Files.lines()` | Lazy line stream — large files |
| `Files.readString()`/`writeString()` | Whole file as String (Java 11+) |
| `Files.walk()` | Recursive directory traversal |
| `transient` | Skip field during serialization |
| `serialVersionUID` | Version control for serialized classes |

## Next: Module 26 — Collections and Generics (List, Set, Queue, Map)