# Med-Connect C4 Model - PlantUML Diagrams

This document contains PlantUML code for generating formal C4 model diagrams for the Med-Connect telemedicine platform, following the C4 model Stage 2 principles for microservices architecture.

## How to Use

You can render these diagrams using:
1. **PlantUML Online Editor**: https://www.plantuml.com/plantuml/uml/
2. **VS Code Extension**: PlantUML extension
3. **IntelliJ IDEA**: Built-in PlantUML support
4. **Command Line**: `plantuml diagram.puml`

---

## 1. System Context Diagram (Level 1)

```plantuml
@startuml C4_Context
!include https://raw.githubusercontent.com/plantuml-stdlib/C4-PlantUML/master/C4_Context.puml

LAYOUT_WITH_LEGEND()

title System Context Diagram for Med-Connect Telemedicine Platform

Person(patient, "Patient", "A patient seeking medical consultation and care")
Person(doctor, "Doctor", "A healthcare professional providing medical services")

System(medconnect, "Med-Connect Platform", "Enterprise telemedicine microservices platform providing online appointments, video consultations, AI medical assistant, prescription management, and secure file storage")

System_Ext(cloudinary, "Cloudinary", "Cloud-based file storage and CDN for medical documents and images")
System_Ext(brevo, "Brevo Email Service", "Transactional email service for appointment and notification emails")
System_Ext(vnpay, "VNPay Payment Gateway", "Payment processing gateway for appointment fees")
System_Ext(ollama, "Ollama AI Service", "Local Large Language Model (LLM) service for AI-powered medical consultations")

Rel(patient, medconnect, "Books appointments, manages health records, consults AI assistant", "HTTPS")
Rel(doctor, medconnect, "Manages profile, reviews appointments, conducts consultations", "HTTPS")

Rel(medconnect, cloudinary, "Uploads/retrieves medical documents", "HTTPS/REST")
Rel(medconnect, brevo, "Sends transactional emails", "HTTPS/REST")
Rel(medconnect, vnpay, "Processes payments", "HTTPS/REST")
Rel(medconnect, ollama, "Queries LLM for medical information", "HTTP/REST")

@enduml
```

---

## 2. Container Diagram (Level 2) - Full Architecture

