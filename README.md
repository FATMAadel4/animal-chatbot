# 🐾 Animal Chatbot

A Spring Boot AI-powered chatbot that answers questions about animals using **RAG (Retrieval-Augmented Generation)**, **Image Classification**, and **OpenAI GPT-4o-mini**.

---

## ✨ Features

- 🔍 **Image Analysis** — Upload an animal image and get a detailed description (gender, age, health condition)
- 📚 **RAG System** — Retrieves accurate animal information from a local knowledge base
- 🌐 **Bilingual Support** — Understands and responds in both **Arabic** and **English**
- 🤖 **Vision AI** — Uses GPT-4o-mini to identify and describe animals from images
- 💬 **Text Questions** — Ask about any animal by name in Arabic or English

---

## 🛠️ Tech Stack

| Technology | Purpose |
|------------|---------|
| Spring Boot 3 | Backend Framework |
| LangChain4j | AI/RAG Integration |
| OpenAI GPT-4o-mini | Chat + Vision Model |
| ChromaDB | Vector Embedding Store |
| Docker | Running ChromaDB |
| Java 17 | Programming Language |

---

## 📁 Project Structure

```
src/
├── main/
│   ├── java/com/example/chatbot/
│   │   ├── Config/
│   │   │   └── AiConfig.java          # OpenAI + ChromaDB configuration
│   │   ├── Controller/
│   │   │   └── ChatController.java    # REST API endpoints
│   │   └── Service/
│   │       ├── AnimalChatFacade.java  # Main orchestrator
│   │       ├── AnimalChatService.java # RAG logic
│   │       ├── VisionService.java     # Image analysis
│   │       └── TextAnimalService.java # Text extraction
│   └── resources/
│       └── data/
│           └── Animal_Data.txt        # Animal knowledge base
```

---

## ⚙️ Prerequisites

- Java 17+
- Maven
- Docker Desktop
- OpenAI API Key

---

## 🚀 Setup & Run

### 1. Clone the repository

```bash
git clone https://github.com/your-username/chatbot.git
cd chatbot
```

### 2. Create `.env` file in the root directory

```
OPENAI_API_KEY=sk-your-api-key-here
```

### 3. Add `.env` to `.gitignore`

```
.env
```

### 4. Set your API Key in `application.yml`

```yaml
server:
  port: 8080

spring:
  servlet:
    multipart:
      max-file-size: 10MB
      max-request-size: 10MB

openai:
  api-key: your-api-key-here
```

### 5. Start ChromaDB with Docker

```bash
docker run -d --restart always -p 8000:8000 --name chroma chromadb/chroma:0.4.24
```

### 6. Run the application

```bash
./mvnw spring-boot:run
```

---

## 📡 API Endpoints

### 🖼️ Image + Optional Question

```http
POST /api/chat/upload
Content-Type: multipart/form-data
```

| Parameter | Type | Required | Description |
|-----------|------|----------|-------------|
| image | File | ✅ | Animal image (jpg, png) |
| question | String | ❌ | Optional question about the animal |

**Example with curl:**
```bash
curl -X POST http://localhost:8080/api/chat/upload \
  -F "image=@lion.jpg" \
  -F "question=ماذا يأكل هذا الحيوان؟"
```

**Response:**
```json
{
  "answer": "🔍 تحليل الصورة:\nهذا أسد ذكر، عمره 5-7 سنوات...\n\nإجابة سؤالك:\nالأسد يتغذى على..."
}
```

---

### 💬 Text Question Only

```http
POST /api/chat/text
Content-Type: application/json
```

**Request Body:**
```json
{
  "question": "أخبرني عن الفيل"
}
```

**Response:**
```json
{
  "answer": "🐘 الفيل\nالموطن: يعيش الفيل في السافانا..."
}
```

---

## 🐾 Supported Animals

The knowledge base includes **30+ animals** such as:

🦁 Lion | 🐯 Tiger | 🐘 Elephant | 🦒 Giraffe | 🦓 Zebra | 🐒 Monkey | 🦍 Gorilla | 🐻 Bear | 🐼 Panda | 🦘 Kangaroo | 🐧 Penguin | 🐊 Crocodile | 🐍 Snake | 🦜 Parrot | 🦉 Owl | 🦅 Eagle | 🐺 Wolf | 🦊 Fox | 🦌 Deer | 🐫 Camel | and more...

---

## 🔄 How It Works

```
User Request
     │
     ▼
ChatController
     │
     ▼
AnimalChatFacade ──────────────────────────────────┐
     │                                             │
     ▼                                             ▼
[Image provided?]                          [Text only]
     │                                             │
     ▼                                             ▼
VisionService                           TextAnimalService
(GPT-4o-mini Vision)                  (Extract animal name)
     │                                             │
     ▼                                             ▼
AnimalChatService (RAG)        AnimalChatService (RAG)
(ChromaDB + Embeddings)        (ChromaDB + Embeddings)
     │                                             │
     └─────────────────┬───────────────────────────┘
                       │
                       ▼
                 Final Response
              (Arabic or English)
```

---

## 📝 Notes

- Make sure **Docker Desktop** is running before starting the app
- ChromaDB must be running on port **8000**
- The app supports images up to **10MB**
- Language detection is automatic based on your question

---

## 📄 License

This project is for educational purposes.
