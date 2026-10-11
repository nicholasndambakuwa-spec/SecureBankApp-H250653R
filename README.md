# Secure Banking Application

## Description
A console-based banking application written in Java that demonstrates Object-Oriented Programming and secure coding practices.
Users can register, log in, open accounts, deposit and withdraw money; everything is saved in text files.

## Student Details
- **Name:** NICHOLAS NDAMBAKUWA
- **Registration Number:**H250653R

## Features
- User registration and login (passwords stored as salted PBKDF2 hashes, never in plain text)
- Account lockout after 3 failed logins, and session timeout after 5 idle minutes
- Create bank accounts and view balances
- Deposit and withdraw funds with full validation
- Transaction history per account
- Data persistence in text files (`users.txt`, `accounts.txt`, `transactions.txt`), loaded on start-up
- Users can only access their own accounts

## Security Measures
| Practice | Where |
|---|---|
| Salted + slow password hashing (PBKDF2-HMAC-SHA256, 210,000 iterations) | `PasswordUtil` |
| Constant-time hash comparison | `PasswordUtil.matches` |
| Allow-list input validation (username, password strength, amounts) | `Validator`, `Money` |
| Money stored as whole cents (`long`), overflow-checked | `Money`, `Account` |
| Brute-force protection (lockout) and generic login errors | `Bank.login` |
| Authorization: ownership check on every account action | `Bank.getOwned` |
| Encapsulation: private fields, read-only history view, immutable `User`/`Transaction` | all model classes |
| Atomic file writes, owner-only permissions, corrupt-line skipping | `DataStore` |
| No stack traces or internals shown to users | `Main`, `BankException` |
| Passwords handled as `char[]` and wiped after use; password input is visible in the console | `Main` |

## Project Structure
```
src/   Main, Bank, Account, User, Transaction, DataStore, PasswordUtil, Validator, Money, BankException
data/  runtime storage (created automatically; local data files are not uploaded)
```

## How to Run (Java 17 or newer)
```bash
javac -d out src/*.java
java -cp out Main
```
Run from the project root folder so the `data/` folder is found.
Demo login: `demo_user` / `Demo@1234` (demo data only).

## Known Limitations
Text files are not encrypted, and there is no multi-user locking; a real bank would use a database and an HSM.