```plantuml
@startuml C4_Container
!include https://raw.githubusercontent.com/plantuml-stdlib/C4-PlantUML/master/C4_Container.puml

LAYOUT_TOP_DOWN()

title Container Diagram for Med-Connect Platform - Microservices Architecture

Person(patient, "Patient", "Medical service seeker")
Person(doctor, "Doctor", "Healthcare provider")

System_Boundary(medconnect, "Med-Connect Platform") {
    Container(web, "Web Application", "React SPA", "Provides user interface for patients and doctors via browser")
    Container(gateway, "API Gateway", "Spring Cloud Gateway", "Routes requests, rate limiting, authentication relay")

    Container(auth, "Auth Service", "Spring Boot", "User authentication, JWT token management, role-based authorization")
    ContainerDb(authdb, "Auth Database", "PostgreSQL", "Stores users, roles, permissions, tokens")

    Container(profile, "Profile Service", "Spring Boot", "User profiles, doctor info, prescription management")
    ContainerDb(profiledb1, "Profile Graph DB", "Neo4j", "Doctor profiles and relationships")
    ContainerDb(profiledb2, "Prescription DB", "MongoDB", "User prescriptions")
    ContainerDb(profilecache, "Profile Cache", "Redis", "Profile caching")

    Container(appointment, "Appointment Service", "Spring Boot", "Appointment scheduling and management")
    ContainerDb(appointmentdb, "Appointment DB", "PostgreSQL", "Appointment records")

    Container(search, "Search Service", "Spring Boot + Elasticsearch", "Full-text search on doctors")
    ContainerDb(searchdb, "Search Index", "Elasticsearch", "Doctor profile index")

    Container(notification, "Notification Service", "Spring Boot + WebSocket", "Real-time notifications and emails")
    ContainerDb(notificationdb, "Notification DB", "MongoDB", "Notification history")

    Container(file, "File Service", "Spring Boot", "Medical document management")
    ContainerDb(filedb, "File Metadata DB", "MongoDB", "File metadata")

    Container(chatbot, "RAG Chat Bot", "Spring Boot + Spring AI", "AI medical consultation")
    ContainerDb(chatdb, "Chat History DB", "PostgreSQL", "Chat sessions")
    ContainerDb(vectordb, "Vector DB", "Qdrant", "Document embeddings for RAG")
    ContainerDb(chatcache, "Chat Cache", "Redis", "Session state")

    Container(video, "Video Call Service", "Spring Boot + WebRTC", "Video consultation sessions")
    ContainerDb(videodb, "Video Session DB", "PostgreSQL", "Call session records")

    Container(payment, "VNPay Payment Service", "Spring Boot", "Payment processing")
    ContainerDb(paymentdb, "Payment DB", "PostgreSQL", "Payment transactions")

    Container(kafka, "Message Bus", "Apache Kafka", "Event-driven messaging")
    ContainerDb(redis, "Distributed Cache", "Redis", "API rate limiting")
}

System_Ext(cloudinary, "Cloudinary", "File CDN")
System_Ext(brevo, "Brevo", "Email service")
System_Ext(vnpayext, "VNPay Gateway", "Payment gateway")
System_Ext(ollama, "Ollama", "LLM service")

' User interactions
Rel(patient, web, "Uses", "HTTPS")
Rel(doctor, web, "Uses", "HTTPS")
Rel(web, gateway, "Makes API calls", "HTTPS/REST + WebSocket")

' Gateway routing
Rel(gateway, auth, "Routes /identity/**", "HTTP/REST")
Rel(gateway, profile, "Routes /profile/**", "HTTP/REST")
Rel(gateway, appointment, "Routes /appointment/**", "HTTP/REST")
Rel(gateway, search, "Routes /search/**", "HTTP/REST")
Rel(gateway, notification, "Routes /notifications/**", "HTTP/REST + WebSocket")
Rel(gateway, file, "Routes /file/**", "HTTP/REST")
Rel(gateway, chatbot, "Routes /chatbot/**", "HTTP/REST")
Rel(gateway, payment, "Routes /payment/**", "HTTP/REST")
Rel(gateway, redis, "Rate limiting", "Redis protocol")

' Service to database
Rel(auth, authdb, "Reads/Writes", "JDBC")
Rel(profile, profiledb1, "Reads/Writes", "Bolt")
Rel(profile, profiledb2, "Reads/Writes", "MongoDB protocol")
Rel(profile, profilecache, "Caches", "Redis protocol")
Rel(appointment, appointmentdb, "Reads/Writes", "JDBC")
Rel(search, searchdb, "Indexes/Queries", "HTTP/REST")
Rel(notification, notificationdb, "Reads/Writes", "MongoDB protocol")
Rel(file, filedb, "Reads/Writes", "MongoDB protocol")
Rel(chatbot, chatdb, "Reads/Writes", "JDBC")
Rel(chatbot, vectordb, "Similarity search", "HTTP/gRPC")
Rel(chatbot, chatcache, "Session state", "Redis protocol")
Rel(video, videodb, "Reads/Writes", "JDBC")
Rel(payment, paymentdb, "Reads/Writes", "JDBC")

' Inter-service communication (Feign/REST)
Rel(profile, auth, "Verifies users", "HTTP/REST")
Rel(profile, file, "Uploads/deletes files", "HTTP/REST")
Rel(profile, notification, "Sends notifications", "HTTP/REST")
Rel(appointment, profile, "Gets doctor/patient info", "HTTP/REST")
Rel(appointment, payment, "Processes payments", "HTTP/REST")
Rel(video, notification, "Sends call notifications", "HTTP/REST")
Rel(chatbot, profile, "Gets user context", "HTTP/REST")
Rel(payment, profile, "Gets payer info", "HTTP/REST")

' Kafka events
Rel(auth, kafka, "Publishes user-role-updated", "Kafka protocol")
Rel(kafka, profile, "Consumes user-role-updated", "Kafka protocol")
Rel(profile, kafka, "Publishes doctor-profile CDC", "Kafka protocol")
Rel(kafka, search, "Consumes CDC events", "Kafka protocol")
Rel(appointment, kafka, "Publishes video-call-events", "Kafka protocol")
Rel(kafka, video, "Consumes video-call-events", "Kafka protocol")
Rel(kafka, notification, "Delivers notifications", "Kafka protocol")

' External systems
Rel(file, cloudinary, "Stores/retrieves files", "HTTPS/REST")
Rel(notification, brevo, "Sends emails", "HTTPS/REST")
Rel(payment, vnpayext, "Processes payments", "HTTPS/REST")
Rel(chatbot, ollama, "Queries LLM", "HTTP/REST")

@enduml
```

