# Projektplan: ParVer (Parkplatz-Verwaltungssystem)
**Zeitraum:** 26.02.2026 – 06.03.2026 (7 Arbeitstage)
**Deadline:** Freitag, 06.03.2026 (Präsentation)

---

## 1. Strategische Zeitplanung (Gantt-Modell)

Das Projekt folgt einem parallelen Entwicklungsansatz (Lanes), um die knappe Zeit bis zum **Stop-Ship** am Donnerstagabend optimal zu nutzen.

```mermaid
gantt
    title ParVer Projekt-Roadmap
    dateFormat  YYYY-MM-DD
    axisFormat  %a, %d.%m.

    section Konzeption (Done)
    Analyse & Anforderungskatalog      :done, c1, 2026-02-26, 1d
    Pflichtenheft & Prozessdesign      :done, c2, 2026-02-27, 1d

    section Backend & API (Lane 1)
    DB-Struktur & User-Rollen          :active, b1, 2026-03-02, 1d
    Logik & Schnittstellen             :b2, 2026-03-03, 1d
    Echtzeit-Synchronisation (WS)      :b3, 2026-03-04, 1d

    section Frontend & PWA (Lane 2)
    App-Grundgerüst (Setup)            :f1, 2026-03-02, 0.5d
    Visuelle Parkplatz-Raster          :f2, after f1, 1d
    Interaktive Funktionen (Buchung)   :f3, after f2, 1.5d

    section QS & Release (Lane 3)
    Funktionstests & Bugfixing         :q1, 2026-03-05, 1d
    STOP-SHIP (Donnerstag 18:00)       :milestone, m1, 2026-03-05, 0d
    Präsentationserstellung & Demo     :p1, 2026-03-06, 0.5d
    Live-Gang (AFBB Präsentation)      :p2, 2026-03-06, 0.5d