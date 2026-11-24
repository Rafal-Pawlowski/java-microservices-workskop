# Warsztat: komunikacja mikroserwisów w praktyce

Ten scenariusz prowadzi krok po kroku przez komunikację synchroniczną HTTP pomiędzy trzema aplikacjami Spring Boot.

Pracujemy na branchu `student`. Nie musisz znać wcześniej `RestClient`. Każdy etap zaczyna się od małego eksperymentu, a dopiero później przechodzimy do kodu.

Kompletne implementacje TODO znajdują się na branchu `workshop-solution`. Nie przełączaj się na niego podczas wykonywania ćwiczeń — służy do późniejszego porównania rozwiązania.

## Co zbudujemy

```text
Klient
  |
  | POST /api/orders
  v
order-service :8080
  |
  | POST /api/inventory/reservations
  v
inventory-service :8081 ---> H2
  |
  | ReservationResponse
  v
order-service
  |
  | POST /api/notifications
  v
notification-service :8082
  |
  | NotificationResponse
  v
order-service ---> OrderResponse ---> Klient
```

`order-service` pełni rolę orkiestratora: decyduje, w jakiej kolejności wywołać pozostałe serwisy.

## Cele warsztatu

Po warsztacie potrafisz:

1. wskazać odpowiedzialność każdego mikroserwisu,
2. prześledzić request od kontrolera do domeny i z powrotem,
3. odróżnić DTO od modelu domenowego,
4. wykonać request HTTP i odczytać status oraz body,
5. zaimplementować klienta HTTP przy użyciu `RestClient`,
6. znaleźć przyczynę błędu w logach właściwego serwisu,
7. wyjaśnić, dlaczego awaria jednego mikroserwisu wpływa na cały synchroniczny flow,
8. rozpoznać problem częściowo wykonanej operacji.

## Plan podstawowy — 45 minut

| Czas | Etap | Rezultat |
|---:|---|---|
| 0–4 min | Branch i build | Projekt kompiluje się |
| 4–8 min | Architektura i ścieżka klas | Wiesz, gdzie zaczyna się request |
| 8–13 min | Uruchomienie i health check | Trzy serwisy mają status `UP` |
| 13–17 min | Test serwisów osobno | Inventory i notification działają niezależnie |
| 17–20 min | Pierwsze zamówienie | Kontrolowany błąd w `OrderService` |
| 20–26 min | Implementacja orkiestracji | Flow dochodzi do `InventoryClient` |
| 26–32 min | Implementacja `InventoryClient` | Pierwsze wywołanie między serwisami działa |
| 32–37 min | Obserwacja częściowego sukcesu | Rezerwacja działa, notification jeszcze nie |
| 37–42 min | Implementacja `NotificationClient` | Pełny flow działa |
| 42–45 min | Happy path i podsumowanie | Otrzymujesz `201 Created` |

