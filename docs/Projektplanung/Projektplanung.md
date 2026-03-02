# Projektplan: ParVer (Parkplatz-Verwaltungssystem)
**Zeitraum:** 26.02.2026 – 06.03.2026 (7 Arbeitstage)
**Deadline:** Freitag, 06.03.2026 (Präsentation)

---

## 1. Strategische Zeitplanung (Gantt-Modell)

Das Projekt folgt einem parallelen Entwicklungsansatz (Lanes), um die knappe Zeit bis zum **Stop-Ship** am Donnerstagabend optimal zu nutzen.

```mermaid
gantt
    title LF12-Projekt: ParVer Parkplatz-Verwaltung
    dateFormat  YYYY-MM-DD
    axisFormat  %a, %d.%m.
    
    section Planung
    Rahmenbedingungen & Analyse    :done, p1, 2026-02-26, 1d
    Pflichtenheft & Zeitplanung    :done, p2, 2026-02-27, 1d
    
    section Entwicklung
    Projektstruktur & DB-Design    :active, e1, 2026-03-02, 1d
    PWA-Setup & UI-Grundgerüst     :active, e2, 2026-03-02, 1d
    API-Anbindung & Logik          :e3, 2026-03-03, 1d
    Raster-Visualisierung          :e4, 2026-03-03, 1d
    Echtzeit-Sync & Push-Dienst    :e5, 2026-03-04, 1d
    
    section Deployment & QS
    Docker & Build-Optimierung     :d1, 2026-03-05, 0.5d
    Qualitätssicherung (Testing)   :d2, 2026-03-05, 0.5d
    STOP-SHIP (Donnerstag Abend)   :milestone, m1, 2026-03-05, 0d
    
    section Abschluss
    Dokumentation & Präsentation   :a1, 2026-03-06, 1d