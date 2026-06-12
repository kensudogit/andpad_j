# ANDPAD Java 版 (`andpad_j`)

Go + Next.js 版 [`andpad`](../andpad) を **Spring Boot 3 + GraphQL + Next.js 15** に移植したリポジトリです。

## 構成

| パス | 内容 |
|------|------|
| `backend/` | Spring Boot 3.5 · Java 21 · JDBC · Spring GraphQL |
| `frontend/` | Next.js 15（Go 版と同一 UI、API を Java に向ける） |
| `graphql/schema.graphql` | GraphQL スキーマ（Go 版と共有） |
| `docker-compose.yml` | PostgreSQL + API + Web |

## クイックスタート（ローカル）

### 1. PostgreSQL

```powershell
cd C:\devlop\andpad_j
docker compose up -d db
```

Postgres: `localhost:5434` / user `andpad` / password `andpad` / db `andpad`

### 2. API（Spring Boot）

```powershell
cd backend
$env:DATABASE_URL="jdbc:postgresql://localhost:5434/andpad"
$env:DB_USER="andpad"
$env:DB_PASSWORD="andpad"
$env:JWT_SECRET="dev-local-secret-minimum-32-characters"
.\gradlew.bat bootRun
```

- GraphQL: http://localhost:8080/graphql
- GraphiQL: http://localhost:8080/graphiql
- Health: http://localhost:8080/health

Go 版 `andpad` が既に :8080 を使っている場合は `$env:SERVER_PORT="8082"` を追加し、フロントは `API_URL=http://localhost:8082` で起動してください。

### 3. Web（Next.js）

```powershell
cd frontend
npm install
npm run dev
```

http://localhost:3000 — デモログイン: `demo@sakura-dental.jp` / `demo1234`

### まとめて起動

```powershell
npm install
npm run install:all
npm run dev          # API :8080 + Web :3000
npm run dev:java     # Go版と8080競合時 — API :8082 + Web(API_URL=8082)
```

## Docker 一括

```powershell
docker compose up --build
```

Web: http://localhost:3002 · API: http://localhost:8083

（ローカルで Go 版が :8080 / :3000 を使用中でも競合しません）

## Go 版との互換

| 項目 | 互換 |
|------|------|
| GraphQL スキーマ | 同一 SDL |
| Flyway マイグレーション | Go 版 001–010 を V001–V010 として移植 |
| JWT クレーム | `uid`, `oid`, `role`, `email`, `name` |
| 認証 Cookie | `dv_token` |
| REST 認証 | `POST /auth/login`, `/auth/register`, `/auth/logout` |
| テナント | `org_id` 列 + JWT（スキーマ分離なし） |

## 実装状況

**実装済み（JDBC + サービス層）**

- 認証・セッション・組織設定
- SaaS モジュール ON/OFF
- 施工案件・モジュール記録
- 予算・原価・ダッシュボード・承認
- Analytics / API連携 / BIM（JDBC 実装）
- 学習コンテンツ（動画10本・講師3名・学習パス6本 — JDBC + DemoCatalog）
- 学習エンゲージメント（進捗・ノート・ブックマーク・クイズ — JDBC）
- DX / CRM / 勤怠 / 契約（基本 CRUD）

**スタブ（一部未実装）**

- 修了証の自動発行（パス完了時）
- AI チャット / RAG — 今後 OpenAI 連携
- Analytics インサイト — ルールベース要約（OpenAI 未設定時）

## 環境変数

| 変数 | 説明 |
|------|------|
| `DATABASE_URL` | `jdbc:postgresql://...` または `postgresql://...`（Railway 形式可） |
| `JWT_SECRET` | HS256 秘密鍵（Go 版と同形式・生文字列） |
| `OPENAI_API_KEY` | 未設定時は AI 機能スタブ |

## 開発

```powershell
cd backend
.\gradlew.bat compileJava
.\gradlew.bat test
.\gradlew.bat bootRun
```

Testcontainers で Postgres を起動し、`/health`・ログイン・GraphQL を検証します。

GraphQL スキーマ変更後は `frontend` で `npm run codegen` を実行してください。
"# andpad_j" 