---

## 3. Container Diagram (Level 2) - Simplified View

```plantuml
@startuml C4_Container_Simplified
!include https://raw.githubusercontent.com/plantuml-stdlib/C4-PlantUML/master/C4_Container.puml

LAYOUT_WITH_LEGEND()

title Container Diagram - Simplified Microservices View

Person(user, "User", "Patient or Doctor")

System_Boundary(medconnect, "Med-Connect Platform") {
    Container(web, "Frontend", "React SPA", "User interface")
    Container(gateway, "API Gateway", "Spring Cloud Gateway (8888)", "Request routing, rate limiting")

    ' Core services
    Container(auth, "Auth Service", "Spring Boot (8080) + PostgreSQL", "Authentication & authorization")
    Container(profile, "Profile Service", "Spring Boot (8081) + Neo4j/MongoDB/Redis", "User profiles & medical records")
    Container(appointment, "Appointment Service", "Spring Boot (8084) + PostgreSQL", "Appointment management")
    Container(search, "Search Service", "Spring Boot (8087) + Elasticsearch", "Doctor search")

    ' Supporting services
    Container(notification, "Notification Service", "Spring Boot (8085) + MongoDB", "Real-time notifications")
    Container(file, "File Service", "Spring Boot (8086) + MongoDB", "Document management")
    Container(chatbot, "RAG Chat Bot", "Spring Boot (8082) + PostgreSQL/Qdrant", "AI consultation")
    Container(video, "Video Call Service", "Spring Boot (8095) + PostgreSQL", "Video consultations")
    Container(payment, "VNPay Service", "Spring Boot (8083) + PostgreSQL", "Payment processing")

    ' Infrastructure
    Container(kafka, "Event Bus", "Apache Kafka", "Async messaging")
}

System_Ext(external, "External Systems", "Cloudinary, Brevo, VNPay, Ollama")

Rel(user, web, "Uses")
Rel(web, gateway, "API calls")
Rel(gateway, auth, "Routes")
Rel(gateway, profile, "Routes")
Rel(gateway, appointment, "Routes")
Rel(gateway, search, "Routes")
Rel(gateway, notification, "Routes")
Rel(gateway, file, "Routes")
Rel(gateway, chatbot, "Routes")
Rel(gateway, payment, "Routes")

Rel(profile, auth, "Verifies users")
Rel(profile, file, "Manages files")
Rel(profile, notification, "Sends alerts")
Rel(appointment, profile, "Gets info")
Rel(appointment, payment, "Processes payment")
Rel(appointment, kafka, "Publishes events")
Rel(kafka, video, "Video call events")
Rel(kafka, search, "CDC events")
Rel(kafka, notification, "Notification events")

Rel(file, external, "Cloudinary")
Rel(notification, external, "Brevo")
Rel(payment, external, "VNPay")
Rel(chatbot, external, "Ollama")

@enduml
```

---

## 4. Container Diagram - Core Services Focus

