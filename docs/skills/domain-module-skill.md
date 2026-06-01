
# Domain Module 구성 가이드 (Spring Modulith 지향)

## Domain 계층(package)

### application
모듈의 외부와 소통하는 최상위 조율자입니다. 유스케이스의 입출력을 담당하며 비즈니스 흐름을 관장합니다.

### activity
유스케이스가 요구하는 고수준 기술 명령의 명세입니다. Service는 이 인터페이스에만 의존하여 기술적 실체로부터 격리됩니다.

### model
비즈니스의 심장입니다. 인프라 지식이 전혀 없으며, `BaseEntity`를 통해 도메인의 식별성과 이력(Auditing)을 관리합니다.

### activity
Activity 구현체가 행위를 지시하기 위해 선언하는 하위 기술 포트입니다. 기술 환경과의 접점을 비즈니스 언어로 정의합니다.

### infra.activity
`activity` 계층의 명세를 구현합니다. 캐싱, 재시도, 다중 포트 조합 등 기술적 실행 시나리오를 조율합니다. Activity 구현 클래스는 suffix를 'Activity' 대신 'Actions'를 사용합니다. 

### infra.adapter
`activity.out`의 포트를 구현합니다. 특정 인프라 기술을 써서 데이터를 AR로 Reconstitute(복원)하는 책임을 집니다.

### endpoint 
외부 요청을 `application` 계층으로 변환하여 전달하는 단순 입구입니다.

---

## Package 구성 요약표

| 계층(package)    | 주요 컴포넌트                                                      |
|:---------------|:-------------------------------------------------------------|
| application    | Service, DTO, Policy Implementation, Event listener          |
| activity       | Activity Interface                                           |
| model          | AR(extends BaseEntity), VO, Policy interface                 |
| activity.out   | Infra Port (Double Porting), Factory interface               |
| infra.activity | Activity Implementation                                      |
| infra.adapter  | Adapter (Port Impl), Repository, Factory Impl                 |
| endpoint       | REST Controller, OpenAPI                                     |

