# Dependency 가이드

## 프레임워크와 빌드 도구
Spring Boot 4 이상의 프레임워크를 기본으로 요구하며, 빌드 도구는 Gradle을 사용한다.

## 표준 Dependency

### 아키텍처 필수
webmvc, jdbc를 사용하며, EntityId 구현을 위한 ULID 라이브러리를 지원한다.

### 데이터베이스
PostgreSQL을 기본 사양으로 하며, 인프라환경에 따라 자유롭게 선정할 수 있다. 테스트를 위해 TestContainer를 사용하므로 적절한 테스트 라이브러리를 설정해주어야 한다.

### 도메인 모듈
Spring Modulith 2 이상을 사용한다. jmolecules 사양을 추가할 수 있다.

### 테스트
슬라이스 테스트를 지원할 관련 라이브러리들이다. `slice-testing-skill.md` 참고

### 문서화
슬라이스 테스트의 Endpoint 를 지원하는 라이브러리로 Spring Rest Docs를 사용한다.

### 개발도구
spring boot의 기본 devtools와 SQL 로깅용으로 p6spy을 사용한다, 초기 infra구성이 미비한 경우 Docker 지원 라이브러리를 사용할 수 있다. 

### 보안
Spring Security를 사용하며, Jwon Web Token 지원을 기본으로 한다.

### 캐시 
적절한 캐시 라이브러리를 선정하되, Spring Boot에서 지원하는 것을 사용한다. (Redis, Valkey 등)

## 작성 예시 

```groovy
dependencies {
	// 아키텍처 필수
	implementation 'org.springframework.boot:spring-boot-starter-webmvc'
	implementation 'org.springframework.boot:spring-boot-starter-jdbc'
	implementation 'com.github.f4b6a3:ulid-creator:5.2.3' // 엔티티아이디용
	// 데이터베이스 
	runtimeOnly 'org.postgresql:postgresql'
	// 도메인 모듈  
	implementation 'org.springframework.modulith:spring-modulith-starter-jdbc'
	implementation 'org.springframework.modulith:spring-modulith-starter-insight' // actuator 포함
	testImplementation 'org.springframework.modulith:spring-modulith-starter-test'
	// 테스트
	testImplementation 'org.springframework.boot:spring-boot-starter-jdbc-test'
	testImplementation 'org.springframework.boot:spring-boot-starter-webmvc-test'
	testImplementation 'org.springframework.boot:spring-boot-testcontainers'
	testImplementation 'org.testcontainers:testcontainers-junit-jupiter'
	testImplementation 'org.testcontainers:testcontainers-postgresql' // 데이터베이스에 맞춰서
	testRuntimeOnly 'org.junit.platform:junit-platform-launcher'
	// 문서화 
	testImplementation 'org.springframework.boot:spring-boot-starter-restdocs'
	testImplementation 'org.springframework.restdocs:spring-restdocs-mockmvc'
	asciidoctorExtensions 'org.springframework.restdocs:spring-restdocs-asciidoctor:3.0.2'
	asciidoctorExtensions 'org.asciidoctor:asciidoctorj:2.5.13'
	// 개발 도구
	implementation 'com.github.gavlyukovskiy:p6spy-spring-boot-starter:2.0.1'
	developmentOnly 'org.springframework.boot:spring-boot-devtools'
	developmentOnly 'org.springframework.boot:spring-boot-docker-compose'
	// // 시큐리티 체인 and jwt (option)
	// implementation 'org.springframework.boot:spring-boot-starter-security'
	// testImplementation 'org.springframework.security:spring-security-test'
	// implementation 'io.jsonwebtoken:jjwt-api:0.12.6'
	// runtimeOnly 'io.jsonwebtoken:jjwt-impl:0.12.6'
	// runtimeOnly 'io.jsonwebtoken:jjwt-jackson:0.12.6'
	// // 캐시 (option)
	// implementation 'org.springframework.boot:spring-boot-starter-data-redis'
	// implementation 'org.springframework.boot:spring-boot-starter-cache'
	// testImplementation 'org.springframework.boot:spring-boot-starter-data-redis-test'

}
```