```plantuml
@startuml C4_Container_Core
!include https://raw.githubusercontent.com/plantuml-stdlib/C4-PlantUML/master/C4_Container.puml

title Container Diagram - Core Services (Auth, Profile, Appointment, Search)

Person(patient, "Patient")
Person(doctor, "Doctor")

System_Boundary(core, "Med-Connect Core Services") {
    Container(gateway, "API Gateway", "Spring Cloud Gateway", "Central entry point")

    ' Auth Service microservice
    Container(auth_api, "Auth Service API", "Spring Boot", "Handles authentication")
    ContainerDb(auth_db, "User Store", "PostgreSQL", "Users, roles, tokens")

    ' Profile Service microservice
    Container(profile_api, "Profile Service API", "Spring Boot", "Manages profiles")
    ContainerDb(profile_graph, "Profile Graph", "Neo4j", "Doctor relationships")
    ContainerDb(profile_doc, "Prescriptions", "MongoDB", "Medical records")
    ContainerDb(profile_cache, "Profile Cache", "Redis", "Hot data")

    ' Appointment Service microservice
    Container(appt_api, "Appointment Service API", "Spring Boot", "Booking logic")
    ContainerDb(appt_db, "Appointment Store", "PostgreSQL", "Appointments")

    ' Search Service microservice
    Container(search_api, "Search Service API", "Spring Boot", "Search logic")
    ContainerDb(search_index, "Search Index", "Elasticsearch", "Doctor index")

    Container(kafka, "Event Bus", "Apache Kafka", "Event streaming")
}

' User interactions
Rel(patient, gateway, "Books appointment, searches doctors")
Rel(doctor, gateway, "Manages profile, views appointments")

' Gateway routing
Rel(gateway, auth_api, "Authentication")
Rel(gateway, profile_api, "Profile management")
Rel(gateway, appt_api, "Appointment booking")
Rel(gateway, search_api, "Doctor search")

' Service-to-database
Rel(auth_api, auth_db, "CRUD operations")
Rel(profile_api, profile_graph, "Graph queries")
Rel(profile_api, profile_doc, "Document storage")
Rel(profile_api, profile_cache, "Cache reads/writes")
Rel(appt_api, appt_db, "CRUD operations")
Rel(search_api, search_index, "Full-text search")

' Inter-service sync calls
Rel(profile_api, auth_api, "Verify user", "Feign")
Rel(appt_api, profile_api, "Get doctor/patient", "Feign")

' Event-driven async
Rel(auth_api, kafka, "user-role-updated")
Rel(kafka, profile_api, "Consume events")
Rel(profile_api, kafka, "doctor-profile CDC")
Rel(appt_api, kafka, "appointment CDC")
Rel(kafka, search_api, "Consume CDC for indexing")

@enduml
```

---

## 5. Component Diagram - Profile Service (Level 3)

```plantuml
@startuml C4_Component_Profile
!include https://raw.githubusercontent.com/plantuml-stdlib/C4-PlantUML/master/C4_Component.puml

title Component Diagram - Profile Service

Container_Boundary(profile, "Profile Service") {
    Component(doctor_ctrl, "DoctorProfileController", "Spring MVC Controller", "REST API for doctor profiles")
    Component(user_ctrl, "UserProfileController", "Spring MVC Controller", "REST API for user profiles")
    Component(prescription_ctrl, "UserPrescriptionController", "Spring MVC Controller", "REST API for prescriptions")

    Component(doctor_svc, "DoctorProfileService", "Service", "Business logic for doctor profiles")
    Component(user_svc, "UserProfileService", "Service", "Business logic for user profiles")
    Component(prescription_svc, "UserPrescriptionService", "Service", "Business logic for prescriptions")
    Component(access_svc, "PrescriptionAccessService", "Service", "Access control for prescriptions")

    Component(doctor_repo, "DoctorProfileRepository", "Neo4j Repository", "Doctor data access")
    Component(user_repo, "UserProfileRepository", "Neo4j Repository", "User data access")
    Component(prescription_repo, "UserPrescriptionRepository", "MongoDB Repository", "Prescription data access")
    Component(specialty_repo, "SpecialtyRepository", "Neo4j Repository", "Specialty relations")

    Component(mapper, "MapStruct Mappers", "Mapper", "Entity-DTO conversion")
    Component(cdc_producer, "DoctorProfileCdcProducer", "Kafka Producer", "Publishes profile changes")
    Component(role_listener, "UserRoleUpdateListener", "Kafka Consumer", "Handles role changes")

    Component(file_client, "FileFeignClient", "Feign Client", "Calls File Service")
    Component(notif_client, "NotificationFeignClient", "Feign Client", "Calls Notification Service")
    Component(auth_client, "AuthServiceClient", "Feign Client", "Calls Auth Service")
}

ContainerDb(neo4j, "Neo4j", "Graph Database", "Doctor profiles")
ContainerDb(mongodb, "MongoDB", "Document Database", "Prescriptions")
ContainerDb(redis, "Redis", "Cache", "Profile cache")
Container(kafka, "Kafka", "Message Bus", "Events")
Container(file_svc, "File Service", "Microservice", "File management")
Container(notif_svc, "Notification Service", "Microservice", "Notifications")
Container(auth_svc, "Auth Service", "Microservice", "Authentication")

' Controller to Service
Rel(doctor_ctrl, doctor_svc, "Uses")
Rel(user_ctrl, user_svc, "Uses")
Rel(prescription_ctrl, prescription_svc, "Uses")
Rel(prescription_ctrl, access_svc, "Uses")

' Service to Repository
Rel(doctor_svc, doctor_repo, "Uses")
Rel(doctor_svc, specialty_repo, "Uses")
Rel(user_svc, user_repo, "Uses")
Rel(prescription_svc, prescription_repo, "Uses")
Rel(access_svc, prescription_repo, "Uses")

' Service to external
Rel(doctor_svc, cdc_producer, "Publishes changes")
Rel(doctor_svc, file_client, "Upload/delete media")
Rel(prescription_svc, notif_client, "Send notifications")
Rel(user_svc, auth_client, "Verify users")

' Repository to DB
Rel(doctor_repo, neo4j, "Reads/Writes")
Rel(user_repo, neo4j, "Reads/Writes")
Rel(specialty_repo, neo4j, "Reads/Writes")
Rel(prescription_repo, mongodb, "Reads/Writes")

' Kafka
Rel(cdc_producer, kafka, "Publishes")
Rel(kafka, role_listener, "Consumes")
Rel(role_listener, doctor_svc, "Creates/updates profile")

' External services
Rel(file_client, file_svc, "HTTP/REST")
Rel(notif_client, notif_svc, "HTTP/REST")
Rel(auth_client, auth_svc, "HTTP/REST")

' Mappers
Rel(doctor_svc, mapper, "Uses")
Rel(user_svc, mapper, "Uses")
Rel(prescription_svc, mapper, "Uses")

@enduml
```

