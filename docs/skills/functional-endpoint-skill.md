# Functional Endpoint Creation Skill

This skill provides guidelines for implementing Spring WebMvc functional endpoints (Router and Handler) instead of traditional `@Controller` classes.

## 1. Architectural Pattern

Functional endpoints are split into two main components:
- **Router**: Defines the API paths and maps them to handler methods.
- **Handler**: Contains the request processing logic, interacting with services.

## 2. Router Implementation Guidelines

- **Class Annotation**: Use `@Configuration`.
- **Bean Definition**: Define a `@Bean` of type `RouterFunction<ServerResponse>`.
- **Builder Usage**: Use `RouterFunctions.route()` to build the routes.
- **Path Nesting**: Use `.path("/api/v1/...", builder -> ...)` for common path prefixes.
- **Filters**: Apply common filters (like `defaultResponse()`) at the end of the route definition.
- **Method Mapping**: Map HTTP methods using method references (e.g., `handler::getSomething`).

### Example Router
```java
@Configuration
public class MyRouter {
    @Bean
    public RouterFunction<ServerResponse> route(MyHandler handler) {
        return RouterFunctions.route()
                .path("/api/my-feature", builder -> builder
                        .GET("/list", handler::getList)
                        .POST("/create", handler::createItem))
                .filter(defaultResponse()) // Apply standard response wrapper
                .build();
    }
}
```

## 3. Handler Implementation Guidelines

- **Class Annotation**: Use `@Component` and `@RequiredArgsConstructor`.
- **Method Signature**: Methods must accept `ServerRequest` and return `ServerResponse`.
- **Data Extraction**:
    - **Path Variables**: `request.pathVariable("name")`
    - **Query Parameters**: `request.param("name").orElse("default")`
    - **Request Body**: `request.body(MyDto.class)`
    - **Attributes**: `request.attribute("key")` (often used for security context/userId)
- **Service Interaction**: Inject services via constructor and call them within handler methods.
- **Response**: Return `ServerResponse.ok().body(data)` or other appropriate status codes.

### Example Handler
```java
@Component
@RequiredArgsConstructor
public class MyHandler {
    private final MyService myService;

    public ServerResponse getList(ServerRequest request) {
        var data = myService.findAll();
        return ServerResponse.ok().body(data);
    }

    public ServerResponse createItem(ServerRequest request) throws ServletException, IOException {
        MyDto dto = request.body(MyDto.class);
        myService.save(dto);
        return ServerResponse.ok().build();
    }
}
```

## 4. Key Considerations

- **Filter Chains**: Functional endpoints make it easy to apply cross-cutting concerns (logging, auth, response wrapping) via `.filter()`.
- **Validation**: Manual validation might be needed if not using `@Valid` as in Controllers.
- **Error Handling**: Exceptions can be handled globally or within the handler/filter.
- **Testing**: Use `MockMvcTester` for testing functional routes.
