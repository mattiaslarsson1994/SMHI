# API Documentation

## Overview

The SMHI Weather Observations Backend provides a REST API for accessing weather data from Swedish Meteorological and Hydrological Institute (SMHI) weather stations. The API merges multiple weather parameters into unified observation points and supports geographic and time-based filtering.

## Base URL

```
http://localhost:8080/api
```

## Authentication

All API endpoints require authentication via API key in the request header:

```
x-api-key: fhn1CaoXEI6gfOVOuHbJbAbFiM8MY4PH
```

### Public Endpoints (No Authentication Required)

- Swagger UI: `http://localhost:8080/swagger-ui.html`
- OpenAPI Documentation: `http://localhost:8080/v3/api-docs`
- Health Check: `http://localhost:8080/actuator/health`
- Favicon: `http://localhost:8080/favicon.ico`

## Endpoints

### 1. Get Weather Stations

Retrieves a list of available weather stations.

```http
GET /api/stations
```

#### Parameters

| Parameter | Type | Required | Default | Description |
|-----------|------|----------|---------|-------------|
| `set` | string | No | "core" | Station set type: "core" (major stations), "additional" (other stations), or "all" (all stations) |

#### Example Request

```bash
curl -H "x-api-key: fhn1CaoXEI6gfOVOuHbJbAbFiM8MY4PH" \
     "http://localhost:8080/api/stations?set=all"
```

#### Response

```json
[
  {
    "id": "159880",
    "name": "Stockholm A",
    "lat": 59.3,
    "lon": 18.1
  },
  {
    "id": "159881",
    "name": "Göteborg A",
    "lat": 57.7,
    "lon": 11.9
  }
]
```

#### Response Fields

| Field | Type | Description |
|-------|------|-------------|
| `id` | string | Unique station identifier |
| `name` | string | Human-readable station name |
| `lat` | number | Latitude coordinate |
| `lon` | number | Longitude coordinate |

---

### 2. Get Weather Observations

Retrieves merged weather observations from specified stations with optional filtering.

```http
GET /api/observations
```

#### Parameters

| Parameter | Type | Required | Default | Description |
|-----------|------|----------|---------|-------------|
| `stationId` | string | No | All stations | Comma-separated list of station IDs |
| `range` | string | No | "last-hour" | Time range: "last-hour" or "last-day" |
| `from` | string | No | - | Start timestamp (ISO 8601 format) |
| `to` | string | No | - | End timestamp (ISO 8601 format) |
| `lat` | number | No | - | Latitude for geographic filtering |
| `lon` | number | No | - | Longitude for geographic filtering |
| `radiusKm` | number | No | - | Search radius in kilometers |

#### Example Requests

**Get latest hour observations for Stockholm:**
```bash
curl -H "x-api-key: fhn1CaoXEI6gfOVOuHbJbAbFiM8MY4PH" \
     "http://localhost:8080/api/observations?stationId=159880&range=last-hour"
```

**Get last day observations for multiple stations:**
```bash
curl -H "x-api-key: fhn1CaoXEI6gfOVOuHbJbAbFiM8MY4PH" \
     "http://localhost:8080/api/observations?stationId=159880,159881&range=last-day"
```

**Get observations within 50km of Stockholm:**
```bash
curl -H "x-api-key: fhn1CaoXEI6gfOVOuHbJbAbFiM8MY4PH" \
     "http://localhost:8080/api/observations?lat=59.3&lon=18.1&radiusKm=50"
```

**Get observations for specific time range:**
```bash
curl -H "x-api-key: fhn1CaoXEI6gfOVOuHbJbAbFiM8MY4PH" \
     "http://localhost:8080/api/observations?from=2025-01-23T10:00:00Z&to=2025-01-23T12:00:00Z"
```

#### Response

```json
[
  {
    "stationId": "159880",
    "stationName": "Stockholm A",
    "lat": 59.3,
    "lon": 18.1,
    "timestampUtc": "2025-01-23T10:00:00Z",
    "gustMs": 12.4,
    "airTempC": 17.1,
    "windSpeedMs": 5.6
  },
  {
    "stationId": "159880",
    "stationName": "Stockholm A",
    "lat": 59.3,
    "lon": 18.1,
    "timestampUtc": "2025-01-23T10:10:00Z",
    "gustMs": 9.8,
    "airTempC": null,
    "windSpeedMs": 4.2
  }
]
```