---

## 6. Deployment Diagram - Docker Compose

```plantuml
@startuml Deployment
!include https://raw.githubusercontent.com/plantuml-stdlib/C4-PlantUML/master/C4_Deployment.puml

title Deployment Diagram - Docker Compose Environment

Deployment_Node(docker, "Docker Host", "Docker Engine") {
    Deployment_Node(network, "med-connect-network", "Docker Network") {

        Deployment_Node(gateway_container, "api-gateway-container", "Docker Container") {
            Container(gateway, "API Gateway", "Spring Cloud Gateway", "Port 8888")
        }

        Deployment_Node(auth_container, "auth-service-container", "Docker Container") {
            Container(auth, "Auth Service", "Spring Boot", "Port 8080")
        }

        Deployment_Node(profile_container, "profile-service-container", "Docker Container") {
            Container(profile, "Profile Service", "Spring Boot", "Port 8081")
        }

        Deployment_Node(appointment_container, "appointment-service-container", "Docker Container") {
            Container(appointment, "Appointment Service", "Spring Boot", "Port 8084")
        }

        Deployment_Node(search_container, "search-service-container", "Docker Container") {
            Container(search, "Search Service", "Spring Boot", "Port 8087")
        }

        Deployment_Node(notification_container, "notification-service-container", "Docker Container") {
            Container(notification, "Notification Service", "Spring Boot", "Port 8085")
        }

        Deployment_Node(file_container, "file-service-container", "Docker Container") {
            Container(file, "File Service", "Spring Boot", "Port 8086")
        }

        Deployment_Node(chatbot_container, "rag-chatbot-container", "Docker Container") {
            Container(chatbot, "RAG Chat Bot", "Spring Boot", "Port 8082")
        }

        Deployment_Node(video_container, "video-call-container", "Docker Container") {
            Container(video, "Video Call Service", "Spring Boot", "Port 8095")
        }

        Deployment_Node(payment_container, "vnpay-service-container", "Docker Container") {
            Container(payment, "VNPay Service", "Spring Boot", "Port 8083")
        }

        ' Databases
        Deployment_Node(postgres_container, "postgres-container", "Docker Container") {
            ContainerDb(postgres, "PostgreSQL", "Database", "Port 5432")
        }

        Deployment_Node(mongo_container, "mongodb-container", "Docker Container") {
            ContainerDb(mongodb, "MongoDB", "Database", "Port 27017")
        }

        Deployment_Node(neo4j_container, "neo4j-container", "Docker Container") {
            ContainerDb(neo4j, "Neo4j", "Graph Database", "Port 7687, 7474")
        }

        Deployment_Node(redis_container, "redis-container", "Docker Container") {
            ContainerDb(redis, "Redis", "Cache", "Port 6379")
        }

        Deployment_Node(elastic_container, "elasticsearch-container", "Docker Container") {
            ContainerDb(elastic, "Elasticsearch", "Search Engine", "Port 9200")
        }

        Deployment_Node(qdrant_container, "qdrant-container", "Docker Container") {
            ContainerDb(qdrant, "Qdrant", "Vector DB", "Port 6334")
        }

        ' Infrastructure
        Deployment_Node(kafka_container, "kafka-container", "Docker Container") {
            Container(kafka, "Apache Kafka", "Message Broker", "Port 9092, 9093, 9094")
        }

        Deployment_Node(ollama_container, "ollama-container", "Docker Container") {
            Container(ollama, "Ollama", "LLM Service", "Port 11434")
        }
    }
}

' Relationships
Rel(gateway, auth, "Routes")
Rel(gateway, profile, "Routes")
Rel(gateway, appointment, "Routes")
Rel(gateway, search, "Routes")
Rel(gateway, notification, "Routes")
Rel(gateway, file, "Routes")
Rel(gateway, chatbot, "Routes")
Rel(gateway, payment, "Routes")

Rel(auth, postgres, "Connects")
Rel(profile, neo4j, "Connects")
Rel(profile, mongodb, "Connects")
Rel(profile, redis, "Connects")
Rel(appointment, postgres, "Connects")
Rel(search, elastic, "Connects")
Rel(notification, mongodb, "Connects")
Rel(file, mongodb, "Connects")
Rel(chatbot, postgres, "Connects")
Rel(chatbot, qdrant, "Connects")
Rel(chatbot, redis, "Connects")
Rel(video, postgres, "Connects")
Rel(payment, postgres, "Connects")

Rel(auth, kafka, "Publishes/Consumes")
Rel(profile, kafka, "Publishes/Consumes")
Rel(appointment, kafka, "Publishes")
Rel(search, kafka, "Consumes")
Rel(notification, kafka, "Consumes")
Rel(video, kafka, "Consumes")

Rel(chatbot, ollama, "Queries LLM")

@enduml
```

