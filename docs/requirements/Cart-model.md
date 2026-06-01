# 장바구니 

## 요구사항

* 장바구니 담기: 고객이 마음에 드는 상품의 옵션과 수량을 선택해서 장바구니에 보관할 수 있어야 합니다. 이미 담은 상품을 또 담으면 개수가 합쳐져야 합니다.
* 장바구니 확인: 고객이 언제든지 자신이 담은 상품 목록, 이미지, 옵션, 가격을 한눈에 볼 수 있어야 합니다.
* 수량 및 옵션 변경: 고객이 장바구니 화면에서 바로 상품 개수를 늘리거나 줄일 수 있어야 하고, 변경된 수량에 따라 금액이 실시간으로 바뀌어야 합니다.
* 상품 제외: 마음이 바뀐 고객이 특정 상품을 장바구니에서 빼거나, 한 번에 모든 상품을 비울 수 있어야 합니다.
* 예상 결제 금액 안내: 전체 상품 금액, 할인 금액, 배송비, 그리고 고객이 최종적으로 결제해야 할 총 금액이 명확하게 표시되어야 합니다.
* 장바구니 전환율 분석: 장바구니에 담긴 상품이 실제 구매로 이어지는 비율(구매 전환율)과 이탈하는 비율을 측정하고, 어떤 상품이 장바구니에 오래 머무는지 추적할 수 있어야 합니다.
* 리타게팅 마케팅 연동: 상품을 장바구니에 담아두고 일정 시간 동안 구매하지 않은 고객에게 리마인드 알림톡이나 할인 쿠폰 푸시를 발송할 수 있는 기반 데이터를 제공해야 합니다.
* 삭제 및 재추가 행동 패턴 추적: 고객이 장바구니에서 특정 상품을 삭제한 후 다른 대체 상품을 추가하는지(브랜드 전환, 가격 비교 등), 아니면 동일 상품을 나중에 다시 추가하는지 행동 흐름을 파악할 수 있어야 합니다.
* 개별 맞춤형 추천 상품 노출: 장바구니 화면 하단에 현재 담은 상품과 함께 구매하면 좋은 연관 상품이나, 삭제한 상품의 대체재 성격을 가진 기획전 상품을 추천하여 추가 구매를 유도해야 합니다.
* 장바구니 담기 데이터 기반의 기획전 기획: 구매까지 가지 않더라도 '장바구니에 가장 많이 담긴 상품 리스트'를 추출하여 메인 페이지나 프로모션 배너(예: "지금 가장 고민 중인 인기 아이템")에 활용할 수 있어야 합니다.

## Usecase

```mermaid
graph LR
    Customer["고객"]
    Marketer["마케터"]

    subgraph ShoppingCartSystem ["장바구니 시스템"]
        UC_AddToCart["장바구니 담기<br>(중복 시 수량 합산)"]
        UC_ViewCart["장바구니 확인<br>(목록, 이미지, 금액 확인)"]
        UC_UpdateQuantity["수량 및 옵션 변경"]
        UC_RemoveItem["상품 제외<br>(선택 삭제 / 전체 비우기)"]
        UC_CalculateTotal["예상 결제 금액 안내<br>(상품가, 할인, 배송비 총액)"]
        
        UC_AnalyzeConversion["장바구니 전환율 분석"]
        UC_RetargetingData["리타게팅 마케팅 연동"]
        UC_TrackBehavior["삭제 및 재추가 행동 패턴 추적"]
        UC_RecommendProducts["개별 맞춤형 추천 상품 노출"]
        UC_PlanPromotion["장바구니 데이터 기반 기획전 기획"]
    end

    Customer --> UC_AddToCart
    Customer --> UC_ViewCart
    Customer --> UC_UpdateQuantity
    Customer --> UC_RemoveItem

    UC_ViewCart -.->|include| UC_CalculateTotal
    UC_ViewCart -.->|include| UC_RecommendProducts

    Marketer --> UC_AnalyzeConversion
    Marketer --> UC_RetargetingData
    Marketer --> UC_TrackBehavior
    Marketer --> UC_PlanPromotion
```