#### Response Fields

| Field | Type | Description |
|-------|------|-------------|
| `stationId` | string | Unique station identifier |
| `stationName` | string | Human-readable station name |
| `lat` | number | Station latitude coordinate |
| `lon` | number | Station longitude coordinate |
| `timestampUtc` | string | UTC timestamp of observation (ISO 8601) |
| `gustMs` | number\|null | Wind gust speed in meters per second |
| `airTempC` | number\|null | Air temperature in Celsius |
| `windSpeedMs` | number\|null | Wind speed in meters per second |

## Data Sources

The API integrates with SMHI's Open Data API and merges data from the following weather parameters:

| Parameter ID | Parameter Name | Description | Status |
|--------------|----------------|-------------|---------|
| 1 | Lufttemperatur | Air Temperature | ✅ Fully Implemented |
| 4 | Vindhastighet | Wind Speed | ✅ Fully Implemented |
| 21 | Byvind | Wind Gust | ✅ Fully Implemented |

## Error Responses

### 401 Unauthorized

Returned when API key is missing or invalid.

```json
{
  "error": "invalid api key"
}
```

### 400 Bad Request

Returned for invalid parameters or malformed requests.

```json
{
  "timestamp": "2025-01-23T10:00:00Z",
  "status": 400,
  "error": "Bad Request",
  "message": "Invalid parameter value",
  "path": "/api/observations"
}
```

### 500 Internal Server Error

Returned for server-side errors or SMHI API unavailability.

```json
{
  "timestamp": "2025-01-23T10:00:00Z",
  "status": 500,
  "error": "Internal Server Error",
  "message": "Unable to fetch data from SMHI",
  "path": "/api/observations"
}
```

## Rate Limiting

The API implements caching to reduce load on SMHI's servers:

- **Station metadata**: Cached for 24 hours
- **Observation data**: Cached for 60 seconds

## Geographic Filtering

When using geographic filtering (`lat`, `lon`, `radiusKm`), the API uses the Haversine formula to calculate distances between the specified coordinates and weather stations. Only observations from stations within the specified radius are returned.

## Time Range Handling

### Predefined Ranges

- **`last-hour`**: Returns observations from the last hour
- **`last-day`**: Returns observations from the last 24 hours

### Custom Time Ranges

Use `from` and `to` parameters with ISO 8601 timestamps:

```
from=2025-01-23T10:00:00Z&to=2025-01-23T12:00:00Z
```

## Data Merging Logic

The API merges multiple weather parameters by:

1. Fetching data for each parameter (temperature, wind gusts, wind speed)
2. Creating a union of all timestamps across parameters
3. Combining measurements into unified observation points
4. Preserving null values for missing measurements
5. Sorting results by timestamp (newest first) and station ID

**Note**: All three weather parameters (temperature, wind speed, and wind gusts) are now fully implemented and included in the merged observations.

## Example Usage Scenarios

### 1. Real-time Weather Dashboard

Get the latest observations from all stations:

```bash
curl -H "x-api-key: fhn1CaoXEI6gfOVOuHbJbAbFiM8MY4PH" \
     "http://localhost:8080/api/observations"
```

### 2. Local Weather Monitoring

Get observations near a specific location:

```bash
curl -H "x-api-key: fhn1CaoXEI6gfOVOuHbJbAbFiM8MY4PH" \
     "http://localhost:8080/api/observations?lat=59.3&lon=18.1&radiusKm=25&range=last-hour"
```

### 3. Historical Data Analysis

Get a full day of data for analysis:

```bash
curl -H "x-api-key: fhn1CaoXEI6gfOVOuHbJbAbFiM8MY4PH" \
     "http://localhost:8080/api/observations?stationId=159880&range=last-day"
```

### 4. Multi-station Comparison

Compare weather across multiple stations:

```bash
curl -H "x-api-key: fhn1CaoXEI6gfOVOuHbJbAbFiM8MY4PH" \
     "http://localhost:8080/api/observations?stationId=159880,159881,159882&range=last-hour"
```

## Interactive Documentation

Access the interactive Swagger UI at:
```
http://localhost:8080/swagger-ui.html
```

The Swagger UI provides:
- Interactive API testing
- Request/response examples
- Parameter documentation
- Authentication testing