---

## 7. Event-Driven Architecture - Kafka Topics

```plantuml
@startuml Kafka_Events
!include https://raw.githubusercontent.com/plantuml-stdlib/C4-PlantUML/master/C4_Dynamic.puml

title Event-Driven Communication via Apache Kafka

Container(auth, "Auth Service", "Spring Boot")
Container(profile, "Profile Service", "Spring Boot")
Container(appointment, "Appointment Service", "Spring Boot")
Container(search, "Search Service", "Spring Boot")
Container(video, "Video Call Service", "Spring Boot")
Container(notification, "Notification Service", "Spring Boot")

queue user_role_topic "user-role-updated"
queue video_call_topic "video-call-events"
queue cdc_profile_topic "cdc.profileservice.doctor_profile_relationships"
queue cdc_user_topic "cdc.authservice.public.users"
queue cdc_appt_topic "cdc.appointmentservice.public.appointments"
queue notif_topic "notification-delivery"

' Publishers
Rel(auth, user_role_topic, "Publishes", "When user role changes")
Rel(profile, cdc_profile_topic, "Publishes", "Doctor profile CDC")
Rel(auth, cdc_user_topic, "Publishes", "User CDC (Debezium)")
Rel(appointment, cdc_appt_topic, "Publishes", "Appointment CDC")
Rel(appointment, video_call_topic, "Publishes", "Video session request")
Rel_U(profile, notif_topic, "Publishes", "Notification events")
Rel_U(video, notif_topic, "Publishes", "Video call notifications")

' Consumers
Rel(user_role_topic, profile, "Consumes", "Creates doctor profile")
Rel(video_call_topic, video, "Consumes", "Creates video session")
Rel(cdc_profile_topic, search, "Consumes", "Indexes doctor profile")
Rel(cdc_user_topic, search, "Consumes", "Indexes user")
Rel(cdc_appt_topic, search, "Consumes", "Indexes appointment")
Rel(notif_topic, notification, "Consumes", "Sends emails")

@enduml
```

---

## 8. Sequence Diagram - Appointment Booking Flow

