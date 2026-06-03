# Spring REST Docs & Asciidoctor 작성 가이드

## 1. 개요
Spring REST Docs는 테스트 기반의 API 문서화 도구입니다. 실제 테스트 케이스를 통과한 요청과 응답만을 문서로 생성하므로, 코드와 문서 사이의 불일치를 원천적으로 방지합니다. 본 프로젝트는 Asciidoctor를 사용하여 마크업 기반의 정적 문서를 생성합니다.

## 2. Build Configuration (gradle)

`build.gradle`에서 다음 설정을 통해 REST Docs 및 Asciidoctor가 연동됩니다.

-   플러그인: `org.asciidoctor.jvm.convert` 플러그인 사용.
-   snippetsDir: 테스트를 통해 생성된 조각(snippet)들이 저장되는 위치 (`build/generated-snippets`).
-   Task 연동: `asciidoctor` 태스크 실행 전 반드시 `test` 태스크가 먼저 실행되어 최신 스니펫을 확보합니다.

```gradle
// 버전이 안맞으면 동작하지 않을 수도 있으니 필수 명시
plugins {
	id 'org.asciidoctor.jvm.convert' version '4.0.4'
}
configurations {
	asciidoctorExtensions
}
ext {
	snippetsDir = file('build/generated-snippets')
}
dependencies {
    // rest docs
	testImplementation 'org.springframework.boot:spring-boot-starter-restdocs'
	testImplementation 'org.springframework.restdocs:spring-restdocs-mockmvc'
	asciidoctorExtensions 'org.springframework.restdocs:spring-restdocs-asciidoctor:3.0.2'
	asciidoctorExtensions 'org.asciidoctor:asciidoctorj:2.5.13'
}

tasks.named("asciidoctor") {
    dependsOn test
    inputs.dir snippetsDir
    configurations "asciidoctorExtensions" 

    sourceDir = file('src/docs/asciidoc')
    baseDirFollowsSourceDir() // 인클루드 경로 추적 활성화
    outputDir = file('build/api-spec')

    attributes (
        'snippets': snippetsDir,
        'toc': 'left',
        'icons': 'font'
    )
}
```

## 3. 문서 구조 (src/docs/asciidoc)

-   `index.adoc`: 문서의 진입점. 전체 구조와 공통 가이드를 포함합니다.
-   `modules/`: 도메인별 API 명세서 (예: `user-sign.adoc`, `attach-file.adoc`).
-   `guides/`: 아키텍처 명세나 공통 정책 가이드.
-   `images/`: 다이어그램 및 정적 이미지 파일.

## 4. 테스트 코드 작성 (Snippet 생성)

`slice-testing-skill.md`의 Endpoint 테스트 섹션을 참고하여 작성합니다. 핵심은 `MockMvcTester`와 `document()` 함수의 활용입니다.

```java
@Test
void api_documentation_test() {
    // When
    var result = mvc.get().uri("/api/my-feature/list").exchange();

    // Then & Document (스니펫 생성)
    result.assertThat().apply(document("my-snippet-id", // build/generated-snippets/my-snippet-id/ 경로 생성
            queryParameters(...),
            responseFields(...)
    ));
}
```

## 5. Asciidoc에서 Snippet 사용

생성된 스니펫을 `.adoc` 파일에 포함할 때는 `include` 지시어를 사용합니다.

```asciidoc
=== API 이름
이 API는 ...을 수행합니다.

==== Request
include::{snippets}/my-snippet-id/http-request.adoc[]

==== Response
include::{snippets}/my-snippet-id/http-response.adoc[]

==== Response Fields
include::{snippets}/my-snippet-id/response-fields.adoc[]
```

## 6. 문서 생성 및 확인

1.  빌드: 다음 명령어를 통해 테스트 수행 및 문서를 생성합니다.
    ```bash
    ./gradlew clean asciidoctor
    ```
2.  결과물: `build/api-spec/` 폴더에 `index.html` 파일이 생성됩니다.
3.  팁: 실시간으로 문서를 확인하며 작성하고 싶다면, `asciidoctor` 태스크를 watch 모드로 실행하거나 IDE의 AsciiDoc 플러그인을 활용하십시오.

## 7. 주요 컨벤션
-   스니펫 ID: 케밥 케이스(`kebab-case`)를 사용합니다 (예: `user-login-success`).
-   공통 필드: 모든 API에 공통으로 들어가는 필드(code, message 등)는 별도의 공통 스니펫을 만들거나 재사용 가능한 필드 설명 리스트를 관리합니다.
-   다이어그램: 시퀀스 다이어그램 등이 필요한 경우 PlantUML을 적극 활용합니다.
