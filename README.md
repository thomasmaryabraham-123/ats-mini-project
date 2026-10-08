# Applicant Tracking System (ATS) - College Mini Project

A complete mini project demonstrating a **Java Swing Desktop App** and a **Django Web Page** sharing the exact same **SQLite database file (`ats.db`)**.

## 1. Project Directory Structure
```text
ats_project/
├── ats.db                 <-- Created automatically by Main.java
├── sqlite-jdbc.jar        <-- Downloaded SQLite JDBC driver
├── Main.java              <-- Java Swing Desktop Application
└── ats_web/               <-- Django Web Application
    ├── manage.py
    ├── ats_web/
    │   ├── __init__.py
    │   ├── settings.py
    │   ├── urls.py
    │   └── wsgi.py
    └── dashboard/
        ├── __init__.py
        ├── models.py
        ├── views.py
        └── templates/
            └── dashboard.html
```

## 2. Match Score Formula
- **80% Skill Match**: (Number of candidate skills matching required job skills / Total required skills) * 100
- **20% Experience Match**: Min(100, (Candidate experience / Job min experience) * 100). If min experience is 0, score is 100%.
- **Final Score**: `0.80 * SkillMatch + 0.20 * ExpMatch`

## 3. How to Run

### Step 1: Download sqlite-jdbc jar
Download the jar file and place it in the `ats_project/` folder:
- Link: https://github.com/xerial/sqlite-jdbc/releases/download/3.45.2.0/sqlite-jdbc-3.45.2.0.jar
- Rename it to `sqlite-jdbc.jar` (or keep the version name and match it in the command).

### Step 2: Compile and Run Java Swing App
Open a terminal in `ats_project/`:
```bash
# On Windows:
javac -cp ".;sqlite-jdbc.jar" Main.java
java -cp ".;sqlite-jdbc.jar" Main

# On Mac / Linux:
javac -cp ".:sqlite-jdbc.jar" Main.java
java -cp ".:sqlite-jdbc.jar" Main
```
*On launch, `Main.java` creates `ats.db` with `jobs` and `candidates` tables automatically.*
Use the app to add jobs and candidates, and update statuses!

### Step 3: Run the Django Web Server
Open a second terminal in the `ats_project/ats_web/` folder:
```bash
pip install django
python manage.py runserver
```
Visit **http://127.0.0.1:8000/** in your browser, or click the **"Open Web Dashboard"** button in Java Swing!

## 4. File Path Note
In `ats_web/settings.py`:
```python
DATABASES = {
    'default': {
        'ENGINE': 'django.db.backends.sqlite3',
        'NAME': BASE_DIR.parent / 'ats.db',
    }
}
```
- `BASE_DIR` is `ats_project/ats_web`.
- `BASE_DIR.parent / 'ats.db'` resolves to `ats_project/ats.db`, pointing directly to the exact file created by Java!