```plantuml
@startuml Sequence_Appointment_Booking
!include https://raw.githubusercontent.com/plantuml-stdlib/C4-PlantUML/master/C4_Sequence.puml

title Sequence Diagram - Patient Books Appointment with Doctor

actor Patient
participant "Frontend" as FE
participant "API Gateway" as GW
participant "Appointment\nService" as APPT
participant "Profile\nService" as PROF
participant "VNPay\nService" as PAY
participant "Kafka" as KAFKA
participant "Video Call\nService" as VIDEO
participant "Notification\nService" as NOTIF

Patient -> FE: Select doctor & time slot
FE -> GW: POST /api/v1/appointment/create
activate GW
GW -> APPT: Route to Appointment Service
activate APPT

APPT -> PROF: GET /doctorProfile/{doctorId}
activate PROF
PROF --> APPT: Doctor details
deactivate PROF

APPT -> PROF: GET /users/{patientId}
activate PROF
PROF --> APPT: Patient details
deactivate PROF

APPT -> PAY: POST /payment/vn-pay
activate PAY
PAY --> APPT: Payment URL
deactivate PAY

APPT -> APPT: Save appointment to PostgreSQL

APPT -> KAFKA: Publish SessionVideoEvent
activate KAFKA
KAFKA --> APPT: Event acknowledged
deactivate KAFKA

APPT --> GW: AppointmentResponse + Payment URL
deactivate APPT
GW --> FE: Response
deactivate GW
FE --> Patient: Show payment page

Patient -> PAY: Complete payment (external)
PAY -> PAY: Process payment
PAY --> Patient: Payment success

KAFKA -> VIDEO: Consume video-call-events
activate VIDEO
VIDEO -> VIDEO: Schedule session\n(5 min before appointment)
VIDEO --> KAFKA: Acknowledged
deactivate VIDEO

VIDEO -> NOTIF: POST /send-video-call-notification
activate NOTIF
NOTIF -> NOTIF: Send email via Brevo
NOTIF -> NOTIF: Send WebSocket notification
NOTIF --> VIDEO: Notification sent
deactivate NOTIF

NOTIF --> Patient: Email & in-app notification
NOTIF --> FE: WebSocket update

@enduml
```

---

## 9. Sequence Diagram - Doctor Profile Update & Search Sync

```plantuml
@startuml Sequence_Profile_Update
!include https://raw.githubusercontent.com/plantuml-stdlib/C4-PlantUML/master/C4_Sequence.puml

title Sequence Diagram - Doctor Updates Profile (with Search Index Sync)

actor Doctor
participant "Frontend" as FE
participant "API Gateway" as GW
participant "Profile\nService" as PROF
participant "Neo4j" as NEO
participant "Kafka" as KAFKA
participant "Search\nService" as SEARCH
participant "Elasticsearch" as ES

Doctor -> FE: Update specialties & services
FE -> GW: PUT /api/v1/profile/doctorProfile/baseProfile
activate GW
GW -> PROF: Route to Profile Service
activate PROF

PROF -> NEO: Update doctor node\nand relationships
activate NEO
NEO --> PROF: Update successful
deactivate NEO

PROF -> KAFKA: Publish DoctorProfileCdcEvent\n(topic: cdc.profileservice.doctor_profile_relationships)
activate KAFKA
KAFKA --> PROF: Event published
deactivate KAFKA

PROF --> GW: Profile updated successfully
deactivate PROF
GW --> FE: Success response
deactivate GW
FE --> Doctor: Show confirmation

KAFKA -> SEARCH: Consume CDC event\n(async, with retry)
activate SEARCH
SEARCH -> SEARCH: Map event to DoctorProfile entity

SEARCH -> ES: Update doctor index\n(same ID)
activate ES
ES --> SEARCH: Index updated
deactivate ES

SEARCH --> KAFKA: Commit offset
deactivate SEARCH

note right of SEARCH
  Next time patient searches for this specialty,
  the updated doctor profile will appear in results
end note

@enduml
```

---

## 10. Microservices Color-Coded Grouping

