# Streaming Data Application (SDA)

A Spring Boot application that simulates, processes, and streams market data over WebSockets with optional CSV export.

## Prerequisites

* **Java 11** or higher installed (`java -version`)
* **Maven 3.8+** installed (`mvn -v`)
* A terminal or IDE (e.g., IntelliJ, Eclipse, VS Code)

---

## Setup

1. **Clone the repository**

   ```bash
   git clone https://github.com/ch3lla/sda.git
   cd sda
   ```

2. **Build the project**

   ```bash
   mvn clean install
   ```

3. **Configure application properties**
   Edit `src/main/resources/application.yml` (or `.properties`) to adjust:

    * WebSocket path
    * CSV export path and interval
    * Buffer sizes

---

## Run the Application

Start the app with:

```bash
mvn spring-boot:run
```

The app will launch on the default port (e.g., `http://localhost:8080`).

---

## Connect to the WebSocket

By default, clients can connect to the configured WebSocket path (e.g., `/ws/data`):

```javascript
const socket = new WebSocket("ws://localhost:8080/market-data");

socket.onmessage = (event) => {
  console.log("Market data:", event.data);
};
```

---

## Run Tests

Unit tests are provided with **JUnit 5** and **Reactor Test**.

Run them with:

```bash
mvn test
```

---

## CSV Export

Processed market data is also written to a CSV file (path configured in `application.properties`).
If the file does not exist, it is created with headers.

---

## Quick Start Summary

1. Clone the repo
2. Run `mvn spring-boot:run`
3. Open a WebSocket client at `ws://localhost:8080/ws/data`
4. Run tests with `mvn test`
