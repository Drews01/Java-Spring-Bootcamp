# Log Updates

## 2025-12-23

- Added a centralized response builder in src/main/java/com/example/demo/base/ResponseUtil.java to encapsulate common `ResponseEntity<ApiResponse<...>>` patterns (OK/Created/error variants) and keep controllers focused on business logic.
- Refactored src/main/java/com/example/demo/controller/AuthController.java to delegate registration/login responses to `ResponseUtil`, removing duplicated status/message wiring.
- Updated src/main/java/com/example/demo/controller/UserController.java so both list and create endpoints now return the standardized `ApiResponse` envelope via `ResponseUtil`.

