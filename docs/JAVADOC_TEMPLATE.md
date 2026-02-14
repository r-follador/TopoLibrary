# Javadoc Template (TopoLibrary)

Use this template for all public APIs.

```java
/**
 * <One-line summary in present tense.>
 *
 * <Optional details: behavior, assumptions, coordinate systems, units.>
 *
 * @param <name> <what it represents, including units/format when relevant>
 * @return <what is returned, including units/format when relevant>
 * @throws <ExceptionType> <when/why it is thrown>
 *
 * <Optional usage snippet>
 * <pre>{@code
 * // Example
 * }</pre>
 */
```

## Conventions

- Document units explicitly (`meters`, `degrees`, `seconds`, cell counts).
- Describe coordinate orientation when arrays are involved (`data[x][y]` as `lon/lat`).
- For methods that may load external data, document expected source layout and failure modes.
- For overloaded methods, keep one canonical method fully documented and make overload docs short references.
