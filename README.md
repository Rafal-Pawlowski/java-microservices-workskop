# Java Microservices Workshop

Warsztat przedstawia komunikację synchroniczną HTTP pomiędzy trzema mikroserwisami napisanymi w Java 21 i Spring Boot.

## Scenariusz warsztatu

Pełna instrukcja krok po kroku znajduje się w pliku:

➡️ **[SCENARIO.md](SCENARIO.md)**

Pracuj na branchu `student`. Kompletne rozwiązanie znajduje się na branchu `workshop-solution` — skorzystaj z niego dopiero po samodzielnym wykonaniu ćwiczeń.

## Architektura

| Serwis | Port | Odpowiedzialność |
|---|---:|---|
| `order-service` | 8080 | Przyjmuje zamówienie i koordynuje proces |
| `inventory-service` | 8081 | Sprawdza magazyn i rezerwuje produkt w H2 |
| `notification-service` | 8082 | Symuluje wysłanie powiadomienia |

```text
Klient
  -> order-service
      -> inventory-service -> H2
      <- ReservationResponse
      -> notification-service
      <- NotificationResponse
  <- OrderResponse
```

## Wymagania

- Java 21 lub nowsza,
- Git,
- IntelliJ IDEA Community lub Ultimate,
- połączenie z internetem podczas pierwszego pobierania zależności,
- cztery terminale albo cztery karty terminala.

Nie musisz instalować Mavena. Repozytorium zawiera Maven Wrapper.

## Pobranie projektu

```bash
git clone https://github.com/Rafal-Pawlowski/java-microservices-workskop.git
cd java-microservices-workskop
git switch student
```

Sprawdź branch:

```bash
git branch --show-current
```

Oczekiwany wynik:

```text
student
```

## Szybka weryfikacja

Windows PowerShell:

```powershell
.\mvnw.cmd clean verify
```

macOS/Linux:

```bash
chmod +x mvnw
./mvnw clean verify
```

Oczekiwany rezultat:

```text
BUILD SUCCESS
```

## Uruchamianie

Uruchom każdy serwis w osobnym terminalu.
> można to zrobić w UI Intelij naciskając strzałki "play"

### Windows PowerShell

```powershell
.\mvnw.cmd -pl inventory-service spring-boot:run
.\mvnw.cmd -pl notification-service spring-boot:run
.\mvnw.cmd -pl order-service spring-boot:run
```

### macOS/Linux

```bash
./mvnw -pl inventory-service spring-boot:run
./mvnw -pl notification-service spring-boot:run
./mvnw -pl order-service spring-boot:run
```

> Polecenia są pokazane razem dla wygody, ale każde należy uruchomić w oddzielnym terminalu.

## Health check

```text
http://localhost:8080/actuator/health
http://localhost:8081/actuator/health
http://localhost:8082/actuator/health
```

Każdy serwis powinien zwrócić status `UP`.

## Struktura repozytorium

To monorepozytorium Maven. Wszystkie mikroserwisy należą do jednego repozytorium Git i są modułami głównego projektu.

```text
java-microservices-workskop/
├── pom.xml
├── order-service/
├── inventory-service/
├── notification-service/
├── README.md
└── SCENARIO.md
```

Nie wykonuj `git init` wewnątrz modułów.

## Najczęstsze problemy

### Nieprawidłowa wersja Javy

```bash
java -version
```

Projekt wymaga Java 21 lub nowszej.

### Port jest zajęty

Jeśli widzisz `Port 8080 was already in use`, sprawdź, czy serwis nie został uruchomiony wcześniej.

### `Connection refused`

Jeden z wymaganych mikroserwisów prawdopodobnie nie działa. Sprawdź jego terminal i endpoint Actuatora.

### `Permission denied` na macOS/Linux

```bash
chmod +x mvnw
```

## Branche

- `student` — kod startowy z TODO,
- `workshop-solution` — kompletne rozwiązanie ćwiczeń.
