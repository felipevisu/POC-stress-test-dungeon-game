# Dungeon Game API Stress Testing with K6

## Project Overview

- **Dungeon Game API**: Java Spring Boot application for managing players, boards, and games.
- **Goal**: Ensure reliability and scalability under heavy load using automated stress testing.

---

## Why Stress Test?

- **Validate performance**: Identify bottlenecks before production.
- **Simulate real-world load**: Multiple users, rapid API calls.
- **Monitor system health**: Database, API, and infrastructure.

---

## K6 – The Load Testing Tool

- **K6**: Modern, open-source tool for API load testing.
- **Features**:
  - Scripting in JavaScript.
  - Custom stages and thresholds.
  - Integration with InfluxDB and Grafana for visualization.

---

## K6 Test Script Highlights

```javascript
export let options = {
  thresholds: {
    http_req_failed: ["rate<0.01"], // <1% errors
    http_req_duration: ["p(95)<200"], // 95% < 200ms
  },
  stages: [
    { duration: "10s", target: 50 },
    { duration: "30s", target: 50 },
    { duration: "20s", target: 200 },
    { duration: "1m", target: 200 },
    { duration: "20s", target: 500 },
    { duration: "2m", target: 500 },
    { duration: "30s", target: 0 },
  ],
};
```

- **Stages**: Gradually ramp up to 500 users, then ramp down.
- **Thresholds**: Automatically fail if performance drops.

---

## API Coverage in Test

- **Player creation**: POST `/api/players`
- **Board creation**: POST `/api/boards`
- **Game play**: POST `/api/games/play`
- **Data retrieval**: GET endpoints for players, boards, games
- **Checks**: Validate each response for correctness and status

---

## Infrastructure & Visualization

- **Docker Compose**: Orchestrates app, database, K6, InfluxDB, Grafana.
- **InfluxDB**: Stores K6 metrics.
- **Grafana**: Visualizes test results with custom dashboards.
- **Test Runner**: Web UI to trigger tests and auto-create dashboards.

---

## Results & Insights

- **Metrics tracked**:
  - Response times (avg, max, 95th percentile)
  - Error rates
  - Requests per second
  - Virtual user count
- **Dashboards**: Each test run generates a new Grafana dashboard for analysis.

---

## Key Takeaways

- **Automated stress testing** ensures your API is production-ready.
- **K6 + Grafana** provides actionable insights.
- **Easy workflow**: One-click test runs, instant dashboards, clear metrics.

---

## Next Steps

- Tune thresholds and stages for your needs.
- Analyze dashboard results after each run.
- Use findings to optimize code and infrastructure.

---

## Q&A

- Open for questions about K6, stress testing, or the setup!
