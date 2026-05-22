# Contributing

## Local build

```bash
mvn -B clean verify
```

## Running tests

```bash
mvn -B test
```

## Coding style

- Java 17 features are welcome (records, sealed classes, pattern matching).
- No Lombok.
- Follow existing code conventions (4-space indent, no wildcard imports).
- Keep classes small and focused.

## Pull requests

- One logical change per PR.
- Ensure `mvn -B clean verify` passes before submitting.
- Write or update tests for any new behavior.
- Keep commit messages concise and descriptive.
