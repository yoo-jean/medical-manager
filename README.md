# MedicalManager

## 프로젝트 개요

의사가 환자 정보를 입력하고, 환자의 병변 분석을 위한 이미지 파일을
업로드하고 조회/삭제할 수 있는 시스템입니다.\
이미지는 서버 파일 시스템에 저장되고, DB에는 이미지의 상대 경로만
저장합니다.

## 실행 방법
```bash
./gradlew bootRun
```
- Swagger UI: [http://localhost:8080/swagger-ui.html](http://localhost:8080/swagger-ui.html)
- H2 Console: [http://localhost:8080/h2-console](http://localhost:8080/h2-console)
  (JDBC URL: `jdbc:h2:mem:frienddb`, User: `sa`, Password: *(빈값)*)

------------------------------------------------------------------------

## 기술 스택

-   Java 17
-   Spring Boot 3.5.7
-   Spring Web
-   Spring Data JPA
-   H2 Database
-   Lombok

------------------------------------------------------------------------

## 📁 파일 저장 경로 설정

`application.properties`:

    file.upload-dir=uploads

애플리케이션 실행 시 프로젝트 루트 경로에 `uploads/` 폴더가 자동
생성됩니다.

이미지는 아래 경로 구조로 저장됩니다.

    uploads/patients/{id}/{timestamp}.{ext}

ex)

    uploads/patients/1/20250120_210030.png

------------------------------------------------------------------------

## 문제 해결 방법

### 1. 2단계 저장 구조 설계

1단계 → 환자 기본 정보 저장\
2단계 → 이미지 업로드 완료 시 조회 가능

이를 구현하기 위해 `Patient` 엔티티에 다음 필드를 추가했습니다

-   `imagePath` : 이미지 상대 경로
-   `imageUploaded` : 이미지 업로드 완료 여부

조회 시에는 `imageUploaded = true` 인 환자만 반환하여\
요구사항인 "1단계 저장 직후에는 조회되지 않고, 이미지 업로드 이후에만
조회 가능" 조건을 만족했습니다.

------------------------------------------------------------------------

### 2. 파일 시스템 기반 이미지 저장

이미지를 DB가 아닌 파일 시스템에 저장하여 성능과 유지보수성을
확보했습니다.

-   업로드 루트: `uploads/`
-   환자 폴더: `patients/{id}/`
-   파일명: `yyyyMMdd_HHmmss.ext`

DB에는 상대 경로만 저장

이를 통해 환경이 바뀌어도 파일 저장 경로만 수정하면 됩니다.

------------------------------------------------------------------------

### 3. 이미지 조회 API

브라우저에서 직접 URL을 입력했을 때\
이미지가 바로 표시되도록 다음과 같은 API를 만들었습니다:

    GET /api/patients/{id}/image

동작 방식:

1.  DB에서 환자 조회
2.  저장된 `imagePath` 확인
3.  파일 시스템에서 Resource로 로딩
4.  HTTP Response로 이미지 스트리밍

이미지 URL은 기본 정보 조회 시 함께 내려줍니다.

    http://localhost:8080/api/patients/2/image

------------------------------------------------------------------------

### 4. 예외 처리

다음 상황을 검증 및 예외 처리:

-   존재하지 않는 환자 ID
-   이미지 미업로드 상태에서 조회 시도
-   png/jpg 외 형식 업로드
-   이미지 없는 환자의 이미지 조회
-   이미 업로드한 환자의 재업로드 시도

현재는 `IllegalArgumentException` 기반이며,\
추후 `@RestControllerAdvice` 로 공통 에러 포맷을 확장하기 쉽게
설계했습니다.

------------------------------------------------------------------------

## 📡 API 명세

### 1) 환자 기본 정보 저장 

**POST /api/patients**

Request Body:

``` json
{
  "name": "홍길동",
  "age": 30,
  "gender": "MALE",
  "hasDisease": true
}
```

Response:

``` json
{
  "patientId": 1
}
```

------------------------------------------------------------------------

### 2) 이미지 업로드 

**POST /api/patients/{id}/image**

Content-Type: multipart/form-data

Response:

``` json
{
  "patientId": 1,
  "imageUrl": "http://localhost:8080/api/patients/1/image"
}
```

------------------------------------------------------------------------

### 3) 환자 기본 정보 조회

**GET /api/patients/{id}**

이미지 업로드가 완료된 경우만 조회 가능.

Response:

``` json
{
  "id": 1,
  "name": "홍길동",
  "age": 30,
  "gender": "MALE",
  "hasDisease": true,
  "imageUrl": "http://localhost:8080/api/patients/1/image"
}
```

------------------------------------------------------------------------

### 4) 이미지 조회

**GET /api/patients/{id}/image**

→ 브라우저에서 직접 이미지 표시됨.

------------------------------------------------------------------------

### 5) 환자 삭제

**DELETE /api/patients/{id}**

이미지 파일 + DB 데이터 모두 삭제됨.

------------------------------------------------------------------------