## Sequence 

```mermaid
sequenceDiagram
    actor Customer as 고객
    actor Marketer as 마케터
    participant UI as 장바구니 화면
    participant Cart as 장바구니 서비스
    participant DB as 데이터베이스
    participant Marketing as 마케팅 솔루션

    %% 1. 장바구니 담기 및 분석 데이터 적재
    rect rgb(240, 240, 240)
        Note right of Customer: 1. 장바구니 담기 및 패턴 추적
        Customer->>UI: 상품 담기 클릭 (옵션, 수량)
        UI->>Cart: 장바구니 추가 요청
        Cart->>DB: 기존 상품 확인 및 수량 합산/저장
        Cart->>DB: 장바구니 담기 행동 로그 저장
        Cart-->>UI: 담기 완료 알림
    end

    %% 2. 장바구니 조회 및 화면 노출
    rect rgb(230, 240, 250)
        Note right of Customer: 2. 장바구니 확인 및 추천 노출
        Customer->>UI: 장바구니 페이지 진입
        UI->>Cart: 장바구니 목록 및 총 금액 요청
        Cart->>DB: 상품 목록, 옵션, 이미지, 가격 조회
        Cart->>Cart: 예상 결제 금액 계산 (상품 총액 + 배송비 - 할인)
        Cart->>Marketing: 맞춤형 추천 상품 리스트 요청
        Marketing-->>Cart: 추천 상품 데이터 반환
        Cart-->>UI: 장바구니 화면 정보 전달
        UI-->>Customer: 목록, 계산 금액, 추천 상품 노출
    end

    %% 3. 수량 변경 및 실시간 금액 계산
    rect rgb(240, 240, 240)
        Note right of Customer: 3. 수량 변경
        Customer->>UI: 수량 변경 (+/-)
        UI->>Cart: 수량 수정 요청
        Cart->>DB: 수량 업데이트
        Cart->>Cart: 예상 결제 금액 재계산
        Cart-->>UI: 변경된 금액 정보 반환
        UI-->>Customer: 실시간 합계 금액 반영
    end

    %% 4. 상품 삭제 및 행동 패턴 추적
    rect rgb(230, 240, 250)
        Note right of Customer: 4. 상품 제외 및 삭제 패턴 추적
        Customer->>UI: 특정 상품 삭제 클릭
        UI->>Cart: 상품 제외 요청
        Cart->>DB: 장바구니 내 상품 삭제
        Cart->>DB: 삭제 행동 데이터 기록 (재추가/대체재 분석용)
        Cart-->>UI: 삭제 완료 처리
    end

    %% 5. 마케터 관점의 데이터 활용
    rect rgb(250, 240, 230)
        Note right of Marketer: 5. 마케팅 분석 및 리타게팅
        Marketer->>Marketing: 전환율 및 행동 흐름 리포트 조회
        Marketing->>DB: 담기/삭제 로그 및 구매 전환 데이터 분석
        Marketing-->>Marketer: 분석 리포트 제공 (인기 상품 목록 포함)
        
        Note over DB, Marketing: 미구매 상태로 일정 시간 경과 시
        Marketing->>DB: 장바구니 방치 고객 추출
        Marketing->>Customer: 리타게팅 알림톡 및 쿠폰 푸시 발송
    end
```

## Activity

### 1. 장바구니 담기 및 분석 데이터 적재

