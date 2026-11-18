# Botiga Online – Java + JDBC + MySQL

Aplicació de consola que gestiona una botiga online: CRUD de productes, clients i descomptes,
creació de comandes amb transaccions i aplicació de descomptes amb *savepoints*.

## Estructura

```
botiga-online/
├── sql/schema.sql          → Creació de la BD i dades de prova
├── lib/                    → Aquí va el driver mysql-connector-j-*.jar
├── src/
│   ├── util/Connexio.java  → Connexió amb DriverManager
│   ├── model/              → POJOs (Client, Producte, Comanda, LiniaComanda, Descompte)
│   ├── dao/                → ClientDAO, ProducteDAO, DescompteDAO, ComandaDAO, ConsultesDAO
│   └── App.java            → Menú principal
└── README.md
```

## 1. Crear la base de dades

```bash
mysql -u root -p < sql/schema.sql
```
(o obre `schema.sql` a MySQL Workbench i executa'l).

## 2. Configurar l'usuari

Edita `src/util/Connexio.java` i posa el teu usuari i contrasenya de MySQL:

```java
private static final String USER = "root";
private static final String PASSWORD = "root";
```

## 3. Driver JDBC

Descarrega **MySQL Connector/J** (https://dev.mysql.com/downloads/connector/j/ → *Platform Independent*)
i copia el `.jar` a la carpeta `lib/`.

## 4. Compilar i executar

**Windows (PowerShell / CMD):**
```bash
javac -d out -cp "lib/*" src/util/*.java src/model/*.java src/dao/*.java src/App.java
java -cp "out;lib/*" App
```

**Linux / macOS:**
```bash
javac -d out -cp "lib/*" src/util/*.java src/model/*.java src/dao/*.java src/App.java
java -cp "out:lib/*" App
```

Amb IntelliJ / Eclipse / NetBeans: marca `src` com a carpeta de codi i afegeix el `.jar` de `lib/` com a llibreria.

## Proves recomanades

| Prova | Com fer-la | Resultat esperat |
|---|---|---|
| Comanda correcta amb descompte | Client 1, 2 Ratolins + 1 Pantalla | Subtotal 159.98 €, descompte 24.00 €, total 135.98 € |
| Rollback per estoc | Client 1, 50 Teclats | Error d'estoc i **no** es crea cap comanda ni baixa l'estoc |
| Rollback per client inexistent | Client 99 | Error de clau forana, estoc intacte |
| Rollback al savepoint | Menú Descomptes → afegir al producte 2 un descompte de 150 % → comprar Teclats | Avís de descompte invàlid, la comanda es guarda **sense descompte** |
| Consultes JOIN | Opcions 5 i 6 del menú | Comandes amb línies i resum subtotal/descompte/total |

## Regles dels descomptes

- `%` → percentatge sobre l'import de la línia (ha d'estar entre 0 i 100).
- `€` → import fix per **unitat** (no pot superar el preu unitari).
- Si un descompte no és vàlid, es fa `rollback(savepoint)` i el tiquet es guarda amb el total sense descompte.
