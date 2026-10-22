# Java Microservices Workshop

Warsztat przedstawia komunikację synchroniczną HTTP pomiędzy trzema mikroserwisami napisanymi w Java 21 i Spring Boot.

## Architektura

| Serwis | Port | Odpowiedzialność |
|---|---:|---|
| `order-service` | 8080 | Przyjmuje zamówienie i koordynuje proces |
| `inventory-service` | 8081 | Sprawdza magazyn i rezerwuje produkt |
| `notification-service` | 8082 | Symuluje wysłanie powiadomienia |

Przepływ zamówienia:

1. Klient wysyła zamówienie do `order-service`.
2. `order-service` wywołuje `inventory-service`.
3. `inventory-service` rezerwuje produkt.
4. `inventory-service` zwraca identyfikator rezerwacji.
5. `order-service` wywołuje `notification-service`.
6. `notification-service` symuluje wysłanie wiadomości.
7. `order-service` zwraca wynik całego procesu.

## Wymagania

- Java 21 lub nowsza
- Git
- IntelliJ IDEA Community lub Ultimate
- trzy osobne terminale

Nie musisz instalować Mavena. Repozytorium zawiera Maven Wrapper.

## Pobranie projektu

```bash
git clone https://github.com/Rafal-Pawlowski/java-microservices-workskop.git
cd java-microservices-workshop
git checkout student
```


# Windows

Polecenia wykonuj w PowerShellu lub terminalu IntelliJ.

## Budowanie projektu

```powershell
.\mvnw.cmd clean verify
```

Oczekiwany wynik:

```text
BUILD SUCCESS
```

## Uruchamianie serwisów

Każdy mikroserwis uruchom w osobnym terminalu.

### Terminal 1 — inventory-service

```powershell
.\mvnw.cmd -pl inventory-service spring-boot:run
```

### Terminal 2 — notification-service

```powershell
.\mvnw.cmd -pl notification-service spring-boot:run
```

### Terminal 3 — order-service

```powershell
.\mvnw.cmd -pl order-service spring-boot:run
```

## Sprawdzenie stanu aplikacji

W czwartym terminalu wykonaj:

```powershell
Invoke-RestMethod "http://localhost:8080/actuator/health"
Invoke-RestMethod "http://localhost:8081/actuator/health"
Invoke-RestMethod "http://localhost:8082/actuator/health"
```

Każda aplikacja powinna zwrócić status:

```text
UP
```

## Wysłanie zamówienia

```powershell
$body = @{
    productId = "LAPTOP-001"
    quantity = 1
    customerEmail = "student@agh.edu.pl"
} | ConvertTo-Json

Invoke-RestMethod `
    -Method Post `
    -Uri "http://localhost:8080/api/orders" `
    -ContentType "application/json" `
    -Body $body
```

> W PowerShellu znak `` ` `` oznacza kontynuację polecenia w następnej linii.

# macOS i Linux

Polecenia wykonuj w aplikacji Terminal lub terminalu IntelliJ.

## Uprawnienia Maven Wrappera

Po pobraniu projektu może być konieczne nadanie uprawnień do uruchomienia pliku:

```bash
chmod +x mvnw
```

## Budowanie projektu

```bash
./mvnw clean verify
```

Oczekiwany wynik:

```text
BUILD SUCCESS
```

## Uruchamianie serwisów

Każdy mikroserwis uruchom w osobnej karcie terminala.

### Terminal 1 — inventory-service

```bash
./mvnw -pl inventory-service spring-boot:run
```

### Terminal 2 — notification-service

```bash
./mvnw -pl notification-service spring-boot:run
```

### Terminal 3 — order-service

```bash
./mvnw -pl order-service spring-boot:run
```

## Sprawdzenie stanu aplikacji

W czwartej karcie terminala wykonaj:

```bash
curl http://localhost:8080/actuator/health
curl http://localhost:8081/actuator/health
curl http://localhost:8082/actuator/health
```

Każda aplikacja powinna zwrócić:

```json
{"status":"UP"}
```

## Wysłanie zamówienia

```bash
curl -X POST http://localhost:8080/api/orders \
  -H "Content-Type: application/json" \
  -d '{
    "productId": "LAPTOP-001",
    "quantity": 1,
    "customerEmail": "student@agh.edu.pl"
  }'
