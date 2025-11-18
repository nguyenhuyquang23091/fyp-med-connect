# Med-Connect Architecture Documentation Index

This document serves as the central index for all architecture documentation for the Med-Connect telemedicine platform.

## 📋 Table of Contents

- [Overview](#overview)
- [Architecture Documents](#architecture-documents)
- [Quick Reference](#quick-reference)
- [Microservices Catalog](#microservices-catalog)
- [How to Use This Documentation](#how-to-use-this-documentation)

---

## Overview

Med-Connect is an **enterprise-grade microservices-based telemedicine platform** built with Java 17 and Spring Boot 3.5. The platform consists of **10 independent microservices** working together to provide comprehensive healthcare services.

### Key Features

✅ **Online Appointment Booking** - Patients can schedule appointments with doctors
✅ **Video Consultations** - WebRTC-based video calls for remote consultations
✅ **AI Medical Assistant** - RAG-powered chatbot for medical questions
✅ **Prescription Management** - Secure storage and access control for prescriptions
✅ **Doctor Search** - Elasticsearch-powered full-text search for finding doctors
✅ **Real-time Notifications** - WebSocket and email notifications
✅ **Payment Processing** - VNPay integration for appointment payments
✅ **Secure File Storage** - Cloudinary-based medical document management

---

## Architecture Documents

### 1. **C4-Architecture-Diagrams.md**
📄 **Description:** Comprehensive C4 model architecture diagrams with ASCII art visualizations

**Contents:**
- System Context Diagram (Level 1) - Shows Med-Connect in the world
- Container Diagram (Level 2) - Shows all microservices and their relationships
- Component Details - Detailed breakdown of each service
- Communication Patterns - Synchronous (Feign/REST) and Asynchronous (Kafka)
- Technology Stack Summary
- Scalability Considerations
- Security Architecture
- Deployment Architecture

**When to use:** For understanding the overall system architecture, service interactions, and technology stack.

---

### 2. **C4-Diagrams-PlantUML.md**
📄 **Description:** Formal C4 model diagrams in PlantUML format (render-ready)

**Contents:**
- System Context Diagram (PlantUML)
- Container Diagram - Full Architecture (PlantUML)
- Container Diagram - Simplified View (PlantUML)
- Container Diagram - Core Services Focus (PlantUML)
- Component Diagram - Profile Service (Level 3) (PlantUML)
- Deployment Diagram - Docker Compose (PlantUML)
- Event-Driven Architecture - Kafka Topics (PlantUML)
- Sequence Diagram - Appointment Booking Flow (PlantUML)
- Sequence Diagram - Doctor Profile Update & Search Sync (PlantUML)
- Microservices Color-Coded Grouping (PlantUML)

**How to render:**
1. Visit: https://www.plantuml.com/plantuml/uml/
2. Copy-paste the desired PlantUML code
3. Or use VS Code PlantUML extension
4. Or use IntelliJ IDEA built-in support

**When to use:** For generating formal, presentation-ready architecture diagrams for stakeholders, documentation, or design reviews.

---

### 3. **PLANNING.md** (Existing)
📄 **Description:** Project overview and configuration guide

**Contents:**
- Service ports and database mappings
- Technology stack
- Development commands
- Code standards and patterns
- Common debugging tips

**When to use:** For setup, configuration, and development workflow reference.

---

### 4. **CLAUDE.md** (Existing)
📄 **Description:** Development guidelines and best practices

**Contents:**
- Code structure and modularity rules
- Testing and reliability guidelines
- Task completion workflow
- Java/Spring Boot conventions

**When to use:** For coding standards and development best practices.

---

## Quick Reference

### Microservices & Ports

| Service | Port | Database | Domain |
|---------|------|----------|--------|
| **API Gateway** | 8888 | Redis | Request routing, rate limiting |
| **Auth Service** | 8080 | PostgreSQL | User authentication & authorization |
| **Profile Service** | 8081 | Neo4j, MongoDB, Redis | User profiles & medical records |
| **RAG Chat Bot** | 8082 | PostgreSQL, Qdrant, Redis | AI medical consultation |
| **VNPay Service** | 8083 | PostgreSQL | Payment processing |
| **Appointment Service** | 8084 | PostgreSQL | Appointment management |
| **Notification Service** | 8085 | MongoDB | Real-time notifications |
| **File Service** | 8086 | MongoDB | Document management |
| **Search Service** | 8087 | Elasticsearch | Doctor search |
| **Video Call Service** | 8095 | PostgreSQL | Video consultations |

### Infrastructure Components

| Component | Port | Purpose |
|-----------|------|---------|
| **Apache Kafka** | 9094 (external), 9092 (internal) | Event streaming |
| **PostgreSQL** | 5432 | Relational database |
| **MongoDB** | 27017 | Document database |
| **Neo4j** | 7687 (bolt), 7474 (browser) | Graph database |
| **Redis** | 6379 | Cache & rate limiting |
| **Elasticsearch** | 9200 | Search engine |
| **Qdrant** | 6334 | Vector database for RAG |
| **Ollama** | 11434 | Local LLM service |

### External Services

| Service | Purpose |
|---------|---------|
| **Cloudinary** | File storage CDN |
| **Brevo** | Transactional email service |
| **VNPay** | Payment gateway |
| **Ollama** | AI/LLM service |

---

## Microservices Catalog

### Core Services (User Management & Profiles)

#### 🔐 Auth Service (8080)
**Responsibilities:** User authentication, JWT token management, RBAC
**Key Features:**
- Login/logout/refresh token
- User registration
- Role management (PATIENT, DOCTOR, ADMIN)
- Token introspection

**Database:** PostgreSQL (users, roles, permissions, tokens)

**Kafka Events:**
- **Publishes:** `user-role-updated` (when user role changes)

---

#### 👤 Profile Service (8081)
**Responsibilities:** User profiles, doctor info, prescription management
**Key Features:**
- Patient and doctor profiles
- Doctor specialties, services, experience
- Prescription storage with access control
- Avatar management

**Databases:**
- Neo4j (doctor profiles and relationships)
- MongoDB (user prescriptions)
- Redis (caching)

**Feign Clients:**
- File Service (media upload/delete)
- Notification Service (send alerts)
- Auth Service (user verification)

**Kafka Events:**
- **Consumes:** `user-role-updated` (creates doctor profiles)
- **Publishes:** `cdc.profileservice.doctor_profile_relationships` (profile changes for search indexing)

---

### Appointment & Scheduling

#### 📅 Appointment Service (8084)
**Responsibilities:** Appointment booking and management
**Key Features:**
- Create/cancel appointments
- View upcoming appointments (paginated)
- Patient and doctor appointment lists
- Payment integration

**Database:** PostgreSQL (appointments)

**Feign Clients:**
- Profile Service (doctor/patient info)
- VNPay Service (payment processing)

**Kafka Events:**
- **Publishes:** `video-call-events` (triggers video session creation)

---

### Search & Discovery

#### 🔍 Search Service (8087)
**Responsibilities:** Full-text search on doctor profiles
**Key Features:**
- Advanced doctor search with filters
- Search suggestions and autocomplete
- Real-time index synchronization via CDC
- Sorting and pagination

**Database:** Elasticsearch (doctor_profiles, users, appointments indexes)

**Kafka Events:**
- **Consumes:**
  - `cdc.authservice.public.users` (user changes)
  - `cdc.appointmentservice.public.appointments` (appointment changes)
  - `cdc.profileservice.doctor_profile_relationships` (doctor profile changes)

---

### Communication Services

#### 🔔 Notification Service (8085)
**Responsibilities:** Real-time in-app and email notifications
**Key Features:**
- WebSocket-based real-time notifications
- Email notifications via Brevo
- Notification history and read status
- Prescription and video call alerts

**Database:** MongoDB (notifications)

**Feign Clients:**
- Brevo Email API (send transactional emails)

**Kafka Events:**
- **Consumes:** `notification-delivery` (sends emails)

---

#### 🎥 Video Call Service (8095)
**Responsibilities:** WebRTC video consultation sessions
**Key Features:**
- Video call room creation
- Scheduled sessions linked to appointments
- Session lifecycle management
- Call status tracking

**Database:** PostgreSQL (session_video_calls)

**Feign Clients:**
- Notification Service (send call notifications)

**Kafka Events:**
- **Consumes:** `video-call-events` (creates sessions 5 min before appointments)

---

### File & Document Management

#### 📁 File Service (8086)
**Responsibilities:** Medical document storage and management
**Key Features:**
- File upload to Cloudinary
- File deletion with authorization
- Medical document metadata tracking

**Database:** MongoDB (file_management)

**External APIs:**
- Cloudinary (file CDN storage)

---

### AI & Intelligence

#### 🤖 RAG Chat Bot (8082)
**Responsibilities:** AI-powered medical consultation
**Key Features:**
- Retrieval Augmented Generation (RAG)
- Chat history management
- Context-aware medical responses
- Conversation deletion

**Databases:**
- PostgreSQL (chat sessions)
- Qdrant (vector embeddings)
- Redis (session caching)

**Feign Clients:**
- Profile Service (user context)

**External APIs:**
- Ollama (local LLM)

---

### Payment Processing

#### 💳 VNPay Payment Service (8083)
**Responsibilities:** Payment processing for appointments
**Key Features:**
- VNPay gateway integration
- Payment callback handling
- Payment status tracking
- Real-time payment updates via Socket.IO

**Database:** PostgreSQL (payments)

**Feign Clients:**
- Profile Service (get payer info)

**External APIs:**
- VNPay Payment Gateway

---

### Gateway & Routing

#### 🌐 API Gateway (8888)
**Responsibilities:** Central entry point for all requests
**Key Features:**
- Request routing to microservices
- Rate limiting (configurable per route)
- Authentication relay
- Request/response filtering

**Database:** Redis (rate limiting counters)

**Routes:**
- `/api/v1/identity/**` → Auth Service (8080)
- `/api/v1/profile/**` → Profile Service (8081)
- `/api/v1/chatbot/**` → RAG Chat Bot (8082)
- `/api/v1/payment/**` → VNPay Service (8083)
- `/api/v1/appointment/**` → Appointment Service (8084)
- `/api/v1/notifications/**` → Notification Service (8085)
- `/api/v1/file/**` → File Service (8086)
- `/api/v1/search/**` → Search Service (8087)
- `/notification/ws/**` → WebSocket (8085)

---

## How to Use This Documentation

### For Developers

1. **Starting a new feature?**
   - Read `CLAUDE.md` for coding standards
   - Check `PLANNING.md` for service architecture
   - Review `C4-Architecture-Diagrams.md` for understanding service interactions

2. **Need to understand inter-service communication?**
   - See "Communication Patterns" section in `C4-Architecture-Diagrams.md`
   - Review Kafka topics and Feign client mappings

3. **Want to visualize the architecture?**
   - Use PlantUML diagrams from `C4-Diagrams-PlantUML.md`
   - Render them in VS Code or online

### For Architects

1. **System design review?**
   - Start with System Context Diagram in `C4-Diagrams-PlantUML.md`
   - Review Container Diagram for service boundaries
   - Check Component Diagram for internal service structure

2. **Scalability planning?**
   - Review "Scalability Considerations" in `C4-Architecture-Diagrams.md`
   - Check data partitioning and caching strategies

3. **Security audit?**
   - Review "Security Architecture" section
   - Check authentication flow and authorization patterns

### For Stakeholders

1. **Understanding the system?**
   - Start with this index for overview
   - Review System Context Diagram (shows Med-Connect in the world)
   - Check "Key Features" section above

2. **Deployment and infrastructure?**
   - Review Deployment Diagram in `C4-Diagrams-PlantUML.md`
   - Check infrastructure components table above

3. **Technology stack?**
   - Review "Quick Reference" section above
   - See "Technology Stack Summary" in `C4-Architecture-Diagrams.md`

---

## Communication Patterns Summary

### Synchronous (Feign/REST)

```
Profile Service ──────▶ File Service (media)
                ├─────▶ Notification Service (alerts)
                └─────▶ Auth Service (verification)

Appointment Service ──▶ Profile Service (info)
                    └─▶ VNPay Service (payment)

Video Call Service ───▶ Notification Service (alerts)

RAG Chat Bot ─────────▶ Profile Service (context)

VNPay Service ────────▶ Profile Service (info)
```

### Asynchronous (Kafka Events)

```
Auth Service ──[user-role-updated]──▶ Profile Service
Profile Service ──[doctor-profile CDC]──▶ Search Service
Appointment Service ──[video-call-events]──▶ Video Call Service
Auth Service ──[user CDC]──▶ Search Service
Appointment Service ──[appointment CDC]──▶ Search Service
Multiple Services ──[notification-delivery]──▶ Notification Service
```

---

## Data Flow Example: Appointment Booking

```
┌─────────┐
│ Patient │
└────┬────┘
     │ 1. Select doctor & time
     ▼
┌──────────────┐
│   Frontend   │
│  (React SPA) │
└──────┬───────┘
       │ 2. POST /api/v1/appointment/create
       ▼
┌──────────────┐
│ API Gateway  │
└──────┬───────┘
       │ 3. Route to Appointment Service
       ▼
┌─────────────────────┐
│ Appointment Service │
│  (8084)             │──┐
└─────────┬───────────┘  │
          │              │ 4. Feign Calls
          ▼              │
    ┌─────────────┐      │
    │  Profile    │◀─────┤ (Validate doctor/patient)
    │  Service    │      │
    └─────────────┘      │
                         │
    ┌─────────────┐      │
    │   VNPay     │◀─────┘ (Process payment)
    │   Service   │
    └─────────────┘
          │
          │ 5. Save appointment to PostgreSQL
          │
          ▼
    ┌─────────────┐
    │    Kafka    │
    │ [video-call-│
    │   events]   │
    └──────┬──────┘
           │ 6. Event consumed
           ▼
    ┌─────────────────┐
    │  Video Call     │
    │  Service (8095) │
    └────────┬────────┘
             │ 7. Create session
             │    (5 min before appointment)
             ▼
    ┌─────────────────┐
    │  Notification   │
    │  Service (8085) │
    └────────┬────────┘
             │ 8. Send notifications
             │
             ├─▶ WebSocket → Patient & Doctor
             └─▶ Email (via Brevo) → Patient & Doctor
```

---

## Service Dependency Graph

```
                    ┌──────────────┐
                    │ API Gateway  │
                    │   (8888)     │
                    └───────┬──────┘
                            │
          ┌─────────────────┼─────────────────┐
          │                 │                 │
    ┌─────▼─────┐     ┌─────▼─────┐    ┌─────▼─────┐
    │   Auth    │     │  Profile  │    │  Search   │
    │  (8080)   │     │  (8081)   │    │  (8087)   │
    └─────┬─────┘     └─────┬─────┘    └─────┬─────┘
          │                 │                 │
          └────────┬────────┴────────┬────────┘
                   │                 │
             ┌─────▼─────┐     ┌─────▼─────┐
             │Appointment│     │   File    │
             │  (8084)   │     │  (8086)   │
             └─────┬─────┘     └───────────┘
                   │
          ┌────────┼────────┐
          │        │        │
    ┌─────▼─┐ ┌────▼───┐ ┌─▼────────┐
    │VNPay  │ │Notif.  │ │Video Call│
    │(8083) │ │(8085)  │ │ (8095)   │
    └───────┘ └────────┘ └──────────┘

    ┌──────────────┐
    │  RAG Chat    │ (Independent)
    │   (8082)     │
    └──────────────┘

    ┌──────────────┐
    │    Kafka     │ (Infrastructure)
    │   (9094)     │
    └──────────────┘
```

**Critical Path:** Auth Service must be running first (other services depend on it for JWT validation)

---

## Technology Stack

### Backend
- **Java 17** - Primary language
- **Spring Boot 3.5.x** - Application framework
- **Spring Cloud Gateway** - API Gateway
- **Spring Security** - OAuth2/JWT authentication
- **Spring Data JPA** - PostgreSQL ORM
- **Spring Data MongoDB** - MongoDB integration
- **Spring Data Neo4j** - Graph database integration
- **Spring Data Redis** - Caching
- **Spring AI** - LLM integration
- **MapStruct** - DTO mapping

### Databases
- **PostgreSQL** - Relational data
- **MongoDB** - Document storage
- **Neo4j** - Graph database
- **Redis** - Caching & rate limiting
- **Elasticsearch** - Full-text search
- **Qdrant** - Vector database

### Messaging & Events
- **Apache Kafka** - Event streaming
- **Debezium** - Change Data Capture
- **Socket.IO** - WebSocket communication

### External APIs
- **Cloudinary** - File CDN
- **Brevo** - Email service
- **VNPay** - Payment gateway
- **Ollama** - Local LLM

### Build & Deployment
- **Maven** - Build tool
- **Docker & Docker Compose** - Containerization

---

## Getting Started

### Prerequisites
- Java 17+
- Docker & Docker Compose
- Maven 3.8+

### Quick Start

```bash
# 1. Start infrastructure (databases, Kafka)
docker-compose up -d

# 2. Build all services
mvn clean install

# 3. Start services (in order)
cd authservice && mvn spring-boot:run &
cd profile_service && mvn spring-boot:run &
cd appointment_service && mvn spring-boot:run &
cd search_service && mvn spring-boot:run &
cd notification_service && mvn spring-boot:run &
cd file_service && mvn spring-boot:run &
cd rag-chat-bot && mvn spring-boot:run &
cd video_call_service && mvn spring-boot:run &
cd vnpay && mvn spring-boot:run &
cd api-gateway && mvn spring-boot:run &
```

### Access Points
- **API Gateway:** http://localhost:8888
- **Frontend:** (Your React app URL)

---

## Document Maintenance

### Update Schedule
- **Major architecture changes:** Update all diagrams immediately
- **New microservice added:** Add to all catalog tables
- **Service port/database change:** Update Quick Reference section

### Version History
- **v1.0** (2025-11-15): Initial architecture documentation
  - Created C4 System Context and Container diagrams
  - Documented all 10 microservices
  - Added PlantUML diagrams for formal visualization

---

## Additional Resources

- **C4 Model:** https://c4model.com/
- **C4 Microservices:** https://c4model.com/abstractions/microservices
- **Spring Boot:** https://spring.io/projects/spring-boot
- **Apache Kafka:** https://kafka.apache.org/
- **PlantUML:** https://plantuml.com/

---

**Document Maintained By:** Med-Connect Engineering Team
**Last Updated:** 2025-11-15
**Version:** 1.0
