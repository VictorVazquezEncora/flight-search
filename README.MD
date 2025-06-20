# Flights Search – Full-Stack Application

> A production-ready flight search platform built with **Spring Boot (Reactive)** in the backend and **React + TypeScript** in the frontend. The application consumes the [Amadeus Flight Offers API](https://developers.amadeus.com/) to provide real-time flight information and is fully containerised with Docker.

<p align="center">
  <img alt="Java" src="https://img.shields.io/badge/Java-17-orange" />
  <img alt="Gradle" src="https://img.shields.io/badge/Gradle-8.x-important" />
  <img alt="React" src="https://img.shields.io/badge/React-18-blue" />
  <img alt="TypeScript" src="https://img.shields.io/badge/TypeScript-5-blue" />
  <img alt="Tailwind" src="https://img.shields.io/badge/TailwindCSS-3-blueviolet" />
  <img alt="Docker" src="https://img.shields.io/badge/Docker-ready-informational" />
</p>

---

## Table of Contents

1. [Project Structure](#project-structure)
2. [Requirements](#requirements)
3. [Environment Variables](#environment-variables)
4. [Quick Start (Docker Compose)](#quick-start-docker-compose)
5. [Running Locally (without Docker)](#running-locally-without-docker)
   1. [Backend](#backend)
   2. [Frontend](#frontend)
6. [API Endpoints](#api-endpoints)
7. [Features](#features)
8. [Architecture](#architecture)
9. [Tech Stack](#tech-stack)
10. [Security Notes](#security-notes)

---

## Project Structure

```
flights-search/
  ├─ backend/            # Spring Boot (Gradle) service
  │   ├─ src/main/java   # Java source code
  │   └─ Dockerfile      # Multi-stage build (JDK → JRE)
  ├─ frontend/           # React + Vite SPA
  │   ├─ src/            # React source code (TypeScript)
  │   └─ Dockerfile      # Build (Node) → runtime (Nginx)
  ├─ docker-compose.yml  # Orchestrates backend & frontend
  ├─ .env.example        # Template for sensitive variables (NOT tracked)
  └─ README.md           # You are here
```

---

## Requirements

| Purpose      | Tool / Version |
|--------------|----------------|
| Build & Run  | Docker 24+ / Docker Compose v2 |
| Backend Dev  | JDK 17+, Gradle Wrapper (included) |
| Frontend Dev | Node.js 18+ (or 20 LTS), npm 9+ |

> *If you intend to use Docker exclusively you only need Docker & Docker Compose.*

---

## Environment Variables

The backend needs an **Amadeus API key and secret**. They are supplied through environment variables and are **never** committed to the repository.

Create a `.env` file in the project frontend folder with the following content:

```dotenv
VITE_DEVELOP_API=
```

Create the `application.yml` file in the project backend folder with the following content:

```application
amadeus:
  api:
    key: "YOUR KEY HERE"
    secret: "YOUR SECRET HERE"
    base-url: "https://test.api.amadeus.com"
```

Docker Compose automatically picks up variables from this file and passes them to the backend container. In local development without Docker you still need to export the same variables to your shell/session.

---

## Quick Start (Docker Compose)

```bash
# 1. Clone the repository
$ git clone https://github.com/your-user/flights-search.git
$ cd flights-search

# 2. Add your credentials
$ cp .env.example .env     # then edit .env with your API
$ add your key and secret to application.yml

# 3. Build and start everything
$ docker compose up --build

# 4. Open the frontend
# http://localhost:3000
```

Compose will:
1. Build the **backend** image (`backend/Dockerfile`) → exposes port 8080 inside the network.
2. Build the **frontend** image (`frontend/Dockerfile`) → served via Nginx on host port `3000`, proxying API calls to `backend:8080` thanks to `nginx.conf`.

Stop the stack with `docker compose down`.

---

## Running Locally (without Docker)

### Backend

```bash
# From the project root
$ cd backend

## Mac/Linux
$ ./gradlew bootRun

## Windows (PowerShell)
> .\gradlew.bat bootRun
```

The service starts on `http://localhost:8080` and reads `AMADEUS_API_KEY` and `AMADEUS_API_SECRET` from your environment.

### Frontend

```bash
# From the project root
$ cd frontend
$ npm install          # install dependencies
$ npm run dev          # starts Vite on http://localhost:5173
```

The dev server proxies requests beginning with `/api` to `http://localhost:8080`, so you get a seamless local workflow.

## API Endpoints

The backend exposes a minimal REST API under the prefix `/api` (see `WebConfig.java`). The main routes are:

| Method | Path                    | Description                       |
|--------|-------------------------|-----------------------------------|
| GET    | `/api/locations`        | Autocomplete IATA codes           |
| POST   | `/api/flight-offers`    | Retrieve flight offers (payload: search parameters) |

> The exact DTOs are defined under `backend/src/main/java/.../dto`.

---

## Features

### Frontend
* Modern UI built with **shadcn/ui** + TailwindCSS.
* Location autocomplete with debounced queries.
* Parameters: origin, destination, departure/return dates, travellers (adults / children / infants), travel class, currency, max price, non-stop filter.
* Reactive loading states with overlay spinners.

### Backend
* **Spring WebFlux** (non-blocking) for high concurrency.
* Centralised error handling (`GlobalExceptionHandler`).
* External API integration via WebClient with token caching.

---

## Architecture

* **Frontend** follows a feature-based directory layout for scalability.
* **Backend** uses a classic layered approach (controller → service → client) with some Hexagonal Architecture principles with use of Ports and Adapters.

---

## Tech Stack

* **Backend**: Spring Boot 3 • WebFlux • GraalVM-ready build
* **Build**: Gradle 8 (wrapper) • JDK 17
* **Frontend**: React 18 • TypeScript 5 • Vite 5 • TailwindCSS 3 • shadcn/ui • React Router 6
* **Containerisation**: Multi-stage Dockerfiles • Nginx static serving • Docker Compose networking

---

## Security Notes

* Secrets **never** live inside `application.yml`. Values are read from **environment variables**.
* `.env` files are excluded via `.gitignore` & `.dockerignore`.
* Docker Compose passes secrets only at runtime; images remain clean.
* Consider Docker Secrets or HashiCorp Vault for production deployments.