```plantuml
@startuml Microservices_Grouping
!include https://raw.githubusercontent.com/plantuml-stdlib/C4-PlantUML/master/C4_Container.puml

title Med-Connect Microservices - Grouped by Domain

skinparam linetype ortho

' User Management Domain
rectangle "User Management" #LightBlue {
    [Auth Service\n(8080)]
    database "PostgreSQL" as AuthDB
    [Auth Service\n(8080)] --> AuthDB
}

' Profile & Medical Records Domain
rectangle "Profile & Medical Records" #LightGreen {
    [Profile Service\n(8081)]
    database "Neo4j" as ProfileNeo
    database "MongoDB" as ProfileMongo
    database "Redis" as ProfileRedis
    [Profile Service\n(8081)] --> ProfileNeo
    [Profile Service\n(8081)] --> ProfileMongo
    [Profile Service\n(8081)] --> ProfileRedis
}

' Appointment & Scheduling Domain
rectangle "Appointment & Scheduling" #LightYellow {
    [Appointment Service\n(8084)]
    database "PostgreSQL" as ApptDB
    [Appointment Service\n(8084)] --> ApptDB
}

' Search & Discovery Domain
rectangle "Search & Discovery" #LightCoral {
    [Search Service\n(8087)]
    database "Elasticsearch" as SearchES
    [Search Service\n(8087)] --> SearchES
}

' Communication Domain
rectangle "Communication" #LightPink {
    [Notification Service\n(8085)]
    database "MongoDB" as NotifDB
    [Notification Service\n(8085)] --> NotifDB

    [Video Call Service\n(8095)]
    database "PostgreSQL" as VideoDB
    [Video Call Service\n(8095)] --> VideoDB
}

' File & Document Domain
rectangle "File & Document" #Lavender {
    [File Service\n(8086)]
    database "MongoDB" as FileDB
    [File Service\n(8086)] --> FileDB
}

' AI & Intelligence Domain
rectangle "AI & Intelligence" #LightCyan {
    [RAG Chat Bot\n(8082)]
    database "PostgreSQL" as ChatDB
    database "Qdrant" as VectorDB
    database "Redis" as ChatRedis
    [RAG Chat Bot\n(8082)] --> ChatDB
    [RAG Chat Bot\n(8082)] --> VectorDB
    [RAG Chat Bot\n(8082)] --> ChatRedis
}

' Payment Domain
rectangle "Payment Processing" #LightGoldenRodYellow {
    [VNPay Service\n(8083)]
    database "PostgreSQL" as PayDB
    [VNPay Service\n(8083)] --> PayDB
}

' Infrastructure
rectangle "Infrastructure" #WhiteSmoke {
    [API Gateway\n(8888)]
    database "Redis" as GWRedis
    [Apache Kafka\n(9094)]
    [API Gateway\n(8888)] --> GWRedis
}

' Key relationships
[API Gateway\n(8888)] ..> [Auth Service\n(8080)]
[API Gateway\n(8888)] ..> [Profile Service\n(8081)]
[API Gateway\n(8888)] ..> [Appointment Service\n(8084)]
[API Gateway\n(8888)] ..> [Search Service\n(8087)]

[Profile Service\n(8081)] --> [Auth Service\n(8080)] : Verify user
[Appointment Service\n(8084)] --> [Profile Service\n(8081)] : Get info
[Appointment Service\n(8084)] --> [VNPay Service\n(8083)] : Payment

[Apache Kafka\n(9094)] <.. [Auth Service\n(8080)] : Events
[Apache Kafka\n(9094)] ..> [Profile Service\n(8081)]
[Apache Kafka\n(9094)] <.. [Appointment Service\n(8084)]
[Apache Kafka\n(9094)] ..> [Search Service\n(8087)]
[Apache Kafka\n(9094)] ..> [Video Call Service\n(8095)]

@enduml
```

---

## Usage Instructions

### Rendering the Diagrams

#### Option 1: PlantUML Online
1. Copy the desired PlantUML code
2. Visit: https://www.plantuml.com/plantuml/uml/
3. Paste the code
4. The diagram will render automatically

#### Option 2: VS Code
1. Install "PlantUML" extension by jebbs
2. Create a `.puml` file and paste the code
3. Press `Alt+D` to preview

#### Option 3: IntelliJ IDEA
1. PlantUML support is built-in
2. Create a `.puml` file and paste the code
3. Right-click → "Show PlantUML Diagram"

#### Option 4: Command Line
```bash
# Install PlantUML
brew install plantuml  # macOS
# or
apt-get install plantuml  # Ubuntu/Debian

# Generate diagram
plantuml diagram.puml

# Output: diagram.png
```

### Customization

You can customize colors, fonts, and layout by adding these directives:

```plantuml
' Change colors
skinparam BackgroundColor White
skinparam Shadowing false

' Change fonts
skinparam DefaultFontName Arial
skinparam DefaultFontSize 12

' Layout direction
LAYOUT_LEFT_RIGHT()
LAYOUT_TOP_DOWN()
```

---

## References

- **C4 Model PlantUML**: https://github.com/plantuml-stdlib/C4-PlantUML
- **PlantUML Official**: https://plantuml.com/
- **C4 Model**: https://c4model.com/
- **Med-Connect Documentation**: See `CLAUDE.md` and `PLANNING.md`

---

**Document Version:** 1.0
**Last Updated:** 2025-11-15
**Maintained By:** Med-Connect Engineering Team
