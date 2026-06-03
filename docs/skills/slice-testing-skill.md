# Slice Testing Skill (Spring Boot 4+, Java 25)

이 스킬은 도메인 모듈 구성 가이드에 따라 `model`, `application`, `infra.adapter`, `endpoint` 계층별로 최적화된 슬라이스 테스트 작성 가이드라인을 제공합니다.

- 도메인 모듈 구성 가이드: `domain-module-skill.md` 참고


## 1. 공통 환경 및 원칙
- Java 25+: 최신 자바 문법(Pattern Matching, Record 등)을 활용합니다.
- Spring Boot 4.0+: 최신 스프링 부트 기능을 활용합니다.
- Jackson 3.0+: `tools.jackson.databind.ObjectMapper`를 사용하며, Java 25의 `Instant` 등을 기본 지원합니다.
- AssertJ: 가독성 높은 검증을 위해 AssertJ를 기본 Assertion 라이브러리로 사용합니다.

### 슬라이스 테스트를 작성하지 않는 계층
- activity
- activity.out (단, ArFactory 인터페이스 내에 정의된 default method 복원 로직은 프레임워크 없는 'Pure Java Test' 형태로 단위 테스트를 필히 작성해야 합니다.)
- infra.activity

---

## 2. 계층별 테스트 전략

### A. Model (Pure Java Test)
도메인 모델은 인프라나 프레임워크 지식이 없는 순수한 자바 코드여야 하며, 테스트 역시 프레임워크 없이 빠르게 수행되어야 합니다.

- 대상: `model` 패키지의 Entity, VO, Policy.
- 방식: JUnit 5 단독 사용 (No `@SpringBootTest`, No `@ExtendWith`).
- 핵심: 비즈니스 로직의 정확성, 불변성(Immutability), 캡슐화 검증.

#### 작성 예제
```java
class MyModelTest {
    @Test
    @DisplayName("모델은 비즈니스 규칙에 따라 상태를 변경하고 불변성을 유지해야 한다")
    void model_logic_test() {
        // Given: 도메인 객체 생성/복원
        MyEntity entity = MyEntity.reconstitute(...);

        // When: 비즈니스 로직 실행
        entity.doSomething(policy);

        // Then: 상태 변화 및 결과 검증
        assertThat(entity.getStatus()).isEqualTo(EXPECTED);
    }
}
```

### B. Application (Mockito-based Mock Test)
서비스(Application) 계층은 여러 Activity를 조율하는 역할을 하며, 협력 객체들을 Mocking하여 비즈니스 흐름을 테스트합니다.

- 대상: `application` 패키지의 Service.
- 방식: `@ExtendWith(MockitoExtension.class)`.
- 핵심: 의존성 주입(`@InjectMocks`, `@Mock`), 협력 객체와의 상호작용(`verify`), 오케스트레이션 논리 검증.

#### 작성 예제
```java
@ExtendWith(MockitoExtension.class)
class MyServiceTest {
    @Mock private LoadActivity loadActivity;
    @Mock private SaveActivity saveActivity;
    
    @InjectMocks private MyService service;

    @Test
    void service_orchestration_test() {
        // Given
        given(loadActivity.load(any())).willReturn(mockData);
        
        // When
        service.executeUseCase(input);
        
        // Then
        verify(saveActivity).save(eq(expectedId), any());
    }
}
```

### C. Infra.Adapter (Testcontainers & JdbcClient Test)
인프라 어댑터는 실제 DB 기술과의 접점을 테스트하며, 가벼운 데이터베이스 환경을 위해 Testcontainers를 사용합니다.

- 대상: `infra.adapter` 패키지의 Adapter.
- 방식: `@JdbcTest` + `@Testcontainers` + `@Import(Adapter.class)`.
- 핵심: `JdbcClient`를 통한 SQL 실행 결과 검증, DB 제약 조건 확인.
- 환경: `PostgreSQLContainer` 사용.

