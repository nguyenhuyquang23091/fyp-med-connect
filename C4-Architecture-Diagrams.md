# Med-Connect C4 Architecture Diagrams

This document contains C4 model architecture diagrams for the Med-Connect telemedicine platform, following the C4 model Stage 2 principles for microservices-based systems.

## Table of Contents
- [System Context Diagram (Level 1)](#system-context-diagram-level-1)
- [Container Diagram (Level 2)](#container-diagram-level-2)
- [Component Details](#component-details)
- [Communication Patterns](#communication-patterns)

---

## System Context Diagram (Level 1)

The System Context diagram shows Med-Connect and how it fits in the world - who uses it and what systems it integrates with.

```
┌─────────────────────────────────────────────────────────────────────────┐
│                         SYSTEM CONTEXT                                  │
└─────────────────────────────────────────────────────────────────────────┘

                    [Patient]                    [Doctor]
                        │                            │
                        │                            │
                        └──────────┬─────────────────┘
                                   │
                                   │ Uses for medical
                                   │ consultations
                                   │
                    ┌──────────────▼────────────────┐
                    │                               │
                    │      Med-Connect Platform     │
                    │                               │
                    │  Enterprise Telemedicine      │
                    │  Microservices Platform       │
                    │                               │
                    │  - Online Appointments        │
                    │  - Video Consultations        │
                    │  - AI Medical Assistant       │
                    │  - Prescription Management    │
                    │  - Secure File Storage        │
                    │                               │
                    └───┬───────┬────────┬──────┬───┘
                        │       │        │      │
                        │       │        │      │
        ┌───────────────┘       │        │      └─────────────────┐
        │                       │        │                        │
        │                       │        │                        │
        │                       │        │                        │
┌───────▼────────┐    ┌────────▼────┐   │            ┌───────────▼─────────┐
│                │    │             │   │            │                     │
│  Cloudinary    │    │  Brevo      │   │            │   VNPay Payment     │
│  (File CDN)    │    │  (Email)    │   │            │   Gateway           │
│                │    │             │   │            │                     │
│  Stores and    │    │  Sends      │   │            │  Processes          │
│  delivers      │    │  transact.  │   │            │  appointment        │
│  medical       │    │  emails     │   │            │  payments           │
│  documents     │    │             │   │            │                     │
│                │    │             │   │            │                     │
└────────────────┘    └─────────────┘   │            └─────────────────────┘
                                        │
                                        │
                                ┌───────▼─────────┐
                                │                 │
                                │  Ollama         │
                                │  (AI Service)   │
                                │                 │
                                │  Provides LLM   │
                                │  for medical    │
                                │  consultations  │
                                │                 │
                                └─────────────────┘

[Legend]
Person: Rectangle with person name
External System: Rectangle with system name
Med-Connect: Center box with detailed description
Relationships: Lines showing data flow
```

### System Users

**Patient**
- Books medical appointments online
- Manages personal health records and prescriptions
- Participates in video consultations
- Consults AI medical assistant
- Receives notifications about appointments

**Doctor**
- Manages professional profile and availability
- Reviews patient appointments
- Conducts video consultations
- Accesses patient prescriptions (with permission)
- Provides medical services and consultations

### External Systems

**Cloudinary (File Storage CDN)**
- Stores medical documents, prescriptions, and user avatars
- Provides secure file delivery
- Handles image transformations and optimizations

**Brevo (Email Service)**
- Sends transactional emails for appointments
- Delivers prescription access notifications
- Sends video call reminders

**VNPay Payment Gateway**
- Processes appointment payments
- Handles payment callbacks
- Manages payment status tracking

**Ollama (AI Service)**
- Provides local Large Language Model (LLM)
- Powers RAG-based medical consultation chatbot
- Processes medical knowledge queries

---

## Container Diagram (Level 2)

The Container diagram shows the high-level shape of the architecture and how responsibilities are distributed across containers (microservices). Following C4 Stage 2 principles, each microservice is shown as paired containers (API + Database).

```
┌──────────────────────────────────────────────────────────────────────────────────────────┐
│                          MED-CONNECT CONTAINER DIAGRAM                                   │
└──────────────────────────────────────────────────────────────────────────────────────────┘

                         [Patient]              [Doctor]
                             │                      │
                             │                      │
                             └──────────┬───────────┘
                                        │
                                        │ HTTPS
                                        │
                    ┌───────────────────▼────────────────────┐
                    │    Frontend Application                │
                    │    [React SPA]                         │
                    │                                        │
                    │  - User Interface                      │
                    │  - WebSocket for real-time updates     │
                    └───────────────┬────────────────────────┘
                                    │ HTTPS/REST + WebSocket
                                    │
                    ┌───────────────▼────────────────────────┐
                    │    API Gateway (8888)                  │
                    │    [Spring Cloud Gateway]              │
                    │                                        │
                    │  - Request routing                     │
                    │  - Rate limiting                       │
                    │  - Authentication relay                │
                    └┬──┬───┬───┬──┬──┬──┬──┬──┬────────────┘
                     │  │   │   │  │  │  │  │  │
        ┌────────────┘  │   │   │  │  │  │  │  └──────────┐
        │               │   │   │  │  │  │  │             │
        │    ┌──────────┘   │   │  │  │  │  └──────┐      │
        │    │              │   │  │  │  │         │      │
┌───────▼────▼───┐  ┌───────▼───▼──▼──▼──▼───┐     │      │
│                │  │                         │     │      │
│ ┌────────────┐ │  │  ┌────────────────┐    │     │      │
│ │  Auth      │ │  │  │  Profile       │    │     │      │
│ │  Service   │─┼──┼─▶│  Service       │    │     │      │
│ │  (8080)    │ │  │  │  (8081)        │    │     │      │
│ └─────┬──────┘ │  │  └────────┬───────┘    │     │      │
│       │        │  │           │            │     │      │
│ ┌─────▼──────┐ │  │  ┌────────▼─────────┐  │     │      │
│ │PostgreSQL  │ │  │  │ Neo4j + MongoDB  │  │     │      │
│ │            │ │  │  │ + Redis          │  │     │      │
│ └────────────┘ │  │  └──────────────────┘  │     │      │
│                │  │                         │     │      │
│ User Auth      │  │  User Profiles &        │     │      │
│ & Roles        │  │  Medical Records        │     │      │
└────────────────┘  └─────────────────────────┘     │      │
                                                    │      │
┌───────────────────────────────────────┐   ┌───────▼──────▼───┐
│                                       │   │                  │
│  ┌─────────────────────┐              │   │ ┌──────────────┐ │
│  │  Appointment        │              │   │ │  Search      │ │
│  │  Service            │              │   │ │  Service     │ │
│  │  (8084)             │              │   │ │  (8087)      │ │
│  └──────┬──────────────┘              │   │ └───────┬──────┘ │
│         │                             │   │         │        │
│  ┌──────▼──────────┐                  │   │ ┌───────▼──────┐ │
│  │  PostgreSQL     │                  │   │ │Elasticsearch │ │
│  │                 │                  │   │ │              │ │
│  └─────────────────┘                  │   │ └──────────────┘ │
│                                       │   │                  │
│  Appointment Booking & Management     │   │  Doctor Search   │
└───────────────────────────────────────┘   └──────────────────┘

┌────────────────┐  ┌────────────────┐  ┌──────────────────────┐
│                │  │                │  │                      │
│ ┌────────────┐ │  │ ┌────────────┐ │  │ ┌──────────────────┐ │
│ │Notification│ │  │ │   File     │ │  │ │   RAG Chat Bot   │ │
│ │Service     │ │  │ │  Service   │ │  │ │   (8082)         │ │
│ │(8085)      │ │  │ │  (8086)    │ │  │ └──────┬───────────┘ │
│ └─────┬──────┘ │  │ └─────┬──────┘ │  │        │             │
│       │        │  │       │        │  │ ┌──────▼───────────┐ │
│ ┌─────▼──────┐ │  │ ┌─────▼──────┐ │  │ │PostgreSQL+Qdrant │ │
│ │  MongoDB   │ │  │ │  MongoDB   │ │  │ │+ Redis           │ │
│ │            │ │  │ │            │ │  │ └──────────────────┘ │
│ └────────────┘ │  │ └────────────┘ │  │                      │
│                │  │                │  │  AI Medical          │
│ Real-time      │  │ Document       │  │  Consultation        │
│ Notifications  │  │ Management     │  │                      │
└────────────────┘  └────────────────┘  └──────────────────────┘

┌─────────────────┐  ┌──────────────────────────────────────┐
│                 │  │                                      │
│ ┌─────────────┐ │  │ ┌──────────────────────────────────┐ │
│ │  VNPay      │ │  │ │  Video Call Service              │ │
│ │  Service    │ │  │ │  (8095)                          │ │
│ │  (8083)     │ │  │ └──────────┬───────────────────────┘ │
│ └──────┬──────┘ │  │            │                         │
│        │        │  │  ┌─────────▼─────────────────────┐   │
│ ┌──────▼──────┐ │  │  │  PostgreSQL                   │   │
│ │ PostgreSQL  │ │  │  │                               │   │
│ │             │ │  │  └───────────────────────────────┘   │
│ └─────────────┘ │  │                                      │
│                 │  │  WebRTC Video Consultations          │
│ Payment         │  │                                      │
│ Processing      │  │                                      │
└─────────────────┘  └──────────────────────────────────────┘

════════════════════════════════════════════════════════════════
                    INFRASTRUCTURE LAYER
════════════════════════════════════════════════════════════════

┌─────────────────────────────────────────────────────────────┐
│                                                             │
│   ┌──────────────┐     ┌──────────────┐                    │
│   │  Apache      │     │  Redis       │                    │
│   │  Kafka       │     │  Cache       │                    │
│   │  (9094)      │     │  (6379)      │                    │
│   │              │     │              │                    │
│   │  Event Bus   │     │  Distributed │                    │
│   │  Messaging   │     │  Cache       │                    │
│   └──────────────┘     └──────────────┘                    │
│                                                             │
└─────────────────────────────────────────────────────────────┘

════════════════════════════════════════════════════════════════
                    EXTERNAL SYSTEMS
════════════════════════════════════════════════════════════════

┌──────────────┐  ┌──────────────┐  ┌──────────────┐  ┌──────────────┐
│  Cloudinary  │  │    Brevo     │  │    VNPay     │  │   Ollama     │
│  (File CDN)  │  │   (Email)    │  │  (Payment)   │  │  (AI/LLM)    │
└──────────────┘  └──────────────┘  └──────────────┘  └──────────────┘


[Legend]
┌─────────┐
│Service  │  = Microservice Container (Hexagon in formal C4)
└─────────┘

┌─────────┐
│Database │  = Data Store Container (Cylinder in formal C4)
└─────────┘

Lines = Communication/Data Flow
Grouped boxes = Paired containers (API + Database)
```

---

## Component Details

### Microservice Containers

#### 1. Auth Service (8080)
**Technology:** Spring Boot + PostgreSQL
**Responsibilities:**
- User authentication (login/logout)
- JWT token management (access/refresh tokens)
- Role-based authorization (PATIENT, DOCTOR, ADMIN)
- User registration and management

**Database:** PostgreSQL
- Tables: users, roles, permissions, tokens

**Integrations:**
- Publishes: `user-role-updated` events to Kafka
- Called by: All services for token validation

---

#### 2. Profile Service (8081)
**Technology:** Spring Boot + Neo4j + MongoDB + Redis
**Responsibilities:**
- User profile management (patients and doctors)
- Doctor specialties, services, and experience tracking
- Prescription management with access control
- Medical record storage

**Databases:**
- Neo4j: Doctor profiles and relationships (graph)
- MongoDB: User prescriptions
- Redis: Caching layer

**Integrations:**
- Calls: File Service (media upload), Notification Service
- Consumes: `user-role-updated` from Kafka
- Publishes: `cdc.profileservice.doctor_profile_relationships` to Kafka

---

#### 3. Appointment Service (8084)
**Technology:** Spring Boot + PostgreSQL
**Responsibilities:**
- Appointment scheduling and management
- Booking cancellations
- Patient and doctor appointment views
- Integration with payment and video call services

**Database:** PostgreSQL
- Tables: appointments

**Integrations:**
- Calls: Profile Service, VNPay Service, Video Call Service
- Publishes: `video-call-events` to Kafka

---

#### 4. Search Service (8087)
**Technology:** Spring Boot + Elasticsearch
**Responsibilities:**
- Full-text search on doctor profiles
- Search suggestions and autocomplete
- Real-time index synchronization via CDC
- Advanced filtering and sorting

**Database:** Elasticsearch
- Indexes: doctor_profiles, users, appointments

**Integrations:**
- Consumes: Multiple CDC topics from Kafka
  - `cdc.authservice.public.users`
  - `cdc.appointmentservice.public.appointments`
  - `cdc.profileservice.doctor_profile_relationships`

---

#### 5. Notification Service (8085)
**Technology:** Spring Boot + MongoDB + WebSocket
**Responsibilities:**
- Real-time in-app notifications (WebSocket)
- Email notifications via Brevo
- Notification history and read status
- Prescription and video call alerts

**Database:** MongoDB
- Collections: notifications

**Integrations:**
- Consumes: `notification-delivery` from Kafka
- Calls: Brevo Email API (external)

---

#### 6. File Service (8086)
**Technology:** Spring Boot + MongoDB + Cloudinary
**Responsibilities:**
- File upload/download management
- Medical document storage
- Integration with Cloudinary CDN
- Authorization-based file deletion

**Database:** MongoDB
- Collections: file_management

**Integrations:**
- Calls: Cloudinary API (external)

---

#### 7. RAG Chat Bot (8082)
**Technology:** Spring Boot + PostgreSQL + Qdrant + Redis + Ollama
**Responsibilities:**
- AI-powered medical consultation
- Retrieval Augmented Generation (RAG)
- Chat history management
- Medical knowledge queries

**Databases:**
- PostgreSQL: Chat sessions and history
- Qdrant: Vector database for embeddings
- Redis: Session caching

**Integrations:**
- Calls: Ollama API (external LLM)
- Calls: Profile Service (user context)

---

#### 8. Video Call Service (8095)
**Technology:** Spring Boot + PostgreSQL + WebSocket
**Responsibilities:**
- WebRTC video call session management
- Room creation and lifecycle
- Scheduled call sessions linked to appointments
- Call status tracking

**Database:** PostgreSQL
- Tables: session_video_calls

**Integrations:**
- Consumes: `video-call-events` from Kafka
- Calls: Notification Service

---

#### 9. VNPay Payment Service (8083)
**Technology:** Spring Boot + PostgreSQL + WebSocket
**Responsibilities:**
- Payment processing via VNPay gateway
- Payment callback handling
- Payment status tracking
- Real-time payment updates via Socket.IO

**Database:** PostgreSQL
- Tables: payments

**Integrations:**
- Calls: VNPay Payment Gateway (external)
- Calls: Profile Service (user info)

---

#### 10. API Gateway (8888)
**Technology:** Spring Cloud Gateway + Redis
**Responsibilities:**
- Request routing to all microservices
- Rate limiting and throttling
- Request/response filtering
- Authentication relay

**Database:** Redis
- Rate limiting counters

**Route Configuration:**
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

## Communication Patterns

### Synchronous Communication (Feign/REST)

```
Profile Service ──────▶ File Service (media upload/delete)
                │
                └─────▶ Notification Service (send notifications)
                │
                └─────▶ Auth Service (user verification)

Appointment Service ──▶ Profile Service (doctor/patient info)
                    │
                    └─▶ VNPay Service (payment processing)

Video Call Service ───▶ Notification Service (call notifications)

RAG Chat Bot ─────────▶ Profile Service (user context)

VNPay Service ────────▶ Profile Service (payer info)
```

### Asynchronous Communication (Kafka Events)

```
┌─────────────────────────────────────────────────────────────┐
│                    KAFKA EVENT BUS                          │
└─────────────────────────────────────────────────────────────┘

Topic: user-role-updated
├─ Producer: Auth Service
└─ Consumer: Profile Service
   Purpose: Sync user role changes, create doctor profiles

Topic: video-call-events
├─ Producer: Appointment Service
└─ Consumer: Video Call Service
   Purpose: Create video call sessions for appointments

Topic: cdc.profileservice.doctor_profile_relationships
├─ Producer: Profile Service
└─ Consumer: Search Service
   Purpose: Index doctor profile changes in Elasticsearch

Topic: cdc.authservice.public.users
├─ Producer: Auth Service (Debezium CDC)
└─ Consumer: Search Service
   Purpose: Index user changes in Elasticsearch

Topic: cdc.appointmentservice.public.appointments
├─ Producer: Appointment Service (Debezium CDC)
└─ Consumer: Search Service
   Purpose: Index appointment changes in Elasticsearch

Topic: notification-delivery
├─ Producers: Multiple services
└─ Consumer: Notification Service
   Purpose: Deliver notifications via email (Brevo)
```

### Data Flow Example: Appointment Booking

```
1. Patient ──[HTTPS]──▶ Frontend (React SPA)
                            │
2. Frontend ─[POST /api/v1/appointment/create]─▶ API Gateway (8888)
                                                      │
3. API Gateway ──[Route]──▶ Appointment Service (8084)
                                  │
                                  ├─[Feign]─▶ Profile Service
                                  │           (validate doctor/patient)
                                  │
                                  ├─[Feign]─▶ VNPay Service
                                  │           (process payment)
                                  │
                                  └─[Kafka: video-call-events]─▶ Kafka
                                                                   │
4. Video Call Service ◀──[Subscribe]───────────────────────────────┘
        │
        └─[Create session 5 min before appointment]
                │
5. Video Call Service ──[Feign]──▶ Notification Service
                                          │
                                          ├─[WebSocket]─▶ Patient
                                          └─[Email via Brevo]─▶ Patient & Doctor
```

---

## Technology Stack Summary

### Languages & Frameworks
- **Java 17** with **Spring Boot 3.5.x**
- **Spring Cloud Gateway** for API Gateway
- **Spring Security** with OAuth2/JWT
- **Spring Data JPA**, **Spring Data MongoDB**, **Spring Data Neo4j**, **Spring Data Redis**
- **Spring AI** for LLM integration
- **MapStruct** for DTO mapping

### Databases
- **PostgreSQL** - Relational data (Auth, Appointments, Payments, Chat, Video Calls)
- **MongoDB** - Document storage (Files, Notifications, Prescriptions)
- **Neo4j** - Graph database (Doctor profiles and relationships)
- **Redis** - Caching and rate limiting
- **Elasticsearch** - Full-text search
- **Qdrant** - Vector database for RAG

### Messaging & Events
- **Apache Kafka** - Event-driven architecture and CDC
- **Debezium** - Change Data Capture
- **Socket.IO** - Real-time WebSocket communication

### External APIs
- **Cloudinary** - File storage and CDN
- **Brevo** - Transactional email service
- **VNPay** - Payment gateway
- **Ollama** - Local LLM for AI chat

### Build & Deployment
- **Maven** - Build tool
- **Docker & Docker Compose** - Containerization

---

## Scalability Considerations

### Horizontal Scaling
Each microservice can be independently scaled based on load:
- **Auth Service**: High read, moderate write (session validation)
- **Search Service**: High read (user searches)
- **Appointment Service**: Moderate read/write (booking patterns)
- **Notification Service**: High write during peak hours
- **Video Call Service**: Burst scaling during consultation hours

### Data Partitioning
- **PostgreSQL**: Sharding by user ID for appointments and auth
- **MongoDB**: Sharded collections for files and notifications
- **Neo4j**: Graph partitioning for large doctor networks
- **Elasticsearch**: Index sharding for search performance

### Caching Strategy
- **Redis** used for:
  - API Gateway rate limiting
  - Profile Service caching (hot doctor profiles)
  - RAG Chat Bot session state
  - JWT token blacklisting

### Asynchronous Processing
- **Kafka** enables:
  - Decoupled service communication
  - Retry mechanisms with exponential backoff
  - Dead Letter Topics (DLT) for failed events
  - Event sourcing and audit trails

---

## Security Architecture

### Authentication Flow
```
1. User ──[POST /auth/login]──▶ Auth Service
                                     │
2. Auth Service ──[Validate credentials]──▶ PostgreSQL
                │
3. Generate JWT Access Token (15 min) + Refresh Token (7 days)
                │
4. Return tokens ──▶ User
                         │
5. Subsequent requests include: Authorization: Bearer <access_token>
                         │
6. API Gateway/Services ──[Validate JWT]──▶ Auth Service (introspect)
```

### Authorization
- **Role-Based Access Control (RBAC)**
  - Roles: PATIENT, DOCTOR, ADMIN
  - Permissions mapped per role
- **Resource-level authorization** in services
  - Doctors can only view own appointments
  - Patients control prescription access

### Data Security
- **Encryption at rest**: Database-level encryption
- **Encryption in transit**: HTTPS/TLS for all communications
- **Secrets management**: Environment variables (`.env` files)
- **Token security**: JWT with signing keys, refresh token rotation

---

## Monitoring & Observability

### Health Checks
Each service exposes Spring Boot Actuator endpoints:
- `/actuator/health` - Service health status
- `/actuator/metrics` - Prometheus metrics

### Distributed Tracing
- Recommended: Spring Cloud Sleuth + Zipkin
- Trace requests across microservices
- Identify performance bottlenecks

### Logging
- Centralized logging (recommended: ELK Stack)
- Structured JSON logs
- Correlation IDs across service boundaries

---

## Deployment Architecture

### Docker Compose Setup
```yaml
services:
  # Databases
  - postgres (5432)
  - mongodb (27017)
  - neo4j (7687, 7474)
  - redis (6379)
  - elasticsearch (9200)
  - qdrant (6334)

  # Infrastructure
  - kafka (9092, 9093, 9094)
  - zookeeper (2181)

  # Microservices
  - api-gateway (8888)
  - auth-service (8080)
  - profile-service (8081)
  - rag-chat-bot (8082)
  - vnpay-service (8083)
  - appointment-service (8084)
  - notification-service (8085)
  - file-service (8086)
  - search-service (8087)
  - video-call-service (8095)

  # External (local development)
  - ollama (11434)
```

### Service Startup Order
1. **Infrastructure**: PostgreSQL, MongoDB, Neo4j, Redis, Kafka, Elasticsearch
2. **Auth Service** (other services depend on it for JWT validation)
3. **Profile Service, File Service**
4. **Appointment Service, Search Service, Notification Service**
5. **Video Call Service, VNPay Service, RAG Chat Bot**
6. **API Gateway** (last, routes to all services)

---

## Future Enhancements

### Potential Additions
- **Service Mesh** (Istio/Linkerd) for advanced traffic management
- **API Documentation** (Swagger/OpenAPI) centralized at gateway
- **Circuit Breaker** (Resilience4j) for fault tolerance
- **Distributed Tracing** (Jaeger/Zipkin)
- **Centralized Configuration** (Spring Cloud Config)
- **Blue-Green Deployments** for zero-downtime updates
- **Auto-scaling** based on metrics (Kubernetes HPA)

---

## References

- **C4 Model**: https://c4model.com/
- **Microservices Stage 2**: https://c4model.com/abstractions/microservices
- **Spring Boot**: https://spring.io/projects/spring-boot
- **Spring Cloud Gateway**: https://spring.io/projects/spring-cloud-gateway
- **Apache Kafka**: https://kafka.apache.org/

---

**Document Version:** 1.0
**Last Updated:** 2025-11-15
**Maintained By:** Med-Connect Engineering Team
