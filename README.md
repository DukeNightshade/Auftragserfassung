# Arbeitszeit- & Aufgabenerfassung

> **Eine Android-App zur einfachen Erfassung von Arbeitszeiten und Tätigkeiten auf Baustellen.**  
> Fokus auf klare Bedienbarkeit für 40–60-Jährige und Design im Stil der *Umbrella Corporation* (Resident Evil).

---
## Tech Stack

![Java](https://img.shields.io/badge/Java-21-orange)
![Android Studio](https://img.shields.io/badge/IDE-Android%20Studio-blue)
![Room](https://img.shields.io/badge/Database-Room-green)
![Status](https://img.shields.io/badge/Status-Active-brightgreen)
![License](https://img.shields.io/badge/License-MIT-lightgrey)

---

## Ziel des Projekts
Das Ziel dieser App ist es, Arbeitszeiten schnell und zuverlässig zu erfassen – inklusive automatischer Pausenberechnung und Baustellenverwaltung.  
Die Anwendung ermöglicht außerdem eine Volltextsuche über die Tätigkeitsbeschreibungen.

---

## Hauptfunktionen

### 🔹 1. Einträge (Arbeitszeiten)
- **Datum**: Standard = aktueller Tag  
- **Arbeitszeit von–bis**  
  - Automatische Berechnung der Pause:  
    - 6–9 h → 30 min  
    - 9–10 h → 45 min  
    - >10 h → 60 min  
- **Baustelle auswählen**  
  - Standard-Baustelle wird vorausgewählt  
- **Beschreibung (Tätigkeit)**  
  - Dynamisches Textfeld (mehrzeilig, wächst automatisch)

---

### 🔹 2. Baustellenverwaltung
- Eigener Screen „Baustellen“  
- Neue Baustellen anlegen:  
  - Pflichtfeld: **Name**  
  - Optional: **Adresse**  
- Standard-/Favoriten-Baustelle mit ⭐ markieren  
  - Wird bei neuen Einträgen automatisch vorausgewählt  

---

### 🔹 3. Suche
- Volltextsuche über das **Beschreibungsfeld** der Einträge  
- Ergebnisse mit **Datum, Baustelle und Vorschautext**

---

## 🎨 Design & Usability
- **Zielgruppe:** 40–60 Jahre  
- **Design:** Modern, kontrastreich – inspiriert von der *Umbrella Corporation*  
  - Farbpalette: Weiß ⚪, Schwarz ⚫, Rot 🔴  
- Große Buttons, klare Navigation, gut lesbare Schriftgrößen  

---

## ⚙️ Technologische Anforderungen
| Komponente | Technologie |
|-------------|-------------|
| **Backend** | Java |
| **IDE** | Android Studio |
| **Datenbank** | Lokale DB über Room Library |

---

## Future Ideas / Erweiterungen
- Export der Einträge (CSV / PDF)  
- Cloud-Synchronisierung oder Online-Backup  
- Statistiken (z. B. gearbeitete Stunden pro Baustelle)  
- Dark Mode  

---

## Projektstatus
**In Entwicklung** – grundlegende Screens und Logik werden umgesetzt.  
Ziel: stabile MVP-Version mit lokaler Datenspeicherung und UI-Flow.

---

## Screens (geplant)
- **Start / Tagesübersicht** – Liste der Einträge  
- **Neuer Eintrag** – Arbeitszeit, Baustelle, Beschreibung  
- **Baustellen** – Übersicht + Hinzufügen  
- **Suche** – Filterung über Beschreibung  

---

## Inspiration
- Resident Evil „Umbrella Corporation“:  
  - Corporate/Stylish UI mit funktionalem Minimalismus.  
- Fokus auf Alltagstauglichkeit und intuitive Nutzung  

---

## Systemanforderungen
- Android 8.0 (Oreo) oder höher  
- Mindestens 2 GB RAM empfohlen  

---

© 2026 Nico Hoffmann