Rozdział [Eksperymenty dodatkowe](#eksperymenty-dodatkowe) można wykonać po części podstawowej albo wykorzystać jako materiał do dalszej nauki.

> Aby realnie zmieścić się w 45 minutach, repozytorium powinno być pobrane, a zależności Mavena pobrane przed zajęciami. Jeżeli przygotowanie komputerów odbywa się podczas warsztatu, zaplanuj 55–60 minut.

Jeżeli **całe spotkanie**, razem z prezentacją, trwa 45 minut, zastosuj skrót:

| Czas | Aktywność |
|---:|---|
| 0–8 min | prezentacja i schemat architektury |
| 8–13 min | szybki przegląd klas, uruchomienie oraz health check |
| 13–17 min | prowadzący demonstruje bezpośredni request do inventory i notification |
| 17–23 min | studenci implementują `OrderService` |
| 23–30 min | studenci implementują `InventoryClient` |
| 30–37 min | studenci implementują `NotificationClient` przez analogię |
| 37–42 min | happy path i jeden błąd walidacji |
| 42–45 min | wnioski i pytania |

W tej wersji pobranie repozytorium oraz `clean verify` są obowiązkowym przygotowaniem przed zajęciami, a eksperymenty awarii pozostają materiałem po warsztacie.

---

# 0. Przygotowanie projektu

## 0.1. Pobierz repozytorium

```bash
git clone https://github.com/Rafal-Pawlowski/java-microservices-workskop.git
cd java-microservices-workskop
git switch student
git branch --show-current
```

Oczekiwany wynik ostatniego polecenia:

```text
student
```

> Nazwa repozytorium zawiera słowo `workskop`. Użyj dokładnie takiej nazwy katalogu, jaką utworzył `git clone`.

## 0.2. Sprawdź Javę

```bash
java -version
```

Projekt wymaga Java 21 lub nowszej.

## 0.3. Zbuduj cały projekt

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

### Co oznacza `clean verify`?

- `clean` usuwa poprzednie katalogi `target`, dzięki czemu zaczynamy od czystego stanu,
- Maven przechodzi przez wcześniejsze fazy cyklu, m.in. `compile`, `test` i `package`,
- `verify` uruchamia wszystkie kontrole przypięte do fazy `verify` i potwierdza, że cały projekt jest poprawny,
- polecenie uruchomione w katalogu głównym buduje wszystkie moduły zapisane w sekcji `<modules>` głównego `pom.xml`.

### Checkpoint 0

| Pytanie | Twoja odpowiedź |
|---|---|
| Na jakim branchu pracujesz? | |
| Jaką masz wersję Javy? | |
| Czy build zakończył się `BUILD SUCCESS`? | |

---

# 1. Poznaj kod, zanim go uruchomisz

## 1.1. Trzy aplikacje, trzy odpowiedzialności

| Moduł | Port | Odpowiedzialność | Główny endpoint |
|---|---:|---|---|
| `order-service` | 8080 | Przyjmuje i koordynuje zamówienie | `POST /api/orders` |
| `inventory-service` | 8081 | Sprawdza i rezerwuje stan magazynowy | `POST /api/inventory/reservations` |
| `notification-service` | 8082 | Symuluje wysłanie wiadomości | `POST /api/notifications` |

Każdy moduł ma własną klasę z metodą `main()`. Oznacza to, że uruchamiamy trzy osobne procesy JVM.

## 1.2. Prześledź klasy

Otwórz poniższe pliki w podanej kolejności.

### `order-service`

1. `api/OrderController.java` — wejście HTTP,
2. `api/CreateOrderRequest.java` — dane przychodzące i walidacja,
3. `application/OrderService.java` — orkiestracja procesu,
4. `client/inventory/InventoryClient.java` — wyjście HTTP do magazynu,
5. `client/notification/NotificationClient.java` — wyjście HTTP do powiadomień,
6. `domain/OrderResult.java` — wynik wewnętrzny,
7. `api/OrderResponse.java` — odpowiedź HTTP dla klienta.

### `inventory-service`

```text
ReservationRequest
    -> InventoryController
    -> InventoryService
    -> InventoryRepository
    -> H2
    -> InventoryReservation
    -> ReservationResponse
```

### `notification-service`

```text
NotificationRequest
    -> NotificationController
    -> NotificationService
    -> NotificationResult
    -> NotificationResponse
```

## 1.3. Jak rozumieć pakiety?

- `api` — kontrakt HTTP: kontrolery, requesty i response'y,
- `application` — przypadek użycia i koordynacja kroków,
- `domain` — reguły oraz pojęcia biznesowe,
- `client` — komunikacja wychodząca do innych systemów,
- `config` — konfiguracja techniczna Springa.

### Pytanie sprawdzające

Dlaczego `OrderController` nie wywołuje bezpośrednio `InventoryController`?

<details>
<summary>Sprawdź odpowiedź</summary>

Kontrolery należą do dwóch oddzielnych aplikacji i działają w osobnych procesach JVM. `order-service` nie ma bezpośredniego dostępu do obiektu `InventoryController`. Może skorzystać z kontraktu HTTP udostępnionego przez `inventory-service`.

</details>

---

# 2. Uruchom serwisy i sprawdź ich stan

Otwórz cztery terminale. Trzy będą przeznaczone na serwisy, a czwarty na wysyłanie requestów.

## Windows PowerShell

Uruchom wszystkie trzy serwisy z UI Intelij lub komendami jak niżej:

Terminal 1:

```powershell
.\mvnw.cmd -pl inventory-service spring-boot:run
```

Terminal 2:

```powershell
.\mvnw.cmd -pl notification-service spring-boot:run
```

Terminal 3:

```powershell
.\mvnw.cmd -pl order-service spring-boot:run
```

## macOS/Linux

Terminal 1:

```bash
./mvnw -pl inventory-service spring-boot:run
```

Terminal 2:

```bash
./mvnw -pl notification-service spring-boot:run
```

Terminal 3:

```bash
./mvnw -pl order-service spring-boot:run
```

## Health check

PowerShell:

```powershell
Invoke-RestMethod "http://localhost:8080/actuator/health"
Invoke-RestMethod "http://localhost:8081/actuator/health"
Invoke-RestMethod "http://localhost:8082/actuator/health"
```

macOS/Linux:

```bash
curl http://localhost:8080/actuator/health
curl http://localhost:8081/actuator/health
curl http://localhost:8082/actuator/health
```

Każdy serwis powinien zwrócić `UP`.

> Actuator odpowiada na pytanie „czy aplikacja działa technicznie?”. Nie gwarantuje jeszcze, że cały proces biznesowy działa poprawnie.

### Checkpoint 1

| Serwis | Port | Status | Terminal z logami |
|---|---:|---|---|
| order-service | 8080 | | |
| inventory-service | 8081 | | |
| notification-service | 8082 | | |

---

# 3. Sprawdź serwisy niezależnie

Na Windows używamy `curl.exe`, aby uniknąć aliasu PowerShell `curl`. Na macOS/Linux zamień `curl.exe` na `curl`.

## 3.1. Odczytaj stan produktu

Windows:

```powershell
curl.exe -i "http://localhost:8081/api/inventory/LAPTOP-001"
```

macOS/Linux:

```bash
curl -i "http://localhost:8081/api/inventory/LAPTOP-001"
```

Odczytaj początkową wartość `availableQuantity`.

## 3.2. Zarezerwuj produkt bezpośrednio

PowerShell:

```powershell
$inventoryBody = @{
    productId = "LAPTOP-001"
    quantity = 1
} | ConvertTo-Json

Invoke-RestMethod `
    -Method Post `
    -Uri "http://localhost:8081/api/inventory/reservations" `
    -ContentType "application/json" `
    -Body $inventoryBody
```

macOS/Linux:

```bash
curl -i -X POST "http://localhost:8081/api/inventory/reservations" \
  -H "Content-Type: application/json" \
  -d '{"productId":"LAPTOP-001","quantity":1}'
```

Oczekiwany rezultat: `201 Created`, identyfikator rezerwacji i status `CONFIRMED`.

Ponownie wykonaj `GET /api/inventory/LAPTOP-001`. Stan powinien zmniejszyć się o 1.

## 3.3. Wyślij powiadomienie bezpośrednio

PowerShell:

```powershell
$notificationBody = @{
    orderId = [guid]::NewGuid().ToString()
    customerEmail = "student@agh.edu.pl"
    message = "Your order has been created"
} | ConvertTo-Json

Invoke-RestMethod `
    -Method Post `
    -Uri "http://localhost:8082/api/notifications" `
    -ContentType "application/json" `
    -Body $notificationBody
```

macOS/Linux:

```bash
curl -i -X POST "http://localhost:8082/api/notifications" \
  -H "Content-Type: application/json" \
  -d '{"orderId":"4a5ba505-dbd5-4e6f-b197-d2349ad14f81","customerEmail":"student@agh.edu.pl","message":"Your order has been created"}'
```

Oczekiwany rezultat: `201 Created` oraz status `SENT`.

### Zatrzymaj się i odpowiedz

Czy mikroserwisy komunikują się już ze sobą?

<details>
<summary>Sprawdź odpowiedź</summary>

Nie. To klient wywołał każdy serwis osobno. Udowodniliśmy tylko, że endpointy `inventory-service` i `notification-service` działają niezależnie.

</details>

### Checkpoint 2

| Eksperyment | Status HTTP | Co pojawiło się w body? | W którym serwisie pojawił się log? |
|---|---:|---|---|
| GET produktu | | | |
| Rezerwacja | | | |
| Powiadomienie | | | |

---

# 4. Wywołaj niedokończony proces zamówienia

PowerShell:

```powershell
curl.exe -i -X POST "http://localhost:8080/api/orders" -H "Content-Type: application/json" -d '{\"productId\":\"LAPTOP-001\",\"quantity\":1,\"customerEmail\":\"student@agh.edu.pl\"}'
```

macOS/Linux:

```bash
curl -i -X POST "http://localhost:8080/api/orders" \
  -H "Content-Type: application/json" \
  -d '{"productId":"LAPTOP-001","quantity":1,"customerEmail":"student@agh.edu.pl"}'
```

Oczekiwany rezultat na branchu `student`: `500 Internal Server Error`.

> Do obserwowania kontrolowanych błędów używamy `curl.exe -i`, ponieważ pokazuje status HTTP i body. `Invoke-RestMethod` dla odpowiedzi 4xx/5xx zgłasza wyjątek PowerShell `WebException`, co może zasłonić właściwy rezultat HTTP.

W logach `order-service` znajdź:

```text
TODO: implement order creation flow
```

## Jak czytać ten błąd?

1. Request dotarł do `OrderController`.
2. Kontroler wywołał `OrderService.createOrder()`.
3. `OrderService.createOrder()` zakończył się kontrolowanym `UnsupportedOperationException`.
4. `InventoryClient` nie został jeszcze wywołany.
5. Request nie dotarł do `inventory-service` ani `notification-service`.

### Checkpoint 3

| Pytanie | Odpowiedź |
|---|---|
| Jaki status otrzymał klient? | |
| Która klasa przerwała flow? | |
| Czy w `inventory-service` pojawił się log rezerwacji? | |
| Czy w `notification-service` pojawił się log? | |

---

# 5. Zaimplementuj orkiestrację w `OrderService`

Otwórz:

```text
order-service/src/main/java/pl/edu/agh/workshop/application/OrderService.java
```

`OrderService` nie udostępnia endpointu. Jest wywoływany przez `OrderController` jak zwykły obiekt Springa. Jego zadaniem jest skoordynowanie kolejnych kroków.

Zastąp `UnsupportedOperationException` poniższym kodem:

```java
UUID orderId = UUID.randomUUID();

log.info(
        "[ORDER] Creating order. orderId={}, productId={}, quantity={}",
        orderId,
        productId,
        quantity
);

InventoryReservationResponse reservation =
        inventoryClient.reserve(productId, quantity);

log.info(
        "[ORDER] Inventory reserved. orderId={}, reservationId={}",
        orderId,
        reservation.reservationId()
);

NotificationClientResponse notification = notificationClient.send(
        orderId,
        customerEmail,
        "Order " + orderId + " has been created"
);

log.info(
        "[ORDER] Notification completed. orderId={}, status={}",
        orderId,
        notification.status()
);

return new OrderResult(
        orderId,
        reservation.reservationId(),
        productId,
        quantity,
        notification.status()
);
```

## Co właśnie zbudowałeś?

1. `orderId` identyfikuje cały proces zamówienia.
2. Wynik `InventoryClient` jest potrzebny do utworzenia `OrderResult`.
3. `NotificationClient` jest wywoływany dopiero po poprawnej rezerwacji.
4. Każde wywołanie jest synchroniczne — bieżący wątek czeka na wynik.
5. `OrderResult` jest modelem wewnętrznym, a `OrderController` zamienia go na `OrderResponse`.

Sprawdź moduł:

Windows:

```powershell
.\mvnw.cmd -pl order-service -am test
```

macOS/Linux:

```bash
./mvnw -pl order-service -am test
```

Uruchom ponownie `order-service` i wyślij zamówienie jeszcze raz.

Tym razem flow powinien dojść dalej i zakończyć się komunikatem:

```text
TODO: implement inventory reservation call
```

To postęp: `OrderController` i `OrderService` działają, a następna brakująca część została ujawniona.

---

# 6. Zaimplementuj `InventoryClient`

Otwórz:

```text
order-service/src/main/java/pl/edu/agh/workshop/client/inventory/InventoryClient.java
```

## STOP: właśnie przekraczasz granicę mikroserwisu

Zatrzymaj się tutaj przed pisaniem kodu. `InventoryClient` ma inne zadanie niż `OrderService` albo `InventoryService`.

```text
OrderService
    |
    | wywołanie metody Java
    v
InventoryClient                  GRANICA APLIKACJI
    |
    | HTTP + JSON  -------------------------------->
    v
InventoryController w innym procesie JVM
```

### Czym jest klasa `Client`?

`InventoryClient` jest adapterem komunikacji wychodzącej. Reprezentuje w `order-service` dostęp do zewnętrznego systemu — tutaj do `inventory-service`.

Odpowiada za szczegóły techniczne:

- wybór metody HTTP,
- zbudowanie ścieżki endpointu,
- przygotowanie requestu,
- wysłanie JSON,
- odebranie odpowiedzi,
- deserializację JSON do obiektu Java,
- docelowo także mapowanie błędów sieciowych i statusów HTTP.

Nie powinien zawierać reguł biznesowych zamówienia. Decyzja „najpierw rezerwacja, później powiadomienie” należy do `OrderService`.

### Klasy tworzące kontrakt komunikacji

| Klasa w `order-service` | Kierunek | Rola |
|---|---|---|
| `InventoryClient` | na zewnątrz | wykonuje wywołanie HTTP |
| `InventoryReservationRequest` | order → inventory | dane wysyłane jako JSON |
| `InventoryReservationResponse` | inventory → order | dane odczytywane z JSON odpowiedzi |
| `RestClientConfig` | konfiguracja | tworzy klienta z bazowym URL |

Po stronie `inventory-service` istnieją oddzielne klasy `ReservationRequest` i `ReservationResponse`. Mają zgodne pola JSON, ale nie są tymi samymi klasami Java.

### Dlaczego nie importujemy DTO z drugiego mikroserwisu?

Gdyby `order-service` zależał bezpośrednio od modułu i klas `inventory-service`, powstałoby silne sprzężenie na poziomie kodu. Zmiana lub wydanie jednego modułu mogłoby wymuszać przebudowę drugiego.

W tej aplikacji serwisy dzielą kontrakt HTTP/JSON, a nie pamięć i nie obiekty Java. Dzięki temu mogą działać jako osobne procesy i być wdrażane niezależnie.

> Podobne nazwy DTO po obu stronach są celowe: opisują tę samą wiadomość sieciową z perspektywy dwóch niezależnych aplikacji.

### Pytania przed przejściem dalej

1. Która klasa wie, że używamy HTTP: `OrderService` czy `InventoryClient`?
2. Która klasa ustala kolejność rezerwacji i powiadomienia?
3. Co się stanie, jeśli `inventory-service` zmieni nazwę pola `reservationId` w JSON?
4. Gdzie umieściłbyś timeout i mapowanie błędu `404`?

<details>
<summary>Sprawdź odpowiedzi</summary>

1. O HTTP wie `InventoryClient`.
2. Kolejność ustala `OrderService`.
3. Kontrakt przestanie być zgodny i deserializacja może się nie udać albo pole nie zostanie poprawnie wypełnione.
4. Timeout konfigurujemy dla klienta HTTP, a status `404` mapujemy przy granicy, czyli w `InventoryClient`.

</details>

## 6.1. Najpierw zrozum konstruktor

```java
public InventoryClient(
        @Qualifier("inventoryRestClient") RestClient restClient
) {
    this.restClient = restClient;
}
```

- `RestClient` wykonuje requesty HTTP,
- Spring tworzy dwa beany typu `RestClient`,
- `@Qualifier("inventoryRestClient")` wybiera klienta z adresem bazowym `http://localhost:8081`,
- konfigurację znajdziesz w `RestClientConfig` oraz `application.yml`.

### Pytanie sprawdzające

Dlaczego sam typ `RestClient` nie wystarcza do wybrania beana?

<details>
<summary>Sprawdź odpowiedź</summary>

W kontekście Springa znajdują się dwa beany tego samego typu: `inventoryRestClient` i `notificationRestClient`. `@Qualifier` usuwa niejednoznaczność.

</details>

## 6.2. Zastąp wyjątek implementacją

```java
InventoryReservationRequest request =
        new InventoryReservationRequest(productId, quantity);

InventoryReservationResponse response = restClient.post()
        .uri("/api/inventory/reservations")
        .body(request)
        .retrieve()
        .body(InventoryReservationResponse.class);

log.info(
        "[INVENTORY -> ORDER] Reservation response received: reservationId={}, status={}",
        response.reservationId(),
        response.status()
);

return response;
```

## 6.3. Co robi każdy fragment?

| Fragment | Znaczenie |
|---|---|
| `post()` | wybiera metodę HTTP POST |
| `uri(...)` | dodaje ścieżkę do bazowego URL klienta |
| `body(request)` | przekazuje obiekt Java; Jackson zapisze go jako JSON |
| `retrieve()` | wykonuje request i rozpoczyna odczyt odpowiedzi |
| `body(InventoryReservationResponse.class)` | zamienia JSON odpowiedzi na obiekt Java |

> Nadawca serializuje obiekt Java do JSON. Odbiorca deserializuje JSON do swojego DTO. W drodze powrotnej proces odbywa się odwrotnie.

## 6.4. Sprawdź kod bez pełnego czyszczenia

Windows:

```powershell
.\mvnw.cmd -pl order-service -am test
```

macOS/Linux:

```bash
./mvnw -pl order-service -am test
```

`-pl order-service` wybiera moduł, a `-am` dołącza moduły wymagane do jego zbudowania. To szybsza pętla pracy niż każdorazowe `clean verify` całego repozytorium.

Uruchom ponownie `order-service`, jeżeli DevTools nie przeładował aplikacji automatycznie.

---

# 7. Wyślij zamówienie ponownie — trzeci kontrolowany błąd

Ponownie wykonaj request z rozdziału 4.

Oczekiwany przebieg:

1. `order-service` wywoła `inventory-service`,
2. magazyn utworzy rezerwację i zmniejszy stan,
3. `order-service` spróbuje wywołać `NotificationClient`,
4. request zakończy się `500`, ponieważ metoda `send()` nadal zawiera TODO.

W logach `order-service` znajdź:

```text
TODO: implement notification call
```

Wykonaj `GET /api/inventory/LAPTOP-001` i sprawdź stan.

## Najważniejszy wniosek tego warsztatu

Request zakończył się błędem, ale rezerwacja została już wykonana. Każdy mikroserwis zarządza własnymi danymi i nie mamy jednej transakcji obejmującej trzy aplikacje.

W systemie produkcyjnym należałoby zaprojektować np. operację kompensującą, proces Saga albo komunikację zdarzeniową. Na tym warsztacie najważniejsze jest zauważenie problemu.

### Checkpoint 4

| Pytanie | Odpowiedź |
|---|---|
| Czy `inventory-service` otrzymał request? | |
| Czy stan magazynu się zmienił? | |
| Czy powiadomienie zostało wysłane? | |
| Dlaczego klient nadal otrzymał błąd? | |

---

# 8. Zaimplementuj `NotificationClient`

Otwórz:

```text
order-service/src/main/java/pl/edu/agh/workshop/client/notification/NotificationClient.java
```

## Zastosuj ten sam wzorzec drugi raz

`NotificationClient` jest drugim adapterem komunikacji wychodzącej. Zwróć uwagę na analogię:

| Inventory | Notification |
|---|---|
| `InventoryClient` | `NotificationClient` |
| `InventoryReservationRequest` | `SendNotificationRequest` |
| `InventoryReservationResponse` | `NotificationClientResponse` |
| `inventoryRestClient` | `notificationRestClient` |
| port 8081 | port 8082 |

Student nie poznaje więc drugiej, całkiem nowej techniki. Stosuje ponownie ten sam wzorzec do innego kontraktu HTTP.

> Nie logujemy pełnego `customerEmail`. Adres e-mail jest daną użytkownika; do śledzenia flow wystarcza `orderId`.

### Dlaczego `NotificationClientResponse` zawiera tylko `status`?

`notification-service` zwraca więcej pól: `notificationId`, `orderId`, `recipient`, `sentAt` i `status`. `order-service` potrzebuje jednak tylko statusu, dlatego jego lokalny DTO wygląda tak:

```java
public record NotificationClientResponse(String status) {
}
```

Jackson odczyta pole `status`, a pozostałe pola JSON pominie. Klient modeluje tylko tę część odpowiedzi, której naprawdę używa. Gdyby `order-service` potrzebował także `notificationId`, należałoby dodać zgodne pole do `NotificationClientResponse`.

Konstruktor używa:

```java
@Qualifier("notificationRestClient")
```

Dlatego ten klient otrzymuje bazowy URL `http://localhost:8082`, a nie adres magazynu.

Zastąp wyjątek implementacją:

```java
SendNotificationRequest request =
        new SendNotificationRequest(orderId, customerEmail, message);

NotificationClientResponse response = restClient.post()
        .uri("/api/notifications")
        .body(request)
        .retrieve()
        .body(NotificationClientResponse.class);

log.info(
        "[NOTIFICATION -> ORDER] Notification response received: orderId={}, status={}",
        orderId,
        response.status()
);

return response;
```

Sprawdź moduł:

Windows:

```powershell
.\mvnw.cmd -pl order-service -am test
```

macOS/Linux:

```bash
./mvnw -pl order-service -am test
```

Uruchom ponownie `order-service`, jeśli jest to potrzebne.

---

# 9. Przetestuj pełny happy path

Wyślij poprawne zamówienie po raz trzeci.

Oczekiwany status:

```text
201 Created
```

Przykładowa odpowiedź:

```json
{
  "orderId": "c307b410-c989-4efd-a48f-0610ff6ad7c7",
  "reservationId": "9271f516-abfe-4349-8a49-8fbe4a5b49a5",
  "productId": "LAPTOP-001",
  "quantity": 1,
  "notificationStatus": "SENT"
}
```

Identyfikatory UUID będą inne przy każdym wywołaniu.

## Odtwórz flow z logów

Ułóż logi chronologicznie:

1. `[ORDER] HTTP request received`,
2. `[ORDER] Creating order`,
3. `[ORDER -> INVENTORY] Sending reservation request`,
4. `[INVENTORY] HTTP reservation request received`,
5. `[INVENTORY] Reservation created`,
6. `[INVENTORY -> ORDER] Reservation response received`,
7. `[ORDER -> NOTIFICATION] Sending notification request`,
8. `[NOTIFICATION] HTTP notification request received`,
9. `Sending notification`,
10. `[NOTIFICATION -> ORDER] Notification response received`,
11. `[ORDER] Notification completed`.

### Checkpoint 5

| Pytanie | Odpowiedź |
|---|---|
| Jaki status HTTP otrzymał klient? | |
| Który serwis wygenerował `reservationId`? | |
| Który serwis wygenerował `orderId`? | |
| Czy `order-service` czekał na odpowiedzi pozostałych serwisów? | |
| Co oznacza `SENT`? | |

---

# 10. Walidacja — błąd przed rozpoczęciem flow

Wyślij niepoprawne dane.

Windows:

```powershell
curl.exe -i -X POST "http://localhost:8080/api/orders" -H "Content-Type: application/json" -d '{"productId":"","quantity":-1,"customerEmail":"not-an-email"}'
```

macOS/Linux:

```bash
curl -i -X POST "http://localhost:8080/api/orders" \
  -H "Content-Type: application/json" \
  -d '{"productId":"","quantity":-1,"customerEmail":"not-an-email"}'
```

Oczekiwany status:

```text
400 Bad Request
```

Sprawdź `CreateOrderRequest`:

```java
@NotBlank String productId,
@Positive int quantity,
@NotBlank @Email String customerEmail
```

`@Valid` w `OrderController` uruchamia walidację przed wejściem do `OrderService`. Dlatego pozostałe mikroserwisy nie powinny otrzymać żadnego requestu.

### Checkpoint 6

| Pytanie | Odpowiedź |
|---|---|
| Jaki status otrzymałeś? | |
| Czy wykonano rezerwację? | |
| W którym miejscu zatrzymano request? | |

---

# 11. Końcowa weryfikacja

Zatrzymaj serwisy i wykonaj pełny build.

Windows:

```powershell
.\mvnw.cmd clean verify
```

macOS/Linux:

```bash
./mvnw clean verify
```

Oczekiwany rezultat:

```text
BUILD SUCCESS
```

Sprawdź własne zmiany:

```bash
git status
git diff
```

Nie wykonuj `git init` wewnątrz modułów. Wszystkie trzy mikroserwisy należą do jednego repozytorium Git.

---

# Eksperymenty dodatkowe

Poniższe zadania pokazują problemy, które pojawiają się w prawdziwych systemach. Nie są wymagane do ukończenia 45-minutowej części podstawowej.

## A. Produkt nie istnieje

Wyślij zamówienie dla:

```json
{
  "productId": "UNKNOWN-001",
  "quantity": 1,
  "customerEmail": "student@agh.edu.pl"
}
```

Bez mapowania błędów `RestClient` otrzyma z magazynu `404`, ale `order-service` może zwrócić klientowi ogólne `500`.

Pytanie: dlaczego kontrakt błędu jednego serwisu nie został automatycznie kontraktem drugiego serwisu?

## B. Brak produktu w magazynie

Produkt `HEADPHONES-001` ma stan `0`.

Bezpośrednie wywołanie `inventory-service` powinno zwrócić:

```text
409 Conflict
```

Po wywołaniu przez `order-service` potrzebujemy świadomie zmapować wyjątek klienta HTTP na wyjątek naszej aplikacji.

## C. Wyłącz `inventory-service`

1. Zatrzymaj serwis działający na porcie 8081.
2. Wyślij poprawne zamówienie do portu 8080.
3. Znajdź w logach `Connection refused` lub inny `RestClientException`.

W produkcyjnej aplikacji nie chcemy zwracać przypadkowego `500`. Lepszym kontraktem jest:

```text
503 Service Unavailable
```

## D. Wyłącz `notification-service`

1. Uruchom ponownie inventory.
2. Zatrzymaj notification na porcie 8082.
3. Wyślij zamówienie.
4. Sprawdź stan magazynu przed i po requeście.

Pytanie: dlaczego rezerwacja istnieje, mimo że klient otrzymał błąd?

## E. Zmapuj błędy `InventoryClient`

Rozbuduj `reserve()` o obsługę wyjątków:

```java
try {
    InventoryReservationRequest request =
            new InventoryReservationRequest(productId, quantity);

    return restClient.post()
            .uri("/api/inventory/reservations")
            .body(request)
            .retrieve()
            .body(InventoryReservationResponse.class);

} catch (HttpClientErrorException.NotFound exception) {
    throw new ProductNotFoundException(productId);
} catch (HttpClientErrorException.Conflict exception) {
    throw new OutOfStockException(productId, quantity);
} catch (RestClientException exception) {
    throw new InventoryServiceUnavailableException(exception);
}
```

Potrzebne importy:

```java
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestClientException;
import pl.edu.agh.workshop.exception.InventoryServiceUnavailableException;
import pl.edu.agh.workshop.exception.OutOfStockException;
import pl.edu.agh.workshop.exception.ProductNotFoundException;
```

`ApiExceptionHandler` w `order-service` zamieni te wyjątki na `404`, `409` i `503`.

## F. Zmapuj awarię `NotificationClient`

```java
try {
    return restClient.post()
            .uri("/api/notifications")
            .body(request)
            .retrieve()
            .body(NotificationClientResponse.class);
} catch (RestClientException exception) {
    throw new NotificationServiceUnavailableException(exception);
}
```

Po zmianach sprawdź tabelę:

| Scenariusz | Oczekiwany status `order-service` |
|---|---:|
| Poprawne zamówienie | `201 Created` |
| Niepoprawne dane wejściowe | `400 Bad Request` |
| Nieznany produkt | `404 Not Found` |
| Brak stanu | `409 Conflict` |
| Inventory wyłączone | `503 Service Unavailable` |
| Notification wyłączone | `503 Service Unavailable` |

## G. Pytania projektowe bez kodowania

1. Czy wysłanie powiadomienia musi blokować odpowiedź dla klienta?
2. Czy notification lepiej wywołać synchronicznie, czy przez broker zdarzeń?
3. Co się stanie, gdy klient ponowi ten sam request po timeoutcie?
4. Jak zapobiec podwójnej rezerwacji?
5. Jak połączyć logi trzech serwisów jednym `correlationId`?
6. Gdzie dodać timeout, retry i circuit breaker?
7. Których danych, np. adresu e-mail, nie powinniśmy umieszczać w logach?

## H. Co można wykorzystać zamiast `RestClient`?

| Rozwiązanie | Kiedy może pasować? |
|---|---|
| `WebClient` | komunikacja reaktywna lub wiele równoległych wywołań |
| Spring HTTP Interface | deklaratywny interfejs opisujący endpointy |
| OpenFeign | deklaratywny klient w ekosystemie Spring Cloud |
| `java.net.http.HttpClient` | niższy poziom i pełna kontrola bez klienta Springa |
| klient wygenerowany z OpenAPI | gdy kontrakt API jest formalnie opisany i generowany |

Na warsztacie używamy `RestClient`, ponieważ dobrze pasuje do synchronicznego, imperatywnego flow i pokazuje kolejne etapy budowania requestu bez dodatkowej infrastruktury Spring Cloud.

---

# Ściąga: jak diagnozować błąd mikroserwisów

1. Zapisz status HTTP otrzymany przez klienta.
2. Zacznij od logów serwisu, do którego wysłałeś request.
3. Znajdź pierwszą linię stack trace z pakietu projektu.
4. Sprawdź, czy request wychodzący został zalogowany.
5. Przejdź do logów wywoływanego serwisu.
6. Sprawdź jego health endpoint.
7. Porównaj URL, port, metodę HTTP, `Content-Type` i JSON.
8. Ustal, czy operacja częściowo zmieniła dane.
9. Dopiero potem zmieniaj kod.

# Odpowiedź na pytanie końcowe

Co jest największym kosztem komunikacji synchronicznej?

`order-service` jest zależny od dostępności i czasu odpowiedzi pozostałych serwisów. Prosty kod klienta HTTP tworzy zależność sieciową, którą trzeba obsłużyć za pomocą dobrych kontraktów błędów, timeoutów, obserwowalności i przemyślanej spójności danych.
