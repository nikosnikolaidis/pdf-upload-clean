# 3. PDF Upload Service: στάδιο 3 (τελικό, clean)

## Τι είναι
Η ίδια λειτουργικότητα με το project 1 (`POST /upload` με πολλά PDF σε multipart), αλλά γραμμένη με καλές πρακτικές. Σκοπός του είναι να είναι το project «όλα εντάξει», ώστε να φανεί αν η πλατφόρμα δίνει καθαρό αποτέλεσμα (λίγα ή καθόλου ευρήματα).

Δεν έχει εξωτερικές εξαρτήσεις στον κώδικα παραγωγής (μόνο JDK).

## Στοιχεία project
| | |
|---|---|
| Γλώσσα / Java | Java 11 (`maven.compiler.release`) |
| Build | Maven (`pom.xml`) |
| Αρχεία κώδικα | 9 main + 3 test (`.java`) |
| Μέγεθος | ~320 γραμμές κώδικα παραγωγής |
| Tests | 15 (JUnit 5.10.2, μόνη εξάρτηση, scope `test`) |

## Αρχεία
| Αρχείο | Ρόλος |
|---|---|
| `pom.xml` | Build σε Java 11, JUnit 5 μόνο για tests |
| `Main.java` | Σημείο εισόδου. Ρυθμίσεις από μεταβλητές περιβάλλοντος `PORT` και `UPLOAD_DIR` (όχι hardcoded) |
| `UploadHandler.java` | Λεπτός προσαρμογέας HTTP: ελέγχει μέθοδο και Content-Type, καλεί το service, γράφει την απάντηση |
| `MultipartParser.java` | Εξάγει το boundary και τα αρχεία από το multipart body |
| `UploadedPart.java` | Αμετάβλητο αντικείμενο (όνομα και περιεχόμενο αρχείου) |
| `PdfValidator.java` | Κανόνες: επέκταση `.pdf`, όχι κενό, μέγιστο 10 MB, υπογραφή `%PDF` |
| `UploadService.java` | Επεξεργάζεται το batch: ένα άκυρο αρχείο δεν μπλοκάρει τα υπόλοιπα |
| `FileStorage.java` | Διεπαφή αποθήκευσης (επιτρέπει αντικατάσταση και mocking) |
| `LocalFileStorage.java` | Αποθήκευση σε δίσκο με όνομα UUID που παράγει ο server |
| `UploadResult.java` | Αμετάβλητο αποτέλεσμα (stored/rejected) και μετατροπή σε JSON με escaping |
| `PdfValidatorTest.java` | 7 tests: έγκυρο, κεφαλαία, λάθος επέκταση, κενό, χωρίς signature, πολύ μικρό, πολύ μεγάλο |
| `MultipartParserTest.java` | 4 tests: πολλά αρχεία, πεδία χωρίς αρχείο, boundary, μη multipart |
| `UploadServiceTest.java` | 4 tests: μικτό batch, προστασία από path traversal, αποτυχία αποθήκευσης, JSON |

## Πώς τρέχει
```bash
mvn verify
PORT=8080 UPLOAD_DIR=uploads java -cp target/classes gr.demo.clean.upload.Main
curl -F files=@a.pdf -F files=@b.pdf localhost:8080/upload
```
Απάντηση: `{"stored":{"a.pdf":"<uuid>.pdf","b.pdf":"<uuid>.pdf"},"rejected":{}}`

## Καλές πρακτικές που εφαρμόζονται
- Μικρές κλάσεις με μία ευθύνη, και μέθοδοι κάτω από ~20 γραμμές. Early returns, χωρίς βαθιά φωλιασμένα `if`.
- Χωρίς hardcoded μυστικά ή διαδρομές: ρυθμίσεις από το περιβάλλον.
- Χωρίς path traversal: το όνομα στον δίσκο παράγεται από τον server (UUID), ποτέ από τον client.
- Αμετάβλητα αντικείμενα με defensive copies των byte arrays. Try-with-resources στα streams.
- Dependency injection με διεπαφή (`FileStorage`) που κάνει εύκολο το testing.
- Λίγα, χρήσιμα σχόλια (Javadoc που εξηγεί το «γιατί»). Ονόματα σταθερών αντί για magic numbers.
- Σύγχρονη LTS Java και καμία εξωτερική εξάρτηση παραγωγής, άρα καμία γνωστή ευπάθεια.
- Logger (`java.util.logging`) αντί για `System.out`.

## Γνωστοί περιορισμοί (δεν είναι ευρήματα που κρύβονται)
- Ο HTTP server είναι ο ενσωματωμένος του JDK, κατάλληλος για demo. Σε παραγωγή θα προτιμούσε κανείς framework.
- Δεν υπάρχει authentication, ούτε όριο στο συνολικό μέγεθος του request.