#### 작성 예제
```java
import org.springframework.boot.jdbc.test.autoconfigure.JdbcTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.postgresql.PostgreSQLContainer;

@Testcontainers
@JdbcTest
@Import(MyJdbcAdapter.class)
class MyJdbcAdapterTest {
    @Container
    @ServiceConnection
    static PostgreSQLContainer postgres = new PostgreSQLContainer("postgres:18.1-alpine");


    @Autowired private MyJdbcAdapter adapter;

    @Test
    void adapter_persistence_test() {
        // When
        adapter.save(data);
        Optional<Data> found = adapter.findById(id);
        
        // Then
        assertThat(found).isPresent();
    }
}
```

### D. Endpoint (MockMvcTester & Spring Rest Docs)
컨트롤러(Endpoint) 테스트는 API 계약을 검증하고 문서를 자동 생성합니다. 이 프로젝트에서는 전통적인 `@Controller` 대신 `Router`와 `Handler`를 사용하는 Functional Endpoint 패턴을 지향합니다.

- 대상: `endpoint` 또는 `api` 패키지의 Router 및 Handler.
- 방식: `@WebMvcTest({MyHandler.class, MyRouter.class})` + `MockMvcTester` + `Spring Rest Docs`.
- 핵심: `MockMvcTester`를 통한 유연한 검증, API 문서화, 공통 응답 구조(`Response.Success/Fail`) 확인.
- 상세 가이드: 스니펫 생성 및 Asciidoctor 연동 방법은 `docs/rest-docs-skill.md`를 참조하십시오.
- 특징: `MockMvc` 직접 주입 대신 `MockMvcTester`를 사용하여 AssertJ 스타일의 체이닝 검증을 수행합니다.

#### 작성 예제
```java
@WebMvcTest({MyHandler.class, MyRouter.class})
@AutoConfigureRestDocs
@Import({RestDocsConfiguration.class, DefaultEndpointHandler.class})
class MyEndpointTest {
    @Autowired
    private MockMvcTester mvc;

    @MockitoBean
    private MyService myService;

    @Test
    @DisplayName("API 호출 시 서비스를 통해 데이터를 처리하고 공통 응답 규격에 맞춰 반환해야 한다")
    void api_documentation_test() {
        // Given
        given(myService.getData(any())).willReturn(mockData);

        // When
        var result = mvc.get().uri("/api/my-feature/list")
                .param("pageNo", "1")
                .requestAttr("userId", "user_01")
                .exchange();

        // Then & Document
        result.assertThat().apply(document("my-feature-list",
                queryParameters(parameterWithName("pageNo").description("페이지 번호")),
                responseFields(
                    fieldWithPath("code").description("응답 코드"),
                    fieldWithPath("data.content").description("실제 데이터 목록"),
                    fieldWithPath("message").description("응답 메시지").optional()
                )
        ));

        assertThat(result).hasStatusOk();
        assertThat(result).bodyJson()
                .extractingPath("$.data.content[0].name").isEqualTo("Expected Name");
    }
}
```

#### RestDocsConfiguration.java 작성 예시

```java
@TestConfiguration
public class RestDocsConfiguration {

    @Bean
    public RestDocsMockMvcConfigurationCustomizer restDocsMockMvcConfigurationCustomizer() {
        return configurer -> configurer.operationPreprocessors()
                .withRequestDefaults(prettyPrint())
                .withResponseDefaults(prettyPrint());
    }
}
```

---

## 3. 요약 및 주의사항
1. Model: 최대한 가볍고 순수하게 유지하십시오.
2. Application: 실제 Activity 구현체에 의존하지 말고 인터페이스(Activity)를 Mocking하십시오.
3. Infra.Adapter: `Testcontainers`를 통해 실제 쿼리 작동 여부를 확인하십시오.
4. Endpoint: `MockMvcTester`와 `RestDocs`를 조합하여 검증과 문서화를 동시에 달성하십시오. 응답은 `Response.Success/Fail` 구조를 따르는지 확인하십시오.