```mermaid

graph TD
    Start([시작: 상품 담기 클릭]) --> RequestAdd[장바구니 추가 요청 수신]
    
    RequestAdd --> CheckDuplicate{이미 장바구니에<br>담긴 상품인가?}
    
    CheckDuplicate -->|Yes| MergeQuantity[기존 수량에 신규 수량 합산]
    CheckDuplicate -->|No| CreateItem[신규 장바구니 항목 생성]
    
    MergeQuantity --> SaveCart[DB: 장바구니 정보 저장]
    CreateItem --> SaveCart
    
    SaveCart --> LogBehavior[DB: 장바구니 담기 행동 로그 기록<br>마케팅 분석용 데이터 적재]
    
    LogBehavior --> SendNotification[담기 완료 알림 반환]
    SendNotification --> End([종료: 화면에 완료 메시지 노출])
```


### 2. 장바구니 조회 및 화면 노출

```mermaid
graph TD
    Start([시작: 장바구니 페이지 진입]) --> RequestData[장바구니 목록 및 총 금액 요청]
    
    %% 데이터 조회 및 계산 (병렬 처리 가능 구획)
    subgraph 데이터 조회 및 계산
        RequestData --> FetchCart[DB: 상품 목록, 옵션, 이미지, 가격 조회]
        RequestData --> FetchRecommend[Marketing: 맞춤형 추천 상품 리스트 요청]
        
        FetchCart --> CalculateTotal[예상 결제 금액 계산<br>상품 총액 + 배송비 - 할인]
    end

    CalculateTotal --> CombineData[화면 전달 데이터 합산]
    FetchRecommend --> CombineData
    
    CombineData --> RenderUI[장바구니 화면 정보 전달 및 렌더링]
    RenderUI --> Display[고객에게 목록, 계산 금액, 추천 상품 노출]
    Display --> End([종료])
```

### 3. 수량 변경 및 실시간 금액 계산

```mermaid
graph TD
    Start([시작: 수량 변경 클릭]) --> ReceiveRequest[수량 수정 요청 수신]
    
    ReceiveRequest --> UpdateDB[DB: 장바구니 상품 수량 업데이트]
    
    UpdateDB --> CalculateItems[각 상품별 총액 계산<br>수량 X 옵션가]
    CalculateItems --> CalculateTotal[전체 예상 결제 금액 계산<br>상품 총액 + 배송비 - 할인]
    
    CalculateTotal --> ReturnData[변경된 금액 정보 반환]
    ReturnData --> RefreshUI[화면에 실시간 합계 금액 반영]
    
    RefreshUI --> End([종료])
```


### 4. 상품 삭제 및 행동 패턴 추적

```mermaid
graph TD
    Start([시작: 상품 제외 클릭]) --> ReceiveRequest[상품 제외 요청 수신]

    ReceiveRequest --> DeleteCartItem[DB: 장바구니 내 해당 상품 삭제]

    DeleteCartItem --> LogDeleteBehavior[DB: 삭제 행동 데이터 기록<br>대체 상품 추가 및 흐름 분석용]

    LogDeleteBehavior --> SendResponse[삭제 완료 처리 결과 반환]
    SendResponse --> UpdateUI[화면에서 해당 상품 제거 및 금액 갱신]

    UpdateUI --> End([종료])
```


### 5. 마케터 관점의 데이터 활용

```mermaid
graph TD
    Start([시작: 마케팅 활동 개시]) --> ActionFork{마케터의 행동 유형}

    %% 경로 1: 마케터의 데이터 분석 흐름
    ActionFork -->|리포트 조회| RequestReport[전환율 및 행동 흐름 리포트 조회 요청]
    RequestReport --> AnalyzeData[DB: 담기/삭제 로그 및 구매 전환 데이터 분석]
    AnalyzeData --> ProvideReport[인기 상품 및 행동 패턴 분석 리포트 제공]
    ProvideReport --> End1([종료: 프로모션/기획전 전략 수립])

    %% 경로 2: 시스템의 자동 리타게팅 흐름 (배치/스케줄러 기반)
    ActionFork -->|스케줄러 실행| CheckIdleCart[시간 경과: 미구매 장바구니 방치 고객 추출]
    CheckIdleCart --> ExtractTarget[대상자 타게팅 및 데이터 매칭<br>이탈 상품, 쿠폰 대상 확인]
    ExtractTarget --> SendPush[고객에게 리타게팅 알림톡 및 할인 쿠폰 발송]
    SendPush --> End2([종료: 고객 재유입 유도 완료])
```