```

> Na macOS i Linux znak `\` oznacza kontynuację polecenia w następnej linii.

# Zadania warsztatowe

Na branchu `student` trzy metody nie mają jeszcze implementacji.

## Zadanie 1 — InventoryClient

Zaimplementuj metodę:

```java
InventoryClient.reserve(String productId, int quantity)
```

Metoda powinna:

1. utworzyć obiekt żądania rezerwacji,
2. wykonać żądanie HTTP `POST`,
3. przesłać żądanie jako JSON,
4. odczytać odpowiedź JSON,
5. zwrócić `InventoryReservationResponse`.

Wywoływany endpoint:

```text
POST http://localhost:8081/api/inventory/reservations
```

## Zadanie 2 — NotificationClient

Zaimplementuj metodę:

```java
NotificationClient.send(
    UUID orderId,
    String customerEmail,
    String message
)
```

Metoda powinna:

1. utworzyć obiekt żądania powiadomienia,
2. wykonać żądanie HTTP `POST`,
3. przesłać żądanie jako JSON,
4. odczytać odpowiedź,
5. zwrócić `NotificationClientResponse`.

Wywoływany endpoint:

```text
POST http://localhost:8082/api/notifications
```

## Zadanie 3 — OrderService

Zaimplementuj metodę:

```java
OrderService.createOrder(
    String productId,
    int quantity,
    String customerEmail
)
```

Metoda powinna:

1. wygenerować `orderId`,
2. wywołać `InventoryClient`,
3. odebrać identyfikator rezerwacji,
4. wywołać `NotificationClient`,
5. utworzyć i zwrócić `OrderResult`.

# Oczekiwany rezultat

Po poprawnym wykonaniu wszystkich zadań żądanie do `order-service` powinno zwrócić wynik podobny do:

```text
orderId            : c307b410-c989-4efd-a48f-0610ff6ad7c7
reservationId      : 9271f516-abfe-4349-8a49-8fbe4a5b49a5
productId          : LAPTOP-001
quantity           : 1
notificationStatus : SENT
```

Wartości `orderId` i `reservationId` będą inne przy każdym żądaniu.

# Najważniejsze pojęcia

## Mikroserwis

Niezależna aplikacja odpowiedzialna za konkretny obszar biznesowy.

## Endpoint

Adres HTTP udostępniający określoną operację aplikacji.

## DTO

Obiekt służący do przesyłania danych pomiędzy warstwami lub aplikacjami.

## RestClient

Klient HTTP używany przez `order-service` do komunikacji z pozostałymi serwisami.

## Komunikacja synchroniczna

`order-service` wysyła żądanie i czeka na odpowiedź drugiego mikroserwisu.

Jeżeli drugi mikroserwis nie działa lub odpowiada zbyt długo, realizacja zamówienia może zakończyć się błędem.

# Najczęstsze problemy

## Port jest zajęty

Przykładowy komunikat:

```text
Port 8080 was already in use
```

Sprawdź, czy ten sam mikroserwis nie został uruchomiony wcześniej.

## Connection refused

Jeden z wymaganych mikroserwisów prawdopodobnie nie działa.

Przed wykonaniem testu końcowego muszą działać wszystkie trzy aplikacje.

## Permission denied na macOS lub Linux

Wykonaj:

```bash
chmod +x mvnw
```

## Java ma nieprawidłową wersję

Windows:

```powershell
java -version
```

macOS/Linux:

```bash
java -version
```

Projekt wymaga Java 21 lub nowszej.

# Rozwiązanie

Kompletna implementacja znajduje się na branchu `workshop-solution`.

Najpierw spróbuj wykonać zadania samodzielnie.