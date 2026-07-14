# comment-aggregate-entity-driven Specification

## Purpose
TBD - created by archiving change refactor-ddd-layer-architecture. Update Purpose after archive.
## Requirements
### Requirement: CommentRepository returns Comment Entity
The system SHALL ensure `CommentRepository` returns `Comment` entity for all persistence operations and queries.

#### Scenario: Find comments by answer id
- **WHEN** `CommentRepository.findByAnswerId(answerId)` is invoked
- **THEN** it SHALL return a list of `Comment` entities

### Requirement: Comment Entity enforces ownership rules
The system SHALL add `update(Long userId, String content, BigDecimal score)` and `delete(Long userId)` methods to the `Comment` entity that validate the acting user is the comment author.

#### Scenario: User updates own comment
- **WHEN** `comment.update(userId, "new content", score)` is invoked for a comment authored by `userId`
- **THEN** the content and score SHALL be updated

#### Scenario: User attempts to update another user's comment
- **WHEN** `comment.update(otherUserId, "new content", score)` is invoked for a comment not authored by `otherUserId`
- **THEN** it SHALL throw a forbidden exception

### Requirement: Comment Entity prevents duplicate comments per answer
The system SHALL provide a `Comment.create(...)` factory that accepts a flag indicating whether the user has already commented on the answer and rejects duplicates.

#### Scenario: User comments twice on same answer
- **WHEN** a second comment creation is attempted for the same user and answer
- **THEN** it SHALL throw a bad request exception

### Requirement: CommentDomainService is removed
The system SHALL delete `CommentDomainService` and `CommentDomainServiceImpl` after migrating their behavior into the `Comment` entity and `CommentAppServiceImpl`.

#### Scenario: Add comment through application service
- **WHEN** `CommentAppServiceImpl.addComment(userId, answerId, content, score)` is invoked
- **THEN** it SHALL check for existing comments, create a `Comment` entity, and persist it through `CommentRepository`

### Requirement: CommentVO is removed or renamed
The system SHALL delete `CommentVO` from `domain.model.vo`; if the API response still needs a dedicated shape, it SHALL be defined as an application-layer result object.

#### Scenario: Compile project after migration
- **WHEN** the project is compiled
- **THEN** no source file in the domain layer SHALL reference `CommentVO`