## Domain Model

```mermaid
classDiagram
    class Cart {
        - Long id
        - Long customerId
        - List~CartItem~ items
        + void addProduct(Product product, SelectedOption option, int quantity)
        + void changeItemQuantity(Long itemId, int quantity)
        + void removeItem(Long itemId)
        + void clear()
        + OrderEstimatedAmount calculateEstimatedAmount(DeliveryPolicy deliveryPolicy)
    }

    class CartItem {
        - Long id
        - Product product
        - SelectedOption option
        - int quantity
        + void addQuantity(int quantity)
        + void changeQuantity(int quantity)
        + Money calculateSubTotal()
    }

    class Product {
        - Long id
        - String name
        - Money basePrice
        - String imageUrl
    }

    class SelectedOption {
        - Long optionId
        - String optionName
        - Money additionalPrice
    }

    class Money {
        - BigDecimal amount
        + Money plus(Money money)
        + Money minus(Money money)
        + Money times(int multiplier)
    }

    class OrderEstimatedAmount {
        - Money totalProductPrice
        - Money totalDiscountPrice
        - Money deliveryFee
        - Money finalPaymentAmount
    }

    class CartBehaviorLog {
        - Long id
        - Long customerId
        - BehaviorType type
        - Long productId
        - LocalDateTime timestamp
    }

    class BehaviorType {
        <<enumeration>>
        ADD
        REMOVE
        QUANTITY_CHANGE
    }

    Cart "1" *-- "0..*" CartItem : contains
    CartItem "1" --> "1" Product : references
    CartItem "1" *-- "1" SelectedOption : holds
    CartItem ..> Money : uses
    Cart ..> OrderEstimatedAmount : creates
    CartBehaviorLog "1" --> "1" BehaviorType : categorizes
```

## Policy

* 상품 추가 유효성 검사 (Cart, CartItem)
  * 최소 담기 수량 제한: 장바구니에 담는 상품의 수량은 반드시 1개 이상이어야 합니다.
  * 옵션 매칭 확인: 동일한 상품이더라도 선택한 옵션(예: 색상, 사이즈)이 다르면 장바구니 내에서 별도의 항목으로 분리하여 생성해야 합니다.
  * 최대 담기 제한: 한 상품 또는 장바구니 전체에 담을 수 있는 최대 수량/종류 제한을 초과하는지 검증해야 합니다.


* 수량 및 옵션 변경 유효성 검사 (CartItem)
  * 수량 변경 범위 검증: 변경하려는 수량은 반드시 1개 이상이어야 하며, 시스템이 지정한 최대 주문 가능 수량을 초과할 수 없습니다.


* 금액 및 가격 계산 규칙 (Cart, CartItem, Money)
  * 개별 상품 총액 계산: 각 항목의 총 가격은 `(상품 기본가 + 옵션 추가가) * 수량`으로 정확하게 계산되어야 하며, 음수 금액이 나올 수 없습니다.
  * 예상 결제 금액 산출 일관성: 전체 상품 금액, 할인 금액, 배송비의 연산 결과가 최종 결제 금액과 일치해야 합니다. (`최종 결제 금액 = 상품 총액 - 할인 총액 + 배송비`)


* 행동 로그 기록 규칙 (CartBehaviorLog)
  * 상태 변경 동기화: 장바구니에 상품이 추가(ADD), 변경(QUANTITY_CHANGE), 삭제(REMOVE)되는 시점에 해당 비즈니스 행위와 소유자 정보를 누락 없이 마케팅 로그 데이터로 생성해야 합니다.
